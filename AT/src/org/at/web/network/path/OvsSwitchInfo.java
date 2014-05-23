package org.at.web.network.path;

import java.util.ArrayList;
import java.util.List;

import org.at.network.types.OvsSwitch;

public class OvsSwitchInfo {
	private List<Integer> outputPorts;
	public OvsSwitch sw;
	private int counter;
	
	public OvsSwitchInfo(OvsSwitch sw){
		this.sw = sw;
		outputPorts = new ArrayList<Integer>();
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
	
	public synchronized void addOutputPort(Integer p){
		if(!outputPorts.contains(p))
			outputPorts.add(p);
	}
	
	public synchronized void removeOutputPort(Integer p){
		outputPorts.remove(p);
	}
	
	public String getCurrentOutputActionString(){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<outputPorts.size()-1;i++)
			sb.append("output="+outputPorts.get(i)+",");
		
		sb.append("output="+outputPorts.get(outputPorts.size()-1));
		
		return sb.toString();
	}
	
	
}
