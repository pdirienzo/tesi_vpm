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
import org.libvirt.Network;


@WebServlet("/AddHypervisor")
public class AddHypervisor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String XML_NETWORK_FILEPATH = "xml_definitions/vpm_network.xml";
	
	/**
	 * Creates and starts a new network
	 * @param h
	 * @throws LibvirtException 
	 * @throws IOException 
	 */
	private void createNetwork(Hypervisor h) throws IOException, LibvirtException{
		HypervisorConnection conn = HypervisorConnection.getConnectionWithTimeout(h, false, 3000);
		Network n = conn.createNetworkFromFile(XML_NETWORK_FILEPATH);
		if(n != null){
			n.setAutostart(true);
			n.create();
			n.free();
		}
		conn.close();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		
		Hypervisor host2add=new Hypervisor(request.getParameter("username"),
								request.getParameter("ip"), 
								Long.valueOf(request.getParameter("port")));
		
		JSONObject message=new JSONObject();
		
		try{
			Database d = new Database();
			d.connect();
			if(d.hypervisorExists(host2add.getHostAddress())){
				message.put("status", "hypervisor gia' presente nella lista");
			}else{
				createNetwork(host2add); // creating a network on the hypervisor
				d.insertHypervisor(host2add);
				message.put("status", "hypervisor aggiunto alla lista");
			}
			
			d.close();
			
		}catch(IOException | LibvirtException ex){
			message.put("status", "fallito inserimento host: "+ex.getMessage());
			ex.printStackTrace();
		}finally{
			response.setContentType("application/json");
			out.println(message);
			out.close();
		}
	}
	
}
