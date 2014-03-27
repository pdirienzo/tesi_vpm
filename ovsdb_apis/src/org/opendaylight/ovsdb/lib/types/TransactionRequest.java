package org.opendaylight.ovsdb.lib.types;

import java.util.ArrayList;

public class TransactionRequest extends Request {

	public ArrayList<Object> params;
	
	public TransactionRequest(String method) {
		super(method);
		params = new ArrayList<Object>();
	}
	
	public static TransactionRequest buildrequest(String method,
			String dbName, 
			ArrayList<Operation> operations){
		
		TransactionRequest req = new TransactionRequest(method);
		req.params.add(dbName);
		
		return req;
	}

}
