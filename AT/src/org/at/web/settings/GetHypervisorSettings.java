package org.at.web.settings;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Database;
import org.at.db.Hypervisor;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class GetHypervisorSettings
 */
@WebServlet("/GetHypervisorSettings")
public class GetHypervisorSettings extends HttpServlet {
	private static final long serialVersionUID = 1L;


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = new PrintWriter(response.getOutputStream());

		JSONArray hypervisorList = new JSONArray();

		try{
			Database d = (Database)getServletContext().getAttribute("database");
			List<Hypervisor> hosts = d.getAllHypervisors();	

			for (Hypervisor h: hosts)
			{
				JSONObject hypervisor = new JSONObject()
				.put("hostname",h.getHostAddress())
				.put("username", h.getName())
				.put("port", h.getPort());

				hypervisorList.put(hypervisor);
			}

		}catch(IOException e){
			JSONObject error = new JSONObject();
			error.put("status", "fallito a ritirare la lista hypervisors");
			hypervisorList.put(error);
		}finally{
			response.setContentType("application/json");
			out.println(hypervisorList);
			out.close();
		}
	}

}
