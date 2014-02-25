package org.at.floodlight;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.at.db.Controller;
import org.at.db.Database;
import org.json.JSONArray;
import org.json.JSONObject;

public class FlowsController {
	private String baseurl;
	private String serviceurl;
	
	private static final AtomicInteger flowId = new AtomicInteger(0);
	
	public FlowsController()  {
		/*Database d = new Database();
		d.connect();
		Controller c = d.getController();
		d.close();
		baseurl = "http://" + c.getHostAddress() + ":" + String.valueOf(
				c.getPort()); 
		serviceurl = "/wm/staticflowentrypusher";*/
	}

	public String getFlowTable(String switchId) {
		if(switchId==null)
			switchId = "all";
		final String url = baseurl + serviceurl + "/list/"+switchId+"/json";
		return RestRequest.get(url);		
	}
	
	public String addFlow(String switchId, String name, String dstmac, String action) {
		final String url = baseurl + serviceurl + "/json";
		final JSONObject data = new JSONObject()
			.put("switch", switchId)
			.put("name", name)
			.put("cookie", "0")
			.put("priority", "32767")
			.put("active", "true")
			.put("dst-mac", dstmac)
			.put("actions", action);
		return RestRequest.post(url, data.toString());
	}
	
	public String removeFlow(String switchId, String name, String dstmac, String action) {
		final String url = baseurl + serviceurl + "/json";
		final JSONObject data = new JSONObject()
			.put("switch", switchId)
			.put("name", name)
			.put("cookie", "0")
			.put("priority", "32767")
			.put("active", "true")
			.put("dst-mac", dstmac)
			.put("actions", action);
		return RestRequest.delete(url, data.toString());
	}
	
	public String clearFlows(String switchId) {
		if(switchId==null)
			switchId = "all";
		String url = baseurl + serviceurl + "/clear/" + switchId + "/json";
		return RestRequest.get(url);
	}
	
	public String queryPortByMac(String switchId, String mac){
		String url = baseurl + "/wm/core/controller/switches/json";
		String response = RestRequest.get(url);
		
		JSONArray switches = new JSONArray(response);
		for(int i=0; i<switches.length(); i++)
			if(switches.getJSONObject(i).getString("dpid").equalsIgnoreCase(switchId.toLowerCase())) {
				JSONArray ports = switches.getJSONObject(i).getJSONArray("ports");
				for(int j=0; j<ports.length(); j++) {
					String hwmac = ports.getJSONObject(j).getString("hardwareAddress");
					if(hwmac.substring(2).equalsIgnoreCase(mac.substring(2))) {
						String port = String.valueOf(ports.getJSONObject(j).getInt("portNumber"));
						return port;
					}
				}
			}

		return null;
	}
	
	public String queryPortByName(String switchId, String name){
		String url = baseurl + "/wm/core/controller/switches/json";
		String response = RestRequest.get(url);
		
		JSONArray switches = new JSONArray(response);
		for(int i=0; i<switches.length(); i++)
			if(switches.getJSONObject(i).getString("dpid").equalsIgnoreCase(switchId.toLowerCase())) {
				JSONArray ports = switches.getJSONObject(i).getJSONArray("ports");
				for(int j=0; j<ports.length(); j++) {
					String cname = ports.getJSONObject(j).getString("name");
					if(cname.equals(name)) {
						String port = String.valueOf(ports.getJSONObject(j).getInt("portNumber"));
						return port;
					}
				}
			}

		return null;
	}
	
	public String querySwitchIdByIp(String ip) {
		String url = baseurl + "/wm/core/controller/switches/json";
		String response = RestRequest.get(url);
		
		JSONArray switches = new JSONArray(response);
		for(int i=0; i<switches.length(); i++)
			if(switches.getJSONObject(i).getString("inetAddress").contains(ip)) {
				return switches.getJSONObject(i).getString("dpid");
			}

		return null;
	}
	
	public static void main(String argv[]) throws IOException{
		FlowsController fl = new FlowsController();
		
		fl.queryPortByMac("00:00:66:3d:61:08:af:44", "52:54:00:B5:EA:F6");
		
		fl.clearFlows(null);
		
		String port = fl.queryPortByMac("00:00:66:3d:61:08:af:44", "52:54:00:B5:EA:F6");
		System.out.println("Port is "+ port);
		
		String portSrc = fl.queryPortByMac("00:00:66:3d:61:08:af:44", "52:54:00:B5:EA:F6");
		String portDst = fl.queryPortByMac("00:00:9A:10:4E:32:56:4E", "52:54:00:B5:EA:F6");
		
		
		//String resp = fl.getFlowTable("00:00:66:3d:61:08:af:44");
		System.out.println("Destination port is "+ portDst);
		
		String resp;
		
		resp = fl.addFlow("00:00:66:3d:61:08:af:44", "flow-1", "52:54:00:B5:EA:F6", 
				"output=1,output="+portSrc);
		
		resp = fl.addFlow("00:00:9A:10:4E:32:56:4E", "flow-2", "52:54:00:B5:EA:F6", 
				"output=1,output="+portDst);
		
		//System.out.println(resp);
		
		resp = fl.getFlowTable("00:00:66:3d:61:08:af:44");
		System.out.println(resp);

	}
	
}