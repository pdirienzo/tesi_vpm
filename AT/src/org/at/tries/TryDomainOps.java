package org.at.tries;

import java.io.IOException;

import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.Network;

public class TryDomainOps {

	public static void main(String[] args) throws IOException, LibvirtException {
		HypervisorConnection hc = HypervisorConnection.getConnectionWithTimeout(
				new Hypervisor("pasquale", "pasquale-VPCEB1A4E",
						16514), false, 3000);
		
		Network n = hc.createNetworkFromFile("xml_definitions/vpm_network.xml");
		n.setAutostart(true);
		n.create();
		hc.close();

	}

}
