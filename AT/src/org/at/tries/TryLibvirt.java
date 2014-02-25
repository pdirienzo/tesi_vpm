package org.at.tries;

import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.libvirt.Domain;
import org.libvirt.DomainInterfaceStats;
import org.libvirt.LibvirtException;

public class TryLibvirt {

	public static void main(String[] args) throws LibvirtException {
		Hypervisor h = new Hypervisor("pasquale", "192.168.1.3", 22);
		HypervisorConnection c = new HypervisorConnection(h);
		
		//Domain dd =c.domainLookupByName("linx");
		
		
		for( Domain d : c.getAllDomains()){
			System.out.println(d.getName()+" running: "+d.isActive());
			d.free();
			
		}
		
		c.close();

	}

}
