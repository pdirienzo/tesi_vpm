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

import org.at.db.Database;
import org.at.db.Hypervisor;
import org.at.libvirt.GetHypervisorStatsThread;
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
		
		Database database = (Database)getServletContext().getAttribute("database");
		List<Hypervisor> hypervisors = database.getAllHypervisors();
		
		GetHypervisorStatsThread[] threads = new GetHypervisorStatsThread[hypervisors.size()];
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for(int i=0;i<threads.length;i++){
			threads[i] = new GetHypervisorStatsThread(hypervisors.get(i));
			futures.add(executor.submit(threads[i]));
		}
		for(Future<?> f : futures){
			try {
				JSONObject j = (JSONObject) f.get();
				hypervisorsJsonList.put(j);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		out.println(hypervisorsJsonList);
		out.close();
	}

}
