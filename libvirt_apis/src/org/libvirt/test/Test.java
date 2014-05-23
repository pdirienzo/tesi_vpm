package org.libvirt.test;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class Test {

	public static void main(String[] args) throws LibvirtException {
		Connect conn = new Connect("qemu://pasquale-PC/system");
		Domain d = conn.domainLookupByName("ubulinx_nfs");
		
		//System.out.println(d.getID());
		d.destroyWithFlags(Domain.DestroyFlags.DESTROY_GRACEFUL);
		System.out.println(String.format("%.2f",d));
		conn.close();

	}

}
