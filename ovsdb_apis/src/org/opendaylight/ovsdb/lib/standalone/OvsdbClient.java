package org.opendaylight.ovsdb.lib.standalone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.ovsdb.lib.notation.Condition;
import org.opendaylight.ovsdb.lib.notation.UUID;

public interface OvsdbClient {
	public String[] getOvsdbNames() throws IOException,OvsdbException;
	public boolean sendEchoRequest() throws IOException,OvsdbException;
	public ArrayList<Object> select(String dbName,String tName,List<String> columns, 
			List<Condition> conditions) throws IOException,OvsdbException;
	
	public UUID insert(String dbName,String tName,List<Object> row) throws IOException,OvsdbException;
}
