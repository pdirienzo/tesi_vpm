package org.at.floodlight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.at.db.Controller;
import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.at.network.types.Port;
import org.at.network.types.VPMGraph;
import org.json.JSONArray;
import org.json.JSONObject;

public class FloodlightController {

	private String baseURL;
	
	public FloodlightController(Controller c){
		this.baseURL = "http://"+c.getHostAddress()
				+":"+c.getPort();	
	}
	
	private HashMap<String, OvsSwitch> getSwitches() throws IOException{
	
		HashMap<String, OvsSwitch> switches = new HashMap<String, OvsSwitch>();
		
		for(JSONObject o : RestRequest.getJSonArray(baseURL+"/wm/core/controller/switches/json")){
			OvsSwitch sw = new OvsSwitch(o.getString("dpid"), o.getString("inetAddress").substring(1).split(":")[0]);
			switches.put(sw.dpid, sw);
		}
		
		return switches;
	}
	
	public VPMGraph<OvsSwitch, LinkConnection> getTopology() throws IOException{
		VPMGraph<OvsSwitch, LinkConnection> jgraph = new VPMGraph<OvsSwitch,LinkConnection>(LinkConnection.class);
		HashMap<String,OvsSwitch> switches = getSwitches();
		for(String key : switches.keySet())
			jgraph.addVertex(switches.get(key));
		
		//now connections
		JSONArray result = RestRequest.getJSonArray(baseURL+"/vpm/topology/links/json");
		for(JSONObject o : result){
			jgraph.addLinkConnection(switches.get(o.getString("src-switch")),
					new Port(o.getString("src-port")),switches.get(o.getString("dst-switch")),
					new Port(o.getString("dst-port")),true); //if they are visible by the controller it means they are tree edges
		}
		return jgraph;
	}
	
	public int getPortNumber(OvsSwitch sw, String portName) throws IOException{
		JSONObject obj = new JSONObject();
		obj.put("switch-dpid", sw.dpid);
		obj.put("port-name", portName);
		int res = RestRequest.postJson(baseURL+"/vpm/topology/portInfo/json",obj).getInt("port-number");
		
		if(res < 0)
			throw new IOException("port "+portName+" not found on switch "+sw);
		
		return res;
	}
	
	public List<Port> getVnetPorts(OvsSwitch sw){
		JSONObject obj = new JSONObject();
		obj.put("switch-dpid", sw.dpid);
		obj.put("port-name", "vnetx");
		JSONObject res = RestRequest.postJson(baseURL+"/vpm/topology/portInfo/json",obj);
		List<Port> vnets = new ArrayList<Port>();
		for(JSONObject o : res.getJSONArray("result"))
			vnets.add(new Port(o.getString("port-name"),o.getInt("port-number")));
		
		return vnets;
	}
	
	public JSONObject getStaticFlows(String dpid) throws IOException{
		JSONObject json = null;

		HttpClient client = HttpClients.createDefault();
		HttpGet getRequest = new HttpGet(
				baseURL+"/wm/staticflowentrypusher/list/"+
						dpid+"/json");

		HttpResponse resp = client.execute(getRequest);
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				resp.getEntity().getContent()));
		String s = null;
		StringBuilder sb = new StringBuilder();
		while((s=rd.readLine())!= null)
			sb.append(s);

		json = new JSONObject(sb.toString());

		rd.close();
		
		return json;
	}
	
	public JSONObject getFlows(String dpid) throws IOException{
		JSONObject json = null;

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

		json = new JSONObject(sb.toString());

		rd.close();
		
		return json;
	}

	public JSONObject addStaticFlow(JSONObject data) throws IOException{
		JSONObject result = null;

		HttpClient client = HttpClients.createDefault();
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

		rd.close();

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
	
	public JSONObject deleteFlow(String dpid,String flowName)throws IOException{
		JSONObject result = null;
		
		JSONObject data = new JSONObject()
		.put("switch", dpid)
		.put("name", flowName);
		
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

		rd.close();

		return result;
	}
	
	
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
				new Controller("192.168.1.180", 8080));
		
		/*for(LinkConnection ovs : f.getSwitchConnections(true))
			System.out.println(ovs);*/
		
		System.out.println(f.getPortNumber(new OvsSwitch("00:00:16:d1:61:6a:85:49","192.168.1.180"), "col0"));
		/*JSONObject data = new JSONObject()
		 //.put("name", "flow-mod-vm1-swl")
		.put("switch", "00:00:16:d1:61:6a:85:49")
		//.put("cookie", "5")
		//.put("priority", "20")
		//.put("idle_timeout","5")
		//.put("vlan-id", "1")
		//.put("ingress-port", "3")
		//.put("ether-type", "0x0800")
		.put("active", "true")
		.put("dst-port", "6633")
		.put("actions", "output=1");
		
		System.out.println(f.addFlow(data));
		f.deleteAllFlows("00:00:00:0c:29:4a:ba:96");
		
		//JSONObject obj = f.getFlows("00:00:00:24:be:c1:a9:5c");
		//JSONObject obj = f.deleteFlow("00:00:00:24:be:c1:a9:5c","pleaseWork_out");
		//System.out.println(obj);*/

	}

}
