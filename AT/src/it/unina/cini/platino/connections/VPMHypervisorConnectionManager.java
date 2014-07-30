package it.unina.cini.platino.connections;

import it.unina.cini.platino.db.Database;
import it.unina.cini.platino.db.DatabaseListener;
import it.unina.cini.platino.db.Hypervisor;
import it.unina.cini.platino.libvirt.HypervisorConnection;
import it.unina.cini.platino.libvirt.NetHypervisorConnection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.libvirt.LibvirtException;

/**
 * This class holds a reference to each hypervisor connection.
 * It also starts a thread which will keep polling unavailable connections at specific
 * time intervals so to notice any hypervisor returning online.
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 */
public class VPMHypervisorConnectionManager implements DatabaseListener{
	
	private static final int CONNECTION_TIMEOUT = 500;
	public static final String HYPERVISOR_CONNECTION_MANAGER = "hmanager";
	private static final String XML_NETWORK_FILEPATH = "xml_definitions/network_template.xml";
	
	private int retryTimout;
	private Timer timer; //this timer is used by the polling thread
	
	//network settings
	private final String NETWORK_NAME;
	private final String NETWORK_PREFIX;
	private final String BRIDGE_NAME;
	
	private boolean active;
	private synchronized boolean isActive(){
		return active;
	}
	
	private synchronized void setActive(boolean v){
		active = v;
	}
	
	private Object connectionsLock;
	
	private List<NetHypervisorConnection> activeConnections;
	private List<Hypervisor> offlineConnections;
	
	private Database d;
	
