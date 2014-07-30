package it.unina.cini.platino.floodlight.types;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class FlowSerializer extends JsonSerializer<Flow>{

	@Override
	public void serialize(Flow flow, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		
		gen.writeStartObject();
		gen.writeStringField("switch", flow.switchDpid);
		gen.writeStringField("name", flow.name);
		gen.writeBooleanField("active", flow.active);
		gen.writeNumberField("priority", flow.priority);
		
		if(flow.etherType != -1)
			gen.writeNumberField("ether-type", flow.etherType);
		if(flow.srcIP != null)
			gen.writeStringField("src-ip", flow.srcIP);
		if(flow.dstIP != null)
			gen.writeStringField("dst-ip", flow.dstIP);
		if(flow.srcMac != null)
			gen.writeStringField("src-mac", flow.srcMac);
		if(flow.dstMac != null)
			gen.writeStringField("dst-mac", flow.dstMac);
		if(flow.srcPort != -1)
			gen.writeNumberField("src-port", flow.srcPort);
		if(flow.dstPort != -1)
			gen.writeNumberField("dst-port", flow.dstPort);
		
		if(flow.protocol != -1)
			gen.writeNumberField("protocol", flow.protocol);
		
		if(flow.actions.size() > 0){
			StringBuilder sb = new StringBuilder();
			for(String action : flow.actions){
				sb.append(action+",");
			}
			sb.deleteCharAt(sb.length()-1);
			
			gen.writeStringField("actions", sb.toString());
		}
		gen.writeEndObject();
	}

}
