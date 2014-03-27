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

import java.util.ArrayList;

import org.opendaylight.ovsdb.lib.message.operations.SelectOperationResult;

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
}
