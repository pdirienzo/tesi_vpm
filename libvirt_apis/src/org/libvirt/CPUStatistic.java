package org.libvirt;

import java.nio.charset.Charset;
import org.libvirt.jna.virNodeCPUStats;
import org.libvirt.jna.virTypedParameter;

public class CPUStatistic {
	public String tag;
    public long val;
    
    //valid for host cpu stats
    public static final int KERNEL_H = 0;
    public static final int USER_H = 1;
    public static final int IDLE_H = 2;
    public static final int IOWAIT_H = 3;

    //valid for guest cpu stats
    public static final int CPU_D = 0;
    public static final int USER_D = 1;
    public static final int SYSTEM_D = 2;
    
    private String createStringFromBytes(byte[] b){
    	Charset ch = Charset.forName("UTF-8");
    	int i = 0;
    	while ((i<b.length) && (b[i]!=0))	
    		i++;
    	
    	return new String(b,0,i,ch);
    }
    
    public CPUStatistic(virNodeCPUStats stat){
    	tag = createStringFromBytes(stat.tag);
    	val = stat.val;
    }
    
    public CPUStatistic(virTypedParameter stat){
    	tag = createStringFromBytes(stat.field);
		val = stat.value.l;
    }

    public String getTag() {
        return tag;
    }

    public long getValue() {
        return val;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setValue(long val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return String.format("tag:%s%nval:%d%n", tag, val);
    }
}
