package org.opendaylight.ovsdb.lib.types;

import java.util.Random;

public abstract class Request {
	public String method;
	public String id;
	
	public Request(String method){
		this.method = method;
		this.id = Integer.toString((new Random()).nextInt(10000));
	}
}
