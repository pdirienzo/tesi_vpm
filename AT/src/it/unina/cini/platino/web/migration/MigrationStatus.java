package it.unina.cini.platino.web.migration;

import it.unina.cini.platino.libvirt.MigrationThread;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.libvirt.DomainJobInfo;

/**
 * A servlet providing information about a specific migration status.
 * lmID ( live migration ID) to identify said migration has to be provided 
 * as parameter by the client.
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
@WebServlet("/MigrationStatus")
public class MigrationStatus extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		PrintWriter out = new PrintWriter(response.getOutputStream());
		response.setContentType("application/json");

		MigrationThread mt = (MigrationThread)getServletContext().getAttribute(
				request.getParameter("lmid"));

		JSONObject state = new JSONObject();
		if(mt != null){
			DomainJobInfo dj = mt.getJobStats();
			if(dj != null){
				state.put("state", String.valueOf(MigrationThread.MIGRATION_PROGRESS))
				.put("id", request.getParameter("lmid"))
				.put("processed", String.valueOf(dj.getMemProcessed()))
				.put("remaining", String.valueOf(dj.getMemRemaining()))
				.put("total", String.valueOf(dj.getMemTotal()));
				if(dj.getMemTotal() != 0){
					float diff = dj.getMemTotal()- dj.getMemRemaining();
					float percent = 100* (diff/(float)dj.getMemTotal());
					state.put("percent", percent);
				}else
					state.put("percent", 0);
				
			}else{
				state.put("state", String.valueOf(mt.getMigrationStatus()))
				.put("id", request.getParameter("lmid"))
				.put("error", mt.getErrorMessage());
			}

		}else{
			state.put("state", "Not Found");
		}
		out.println(state.toString());
		out.close();
	}

}
