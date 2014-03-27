package org.opendaylight.ovsdb.lib.notation.json;

import java.io.IOException;

import org.opendaylight.ovsdb.lib.notation.OvsDBSet;
import org.opendaylight.ovsdb.lib.notation.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OvsdbSetDeserializer extends JsonDeserializer<OvsDBSet<?>> {

	@Override
	public OvsDBSet<?> deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		
		String s = null;
		while( (s=parser.nextTextValue()) != null){
			System.out.println(s);
		}
		return null;
	}
	
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException{
		String setArray = "{[set, [[uuid, 6e827146-336a-4279-b115-02dc902a93b9], [uuid, a82113ad-18b4-4f95-8dba-89de47f24078], [uuid, f9acec85-db0f-43e4-b3cf-a74085f01dbd]]]}";
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<OvsDBSet<UUID>> tep = new TypeReference<OvsDBSet<UUID>>() {
		};
		
		OvsDBSet<UUID> s = mapper.readValue(setArray, tep);
	}

}
