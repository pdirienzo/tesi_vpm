/*
 * Copyright (C) 2013 EBay Software Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Authors : Ashwin Raveendran, Madhu Venugopal
 */
package org.opendaylight.ovsdb.lib.notation;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.ovsdb.lib.notation.json.UUIDSerializer;
import org.opendaylight.ovsdb.lib.notation.json.UUIDStringConverter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(contentConverter = UUIDStringConverter.class)
@JsonSerialize(using = UUIDSerializer.class)
/*
 * Handles both uuid and named-uuid.
 */
public class UUID {
    String val;

    public UUID(String value) {
        this.val = value;
    }
    
    public UUID(List<String> jsonArray){
    	this.val = jsonArray.get(1);
    }
    
    /**
     * Returns the openvswitch uuid representation, which is a json array
     * with two elements: the "uuid" string followed by the actual uuid 
     * Ex. [uuid,8541-2145-fs54-a451]
     * 
     * @return
     */
    public List<String> toJsonArray(){
    	List<String> o = new ArrayList<String>();
    	o.add("uuid");
    	o.add(val);
    	return o;
    }
    
    /**
     * Returns the openvswitch named uuid representation, which is a json array
     * with two elements: the "named-uuid" string followed by the actual uuid 
     * This is useful when chaining multiple transactions
     * 
     * Ex. [named-uuid, myPort]
     * 
     * @return
     */
    public List<String> toNamedJsonArray(){
    	List<String> o = new ArrayList<String>();
    	o.add("named-uuid");
    	o.add(val);
    	return o;
    }

    @Override
    public String toString() {
        return val;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((val == null) ? 0 : val.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UUID other = (UUID) obj;
        if (val == null) {
            if (other.val != null)
                return false;
        } else if (!val.equals(other.val))
            return false;
        return true;
    }
}
