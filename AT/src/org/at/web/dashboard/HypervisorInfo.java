package org.at.web.dashboard;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.connections.HypervisorConnectionManager;
import org.at.db.Hypervisor;
import org.at.libvirt.NetHypervisorConnection;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class HypervisorInfo
 */
@WebServlet("/HypervisorInfo")
public class HypervisorInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private ExecutorService executor;
	
	@Override
	public void init() throws ServletException {
		super.init();
		executor = Executors.newCachedThreadPool();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		executor.shutdown();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		JSONArray hypervisorsJsonList = new JSONArray();  //the list we will send
		HypervisorConnectionManager manager = (HypervisorConnectionManager)getServletContext()
				.getAttribute(HypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER);
		
		List<NetHypervisorConnection> hypervisors = manager.getActiveConnections();
		
		GetHypervisorStatsThread[] threads = new GetHypervisorStatsThread[hypervisors.size()];
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for(int i=0;i<threads.length;i++){
			threads[i] = new GetHypervisorStatsThread(hypervisors.get(i));
			futures.add(executor.submit(threads[i]));
		}
		
		JSONArray offlines = new JSONArray();
		
		for(Hypervisor h : manager.getOfflineHypervisors()){
			JSONObject j = new JSONObject()
			.put("id", h.getId())
			.put("ip",h.toString())
			.put("status", Hypervisor.STATUS_OFFLINE);
			offlines.put(j);
		}
		
		//these should be JUST online hypervisors
		int i = 0;
		for(Future<?> f : futures){
			try {
				JSONObject j = (JSONObject) f.get();
				if(j.getString("status").equals(Hypervisor.STATUS_OFFLINE)){//if one of these have status offline it can just mean
																			//that it is not online anymore, let's remove it
					manager.setInactive(threads[i].getHypervisorConnection());
					offlines.put(j);
				}else
					hypervisorsJsonList.put(j);
				
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			
			i++;
		}
		
		hypervisorsJsonList.concat(offlines);
		
		out.println(hypervisorsJsonList);
		out.close();
	}

}
