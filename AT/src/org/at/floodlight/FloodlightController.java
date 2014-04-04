package org.at.floodlight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.at.db.Controller;
import org.at.floodlight.types.LinkConnection;
import org.at.floodlight.types.OvsSwitch;
import org.json.JSONArray;
import org.json.JSONObject;

public class FloodlightController {

	private String baseURL;

	public FloodlightController(Controller c){
		this.baseURL = "http://"+c.getHostAddress()
				+":"+c.getPort();
	}
	
	public LinkConnection[] getSwitchConnections() throws IOException{
		JSONArray result = RestRequest.getJSonArray(baseURL+"/wm/topology/links/json");
		LinkConnection[] links = new LinkConnection[result.length()];
		for(int i=0;i<links.length;i++){
			JSONObject o = result.getJSONObject(i);
			links[i] = new LinkConnection(o.getString("src-switch"), o.getString("dst-switch"),o.getInt("src-port"), o.getInt("dst-port"));
		}
		
		return links;
	}
	
	public OvsSwitch[] getSwitches() throws IOException{
		JSONArray result = RestRequest.getJSonArray(baseURL+"/wm/core/controller/switches/json");
		
		OvsSwitch[] switches = new OvsSwitch[result.length()];
		
		for(int i=0;i<switches.length;i++){
			JSONObject o = result.getJSONObject(i);
			String inet = (o.getString("inetAddress").substring(1)).split(":")[0];
			switches[i] = new OvsSwitch(o.getString("dpid"), inet);
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
				new Controller("127.0.0.1", 8080));
		
		for(LinkConnection ovs : f.getSwitchConnections())
			System.out.println(ovs);
		 /*JSONObject data = new JSONObject()
		 .put("name", "flow-mod-vm1-swl")
		.put("switch", "00:00:00:24:be:c1:a9:5c")
		.put("cookie", "5")
		.put("priority", "200")
		.put("idle_timeout","5")
		.put("vlan-id", "1")
		.put("ingress-port", "3")
		.put("ether-type", "0x0800")
		.put("active", "true")
		.put("dst-ip", "192.168.1.1")
		.put("actions", "output=1");
		
		System.out.println(f.addFlow(data));*/
		//f.deleteAllFlows("00:00:00:24:be:c1:a9:5c");
		
		//JSONObject obj = f.getFlows("00:00:00:24:be:c1:a9:5c");
		//JSONObject obj = f.deleteFlow("00:00:00:24:be:c1:a9:5c","pleaseWork_out");
		//System.out.println(obj);

	}

}
