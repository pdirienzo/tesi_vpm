package it.unina.cini.platino.connections;

import it.unina.cini.platino.db.Database;
import it.unina.cini.platino.db.DatabaseEventDispatcher;
import it.unina.cini.platino.floodlight.FloodlightController;
import it.unina.cini.platino.network.NetworkConverter;
import it.unina.cini.platino.network.types.LinkConnection;
import it.unina.cini.platino.network.types.OvsSwitch;
import it.unina.cini.platino.network.types.VPMGraph;
import it.unina.cini.platino.network.types.VPMGraphHolder;
import it.unina.cini.platino.web.network.path.backend.VPMPathManager;

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

import com.mxgraph.io.mxCodec;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

/**
 * Context listener for the tomcat container. It initializes structures and register
 * this web application as a listener for Floodlight's events
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 */
public class VPMContextServerListener implements ServletContextListener {

	public static final String FLOODLIGHT_CALLBACK_URI = ":8080/VPM/VPMEventListener";
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

	/**
	 * Restores a previously saved network configuration. A check on the actual
	 * network status is performed as well so to check if saved one is still valid.
	 * @param ctx
	 */
	public void restoreNetwork(ServletContext ctx) {

		Properties props = (Properties)ctx.getAttribute("properties");
		BR_NAME = props.getProperty("bridge_name");
		BR_PORT = Integer.parseInt(props.getProperty("ovs_manager_port"));
		VLAN_ID = Integer.parseInt(props.getProperty("vpm_vlan_id"));

		//we'll get a previous topology if saved, following code will be executed just if a valid instance of the controller
		//is present
		try {
			System.out.println("getting old graph if existent...");
			FloodlightController controller = FloodlightController.getDbController();
			//registering listener
			controller.registerListener(FLOODLIGHT_CALLBACK_URI, props.getProperty("network_interface_prefix"));

			mxGraph savedmxGraph = topologyFromFile();
			if(savedmxGraph != null){
				VPMGraph<OvsSwitch, LinkConnection> savedGraph = NetworkConverter.mxToJgraphT(savedmxGraph, true);	
				System.out.println("A saved graph has been found, checking if still valid...");

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
			System.out.println("error contacting controller");
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
			c.getServletContext().setAttribute(VPMPathManager.VPM_PATH_MANAGER, 
					(Class.forName(props.getProperty("path_manager_class")).newInstance()));

			Database.initialize(Database.DEFAULT_DBPATH);
			c.getServletContext().setAttribute(Database.DATABASE, new Database());

			restoreNetwork(c.getServletContext());

			VPMHypervisorConnectionManager manager = new VPMHypervisorConnectionManager(MANAGER_RETRY_TIME,
					props.getProperty("network_name"),
					props.getProperty("bridge_name"),
					props.getProperty("network_interface_prefix"));
			DatabaseEventDispatcher.addListener(manager);
			manager.start();

			c.getServletContext().setAttribute(VPMHypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER, manager);

		} catch (IOException e) {
			System.err.println("Failed to initialize db: "+e.getMessage());
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void contextDestroyed(ServletContextEvent c) {
		VPMHypervisorConnectionManager manager = (VPMHypervisorConnectionManager)c.getServletContext()
				.getAttribute(VPMHypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER);

		if(manager != null){
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



}
