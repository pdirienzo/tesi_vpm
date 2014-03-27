package org.opendaylight.ovsdb.lib.types;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InsertPort{
	public String name;
	@JsonProperty(required=false)
	public List<String> interfaces;
	
	public InsertPort(String name){
		this.name = name;
	}
	
	public InsertPort(String name,String uuid){
		this.name = name;
		interfaces = new ArrayList<String>();
		interfaces.add("uuid");
		interfaces.add(uuid);
	}
}
