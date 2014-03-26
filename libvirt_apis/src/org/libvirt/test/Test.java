package org.libvirt.test;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class Test {

	public static void main(String[] args) throws LibvirtException {
		Connect conn = new Connect("qemu:///system");
		Domain d = conn.domainLookupByName("linx2");
		
		int i=0;
		while(i<5){
			System.out.println(String.format("%.2f", d.getCPUOverallUsage(1000)));
			i++;
		}
		//System.out.println(String.format("%.2f",v));
		//conn.close();

	}

}
