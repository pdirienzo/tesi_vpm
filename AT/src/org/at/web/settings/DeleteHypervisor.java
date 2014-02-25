package org.at.web.settings;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Database;
import org.json.JSONObject;


@WebServlet("/DeleteHypervisor")
public class DeleteHypervisor extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		JSONObject message = new JSONObject();
		
		String hostname=request.getParameter("hostname");
		try{
			Database d = (Database)getServletContext().getAttribute("database");
			d.deleteHypervisor(hostname);
			
			message.put("status","hypervisor eliminato con successo" );
		}catch(IOException ex){
			message.put("status", "fallita eliminazione hypervisor: "+ex.getMessage());
			ex.printStackTrace();
		}finally{
			response.setContentType("application/json");
			out.println(message);
			out.close();
		}
	}
	
}
