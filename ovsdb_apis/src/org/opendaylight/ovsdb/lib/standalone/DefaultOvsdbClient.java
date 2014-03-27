package org.opendaylight.ovsdb.lib.standalone;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.ovsdb.lib.message.ListDbsResponse;
import org.opendaylight.ovsdb.lib.message.OvsdbRequest;
import org.opendaylight.ovsdb.lib.message.OvsdbResponse;
import org.opendaylight.ovsdb.lib.message.SelectResponse;
import org.opendaylight.ovsdb.lib.message.TransactResponse;
import org.opendaylight.ovsdb.lib.message.operations.DeleteOperation;
import org.opendaylight.ovsdb.lib.message.operations.InsertOperationNew;
import org.opendaylight.ovsdb.lib.message.operations.MutateOperation;
import org.opendaylight.ovsdb.lib.message.operations.OperationResult;
import org.opendaylight.ovsdb.lib.message.operations.SelectOperation;
import org.opendaylight.ovsdb.lib.notation.Condition;
import org.opendaylight.ovsdb.lib.notation.Function;
import org.opendaylight.ovsdb.lib.notation.Mutation;
import org.opendaylight.ovsdb.lib.notation.Mutator;
import org.opendaylight.ovsdb.lib.notation.OvsdbOptions;
import org.opendaylight.ovsdb.lib.notation.UUID;
import org.opendaylight.ovsdb.lib.table.Bridge;
import org.opendaylight.ovsdb.lib.table.Interface;
import org.opendaylight.ovsdb.lib.table.Open_vSwitch;
import org.opendaylight.ovsdb.lib.table.Port;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class creates a new connection for each request it makes.
 * Each request is synchronous and so blocking: the call will return
 * just on result or error. 
 * 
 * @author pasquale
 *
 */
public class DefaultOvsdbClient /*implements OvsdbClient*/ {
	
	private ObjectMapper mapper;
	private String sAddr;
	private int port;
	
	private boolean debug;
	
	public DefaultOvsdbClient(String serverAddr,int port){
		mapper = new ObjectMapper();
		mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET,false);
		
