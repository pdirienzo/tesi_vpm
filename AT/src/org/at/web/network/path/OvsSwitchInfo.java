package org.at.web.network.path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.at.network.types.OvsSwitch;

import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;

public class OvsSwitchInfo {
	private HashMap<Integer, Integer> outputPorts;
	private List<Integer> vnetPorts;
	
	public OvsSwitch sw;
	private int counter;

	public OvsSwitchInfo(OvsSwitch sw){
		this.sw = sw;
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
		/*Iterator<Integer> keys = outputPorts.keySet().iterator();
		int i = 0;
		int size = outputPorts.keySet().size();
		
		while( i < size-1 ){
			sb.append("output="+keys.next()+",");
			i++;
		}
		sb.append("output="+keys.next());*/
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
			
			sb.deleteCharAt(sb.length()-1);
		}
		
		return sb.toString();
	}
	
	public static void main(String[] args){
		OvsSwitchInfo i = new OvsSwitchInfo(new OvsSwitch("m","n"));
		i.addOutputPort(5);
		i.addOutputPort(5);
		i.addOutputPort(3);
		i.addVnetPort(1);
		System.out.println(i.getCurrentOutputActionString()+" "+i.getCurrentVnetActionString());
	}

}
