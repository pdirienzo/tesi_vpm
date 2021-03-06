package org.opendaylight.ovsdb.lib.standalone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opendaylight.ovsdb.lib.message.TransactResponse;
import org.opendaylight.ovsdb.lib.notation.Condition;
import org.opendaylight.ovsdb.lib.notation.Function;
import org.opendaylight.ovsdb.lib.notation.OvsDBSet;
import org.opendaylight.ovsdb.lib.notation.OvsdbOptions;
import org.opendaylight.ovsdb.lib.notation.OvsdbRow;
import org.opendaylight.ovsdb.lib.notation.UUID;
import org.opendaylight.ovsdb.lib.table.Bridge;
import org.opendaylight.ovsdb.lib.table.Interface;
import org.opendaylight.ovsdb.lib.table.Open_vSwitch;
import org.opendaylight.ovsdb.lib.table.Port;

public class PRova {

	public static void main(String[] args) throws IOException, OvsdbException {
		
		DefaultOvsdbClient client = new DefaultOvsdbClient("192.168.1.179", 6640);
		client.setDebugMode(true);
		String[] names = client.getOvsdbNames();
		
		/*OvsdbOptions opts = new OvsdbOptions();
		opts.put(OvsdbOptions.REMOTE_IP, "143.225.229.197");
		
		OvsDBSet<Integer> trunks = new OvsDBSet<Integer>();
		trunks.add(1);
		trunks.add(2);
		trunks.add(3);*/
		
		System.out.println(client.getBridgeDpid(names[0], "br0"));
		
		/*for(String s : client.listPorts(names[0], "br0")){
			System.out.println(s);
		}*/
		//client.deleteBridge(names[0], "br1");
		//client.addBridge(names[0], "br1");
		//client.addPort(names[0], "br0", "cool0", Interface.Type.gre.name(),2,trunks,opts);
		//client.addPort(names[0], "br1", "cool1", Interface.Type.gre.name(),2,trunks,opts);
		//client.deletePort(names[0],"br0","cool0");
		
		//System.out.println(client.getUUIDFromName(names[0], names[0], names[0]));
		
		/*
		 //select demo
		
		List<Condition> where = new ArrayList<Condition>();
		where.add(new Condition(Bridge.Column.name.name(), Function.EQUALS, "br0"));
		//where.add(new Condition(Bridge.Column.ports.name(),FUNCTION.))
		
		List<String> columns = new ArrayList<>();
		columns.add(Bridge.Column.name.name());
		//columns.add(Bridge.Column.ports.name());
		columns.add(Bridge.Column.ports.name());
		columns.add(Bridge.Column._uuid.name());
		//columns.add(Interface.Column.options.name());
		
		
		
		List<OvsdbRow> rs = 
				client.select(names[0], Bridge.NAME.getName()
				, columns, where);
		
		for(OvsdbRow row : rs){
			System.out.println(row.getColumn("name"));
		}*/
		
		
		/*
		//insert port
		//new port created d49f4ede-f241-4b32-81fd-9caa4111823a
		List<Object> row = new ArrayList<Object>();
		
		InsertPort node = new InsertPort("cool0" ,
				"f570d08e-4dc5-492a-8fbd-46eb96996e47");
		row.add(node);
		
		System.out.println(
				client.insert(names[0], Port.NAME.getName(),row)
				);
		
		*/
		/*
		
         //rows=[{name=br0, interfaces=[uuid, 81ec05f9-6ce2-421d-8491-9fe37cb3aced]}], error=null]
		//insert interface demo
		// new interface created: 124c5af5-ff63-453d-9807-b987ff4f2f85
		
		
		List<Object> row = new ArrayList<Object>();
		
		row.add(JsonNodeFactory.instance.objectNode().put("name", "cool0")
				.put("type", "internal"));
		
		System.out.println(
				client.insert(names[0], Interface.NAME.getName(),row)
				);
		
		*/
	}
	
	

}
