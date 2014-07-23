package net.floodlightcontroller.vpm;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.floodlightcontroller.vpm.json.VPMEventForwarding;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VPMNotificationService extends ServerResource{
	
	private class VPMListener{
		String callback;
		String vnet_interface;
		
		public VPMListener(String callback, String vnet){
			this.callback=callback;
			this.vnet_interface=vnet;
		}
		
		public boolean equals(Object o){
			return ((VPMListener) o).callback.equals(this.callback);
		}
	}
	
	private static ObjectMapper mapper = new ObjectMapper();
	private static Set<VPMListener> listeners = new HashSet<VPMListener>();
	
	public VPMNotificationService(){
		super();
		mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET,false);
	}
	
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
		List<VPMListener> toBeRemoved = new ArrayList<VPMListener>();
		
		for(VPMListener listener : listeners){
			try{
				post(listener.callback,data);
			}catch(IOException ex){
				System.err.println("Unable to send notification to "+listener.callback+"\nIt will be removed");
				toBeRemoved.add(listener);
			}
		}
		
		for(VPMListener ls : toBeRemoved)
			listeners.remove(ls);
	}
	
	public synchronized static void notifyEventForwarding(VPMEventForwarding vpmev) throws JsonProcessingException{	
		List<VPMListener> toBeRemoved = new ArrayList<VPMListener>();
		System.out.println("EVENT FORWARDING: "+mapper.writeValueAsString(vpmev));
		for(VPMListener listener : listeners){
			try{
				
				if (vpmev.vnet.startsWith(listener.vnet_interface)){
					post(listener.callback,mapper.writeValueAsString(vpmev));
				}
			}catch(IOException ex){
				System.err.println("Unable to send notification to "+listener.callback+"\nIt will be removed");
				toBeRemoved.add(listener);
			}
		}
		
		for(VPMListener ls : toBeRemoved)
			listeners.remove(ls);
	}
	
	@Post
	public synchronized String registerListener(String json){
		
		Map<String,String> mp=null;
		try {
			mp=jsonToStorageEntry(json);
			VPMListener ls = new VPMListener("http://"+getClientInfo().getAddress()+mp.get("callback"), 
					mp.get("vnet-prefix"));
			if (mp.get("op").equals("SUBSCRIBE")){
				listeners.add(ls);
			}else if (mp.get("op").equals("UNSUBSCRIBE")){
				listeners.remove(ls);
			}
			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(mp.get("op")+" status:\"success\"");
		return "{\"op\":\""+mp.get("op")+"\",\"status\":\"success\"}";
	}
	
	public static Map<String, String> jsonToStorageEntry(String fmJson) throws IOException {
        Map<String, String> entry = new HashMap<String, String>();
        MappingJsonFactory f = new MappingJsonFactory();
        JsonParser jp;
        
        try {
            jp = f.createJsonParser(fmJson);
        } catch (JsonParseException e) {
            throw new IOException(e);
        }
        
        jp.nextToken();
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("Expected START_OBJECT");
        }
        
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            if (jp.getCurrentToken() != JsonToken.FIELD_NAME) {
                throw new IOException("Expected FIELD_NAME");
            }
            
            String n = jp.getCurrentName();
            jp.nextToken();
            if (jp.getText().equals("")) 
                continue;
            
            if (n == "op")
                entry.put("op", jp.getText());
            else if (n == "callback")
                entry.put("callback", jp.getText());
            else if (n == "vnet-prefix")
            	entry.put("vnet-prefix", jp.getText());
        }
        
        return entry;
    }

}
