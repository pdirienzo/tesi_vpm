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
import org.libvirt.DomainJobInfo;

/**
 * Servlet implementation class MigrationStatus
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
		DomainJobInfo dj = mt.getJobStats();
		if(dj != null){
			state.put("state", String.valueOf(MigrationThread.MIGRATION_PROGRESS))
			.put("processed", String.valueOf(dj.getMemProcessed()))
			.put("remaining", String.valueOf(dj.getMemRemaining()))
			.put("total", String.valueOf(dj.getMemTotal()));
		}else{//TODO this is just dummy to adapt to old client interface
			state.put("state", String.valueOf(mt.getMigrationStatus()))
			.put("processed", 100)
			.put("remaining", 100)
			.put("total", 100);
		}

		out.println(state.toString());
		out.close();
	}

}
