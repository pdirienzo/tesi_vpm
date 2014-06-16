package org.at.web.settings;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Database;
import org.at.db.ISCSITarget;
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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		JSONObject resp = new JSONObject();
		
		try {
			db.connect();
			
			JSONArray targets = new JSONArray();
			for(ISCSITarget target : db.getAllISCSITargets()){
				JSONObject jsonTarget = new JSONObject();
				jsonTarget.put("name", target.name);
				jsonTarget.put("hostname", target.hostname);
				jsonTarget.put("iqn", target.iqn);
				
				targets.put(jsonTarget);
			}
			db.close();
			
			resp.put("status", "ok");
			resp.put("data", targets);
		} catch (IOException ex) {
			resp.put("status", "error");
			resp.put("details", ex.getMessage());
		}
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
