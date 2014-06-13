package org.at.web.network.events;

import java.io.IOException;
import java.util.Iterator;

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
import org.at.network.types.Port;
import org.at.network.types.VPMGraph;
import org.at.network.types.VPMGraphHolder;
import org.at.web.network.path.VPMPathManager;
import org.at.web.network.path.types.VPMSwitchInfo;
import org.json.JSONObject;

/**
 * Servlet implementation class VPMToleranceManager
 */
@WebServlet("/VPMEventListener")
public class VPMEventListener extends HttpServlet {
	private static final long serialVersionUID = 1L;

	//event types
	private enum Event { TOPOLOGY, VM };
	private enum VM_OP { ADD, REMOVE };
	private enum TOPOLOGY_OP { ADD, REMOVE };

	private VPMPathManager pathManager;

	@Override
	public void init() throws ServletException {
		super.init();
		pathManager = (VPMPathManager)getServletContext().getAttribute(VPMPathManager.VPM_PATH_MANAGER);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

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

	private JSONObject getToVnetFlow(VPMSwitchInfo infos){
		JSONObject flow = null;
		Iterator<String> it = infos.flows.keySet().iterator();

		while((flow == null) && it.hasNext()){
			String key = it.next();
			if(key.startsWith(VPMPathManager.TO_VNET_FLOW))
				flow = infos.flows.get(key);
		}

		return flow;
	}

	private JSONObject getFromVnetFlow(VPMSwitchInfo infos, int vnetN){
		JSONObject flow = null;
		Iterator<String> it = infos.flows.keySet().iterator();

		while((flow == null) && it.hasNext()){
			String key = it.next();
			if(key.equals(VPMPathManager.FROM_VNET_FLOW + vnetN + "_" + infos.sw.dpid.replace(":", "")))
				flow = infos.flows.get(key);
		}

		return flow;
	}


	private JSONObject getPassbyFlow(VPMSwitchInfo infos){
		JSONObject flow = null;
		Iterator<String> it = infos.flows.keySet().iterator();

		while((flow == null) && it.hasNext()){
			String key = it.next();
			if(key.equals(VPMPathManager.PASSBY_FLOW+infos.sw.dpid.replace(":", "")))
				flow = infos.flows.get(key);

		}

		return flow;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject event = new JSONObject(request.getParameter("data"));
		switch(Event.valueOf(event.getString("type"))){

		case TOPOLOGY:
			VPMGraph<OvsSwitch, LinkConnection> network = ((VPMGraphHolder)getServletContext().getAttribute(VPMGraphHolder.VPM_GRAPH_HOLDER)).getGraph();
			if(network != null){
				for(OvsSwitch sw : network.vertexSet())
					System.out.println(sw);
				
				TOPOLOGY_OP topOp = TOPOLOGY_OP.valueOf(event.getString("op"));
				if(topOp == TOPOLOGY_OP.REMOVE){
					for(JSONObject rem : event.getJSONArray("result")){
						if(rem.getString("type").equals("switch")){
							OvsSwitch target = new OvsSwitch(rem.getString("dpid"),"");
							System.out.println("--> "+target);
							System.out.println("contains vertex: "+network.containsVertex(target));
						}else if (rem.getString("type").equals("link")){
							System.out.println("contains edge: "+
									network.containsEdge(new OvsSwitch(rem.getString("src-dpid"),""), 
									new OvsSwitch(rem.getString("dst-dpid"),"")));
						}
					}
				}
			}
			
			break;

		case VM:
			synchronized(pathManager){
				VPMSwitchInfo swInfos = pathManager.getSwitchInfos(event.getString("switch"));
				if(swInfos != null){
					if(swInfos.sw.type != OvsSwitch.Type.ROOT){ //we are creating a new rule only if sw is not a root 
						VM_OP vmOp = VM_OP.valueOf(event.getString("op"));
						Port vnetPort = new Port(event.getString("vnet"));
						FloodlightController controller = getController();
						if(vmOp == VM_OP.ADD){
							System.out.println("added a new vm "+vnetPort);

							swInfos.addVnetPort(vnetPort.number);

							JSONObject fromVnet = null;

							if(swInfos.getVnetNumber() > 1){ //in this case there is a rule for an existing vnet, so we have to alter it
								//to add this new one

								System.out.println("There is already a vm on this switch");
								JSONObject flow = getToVnetFlow(swInfos);
								flow.put("actions", swInfos.getCurrentVnetActionString()); //adding the port to output rules

								controller.addStaticFlow(flow);
								swInfos.flows.put(flow.getString("name"), flow);

								fromVnet = getFromVnetFlow(swInfos, vnetPort.number);
								fromVnet.put("ingress-port", vnetPort.number);

								controller.addStaticFlow(fromVnet);
								swInfos.flows.put(fromVnet.getString("name"), fromVnet);

							}else{ //in this other case we have a passby rule we have to delete, and then add a couple rules to forward traffic
								//to this vnet

								System.out.println("No vms...caos!");
								JSONObject passBy = getPassbyFlow(swInfos);
								System.out.println("So, this is the name: "+passBy.getString("name"));


								//now we have to create a couple rules
								//first to vnet
								JSONObject toVnet = new JSONObject(passBy);
								toVnet.put("name", VPMPathManager.TO_VNET_FLOW+swInfos.sw.dpid.replace(":", ""));
								toVnet.put("actions", swInfos.getCurrentVnetActionString());

								//from vnet
								fromVnet = new JSONObject(passBy)
								.put("name", VPMPathManager.FROM_VNET_FLOW+vnetPort.number+ "_" + swInfos.sw.dpid.replace(":", ""))
								.put("ingress-port", vnetPort.number);

								System.out.println("removing "+passBy.getString("name"));

								controller.deleteFlow(swInfos.sw.dpid, passBy.getString("name")); //cleaning passBy flow
								System.out.println("adding "+toVnet.getString("name"));
								controller.addStaticFlow(toVnet);

								System.out.println("adding "+fromVnet.getString("name"));
								controller.addStaticFlow(fromVnet);	

								//updating data structure
								swInfos.flows.put(toVnet.getString("name"), toVnet);
								swInfos.flows.put(fromVnet.getString("name"), fromVnet);
								swInfos.flows.remove(passBy);

							}

						}else if (vmOp == VM_OP.REMOVE){
							System.out.println("removed a port");
							swInfos.removeVnetPort(vnetPort.number);

							if(swInfos.getVnetNumber() > 0){ //in this case there are other vms so we have to rewrite the flow rule
								System.out.println("There are still more vms on this switch");
								JSONObject flow = getToVnetFlow(swInfos);
								flow.put("actions", swInfos.getCurrentVnetActionString()); //adding the port to output rules

								//altering rule to vnet to exclude this vnet from output ports
								controller.addStaticFlow(flow);
								//removing the rule from this vnet to external
								controller.deleteFlow(swInfos.sw.dpid, VPMPathManager.FROM_VNET_FLOW+vnetPort.number+ "_" + swInfos.sw.dpid.replace(":", ""));

								swInfos.flows.put(flow.getString("name"), flow);
								swInfos.flows.remove(VPMPathManager.FROM_VNET_FLOW+vnetPort.number+ "_" + swInfos.sw.dpid.replace(":", ""));

							}else{ //there was just one vnet so now we need to remove 2 rules and install a passby one

								//adding passByrule
								JSONObject passBy = new JSONObject(getFromVnetFlow(swInfos, vnetPort.number));
								passBy.remove("ingress-port"); 
								passBy.put("name", VPMPathManager.PASSBY_FLOW+swInfos.sw.dpid.replace(":", ""));

								controller.deleteFlow(swInfos.sw.dpid, VPMPathManager.TO_VNET_FLOW + swInfos.sw.dpid.replace(":", ""));
								controller.deleteFlow(swInfos.sw.dpid, VPMPathManager.FROM_VNET_FLOW+vnetPort.number+ "_" + swInfos.sw.dpid.replace(":", ""));
								controller.addStaticFlow(passBy);


								swInfos.flows.remove(VPMPathManager.FROM_VNET_FLOW+vnetPort.number+ "_" + swInfos.sw.dpid.replace(":", ""));
								swInfos.flows.remove(VPMPathManager.TO_VNET_FLOW + swInfos.sw.dpid.replace(":", ""));
								swInfos.flows.put(passBy.getString("name"), passBy);
							}
						}
					}//END IF != ROOT

				}else{
					System.out.println("There is no path for this switch so we will not do anything");
				}

			}
			break;
		}

	}

}
