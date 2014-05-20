package org.at.network;

import java.util.HashMap;
import java.util.Iterator;

import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.at.network.types.OvsSwitch.Type;
import org.at.network.types.Port;
import org.at.network.types.VPMGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.DijkstraShortestPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxUtils;
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
	
	public static VPMGraph<OvsSwitch,LinkConnection> mxToJgraphT(mxGraph graph){
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
			
			LinkConnection link = myGraph.addLinkConnection(source, new Port(((Element)cell.getValue()).getAttribute("srcPort")), target, 
					new Port(((Element)cell.getValue()).getAttribute("dstPort")), Boolean.valueOf(((Element)cell.getValue()).getAttribute("isTree")));
		
			myGraph.setEdgeWeight(link, link.getSource().type.getValue() + link.getTarget().type.getValue());
		}
		
		return myGraph;
	}
	
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
	
	public static void main(String[] args){
		VPMGraph<OvsSwitch, LinkConnection> myGraph = new VPMGraph<>(LinkConnection.class);
		
		OvsSwitch a = new OvsSwitch("a","1");
		OvsSwitch b = new OvsSwitch("b","2");
		OvsSwitch c = new OvsSwitch("c","3");
		OvsSwitch d = new OvsSwitch("d","4");
		
		myGraph.addVertex(a);
		myGraph.addVertex(b);
		myGraph.addVertex(c);
		myGraph.addVertex(d);
		
		LinkConnection[] conns = new LinkConnection[4];
		conns[0] = myGraph.addLinkConnection(a, new Port("p1",1),b,new Port("p2",2));
		conns[1] = myGraph.addLinkConnection(b, new Port("p2",2),c,new Port("p3",1));
		conns[2] = myGraph.addLinkConnection(c, new Port("p3",1),d,new Port("p4",2),true);
		conns[3] = myGraph.addLinkConnection(a, new Port("p1",4),c,new Port("p2",1));
		
		OvsSwitch b1 = new OvsSwitch("b","2");
		OvsSwitch d1 = new OvsSwitch("d","4");
		
		for(OvsSwitch o : myGraph.vertexSet()){
			if(o.equals(b1))
				b1 = o;
			else if(o.equals(d1))
				d1 = o;
		}
		
		
		DijkstraShortestPath<OvsSwitch, LinkConnection> dj = new DijkstraShortestPath<OvsSwitch, LinkConnection>(myGraph, 
				b1, d1);
		
		mxGraph dx = NetworkConverter.jpathToMx(dj.getPath());
		mxCodec codec = new mxCodec();	
		System.out.println(mxUtils.getPrettyXml(codec.encode(dx.getModel())));
		
		mxGraph mx = NetworkConverter.jgraphToMx(myGraph);
		//System.out.println(mxUtils.getPrettyXml(codec.encode(mx.getModel())));
		
		
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
