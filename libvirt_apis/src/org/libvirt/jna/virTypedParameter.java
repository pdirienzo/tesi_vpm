package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class virTypedParameter extends Structure{

	public byte[] field = new byte[80];
	public int type;
	public VirUnion value;
	
	private static final List fields = Arrays.asList( "field", "type",
    		"value");
	@Override
	protected List getFieldOrder() {
		// TODO Auto-generated method stub
		return fields;
	}

}
