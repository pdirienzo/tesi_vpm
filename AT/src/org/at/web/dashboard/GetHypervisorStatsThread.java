package org.at.web.dashboard;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.at.db.Hypervisor;
import org.at.libvirt.NetHypervisorConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class GetHypervisorStatsThread implements Callable<JSONObject>{
	private NetHypervisorConnection c;
	
	public NetHypervisorConnection getHypervisorConnection(){
		return c;
	}
	
	public GetHypervisorStatsThread(NetHypervisorConnection c){
		this.c = c;
	}
	
	@Override
	public JSONObject call() {
		Hypervisor h = c.getHypervisor();
		JSONObject hypervisorJ = new JSONObject()
		.put("id", h.getId())
		.put("hostname",h.toString());
		
		try{
			hypervisorJ.put("ip", c.getIpAddress());
		}catch(IOException | LibvirtException ex){
			hypervisorJ.put("ip", "undefined");
		}
		
		try {
			hypervisorJ.put("status", Hypervisor.STATUS_ONLINE);
			hypervisorJ.put("cpuUsage", String.format(Locale.US,"%.2f", 
					c.getCPUOverallUsage(500)));
			JSONArray machines = new JSONArray();
			for(Domain d : c.getAllDomains()){
				JSONObject vm = new JSONObject();
				vm.put("name", d.getName());
				int active = d.isActive();
				
				if(active == 1) {
					vm.put("status", "running");
					vm.put("cpuUsage", String.format(Locale.US,"%.2f", 
							d.getCPUOverallUsage(500)));
				}
				else if(active == 0)
					vm.put("status", "stopped");
				else
					vm.put("status", "unknown");
				
				machines.put(vm);
			}
			
			hypervisorJ.put("machines", machines);
			
		} catch (LibvirtException e) {
			//if hypervisor is not online or there is any generic error
			//(usually if there is an error it is just cuz the hypervisor
			//is offline)
			e.printStackTrace();
			System.out.println(e.getMessage());
			hypervisorJ.put("status", Hypervisor.STATUS_OFFLINE);
		}
		
		return hypervisorJ;
	}
}
