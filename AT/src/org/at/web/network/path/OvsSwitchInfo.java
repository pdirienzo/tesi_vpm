package org.at.web.network.path;

import java.util.HashMap;
import java.util.Iterator;

import org.at.network.types.OvsSwitch;

public class OvsSwitchInfo {
	private HashMap<Integer, Integer> outputPorts;
	public OvsSwitch sw;
	private int counter;

	public OvsSwitchInfo(OvsSwitch sw){
		this.sw = sw;
		outputPorts = new HashMap<Integer, Integer>();
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
		Iterator<Integer> keys = outputPorts.keySet().iterator();
		int i = 0;
		int size = outputPorts.keySet().size();
		
		while( i < size-1 ){
			sb.append("output="+keys.next()+",");
			i++;
		}
		sb.append("output="+keys.next());

		return sb.toString();
	}

}
