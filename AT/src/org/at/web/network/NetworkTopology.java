package org.at.web.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Controller;
import org.at.db.Database;
import org.at.floodlight.FloodlightController;
import org.at.network.NetworkConverter;
import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.at.network.types.Port;
import org.at.network.types.VPMGraph;
import org.at.network.types.VPMGraphHolder;
import org.at.web.network.path.VPMPathHolder;
import org.at.web.network.path.VPMSwitchInfoHolder;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.json.JSONObject;
import org.opendaylight.ovsdb.lib.notation.OvsDBSet;
import org.opendaylight.ovsdb.lib.notation.OvsdbOptions;
import org.opendaylight.ovsdb.lib.standalone.DefaultOvsdbClient;
import org.opendaylight.ovsdb.lib.standalone.OvsdbException;
import org.opendaylight.ovsdb.lib.table.Interface;

import com.mxgraph.io.mxCodec;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

/**
 * Servlet implementation class GetNetworkTopology
 */
@WebServlet("/NetworkTopology")
public class NetworkTopology extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String TOPOLOGY_XML = "./topology/topology.xml";

	private String BR_NAME;
	private int BR_PORT;
	private int VLAN_ID;


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

	private void topologyToFile(mxGraph topo) throws FileNotFoundException{
		mxCodec codec = new mxCodec();	
		String xmlString =  mxXmlUtils.getXml(codec.encode(topo.getModel()));

		PrintWriter pw = new PrintWriter(new File(TOPOLOGY_XML));
		pw.write(xmlString);
		pw.flush();
		pw.close();
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public NetworkTopology() {
		super();
	}

	@Override
	public void init() throws ServletException {
		super.init();

		Properties props = (Properties)getServletContext().getAttribute("properties");
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
				VPMGraph<OvsSwitch, LinkConnection> actualGraph = getController().getTopology();
				//we have to check if every tree edge defined in the saved graph still exists
				//(just tree edges are phisical links and so visible by the controller)
				boolean valid = true;
				int savedTreeEdgesCount = 0;
				
				if(savedGraph.vertexSet().size() == actualGraph.vertexSet().size()){
					Iterator<LinkConnection> savedEdges = savedGraph.edgeSet().iterator();
					while( (valid) && savedEdges.hasNext()){
						LinkConnection tEdge = savedEdges.next();
						if(tEdge.isTree){
							savedTreeEdgesCount++;
							valid = (actualGraph.containsEdge(tEdge.getSource(),tEdge.getTarget()) || 
								actualGraph.containsEdge(tEdge.getTarget(),tEdge.getSource()));//checking if exists

							if(valid){ //between two different starts controller may have been restarted and so port ids could have
								//been changed
								LinkConnection freshL = actualGraph.getEdge(tEdge.getSource(), tEdge.getTarget());
								System.out.println(freshL);
								tEdge.setSourceP(freshL.getSrcPort());
								tEdge.setTargetP(freshL.getTargetPort());
							}
						}
					}
					
					if(savedTreeEdgesCount != actualGraph.edgeSet().size())
						valid = false;
				
				}else
					valid = false;


				if(valid){
					((VPMGraphHolder)
							getServletContext().getAttribute(VPMGraphHolder.VPM_GRAPH_HOLDER)).addGraph(savedGraph);
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


	private FloodlightController getController() throws IOException{
		Database d = new Database();
		d.connect();
		Controller c = d.getController();
		d.close();

		if(c==null)
			return null;

		FloodlightController controller = new FloodlightController(c);

		return controller;
	}



	private KruskalMinimumSpanningTree<OvsSwitch, LinkConnection> createTree(VPMGraph<OvsSwitch,LinkConnection> graph){	

		KruskalMinimumSpanningTree<OvsSwitch, LinkConnection> k = new KruskalMinimumSpanningTree<OvsSwitch,LinkConnection>(graph);

		Iterator<LinkConnection> iterator = graph.edgeSet().iterator();

		iterator = k.getMinimumSpanningTreeEdgeSet().iterator();

		while(iterator.hasNext()){
			LinkConnection l = iterator.next();
			l.isTree = true;
			graph.setEdgeWeight(l, 0); //setting tree's weight to zero so that minimum shortest path alghoritm just inspects tree edges
		}

		return k;
	}


	private void potateRelays(VPMGraph<OvsSwitch, LinkConnection> g){
		for(OvsSwitch sw : g.vertexSet()){
			if(sw.type == OvsSwitch.Type.RELAY || sw.type == OvsSwitch.Type.NULL){
				boolean hasLeaf = false;
				boolean hasRootRelay = false;

				Set<LinkConnection> links = g.edgesOf(sw);
				for(LinkConnection l : links){
					if(l.isTree){
						if( (l.getTarget().type == OvsSwitch.Type.LEAF) || (l.getSource().type == OvsSwitch.Type.LEAF))
							hasLeaf = true;
						else if( (l.getTarget().type == OvsSwitch.Type.RELAY || l.getTarget().type == OvsSwitch.Type.ROOT)
								|| (l.getSource().type == OvsSwitch.Type.RELAY || l.getSource().type == OvsSwitch.Type.ROOT))
							hasRootRelay = true;
					}
				}

				if(!(hasLeaf && hasRootRelay) ){
					for(LinkConnection l : g.edgesOf(sw))
						if(l.isTree)
							l.isTree = false;
				}
			}
		}
	}


	/**
	 * @throws IOException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject jsResp = new JSONObject();

		try{
			VPMGraph<OvsSwitch, LinkConnection> graph = null;
			VPMGraphHolder holder = (VPMGraphHolder)
					getServletContext().getAttribute(VPMGraphHolder.VPM_GRAPH_HOLDER);

			if( (graph = holder.getGraph()) == null){ //first time execution or invalid graph

				FloodlightController controller = getController();

				if(controller != null){
					graph = controller.getTopology();
					holder.addGraph(graph);

					mxCodec codec = new mxCodec();	
					String xmlString =  mxXmlUtils.getXml(codec.encode(
							(NetworkConverter.jgraphToMx(graph)).getModel()));

					jsResp.put("status", "ok");
					jsResp.put("graph", xmlString);

				}else{
					jsResp.put("status", "error");
					jsResp.put("details", "No controller set");
				}

			}else{
				mxCodec codec = new mxCodec();	
				String xmlString =  mxXmlUtils.getXml(codec.encode(NetworkConverter.jgraphToMx(graph).getModel()));
				jsResp.put("status", "ok");
				jsResp.put("graph", xmlString);
			}


		}catch(IOException ex){
			System.err.println(ex.getMessage());
			jsResp.put("status","error");
			jsResp.put("details", "Communication error with "+ex.getMessage());
		}finally{
			out.println(jsResp.toString());
			out.close();
		}

	}

	/**
	 * TODO experimental, do it better...it just takes last 2 symbols of dpids
	 * @param dpidSrc
	 * @param dpidDst
	 * @return
	 */
	private String computePortName(String dpidSrc,String dpidDst){
		StringBuilder sb = new StringBuilder();
		sb.append("gre");
		String[] subs = dpidSrc.split(":");
		sb.append(subs[subs.length-2]);
		sb.append(subs[subs.length-1]);

		subs = dpidDst.split(":");
		sb.append(subs[subs.length-2]);
		sb.append(subs[subs.length-1]);

		return sb.toString();

	}

	private List<LinkConnection> getTreeNodes(VPMGraph<OvsSwitch, LinkConnection> jgraph){
		List<LinkConnection> tList = new ArrayList<LinkConnection>();

		for(LinkConnection lc : jgraph.edgeSet())
			if(lc.isTree)
				tList.add(lc);

		return tList;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = new PrintWriter(response.getOutputStream());
		response.setContentType("application/json");
		JSONObject jResponse = new JSONObject();

		VPMGraphHolder holder = (VPMGraphHolder)getServletContext().getAttribute(VPMGraphHolder.VPM_GRAPH_HOLDER);
		VPMGraph<OvsSwitch, LinkConnection> oldGraph = holder.getGraph();

		FloodlightController controller = getController();

		if(oldGraph != null){ //if it is still valid
			try{
				//we get user made topology
				mxGraph userGraph = new mxGraph();
				org.w3c.dom.Node node = mxXmlUtils.parseXml(request.getParameter("xml"));
				mxCodec decoder = new mxCodec(node.getOwnerDocument());
				decoder.decode(node.getFirstChild(),userGraph.getModel());	

				//converting
				VPMGraph<OvsSwitch, LinkConnection> jgraph =
						(VPMGraph<OvsSwitch, LinkConnection>) NetworkConverter.mxToJgraphT(userGraph, false);

				//from now we have to forget about usergraph as just jgraph will be updated

				//creating tree
				createTree(jgraph); //it sets isTree=true for tree edges
				//potating relays
				potateRelays(jgraph);

				//we get previous and current link connections:we'll compare them to see if we need to phisically add/remove
				//gre tunnels
				List<LinkConnection> previousTreeLinks = getTreeNodes(oldGraph);
				List<LinkConnection> newTreeLinks = getTreeNodes(jgraph);

				for(LinkConnection l : newTreeLinks){

					//we try to remove the link from the old list: if it is contained it
					//will not be processed as it existed before and still exists now
					boolean removed = previousTreeLinks.remove(l);

					if(!removed){ //if remotion fails it means this link is not currently present 

						// so we phisically add the link to the switches as it is part of a tree

						//1. starting from source switch
						DefaultOvsdbClient client = new DefaultOvsdbClient(l.getSource().ip, BR_PORT);
						String ovs = null;

						OvsDBSet<Integer> trunks = new OvsDBSet<Integer>();
						trunks.add(VLAN_ID);

						ovs = client.getOvsdbNames()[0];
						OvsdbOptions opts = new OvsdbOptions();
						opts.put(OvsdbOptions.REMOTE_IP, l.getTarget().ip);
						String srcPortName = computePortName(l.getSource().dpid,l.getTarget().dpid);
						client.addPort(ovs, BR_NAME, srcPortName, Interface.Type.gre.name(),0,trunks,opts);

						l.setSourceP(new Port(srcPortName,controller.getPortNumber(l.getSource(), srcPortName)));

						//2. now the other one
						client = new DefaultOvsdbClient(l.getTarget().ip, BR_PORT);
						opts = new OvsdbOptions();
						opts.put(OvsdbOptions.REMOTE_IP, l.getSource().ip);
						String targetPortName = computePortName(l.getTarget().dpid,l.getSource().dpid);
						client.addPort(ovs, BR_NAME, targetPortName, Interface.Type.gre.name(),0,trunks,opts);

						l.setTargetP(new Port(targetPortName,controller.getPortNumber(l.getTarget(), targetPortName)));
						//System.out.println("Creating "+computePortName(l.getSource().dpid,l.getTarget().dpid)+" on "+l.getSource());
						//System.out.println("Creating "+computePortName(l.getTarget().dpid,l.getSource().dpid)+" on "+l.getTarget());


					} //if it is not part of a tree we do nothing
				}

				if(previousTreeLinks.size() > 0){ //user deleted some link
					for(LinkConnection l : previousTreeLinks){
						//System.out.println("To delete " + l);
						DefaultOvsdbClient client = new DefaultOvsdbClient(l.getSource().ip, BR_PORT);
						String ovs = client.getOvsdbNames()[0];
						//1.
						client.deletePort(ovs, BR_NAME,l.getSrcPort().name);

						//2.
						client = new DefaultOvsdbClient(l.getTarget().ip, BR_PORT);
						client.deletePort(ovs, BR_NAME,l.getTargetPort().name);

					}
				}

				controller.resetAllFlowsForAllSwitches(); //resetting flows
				getServletContext().setAttribute(VPMSwitchInfoHolder.SWITCH_INFO_HOLDER, new VPMSwitchInfoHolder());//resetting switches
				getServletContext().setAttribute(VPMPathHolder.VPM_PATHS, new VPMPathHolder()); //and path variables

				holder.addGraph(jgraph);
				jResponse.put("status", "ok");

				//finally saving topology to file
				topologyToFile(NetworkConverter.jgraphToMx(jgraph));

			}catch(IOException | OvsdbException ex){
				jResponse.put("status", "error");
				jResponse.put("details", ex.getMessage());
				ex.printStackTrace();
			}

		}else{
			jResponse.put("status", "error");
			jResponse.put("details", "Something changed in the topology, please check again");
		}

		out.println(jResponse.toString());
		out.close();
	}


}

