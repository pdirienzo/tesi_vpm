package org.at.network.types;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class VPMGraph<V, E extends LinkConnection> extends 
	ListenableUndirectedWeightedGraph<V, E>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public VPMGraph(Class<? extends E> edgeClass) {
		super(edgeClass);
	}
	
	public VPMGraph(WeightedGraph<V, E> graph){
		super(graph);
	}
	
	public LinkConnection addLinkConnection(V sourceVertex, Port sourcePort, 
			V targetVertex, Port targetPort){
		LinkConnection l = (LinkConnection)super.addEdge(sourceVertex, targetVertex);
		l.setSourceP(sourcePort);
		l.setTargetP(targetPort);
		l.isTree = false;
		
		return l;
	}
	
	public LinkConnection addLinkConnection(V sourceVertex, Port sourcePort, 
			V targetVertex, Port targetPort, boolean isTree){
		
		LinkConnection l = this.addLinkConnection(sourceVertex, sourcePort, targetVertex, targetPort);
		l.isTree = isTree;
		
		return l;
	}
	
	/**
	 * @deprecated use addLinkConnection instead
	 */
	@Deprecated 
	@Override
	public E addEdge(V sourceVertex, V targetVertex) {
		return super.addEdge(sourceVertex, targetVertex);
	}
	
	/**
	 * @deprecated use addLinkConnection instead
	 */
	@Deprecated
	@Override
	public boolean addEdge(V sourceVertex, V targetVertex, E e) {
		return super.addEdge(sourceVertex, targetVertex, e);
	}

	
}
