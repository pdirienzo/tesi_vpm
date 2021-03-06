package it.unina.cini.platino.web.network.events;

import it.unina.cini.platino.floodlight.FloodlightController;
import it.unina.cini.platino.floodlight.FloodlightPort;
import it.unina.cini.platino.network.types.LinkConnection;
import it.unina.cini.platino.network.types.OvsSwitch;
import it.unina.cini.platino.network.types.VPMGraph;
import it.unina.cini.platino.network.types.VPMGraphHolder;
import it.unina.cini.platino.web.network.path.backend.DefaultVPMPathManager;
import it.unina.cini.platino.web.network.path.backend.VPMSwitchInfo;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * A servlet acting as a listener for the Floodlight controller module.
 * It receives events and performs maintenance operations.
 * 
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 */
@WebServlet("/VPMEventListener")
public class VPMEventListener extends HttpServlet {
	private static final long serialVersionUID = 1L;

	//event types
	private enum Event { TOPOLOGY, VM };
	private enum VM_OP { ADD, REMOVE };
	private enum TOPOLOGY_OP { ADD, REMOVE };

	private DefaultVPMPathManager pathManager;

	@Override
	public void init() throws ServletException {
		super.init();
		
		pathManager = (DefaultVPMPathManager)getServletContext().getAttribute(DefaultVPMPathManager.VPM_PATH_MANAGER);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private JSONObject getToVnetFlow(VPMSwitchInfo infos){
		JSONObject flow = null;
		Iterator<String> it = infos.flows.keySet().iterator();

		while((flow == null) && it.hasNext()){
			String key = it.next();
			if(key.startsWith(DefaultVPMPathManager.TO_VNET_FLOW))
				flow = infos.flows.get(key);
		}

		return flow;
	}

	private JSONObject getFromVnetFlow(VPMSwitchInfo infos, int vnetN){
		JSONObject flow = null;
		Iterator<String> it = infos.flows.keySet().iterator();

		while((flow == null) && it.hasNext()){
			String key = it.next();
			if(key.equals(DefaultVPMPathManager.FROM_VNET_FLOW + vnetN + "_" + infos.sw.dpid.replace(":", "")))
				flow = infos.flows.get(key);
		}

		return flow;
	}


	private JSONObject getPassbyFlow(VPMSwitchInfo infos){
		JSONObject flow = null;
		Iterator<String> it = infos.flows.keySet().iterator();

		while((flow == null) && it.hasNext()){
			String key = it.next();
			if(key.equals(DefaultVPMPathManager.PASSBY_FLOW+infos.sw.dpid.replace(":", "")))
				flow = infos.flows.get(key);

		}

		return flow;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject event = new JSONObject(request.getParameter("data"));
		System.out.println("Received  "+event.toString());
		switch(Event.valueOf(event.getString("type"))){

		/*TODO fault handling
		 * An automatic system should verify if there is any redundancy link
		 * to use to restore the network. Any existing path should be kept.
		 * If it is not possible to restore the topology anymore the existing
		 * graph should be invalidated and a notify should be sent to the client
		 * 
		 */
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
						FloodlightPort vnetPort = new FloodlightPort(event.getString("vnet"));
						FloodlightController controller = FloodlightController.getDbController();
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
								toVnet.put("name", DefaultVPMPathManager.TO_VNET_FLOW+swInfos.sw.dpid.replace(":", ""));
								toVnet.put("actions", swInfos.getCurrentVnetActionString());

								//from vnet
								fromVnet = new JSONObject(passBy)
								.put("name", DefaultVPMPathManager.FROM_VNET_FLOW+vnetPort.number+ "_" + swInfos.sw.dpid.replace(":", ""))
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
								controller.deleteFlow(swInfos.sw.dpid, DefaultVPMPathManager.FROM_VNET_FLOW+vnetPort.number+ "_" + swInfos.sw.dpid.replace(":", ""));

								swInfos.flows.put(flow.getString("name"), flow);
								swInfos.flows.remove(DefaultVPMPathManager.FROM_VNET_FLOW+vnetPort.number+ "_" + swInfos.sw.dpid.replace(":", ""));

							}else{ //there was just one vnet so now we need to remove 2 rules and install a passby one

								//adding passByrule
								JSONObject passBy = new JSONObject(getFromVnetFlow(swInfos, vnetPort.number));
								passBy.remove("ingress-port"); 
								passBy.put("name", DefaultVPMPathManager.PASSBY_FLOW+swInfos.sw.dpid.replace(":", ""));

								controller.deleteFlow(swInfos.sw.dpid, DefaultVPMPathManager.TO_VNET_FLOW + swInfos.sw.dpid.replace(":", ""));
								controller.deleteFlow(swInfos.sw.dpid, DefaultVPMPathManager.FROM_VNET_FLOW+vnetPort.number+ "_" + swInfos.sw.dpid.replace(":", ""));
								controller.addStaticFlow(passBy);


								swInfos.flows.remove(DefaultVPMPathManager.FROM_VNET_FLOW+vnetPort.number+ "_" + swInfos.sw.dpid.replace(":", ""));
								swInfos.flows.remove(DefaultVPMPathManager.TO_VNET_FLOW + swInfos.sw.dpid.replace(":", ""));
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
