package net.floodlightcontroller.vpm;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.ImmutablePort;

import org.openflow.util.HexString;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;

public class VPMTopologyGetPortResource extends ServerResource {
	
	private IFloodlightProviderService ifps;
	
	@Override
	public void init(Context arg0, Request arg1, Response arg2) {
		// TODO Auto-generated method stub
		super.init(arg0, arg1, arg2);
		ifps = (IFloodlightProviderService)getContext().getAttributes().
                get(IFloodlightProviderService.class.getCanonicalName());
	}
	
	
	/**
	 * Return the port number given the switch-dpid and the port-name
	 * @param Json
	 * @return String
	 */
	@Post
	public String getPortInfo(String fmJson){
		String response="";
		Map<String,String> mp=null;
		boolean isPopulated = false;
		try {
			mp=jsonToStorageEntry(fmJson);
		
			if(mp.get("port-name").equals("vnetx")){ // user wants every vnet port
				StringBuilder sb = new StringBuilder();
				sb.append("{\"result\":[");
				Object[] ports = ifps.getSwitch(HexString.toLong(mp.get("switch-dpid"))).getPorts().toArray();
				for(int i=0;i<ports.length;i++){
					ImmutablePort port = (ImmutablePort)ports[i];
					if (port.getName().startsWith("vnet")){
						sb.append("{\"port-name\":\""+port.getName()+"\",");
						sb.append("\"port-number\":\""+port.getPortNumber()+"\"},");
						isPopulated=true;
							
					}
				}
				if (isPopulated){
					sb.deleteCharAt(sb.length()-1);	
				}
				
				sb.append("]}");
				
				response = sb.toString();
			}else
				response = "{\"port-number\":\""+String.valueOf(ifps.getSwitch(HexString.toLong(mp.get("switch-dpid")))
					.getPort(mp.get("port-name")).getPortNumber())+"\"}";
		} catch (IOException | NullPointerException e) {
			response="{\"port-number\":\"-1\"}";
		}
		return response;
	}
	
	public static Map<String, String> jsonToStorageEntry(String fmJson) throws IOException {
        Map<String, String> entry = new HashMap<String, String>();
        MappingJsonFactory f = new MappingJsonFactory();
        JsonParser jp;
        
        try {
            jp = f.createJsonParser(fmJson);
        } catch (JsonParseException e) {
            throw new IOException(e);
        }
        
        jp.nextToken();
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("Expected START_OBJECT");
        }
        
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            if (jp.getCurrentToken() != JsonToken.FIELD_NAME) {
                throw new IOException("Expected FIELD_NAME");
            }
            
            String n = jp.getCurrentName();
            jp.nextToken();
            if (jp.getText().equals("")) 
                continue;
            
            if (n == "switch-dpid")
                entry.put("switch-dpid", jp.getText());
            else if (n == "port-name")
                entry.put("port-name", jp.getText());
        }
        
        return entry;
    }
	
}
