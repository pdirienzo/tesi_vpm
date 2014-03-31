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
import org.opendaylight.ovsdb.lib.notation.OvsDBSet;
import org.opendaylight.ovsdb.lib.notation.OvsdbRow;
import org.opendaylight.ovsdb.lib.notation.UUID;

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
    	/*String plain = \"{\\"id\\":\\"4135\\",\\"result\\":[{\\"rows\\":[{\\"name\\":\\"br0\\",\\"_uuid\\":[\\"uuid\\",\\"81ec05f9-6ce2-421d-8491-9fe37cb3aced\\"]}\"
    			+\",{\\"name\\":\\"cool0\\",\\"_uuid\\":[\\"uuid\\",\\"56ab3f42-8b1c-4939-a338-7ff9b2613960\\"]},{\\"name\\":\\"cool1\\",\\"_uuid\\":\"
    			+\"[\\"uuid\\",\\"b0c59ca1-3830-46df-abbe-bdb803cb5bfd\\"]},{\\"name\\":\\"prova0\\",\\"_uuid\\":[\\"uuid\\",\"
    			+\"\\"c96ffa6c-a5ee-4575-bc1c-a10f82c8cd45\\"]}]}],\\"error\\":null}\";*/
    	
    	String plain = "{\"id\":\"9596\",\"result\":[{\"rows\":[{\"_uuid\":[\"uuid\",\"744188ba-8b69-4c84-86d3-d220e1621bfc\"],\"name\":\"br0\",\"ports\":[\"set\",[[\"uuid\",\"6e827146-336a-4279-b115-02dc902a93b9\"],[\"uuid\",\"a82113ad-18b4-4f95-8dba-89de47f24078\"]]]}]}],\"error\":null}";
    	
    	TypeReference<SelectResponse> typeRef = new TypeReference<SelectResponse>() {
		};
		
		ObjectMapper mapper = new ObjectMapper();
		SelectResponse r = mapper.readValue(plain, SelectResponse.class); 
		OvsdbRow row = r.getResult().get(0).getRows().get(0);
		OvsDBSet<UUID> uuids = (OvsDBSet<UUID>)row.getColumn("ports");
		System.out.println(uuids.get(0));
	
		/*ArrayList thisSet = (ArrayList)r.getResult().get(0).getRows().get(0).get("ports");
		System.out.println(r.getResult().get(0).getRows().get(0).get("ports"));
		System.out.println(((ArrayList)thisSet.get(1)).get(0));
		
		TypeReference<OvsDBSet<UUID>> type = new TypeReference<OvsDBSet<UUID>>() {
		};
		
		OvsDBSet<UUID> l = new OvsDBSet<UUID>();
		l.add(new UUID("ciao"));
		mapper.writeValue(System.out, l);
		String dunno = "[\"set\",[[\"named-uuid\",\"ciao\"],[\"named-uuid\",\"ehy\"]]]";
		OvsDBSet<UUID> dunnoS = mapper.readValue(dunno,type);
		System.out.println(": "+dunnoS.get(1));*/
		//System.out.println(r.getResult().get(0).getRows().get(1).get(\"_uuid\"));
		//System.out.println(o.get(\"hi\"));
    }
}
