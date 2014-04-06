package org.at.web.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Database;
import org.at.floodlight.FloodlightController;
import org.at.floodlight.types.LinkConnection;
import org.at.floodlight.types.OvsSwitch;
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

	private static final String BR_NAME = "br0";
	private static final int BR_PORT = 6640;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public NetworkTopology() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args){
		mxGraph graph = new mxGraph();

		graph.getModel().beginUpdate();

		try{
			Object v1 = graph.insertVertex(graph.getDefaultParent(), null, "Hello", 20, 20, 80, 30);
			Object v2 = graph.insertVertex(graph.getDefaultParent(), null, "World!", 200, 150, 80, 30);
			graph.insertEdge(graph.getDefaultParent(),null,"",v1,v2);
		}finally{
			graph.getModel().endUpdate();
		}

		for(Object cell : graph.getChildCells(graph.getDefaultParent(), true, false)){
			graph.getView().getState(cell).getLabel();
			mxCell c = (mxCell)cell;

			System.out.println(c.isEdge()+" "+c.getAttribute("Label")+"---"+graph.getView().getState(c).getLabel());
		}
		/*
		mxCodec codec = new mxCodec();

		String xml = mxXmlUtils.getXml(codec.encode(graph.getModel()));
		System.out.println("First: "+xml);
		mxGraph newGraph = new mxGraph();
		org.w3c.dom.Node node = mxXmlUtils.parseXml(xml);

		mxCodec decoder = new mxCodec(node.getOwnerDocument());
		decoder.decode(node.getFirstChild(),newGraph.getModel());


		System.out.println(mxXmlUtils.getXml(codec.encode(newGraph.getModel())));*/
		//System.out.println(xml);
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
		FloodlightController controller = new FloodlightController(d.getController());
		d.close();

		return controller;
	}

	private List<LinkConnection> deleteOpposites(List<LinkConnection> original){
		List<LinkConnection> result = new ArrayList<LinkConnection>();

		while(original.size() > 0){
			LinkConnection l = original.remove(0); //taking first element
			for(int i=0;i<original.size();i++){
				if(original.get(i).oppositeLink(l))
					original.remove(i);
			}
			result.add(l);
		}

		return result;
	}



	/**
	 * @throws IOException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject jsResp = new JSONObject();

		try{

			FloodlightController controller = getController();
			List<OvsSwitch> switches = controller.getSwitches();
			List<LinkConnection> connections = deleteOpposites(controller.getSwitchConnections());


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
					graph.insertEdge(graph.getDefaultParent(), null, linkEl, vertexes[getVertexId(connections.get(i).dpidSrc, switches)], 
							vertexes[getVertexId(connections.get(i).dpidDst, switches)]);

				}

			}finally{
				graph.getModel().endUpdate();
			}

			mxCodec codec = new mxCodec();	
			String xmlString =  mxXmlUtils.getXml(codec.encode(graph.getModel()));

			//System.out.println(mxUtils.getPrettyXml(codec.encode(graph.getModel())));

			jsResp.put("status", "ok");
			jsResp.put("graph", xmlString);
		}catch(IOException ex){
			ex.printStackTrace();
			jsResp.put("status","error");
			jsResp.put("details", ex.getMessage());
		}finally{
			out.println(jsResp.toString());
			out.close();
		}

	}

	private LinkConnection domToLink(Element el){
		return new LinkConnection(el.getAttribute("srcDpid"),el.getAttribute("dstDpid") ,
				Integer.parseInt(el.getAttribute("srcPort")),Integer.parseInt(el.getAttribute("dstPort")));
	}

	private Element linkToDom(Document doc, LinkConnection link){
		Element linkEl = doc.createElement("link");
		linkEl.setAttribute("srcPort", String.valueOf(link.srcPort));
		linkEl.setAttribute("dstPort", String.valueOf(link.dstPort));
		linkEl.setAttribute("srcDpid", link.dpidSrc);
		linkEl.setAttribute("dstDpid", link.dpidDst);

		return linkEl;
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
			if(links.get(i).dpidSrc.equals(l.dpidSrc) && links.get(i).dpidDst.equals(l.dpidDst) ||
					links.get(i).dpidSrc.equals(l.dpidDst) && links.get(i).dpidDst.equals(l.dpidSrc))
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

		try{
			Database d = new Database();
			d.connect();
			FloodlightController controller = new FloodlightController(d.getController());
			d.close();

			//we get link connections
			List<LinkConnection> links = deleteOpposites(controller.getSwitchConnections());
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

						String srcPortName = computePortName(l.dpidSrc, l.dpidDst);
						String dstPortName = computePortName(l.dpidDst, l.dpidSrc);
						String srcIpAddr = getSwitchFromDpid(switches, l.dpidSrc).ip;
						String dstIpAddr = getSwitchFromDpid(switches, l.dpidDst).ip;

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
					String srcIpAddr = getSwitchFromDpid(switches, l.dpidSrc).ip;
					String dstIpAddr = getSwitchFromDpid(switches, l.dpidDst).ip;
					String srcPortName = computePortName(l.dpidSrc, l.dpidDst);
					String dstPortName = computePortName(l.dpidDst, l.dpidSrc);

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
		}

		out.println(jResponse.toString());
		out.close();
	}

}
