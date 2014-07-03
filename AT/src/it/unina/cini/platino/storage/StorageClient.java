package it.unina.cini.platino.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.json.JSONObject;

public class StorageClient {

	private String ip;
	private int port;
	
	public StorageClient(String ip,int port){
		this.ip = ip;
		this.port = port;
	}
	
	public JSONObject listFlavours() throws IOException{
		JSONObject request = new JSONObject();
		request.put("method","list_flavours");
		return sendRequest(request);
	}
	
	private JSONObject sendRequest(JSONObject request) throws IOException{
		Socket s = new Socket();
		
		InetSocketAddress addr = new InetSocketAddress(
				InetAddress.getByName(ip), port);
		
		s.connect(addr, 3000);
		
		PrintWriter outToServer = new PrintWriter(s.getOutputStream(),true);
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
				s.getInputStream()));
		outToServer.println(request.toString());
		JSONObject response = new JSONObject(inFromServer.readLine());
		s.close();
		
		if(response.get("status").equals("error"))
			throw new IOException(response.getString("details"));
		
		return response;
	}
	
	public JSONObject createImage(JSONObject j) throws IOException{
		JSONObject request = new JSONObject();
		request.put("method","create");
		request.put("params", j);
		return sendRequest(request);
	}
	
	public static void main (String[] args) throws IOException{
		StorageClient c = new StorageClient("192.168.1.4", 8123);
		
		JSONObject params = new JSONObject();
		params.put("vmname", "linx");
		params.put("hypervisor", "H1");
		params.put("flavour", "test");
		JSONObject resp = c.createImage(params);
		System.out.println(resp.toString());
		/*
		//list flavours
		JSONObject res = c.listFlavours();
		System.out.println(res.toString());
		if(res.getString("status").equals("ok")){
			for(JSONObject flavour : res.getJSONArray("flavours")){
				System.out.println(">" +flavour.getString("name"));
			}
		}*/
		
	}
}
