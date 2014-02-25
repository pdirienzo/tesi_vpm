package org.at.libvirt;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.at.db.Database;
import org.at.db.Hypervisor;
import org.at.floodlight.FlowsController;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainJobInfo;
import org.libvirt.LibvirtException;

public class LiveMigration {
	private static final AtomicInteger fid = new AtomicInteger(0);
	
	private final FlowsController fc;
	
	private String vmname;
	
	private String srcmac;
	private String srcip;
	private String dstip;
	
	private String dpidSrc;
	private String portSrc;
	private String portTunSrc;
	
	private String dpidDst;
	private String portDst;
	private String portTunDst;

	private int[] flowsId = new int[2];
	
	private Connect srcHypervisor;
	private Connect dstHypervisor;
	private Domain  domain;
	
	private long startTime = 0l;
	private long endTime   = 0l;
	
	/* Status:
	 * 2 - Finish
	 * 1 - Running
	 * 0 - Idle
	 *-1 - Error
	 */
	private int  status    = 0;
	
	public LiveMigration(String vmname, String srcip, String dstip) throws IOException {
		this.fc = new FlowsController();
		this.vmname = vmname;
		this.srcip  = srcip;
		this.dstip  = dstip;
		this.srcmac = Libvirt.getMacAddressByName(vmname);
	}
	
	public void migrate() throws IOException {
	/*	Database d = new Database();
		d.connect();
		
		Hypervisor src = d.getHypervisor(srcip);
		Hypervisor dst = d.getHypervisor(dstip);
		d.close();
		
		srcHypervisor = Libvirt.getHypervisorConnection(src);
		dstHypervisor = Libvirt.getHypervisorConnection(dst);
		
		try {
			domain = srcHypervisor.domainLookupByName(vmname);
		} catch (LibvirtException e) {
			e.printStackTrace();
		}
		
		startTime = System.currentTimeMillis();
		//START MIGRATE THREAD
		try {
			new MigrationThread().start();
			status = 1;
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		//SETUP NETWORK
		setupNetworkMigration();
	}
	
	public long getElapsedTime() {
		return endTime - startTime;
	}
	
	public long[] getDomainJobInfo() {
		/*
		 * Returns [Processed, Remains, Total] Memory
		 */
		try {
			DomainJobInfo i = domain.getJobInfo();
			long[] stats = {i.getMemProcessed(),i.getMemRemaining(), i.getMemTotal()};
			return stats;
		} catch (LibvirtException e) {
			e.printStackTrace();
			long[] stats = {1,1,1};
			return stats;
		}
	}
	
	public int getState() {
		return status;
	}
	
	private void setupNetworkMigration() {
		dpidSrc = fc.querySwitchIdByIp(srcip);
		portSrc = fc.queryPortByMac(dpidSrc, srcmac);
		portTunSrc = fc.queryPortByName(dpidSrc, "gre0");
		
		dpidDst = fc.querySwitchIdByIp(dstip);
		portDst = fc.queryPortByMac(dpidDst, srcmac);
		portTunDst = fc.queryPortByName(dpidDst, "gre0");
		
		flowsId[0] = fid.getAndAdd(1);
		fc.addFlow(dpidSrc, "flow-"+String.valueOf(flowsId[0]), srcmac, 
				"output="+portTunSrc+",output="+portSrc);
		
		flowsId[1] = fid.getAndAdd(1);
		fc.addFlow(dpidDst, "flow-"+String.valueOf(flowsId[1]), srcmac,
				"output="+portTunDst+",output="+portDst);
	}
	
	private void finalizeNetworkMigration() {
		fc.removeFlow(dpidSrc, "flow-"+String.valueOf(flowsId[0]), srcmac, 
				"output="+portTunSrc+",output="+portSrc);
		
		fc.removeFlow(dpidDst, "flow-"+String.valueOf(flowsId[1]), srcmac,
				"output="+portTunDst+",output="+portDst);
	}
	
	private class MigrationThread extends Thread {
		
		public void run() {
			domain = Libvirt.migrate(domain, dstHypervisor);
			endTime = System.currentTimeMillis();
			if(domain==null)
				status = -1;
			else
				status = 2;
			
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			finalizeNetworkMigration();
			
			try {
				srcHypervisor.close();
				dstHypervisor.close();
			} catch (LibvirtException e) {
				e.printStackTrace();
			}
		}
	}
	
}
