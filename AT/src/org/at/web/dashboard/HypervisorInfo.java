package org.at.web.dashboard;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Database;
import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

/**
 * Servlet implementation class HypervisorInfo
 */
@WebServlet("/HypervisorInfo")
public class HypervisorInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		JSONArray hypervisorsJsonList = new JSONArray();  //the list we will send
		
		Database database = (Database)getServletContext().getAttribute("database");
		List<Hypervisor> hypervisors = database.getAllHypervisors();
		
		for(Hypervisor hyp : hypervisors){
			HypervisorConnection c;
			JSONObject hypervisor = new JSONObject()
			.put("ip",hyp.getName()+"@"+hyp.getHostAddress());
			
			try {
				c = new HypervisorConnection(hyp);
				hypervisor.put("status", "online");
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
				hypervisor.put("machines", machines);
				
			} catch (LibvirtException e) {
				System.out.println(e.getMessage());
				hypervisor.put("status", "offline");
			}finally{
				hypervisorsJsonList.put(hypervisor);
			}
		}
		
		out.println(hypervisorsJsonList);
		out.close();
	}

}
