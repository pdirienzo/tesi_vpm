package net.floodlightcontroller.vpm;


import java.util.HashSet;
import java.util.Set;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class VPMRegistration extends ServerResource{
	
	private static Set<String> listeners = new HashSet<String>();
	
	@Override
	public void init(Context arg0, Request arg1, Response arg2) {
		// TODO Auto-generated method stub
		super.init(arg0, arg1, arg2);
		
	}
	
	@Post
	public String registerListener(String callbackURI){
	System.out.println(getClientInfo().getAddress());
	return callbackURI;
	
	}
	

}
