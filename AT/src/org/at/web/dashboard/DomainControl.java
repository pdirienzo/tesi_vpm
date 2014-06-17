package org.at.web.dashboard;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.connections.VPMHypervisorConnectionManager;
import org.at.db.Database;
import org.at.db.ISCSITarget;
import org.at.db.VolumeAllocation;
import org.at.libvirt.HypervisorConnection;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.json.JSONObject;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;

/**
 * Servlet implementation class DomainControl
 */
@WebServlet("/DomainControl")
public class DomainControl extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String XML_VM_FILEPATH = "./xml_definitions/vm_template.xml";
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DomainControl() {
		super();
	}
	
	private String randomMACAddress(){
		Random rand = new Random();
		byte[] macAddr = new byte[6];
		rand.nextBytes(macAddr);
		
		macAddr[0] = (byte)(macAddr[0] & (byte)254);  //zeroing last 2 bytes to make it unicast and locally adminstrated
		
		StringBuilder sb = new StringBuilder(18);
		for(byte b : macAddr){
			
			if(sb.length() > 0)
				sb.append(":");
			
			sb.append(String.format("%02x", b));
		}
		
		
		return sb.toString();
	}
	
	public static void main(String[] args){
	}
	
	private boolean volumeIsIn(String volumeName, List<VolumeAllocation> allocations){
		boolean found = false;
		int i = 0;
		
		while((!found) && (i<allocations.size()))
			if(allocations.get(i).volume.equals(volumeName))
				found = true;
			else
				i++;
		
		return found;
	}
	
	private String getVMDescription(String vmname, String iscsiPath) throws IOException{
		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(XML_VM_FILEPATH);
 
		Document doc = null;
		try{
			doc = (Document) builder.build(xmlFile);
		}catch(JDOMException ex){
			throw new IOException(ex.getMessage());
		}
		
		Element rootNode = doc.getRootElement();
		rootNode.getChild("name").setText(vmname);
		rootNode.getChild("uuid").setText(UUID.randomUUID().toString());
		
		Element devicesNode = rootNode.getChild("devices");
		
		//settings iscsi
		devicesNode.getChild("disk").getChild("source").setAttribute("dev",iscsiPath);
		
		//setting mac address
		devicesNode.getChild("interface").getChild("mac").setAttribute("address",randomMACAddress());
		
		//setting network
		String network_name = ((Properties)getServletContext().getAttribute("properties")).getProperty("network_name");
		
		devicesNode.getChild("interface").getChild("source").setAttribute("network",network_name);
		
		return new XMLOutputter().outputString(doc);
	}
	
	private void createVM(HypervisorConnection conn, String hypervisorId, String vmName, boolean autostart) throws IOException, LibvirtException{
		Database d = (Database)getServletContext().getAttribute(Database.DATABASE);
		d.connect();
		
		int iscsiID = d.getHypervisorById(hypervisorId).getISCSI();
		ISCSITarget iscsiTarget = d.getTargetById(iscsiID);
		List<VolumeAllocation> allocations = d.getISCSIVolumes(iscsiID);
		
		d.close();
		
		System.out.println(iscsiTarget.name);
		StoragePool sp = conn.storagePoolLookupByName(iscsiTarget.name);
		
		
		
		String[] volumes = sp.listVolumes();
		
		if(allocations.size() == volumes.length) //following code will be executed just if there is at least a free LUN
			throw new IOException("The ISCSI Target for this hypervisor has no free LUNs. Add some more or delete some vms from other hypervisors");
		
		//finding a free lun
		boolean found = false;
		int i = 0;
		while( (!found) && (i<volumes.length)){
			if(!volumeIsIn(volumes[i], allocations))
				found = true; //found a free volume
			else
				i++;
		}
		
		//building the path
		String iscsiFullPath = "/dev/disk/by-path/ip-"+InetAddress.getByName(iscsiTarget.hostname).getHostAddress()+":" +iscsiTarget.port + "-iscsi-"+
				iscsiTarget.iqn+"-lun-"+i; 
		
		//creating (finally)
		
		Domain newDomain = conn.domainDefineXML(getVMDescription(vmName, iscsiFullPath));
		if(autostart)
			newDomain.create();
		
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


		VPMHypervisorConnectionManager manager = (VPMHypervisorConnectionManager)getServletContext().getAttribute(
				VPMHypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER);
		HypervisorConnection conn = manager.getActiveConnection(hypervisorId);

		System.out.println("action "+action);
		try{
			if(action.equals("boot")){
				conn.bootDomain(guestName);
				jResponse.put("result", "success");
			}else if(action.equals("create")){
				createVM(conn, hypervisorId, guestName, Boolean.parseBoolean(request.getParameter("autostart")));
				jResponse.put("result", "success");
			}else if(action.equals("shutdown")){
				conn.shutdownDomain(guestName);
				jResponse.put("result", "success");
			}else if(action.equals("destroy")){
				
			}else{
				jResponse.put("result", "error");
				jResponse.put("details", "unrecognized action");
			}



		}catch(IOException | LibvirtException ex){
			jResponse.put("result", "error");
			jResponse.put("details",ex.getMessage());
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
