package org.at.web.streaming;

public class PipelineDestinationInfo {

	private int vrtpport;
	
	private String nodeID;
	
	private String host;

	public PipelineDestinationInfo() {
		
	}
	public PipelineDestinationInfo(String host, int vrtpport, int vrtcpport, String nodeID) {
		this.host=host;
		this.vrtpport=vrtpport;
		this.nodeID=nodeID;
	}
	
	public boolean isCenternode(){
		return nodeID!=null;
	}
	public boolean isLeaf(){
		return nodeID==null;
	}

	@Override
	public String toString(){
		return nodeID==null?host+":"+vrtpport:nodeID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((nodeID == null) ? 0 : nodeID.hashCode());
		result = prime * result + vrtpport;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PipelineDestinationInfo))
			return false;
		PipelineDestinationInfo other = (PipelineDestinationInfo) obj;
		return this.toString().equals(other.toString());
	}

	public int getVrtpport() {
		return vrtpport;
	}

	public void setVrtpport(int vrtpport) {
		this.vrtpport = vrtpport;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	
}
