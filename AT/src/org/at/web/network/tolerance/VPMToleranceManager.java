package org.at.web.network.tolerance;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.at.network.types.VPMGraph;
import org.at.network.types.VPMGraphHolder;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class VPMToleranceManager
 */
@WebServlet("/VPMToleranceManager")
public class VPMToleranceManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VPMToleranceManager() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		VPMGraph<OvsSwitch, LinkConnection> graph = ((VPMGraphHolder)getServletContext().getAttribute(
				VPMGraphHolder.VPM_GRAPH_HOLDER)).getGraph();
		
		JSONArray result = (new JSONObject(request.getParameter("data"))).getJSONArray("result");
		for(JSONObject o : result)
			System.out.println(o);
	}

}
