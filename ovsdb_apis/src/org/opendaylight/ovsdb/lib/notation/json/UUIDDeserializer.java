package org.opendaylight.ovsdb.lib.notation.json;

import java.io.IOException;

import org.opendaylight.ovsdb.lib.notation.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class UUIDDeserializer extends JsonDeserializer<UUID>{

	@Override
	public UUID deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		// TODO Auto-generated method stub
		return null;
	}

}
