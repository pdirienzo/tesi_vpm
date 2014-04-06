package org.at.tries;

import java.io.IOException;

import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class TryDomainOps {

	public static void main(String[] args) throws IOException, LibvirtException {
		HypervisorConnection hc = HypervisorConnection.getConnectionWithTimeout(
				new Hypervisor("sprom", "sprom-Dell",
						16514), false, 3000);
		for(Domain d : hc.getAllDomains())
			System.out.println(d.getName());
		hc.close();

	}

}
