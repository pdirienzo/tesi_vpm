package org.at.web.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

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

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

/**
 * Servlet implementation class GetNetworkTopology
 */
@WebServlet("/NetworkTopology")
public class NetworkTopology extends HttpServlet {
	private static final long serialVersionUID = 1L;

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

	private int getVertexId(String dpid,OvsSwitch[] switches){
		int index = -1;
		int i = 0;

		while((index==-1) && (i<switches.length)){
			if(switches[i].dpid.equals(dpid))
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
			OvsSwitch[] switches = controller.getSwitches();
			LinkConnection[] connections = controller.getSwitchConnections();


			mxGraph graph = new mxGraph();
			mxCell[] vertexes = new mxCell[switches.length];

			graph.getModel().beginUpdate();
			org.w3c.dom.Document doc = mxDomUtils.createDocument();

			try{	
				for(int i=0;i<switches.length;i++){
					org.w3c.dom.Element swEl = doc.createElement("switch");
					swEl.setAttribute("dpid", switches[i].dpid);
					swEl.setAttribute("ip", switches[i].ip);
					vertexes[i] = (mxCell)graph.insertVertex(graph.getDefaultParent(), null, swEl, 200*i, 10, 
							150, 30);
				}

				for(int i=0;i<connections.length;i++){
					org.w3c.dom.Element linkEl = doc.createElement("link");
					linkEl.setAttribute("srcPort", String.valueOf(connections[i].srcPort));
					linkEl.setAttribute("dstPort", String.valueOf(connections[i].dstPort));
					graph.insertEdge(graph.getDefaultParent(), null, linkEl, vertexes[getVertexId(connections[i].dpidSrc, switches)], 
							vertexes[getVertexId(connections[i].dpidDst, switches)]);
				
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

	private boolean linkExists(LinkConnection[] links,String src,String dst){
		boolean result = false;
		int i = 0;

		while((!result) && i<links.length){
			if(links[i].dpidSrc.equals(src) && links[i].dpidDst.equals(dst))
				result = true;
			else 
				i++;
		}
		return result;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = new PrintWriter(response.getOutputStream());
		Database d = new Database();
		d.connect();
		FloodlightController controller = new FloodlightController(d.getController());
		d.close();

		LinkConnection[] links = controller.getSwitchConnections();

		mxGraph graph = new mxGraph();
		org.w3c.dom.Node node = mxXmlUtils.parseXml(request.getParameter("xml"));
		mxCodec decoder = new mxCodec(node.getOwnerDocument());
		decoder.decode(node.getFirstChild(),graph.getModel());


		for(Object cell : graph.getChildCells(graph.getDefaultParent(), false, true)){ //getting edges
			
		}

		out.println("ok");
		out.close();
	}

}
