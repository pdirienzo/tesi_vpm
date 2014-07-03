package it.unina.cini.platino.network.types;

import org.jgrapht.graph.DefaultWeightedEdge;

public class LinkConnection extends DefaultWeightedEdge {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Port sourceP;
	private Port targetP;
	public boolean isTree;	
	
	
	/*
	//this is just in order to make hashcode() work properly so these fields should
	//always be private and never accessed. Use instead the getters methods which
	//are inherited from the father class
	private OvsSwitch src;
	private OvsSwitch trg;
	LinkConnection(OvsSwitch src, OvsSwitch trg, Port sourceP, Port targetP){
		this.src = src;
		this.trg = trg;
		this.sourceP = sourceP;
		this.targetP = targetP;
	}*/
	
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
		return "42".hashCode();//((getSource().toString()+getTarget().toString()).hashCode());
	}
	
}
