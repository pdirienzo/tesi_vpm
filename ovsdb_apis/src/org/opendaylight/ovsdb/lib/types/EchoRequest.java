package org.opendaylight.ovsdb.lib.types;

public class EchoRequest extends Request{

	public Object[] params;
	public EchoRequest(String method) {
		super(method);
		params = new Object[0];
	}

}
