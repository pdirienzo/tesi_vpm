package it.unina.cini.platino.tries;

import it.unina.cini.platino.db.Hypervisor;
import it.unina.cini.platino.libvirt.HypervisorConnection;

import java.io.IOException;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.MemoryStatistic;

public class TryMemStats {

	
	public static void main(String[] args) throws IOException, LibvirtException {
		HypervisorConnection hc = HypervisorConnection.getConnectionWithTimeout(
				new Hypervisor("pasquale", "pasquale-VPCEB1A4E",
						16514), true, 3000);
		int max = 1;
		
		for(int i=0;i< max;i++){
			Domain d = hc.domainLookupByName("linx2");
			MemoryStatistic[] memStats =d.memoryStats(1);
			
			for(int j=0;j<memStats.length;j++)
				System.out.println("available: "
						+memStats[j].getTag()
						+"/"+memStats[j].getValue());
			
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		hc.close();
		
		System.out.print("see ya");

	}

}
