package org.opendaylight.ovsdb.lib.notation.json;

import java.io.IOException;

import org.opendaylight.ovsdb.lib.notation.OvsDBSet;
import org.opendaylight.ovsdb.lib.notation.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OvsdbSetDeserializer extends JsonDeserializer<OvsDBSet<?>> {

	@Override
	public OvsDBSet<?> deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		
		//ObjectCodec codec = parser.getCodec();
		//JsonNode node = codec.readTree(parser);
		//System.out.println(node.get("ports"));
		return new OvsDBSet<UUID>();
	}
	
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException{
		String setArray = "{\"ports\":[\"set\", [[\"uuid\", \"6e827146-336a-4279-b115-02dc902a93b9\"], [\"uuid\", \"a82113ad-18b4-4f95-8dba-89de47f24078\"]]]}";
		ObjectMapper mapper = new ObjectMapper();
		/*TypeReference<OvsDBSet<UUID>> tep = new TypeReference<OvsDBSet<UUID>>() {
		};
		
		OvsDBSet<UUID> s = mapper.readValue(setArray, tep);*/
		String uuid = "\"row\":[\"uuid\", \"6e827146-336a-4279-b115-02dc902a93b9\"]";
		UUID ud = mapper.readValue(uuid, UUID.class);
		mapper.writeValue(System.out, ud);
	}

}
