package org.at.libvirt;

import java.io.IOException;

import org.at.db.Hypervisor;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainJobInfo;
import org.libvirt.LibvirtException;

public class Libvirt {
	
	public static Connect getHypervisorConnection(Hypervisor h) {
		try {
			return new Connect("qemu+ssh://"+h.getName()+"@"+h.getHostAddress()+"/system?no_tty=1", false); //+"/system?no_tty=1",false);
		} catch (LibvirtException e) {e.printStackTrace();}
		return null;
	}
	
	public static Domain migrate(Domain src, Connect dst) {
		try {
			return src.migrate(dst, 1, null,null, 0);
		} catch (LibvirtException e) {e.printStackTrace();}	
		return null;
	}
	
	/*
	public static String getNameByMacAddress(String macTarget) throws IOException {
		Database database = new Database();
		database.connect();
		List<Hypervisor> hosts = database.getAllHypervisors();
		database.close();
		
		try {
			String result = null;
			
			for(Hypervisor host : hosts) {
				Connect conn = getHypervisorConnection(host);
				String[] domains = conn.listDefinedDomains();
				
				Domain d=null;
				int i = 0;
				while((result == null) && (i<domains.length)) {
					d = conn.domainLookupByName(domains[i]);
					String conf = d.getXMLDesc(0);
					d.free();
					if(conf.contains(macTarget.toLowerCase())){
						result = d.getName();
					}else
						i++;
				}
				conn.close();

			}
		} catch (LibvirtException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
			
		}
		return null;
	}
	*/
	
	public static String getMacAddressByName(String name) throws IOException{
		/*Database database = new Database();
		database.connect();
		List<Hypervisor> hosts = database.getAllHypervisors();
		database.close();
		try {
			for(Hypervisor h: hosts) {
				Connect conn = getHypervisorConnection(h);
				Domain d = conn.domainLookupByName(name);
				if(d!=null) {
					String conf = d.getXMLDesc(0);
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					ByteArrayInputStream xml = new ByteArrayInputStream(conf.getBytes());
					Document doc = db.parse(xml);
					Element net = (Element)doc.getElementsByTagName("mac").item(0);
					String mac = net.getAttribute("address");
					return mac;
				}
				
				conn.close();
			}
		} catch (LibvirtException | SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		finally{
			System.gc();
		}*/
		return null;
	}
	
	public static void getJobInfo(Domain d){
		try {
			DomainJobInfo info= d.getJobInfo();
			System.out.println("mem processed "+info.getMemProcessed()/1024/1024);
			System.out.println("mem remaining "+info.getMemRemaining()/1024/1024);
			System.out.println("mem total "+info.getMemTotal()/1024/1024);
			System.out.println("data processed "+info.getDataProcessed()/1024/1024);
			System.out.println("data remaining "+info.getDataRemaining()/1024/1024);
			System.out.println("data total "+info.getDataTotal()/1024/1024);
			System.out.println("time elapsed "+info.getTimeElapsed());
			System.out.println("time remaining "+info.getTimeRemaining());
		} catch (LibvirtException e) {e.printStackTrace();}
	}
	
}
