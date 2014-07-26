package it.unina.cini.platino.web.migration;


import it.unina.cini.platino.connections.VPMHypervisorConnectionManager;
import it.unina.cini.platino.libvirt.MigrationThread;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


/**
 * A servlet performing a live migration between two hypervisors.
 * To be efficient a new MigrationThread is instantiated and a lmID (live migration ID) is
 * provided as response to the client to keep track of this specific migration.
 * 
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
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
		
		VPMHypervisorConnectionManager manager =(VPMHypervisorConnectionManager)getServletContext().getAttribute(VPMHypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER);
		
		
		MigrationThread mt = new MigrationThread(manager.getActiveConnection(srcip).getHypervisor(), 
				manager.getActiveConnection(dstip).getHypervisor(), vname, getServletContext());
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
