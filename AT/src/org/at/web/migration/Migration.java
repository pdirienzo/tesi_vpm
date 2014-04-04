package org.at.web.migration;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.connections.HypervisorConnectionManager;
import org.at.libvirt.MigrationThread;
import org.json.JSONObject;


/**
 * Servlet implementation class Migration
 */
@WebServlet("/Migration")
public class Migration extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		PrintWriter out = new PrintWriter(response.getOutputStream());
		response.setContentType("application/json");
		
		String vname  = request.getParameter("vmname");
		String srcip = request.getParameter("srcip");
		String dstip = (request.getParameter("dstip"));
		
		HypervisorConnectionManager manager =(HypervisorConnectionManager)getServletContext().getAttribute(HypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER);
		
		MigrationThread mt = new MigrationThread(manager.getActiveConnection(srcip).getHypervisor(), manager.getActiveConnection(dstip).getHypervisor(), vname);
		mt.start();
		
		String id = srcip+"_"+dstip+"_"+vname;
		
		//saving an handle to the thread to keep trace of the status
		getServletContext().setAttribute(id, mt);
		JSONObject json = new JSONObject()
		.put("state", "ok")
		.put("id", id);
		out.println(json.toString());
		out.close();
	}

}
