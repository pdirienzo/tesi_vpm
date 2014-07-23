package it.unina.cini.platino.web.settings;

import it.unina.cini.platino.connections.VPMContextServerListener;
import it.unina.cini.platino.db.Controller;
import it.unina.cini.platino.db.Database;
import it.unina.cini.platino.floodlight.FloodlightController;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


@WebServlet("/AddController")
public class AddController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Controller c = new Controller(request.getParameter("ip"),Long.valueOf(request.getParameter("port")));
		JSONObject message=new JSONObject();
		PrintWriter out = response.getWriter();
		
		try{
			Database d = new Database();
			d.connect();
			d.insertController(c);
			d.close();
			
			FloodlightController f = new FloodlightController(c);
			f.registerListener(VPMContextServerListener.FLOODLIGHT_CALLBACK_URI);
			
			message.put("status", "controller settings edited");
		}catch(IOException ex){
			message.put("status", "failed to add controller: "+ex.getMessage());
			ex.printStackTrace();
		}finally{
			response.setContentType("application/json");
			out.println(message);
			out.close();
		}
	}
}
