package it.unina.cini.platino.db;

import java.io.IOException;
import java.net.InetAddress;

/**
 * An class representing an Hypervisor row in the Database.
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
public class Hypervisor {
	private String id;
	private String name;
	private String hostname;
	private String ipAddress;
	private long port;
	
	public final static String STATUS_ONLINE = "online";
	public final static String STATUS_OFFLINE = "offline";
	
	public Hypervisor(int id, String name, String hostname, long port) {
		this.id = "H"+id;
		this.name = name;
		this.hostname = hostname;
	
		try{
			this.ipAddress = InetAddress.getByName(hostname).getHostAddress(); //resolving, if possible, the hostname
			
		}catch(IOException ex){
			System.out.println("could not resolve address for hypervisor "+id+"hostname <"+hostname+">");
			this.ipAddress = null;
		}
		this.port = port;
	}
	
	public Hypervisor(String name, String hostname, long port) {
		this(0, name, hostname, port);
		this.id = null;
		
		/*this.id = null;
		this.name = name;
		this.hostname = ipAddress;
		this.port = port;*/
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
