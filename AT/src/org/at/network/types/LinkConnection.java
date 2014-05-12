package org.at.network.types;

import org.jgrapht.graph.DefaultEdge;

public class LinkConnection extends DefaultEdge {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public OvsSwitch src;
	public Port srcPort;
	public OvsSwitch target;
	public Port targetPort;
	public boolean isTree;
	
	public LinkConnection(String src,String dst, Port srcPort, Port dstPort){
		this(src,"",dst,"",srcPort,dstPort);
	}
	
	public LinkConnection(String src, String srcIp, String dst, String dstIp, Port srcPort,
			Port dstPort){
		this.src = new OvsSwitch(src, srcIp);
		this.target = new OvsSwitch(dst,dstIp);
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
