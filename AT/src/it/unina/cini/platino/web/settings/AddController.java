package it.unina.cini.platino.web.settings;

import it.unina.cini.platino.connections.VPMContextServerListener;
import it.unina.cini.platino.db.Controller;
import it.unina.cini.platino.db.Database;
import it.unina.cini.platino.floodlight.FloodlightController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * A servlet allowing the user to insert controller related data into the Database
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
@WebServlet("/AddController")
public class AddController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Controller c = new Controller(request.getParameter("ip"),Long.valueOf(request.getParameter("port")));
		JSONObject message=new JSONObject();
		PrintWriter out = response.getWriter();
		
		try{
			Database d = new Database();
			d.connect();
			d.insertController(c);
			d.close();
			
			FloodlightController f = new FloodlightController(c);
			try{
				f.registerListener(VPMContextServerListener.FLOODLIGHT_CALLBACK_URI,
						((Properties)getServletContext().getAttribute("properties"))
						.getProperty("network_interface_prefix"));
			}catch(IOException ex2){
				ex2.printStackTrace();
			}
			message.put("status", "controller settings edited");
		}catch(IOException ex){
			message.put("status", "failed to add controller: "+ex.getMessage());
			ex.printStackTrace();
		}finally{
			response.setContentType("application/json");
			out.println(message);
			out.close();
		}
	}
}
