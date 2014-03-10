package org.at.web.migration;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Database;
import org.at.db.Hypervisor;
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
		String srcip = request.getParameter("srcip").split("@")[1];
		String dstip = (request.getParameter("dstip")).split("@")[1];
		
		Database d = (Database)getServletContext().getAttribute("database");
		Hypervisor srcHyp = d.getHypervisorByIp(srcip);
		Hypervisor dstHyp = d.getHypervisorByIp(dstip);
		
		MigrationThread mt = new MigrationThread(srcHyp, dstHyp, vname);
		mt.start();
		
		//String id = "lm-"+vname+"-"+srcip.split("\\.")[3]+"-"+dstip.split("\\.")[3]; 
		String id = UUID.randomUUID().toString();
		
		//saving an handle to the thread to keep trace of the status
		getServletContext().setAttribute(id, mt);
		JSONObject json = new JSONObject()
		.put("state", "ok")
		.put("id", id);
		out.println(json.toString());
		out.close();
	}

}
