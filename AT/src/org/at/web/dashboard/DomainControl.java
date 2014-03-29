package org.at.web.dashboard;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.connections.HypervisorConnectionManager;
import org.at.libvirt.HypervisorConnection;
import org.json.JSONObject;
import org.libvirt.LibvirtException;

/**
 * Servlet implementation class DomainControl
 */
@WebServlet("/DomainControl")
public class DomainControl extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DomainControl() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String hypervisorId = request.getParameter("hypervisorId");
		String guestName = request.getParameter("guestName");
		String action = request.getParameter("action");
		
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		JSONObject jResponse = new JSONObject();
		
		if( (hypervisorId == null) || (guestName == null) || (action == null)){
			jResponse.put("result", "error");
			jResponse.put("details", "missing some important paramether");
			
		}else{
			HypervisorConnectionManager manager = (HypervisorConnectionManager)getServletContext().getAttribute(
				HypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER);
			HypervisorConnection conn = manager.getActiveConnection(hypervisorId);
		
			try{
				if(action.equals("boot")){
					conn.bootDomain(guestName);
					jResponse.put("result", "success");
				}else if(action.equals("shutdown")){
					conn.shutdownDomain(guestName);
					jResponse.put("result", "success");
				}else{
					jResponse.put("result", "error");
					jResponse.put("details", "unrecognized action");
				}
				
				
				
			}catch(LibvirtException ex){
				jResponse.put("result", "error");
				jResponse.put("details",ex.getMessage());
			}
		}
		
		out.println(jResponse.toString());
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
