package it.unina.cini.platino.network.types;


/**
 * Holds a VPMGraph instance with a field to check validity
 * @author pasquale
 *
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