	// listener methods----------------------------------------------------------->
	// called when a new hypervisor is added/deleted to/from the database.
	// makes sure that saved connections are constisten
	@Override
	public void hypervisorInserted(Hypervisor h) {
		try {
			addHypervisor(h,getNetworkDescription());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void hypervisorDeleted(Hypervisor h) {
		removeHypervisor(h);
	}
	
	//<-----------------------------------------------------------------------------
	
	//utility ------------------------------------------------------------------------->
	
	private String getNetworkDescription() throws IOException{
		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(XML_NETWORK_FILEPATH);
 
		Document doc = null;
		try{
			doc = (Document) builder.build(xmlFile);
		}catch(JDOMException ex){
			throw new IOException(ex.getMessage());
		}
		
		Element rootNode = doc.getRootElement();
		rootNode.getChild("name").setText(NETWORK_NAME);
		rootNode.getChild("bridge").setAttribute("name",BRIDGE_NAME);
		
		return new XMLOutputter().outputString(doc);
	}
	
	/**
	 * Finds an hypervisor connection among active connections
	 * @param h the hypervisor we are searching relative connection
	 * @return
	 */
	private NetHypervisorConnection getActiveConnection(Hypervisor h){
		int i = 0;
		NetHypervisorConnection conn = null;
		
		
		while((conn == null) && (i<activeConnections.size())){
			if(activeConnections.get(i).getHypervisor().equals(h)){//found
				conn = activeConnections.get(i);
			}else
				i++;
		}
		
		return conn;
	}
	
	//<--------------------------------------------------------------------------------
	/**
	 * Creates a connection manager with a specified timeout in order to
	 * check if any hypervisor has returned on. 
	 * 
	 * @param retryTimeout 0 for no retry
	 * @param dbPath path to the sqlite db
	 * @throws IOException 
	 */
	public VPMHypervisorConnectionManager(int retryTimeout,String dbPath,String network_name,
			String bridge_name, String netPrefix) {
		this.retryTimout = retryTimeout;
		this.NETWORK_NAME = network_name;
		this.BRIDGE_NAME = bridge_name;
		this.NETWORK_PREFIX = netPrefix;
		activeConnections = new ArrayList<NetHypervisorConnection>();
		offlineConnections = new ArrayList<Hypervisor>();
		d = new Database(dbPath);
		timer = new Timer();
		
		connectionsLock = new Object();
	}
	
	/**
	 * Creates a connection manager with a specified timeout in order to
	 * check if any hypervisor has returned on. Default db path is used
	 * 
	 * @param retryTimeout 0 for no retry
	 * @throws IOException 
	 */
	public VPMHypervisorConnectionManager(int retryTimeout,String network_name,String bridge_name,
			String netPrefix) throws IOException{
		this(retryTimeout,Database.DEFAULT_DBPATH,network_name,bridge_name,netPrefix);
	}
	
	void start() throws IOException{
		d.connect();
		
		String netDescr = getNetworkDescription();
		
		for(Hypervisor h : d.getAllHypervisors()){
			addHypervisor(h,netDescr);
		}	
		d.close();
		
		if(retryTimout != 0){ //a zero value means user wants to recover connections
			setActive(true);
			timer.schedule(new ConnectionCheckerThread(),0);
		}
	}
	
	void stop() throws IOException{
		
		if(retryTimout != 0){//polling thread was active
			setActive(false);
			timer.cancel();
		}
		
		//closing every active connection
		synchronized(connectionsLock){

			//closing every active connection
			Iterator<NetHypervisorConnection> iterator = activeConnections.iterator();
			while(iterator.hasNext()){
				iterator.next();
				iterator.remove();//removeHypervisor(iterator.next().getHypervisor());
			}
		}
		
	}
	
	
	/**
	 * Adds a new hypervisor to the connection manager.
	 * Manager will attempt to establish a connection with given hypervisor and will classify it as
	 * either online or offline
	 * @param h the hypervisor to be added
	 * @param networkDescr a description of the network to be run on this hypervisor
	 */
	public void addHypervisor(Hypervisor h,String networkDescr){
		try {
			NetHypervisorConnection conn = NetHypervisorConnection.getConnectionWithTimeout(h,
					NETWORK_NAME,NETWORK_PREFIX, networkDescr,
					CONNECTION_TIMEOUT);
			synchronized(connectionsLock){
				activeConnections.add(conn);
			}

		} catch (IOException e) {
			System.err.println("Hypervisor "+h+" was offline, adding it to offline list");
			synchronized(connectionsLock){
				offlineConnections.add(h);
			}
		} catch (LibvirtException e1){
			e1.printStackTrace();
		}
	}

	/**
	 * Removes an hypervisor from the manager, regardless of its current status
	 * @param h the hypervisor to be removed
	 */
	public void removeHypervisor(Hypervisor h){
		synchronized(connectionsLock){

			//we check if it is among the offlines
			if(offlineConnections.contains(h)){
				offlineConnections.remove(h); //easy!

			}else{ //we have to find it among active connections
				HypervisorConnection hc = getActiveConnection(h);
				activeConnections.remove(hc);
			}
		}
	}

	public List<NetHypervisorConnection> getActiveConnections(){
		return new ArrayList<NetHypervisorConnection>(activeConnections);
	}

	public List<Hypervisor> getOfflineHypervisors(){
		return new ArrayList<Hypervisor>(offlineConnections);
	}

	public NetHypervisorConnection getActiveConnection(String hypervisorId){
		NetHypervisorConnection conn = null;
		synchronized (connectionsLock){
			int i = 0;
			while((conn == null) && (i<activeConnections.size())){
				if(activeConnections.get(i).getHypervisor().getId().equals(hypervisorId)){//found
					conn = activeConnections.get(i);
				}else
					i++;
			}
		}

		return conn;
	}

	/**
	 * Marks as inactive a connection, if retry timeout is !=0 the system will periodically check it to see
	 * if it returns online
	 * 
	 * @param c
	 */
	public void setInactive(NetHypervisorConnection c){
		synchronized(connectionsLock){
			activeConnections.remove(c);
			offlineConnections.add(c.getHypervisor());
		}
	}

	/**
	 * This class periodically pools offline connections in order to see if an hypervisor was active
	 * @author pasquale
	 *
	 */
	private class ConnectionCheckerThread extends TimerTask{

		@Override
		public void run() {
			synchronized(connectionsLock){
				Iterator<Hypervisor> iterator = offlineConnections.iterator();
				while(iterator.hasNext()){
					try {
						Hypervisor h = iterator.next();
						//this constructor call will either succeed or throw exception, in this last case we can
						//assume the hypervisor is still offline and so we do nothing
						NetHypervisorConnection c = NetHypervisorConnection.getConnectionWithTimeout(h, 
								NETWORK_NAME, NETWORK_PREFIX,
								getNetworkDescription(),CONNECTION_TIMEOUT);
						activeConnections.add(c);
						iterator.remove();
					} catch (IOException | LibvirtException e) {
						//System.err.println(h+" is still offline");
					}
				}
			}
			if(retryTimout!= 0 && isActive()){
				timer.schedule(new ConnectionCheckerThread(), retryTimout);
			}
		}

	}
}
