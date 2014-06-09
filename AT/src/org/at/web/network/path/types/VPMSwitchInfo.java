package org.at.web.network.path.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.at.network.types.OvsSwitch;
import org.json.JSONObject;

public class VPMSwitchInfo {
	private HashMap<Integer, Integer> outputPorts;
	private List<Integer> vnetPorts;
	
	public OvsSwitch sw;
	private int counter;
	public HashMap<String, JSONObject> flows; //TODO populate this one with <name of flow, flow> pair

	public VPMSwitchInfo(OvsSwitch sw){
		this.sw = sw;
		flows = new HashMap<String, JSONObject>();
		outputPorts = new HashMap<Integer, Integer>();
		vnetPorts = new ArrayList<Integer>();
		counter = 0;
	}

	public synchronized int getCounter(){
		return counter;
	}

	public synchronized void incrementCounter(){
		counter++;
	}

	public synchronized void decrementCounter(){
		counter--;
	}
	
	public synchronized void addVnetPort(Integer p){
		if(!vnetPorts.contains(p))
			vnetPorts.add(p);
	}
	
	public synchronized void removeVnetPort(Integer p){
		vnetPorts.remove(p);
	}
	
	public synchronized int getVnetNumber(){
		return vnetPorts.size();
	}

	public synchronized void addOutputPort(Integer p){
		Integer count = outputPorts.get(p);
		if(count == null){
			outputPorts.put(p, new Integer(0));
			count = outputPorts.get(p);
		}
		
		outputPorts.put(p,count+1);
	}

	public synchronized void removeOutputPort(Integer p){
		Integer count = outputPorts.get(p);
		if(count != null){
			count--;
			if(count == 0)
				outputPorts.remove(p);
			else
				outputPorts.put(p, count);
		}
	}

	public synchronized String getCurrentOutputActionString(){
		StringBuilder sb = new StringBuilder();
		
		if(outputPorts.size() > 0){
			for(Integer port : outputPorts.keySet() )
				sb.append("output="+port+",");
			
			sb.deleteCharAt(sb.length()-1); //removing last comma
		}

		return sb.toString();
	}
	
	public synchronized String getCurrentVnetActionString(){
		StringBuilder sb = new StringBuilder();
		
		if(vnetPorts.size() > 0){
			for(Integer port : vnetPorts)
				sb.append("output="+port+",");
			
			sb.deleteCharAt(sb.length()-1); //removing last comma
		}
		
		return sb.toString();
	}
	
	public List<Integer> getVMPorts(){
		List<Integer> ports = new ArrayList<>();
		for(Integer i : vnetPorts)
			ports.add(i);
		
		return ports;
	}

}
