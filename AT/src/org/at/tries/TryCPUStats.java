package org.at.tries;

import java.io.IOException;

import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.libvirt.CPUStatistic;
import org.libvirt.LibvirtException;

public class TryCPUStats {

	public static void main(String[] args) throws IOException, LibvirtException {
		HypervisorConnection hc = HypervisorConnection.getConnectionWithTimeout(
				new Hypervisor("pasquale", "pasquale-VPCEB1A4E",
						16514), true, 3000);
		
		for (CPUStatistic c : hc.getCpuStatistics())
			System.out.println(c);
		hc.close();
	}

}
