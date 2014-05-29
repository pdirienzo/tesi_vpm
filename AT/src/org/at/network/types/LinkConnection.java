package org.at.network.types;

import org.jgrapht.graph.DefaultWeightedEdge;

public class LinkConnection extends DefaultWeightedEdge {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Port sourceP;
	private Port targetP;
	public boolean isTree;	
	
	//these setters can be called just by classes belonging to the same package
	public void setSourceP(Port source){
		this.sourceP = source;	
	}
	
	public void setTargetP(Port target){
		this.targetP = target;
	}
	
	//public section, just getters
	public OvsSwitch getSource(){
		return (OvsSwitch)super.getSource();
	}
	
	public Port getSrcPort(){
		return this.sourceP;
	}
	
	public Port getTargetPort(){
		return this.targetP;
	}
	
	public OvsSwitch getTarget(){
		return (OvsSwitch)super.getTarget();
	}
	
	public double getWeight(){
		return super.getWeight();
	}
	
	@Override
	public boolean equals(Object o){
		LinkConnection l = (LinkConnection)o;
		
		if(this.getSource()==null || this.getTarget() == null || l.getSource() == null || l.getTarget() == null)
			return false;
		else
			return ( this.getSource().equals(l.getSource()) && this.getTarget().equals(l.getTarget()) 
					&& this.sourceP.equals(l.sourceP) && this.targetP.equals(l.targetP));
	}
	
	public boolean oppositeLink(LinkConnection l){
		if(this.getSource()==null || this.getTarget() == null || l.getSource() == null || l.getTarget() == null)
			return false;
		else 
			return(this.getSource().equals(l.getTarget())) && (this.getTarget().equals(l.getSource())) && 
					this.sourceP.equals(l.targetP) && this.targetP.equals(l.sourceP);
		
	}
	
	public String toString(){
		return "OvsLink from "+getSource()+"["+sourceP+"] to "+getTarget()+"["+targetP+"]";
	}
	
	@Override
	public int hashCode() {	
		return ("42".hashCode());
	}
	
	
	
	/*
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
	}*/
	
}
