package org.at.connections;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.at.db.Database;
import org.at.db.DatabaseListener;
import org.at.db.Hypervisor;
import org.at.libvirt.NetHypervisorConnection;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.libvirt.LibvirtException;

public class HypervisorConnectionManager implements DatabaseListener{
	
	private static final int CONNECTION_TIMEOUT = 500;
	public static final String HYPERVISOR_CONNECTION_MANAGER = "hmanager";
	private static final String XML_NETWORK_FILEPATH = "xml_definitions/network_template.xml";
	
	private int retryTimout;
	private Timer timer; //this timer is used by the polling thread
	
	//network settings
	private final String NETWORK_NAME;
	private final String BRIDGE_NAME;
	
	private boolean active;
	private synchronized boolean isActive(){
		return active;
	}
	
	private synchronized void setActive(boolean v){
		active = v;
	}
	
	
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
	
	//<--------------------------------------------------------------------------------
	/**
	 * Creates a connection manager with a specified timeout in order to
	 * check if any hypervisor has returned on. 
	 * 
	 * @param retryTimeout 0 for no retry
	 * @param dbPath path to the sqlite db
	 * @throws IOException 
	 */
	public HypervisorConnectionManager(int retryTimeout,String dbPath,String network_name,String bridge_name) {
		this.retryTimout = retryTimeout;
		this.NETWORK_NAME = network_name;
		this.BRIDGE_NAME = bridge_name;
		activeConnections = new ArrayList<NetHypervisorConnection>();
		offlineConnections = new ArrayList<Hypervisor>();
		d = new Database(dbPath);
		timer = new Timer();
	}
	
	/**
	 * Creates a connection manager with a specified timeout in order to
	 * check if any hypervisor has returned on. Default db path is used
	 * 
	 * @param retryTimeout 0 for no retry
	 * @throws IOException 
	 */
	public HypervisorConnectionManager(int retryTimeout,String network_name,String bridge_name) throws IOException{
		this(retryTimeout,Database.DEFAULT_DBPATH,network_name,bridge_name);
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
	
	public synchronized void addHypervisor(Hypervisor h,String networkDescr){
		try {
			NetHypervisorConnection conn = NetHypervisorConnection.getConnectionWithTimeout(h,NETWORK_NAME,networkDescr,
					CONNECTION_TIMEOUT); 
			activeConnections.add(conn);
		} catch (IOException e) {
			System.err.println("Hypervisor "+h+" was offline, adding it to offline list");
			offlineConnections.add(h);
		} catch (LibvirtException e1){
			e1.printStackTrace();
		}
	}
	
	//TODO to solve
	synchronized void stop() throws IOException{
		
		if(retryTimout != 0){//polling thread was active
			setActive(false);
			timer.cancel();
		}
		
		//closing every active connection
		for(NetHypervisorConnection hc : getActiveHypervisors())
			removeHypervisor(hc.getHypervisor());
		
	}
	
	public synchronized void removeHypervisor(Hypervisor h){
		//we check if it is among the offlines
		if(offlineConnections.contains(h)){
			offlineConnections.remove(h); //easy!
			
		}else{ //we have to check if it is among active connections
			int i = 0;
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
		}
	}
	
	public synchronized List<NetHypervisorConnection> getActiveHypervisors(){
		return this.activeConnections;
	}
	
	public synchronized List<Hypervisor> getOfflineHypervisors(){
		return this.offlineConnections;
	}
	
	public synchronized NetHypervisorConnection getActiveConnection(Hypervisor h){
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
	
	public synchronized NetHypervisorConnection getActiveConnection(String hypervisorId){
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
			for(Hypervisor h : getOfflineHypervisors()){
				try {
					//this constructor call will either succeed or throw exception, in this last case we can
					//assume the hypervisor is still offline and so we do nothing
					NetHypervisorConnection c = NetHypervisorConnection.getConnectionWithTimeout(h, NETWORK_NAME,
							getNetworkDescription(),CONNECTION_TIMEOUT);
					getActiveHypervisors().add(c);
					getOfflineHypervisors().remove(h);
				} catch (IOException | LibvirtException e) {
					//System.err.println(h+" is still offline");
				}
			}
		
			if(retryTimout!= 0 && isActive()){
				timer.schedule(new ConnectionCheckerThread(), retryTimout);
			}
		}
		
	}
}
