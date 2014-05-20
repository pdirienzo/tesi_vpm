package org.at.web.network.path;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
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
				
				System.out.println("DEBUG, here's the edge list");
				
				for(LinkConnection l : shortest.getPathEdgeList()){
					System.out.println(l);
				}
				
				pathToClient = NetworkConverter.jgraphToMx((VPMGraph<OvsSwitch,LinkConnection>)shortest.getPath().getGraph());
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
