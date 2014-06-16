package org.at.db;

import java.io.IOException;
import java.net.InetAddress;

public class Hypervisor {
	private String id;
	private String name;
	private String hostname;
	private String ipAddress;
	private int iscsiID;
	private long port;
	
	public final static String STATUS_ONLINE = "online";
	public final static String STATUS_OFFLINE = "offline";
	
	public Hypervisor(int id, String name, String hostname, long port, int iscsiID) {
		this.id = "H"+id;
		this.name = name;
		this.hostname = hostname;
		this.iscsiID = iscsiID;
		try{
			this.ipAddress = InetAddress.getByName(hostname).getHostAddress(); //resolving, if possible, the hostname
			
		}catch(IOException ex){
			System.out.println("could not resolve address for hypervisor "+id+"hostname <"+hostname+">");
			this.ipAddress = null;
		}
		this.port = port;
	}
	
	public Hypervisor(String name, String hostname, long port, int iscsiID) {
		this(0, name, hostname, port, iscsiID);
		this.id = null;
		
		/*this.id = null;
		this.name = name;
		this.hostname = ipAddress;
		this.port = port;*/
	}
	
	public void setISCSI(int iscsi){
		this.iscsiID = iscsi;
	}
	
	public int getISCSI(){
		return iscsiID;
	}
	
	public String getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = "H"+id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHostname() {
		return hostname;
	}

	/**
	 * 
	 * @return the resolved ip address or null if wasn't possible to retrieve it
	 */
	public String getIPAddress() {
		return ipAddress; 
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public long getPort() {
		return port;
	}

	public void setPort(long port) {
		this.port = port;
	}
	
	@Override
	public boolean equals(Object h1){
		Hypervisor h = (Hypervisor)h1;
		return (h.getName().equals(name) && h.getHostname().equals(hostname) && (h.getPort() == port));
	}
	
	@Override
	public String toString(){
		return name+"@"+hostname+":"+port;
	}

}
