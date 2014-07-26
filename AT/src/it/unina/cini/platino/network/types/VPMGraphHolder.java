package it.unina.cini.platino.network.types;


/**
 * An holder class for a VPM graph. Its boolean "valid" fields can be set so to invalidate
 * contained graph.
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
public class VPMGraphHolder {
	
	public static final String VPM_GRAPH_HOLDER = "GRAPH_HOLDER";
	
	private boolean valid;
	private VPMGraph<OvsSwitch, LinkConnection> graph;
	
	public synchronized void addGraph(VPMGraph<OvsSwitch, LinkConnection> graph){
		this.graph = graph;
		valid = true;
	}
	
	public synchronized void invalidate(){
		valid= false;
	}
	
	/**
	 * Returns the graph object if still valid, null otherwise
	 * @return
	 */
	public synchronized VPMGraph<OvsSwitch, LinkConnection> getGraph(){
		if(valid)
			return graph;
		else
			return null;
	}
 
}
