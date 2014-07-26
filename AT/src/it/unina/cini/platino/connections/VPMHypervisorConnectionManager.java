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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	
	private Lock connectionsLock;
	
	private List<NetHypervisorConnection> activeConnections;
	private List<Hypervisor> offlineConnections;
	
	private Database d;
	
	// listener methods----------------------------------------------------------->
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
		
		connectionsLock = new ReentrantLock();
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
		
		connectionsLock.lock();
		
		//closing every active connection
		Iterator<NetHypervisorConnection> iterator = activeConnections.iterator();
		while(iterator.hasNext()){
			removeHypervisor(iterator.next().getHypervisor());
		}
		
		connectionsLock.unlock();
		//TODO to solve
		
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
			
			connectionsLock.lock();
			activeConnections.add(conn);
			connectionsLock.unlock();
			
		} catch (IOException e) {
			System.err.println("Hypervisor "+h+" was offline, adding it to offline list");
			connectionsLock.lock();
			offlineConnections.add(h);
			connectionsLock.unlock();
		} catch (LibvirtException e1){
			e1.printStackTrace();
		}
	}
	
	/**
	 * Removes an hypervisor from the manager, regardless of its current status
	 * @param h the hypervisor to be removed
	 */
	public void removeHypervisor(Hypervisor h){
		
		connectionsLock.lock();
		
		//we check if it is among the offlines
		if(offlineConnections.contains(h)){
			offlineConnections.remove(h); //easy!
			
		}else{ //we have to find it among active connections
			HypervisorConnection hc = getActiveConnection(h);
			activeConnections.remove(hc);
			/*int i = 0;
			boolean result = false;
			while((!result) && (i<activeConnections.size())){
				if(activeConnections.get(i).getHypervisor().equals(h)){//found
					try {
						NetHypervisorConnection conn = activeConnections.get(i);
						conn.close();
						activeConnections.remove(i); //and then remove the element itself
						result = true;
					} catch (LibvirtException e) {
						e.printStackTrace();
					}
				}else
					i++;
			}
			*/	
		}
		
		connectionsLock.unlock();
	}
	
	public List<NetHypervisorConnection> getActiveConnections(){
		return this.activeConnections;
	}
	
	public List<Hypervisor> getOfflineHypervisors(){
		return this.offlineConnections;
	}
	
	public NetHypervisorConnection getActiveConnection(String hypervisorId){
		int i = 0;
		NetHypervisorConnection conn = null;
		
		while((conn == null) && (i<activeConnections.size())){
			if(activeConnections.get(i).getHypervisor().getId().equals(hypervisorId)){//found
				conn = activeConnections.get(i);
			}else
				i++;
		}
		
		return conn;
	}
	
	/**
	 * Marks as inactive a connection, if retry timeout is !=0 the system will periodically check it to see
	 * if it returns online
	 * 
	 * @param c
	 */
	public synchronized void setInactive(NetHypervisorConnection c){
		activeConnections.remove(c);
		offlineConnections.add(c.getHypervisor());
	}
	
	/**
	 * This class periodically pools offline connections in order to see if an hypervisor was active
	 * @author pasquale
	 *
	 */
	private class ConnectionCheckerThread extends TimerTask{

		@Override
		public void run() {
			connectionsLock.lock();
			
			for(Hypervisor h : getOfflineHypervisors()){
				try {
					//this constructor call will either succeed or throw exception, in this last case we can
					//assume the hypervisor is still offline and so we do nothing
					NetHypervisorConnection c = NetHypervisorConnection.getConnectionWithTimeout(h, 
							NETWORK_NAME, NETWORK_PREFIX,
							getNetworkDescription(),CONNECTION_TIMEOUT);
					activeConnections.add(c);
					getOfflineHypervisors().remove(h);
				} catch (IOException | LibvirtException e) {
					//System.err.println(h+" is still offline");
				}
			}
			
			connectionsLock.unlock();
		
			if(retryTimout!= 0 && isActive()){
				timer.schedule(new ConnectionCheckerThread(), retryTimout);
			}
		}
		
	}
}
