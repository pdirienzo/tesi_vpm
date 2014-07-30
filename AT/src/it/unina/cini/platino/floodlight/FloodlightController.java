package it.unina.cini.platino.floodlight;

import it.unina.cini.platino.connections.RestRequest;
import it.unina.cini.platino.db.Controller;
import it.unina.cini.platino.db.Database;
import it.unina.cini.platino.network.types.LinkConnection;
import it.unina.cini.platino.network.types.OvsSwitch;
import it.unina.cini.platino.network.types.VPMGraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A proxy class which grants interaction with an existing Floodlight controller.
 * Actual communication is performed through REST APIs.
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 */
public class FloodlightController {

	private String baseURL;
	
	public FloodlightController(Controller c){
		this.baseURL = "http://"+c.getHostAddress()
				+":"+c.getPort();	
	}
	
	/**
	 * Gets an instance of the floodlight controller from the data saved into the db.
	 * 
	 * @throws IOException if no controller is defined or specified one is not reachable
	 * @return an instance of floodlight controller
	 */
	public static FloodlightController getDbController() throws IOException{
		Database d = new Database();
		d.connect();
		Controller c = d.getController();
		d.close();

		if(c==null)
			throw new IOException("No floodlight controller defined");

		FloodlightController controller = new FloodlightController(c);

		return controller;
	}
	
	/**
	 * 
	 * @return a list of switches currently attached to this controller
	 * @throws IOException
	 */
	private HashMap<String, OvsSwitch> getSwitches() throws IOException{
	
		HashMap<String, OvsSwitch> switches = new HashMap<String, OvsSwitch>();
		
		for(JSONObject o : RestRequest.getJSonArray(baseURL+"/wm/core/controller/switches/json")){
			OvsSwitch sw = new OvsSwitch(o.getString("dpid"), o.getString("inetAddress").substring(1).split(":")[0]);
			switches.put(sw.dpid, sw);
		}
		
		return switches;
	}
	
	/**
	 * Registers a new listener for the notify service. 
	 * Listener has to be an http servlet or any entity (cgi, ...) supporting POST requests, 
	 * the notify system will send a POST to specified URI containing any updates
	 * 
	 * @param callbackURI the servlet's callback URI (where the POST request will be sent)
	 * @param vnetPrefix prefix of the VM interface attached to the OVS 
	 * @throws IOException
	 */
	public void registerListener(String callbackURI, String vnetPrefix) throws IOException{
		JSONObject data = new JSONObject();
		data.put("op", "SUBSCRIBE")
		.put("callback", callbackURI)
		.put("vnet-prefix", vnetPrefix);
		System.out.println(data.toString());
		RestRequest.postJson(baseURL+"/vpm/topology/listener/register", data);	
	}
	
	/**
	 * unregisters a previously registered listener
	 * @param callbackURI
	 * @throws IOException
	 */
	public void removeListener(String callbackURI) throws IOException{
		JSONObject data = new JSONObject();
		data.put("op", "UNSUBSCRIBE")
		.put("callback", callbackURI);
		RestRequest.postJson(baseURL+"/vpm/topology/listener/register", data);	
	}
	
	/**
	 * 
	 * @return this floodlight's controller base URL
	 */
	public String getUrl(){
		return baseURL;
	}
	
	/**
	 * Obtains the topology this floodlight controller can see.
	 * It consists of every switch currently attached to the controller along with any
	 * links
	 * 
	 * @return a VPMGraph object representing this network
	 * @throws IOException
	 */
	public VPMGraph<OvsSwitch, LinkConnection> getTopology() throws IOException{
		VPMGraph<OvsSwitch, LinkConnection> jgraph = new VPMGraph<OvsSwitch,LinkConnection>(LinkConnection.class);
		HashMap<String,OvsSwitch> switches = getSwitches();
		for(String key : switches.keySet())
			jgraph.addVertex(switches.get(key));
		
		//now connections
		JSONArray result = RestRequest.getJSonArray(baseURL+"/vpm/topology/links/json");
		for(JSONObject o : result){
			jgraph.addLinkConnection(switches.get(o.getString("src-switch")),
					new FloodlightPort(o.getString("src-port")),switches.get(o.getString("dst-switch")),
					new FloodlightPort(o.getString("dst-port")),true); //if they are visible by the controller it means they are tree edges
		}
		return jgraph;
	}

