package org.at.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.at.network.types.OvsSwitch.Type;
import org.at.network.types.Port;
import org.jgrapht.Graph;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.view.mxGraph;

public final class NetworkConverter {

	private static LinkConnection domToLink(Element el){
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
		
	}
}
