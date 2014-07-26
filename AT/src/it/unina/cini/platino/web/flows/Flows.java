package it.unina.cini.platino.web.flows;

import it.unina.cini.platino.floodlight.FloodlightController;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A servlet providing a list of static flows installed on a specific switch
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
@WebServlet("/Flows")
public class Flows extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Flows() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject jResp = new JSONObject();
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		
		try{
			FloodlightController controller = FloodlightController.getDbController();
			JSONArray flows = controller.getStaticFlows(request.getParameter("dpid"));
			jResp.put("status", "ok");
			jResp.put("data", flows);
		}catch(IOException ex){
			jResp.put("status", "error");
			jResp.put("details", ex.getMessage());
		}
		out.println(jResp);
		out.close();
	}

}
