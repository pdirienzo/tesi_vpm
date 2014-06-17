package org.at.tries;

import java.io.IOException;

import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.libvirt.CPUStatistic;
import org.libvirt.LibvirtException;
import org.libvirt.NodeInfo;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;

public class TryCPUStats {

	public static void main(String[] args) throws IOException, LibvirtException {
		HypervisorConnection hc = HypervisorConnection.getConnectionWithTimeout(
				new Hypervisor("pasquale", "pasquale-VPCEB1A4E",
						16514,0), true, 3000);
		
		String[] pools = hc.listStoragePools();
		StoragePool pool = hc.storagePoolLookupByName("lun_atico");
		
		for(String s : pool.listVolumes()){
			
			System.out.println(s);
		}
		//System.out.println(hc.getCapabilities());
		hc.close();
	}

}
