package org.at.web.settings;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.at.db.Database;
import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.json.JSONObject;
import org.libvirt.LibvirtException;


@WebServlet("/DeleteHypervisor")
public class DeleteHypervisor extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private void deleteNetwork(Hypervisor h) throws IOException, LibvirtException{
		HypervisorConnection conn = HypervisorConnection.getConnectionWithTimeout(h, false, 3000);
		conn.networkLookupByName("vpm-network").undefine();
		conn.close();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		JSONObject message = new JSONObject();
		
		String hostname=request.getParameter("hostname");
		try{
			String details = "";
			Database d = new Database();
			d.connect();
			Hypervisor h = d.getHypervisorByIp(hostname);
			try{
				deleteNetwork(h);
				details = "hypervisor eliminato con successo";
			}catch(IOException | LibvirtException ex){
				details = "hypervisor non online: non e' stato possibile eliminare la rete";
			}finally{
				d.deleteHypervisor(hostname);
				d.close();
			}
			message.put("status","ok");
			message.put("details",details );
			
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
