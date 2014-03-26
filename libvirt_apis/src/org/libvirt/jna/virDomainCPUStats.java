package org.libvirt.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class virDomainCPUStats extends Structure{
	public long number ;
    public long state ;
    public long	cpuTime;	
    public long	cpu;	

    private static final List fields = Arrays.asList( "number", "state",
    		"cpuTime","cpu");

    @Override
    protected List getFieldOrder() {
        return fields;
    }
}
