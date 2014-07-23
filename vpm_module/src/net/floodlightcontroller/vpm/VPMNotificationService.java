package net.floodlightcontroller.vpm;


import java.util.HashSet;
import java.util.Set;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class VPMNotificationService extends ServerResource{
	
	private static Set<String> listeners = new HashSet<String>();
	
	public synchronized static void notifyEvent(){
		
	}
	
	@Post
	public synchronized String registerListener(String callbackURI){
		listeners.add("http://"+getClientInfo().getAddress()+callbackURI);
		for(String s :  listeners)
			System.out.println(s);
		return "{\"status\":\"success\"}";
	}
	

}
