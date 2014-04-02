package org.at.floodlight.types;

public class LinkConnection {
	public String dpidSrc;
	public int srcPort;
	public String dpidDst;
	public int dstPort;
	
	public LinkConnection(String src,String dst, int srcPort,int dstPort){
		this.dpidSrc = src;
		this.dpidDst = dst;
		this.srcPort = srcPort;
		this.dstPort = dstPort;
	}
	
	public boolean sameLink(LinkConnection l){
		boolean same = (this.dpidSrc.equals(l.dpidSrc)) && (this.dpidDst.equals(l.dpidDst)) && (this.srcPort == l.srcPort) && (this.dstPort == l.dstPort);
		boolean reverse = (this.dpidSrc.equals(l.dpidDst)) && (this.dpidDst.equals(l.dpidSrc)) && (this.srcPort == l.dstPort) && (this.dstPort == l.srcPort);
	
		return same || reverse;
	}
	
	public boolean equals(Object o){
		LinkConnection l = (LinkConnection)o;
		return (this.dpidSrc.equals(l.dpidSrc)) && (this.dpidDst.equals(l.dpidDst)) && (this.srcPort == l.srcPort) && (this.dstPort == l.dstPort);
	}
	
	public String toString(){
		return "OvsLink/ from "+this.dpidSrc+":"+this.srcPort+" to "+this.dpidDst+":"+this.dstPort;
	}
	
}
