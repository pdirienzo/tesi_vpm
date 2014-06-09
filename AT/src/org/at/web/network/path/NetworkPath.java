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

	/*private static final String PASSBY_FLOW = "RTP_INTERSWITCH";
	private static final String TO_VNET_FLOW = "RTP_TOVNET";
	private static final String FROM_VNET_FLOW = "RTP_FROMVNET";

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
	 */
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
		/*String pathName = computePathName(srcDpid, targetdpid);

		VPMPathInfoHolder pathHolder = (VPMPathInfoHolder)getServletContext().getAttribute(VPMPathInfoHolder.VPM_PATHS);
		VPMPathInfo pathInfo = pathHolder.get(pathName);*/

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

	/*
	private void addFlows(GraphPath<OvsSwitch,LinkConnection> jpath, String externalBcast) throws IOException{

		VPMSwitchInfoHolder vsHolder = (VPMSwitchInfoHolder)getServletContext().
				getAttribute(VPMSwitchInfoHolder.SWITCH_INFO_HOLDER);


		FloodlightController controller = getController();
		List<OvsSwitch> nodes = Graphs.getPathVertexList(jpath);

		//this is a template with informations common to each flow we are going to install
		//according to the specific situation we will vary the:
		//-name
		//-dpid 
		//-ingress port (not always)
		//-output rule
		JSONObject basicFlow = new JSONObject()
		.put("cookie", (new Random()).nextInt())
		.put("priority", "100")
		.put("dst-ip", "10.0.0.255")
		.put("ether-type", "0x0800")
		.put("active", "true");

		for(int i=0;i<nodes.size();i++){
			try{
				VPMSwitchInfo infos = vsHolder.get(nodes.get(i).dpid);

				if(infos == null){ //first time this switch is crossed by a path
					infos = new VPMSwitchInfo(nodes.get(i));
					vsHolder.put(infos.sw.dpid, infos);
				}
				basicFlow.put("switch", infos.sw.dpid); //setting the dpid for this flow rule

				//if there is a VM we send the traffic to it.
				//As VM are always there, just one rule is enough regardless
				//of the number of paths traversing this switch
				if(infos.getCounter() == 0 && infos.sw.type != OvsSwitch.Type.ROOT){
					for(Port vnet : controller.getVnetPorts(infos.sw))
						infos.addVnetPort(vnet.number);

					if(infos.getVnetNumber() > 0){ 
						JSONObject toVMFlow = new JSONObject(basicFlow);
						toVMFlow.put("name", TO_VNET_FLOW+infos.sw.dpid.replace(":", ""));
						toVMFlow.put("actions", infos.getCurrentVnetActionString());
						controller.addStaticFlow(toVMFlow);
					}
					vsHolder.put(nodes.get(i).dpid, infos);
				}

				String action = null; //the action string
				if(infos.sw.type != OvsSwitch.Type.LEAF){ //if it's not a final destination 
					OvsSwitch ovsDst = nodes.get(i+1); //getting next switch 
					String path = computePathName(infos.sw.dpid, ovsDst.dpid);
					//this is the gre tunnel connecting this switch to the next one
					infos.addOutputPort(controller.getPortNumber(infos.sw, "gre"+path));
					action = infos.getCurrentOutputActionString();
				}else{
					if(test)
						action = "set-dst-ip="+infos.sw.ip;
					else
						action = "set-dst-ip="+externalBcast;

					action += ",output="+controller.getPortNumber(infos.sw, "patch1");
				}

				if(infos.getVnetNumber() > 0 && infos.sw.type != OvsSwitch.Type.ROOT){ //if there are some vnets then we have to create a flow rule for each
					//outgoing traffic from them

					JSONObject fromVMFlow = new JSONObject(basicFlow);

					//now for traffic from vm
					fromVMFlow.put("actions", action);

					for(Integer vnetN : infos.getVMPorts()){
						fromVMFlow.put("name", FROM_VNET_FLOW + vnetN + "_" + infos.sw.dpid.replace(":", ""));
						fromVMFlow.put("ingress-port", vnetN);
						controller.addStaticFlow(fromVMFlow);
					}

				}else{ //if no vms we just make the traffic flow to next openvswitch
					basicFlow.put("name", PASSBY_FLOW+infos.sw.dpid.replace(":", ""));
					basicFlow.put("actions", action);
					controller.addStaticFlow(basicFlow);
				}

				infos.incrementCounter(); //on this switch we have a new route passing by
			}catch(IOException ex){
				//this way if any exception occurs we make sure to decrement the counter so to have
				//a consistent data structure
				throw ex;
			}

		}
	}

	private void deleteFlows(GraphPath<OvsSwitch,LinkConnection> jpath) throws IOException{
		VPMSwitchInfoHolder vsHolder = (VPMSwitchInfoHolder)getServletContext().
				getAttribute(VPMSwitchInfoHolder.SWITCH_INFO_HOLDER);


		FloodlightController controller = getController();
		List<OvsSwitch> nodes = Graphs.getPathVertexList(jpath);

		for(int i=0;i<nodes.size();i++){
			VPMSwitchInfo infos = vsHolder.get(nodes.get(i).dpid);

			try{

				JSONObject basicFlow = new JSONObject()
				.put("switch", infos.sw.dpid)
				.put("cookie", (new Random()).nextInt())
				.put("priority", "100")
				.put("dst-ip", "10.0.0.255")
				.put("ether-type", "0x0800")
				.put("active", "true");


				if(infos.getCounter() > 1){ //more than one path, we have to remove ports and rewrite rules
						OvsSwitch ovsDst = nodes.get(i+1); //getting next switch 
						String path = computePathName(infos.sw.dpid, ovsDst.dpid);
						//this is the gre tunnel connecting this switch to the next one
						infos.removeOutputPort(controller.getPortNumber(infos.sw, "gre"+path));
						basicFlow.put("actions",infos.getCurrentOutputActionString());

						if(infos.sw.type != OvsSwitch.Type.ROOT && infos.getVnetNumber() > 0){
							JSONObject fromVMFlow = new JSONObject(basicFlow);

							for(Integer vnetN : infos.getVMPorts()){
								fromVMFlow.put("name", FROM_VNET_FLOW + vnetN + "_" + infos.sw.dpid.replace(":", ""));
								fromVMFlow.put("ingress-port", vnetN);
								controller.addStaticFlow(fromVMFlow);
							}
						}else{
							basicFlow.put("name", PASSBY_FLOW+infos.sw.dpid.replace(":", ""));
							controller.addStaticFlow(basicFlow);
						}

				}else{ //just one path: we delete the rules
					if(infos.sw.type != OvsSwitch.Type.ROOT && infos.getVnetNumber() > 0){
						controller.deleteFlow(infos.sw.dpid, TO_VNET_FLOW+infos.sw.dpid.replace(":", ""));

						for(Integer vnetN : infos.getVMPorts()){
							controller.deleteFlow(infos.sw.dpid, FROM_VNET_FLOW + vnetN + "_" + infos.sw.dpid.replace(":", ""));
							infos.removeVnetPort(vnetN);
						}

					}else
						controller.deleteFlow(infos.sw.dpid, PASSBY_FLOW+infos.sw.dpid.replace(":", ""));
				}

				infos.decrementCounter(); //a path less for this switch

			}catch(IOException ex){
				//this way if any exception occurs we make sure to decrement the counter so to have
				//a consistent data structure
				infos.incrementCounter();
				throw ex;
			}
		}
	}*/

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

		//String pathName = computePathName(srcDpid, targetdpid);

		String op = jsReq.getString("op");

		try{

			/*VPMPathInfoHolder pathHolder = (VPMPathInfoHolder)getServletContext().getAttribute(VPMPathInfoHolder.VPM_PATHS);
			VPMPathInfo pathToClientInfo = pathHolder.get(pathName);*/
			VPMPathManager pathManager = (VPMPathManager)getServletContext().getAttribute(
					VPMPathManager.VPM_PATH_MANAGER);

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


					/*DijkstraShortestPath<OvsSwitch, LinkConnection> shortest = new DijkstraShortestPath<OvsSwitch, LinkConnection>(currentGraph,
							findOriginal(currentGraph, new OvsSwitch(srcDpid,jsReq.getString("src_ip"))), 
							findOriginal(currentGraph, new OvsSwitch(targetdpid, jsReq.getString("dst_ip"))));

					addFlows(shortest.getPath(), jsReq.getString("broadcast"));

					pathHolder.put(pathName, new VPMPathInfo(shortest.getPath(),jsReq.getString("broadcast")));
					pathToClient = shortest.getPath();*/

					pathManager.installShortestPath(currentGraph, 
							findOriginal(currentGraph, new OvsSwitch(srcDpid,jsReq.getString("src_ip"))), 
							findOriginal(currentGraph, new OvsSwitch(targetdpid, jsReq.getString("dst_ip"))), external);

					pathToClient = pathManager.getPath(srcDpid, targetdpid).path;
				}

				mxCodec codec = new mxCodec();	
				String xmlString =  mxXmlUtils.getXml(codec.encode(
						(NetworkConverter.jpathToMx(pathToClient)).getModel()));
				jsResp.put("path", xmlString);

			}else if(op.equals("del")){
				if(pathToClient != null){ //if there is no path the request makes no sense
					/*deleteFlows(pathToClient);
					pathHolder.remove(pathName);*/
					pathManager.uninstallPath(srcDpid, targetdpid);
				}else
					throw new IOException("You are trying to delete a non existent path");
			}

			jsResp.put("status", "ok");

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
