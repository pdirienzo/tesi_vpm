package org.at.web.network.path;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Controller;
import org.at.db.Database;
import org.at.floodlight.FloodlightController;
import org.at.network.NetworkConverter;
import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.at.network.types.VPMGraph;
import org.at.network.types.VPMGraphHolder;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.DijkstraShortestPath;
import org.json.JSONObject;

import com.mxgraph.io.mxCodec;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

/**
 * Servlet implementation class VPMPathManager
 */
@WebServlet("/VPMPathManager")
public class VPMPathManager extends HttpServlet {
	private static final long serialVersionUID = 1L;

	

	private FloodlightController getController() throws IOException{
		Database d = new Database();
		d.connect();
		Controller c = d.getController();
		d.close();

		if(c==null)
			throw new IOException("couldn't contact controller");

		FloodlightController controller = new FloodlightController(c);

		return controller;
	}

	//TODO temporary
	private String computePathName(String dpidSrc,String dpidDst){
		StringBuilder sb = new StringBuilder();

		String[] subs = dpidSrc.split(":");
		sb.append(subs[subs.length-2]);
		sb.append(subs[subs.length-1]);

		subs = dpidDst.split(":");
		sb.append(subs[subs.length-2]);
		sb.append(subs[subs.length-1]);

		return sb.toString();
	}
	
	//TODO stupid workaround
		private OvsSwitch findOriginal(VPMGraph<OvsSwitch, LinkConnection> graph, OvsSwitch ovs){
			OvsSwitch found = null;
			
			Iterator<OvsSwitch> ves = graph.vertexSet().iterator();
			while((found == null) && ves.hasNext()){
				OvsSwitch temp = ves.next();
				if(temp.equals(ovs))
					found = temp;
			}
			
			return found;
		}

	/**
	 * @throws IOException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject jsResp = new JSONObject();
		
		String srcDpid = request.getParameter("src_dpid");
		String targetdpid = request.getParameter("dst_dpid");
		String pathName = computePathName(srcDpid, targetdpid);
		
		PathHolder pathHolder = (PathHolder)getServletContext().getAttribute(PathHolder.VPM_PATHS);
		mxGraph pathToClient = pathHolder.get(pathName);

		if(pathToClient != null){
			jsResp.put("status", "ok_path");
			mxCodec codec = new mxCodec();	
			String xmlString =  mxXmlUtils.getXml(codec.encode(
					(pathToClient).getModel()));
			jsResp.put("path", xmlString);
		}else
			jsResp.put("status", "ok_no_path");
		
		out.println(jsResp.toString());
		out.close();
	}
	
	private void setupFlows(GraphPath<OvsSwitch,LinkConnection> jpath) throws IOException{
		FloodlightController controller = getController();
		List<OvsSwitch> nodes = Graphs.getPathVertexList(jpath);
		for(int i=0;i<nodes.size()-1;i++){
			OvsSwitch ovsSrc = nodes.get(i);
			OvsSwitch ovsDst = nodes.get(i+1);
			String path = computePathName(ovsSrc.dpid, ovsDst.dpid);
			
			JSONObject data = new JSONObject()
			.put("name", "flow-stream-"+path)
			.put("switch", ovsSrc.dpid)
			.put("cookie", (new Random()).nextInt())
			.put("priority", "100")
			.put("dst-ip", "10.0.0.255")
			//.put("ingress-port", "3")
			.put("ether-type", "0x0800")
			.put("active", "true")
			.put("actions", "output="+controller.getPortNumber(ovsSrc, "gre"+path));
			controller.addStaticFlow(data);
		}
		
		OvsSwitch endNode = nodes.get(nodes.size()-1);
		//the final flow to output it on the physical network
		JSONObject data = new JSONObject()
		.put("name", "flow-stream-"+endNode.dpid+"patch")
		.put("switch", endNode.dpid)
		.put("cookie", (new Random()).nextInt())
		.put("priority", "100")
		.put("dst-ip", "10.0.0.255")
		//.put("ingress-port", "3")
		.put("ether-type", "0x0800")
		.put("active", "true")
		.put("actions", "set-dst-ip=255.255.255.255,output="+controller.getPortNumber(endNode, "patch1"));//need to know original network
		controller.addStaticFlow(data);
	}
	
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject jsResp = new JSONObject();
		
		JSONObject jsReq = new JSONObject(request.getParameter("path"));
		String srcDpid = jsReq.getString("src_dpid");
		String targetdpid = jsReq.getString("dst_dpid");
		String pathName = computePathName(srcDpid, targetdpid);

		try{
			PathHolder pathHolder = (PathHolder)getServletContext().getAttribute(PathHolder.VPM_PATHS);
			mxGraph pathToClient = pathHolder.get(pathName);

			if(pathToClient == null){ //no path was saved
				VPMGraphHolder holder = (VPMGraphHolder)getServletContext().getAttribute(VPMGraphHolder.VPM_GRAPH_HOLDER);
				VPMGraph<OvsSwitch, LinkConnection> currentGraph = holder.getGraph();

				if(currentGraph == null)
					currentGraph = getController().getTopology();
				
			
				DijkstraShortestPath<OvsSwitch, LinkConnection> shortest = new DijkstraShortestPath<OvsSwitch, LinkConnection>(currentGraph,
						findOriginal(currentGraph, new OvsSwitch(srcDpid,jsReq.getString("src_ip"))), 
						findOriginal(currentGraph, new OvsSwitch(targetdpid, jsReq.getString("dst_ip"))));
				
				setupFlows(shortest.getPath());
				pathToClient = NetworkConverter.jpathToMx(shortest.getPath());
				pathHolder.put(pathName, pathToClient);
				
			}

			jsResp.put("status", "ok");
			mxCodec codec = new mxCodec();	
			String xmlString =  mxXmlUtils.getXml(codec.encode(
					(pathToClient).getModel()));
			jsResp.put("path", xmlString);

		}catch(IOException ex){
			System.err.println(ex.getMessage());
			jsResp.put("status","error");
			jsResp.put("details", ex.getMessage());
		}finally{
			out.println(jsResp.toString());
			out.close();
		}
	}

}
