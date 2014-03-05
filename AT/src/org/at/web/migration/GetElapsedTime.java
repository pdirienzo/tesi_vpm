package org.at.web.migration;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.at.libvirt.MigrationThread;
import org.json.JSONObject;

/**
 * Servlet implementation class GetElapsedTime
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
		long time = mt.getElapsedTime();
		JSONObject json = new JSONObject().put("elapsed",String.valueOf(time));
		
		//TODO removes the thread handle
		getServletContext().removeAttribute(lmid);
		out.println(json.toString());
		out.close();
	}

}
