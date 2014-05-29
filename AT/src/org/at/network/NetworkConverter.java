package org.at.network;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.at.network.types.OvsSwitch.Type;
import org.at.network.types.Port;
import org.at.network.types.VPMGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.view.mxGraph;

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
				link = myGraph.addLinkConnection(source, new Port(((Element)cell.getValue()).getAttribute("srcPort")), target, 
					new Port(((Element)cell.getValue()).getAttribute("dstPort")), Boolean.valueOf(((Element)cell.getValue()).getAttribute("isTree")));
			else
				link = myGraph.addLinkConnection(source, new Port(((Element)cell.getValue()).getAttribute("srcPort")), target, 
						new Port(((Element)cell.getValue()).getAttribute("dstPort")));
			
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
	
	//TODO ********************** remove this shit **********************************************************************
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
	
	private static void potateRelays(VPMGraph<OvsSwitch, LinkConnection> g){
		for(OvsSwitch sw : g.vertexSet()){
			if(sw.type == OvsSwitch.Type.RELAY || sw.type == OvsSwitch.Type.NULL){
				boolean hasLeaf = false;
				boolean hasRootRelay = false;
				
				Set<LinkConnection> links = g.edgesOf(sw);
				for(LinkConnection l : links){
					if(l.isTree){
						if( (l.getTarget().type == OvsSwitch.Type.LEAF) || l.getSource().type == OvsSwitch.Type.LEAF )
							hasLeaf = true;
						else if( (l.getTarget().type == OvsSwitch.Type.RELAY || l.getTarget().type == OvsSwitch.Type.ROOT)
								|| (l.getSource().type == OvsSwitch.Type.RELAY || l.getSource().type == OvsSwitch.Type.ROOT))
							hasRootRelay = true;
					}
				}
				
				if(!(hasLeaf && hasRootRelay) ){
					for(LinkConnection l : g.edgesOf(sw))
						if(l.isTree)
							l.isTree = false;
				}
			}
		}
	}
	
	//****************************************************************************************
	
	public static void main(String[] args){
		VPMGraph<OvsSwitch, LinkConnection> myGraph = new VPMGraph<>(LinkConnection.class);
		ConnectivityInspector<OvsSwitch, LinkConnection> cIsp = new ConnectivityInspector<>(myGraph);
		myGraph.addGraphListener(cIsp);
		
		OvsSwitch a = new OvsSwitch("a","1",OvsSwitch.Type.ROOT);
		OvsSwitch b = new OvsSwitch("b","2", OvsSwitch.Type.RELAY);
		OvsSwitch c = new OvsSwitch("c","3", OvsSwitch.Type.LEAF);
		OvsSwitch d = new OvsSwitch("d","4", OvsSwitch.Type.RELAY);
		OvsSwitch e = new OvsSwitch("e","5",OvsSwitch.Type.RELAY);
		OvsSwitch f = new OvsSwitch("f","6",OvsSwitch.Type.RELAY);
		OvsSwitch g = new OvsSwitch("g","7",OvsSwitch.Type.RELAY);
		OvsSwitch h = new OvsSwitch("h","8",OvsSwitch.Type.RELAY);
		
		myGraph.addVertex(a);
		myGraph.addVertex(b);
		myGraph.addVertex(c);
		myGraph.addVertex(d);
		myGraph.addVertex(e);
		myGraph.addVertex(f);
		myGraph.addVertex(g);
		myGraph.addVertex(h);
		
		LinkConnection[] conns = new LinkConnection[8];
		conns[0] = myGraph.addLinkConnection(a, new Port("p1",1),b,new Port("p2",2));
		conns[1] = myGraph.addLinkConnection(c, new Port("p2",2),b,new Port("p3",1));
		conns[2] = myGraph.addLinkConnection(b, new Port("p3",1),d,new Port("p4",2));
		conns[3] = myGraph.addLinkConnection(d, new Port("p1",4),e,new Port("p2",1));
		conns[4] = myGraph.addLinkConnection(f, new Port("p1",4),b,new Port("p2",1));
		conns[5] = myGraph.addLinkConnection(a, new Port("p5",5),f,new Port("p5",2));
		conns[6] = myGraph.addLinkConnection(d, new Port("p6",7),g,new Port("p3",1));
		conns[7] = myGraph.addLinkConnection(g, new Port("p6",7),h,new Port("p3",1));
		
		
		VPMGraph<OvsSwitch, LinkConnection> myGraph2 = new VPMGraph<>(LinkConnection.class);
		
		OvsSwitch copy = new OvsSwitch("a","1");
		OvsSwitch copy2 = new OvsSwitch("b","2", OvsSwitch.Type.RELAY);
		
		myGraph2.addVertex(copy);
		myGraph2.addVertex(b);
		myGraph2.addVertex(c);
		myGraph2.addVertex(d);
		myGraph2.addVertex(e);
		myGraph2.addVertex(f);
		myGraph2.addVertex(g);
		myGraph2.addVertex(h);
		
		LinkConnection[] conns2 = new LinkConnection[8];
	    conns2[0] = myGraph2.addLinkConnection(copy, new Port("p1",1),copy2,new Port("p2",2));
		conns2[1] = myGraph2.addLinkConnection(c, new Port("p2",2),b,new Port("p3",1));
		conns2[2] = myGraph2.addLinkConnection(b, new Port("p3",1),d,new Port("p4",2));
		conns2[3] = myGraph2.addLinkConnection(d, new Port("p1",4),e,new Port("p2",1));
		conns2[4] = myGraph2.addLinkConnection(f, new Port("p1",4),b,new Port("p2",1));
		conns2[5] = myGraph2.addLinkConnection(a, new Port("p5",5),f,new Port("p5",2));
	    conns2[6] = myGraph2.addLinkConnection(d, new Port("p6",7),g,new Port("p3",1));
		conns2[7] = myGraph2.addLinkConnection(g, new Port("p6",7),h,new Port("p3",1));		
		

		
		
		//myGraph2.equals(myGraph);
		System.out.print(myGraph2.containsEdge(b, c));
		/*OvsSwitch b1 = new OvsSwitch("b","2");
		OvsSwitch d1 = new OvsSwitch("d","4");
		
		for(OvsSwitch o : myGraph.vertexSet()){
			if(o.equals(b1))
				b1 = o;
			else if(o.equals(d1))
				d1 = o;
		}*/
		
		
		
		/*createTree(myGraph);
		potateRelays(myGraph);
		for(LinkConnection l : myGraph.edgeSet()){
			if(l.isTree)
				System.out.println(l);
		}
		
		
		System.out.println("Is connected: "+cIsp.isGraphConnected());
		myGraph.removeEdge(conns[5]);
		myGraph.removeEdge(conns[4]);*/
		
		//cIsp = new ConnectivityInspector<>(myGraph);
		//System.out.println("Is connected: "+cIsp.isGraphConnected());
		/*DijkstraShortestPath<OvsSwitch, LinkConnection> dj = new DijkstraShortestPath<OvsSwitch, LinkConnection>(myGraph, 
				b1, d1);
		
		
		mxGraph dx = NetworkConverter.jpathToMx(dj.getPath());*/
		//mxCodec codec = new mxCodec();	
		//System.out.println(mxUtils.getPrettyXml(codec.encode(dx.getModel())));//
		
		
		
		
		//System.out.println(anotherJ.toString());
	}
	
	/*private static LinkConnection domToLink(Element el){
		return new LinkConnection(el.getAttribute("srcDpid"),el.getAttribute("dstDpid") ,
				new Port(el.getAttribute("srcPort")), new Port(el.getAttribute("dstPort")));
	}

	private static Element linkToDom(Document doc, LinkConnection link){
		org.w3c.dom.Element linkEl = doc.createElement("link");
		linkEl.setAttribute("srcPort", link.srcPort.toString());
		linkEl.setAttribute("dstPort", link.targetPort.toString());
		linkEl.setAttribute("srcDpid", link.src.dpid);
		linkEl.setAttribute("dstDpid", link.target.dpid);
		linkEl.setAttribute("isTree", String.valueOf(link.isTree));

		return linkEl;
	}

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

	public static Graph<OvsSwitch,LinkConnection> mxToJgraphT(mxGraph graph){

		ListenableUndirectedWeightedGraph<OvsSwitch, LinkConnection> myGraph = 
				new ListenableUndirectedWeightedGraph<>(LinkConnection.class);

				HashMap<String, OvsSwitch> switches = new HashMap<String, OvsSwitch>();

				for(Object cell : graph.getChildCells(graph.getDefaultParent(),true, false)){// getting vertexes
					OvsSwitch sw = domToSwitch((Element)(((mxCell)cell).getValue())); //converting

					myGraph.addVertex(sw); //putting it into the graph
					switches.put(sw.dpid, sw);  // and into the hashtable so to get it later
				}

				for(Object cell : graph.getChildCells(graph.getDefaultParent(),false, true)){ //now links
					LinkConnection link = domToLink((Element)(((mxCell)cell).getValue()));
					myGraph.addEdge(switches.get(link.src.dpid), switches.get(link.target.dpid), link);
					myGraph.setEdgeWeight(link, link.getSource().type.getValue() + link.getTarget().type.getValue());
				}

				return myGraph;
	}
	
	public static ListenableUndirectedWeightedGraph<OvsSwitch, LinkConnection> getJgraphTopology(List<OvsSwitch> swList,
			List<LinkConnection> linkList) throws IOException{
		
		ListenableUndirectedWeightedGraph<OvsSwitch, LinkConnection> graph =
				new ListenableUndirectedWeightedGraph<OvsSwitch, LinkConnection>(LinkConnection.class);
		
		HashMap<String, OvsSwitch> switches = new HashMap<String, OvsSwitch>();
		
		for(OvsSwitch sw : swList){// getting vertexes

			graph.addVertex(sw); //putting it into the graph
			switches.put(sw.dpid, sw);  // and into the hashtable so to get it later
		}

		for(LinkConnection link : linkList){ //now links
			graph.addEdge(switches.get(link.src.dpid), switches.get(link.target.dpid), link);
			graph.setEdgeWeight(link, link.getSource().type.getValue() + link.getTarget().type.getValue());
		}
		
		return graph;
		
	}
	
	public static mxGraph getMxTopology(List<OvsSwitch> swList,
			List<LinkConnection> linkList) throws IOException{
		
		mxGraph graph = new mxGraph();
		try{
			graph.getModel().beginUpdate();
			org.w3c.dom.Document doc = mxDomUtils.createDocument();
			HashMap<String, mxCell> vertexes = new HashMap<String, mxCell>();
			
			for(OvsSwitch sw : swList){
				org.w3c.dom.Element swEl = switchToDom(doc,sw);
				vertexes.put(sw.dpid, (mxCell)graph.insertVertex(graph.getDefaultParent(), null, swEl, 10, 10, 
						100, 50));
			}
			
			for(LinkConnection l : linkList){
				Element linkEl = linkToDom(doc,l);

				graph.insertEdge(graph.getDefaultParent(), null, linkEl, vertexes.get(l.src.dpid), 
						vertexes.get(l.target.dpid));
			}
			
		}finally{
			graph.getModel().endUpdate();
		}
		
		return graph;
	}
	
	

	public static mxGraph jgraphToMx(Graph<OvsSwitch, LinkConnection> graph){
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

				myGraph.insertEdge(myGraph.getDefaultParent(), null, linkEl, vertexes.get(l.src.dpid), 
						vertexes.get(l.target.dpid));

			}

		}finally{
			myGraph.getModel().endUpdate();
		}
		return myGraph;

	}
	
	public List<LinkConnection> getLinkConnection(Graph<OvsSwitch, LinkConnection> graph){
		List<LinkConnection> l = new ArrayList<LinkConnection>();
		Iterator<LinkConnection> it = graph.edgeSet().iterator();
		while(it.hasNext())
			l.add(it.next());
		return l;
	}
	
	public List<LinkConnection> getLinkConnection(mxGraph graph){
		List<LinkConnection> l = new ArrayList<LinkConnection>();
		for(Object cell : graph.getChildCells(graph.getDefaultParent(),false,true)){
			l.add(domToLink((Element)(((mxCell)cell).getValue())));
		}
		return l;
		
	}*/
}
