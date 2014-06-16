package org.at.web.settings;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Database;
import org.at.db.Hypervisor;
import org.json.JSONObject;


@WebServlet("/AddHypervisor")
public class AddHypervisor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		System.out.println("Check iscsi "+Integer.valueOf(request.getParameter("iscsi")));
		
		Hypervisor host2add=new Hypervisor(request.getParameter("username"),
								request.getParameter("ip"), 
								Long.valueOf(request.getParameter("port")),
								Integer.valueOf(request.getParameter("iscsi")));
		
		JSONObject message=new JSONObject();
		
		try{
			Database d = new Database();
			d.connect();
			if(d.hypervisorExists(host2add.getHostname())){
				message.put("status", "hypervisor gia' presente nella lista");
			}else{
				d.insertHypervisor(host2add);
				message.put("status", "hypervisor aggiunto alla lista");
			}
			
			d.close();
			
		}catch(IOException ex){
			message.put("status", "fallito inserimento host: "+ex.getMessage());
			ex.printStackTrace();
		}finally{
			response.setContentType("application/json");
			out.println(message);
			out.close();
		}
	}
	
}
