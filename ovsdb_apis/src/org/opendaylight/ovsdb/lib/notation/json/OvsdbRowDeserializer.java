package org.opendaylight.ovsdb.lib.notation.json;

import java.io.IOException;

import org.opendaylight.ovsdb.lib.notation.OvsdbRow;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class OvsdbRowDeserializer extends JsonDeserializer<OvsdbRow>{

	@Override
	public OvsdbRow deserialize(JsonParser arg0, DeserializationContext arg1)
			throws IOException, JsonProcessingException {
		// TODO Auto-generated method stub
		return null;
	}

}
