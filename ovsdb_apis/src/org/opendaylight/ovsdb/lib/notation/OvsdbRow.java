package org.opendaylight.ovsdb.lib.notation;

import java.util.HashMap;

import org.opendaylight.ovsdb.lib.notation.json.Converter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(converter = Converter.OvsdbRowConverter.class)
public class OvsdbRow extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unchecked")
	public <T>  OvsDBSet<T> getAsSet(String key){
		return (OvsDBSet<T>)super.get(key);
	}
	
	public UUID getAsUUID(String key){
		return (UUID)super.get(key);
	}
	
	public String getAsString(String key){
		return (String)super.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getColumn(String key){
		return (T)super.get(key);
	}

}
