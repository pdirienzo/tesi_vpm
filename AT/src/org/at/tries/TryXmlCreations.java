package org.at.tries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.libvirt.LibvirtException;

public class TryXmlCreations {

	private static final String XML_ROOT ="xml_definitions"; 
	
	public static void main(String[] args) throws JDOMException, IOException, LibvirtException {
		Properties props = new Properties();
		props.loadFromXML(new FileInputStream("config/config.xml"));
		
		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(XML_ROOT+"/vm_template.xml");
 
		Document doc = (Document) builder.build(xmlFile);
		Element rootNode = doc.getRootElement();
		
		XMLOutputter o = new XMLOutputter();
		System.out.println(o.outputString(doc));
		
		/*HypervisorConnection h = HypervisorConnection.getConnectionWithTimeout(
				new Hypervisor("pasquale","pasquale-VPCEB1A4E", 16514), false, 3000);
		
		
		h.close();*/
		
	}

}
