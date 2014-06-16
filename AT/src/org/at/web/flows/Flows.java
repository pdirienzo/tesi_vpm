package org.at.web.flows;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.floodlight.FloodlightController;
import org.json.JSONObject;

/**
 * Servlet implementation class Flows
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
			JSONObject flows = controller.getStaticFlows(request.getParameter("dpid"));
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
