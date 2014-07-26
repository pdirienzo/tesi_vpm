package it.unina.cini.platino.network.types;

import it.unina.cini.platino.floodlight.FloodlightPort;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * A JGraphT edge extension class to represent a VPM connection
 * 
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 */
public class LinkConnection extends DefaultWeightedEdge {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FloodlightPort sourceP;
	private FloodlightPort targetP;
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
	public void setSourceP(FloodlightPort source){
		this.sourceP = source;	
	}
	
	public void setTargetP(FloodlightPort target){
		this.targetP = target;
	}
	
	//public section, just getters
	public OvsSwitch getSource(){
		return (OvsSwitch)super.getSource();
	}
	
	public FloodlightPort getSrcPort(){
		return this.sourceP;
	}
	
	public FloodlightPort getTargetPort(){
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
