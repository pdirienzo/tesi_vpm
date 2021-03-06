package it.unina.cini.platino.tests;

import it.unina.cini.platino.db.Hypervisor;
import it.unina.cini.platino.libvirt.MigrationThread;

import java.io.IOException;

import org.libvirt.LibvirtException;

public class TryMigrationThread{

	public static void main(String[] args) throws LibvirtException, IOException {
		Hypervisor h1 = new Hypervisor("pasquale", "pasquale-VPCEB1A4E", 16514);
		Hypervisor h2 = new Hypervisor("sprom", "sprom-Dell", 16514);
		
		
		System.out.println("we are starting");
		MigrationThread mt = null; /*new MigrationThread(h1, h2, "linx2", "br0", 6640);*/
		mt.start();
		
		while (mt.getMigrationStatus() == MigrationThread.MIGRATION_IDLE)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		
		
		while(mt.getMigrationStatus() == MigrationThread.MIGRATION_PROGRESS){	
		try {
			Thread.sleep(500);
			long total = mt.getTotalMb();
			long processed = mt.getProcessedMb();
			if(total != 0){
				System.out.println("total: "+total);
				float diff = total-mt.getRemainingMb();
				float percent = 100* (diff/(float)total);
				System.out.println(percent+"% Remaining: "+ mt.getRemainingMb());
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		/*HypervisorConnection c1 = HypervisorConnection.getConnectionWithTimeout(h1, false,
				3000);
		HypervisorConnection c2 = HypervisorConnection.getConnectionWithTimeout(h2, false,
				3000);
		
		
		if(c2.migrate("linx2", h1)){
			System.out.println("ok");
		}
		
		c1.close();
		c2.close();*/
		System.out.println("Done");
	}

}
