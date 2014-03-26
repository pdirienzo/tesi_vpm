package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

public class virNodeCPUStats extends Structure{
	public byte[] tag = new byte[80];
    public long val ;

    private static final List fields = Arrays.asList( "tag", "val");

    @Override
    protected List getFieldOrder() {
        return fields;
    }
}
