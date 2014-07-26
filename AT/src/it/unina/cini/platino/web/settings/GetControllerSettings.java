package it.unina.cini.platino.web.settings;

import it.unina.cini.platino.db.Controller;
import it.unina.cini.platino.db.Database;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * A servlet allowing the user to get controller related data from the Database
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
@WebServlet("/GetControllerSettings")
public class GetControllerSettings extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		
		Database d = new Database();
		d.connect();
		Controller c = d.getController();
		d.close();
		
		JSONObject json;
		if(c !=null) //a controller exists
		{
			json = new JSONObject()
				.put("hostname",c.getHostAddress())
				.put("port", c.getPort())
				.put("ui_url", "http://"+c.getHostAddress()+":8080/ui/index.html");
		} else {
			json = new JSONObject()
				.put("hostname", "Non configurato")
				.put("port", "Non configurato")
				.put("ui_url", "Non configurato");
		}
		
		out.println(json);
		out.close();
		
	}

}
