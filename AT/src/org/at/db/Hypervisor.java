package org.at.db;

public class Hypervisor {
	private String id;
	private String name;
	private String ipAddress;
	private long port;
	
	public final static String STATUS_ONLINE = "online";
	public final static String STATUS_OFFLINE = "offline";
	
	public Hypervisor(int id, String name, String ipAddress, long port) {
		this.id = "H"+id;
		this.name = name;
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	public Hypervisor(String name, String ipAddress, long port) {
		this.id = null;
		this.name = name;
		this.ipAddress = ipAddress;
		this.port = port;
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

	public String getHostAddress() {
		return ipAddress;
	}

	public void setHostname(String hostname) {
		this.ipAddress = hostname;
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
		return (h.getName().equals(name) && h.getHostAddress().equals(ipAddress) && (h.getPort() == port));
	}
	
	@Override
	public String toString(){
		return name+"@"+ipAddress+":"+port;
	}

}
