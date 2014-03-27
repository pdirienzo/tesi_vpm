/*
 * Copyright (C) 2013 Red Hat, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Authors : Madhu Venugopal
 */
package org.opendaylight.ovsdb.lib.message.operations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
// TODO Madhu : This is not complete. Getting it in to enable other committers to make progress

@JsonInclude(Include.NON_NULL)
public class InsertOperationNew extends Operation {
    public String table;
    public Object row;
    
    @JsonProperty("uuid-name") 
    public String uuidname;

    public InsertOperationNew() {
        super();
        super.setOp("insert");
    }

    public InsertOperationNew(String table,/* String uuidName,*/
    		Object  row) {
        this();
        this.table = table;
        this.row = row;
    }
    
    public InsertOperationNew(String table,/* String uuidName,*/
    		Object  row, String uuidname) {
        this(table,row);
        this.uuidname = uuidname;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
/*
    public String getUuidName() {
        return uuidName;
    }

    public void setUuidName(String uuidName) {
        this.uuidName = uuidName;
    }*/

    public Object  getRow() {
        return row;
    }

    public void setRow(Object  row) {
        this.row = row;
    }

    @Override
    public String toString() {
        return "InsertOperation [table=" + table /*+ ", uuidName=" + uuidName*/
                + ", row=" + row + ", toString()=" + super.toString() + "]";
    }
}
