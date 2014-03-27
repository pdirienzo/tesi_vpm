package org.opendaylight.ovsdb.lib.types;

import java.util.ArrayList;

public class Response {
	public String id;
	public Error error;
	public ArrayList<Object> result = new ArrayList<Object>();
}
