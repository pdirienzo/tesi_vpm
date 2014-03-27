/*
 * [[ Authors will Fill in the Copyright header ]]
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Authors : Brent Salisbury, Evan Zeller
 */
package org.opendaylight.ovsdb.lib.message;

import java.util.List;
import java.util.Random;

public class OvsdbRequest {
    public String method;
    public List<Object> params;
    public String id;

    public OvsdbRequest(String method, List<Object> arg){
        this.method = method;
        this.params = arg;
        Random x = new Random();
        this.id = Integer.toString(x.nextInt(10000));
    }
    /*
    public OvsdbRequest(String method, List<String> arg, int id){
        this.method = method;
        this.params = arg;
        Random x = new Random();
        this.id = Integer.toString(x.nextInt(10000));
    }*/
    
}
