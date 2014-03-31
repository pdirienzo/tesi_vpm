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

import org.opendaylight.ovsdb.lib.message.operations.SelectOperationResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SelectResponse extends OvsdbResponse {
    ArrayList<SelectOperationResult> result;

    public ArrayList<SelectOperationResult> getResult() {
        return result;
    }

    public void setResult(ArrayList<SelectOperationResult> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "TransactResponse [result=" + result + "]";
    }
    
    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException{
    	String response = "{\"id\":\"9596\",\"result\":[{\"rows\":[{\"_uuid\":[\"uuid\",\"744188ba-8b69-4c84-86d3-d220e1621bfc\"],\"name\":\"br0\",\"ports\":[\"set\",[[\"uuid\",\"6e827146-336a-4279-b115-02dc902a93b9\"],[\"uuid\","
    			+ "\"a82113ad-18b4-4f95-8dba-89de47f24078\"]]]}]}],\"error\":null}";
    	
    	ObjectMapper mapper = new ObjectMapper();
    	
    	mapper.readValue(response, SelectResponse.class);
    }
}
