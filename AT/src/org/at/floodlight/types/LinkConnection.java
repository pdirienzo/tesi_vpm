package org.at.floodlight.types;

import org.jgrapht.graph.DefaultEdge;

public class LinkConnection extends DefaultEdge {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String src;
	public int srcPort;
	public String target;
	public int targetPort;
	public boolean isTree;
	
	public LinkConnection(String src,String dst, int srcPort,int dstPort){
		this.src = src;
		this.target = dst;
		this.srcPort = srcPort;
		this.targetPort = dstPort;
		this.isTree = false;
	}
	
	public boolean oppositeLink(LinkConnection l){
		//boolean same = (this.dpidSrc.equals(l.dpidSrc)) && (this.dpidDst.equals(l.dpidDst)) && (this.srcPort == l.srcPort) && (this.dstPort == l.dstPort);
		boolean reverse = (this.src.equals(l.target)) && (this.target.equals(l.src)) && (this.srcPort == l.targetPort) && (this.targetPort == l.srcPort);
	
		return reverse;
	}
	
	public boolean equals(Object o){
		LinkConnection l = (LinkConnection)o;
		return (this.src.equals(l.src)) && (this.target.equals(l.target)) && (this.srcPort == l.srcPort) && (this.targetPort == l.targetPort);
	}
	
	public String toString(){
		return "OvsLink/ from "+this.src+":"+this.srcPort+" to "+this.target+":"+this.targetPort;
	}
	
}
