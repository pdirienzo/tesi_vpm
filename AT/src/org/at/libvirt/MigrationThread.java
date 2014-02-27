package org.at.libvirt;

import org.at.db.Hypervisor;
import org.libvirt.Domain;
import org.libvirt.DomainJobInfo;
import org.libvirt.LibvirtException;

public class MigrationThread extends Thread {
	
	public static final int MIGRATION_IDLE = 0;
	public static final int MIGRATION_PROGRESS = 1;
	public static final int MIGRATION_SUCCESS = 2;
	public static final int MIGRATION_FAIL = -1;
	
	private int status;
	
	private Hypervisor srcH;
	HypervisorConnection conn;
	private Hypervisor dstH;
	private String domainName;
	private Domain inMigrationDomain;
	
	private long startTime;
	private long elapsedTime;
	
	public MigrationThread(Hypervisor src,Hypervisor dst,String domainName){
		setMigrationStatus(MIGRATION_IDLE);
		this.srcH = src;
		this.dstH = dst;
		this.domainName = domainName;
		this.elapsedTime = -1; //value to say that migration is still in progress
	}
	
	public void run(){
		try {
			conn = new HypervisorConnection(srcH);
			setMigrationStatus(MIGRATION_PROGRESS);
			startTime = System.currentTimeMillis();//setting start time
			inMigrationDomain = conn.domainLookupByName(domainName);
			if(conn.migrate(domainName, dstH))
				setMigrationStatus(MIGRATION_SUCCESS);
			else
				setMigrationStatus(MIGRATION_FAIL);
			
			elapsedTime = System.currentTimeMillis() - startTime;
			
			conn.close();
		} catch (LibvirtException e) {
			e.printStackTrace();
			setMigrationStatus(MIGRATION_FAIL);
		}
	}
	
	private synchronized void setMigrationStatus(int newStatus){
		status = newStatus;
	}
	
	/**
	 * Returns the current status of this migration. Use MigrationThread
	 * constants to parse it
	 * @return
	 */
	public synchronized int getMigrationStatus(){
		return status;
	}
	
	/**
	 * returns the job stats about this migration. Use it when migration is in
	 * progress. This method returns null if migration is not in progress so
	 * always check the result and call getMigrationStatus method to get additional
	 * informations
	 * @return
	 * @throws LibvirtException
	 */
	public synchronized DomainJobInfo getJobStats(){
		DomainJobInfo infos = null;
		
		if(getMigrationStatus() == MIGRATION_PROGRESS)
			try {
				infos = inMigrationDomain.getJobInfo();//conn.domainLookupByName(domainName).getJobInfo();
			} catch (LibvirtException e) {
			}
		
		return infos;
	}
	
	/**
	 * Return the time this migration needed to complete. If migration is in progress
	 * it returns the time from the start.
	 * @return
	 */
	public synchronized long getElapsedTime(){
		if(elapsedTime != -1)
			return elapsedTime;
		else 
			return System.currentTimeMillis() - startTime;
	}

}
