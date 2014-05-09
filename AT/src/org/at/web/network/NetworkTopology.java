package org.at.web.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Controller;
import org.at.db.Database;
import org.at.floodlight.FloodlightController;
import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.json.JSONObject;
import org.opendaylight.ovsdb.lib.notation.OvsdbOptions;
import org.opendaylight.ovsdb.lib.standalone.DefaultOvsdbClient;
import org.opendaylight.ovsdb.lib.standalone.OvsdbException;
import org.opendaylight.ovsdb.lib.table.Interface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

/**
 * Servlet implementation class GetNetworkTopology
 */
@WebServlet("/NetworkTopology")
public class NetworkTopology extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String BR_NAME;
	private int BR_PORT;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public NetworkTopology() {
		super();
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		
		Properties props = (Properties)getServletContext().getAttribute("properties");
		BR_NAME = props.getProperty("bridge_name");
		BR_PORT = Integer.parseInt(props.getProperty("ovs_manager_port"));
	}

	private int getVertexId(String dpid,List<OvsSwitch> switches){
		int index = -1;
		int i = 0;

		while((index==-1) && (i<switches.size())){
			if(switches.get(i).dpid.equals(dpid))
				index=i;
			else
				i++;
		}
		return i;
	}

	private FloodlightController getController() throws IOException{
		Database d = new Database();
		d.connect();
		Controller c = d.getController();
		d.close();
		
		if(c==null)
			return null;
		
		FloodlightController controller = new FloodlightController(c);
		
		return controller;
	}

	

	private void createTree(List<LinkConnection> links){
		ListenableUndirectedGraph<String,LinkConnection> graph = new ListenableUndirectedGraph<String, LinkConnection>(LinkConnection.class);
		
		for(LinkConnection link : links){
			graph.addVertex(link.src);
			graph.addVertex(link.target);
			graph.addEdge(link.src, link.target,link);
		
		}
		
		KruskalMinimumSpanningTree<String, LinkConnection> k = new KruskalMinimumSpanningTree<String,LinkConnection>(graph);
		
		Iterator<LinkConnection> iterator = k.getMinimumSpanningTreeEdgeSet().iterator();
		
		while(iterator.hasNext()){
			iterator.next().isTree = true;
		}
	}


	/**
	 * @throws IOException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject jsResp = new JSONObject();

		try{

			FloodlightController controller = getController();
			
			if(controller != null){
				List<OvsSwitch> switches = controller.getSwitches();
				List<LinkConnection> connections = controller.getSwitchConnections(true);
				
				//TODO think a better way (tree should be stored so to not repeat this
				createTree(connections);
				
				//*************************************************************


				mxGraph graph = new mxGraph();
				mxCell[] vertexes = new mxCell[switches.size()];

				graph.getModel().beginUpdate();
				org.w3c.dom.Document doc = mxDomUtils.createDocument();

				try{	
					for(int i=0;i<switches.size();i++){
						org.w3c.dom.Element swEl = doc.createElement("switch");
						swEl.setAttribute("dpid", switches.get(i).dpid);
						swEl.setAttribute("ip", switches.get(i).ip);
						vertexes[i] = (mxCell)graph.insertVertex(graph.getDefaultParent(), null, swEl, 10, 10, 
								100, 50);
					}

					for(int i=0;i<connections.size();i++){
						Element linkEl = linkToDom(doc,connections.get(i));
						graph.insertEdge(graph.getDefaultParent(), null, linkEl, vertexes[getVertexId(connections.get(i).src, switches)], 
								vertexes[getVertexId(connections.get(i).target, switches)]);

					}

				}finally{
					graph.getModel().endUpdate();
				}

				mxCodec codec = new mxCodec();	
				String xmlString =  mxXmlUtils.getXml(codec.encode(graph.getModel()));

				//System.out.println(mxUtils.getPrettyXml(codec.encode(graph.getModel())));

				jsResp.put("status", "ok");
				jsResp.put("graph", xmlString);
			
			}else{
				jsResp.put("status", "error");
				jsResp.put("details", "No controller set");
			}
			
			
		}catch(IOException ex){
			System.err.println(ex.getMessage());
			jsResp.put("status","error");
			jsResp.put("details", "Communication error with "+ex.getMessage());
		}finally{
			out.println(jsResp.toString());
			out.close();
		}*/

	}

	

	

	private OvsSwitch getSwitchFromDpid(List<OvsSwitch> switches,String dpid){
		boolean found = false;
		OvsSwitch sw = null;

		int i=0;
		while((!found) && (i<switches.size())){
			if((sw=switches.get(i)).dpid.equals(dpid)){
				found = true;
			}else
				i++;
		}

		if(found)
			return sw;
		else
			return null;
	}

	/**
	 * TODO experimental, do it better...it just takes last 2 symbols of dpids
	 * @param dpidSrc
	 * @param dpidDst
	 * @return
	 */
	private String computePortName(String dpidSrc,String dpidDst){
		StringBuilder sb = new StringBuilder();
		sb.append("gre");
		String[] subs = dpidSrc.split(":");
		sb.append(subs[subs.length-2]);
		sb.append(subs[subs.length-1]);

		subs = dpidDst.split(":");
		sb.append(subs[subs.length-2]);
		sb.append(subs[subs.length-1]);

		return sb.toString();

	}

	private int linkPresent(LinkConnection l, List<LinkConnection> links){
		int index = -1;
		int i = 0;

		while((index==-1) && (i<links.size())){
			if(links.get(i).src.equals(l.src) && links.get(i).target.equals(l.target) ||
					links.get(i).src.equals(l.target) && links.get(i).target.equals(l.src))
				index = i;
			else
				i++;
		}

		return index;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = new PrintWriter(response.getOutputStream());
		response.setContentType("application/json");
		JSONObject jResponse = new JSONObject();
/*
		try{
			Database d = new Database();
			d.connect();
			FloodlightController controller = new FloodlightController(d.getController());
			d.close();

			//we get link connections
			List<LinkConnection> links = controller.getSwitchConnections(true);
			//we get switches
			List<OvsSwitch> switches = controller.getSwitches();

			//we get user made topology
			mxGraph graph = new mxGraph();
			org.w3c.dom.Node node = mxXmlUtils.parseXml(request.getParameter("xml"));
			mxCodec decoder = new mxCodec(node.getOwnerDocument());
			decoder.decode(node.getFirstChild(),graph.getModel());

			for(Object cell : graph.getChildCells(graph.getDefaultParent(),false,true)){//graph.getDefaultParent(), false, true)){ //getting edges
				Element element = (Element)(((mxCell)cell).getValue());

				if(element.getNodeName().equals("link")){
					LinkConnection l = domToLink(element);
					int index = linkPresent(l, links);
					if(index != -1){ //we simply remove the link from the list: it will not be processed
						links.remove(index);

					}else{//we add the link phisically to the switches

						String srcPortName = computePortName(l.src, l.target);
						String dstPortName = computePortName(l.target, l.src);
						String srcIpAddr = getSwitchFromDpid(switches, l.src).ip;
						String dstIpAddr = getSwitchFromDpid(switches, l.target).ip;

						//1. starting from source switch
						DefaultOvsdbClient client = new DefaultOvsdbClient(srcIpAddr, BR_PORT);
						String ovs = null;

						ovs = client.getOvsdbNames()[0];
						OvsdbOptions opts = new OvsdbOptions();
						opts.put(OvsdbOptions.REMOTE_IP, dstIpAddr);
						client.addPort(ovs, BR_NAME, srcPortName, Interface.Type.gre.name(),0,null,opts);

						//2. now the other one
						client = new DefaultOvsdbClient(dstIpAddr, BR_PORT);
						opts = new OvsdbOptions();
						opts.put(OvsdbOptions.REMOTE_IP, srcIpAddr);
						client.addPort(ovs, BR_NAME, dstPortName, Interface.Type.gre.name(),0,null,opts);
					}
				}
			}

			if(links.size() > 0){ //user deleted some link
				for(LinkConnection l : links){
					String srcIpAddr = getSwitchFromDpid(switches, l.src).ip;
					String dstIpAddr = getSwitchFromDpid(switches, l.target).ip;
					String srcPortName = computePortName(l.src, l.target);
					String dstPortName = computePortName(l.target, l.src);

					DefaultOvsdbClient client = new DefaultOvsdbClient(srcIpAddr, BR_PORT);
					String ovs = client.getOvsdbNames()[0];
					//1.
					client.deletePort(ovs, BR_NAME,srcPortName);

					//2.
					client = new DefaultOvsdbClient(dstIpAddr, BR_PORT);
					client.deletePort(ovs, BR_NAME,dstPortName);


				}
			}

			jResponse.put("status", "ok");

		}catch(IOException | OvsdbException ex){
			jResponse.put("status", "error");
			jResponse.put("details", ex.getMessage());
			ex.printStackTrace();
		}*/

		out.println(jResponse.toString());
		out.close();
	}

}
