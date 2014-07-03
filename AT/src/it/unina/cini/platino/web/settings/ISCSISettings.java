package it.unina.cini.platino.web.settings;

import it.unina.cini.platino.db.Database;
import it.unina.cini.platino.db.ISCSITarget;

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
 * Servlet implementation class ISCSISettings
 */
@WebServlet("/ISCSISettings")
public class ISCSISettings extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Database db;
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ISCSISettings() {
		super();
	}

	@Override
	public void init() throws ServletException {
		super.init();

		db = (Database)getServletContext().getAttribute(Database.DATABASE);
	}

	/**
	 * @throws IOException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject resp = new JSONObject();
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");

		try {
			db.connect();

			JSONArray targets = new JSONArray();
			for(ISCSITarget target : db.getAllISCSITargets()){
				JSONObject jsonTarget = new JSONObject();
				jsonTarget.put("id", target.id);
				jsonTarget.put("name", target.name);
				jsonTarget.put("hostname", target.hostname);
				jsonTarget.put("iqn", target.iqn);
				jsonTarget.put("port", target.port);

				targets.put(jsonTarget);
			}
			db.close();

			resp.put("status", "ok");
			resp.put("data", targets);
		} catch (IOException ex) {
			resp.put("status", "error");
			resp.put("details", ex.getMessage());
		}

		out.println(resp);
		out.close();

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter pw = response.getWriter();
		response.setContentType("application/json");
		JSONObject jResponse = new JSONObject();

		try{
			db.connect();
			if(request.getParameter("op").equals("add")){
				db.insertISCSITarget(new ISCSITarget(-1, request.getParameter("name"), request.getParameter("hostname"), 
						Integer.parseInt(request.getParameter("port")),request.getParameter("iqn")));
			}else if(request.getParameter("op").equals("del")){
				db.deleteISCSITarget(new ISCSITarget(Integer.parseInt(request.getParameter("id")) , "", "", -1 , ""));
			}else
				throw new IOException("unrecognized operation");
			
			db.close();
			jResponse.put("status", "ok");
			
		}catch(IOException ex){
			jResponse.put("status", "error");
			jResponse.put("details", ex.getMessage());
		}

		pw.println(jResponse);
	}

}
