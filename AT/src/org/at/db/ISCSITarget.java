package org.at.db;

public class ISCSITarget {
	public int id;
	public String name;
	public String hostname;
	public String iqn;
	public int port;
	
	
	public ISCSITarget(int id, String name, String hostname, int port, String iqn){
		this.id = id;
		this.name = name;
		this.hostname = hostname;
		this.iqn = iqn;
	}
}
