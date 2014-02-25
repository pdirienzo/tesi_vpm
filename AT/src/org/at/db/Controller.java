package org.at.db;

public class Controller {

	private String hostAddress;
	private long port;
	
	public Controller(String hostAddress,long port) {
		this.hostAddress = hostAddress;
		this.port = port;
	}

	public String getHostAddress() {
		return hostAddress;
	}

	public void setHostAddress(String hostname) {
		this.hostAddress = hostname;
	}

	public long getPort() {
		return port;
	}

	public void setPort(long port) {
		this.port = port;
	}
}
