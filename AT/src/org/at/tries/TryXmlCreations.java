package org.at.tries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;

public class TryXmlCreations {

	private static final String XML_ROOT ="xml_definitions"; 
	
	public static void main(String[] args) throws JDOMException, IOException, LibvirtException {
		String iqn = "iqn.2014-02.lime:vm1";
		String name = "vpmstreaming";
		String hostname = "pasquale-VPCEB1A4E";
		
		Properties props = new Properties();
		props.loadFromXML(new FileInputStream("config/config.xml"));
		
		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(XML_ROOT+"/storage_template.xml");
 
		Document doc = (Document) builder.build(xmlFile);
		Element rootNode = doc.getRootElement();
		rootNode.getChild("name").setText(name);
		rootNode.getChild("uuid").setText(UUID.randomUUID().toString());
		Element sourceNode = rootNode.getChild("source");
		sourceNode.getChild("host").setAttribute("name",name);
		sourceNode.getChild("device").setAttribute("path",iqn);
		sourceNode.getChild("initiator").getChild("iqn").setAttribute("name",iqn);
		
		XMLOutputter o = new XMLOutputter();
		System.out.println(o.outputString(doc));
		
		HypervisorConnection h = HypervisorConnection.getConnectionWithTimeout(
				new Hypervisor("pasquale","pasquale-VPCEB1A4E", 16514,0), false, 3000);
		
		StoragePool pollo = null;
		
		try{
			pollo = h.storagePoolLookupByName(name);
			System.out.println(pollo.getXMLDesc(0));
		}catch(LibvirtException ex){
			System.out.println("pollo doesn't exists, creating");
			pollo = h.storagePoolDefineXML(o.outputString(doc), 0);
			pollo.create(0);
			pollo.setAutostart(1);
		}
	
		for(String vol : pollo.listVolumes())
			System.out.println(vol);
		//System.out.println(pollo.getXMLDesc(0));
		h.close();
		
	}

}