	/**
	 * Given a port name (es. eth0) returns the port number assigned by floodlight
	 * 
	 * @param sw switch containing specified port
	 * @param portName the port name
	 * @return the port number associated with that port name
	 * @throws IOException
	 */
	public int getPortNumber(OvsSwitch sw, String portName) throws IOException{
		JSONObject obj = new JSONObject();
		obj.put("switch-dpid", sw.dpid);
		obj.put("port-name", portName);
		int res = RestRequest.postJson(baseURL+"/vpm/topology/portInfo/json",obj).getInt("port-number");
		
		if(res < 0)
			throw new IOException("port "+portName+" not found on switch "+sw);
		
		return res;
	}
	
	/**
	 * Returns a list of ports associated with VMs
	 * @param sw
	 * @param prefix
	 * @return
	 * @throws IOException
	 */
	public List<FloodlightPort> getVMPorts(OvsSwitch sw, String prefix) throws IOException{
		JSONObject obj = new JSONObject();
		obj.put("switch-dpid", sw.dpid);
		obj.put("port-name", "vnetx");
		obj.put("port-prefix", prefix);
		JSONObject res = RestRequest.postJson(baseURL+"/vpm/topology/portInfo/json",obj);
		List<FloodlightPort> vnets = new ArrayList<FloodlightPort>();
		for(JSONObject o : res.getJSONArray("result"))
			vnets.add(new FloodlightPort(o.getString("port-name"),o.getInt("port-number")));
		
		return vnets;
	}
	
	public JSONArray getStaticFlows(String dpid) throws IOException{
		JSONArray flows = new JSONArray();
	
		JSONObject notFormatted = (RestRequest.getJson(baseURL+"/wm/staticflowentrypusher/list/"+
						dpid+"/json")).getJSONObject(dpid);
		
		
		for(Object flowName : notFormatted.keySet()){
			JSONObject rawFlow = notFormatted.getJSONObject((String)flowName);
			/*
			JSONObject flow = new JSONObject();
			flow.put("name", flowName);
			flow.put("match",rawFlow.get("match"));
			flow.put("actions", rawFlow.get("actions"));
			//flow.put("match", notFormatted.get)*/
			rawFlow.put("name", flowName);
			
			if(rawFlow.get("actions") != JSONObject.NULL){
				JSONArray actions = rawFlow.getJSONArray("actions");
				for(JSONObject jos : actions)
					if(jos.getString("type").equals("SET_NW_DST")){
						jos.put("networkAddress", IntToInet(jos.getInt("networkAddress")));
					}
			}
			
			flows.put(rawFlow);
		}
		return flows;
	}
	
	private String IntToInet(int bytes) throws IOException{
		byte[] unpack = new byte[] {
			    (byte)((bytes >>> 24) & 0xff),
			    (byte)((bytes >>> 16) & 0xff),
			    (byte)((bytes >>>  8) & 0xff),
			    (byte)((bytes       ) & 0xff)
			  };
		
		return InetAddress.getByAddress(unpack).getHostAddress();
	}
	
	/*
	public JSONArray getFlows(String dpid) throws IOException{

		HttpClient client = HttpClients.createDefault();
		HttpGet getRequest = new HttpGet(
				baseURL+"/wm/core/switch/"+
						dpid+"/flow/json");

		HttpResponse resp = client.execute(getRequest);
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				resp.getEntity().getContent()));
		String s = null;
		StringBuilder sb = new StringBuilder();
		while((s=rd.readLine())!= null)
			sb.append(s);

		rd.close();
		
		JSONArray flows = (new JSONObject(sb.toString())).getJSONArray(dpid);
		
		return flows;
	}*/

	/**
	 * Adds a static flow to this floodlight controller
	 * 
	 * @param data a JSON object representing the data
	 * @return
	 * @throws IOException
	 */
	public JSONObject addStaticFlow(JSONObject data) throws IOException{
		JSONObject result = null;

		/*HttpClient client = HttpClients.createDefault();
		HttpPost postRequest = new HttpPost(baseURL+
				"/wm/staticflowentrypusher/json");
		postRequest.setHeader("Content-type", "application/json");
		postRequest.setEntity(new StringEntity(data.toString()));
		HttpResponse resp = client.execute(postRequest);

		BufferedReader rd = new BufferedReader(new InputStreamReader(
				resp.getEntity().getContent()));
		
		String s = null;
		StringBuilder sb = new StringBuilder();
		while((s=rd.readLine())!= null)
			sb.append(s);

		result= new JSONObject(sb.toString());

		rd.close();*/
		result = RestRequest.postJson(baseURL+"/wm/staticflowentrypusher/json", data);

		return result;
	}
	
