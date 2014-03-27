package org.opendaylight.ovsdb.lib.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Error {
	public String error;
	
	@JsonProperty(required=false)
	public String details;
	
	public Error(String error){
		this.error = error;
	}
	
	public Error(String error,String details){
		this.error = error;
		this.details = details;
	}
	
}
