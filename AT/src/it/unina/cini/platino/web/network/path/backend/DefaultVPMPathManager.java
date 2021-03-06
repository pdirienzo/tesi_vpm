package it.unina.cini.platino.web.network.path.backend;

import it.unina.cini.platino.floodlight.FloodlightController;
import it.unina.cini.platino.floodlight.FloodlightPort;
import it.unina.cini.platino.network.types.LinkConnection;
import it.unina.cini.platino.network.types.OvsSwitch;
import it.unina.cini.platino.network.types.VPMGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.DijkstraShortestPath;
import org.json.JSONObject;

/**
 * A default PathManager implementation for lazy bones. It is made of two parts:
 * -basic part
 * --automatically implemented. If no VMs are present on a switch along specified path 
 * --it will install a flow which just makes application related packets flow to next switch,
 * --while if one or more application related VM (recognized by its interface name) is on that 
 * --switch it will install two flows: one to direct packets to those VMs and one from VMs
 * --to next switch.
 * 
 * -extend part
 * --flow match fields decided by the user through a json file present in the "vpm_path_plugin"
 * --folder. By altering match files the user can identify application related flows to which
 * --apply the basic part.
 * 
 * Note that this implementation of PathManager just works for the Floodlight controller.
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
public class DefaultVPMPathManager implements VPMPathManager{
	
	public static final String EXTENDED_FILEPATH = "vpm_path_plugin/Match.json";
	
	private VPMSwitchInfoHolder switchInfos;
	private VPMPathInfoHolder pathInfos;
	
	public DefaultVPMPathManager(){
		switchInfos = new VPMSwitchInfoHolder();
		pathInfos = new VPMPathInfoHolder();
	}
	
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
	
	
	public VPMSwitchInfo getSwitchInfos(String dpid){
		return switchInfos.get(dpid);
	}
	
	public VPMPathInfo getPath(String rootDpid, String leafDpid){
		return pathInfos.get(computePathName(rootDpid, leafDpid));
	}
	

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
			OvsSwitch src, OvsSwitch dest, String external, String portPrefix) throws IOException{
		
		DijkstraShortestPath<OvsSwitch, LinkConnection> shortest = new DijkstraShortestPath<OvsSwitch, LinkConnection>(graph, src, dest);
		addFlows(shortest.getPath(), external,portPrefix);
		
		String pathName = computePathName(src.dpid, dest.dpid); 
		pathInfos.put(pathName, new VPMPathInfo(shortest.getPath(), external, portPrefix));
	
		return pathInfos.get(pathName);
	}
	
	
	public void uninstallPath(String rootDpid, String leafDpid) throws IOException{
		VPMPathInfo info = getPath(rootDpid, leafDpid);
		if(info != null){
			deleteFlows(info.path);
			pathInfos.remove(computePathName(rootDpid, leafDpid));
		}
	}
	
	private static JSONObject constructMatchingFlow() throws IOException{
		JSONObject basicFlow = new JSONObject()
		.put("cookie", (new Random()).nextInt(Integer.MAX_VALUE))
		.put("priority", "100")
		.put("active", "true");
		
		byte[] jsonData = Files.readAllBytes(Paths.get(EXTENDED_FILEPATH));
		
		JSONObject extended = new JSONObject(new String(jsonData));
		return basicFlow.add(extended);
	}
	
	private void addFlows(GraphPath<OvsSwitch,LinkConnection> jpath, String externalBcast, String portPrefix) throws IOException{

		FloodlightController controller = FloodlightController.getDbController();
		List<OvsSwitch> nodes = Graphs.getPathVertexList(jpath);

		for(int i=0;i<nodes.size();i++){
			//this is a template with informations common to each flow we are going to install
			//according to the specific situation we will vary the:
			//-name
			//-dpid 
			//-ingress port (not always)
			//-output rule
			JSONObject basicFlow = constructMatchingFlow();/*new JSONObject()
			.put("cookie", (new Random()).nextInt())
			.put("priority", "100")
			.put("dst-ip", "10.0.0.255")
			.put("ether-type", "0x0800")
			.put("active", "true");*/
			
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
					for(FloodlightPort vnet : controller.getVMPorts(infos.sw,portPrefix))
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
		
		FloodlightController controller = FloodlightController.getDbController();
		List<OvsSwitch> nodes = Graphs.getPathVertexList(jpath);

		for(int i=0;i<nodes.size();i++){
			VPMSwitchInfo infos = switchInfos.get(nodes.get(i).dpid);
			
			try{
				
				JSONObject basicFlow = constructMatchingFlow();
				
				basicFlow.put("switch", infos.sw.dpid);
				
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
