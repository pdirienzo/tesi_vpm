package org.at.floodlight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.at.db.Controller;
import org.at.network.NetworkConverter;
import org.at.network.types.LinkConnection;
import org.at.network.types.Port;
import org.at.network.types.OvsSwitch;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mxgraph.view.mxGraph;

public class FloodlightController {

	private String baseURL;
	
	public FloodlightController(Controller c){
		this.baseURL = "http://"+c.getHostAddress()
				+":"+c.getPort();
		
	}
	
	private List<LinkConnection> deleteOpposites(List<LinkConnection> original){
		List<LinkConnection> result = new ArrayList<LinkConnection>();

		while(original.size() > 0){
			LinkConnection l = original.remove(0); //taking first element
			for(int i=0;i<original.size();i++){
				if(original.get(i).oppositeLink(l))
					original.remove(i);
			}
			result.add(l);
		}

		return result;
	}
	
	private boolean isOpposite(LinkConnection l, List<LinkConnection> connections){
		boolean opposite = false;
		int i = 0;
		
		while((!opposite) && (i<connections.size())){
			if(connections.get(i).oppositeLink(l))
				opposite = true;
			else
				i++;
		}
		
		return opposite;
	}
	
	public ListenableUndirectedWeightedGraph<OvsSwitch, LinkConnection> getJgraphTopology() throws IOException{
		return NetworkConverter.getJgraphTopology(getSwitches(), getSwitchConnections(false));
	}
	
	public mxGraph getMxTopology() throws IOException{
		return NetworkConverter.getMxTopology(getSwitches(), getSwitchConnections(false));
	}
	
	public List<LinkConnection> getSwitchConnections(boolean undirected) throws IOException{
		JSONArray result = RestRequest.getJSonArray(baseURL+"/vpm/topology/links/json");
		List<LinkConnection> links = new ArrayList<LinkConnection>(result.length());
		for(int i=0;i<result.length();i++){
			JSONObject o = result.getJSONObject(i);
			LinkConnection l = new LinkConnection(
					o.getString("src-switch"),o.getString("src-inet"), 
					o.getString("dst-switch"),o.getString("dst-inet"),
					new Port(o.getString("src-port")), 
					new Port(o.getString("dst-port")) );
			
			if(undirected){
				if(!isOpposite(l, links))
					links.add(l);
				
			}else
				links.add(l);
		}
		
		return links;
	}
	
	public List<OvsSwitch> getSwitches() throws IOException{
		JSONArray result = RestRequest.getJSonArray(baseURL+"/wm/core/controller/switches/json");
		
		List<OvsSwitch> switches = new ArrayList<OvsSwitch>(result.length());
		
		for(int i=0;i<result.length();i++){
			JSONObject o = result.getJSONObject(i);
			String inet = (o.getString("inetAddress").substring(1)).split(":")[0];
			switches.add(new OvsSwitch(o.getString("dpid"), inet));
		}
		
		return switches;
	}
	
	//TODO

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

	public JSONObject addFlow(JSONObject data) throws IOException{
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
		 
		JSONObject data = new JSONObject()
		 .put("name", "flow-mod-vm1-swl")
		.put("switch", "00:00:00:0c:29:4a:ba:96")
		//.put("cookie", "5")
		.put("priority", "20")
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
		//System.out.println(obj);

	}

}
