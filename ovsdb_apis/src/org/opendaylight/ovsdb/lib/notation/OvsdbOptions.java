package org.opendaylight.ovsdb.lib.notation;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.opendaylight.ovsdb.lib.notation.json.OvsdbOptionsSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = OvsdbOptionsSerializer.class)
public class OvsdbOptions extends Properties{

	public static final String REMOTE_IP = "remote_ip";
	public static final String LOCAL_IP = "local_ip";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public List<Object> getAsJsonArray(String key){
    	List<Object> o = new ArrayList<Object>();
    	o.add(key);
    	o.add(super.getProperty(key));
    	return o;
    }
}
