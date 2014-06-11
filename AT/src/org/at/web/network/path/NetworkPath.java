package org.at.web.network.path;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.network.NetworkConverter;
import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.at.network.types.VPMGraph;
import org.at.network.types.VPMGraphHolder;
import org.at.web.network.path.types.VPMPathInfo;
import org.jgrapht.GraphPath;
import org.json.JSONObject;

import com.mxgraph.io.mxCodec;
import com.mxgraph.util.mxXmlUtils;

/**
 * Servlet implementation class VPMPathManager
 */
@WebServlet("/NetworkPath")
public class NetworkPath extends HttpServlet {
	private static final long serialVersionUID = 1L;

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

		VPMPathInfo pathInfo = ((VPMPathManager)getServletContext().getAttribute(VPMPathManager.VPM_PATH_MANAGER)).getPath(srcDpid, targetdpid);


		if(pathInfo != null){
			GraphPath<OvsSwitch, LinkConnection> pathToClient = pathInfo.path;
			String externalAddr = pathInfo.externalAddress;
			jsResp.put("status", "ok_path");
			mxCodec codec = new mxCodec();	
			String xmlString =  mxXmlUtils.getXml(codec.encode(
					(NetworkConverter.jpathToMx(pathToClient)).getModel()));
			jsResp.put("path", xmlString);
			jsResp.put("external", externalAddr);
		}else
			jsResp.put("status", "ok_no_path");

		out.println(jsResp.toString());
		out.close();
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

		String op = jsReq.getString("op");

		try{

			VPMPathManager pathManager = (VPMPathManager)getServletContext().getAttribute(
					VPMPathManager.VPM_PATH_MANAGER);

			synchronized(pathManager){
				
				VPMPathInfo pathToClientInfo = pathManager.getPath(srcDpid, targetdpid);

				GraphPath<OvsSwitch, LinkConnection> pathToClient = null;
				if(pathToClientInfo != null)
					pathToClient = pathToClientInfo.path;

				if(op.equals("add")){
					String external = null;
					if(!jsReq.getBoolean("test"))
						external = jsReq.getString("broadcast");
					else
						external = targetdpid;

					if(pathToClientInfo == null){ //no path was saved
						VPMGraphHolder holder = (VPMGraphHolder)getServletContext().getAttribute(VPMGraphHolder.VPM_GRAPH_HOLDER);
						VPMGraph<OvsSwitch, LinkConnection> currentGraph = holder.getGraph();

						if(currentGraph == null)
							throw new IOException("You need to setup a tree before asking for a path");

						pathToClient = (pathManager.installShortestPath(currentGraph, 
								findOriginal(currentGraph, new OvsSwitch(srcDpid,jsReq.getString("src_ip"))), 
								findOriginal(currentGraph, new OvsSwitch(targetdpid, jsReq.getString("dst_ip"))), external)).path;

					}

					mxCodec codec = new mxCodec();	
					String xmlString =  mxXmlUtils.getXml(codec.encode(
							(NetworkConverter.jpathToMx(pathToClient)).getModel()));
					jsResp.put("path", xmlString);

				}else if(op.equals("del")){
					if(pathToClient != null){ //if there is no path the request makes no sense
						pathManager.uninstallPath(srcDpid, targetdpid);
					}else
						throw new IOException("You are trying to delete a non existent path");
				}

				jsResp.put("status", "ok");
			}

		}catch(IOException ex){
			ex.printStackTrace();
			System.err.println(ex.getMessage());
			jsResp.put("status","error");
			jsResp.put("details", ex.getMessage());
		}finally{
			out.println(jsResp.toString());
			out.close();
		}
	}

}
