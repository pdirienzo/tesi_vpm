package org.at.web.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

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
import org.jgrapht.Graph;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;
import org.json.JSONObject;
import org.opendaylight.ovsdb.lib.notation.OvsDBSet;
import org.opendaylight.ovsdb.lib.notation.OvsdbOptions;
import org.opendaylight.ovsdb.lib.standalone.DefaultOvsdbClient;
import org.opendaylight.ovsdb.lib.standalone.OvsdbException;
import org.opendaylight.ovsdb.lib.table.Interface;

import com.mxgraph.io.mxCodec;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

/**
 * Servlet implementation class GetNetworkTopology
 */
@WebServlet("/NetworkTopology")
public class NetworkTopology extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String TOPOLOGY_XML = "./topology/topology.xml";

	public static final String VPM_TOPOLOGY = "vpm_topo";

	private String BR_NAME;
	private int BR_PORT;
	private int VLAN_ID;


	private mxGraph topologyFromFile() throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(TOPOLOGY_XML)));
		StringBuilder xml = new StringBuilder();
		String read = null;
		while((read=reader.readLine()) != null)
			xml.append(read);
		reader.close();

		mxGraph graph = new mxGraph();
		org.w3c.dom.Node node = mxXmlUtils.parseXml(xml.toString());
		mxCodec decoder = new mxCodec(node.getOwnerDocument());
		decoder.decode(node.getFirstChild(),graph.getModel());

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



	private KruskalMinimumSpanningTree<OvsSwitch, LinkConnection> createTree(ListenableUndirectedWeightedGraph<OvsSwitch,LinkConnection> graph){	

		KruskalMinimumSpanningTree<OvsSwitch, LinkConnection> k = new KruskalMinimumSpanningTree<OvsSwitch,LinkConnection>(graph);


		Iterator<LinkConnection> iterator = k.getMinimumSpanningTreeEdgeSet().iterator();

		while(iterator.hasNext()){
			iterator.next().isTree = true;
		}

		return k;
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
			Graph<OvsSwitch, LinkConnection> graph = null;
			if( (graph = (Graph<OvsSwitch, LinkConnection>)getServletContext().getAttribute(VPM_TOPOLOGY)) == null){ //first time execution
				//TODO check old topology with actual one
				FloodlightController controller = getController();

				if(controller != null){
					
					mxCodec codec = new mxCodec();	
					String xmlString =  mxXmlUtils.getXml(codec.encode(controller.getMxTopology().getModel()));

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

	/*private OvsSwitch getSwitchFromDpid(List<OvsSwitch> switches,String dpid){
		boolean found = false;
		OvsSwitch sw = null;

		int i=0;
		while((!found) && (i<switches.size())){
			if((sw=switches.get(i)).dpid.equals(dpid)){
				found = true;
			}else
				i++;
		}

		if(found)
			return sw;
		else
			return null;
	}*/

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

	private int linkPresent(LinkConnection l, List<LinkConnection> links){
		int index = -1;
		int i = 0;

		while((index==-1) && (i<links.size())){
			if(links.get(i).src.equals(l.src) && links.get(i).target.equals(l.target) ||
					links.get(i).src.equals(l.target) && links.get(i).target.equals(l.src))
				index = i;
			else
				i++;
		}

		return index;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = new PrintWriter(response.getOutputStream());
		response.setContentType("application/json");
		JSONObject jResponse = new JSONObject();

		try{
			//TODO do a comparison with the controller
			
			
			//we get user made topology
			mxGraph userGraph = new mxGraph();
			org.w3c.dom.Node node = mxXmlUtils.parseXml(request.getParameter("xml"));
			mxCodec decoder = new mxCodec(node.getOwnerDocument());
			decoder.decode(node.getFirstChild(),userGraph.getModel());

			mxCodec codec = new mxCodec();	
			System.out.println(mxUtils.getPrettyXml(codec.encode(userGraph.getModel())));
			
			
			//saving topology to file
			topologyToFile(userGraph);
			
			//converting
			ListenableUndirectedWeightedGraph<OvsSwitch, LinkConnection> jgraph =
					(ListenableUndirectedWeightedGraph<OvsSwitch, LinkConnection>) NetworkConverter.mxToJgraphT(userGraph);
			
			//creating tree
			KruskalMinimumSpanningTree<OvsSwitch, LinkConnection> tree = createTree(jgraph); //it sets isTree=true for tree edges
			
			FloodlightController controller = getController();

			//we get link connections
			List<LinkConnection> links = controller.getSwitchConnections(false);
			//we get switches
			//List<OvsSwitch> switches = controller.getSwitches();
			
			
			
			Iterator<LinkConnection> iterator = jgraph.edgeSet().iterator();

			

		
			while(iterator.hasNext()){
				LinkConnection l = iterator.next();
				
				
				int index = linkPresent(l, links);
				if(index != -1){ //we simply remove the link from the list: it will not be processed as it existed before and still exists now
					links.remove(index);

				}else if(l.isTree){//we phisically add the link to the switches
					
					//1. starting from source switch
					DefaultOvsdbClient client = new DefaultOvsdbClient(l.getSource().ip, BR_PORT);
					String ovs = null;

					OvsDBSet<Integer> trunks = new OvsDBSet<Integer>();
					trunks.add(VLAN_ID);

					ovs = client.getOvsdbNames()[0];
					OvsdbOptions opts = new OvsdbOptions();
					opts.put(OvsdbOptions.REMOTE_IP, l.getTarget().ip);
					client.addPort(ovs, BR_NAME, computePortName(l.getSource().dpid,l.getTarget().dpid), Interface.Type.gre.name(),0,trunks,opts);

					//2. now the other one
					client = new DefaultOvsdbClient(l.getTarget().ip, BR_PORT);
					opts = new OvsdbOptions();
					opts.put(OvsdbOptions.REMOTE_IP, l.getSource().ip);
					client.addPort(ovs, BR_NAME, computePortName(l.getTarget().dpid,l.getSource().dpid), Interface.Type.gre.name(),0,trunks,opts);
					
					
					System.out.println("Creating "+computePortName(l.src.dpid,l.target.dpid)+" on "+l.getSource());
					System.out.println("Creating "+computePortName(l.target.dpid,l.src.dpid)+" on "+l.getTarget());
					links.remove(l); //removing from links list
				}

			}

			if(links.size() > 0){ //user deleted some link
				for(LinkConnection l : links){
					System.out.println("To delete "+l);
					DefaultOvsdbClient client = new DefaultOvsdbClient(l.getSource().ip, BR_PORT);
					String ovs = client.getOvsdbNames()[0];
					//1.
					client.deletePort(ovs, BR_NAME,l.srcPort.name);

					//2.
					client = new DefaultOvsdbClient(l.getTarget().ip, BR_PORT);
					client.deletePort(ovs, BR_NAME,l.targetPort.name);

				}
			}
			
			getServletContext().setAttribute(VPM_TOPOLOGY, jgraph);
	
			jResponse.put("status", "ok");

		}catch(IOException | OvsdbException ex){
			jResponse.put("status", "error");
			jResponse.put("details", ex.getMessage());
			ex.printStackTrace();
		}

		out.println(jResponse.toString());
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	/*protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = new PrintWriter(response.getOutputStream());
		response.setContentType("application/json");
		JSONObject jResponse = new JSONObject();

		try{
			FloodlightController controller = getController();

			//we get link connections
			List<LinkConnection> links = controller.getSwitchConnections(false);
			//we get switches
			List<OvsSwitch> switches = controller.getSwitches();

			//we get user made topology
			mxGraph graph = new mxGraph();
			org.w3c.dom.Node node = mxXmlUtils.parseXml(request.getParameter("xml"));
			mxCodec decoder = new mxCodec(node.getOwnerDocument());
			decoder.decode(node.getFirstChild(),graph.getModel());

			ListenableUndirectedWeightedGraph<OvsSwitch, LinkConnection> jgraph =
					(ListenableUndirectedWeightedGraph<OvsSwitch, LinkConnection>) NetworkConverter.mxToJgraphT(graph);


			Iterator<LinkConnection> iterator = jgraph.edgeSet().iterator();

			//for(Object cell : graph.getChildCells(graph.getDefaultParent(),false,true)){//graph.getDefaultParent(), false, true)){ //getting edges
			//	Element element = (Element)(((mxCell)cell).getValue());

			while(iterator.hasNext()){
					LinkConnection l = iterator.next();

					int index = linkPresent(l, links);
					if(index != -1){ //we simply remove the link from the list: it will not be processed
						links.remove(index);

					}else{//we add the link phisically to the switches

						String srcPortName = computePortName(l.src, l.target);
						String dstPortName = computePortName(l.target, l.src);

						String srcIpAddr = getSwitchFromDpid(switches, l.src).ip;
						String dstIpAddr = getSwitchFromDpid(switches, l.target).ip;

						//1. starting from source switch
						DefaultOvsdbClient client = new DefaultOvsdbClient(srcIpAddr, BR_PORT);
						String ovs = null;

						OvsDBSet<Integer> trunks = new OvsDBSet<Integer>();
						trunks.add(VLAN_ID);

						ovs = client.getOvsdbNames()[0];
						OvsdbOptions opts = new OvsdbOptions();
						opts.put(OvsdbOptions.REMOTE_IP, dstIpAddr);
						client.addPort(ovs, BR_NAME, srcPortName, Interface.Type.gre.name(),0,trunks,opts);

						//2. now the other one
						client = new DefaultOvsdbClient(dstIpAddr, BR_PORT);
						opts = new OvsdbOptions();
						opts.put(OvsdbOptions.REMOTE_IP, srcIpAddr);
						client.addPort(ovs, BR_NAME, dstPortName, Interface.Type.gre.name(),0,trunks,opts);
					}

			}

			if(links.size() > 0){ //user deleted some link
				for(LinkConnection l : links){
					String srcIpAddr = getSwitchFromDpid(switches, l.src).ip;
					String dstIpAddr = getSwitchFromDpid(switches, l.target).ip;
					String srcPortName = computePortName(l.src, l.target);
					String dstPortName = computePortName(l.target, l.src);

					DefaultOvsdbClient client = new DefaultOvsdbClient(srcIpAddr, BR_PORT);
					String ovs = client.getOvsdbNames()[0];
					//1.
					client.deletePort(ovs, BR_NAME,srcPortName);

					//2.
					client = new DefaultOvsdbClient(dstIpAddr, BR_PORT);
					client.deletePort(ovs, BR_NAME,dstPortName);


				}
			}

			jResponse.put("status", "ok");

		}catch(IOException | OvsdbException ex){
			jResponse.put("status", "error");
			jResponse.put("details", ex.getMessage());
			ex.printStackTrace();
		}

		out.println(jResponse.toString());
		out.close();
	}*/

}
