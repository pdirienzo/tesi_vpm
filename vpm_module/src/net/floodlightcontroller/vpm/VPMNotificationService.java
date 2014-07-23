package net.floodlightcontroller.vpm;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class VPMNotificationService extends ServerResource{
	
	private static Set<String> listeners = new HashSet<String>();
	
	private static void post(String url, String data) throws IOException {
		/* POST Method */
		HttpURLConnection conn = (HttpURLConnection)((new URL(url)).openConnection());
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
		dos.writeBytes("data="+data);
		dos.flush();
		dos.close();
		System.out.println(url+"-->"+conn.getResponseCode());	
	}
	
	public synchronized static void notifyEvent(String data){
		List<String> toBeRemoved = new ArrayList<String>();
		
		for(String url : listeners){
			try{
				post(url,data);
			}catch(IOException ex){
				System.err.println("Unable to send notification to "+url+"\nIt will be removed");
				toBeRemoved.add(url);
			}
		}
		
		for(String url : toBeRemoved)
			listeners.remove(url);
	}
	
	@Post
	public synchronized String registerListener(String callbackURI){
		listeners.add("http://"+getClientInfo().getAddress()+callbackURI);
		
		return "{\"status\":\"success\"}";
	}
	

}
