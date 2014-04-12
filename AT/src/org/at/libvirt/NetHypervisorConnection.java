package org.at.libvirt;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.at.db.Hypervisor;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.Network;

/**
 * Same of HypervisorConnection, but supports a network definition too. 
 * Adding/starting/stopping/removing a vm using this class will automagically attach/detach the vm to/from the created network
 * As we are modifying some resources the connection estabilished by this class will always be read-write
 * 
 * @author pasquale
 *
 */
public class NetHypervisorConnection extends HypervisorConnection{
	
	private Network net;
	
	private boolean networkExists(String networkName) throws LibvirtException{
		boolean result = false;
		int i=0;
		String[] networks = super.listNetworks();
		
		while((!result) && i<networks.length)
			if(networks[i].equals(networkName))
				result = true;
			else
				i++;
		
		return result;
	}
	
	private NetHypervisorConnection(Hypervisor h, String method, String networkName,String networkDefinition) throws LibvirtException{
		super(h,method, false);
		
		if(networkExists(networkName)){
			net = super.networkLookupByName(networkName);
		}else{
			net = super.networkCreateXML(networkDefinition);
		}
	}
	
	private NetHypervisorConnection(Hypervisor h,String networkName,String networkDefinition) throws LibvirtException{
		this(h,DEFAULT_CONN_METHOD,networkName,networkDefinition);
	}
	
	public final static NetHypervisorConnection getConnectionWithTimeout(
			Hypervisor h, String method, String networkName,String networkDefinition, int timeout) throws IOException, LibvirtException{
		
		checkConnection(h, timeout);		
		return new NetHypervisorConnection(h, method,networkName,networkDefinition);
	}
	
	public final static NetHypervisorConnection getConnectionWithTimeout(Hypervisor h,String networkName,
			String networkDefinition,int timeout) throws IOException, LibvirtException{
		
		checkConnection(h, timeout);
		return new NetHypervisorConnection(h,networkName,networkDefinition);
	}
	
	public Network getNetwork(){
		return net;
	}
	
	public void setNetwork(Network net){
		this.net = net;
	}
	
	@Override
	public int close() throws LibvirtException {
		net.destroy();
		return super.close();
	}
	
	@Override
	public void bootDomain(String name) throws LibvirtException {
		Domain d = domainLookupByName(name);
		
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try{
			String descr = d.getXMLDesc(2); //2=VIR_DOMAIN_XML_INACTIVE, we are looking at an inactive domain
			doc = (Document) builder.build(new ByteArrayInputStream(descr.getBytes()));
			Element networkNode = doc.getRootElement().getChild("devices").getChild("interface");
			networkNode.setAttribute("type","network");
			networkNode.getChild("source").setAttribute("network",net.getName());
			d.free();
			d = super.domainCreateXML(new XMLOutputter().outputString(doc), 0);
		}catch(IOException | JDOMException ex){
			ex.printStackTrace();
		}finally{
			d.free();
		}
	}
	
	
	@Override
	public boolean migrate(String domainName, Hypervisor destination)
			throws LibvirtException, IOException {
		
		NetHypervisorConnection destConn = 
				NetHypervisorConnection.getConnectionWithTimeout(destination,net.getName(),net.getXMLDesc(1),
						DEFAULT_TIMEOUT);
		Domain domain = super.domainLookupByName(domainName);
		Domain newDomain = null;
		newDomain = domain.migrate(destConn, 1, null, null, 0);
		destConn.close();
		return (newDomain != null);
	}
}
