package org.at.connections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.at.db.Database;
import org.at.db.DatabaseEventDispatcher;
import org.at.floodlight.FloodlightController;
import org.at.network.NetworkConverter;
import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.at.network.types.VPMGraph;
import org.at.network.types.VPMGraphHolder;
import org.at.web.network.path.DefaultVPMPathManager;
import org.at.web.network.path.VPMPathManager;

import com.mxgraph.io.mxCodec;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

public class VPMContextServerListener implements ServletContextListener {

	private static final String TOPOLOGY_XML = "./topology/topology.xml";
	private static final int MANAGER_RETRY_TIME = 5000;	
	private static final String PROPERTIES_PATH = "config/config.xml";
	public static String BR_NAME;
	public static int BR_PORT;
	public static int VLAN_ID;
	
	
	private mxGraph topologyFromFile() throws IOException{
		mxGraph graph = null;

		if(new File(TOPOLOGY_XML).exists()){
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(TOPOLOGY_XML)));
			StringBuilder xml = new StringBuilder();
			String read = null;
			while((read=reader.readLine()) != null)
				xml.append(read);
			reader.close();

			graph = new mxGraph();
			org.w3c.dom.Node node = mxXmlUtils.parseXml(xml.toString());
			mxCodec decoder = new mxCodec(node.getOwnerDocument());
			decoder.decode(node.getFirstChild(),graph.getModel());
		}

		return graph;
	}
	
	public void restoreNetwork(ServletContext ctx) {

		Properties props = (Properties)ctx.getAttribute("properties");
		BR_NAME = props.getProperty("bridge_name");
		BR_PORT = Integer.parseInt(props.getProperty("ovs_manager_port"));
		VLAN_ID = Integer.parseInt(props.getProperty("vpm_vlan_id"));

		//we'll get a previous topology if saved
		try {
			System.out.println("getting old graph if existent...");

			mxGraph savedmxGraph = topologyFromFile();
			if(savedmxGraph != null){
				VPMGraph<OvsSwitch, LinkConnection> savedGraph = NetworkConverter.mxToJgraphT(savedmxGraph, true);
				
				System.out.println("A saved graph has been found, checking if still valid...");
				FloodlightController controller = FloodlightController.getDbController();
				VPMGraph<OvsSwitch, LinkConnection> actualGraph = controller.getTopology();
				//we have to check if every tree edge defined in the saved graph still exists
				//(just tree edges are phisical links and so visible by the controller)
				boolean valid = true;
				int savedTreeEdgesCount = 0;

				if(savedGraph.vertexSet().size() == actualGraph.vertexSet().size()){ //rough control: if the vertex size is different then it's 
																					//100% different
					
					Iterator<LinkConnection> savedEdges = savedGraph.edgeSet().iterator();
					while( (valid) && savedEdges.hasNext()){
						LinkConnection tEdge = savedEdges.next();
						if(tEdge.isTree){ //we just care about tree edges as they are the only phisical links the controller can see
							savedTreeEdgesCount++;
							valid = actualGraph.containsEdge(tEdge.getSource(),tEdge.getTarget());// || 
									//(actualGraph.containsEdge(tEdge.getTarget(),tEdge.getSource()));//checking if exists

							if(valid){ //between two different starts controller may have been restarted and so port ids could have
								//been changed
								tEdge = actualGraph.getEdge(tEdge.getTarget(), tEdge.getSource());
							}
						}
					}

					if(savedTreeEdgesCount != actualGraph.edgeSet().size())
						valid = false;

				}else
					valid = false;


				if(valid){
					((VPMGraphHolder)
							ctx.getAttribute(VPMGraphHolder.VPM_GRAPH_HOLDER)).addGraph(savedGraph);
					System.out.println("Valid graph reverted back"+savedGraph);
				}else{
					//TODO send a message informing the user that saved graph is not valid anymore
					System.err.println("Saved graph is not valid anymore");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void contextInitialized(ServletContextEvent c) {
		System.out.println("this game will be started");
		
		//Initializing db if this is the first app start
		try {
			Properties props = new Properties();
			props.loadFromXML(new FileInputStream(PROPERTIES_PATH));
			c.getServletContext().setAttribute("properties", props);
			
			c.getServletContext().setAttribute(VPMGraphHolder.VPM_GRAPH_HOLDER, 
					new VPMGraphHolder());
			c.getServletContext().setAttribute(VPMPathManager.VPM_PATH_MANAGER, new DefaultVPMPathManager());
			
			Database.initialize(Database.DEFAULT_DBPATH);
			c.getServletContext().setAttribute(Database.DATABASE, new Database());
			
			restoreNetwork(c.getServletContext());
			
			VPMHypervisorConnectionManager manager = new VPMHypervisorConnectionManager(MANAGER_RETRY_TIME,props.getProperty("network_name"),
					props.getProperty("bridge_name"));
			DatabaseEventDispatcher.addListener(manager);
			manager.start();
			
			c.getServletContext().setAttribute(VPMHypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER, manager);
			
		} catch (IOException e) {
			System.err.println("Failed to initialize db: "+e.getMessage());
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent c) {
		VPMHypervisorConnectionManager manager = (VPMHypervisorConnectionManager)c.getServletContext()
				.getAttribute(VPMHypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER);
		try {
			manager.stop();
			System.out.println("DB connection closed");
			FloodlightController controller = FloodlightController.getDbController();
			controller.resetAllFlowsForAllSwitches();
			System.out.println("Flows resetted");
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}

	

}
