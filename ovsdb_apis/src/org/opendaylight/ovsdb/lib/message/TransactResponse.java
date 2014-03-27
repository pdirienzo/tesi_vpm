/*
 * Copyright (C) 2013 EBay Software Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Authors : Ashwin Raveendran, Madhu Venugopal
 */
package org.opendaylight.ovsdb.lib.message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opendaylight.ovsdb.lib.message.operations.OperationResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TransactResponse extends OvsdbResponse {
    ArrayList<OperationResult> result;

    public ArrayList<OperationResult> getResult() {
        return result;
    }

    public void setResult(ArrayList<OperationResult> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "TransactResponse [result=" + result + "]";
    }
    
    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException{
    	String plain = "{\"id\":\"4135\",\"result\":[{\"rows\":[{\"name\":\"br0\",\"_uuid\":[\"uuid\",\"81ec05f9-6ce2-421d-8491-9fe37cb3aced\"]}"
    			+",{\"name\":\"cool0\",\"_uuid\":[\"uuid\",\"56ab3f42-8b1c-4939-a338-7ff9b2613960\"]},{\"name\":\"cool1\",\"_uuid\":"
    			+"[\"uuid\",\"b0c59ca1-3830-46df-abbe-bdb803cb5bfd\"]},{\"name\":\"prova0\",\"_uuid\":[\"uuid\","
    			+"\"c96ffa6c-a5ee-4575-bc1c-a10f82c8cd45\"]}]}],\"error\":null}";
    	
    	TypeReference<SelectResponse> typeRef = new TypeReference<SelectResponse>() {
		};
		
		ObjectMapper mapper = new ObjectMapper();
		SelectResponse r = mapper.readValue(plain, SelectResponse.class); 
		System.out.println(r.getResult().get(0).getRows().get(1).get("_uuid"));
		//System.out.println(o.get("hi"));
    }
}
