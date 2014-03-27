/*
 * Copyright (C) 2013 Red Hat, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Authors : Madhu Venugopal, Ashwin Raveendran
 */
package org.opendaylight.ovsdb.lib.notation.json;

import java.io.IOException;
import org.opendaylight.ovsdb.lib.notation.OvsdbOptions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class OvsdbOptionsSerializer extends JsonSerializer<OvsdbOptions> {
   
	
	@Override
	public void serialize(OvsdbOptions options, JsonGenerator generator,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		
		generator.writeStartArray();
		generator.writeString("map");
		generator.writeStartArray();
		
		for (String key : options.stringPropertyNames()) {
				generator.writeStartArray();
				generator.writeObject(key);
				generator.writeObject(options.get(key));
				generator.writeEndArray();
			}
			generator.writeEndArray();
			generator.writeEndArray();
		
	}
}