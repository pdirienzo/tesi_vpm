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
