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

/**
 * A servlet providing information about time taken to perform a specific migration.
 * Said migration is identified by a lmID ( live migration ID) which has to be provided 
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
@WebServlet("/GetElapsedTime")
public class GetElapsedTime extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		PrintWriter out = new PrintWriter(response.getOutputStream());
		response.setContentType("application/json");
		
		String lmid = request.getParameter("lmid");
		MigrationThread mt = (MigrationThread)getServletContext().getAttribute(lmid);

		JSONObject json = new JSONObject();
		json.put("id", lmid);
		if(mt != null){
			long time = mt.getElapsedTime();
			json.put("elapsed",String.valueOf(time));
		}else
			json.put("status", "Not found");

		//TODO removes the thread handle
		getServletContext().removeAttribute(lmid);
		out.println(json.toString());
		out.close();
	}

}
