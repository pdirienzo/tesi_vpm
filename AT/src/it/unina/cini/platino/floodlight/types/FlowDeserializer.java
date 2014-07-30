package it.unina.cini.platino.floodlight.types;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class FlowDeserializer  extends JsonDeserializer<Flow> {

	@Override
	public Flow deserialize(JsonParser jp, DeserializationContext ctx)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		Flow flow = new Flow();
		flow.switchDpid = node.get("switch").asText();
		flow.name = node.get("name").asText();
		flow.dstIP = node.get("dst-ip").asText();
		flow.srcIP = node.get("src-ip").asText();
		flow.etherType = node.get("ether-type").shortValue();
		flow.protocol = node.get("protocol").shortValue();
		flow.priority = node.get("priority").asInt();
		flow.srcMac = node.get("src-mac").asText();
		flow.dstMac = node.get("dst-mac").asText();
		
		return null;
	}

}
