package org.at.libvirt;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.at.db.Hypervisor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class GetHypervisorStatsThread implements Callable<JSONObject>{
	private Hypervisor h;
	
	public GetHypervisorStatsThread(Hypervisor h){
		this.h = h;
	}
	
	@Override
	public JSONObject call() {
		HypervisorConnection c;
		JSONObject hypervisorJ = new JSONObject()
		.put("ip",h.getName()+"@"+h.getHostAddress());
		
		try {
			c = HypervisorConnection.getConnectionWithTimeout(h,
					true, 
					HypervisorConnection.DEFAULT_TIMEOUT);
			
			hypervisorJ.put("status", "online");
			JSONArray machines = new JSONArray();
			for(Domain d : c.getAllDomains()){
				JSONObject vm = new JSONObject();
				vm.put("name", d.getName());
				int active = d.isActive();

				if(active == 1)
					vm.put("status", "running");
				else if(active == 0)
					vm.put("status", "stopped");
				else
					vm.put("status", "unknown");
				
				machines.put(vm);
			}
		
			c.close();
			hypervisorJ.put("machines", machines);
			
		} catch (LibvirtException | IOException e) {
			//if hypervisor is not online or there is any generic error
			//(usually if there is an error it is just cuz the hypervisor
			//is offline)
			e.printStackTrace();
			System.out.println(e.getMessage());
			hypervisorJ.put("status", "offline");
		}
		
		return hypervisorJ;
	}
}
