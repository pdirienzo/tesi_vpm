package org.at.db;

public class Hypervisor {
	private String name;
	private String ipAddress;
	private long port;
	
	public Hypervisor(String name, String ipAddress, long port) {
		this.name = name;
		this.ipAddress = ipAddress;
		this.port = port;
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

}
