package org.at.tries;

import java.io.IOException;

import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.libvirt.CPUStatistic;
import org.libvirt.LibvirtException;
import org.libvirt.NodeInfo;

public class TryCPUStats {

	public static void main(String[] args) throws IOException, LibvirtException {
		HypervisorConnection hc = HypervisorConnection.getConnectionWithTimeout(
				new Hypervisor("pasquale", "pasquale-VPCEB1A4E",
						16514), true, 3000);
		
		NodeInfo infos = hc.nodeInfo();
		hc.close();
	}

}
