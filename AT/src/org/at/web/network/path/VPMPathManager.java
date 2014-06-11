package org.at.web.network.path;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.at.db.Controller;
import org.at.db.Database;
import org.at.floodlight.FloodlightController;
import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.at.network.types.Port;
import org.at.network.types.VPMGraph;
import org.at.web.network.path.types.VPMPathInfo;
import org.at.web.network.path.types.VPMPathInfoHolder;
import org.at.web.network.path.types.VPMSwitchInfo;
import org.at.web.network.path.types.VPMSwitchInfoHolder;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.DijkstraShortestPath;
import org.json.JSONObject;

public class VPMPathManager {
	public static final String VPM_PATH_MANAGER = "PathManager";
	
	public static final String PASSBY_FLOW = "RTP_PASSTHROUGH";
	public static final String TO_VNET_FLOW = "RTP_TOVNETS";
	public static final String FROM_VNET_FLOW = "RTP_FROMVNET";
	
	private VPMSwitchInfoHolder switchInfos;
	private VPMPathInfoHolder pathInfos;
	
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
	 * Returns the infos associated to the switch which has the dpid passed as parameter.
	 * 
	 * @param dpid
	 * @return the switchinfo structure associated to that switch or null if the latter is not present
	 */
	public VPMSwitchInfo getSwitchInfos(String dpid){
		return switchInfos.get(dpid);
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
	
	public VPMPathManager(){
		switchInfos = new VPMSwitchInfoHolder();
		pathInfos = new VPMPathInfoHolder();
	}
	
	/**
	 * Returns a PathInfo instance if path exists, null otherwise
	 * @param rootDpid
	 * @param leafDpid
	 * @return
	 */
	public VPMPathInfo getPath(String rootDpid, String leafDpid){
		return pathInfos.get(computePathName(rootDpid, leafDpid));
	}
	
	/**
	 * Returns a PathInfo instance for a path ending on the specified leaf, null otherwise
	 * @param leafDpid
	 * @return
	 */
	public VPMPathInfo getPath(String leafDpid){
		Iterator<String> keys = pathInfos.keySet().iterator();
		String[] subs = leafDpid.split(":");
		String endName = subs[subs.length-2] + subs[subs.length-1];  
		
		VPMPathInfo pathInfo = null;
		
		while( (pathInfo == null) && keys.hasNext()){
			String key = keys.next();
			if(key.endsWith(endName))
				pathInfo = pathInfos.get(key);
		}
		
		return pathInfo;
	}
	
	public VPMPathInfo installShortestPath(VPMGraph<OvsSwitch, LinkConnection> graph, 
			OvsSwitch src, OvsSwitch dest, String external) throws IOException{
		
		DijkstraShortestPath<OvsSwitch, LinkConnection> shortest = new DijkstraShortestPath<OvsSwitch, LinkConnection>(graph, src, dest);
		addFlows(shortest.getPath(), external);
		
		String pathName = computePathName(src.dpid, dest.dpid); 
		pathInfos.put(pathName, new VPMPathInfo(shortest.getPath(), external));
	
		return pathInfos.get(pathName);
	}
	
	
	
	public void uninstallPath(String rootDpid, String leafDpid) throws IOException{
		VPMPathInfo info = getPath(rootDpid, leafDpid);
		if(info != null){
			deleteFlows(info.path);
			pathInfos.remove(computePathName(rootDpid, leafDpid));
		}
	}
	
	//********************************************** auxiliary functions ***************************************************
	/*public void addVnetToFlow(VPMSwitchInfo infos, JSONObject flow, FloodlightController controller) throws IOException{
		if(controller == null)
			controller = getController();
		
		
	}*/
	
	//****************************************** flow logic ******************************************************************************
	private void addFlows(GraphPath<OvsSwitch,LinkConnection> jpath, String externalBcast) throws IOException{

		FloodlightController controller = getController();
		List<OvsSwitch> nodes = Graphs.getPathVertexList(jpath);


		for(int i=0;i<nodes.size();i++){
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
			
			try{
				VPMSwitchInfo infos = switchInfos.get(nodes.get(i).dpid);

				if(infos == null){ //first time this switch is crossed by a path
					infos = new VPMSwitchInfo(nodes.get(i));
					switchInfos.put(infos.sw.dpid, infos);
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
						infos.flows.put(toVMFlow.getString("name"), toVMFlow);
					}
					switchInfos.put(nodes.get(i).dpid, infos);
				}

				String action = null; //the action string
				if(infos.sw.type != OvsSwitch.Type.LEAF){ //if it's not a final destination 
					OvsSwitch ovsDst = nodes.get(i+1); //getting next switch 
					String path = computePathName(infos.sw.dpid, ovsDst.dpid);
					//this is the gre tunnel connecting this switch to the next one
					infos.addOutputPort(controller.getPortNumber(infos.sw, "gre"+path));
					action = infos.getCurrentOutputActionString();
				}else{
					if(externalBcast == null)
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
						infos.flows.put(fromVMFlow.getString("name"), fromVMFlow);
					}

				}else{ //if no vms we just make the traffic flow to next openvswitch
					basicFlow.put("name", PASSBY_FLOW+infos.sw.dpid.replace(":", ""));
					basicFlow.put("actions", action);
					controller.addStaticFlow(basicFlow);
					infos.flows.put(basicFlow.getString("name"), basicFlow);
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
		
		FloodlightController controller = getController();
		List<OvsSwitch> nodes = Graphs.getPathVertexList(jpath);

		for(int i=0;i<nodes.size();i++){
			VPMSwitchInfo infos = switchInfos.get(nodes.get(i).dpid);
			
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
								infos.flows.put(fromVMFlow.getString("name"), fromVMFlow);
							}
						}else{
							basicFlow.put("name", PASSBY_FLOW+infos.sw.dpid.replace(":", ""));
							
							controller.addStaticFlow(basicFlow);
							infos.flows.put(basicFlow.getString("name"), basicFlow);
						}
					
				}else{ //just one path: we delete the rules
					if(infos.sw.type != OvsSwitch.Type.ROOT && infos.getVnetNumber() > 0){
						
						controller.deleteFlow(infos.sw.dpid, TO_VNET_FLOW+infos.sw.dpid.replace(":", ""));
						infos.flows.remove(TO_VNET_FLOW+infos.sw.dpid.replace(":", ""));
						
						for(Integer vnetN : infos.getVMPorts()){
							controller.deleteFlow(infos.sw.dpid, FROM_VNET_FLOW + vnetN + "_" + infos.sw.dpid.replace(":", ""));
							infos.flows.remove(FROM_VNET_FLOW + vnetN + "_" + infos.sw.dpid.replace(":", ""));
							infos.removeVnetPort(vnetN);
						}
						
					}else{
						controller.deleteFlow(infos.sw.dpid, PASSBY_FLOW+infos.sw.dpid.replace(":", ""));
						infos.flows.remove(PASSBY_FLOW+infos.sw.dpid.replace(":", ""));
					}
					switchInfos.remove(infos); //we remove this switch from switch infos as it isn't part of a path anymore
				}
				
				infos.decrementCounter(); //a path less for this switch
				
			}catch(IOException ex){
				//this way if any exception occurs we make sure to decrement the counter so to have
				//a consistent data structure
				infos.incrementCounter();
				throw ex;
			}
		}
	}
}
