package org.at.web.settings;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Controller;
import org.at.db.Database;
import org.json.JSONObject;

/**
 * Servlet implementation class GetControllerSettings
 */
@WebServlet("/GetControllerSettings")
public class GetControllerSettings extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		
		Database d = (Database)getServletContext().getAttribute("database");
		Controller c = d.getController();
		
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