	/**
	 * This is a workaround: apache's HttpDelete class does not support a body, which
	 * is required for floodlight rest api to work. So, as HttpDelete is a simple HttpPost,
	 * we simply create our own class to do this.
	 * @author pasquale
	 *
	 */
	private class HttpDeleteWithBody extends HttpPost{
		public HttpDeleteWithBody(String url){
			super(url);
		}
		
		@Override
		public String getMethod(){
			return "DELETE";
		}
	}
	
	/**
	 * Deletes a flow from this floodlight controller
	 * 
	 * @param dpid
	 * @param flowName
	 * @return
	 * @throws IOException
	 */
	public JSONObject deleteFlow(String dpid,String flowName)throws IOException{
		JSONObject result = null;
		
		JSONObject data = new JSONObject()
		.put("switch", dpid)
		.put("name", flowName);
		
		/*
		HttpClient client = HttpClients.createDefault();
		HttpDeleteWithBody delRequest = new HttpDeleteWithBody(baseURL+
				"/wm/staticflowentrypusher/json");
		delRequest.setHeader("Content-type", "application/json");
		delRequest.setEntity(new StringEntity(data.toString()));
		HttpResponse resp = client.execute(delRequest);

		BufferedReader rd = new BufferedReader(new InputStreamReader(
				resp.getEntity().getContent()));
		
		String s = null;
		StringBuilder sb = new StringBuilder();
		while((s=rd.readLine())!= null)
			sb.append(s);

		result= new JSONObject(sb.toString());

		rd.close();*/
		result = RestRequest.deleteJson(baseURL+ "/wm/staticflowentrypusher/json", data);

		return result;
	}
	
	/**
	 * Resets all application related flows
	 * @throws IOException
	 */
	public void resetAllFlowsForAllSwitches() throws IOException{
		HttpClient client = HttpClients.createDefault();
		HttpGet getRequest = new HttpGet(
				baseURL+"/vpm/topology/forwarding/reset");
		client.execute(getRequest);
	}
	
	public void deleteAllFlows(String dpid) throws IOException{
		HttpClient client = HttpClients.createDefault();
		HttpGet getRequest = new HttpGet(
				baseURL+"/wm/staticflowentrypusher/clear/"+
						dpid+"/json");
		client.execute(getRequest);
	}

	
	public static void main(String[] args) throws IOException{
		
		FloodlightController f = new FloodlightController(
				new Controller("192.168.1.181", 8080));
		
		JSONArray flows = f.getStaticFlows("00:00:72:5b:2d:c5:15:46");
		System.out.println(((JSONObject)flows.get(0)).get("match"));
		
		/*JSONObject jos = f.getStaticFlows("00:00:72:5b:2d:c5:15:46");
		for(Object s : jos.keySet()){
			
			JSONObject flow = jos.getJSONObject((String)s);
			if(flow.get("actions") != JSONObject.NULL)
				System.out.println(s+"-->"+flow.get("actions"));
			
		}*/
		//System.out.println(jos.get("actions"));
		/*for(LinkConnection ovs : f.getSwitchConnections(true))
			System.out.println(ovs);*/
		
		//System.out.println(f.getPortNumber(new OvsSwitch("00:00:16:d1:61:6a:85:49","192.168.1.180"), "col0"));
		JSONObject data = new JSONObject()
		 .put("name", "flow-test-abc-swf")
		.put("switch", "00:00:72:5b:2d:c5:15:46")
		.put("cookie", "5")
		//.put("priority", "20")
		//.put("idle_timeout","5")
		.put("vlan-id", "2")
		//.put("ingress-port", "3")
		//.put("ether-type", "0x0800")
		.put("active", "true")
		.put("actions", "output=3")
		.put("dst-port", "6633");
		
		//System.out.println(f.addStaticFlow(data));
		//f.deleteAllFlows("00:00:00:0c:29:4a:ba:96");
		
		//JSONObject obj = f.getFlows("00:00:00:24:be:c1:a9:5c");
		//JSONObject obj = f.deleteFlow("00:00:00:24:be:c1:a9:5c","pleaseWork_out");
		//System.out.println(obj);*/

	}

}