		this.sAddr = serverAddr;
		this.port = port;
		debug = false;
	}
	
	public void setDebugMode(boolean v){
		debug = v;
	}
	
	public String[] getOvsdbNames() throws IOException, OvsdbException{
		ListDbsResponse resp = sendRequest("list_dbs", new ArrayList<Object>(),
				ListDbsResponse.class);
		String[] names = new String[resp.result.size()];
		for(int i=0;i<names.length;i++)
			names[i] = (String)resp.result.get(i);
		
		return names;
		
	}
	
	private <T extends OvsdbResponse> T sendRequest(String method, List<Object> params,
			Class<T> expectedResponse) throws IOException, OvsdbException{
		Socket s = new Socket(sAddr,port);
		OvsdbRequest req = new OvsdbRequest(method, params);
		if(debug){
			System.out.println("Sending request --->");
			mapper.writeValue(System.out,req);
			System.out.println("\n<----------------");
		}
			
		mapper.writeValue(s.getOutputStream(), req);
		
		T resp = mapper.readValue(s.getInputStream(),
				expectedResponse);
		
		if(debug){
			System.out.println("Received --->");
			mapper.writeValue(System.out,resp);
			System.out.println("\n<----------------");
		}
		s.close();
		
		
		if(resp.error != null){
			throw new OvsdbException((resp.error.error));
		}
		
		return resp;
	}

	//@Override
	public boolean sendEchoRequest() throws IOException, OvsdbException {
		boolean result = true;
		sendRequest("echo", new ArrayList<Object>(),OvsdbResponse.class); 
		
		return result;
	}

	//@Override
	public List<HashMap<String,Object>> select(String dbName, String tName, List<String> columns,
			List<Condition> conditions) throws IOException,OvsdbException {
		
		List<Object> params = new ArrayList<Object>();
		params.add(dbName);
		//costruisci select op
		SelectOperation select = new SelectOperation(tName,
				conditions, columns);
		//inserisci in params
		params.add(select);
		
		SelectResponse r = sendRequest("transact", params,SelectResponse.class);	
		
		return r.getResult().get(0).getRows();
		
	}
	
	
	@SuppressWarnings("unchecked")
	private UUID getUUIDFromName(String ovs,String table,String name) throws OvsdbException, IOException{
		List<Condition> where = new ArrayList<Condition>();
		List<String> columns = new ArrayList<String>();
		
		if(table.equals(Port.NAME.getName())){
			where.add(new Condition(Port.Column.name.name(),Function.EQUALS,name));
			columns.add(Port.Column._uuid.name());
		}else if(table.equals(Bridge.NAME.getName())){
			where.add(new Condition(Bridge.Column.name.name(),Function.EQUALS,name));
			columns.add(Bridge.Column._uuid.name());
		}else if(table.equals(Interface.NAME.getName())){
			where.add(new Condition(Interface.Column.name.name(),Function.EQUALS,name));
			columns.add(Interface.Column._uuid.name());
		}else if(table.equals(ovs)){
			columns.add(Open_vSwitch.Column._uuid.name());
		}else
			throw new OvsdbException("Unrecognized table name");
		
		List<HashMap<String, Object>>  res = select(ovs, table, columns, where); // just one row will be returned as name is unique
		if(res.size() == 0)
			throw new OvsdbException("Impossible to find name: "+name);
		
		return new UUID((List<String>)res.get(0).get(columns.get(0)));
		
	}
	
	public UUID insert(String dbName,String tName,List<Object> row) 
			throws IOException,OvsdbException{
		
		List<Object> params = new ArrayList<Object>();
		params.add(dbName);
		
		//costruisci insert op
		InsertOperationNew insert = new InsertOperationNew(
				tName, row.get(0));
		params.add(insert);
		
		TransactResponse resp = sendRequest("transact", params,TransactResponse.class);
		
		checkTransactionErrors(resp);
		
		return resp.getResult().get(0).getUuid();
		
	}
	
	/**
	 * Deletes all ports in a bridge
	 * 
	 * @param ovs
	 * @param bridgeName
	 */
	public void deleteAllPorts(String ovs,String bridgeName){
		
	}
	
	public void deletePort(String ovs, String bridgeName, String portName) throws IOException, OvsdbException{
		List<Object> params = new ArrayList<Object>();
		params.add(ovs);
		
		//we will have to do the inverse of addPort
		//first we have to get uuid for the port as in the Bridge table port is referred by the uuid
		UUID portUUID = getUUIDFromName(ovs, Port.NAME.getName(), portName);
		
		//1. we do a mutation on the bridge 
		List<Condition> where = new ArrayList<Condition>();
		where.add(new Condition(Bridge.NAME.getName(), Function.EQUALS, bridgeName));
		//mutation!
		List<Mutation> mutations = new ArrayList<Mutation>();
		mutations.add(new Mutation(Bridge.Column.ports.name(),Mutator.DELETE,portUUID.toJsonArray()));
		//creation of the mutate op
		MutateOperation mutationOp = new MutateOperation(Bridge.NAME.getName(), where, mutations);
		params.add(mutationOp);
		
		//2. now we delete the port from the port table
		List<Condition> wherePort = new ArrayList<Condition>();
		wherePort.add(new Condition("name", Function.EQUALS, portName));
		DeleteOperation deletePort = new DeleteOperation(Port.NAME.getName(), wherePort);
		params.add(deletePort);
		
		//3. finally we delete the inteface this port represents
		DeleteOperation deleteInterface = new DeleteOperation(Interface.NAME.getName(), wherePort);
		params.add(deleteInterface);		
		
		//sending
		TransactResponse resp = sendRequest("transact", params, TransactResponse.class);
		
		checkTransactionErrors(resp);
		
	}
	
	public void addBridge(String ovs,String bridgeName) throws IOException, OvsdbException{
		List<Object> params = new ArrayList<Object>();
		params.add(ovs);
		
		//we obtain the ovsdb uuid
		UUID ovsdbUUID = getUUIDFromName(ovs, ovs, ovs);
		
		//1. for starters we add a new bridge and receive the generated uuid
		Map<String,Object> bridgeNode = new HashMap<String,Object>();
		bridgeNode.put(Bridge.Column.name.name(), bridgeName);
		
		InsertOperationNew insertBridge = new InsertOperationNew(Bridge.NAME.getName(),
				bridgeNode,"myBridge");
		
		params.add(insertBridge);
		
		//2 we add the bridge uuid to the ovsdb
		UUID bridgeUUID = new UUID("myBridge");
		
		//we mutate the row for the interested ovsdb
		List<Condition> where = new ArrayList<Condition>();
		where.add(new Condition(Open_vSwitch.Column._uuid.name(), Function.EQUALS, ovsdbUUID.toJsonArray()));
		
		//mutation!
		List<Mutation> mutations = new ArrayList<Mutation>();
		mutations.add(new Mutation(Open_vSwitch.Column.bridges.name(), Mutator.INSERT, bridgeUUID.toNamedJsonArray() ));
		
		//creation of the mutate op
		MutateOperation bridgeMutation = new MutateOperation(ovs, where, mutations);
		params.add(bridgeMutation);
		
		//sending
		TransactResponse resp = sendRequest("transact", params, TransactResponse.class);
		checkTransactionErrors(resp);
		
		this.addPort(ovs, bridgeName, bridgeName, Interface.Type.internal.name(), null);
	}
	
	public void deleteBridge(String ovs,String bridgeName) throws OvsdbException, IOException{
		List<Object> params = new ArrayList<Object>();
		params.add(ovs);
		
		//we obtain the ovsdb and bridge uuids
		UUID ovsdbUUID = getUUIDFromName(ovs, ovs, ovs);
		UUID bridgeUUID = getUUIDFromName(ovs, Bridge.NAME.getName(), bridgeName);
		
		//we will have to do the inverse of addBridge
		//1. we do a mutation on the ovsdb table to delete any reference to the bridge
		List<Condition> where = new ArrayList<Condition>();
		where.add(new Condition(Open_vSwitch.Column._uuid.name(), Function.EQUALS, ovsdbUUID.toJsonArray()));
		
		//mutation!
		List<Mutation> mutations = new ArrayList<Mutation>();
		mutations.add(new Mutation(Open_vSwitch.Column.bridges.name(), Mutator.DELETE, bridgeUUID.toNamedJsonArray() ));
				
		//creation of the mutate op
		MutateOperation bridgeMutation = new MutateOperation(ovs, where, mutations);
		params.add(bridgeMutation);
		
		//2. we remove the bridge
		List<Condition> whereBridge = new ArrayList<Condition>();
		whereBridge.add(new Condition(Bridge.Column.name.name(), Function.EQUALS, bridgeName));
		
		DeleteOperation deleteBridge = new DeleteOperation(Bridge.NAME.getName(),whereBridge);
		params.add(deleteBridge);
				
		
	}
	
	public void addPort(String ovs,String bridgeName,String portName,String portType,OvsdbOptions options) throws IOException, OvsdbException{
		List<Object> params = new ArrayList<Object>();
		params.add(ovs);
		
		//in order to add a port we have to do three calls
		//1. for starters we add a new interface and receive the generated uuid
		Map<String,Object> interfaceNode = new HashMap<String,Object>();
		interfaceNode.put("name", portName);
		interfaceNode.put("type", portType);
		
		
		InsertOperationNew insertInterface = new InsertOperationNew(Interface.NAME.getName(),
				interfaceNode,"myInterface");
		
		params.add(insertInterface);
		
		if(options != null && options.isEmpty()==false){
			//1.1 we have to add the mutation for options
			
			//we mutate the row for the interested interface
			List<Condition> where = new ArrayList<Condition>();
			where.add(new Condition("name", Function.EQUALS, portName));
			
			//mutation!
			List<Mutation> mutations = new ArrayList<Mutation>();
			mutations.add(new Mutation(Interface.Column.options.name(), Mutator.INSERT, options));
			
			//creation of the mutate op
			MutateOperation interfaceMutation = new MutateOperation(Interface.NAME.getName(), where, mutations);
			params.add(interfaceMutation);
		}
		
		//2. now we add the new interface to the port list
		UUID interfaceUUID = new UUID("myInterface");
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("name",portName);
		m.put("interfaces", interfaceUUID.toNamedJsonArray());
		InsertOperationNew insertPort = new InsertOperationNew(Port.NAME.getName(),
				m,"myPort");
		params.add(insertPort);
		
		
		//3. finally we add this port to the bridge doing a mutation ;)
		UUID portUUID = new UUID("myPort");
		//we mutate the row for the interested bridge
		List<Condition> where = new ArrayList<Condition>();
		where.add(new Condition("name", Function.EQUALS, bridgeName));
				
		//mutation!
		List<Mutation> mutations = new ArrayList<Mutation>();
		mutations.add(new Mutation(Bridge.Column.ports.name(),Mutator.INSERT,portUUID.toNamedJsonArray()));
		//creation of the mutate op
		MutateOperation mutationOp = new MutateOperation(Bridge.NAME.getName(), where, mutations);
		params.add(mutationOp);
		
		//sending
		TransactResponse resp =sendRequest("transact", params, TransactResponse.class);
		checkTransactionErrors(resp);
		
	}
	
	/**
	 * Utily method to check for transaction errors
	 * @param resp
	 * @throws OvsdbException
	 */
	private void checkTransactionErrors(TransactResponse resp) throws OvsdbException{
		String err = null;
		for(OperationResult res : resp.getResult()){
			if ((err =res.getError()) != null)
				throw new OvsdbException("Error: "+err+"\nDetails: "+res.getDetails());
		}
	}
	
	

}
