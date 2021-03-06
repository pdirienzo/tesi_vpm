package it.unina.cini.platino.network;

import it.unina.cini.platino.floodlight.FloodlightPort;
import it.unina.cini.platino.network.types.LinkConnection;
import it.unina.cini.platino.network.types.OvsSwitch;
import it.unina.cini.platino.network.types.VPMGraph;
import it.unina.cini.platino.network.types.OvsSwitch.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.GraphPathImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.view.mxGraph;

/**
 * Utility class used to convert network representations from/to JGraphT and mxGraph
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
public final class NetworkConverter {

	private static OvsSwitch domToSwitch(Element el){
		return new OvsSwitch( el.getAttribute("dpid"), el.getAttribute("ip") , Type.valueOf(el.getAttribute("type")));
	}

	private static Element switchToDom(Document doc, OvsSwitch sw){
		org.w3c.dom.Element swEl = doc.createElement("switch");
		swEl.setAttribute("dpid", sw.dpid);
		swEl.setAttribute("ip", sw.ip);
		swEl.setAttribute("type", sw.type.name());
		
		return swEl;
	}
	
	private static Element linkToDom(Document doc, LinkConnection link){
		org.w3c.dom.Element linkEl = doc.createElement("link");
		linkEl.setAttribute("srcPort", link.getSrcPort().toString());
		linkEl.setAttribute("dstPort", link.getTargetPort().toString());
		linkEl.setAttribute("isTree", String.valueOf(link.isTree));

		return linkEl;
	}
	
	/**
	 * Converts an mx representation of a graph to a jgraphT one.
	 * 
	 * @param graph mxGraph instance to be converted
	 * @param markTreeLinks this will allow you to ignore tree links (ie. if a link is marked as tree it will not be marked
	 * as such in the jgraphT instance). This can be useful when the resulting jgraphT instance has to be passed to a tree
	 * calculator alghoritm, this way you don't have to manually reset those links.
	 * @return
	 */
	public static VPMGraph<OvsSwitch,LinkConnection> mxToJgraphT(mxGraph graph, boolean markTreeLinks){
		VPMGraph<OvsSwitch, LinkConnection> myGraph = new VPMGraph<OvsSwitch, LinkConnection>(LinkConnection.class);
		
		HashMap<String, OvsSwitch> switches = new HashMap<String, OvsSwitch>();

		for(Object cell : graph.getChildCells(graph.getDefaultParent(),true, false)){// getting vertexes
			OvsSwitch sw = domToSwitch((Element)(((mxCell)cell).getValue())); //converting

			myGraph.addVertex(sw); //putting it into the graph
			switches.put(sw.dpid, sw);  // and into the hashtable so to get it later
		}
		
		for(Object o : graph.getChildCells(graph.getDefaultParent(),false, true)){ //now links
			mxCell cell = (mxCell)o;
			OvsSwitch source = switches.get(((Element)cell.getSource().getValue()).getAttribute("dpid"));
			OvsSwitch target = switches.get(((Element)cell.getTarget().getValue()).getAttribute("dpid"));
			
			LinkConnection link = null;
			if(markTreeLinks)
				link = myGraph.addLinkConnection(source, new FloodlightPort(((Element)cell.getValue()).getAttribute("srcPort")), target, 
					new FloodlightPort(((Element)cell.getValue()).getAttribute("dstPort")), Boolean.valueOf(((Element)cell.getValue()).getAttribute("isTree")));
			else
				link = myGraph.addLinkConnection(source, new FloodlightPort(((Element)cell.getValue()).getAttribute("srcPort")), target, 
						new FloodlightPort(((Element)cell.getValue()).getAttribute("dstPort")));
			
			myGraph.setEdgeWeight(link, link.getSource().type.getValue() + link.getTarget().type.getValue());
		}
		
		return myGraph;
	}
	
	/**
	 * Convenience method which returns an exactly identical representation of the graph
	 * @param graph
	 * @return
	 */
	/*public static VPMGraph<OvsSwitch,LinkConnection> mxToJgraphT(mxGraph graph){
		return mxToJgraphT(graph,true);
	}*/
	
	public static mxGraph jgraphToMx(VPMGraph<OvsSwitch, LinkConnection> graph){
		mxGraph myGraph = new mxGraph();
		
		try{
			myGraph.getModel().beginUpdate();
			org.w3c.dom.Document doc = mxDomUtils.createDocument();

			Iterator<OvsSwitch> switches = graph.vertexSet().iterator();
			HashMap<String, mxCell> vertexes = new HashMap<String, mxCell>();

			while(switches.hasNext()){
				OvsSwitch sw = switches.next();
				org.w3c.dom.Element swEl = switchToDom(doc,sw);

				vertexes.put(sw.dpid, (mxCell)myGraph.insertVertex(myGraph.getDefaultParent(), null, swEl, 10, 10, 
						100, 50));
			}

			Iterator<LinkConnection> links = graph.edgeSet().iterator();

			while(links.hasNext()){
				LinkConnection l = links.next();
				
				Element linkEl = linkToDom(doc,l);
				myGraph.insertEdge(myGraph.getDefaultParent(), null, linkEl, vertexes.get(l.getSource().dpid), 
						vertexes.get(l.getTarget().dpid));

			}

		}finally{
			myGraph.getModel().endUpdate();
		}
		return myGraph;

	}
	
	public static GraphPath<OvsSwitch,LinkConnection> mxToJpath(mxGraph graph){
		VPMGraph<OvsSwitch, LinkConnection> jgraph = mxToJgraphT(graph, true);
		
		List<LinkConnection> edgeList = new ArrayList<LinkConnection>();
		for(LinkConnection l : jgraph.edgeSet())
			edgeList.add(l);
		
		
		
		return new GraphPathImpl<OvsSwitch, LinkConnection>(jgraph,
				jgraph.getEdgeSource(edgeList.get(0)),
				jgraph.getEdgeTarget(edgeList.get(edgeList.size()-1)),
				edgeList, 2);
	}
	
	public static mxGraph jpathToMx(GraphPath<OvsSwitch, LinkConnection> jpath){
		mxGraph graph = new mxGraph();
		try{
			graph.getModel().beginUpdate();
			org.w3c.dom.Document doc = mxDomUtils.createDocument();
			
			HashMap<String, mxCell> vertexes = new HashMap<String, mxCell>();
			//switches...
			for(OvsSwitch o : Graphs.getPathVertexList(jpath)){
				vertexes.put(o.dpid, (mxCell)graph.insertVertex(graph.getDefaultParent(),null, 
						switchToDom(doc,o), 10, 10, 100, 50));
			}
			
			//now edges
			for(LinkConnection l : jpath.getEdgeList()){
				graph.insertEdge(graph.getDefaultParent(), null, linkToDom(doc,l), vertexes.get(l.getSource().dpid), 
						vertexes.get(l.getTarget().dpid));
			}
			
		}finally{
			graph.getModel().endUpdate();
		}
		return graph;
	}
	
	
	private static KruskalMinimumSpanningTree<OvsSwitch, LinkConnection> createTree(VPMGraph<OvsSwitch,LinkConnection> graph){	

		KruskalMinimumSpanningTree<OvsSwitch, LinkConnection> k = new KruskalMinimumSpanningTree<OvsSwitch,LinkConnection>(graph);

		//debug
		System.out.println("Called kruskal alghoritm with weights:");
		Iterator<LinkConnection> iterator = graph.edgeSet().iterator();

		while(iterator.hasNext()){
			LinkConnection l = iterator.next();
			System.out.println(l.getSource().type.name()+" --> "+l.getTarget().type.name()+" /"+l.getWeight());
		}

		//

		iterator = k.getMinimumSpanningTreeEdgeSet().iterator();

		while(iterator.hasNext()){
			LinkConnection l = iterator.next();
			l.isTree = true;
			graph.setEdgeWeight(l, 0); //setting tree's weight to zero so that minimum shortest path alghoritm just inspects tree edges
		}

		return k;
	}
	
	/**
	 * This function removes not necessary edges from the graph before for it to be processed
	 * by the Kruskal algorithm. That is, if a path ends up to a non leaf switch, links
	 * will be removed as they are not necessary for the overall distribution network.  
	 * @param g
	 */
	private static void potateRelays(VPMGraph<OvsSwitch, LinkConnection> g){
		for(OvsSwitch sw : g.vertexSet()){
			int k=0;
			int j=0;
			int i=0;
			if(sw.type == OvsSwitch.Type.RELAY || sw.type == OvsSwitch.Type.NULL){
					ArrayList<Integer> hasLeaf = new ArrayList<Integer>();
					ArrayList<Integer> hasRoot = new ArrayList<Integer>();
					ArrayList<String> hasRelay = new ArrayList<String>();
					Set<LinkConnection> links = g.edgesOf(sw);
					//Controllo che la dimensione sia maggiore di 1 in tal caso significa che non sono isolato
					// un relay deve avere per forza due link uno verso la radice e uno vero la foglia nel caso base.
					if (links.size() > 1){
					
						for(LinkConnection l : links){
							if(l.isTree){
								if( (l.getTarget().type == OvsSwitch.Type.LEAF) || (l.getSource().type == OvsSwitch.Type.LEAF)){
									hasLeaf.add(1);
									k++;
									}
								else if((l.getTarget().type == OvsSwitch.Type.ROOT || l.getSource().type == OvsSwitch.Type.ROOT)){
									hasRoot.add(2);
									j++;
								}
								else if (l.getTarget().type == OvsSwitch.Type.RELAY && l.getSource().type == OvsSwitch.Type.RELAY){
									if(l.getTarget().dpid != sw.dpid){
										//Devo ottenere in qualche modo l'OvsSwitch target
										//OvsSwitch newTarget = l.getTarget();
										hasRelay.add(checkNode(l.getTarget(),l,g));
									  }
									else {
									   //Ottengo il prossimo relay dalla sorgente del link 
									   hasRelay.add(checkNode(l.getSource(),l,g));
									}
									i++;
								}
							}
						}
						//Blocco di controlli...
						if (k > 0) { // ho sicuramente una foglia collegata controllo se ho anche una radice
							if (!(j>0)){ // non ho una radice controllo se ho qualche relay
								if(i>0){ // ho i relay
									// controllo se qualcuno e' attaccato alla radice altrimenti cancello i miei link
									int n=0; 
									boolean isRooted=false;
									while(n<i){
										if(hasRelay.get(n).contains("ROOT")){
											isRooted=true;
											break;
										}
										n++;
									}
									if (!isRooted){
										for(LinkConnection l : g.edgesOf(sw)){
										if(l.isTree)
											l.isTree = false;
										}
									}
									
								}else { //non ho i relay quindi sono solo attaccato alla foglia posso eliminare
									for(LinkConnection l : g.edgesOf(sw)){
										if(l.isTree)
											l.isTree = false;
									}
								}
							}
						}
						else {
							if (!(j>0)){ // non ho una radice e nemmeno una foglia controllo se ho qualche relay
								if(i>0){ // ho i relay
									// controllo se qualcuno e' attaccato alla radice e ad una foglia altrimenti cancello i miei link
									int n=0; 
									boolean isRooted=false;
									boolean hasALeaf=false;
									while(n<i){
										if(hasRelay.get(n).contains("ROOT") ){
											isRooted=true;
											
										}
										if(hasRelay.get(n).contains("LEAF")){
										   hasALeaf=true;
										}
										n++;
									}
									if (!isRooted && !hasALeaf){ //non hanno radice ne foglie e una catena di relay
										for(LinkConnection l : g.edgesOf(sw)){
										if(l.isTree)
											l.isTree = false;
										}
									}
									
								}else { //non ho i relay quindi sono solo attaccato alla foglia posso eliminare
									for(LinkConnection l : g.edgesOf(sw)){
										if(l.isTree)
											l.isTree = false;
									}
								}
							}
							else {
								//sono attaccato ad una radice ma non ho foglie controllo se ho relay che hanno foglie
								if(i>0){ // ho i relay
									// controllo se qualcuno e' attaccato alla radice e ad una foglia altrimenti cancello i miei link
									int n=0; 
									boolean hasALeaf=false;
									while(n<i){
										if(hasRelay.get(n).contains("LEAF")){
										   hasALeaf=true;
										   break;
										}
										n++;
									}
									if (!hasALeaf){ //non hanno foglie e una catena di relay
										for(LinkConnection l : g.edgesOf(sw)){
										if(l.isTree)
											l.isTree = false;
										}
									}
									
								}else { //non ho i relay quindi sono solo attaccato alla foglia posso eliminare
									for(LinkConnection l : g.edgesOf(sw)){
										if(l.isTree)
											l.isTree = false;
									}
								}
							}
						}
						
						
						
					}
					else {
						for(LinkConnection l : g.edgesOf(sw))
							if(l.isTree)
								l.isTree = false;
					}

					
				}
		}
		
	}
 
	/**
	 * Auxiliary function for the potate tree method
	 * @param sw
	 * @param link
	 * @param g
	 * @return
	 */
 private static String checkNode(OvsSwitch sw, LinkConnection link, VPMGraph<OvsSwitch,LinkConnection> g){
	String ret = "";
	int root=0;
	int leaf=0;
	int count = 0; // if count is 1 it means that relay is isolated and should not be included in the final tree
	Set<LinkConnection> links = g.edgesOf(sw);
	for(LinkConnection l : links){
		if(l.isTree && !l.equals(link)){
			if( (l.getTarget().type == OvsSwitch.Type.RELAY) && (l.getSource().type == OvsSwitch.Type.RELAY)){
			  if(l.getTarget().dpid != sw.dpid){
				
				ret=checkNode(l.getTarget(),l,g);
			  }
			  else {
			   
			   ret=checkNode(l.getSource(),l,g);
			  }
			}
			else if( (l.getTarget().type == OvsSwitch.Type.LEAF && l.getSource().type == OvsSwitch.Type.RELAY) || 
			(l.getTarget().type == OvsSwitch.Type.RELAY && l.getSource().type == OvsSwitch.Type.LEAF)){ 
					leaf+=1; //returning 1 if directly attached to a leaf
			}
			else if( (l.getTarget().type == OvsSwitch.Type.RELAY && l.getSource().type == OvsSwitch.Type.ROOT) || 
			(l.getTarget().type == OvsSwitch.Type.ROOT && l.getSource().type == OvsSwitch.Type.RELAY)){ //relay connected to the root
				root+=1;
			}
			count++;
		}
		else if(l.isTree && l.equals(link)) count++;
	}
	if (count > 1){
		if(leaf > 0) {
			if(root > 0){
				if (ret != ""){
					 if(!ret.contains("LEAF")) 
						ret+="LEAF"+","+"ROOT,";
					 else
						ret+="ROOT,"; 
				}
				else ret+="LEAF"+","+"ROOT,";
				
			}
			else if (ret != ""){
				if(!ret.contains("LEAF"))
					ret+="LEAF,";
			}
			else ret+="LEAF";
		}
		else if (root>0) ret+="ROOT,"; 
		return ret;
	}
	else return "";
 }
	
	//****************************************************************************************
	
	public static void main(String[] args){
		VPMGraph<OvsSwitch, LinkConnection> myGraph = new VPMGraph<>(LinkConnection.class);
		ConnectivityInspector<OvsSwitch, LinkConnection> cIsp = new ConnectivityInspector<>(myGraph);
		myGraph.addGraphListener(cIsp);
		
		OvsSwitch a = new OvsSwitch("a","1",OvsSwitch.Type.ROOT);
		OvsSwitch b = new OvsSwitch("b","2", OvsSwitch.Type.RELAY);
		OvsSwitch c = new OvsSwitch("c","3", OvsSwitch.Type.RELAY);
		OvsSwitch d = new OvsSwitch("d","4", OvsSwitch.Type.RELAY);
		OvsSwitch e = new OvsSwitch("e","5",OvsSwitch.Type.RELAY);
		OvsSwitch f = new OvsSwitch("f","6",OvsSwitch.Type.LEAF);
		OvsSwitch g = new OvsSwitch("g","7",OvsSwitch.Type.RELAY);
		OvsSwitch h = new OvsSwitch("h","8",OvsSwitch.Type.LEAF);
		
		myGraph.addVertex(a);
		myGraph.addVertex(b);
		myGraph.addVertex(c);
		myGraph.addVertex(d);
		myGraph.addVertex(e);
		myGraph.addVertex(f);
		myGraph.addVertex(g);
		myGraph.addVertex(h);
		
		LinkConnection[] conns = new LinkConnection[8];
		conns[0] = myGraph.addLinkConnection(a, new FloodlightPort("p1",1),b,new FloodlightPort("p2",2));
		conns[1] = myGraph.addLinkConnection(c, new FloodlightPort("p2",2),b,new FloodlightPort("p3",1));
		conns[2] = myGraph.addLinkConnection(b, new FloodlightPort("p3",1),d,new FloodlightPort("p4",2));
		conns[3] = myGraph.addLinkConnection(d, new FloodlightPort("p1",4),e,new FloodlightPort("p2",1));
		conns[4] = myGraph.addLinkConnection(f, new FloodlightPort("p1",4),b,new FloodlightPort("p2",1));
		conns[5] = myGraph.addLinkConnection(a, new FloodlightPort("p5",5),f,new FloodlightPort("p5",2));
		conns[6] = myGraph.addLinkConnection(d, new FloodlightPort("p6",7),g,new FloodlightPort("p3",1));
		conns[7] = myGraph.addLinkConnection(g, new FloodlightPort("p6",7),h,new FloodlightPort("p3",1));
		
		
		VPMGraph<OvsSwitch, LinkConnection> myGraph2 = new VPMGraph<>(LinkConnection.class);
		
		//OvsSwitch copy = new OvsSwitch("a","1");
		//OvsSwitch copy2 = new OvsSwitch("b","2", OvsSwitch.Type.RELAY);
		
		myGraph2.addVertex(a);
		myGraph2.addVertex(b);
		myGraph2.addVertex(c);
		myGraph2.addVertex(d);
		myGraph2.addVertex(e);
		myGraph2.addVertex(f);
		myGraph2.addVertex(g);
		myGraph2.addVertex(h);
		
		LinkConnection[] conns2 = new LinkConnection[8];
	    conns2[0] = myGraph2.addLinkConnection(a, new FloodlightPort("p1",1),b,new FloodlightPort("p2",2));
		conns2[1] = myGraph2.addLinkConnection(c, new FloodlightPort("p2",2),b,new FloodlightPort("p3",1));
		conns2[2] = myGraph2.addLinkConnection(b, new FloodlightPort("p3",1),d,new FloodlightPort("p4",2));
		conns2[3] = myGraph2.addLinkConnection(d, new FloodlightPort("p1",4),e,new FloodlightPort("p2",1));
		conns2[4] = myGraph2.addLinkConnection(f, new FloodlightPort("p1",4),b,new FloodlightPort("p2",1));
		conns2[5] = myGraph2.addLinkConnection(a, new FloodlightPort("p5",5),f,new FloodlightPort("p5",2));
	    conns2[6] = myGraph2.addLinkConnection(d, new FloodlightPort("p6",7),g,new FloodlightPort("p3",1));
		conns2[7] = myGraph2.addLinkConnection(g, new FloodlightPort("p6",7),h,new FloodlightPort("p3",1));		
		
		createTree(myGraph);
		potateRelays(myGraph);
		for(LinkConnection l : myGraph.edgeSet()){
			if(l.isTree)
				System.out.println(l);
		}
		
		//System.out.println(anotherJ.toString());
	}
	
}
