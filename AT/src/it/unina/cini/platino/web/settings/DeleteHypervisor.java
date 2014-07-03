package it.unina.cini.platino.web.settings;

import it.unina.cini.platino.db.Database;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


@WebServlet("/DeleteHypervisor")
public class DeleteHypervisor extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		JSONObject message = new JSONObject();

		String hostname=request.getParameter("hostname");
		try{
			Database d = new Database();
			d.connect();
			d.deleteHypervisor(hostname);
			d.close();
			message.put("status","ok");
			message.put("details","hypervisor eliminato con successo");

		}catch(IOException ex){
			message.put("status", "error");
			message.put("details", "fallita eliminazione hypervisor: "+ex.getMessage());
			ex.printStackTrace();
		}finally{
			response.setContentType("application/json");
			out.println(message);
			out.close();
		}
	}

}
