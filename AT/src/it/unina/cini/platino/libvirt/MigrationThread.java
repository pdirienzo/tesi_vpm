package it.unina.cini.platino.libvirt;

import it.unina.cini.platino.db.Hypervisor;
import it.unina.cini.platino.network.types.LinkConnection;
import it.unina.cini.platino.network.types.OvsSwitch;
import it.unina.cini.platino.network.types.VPMGraph;
import it.unina.cini.platino.network.types.VPMGraphHolder;
import it.unina.cini.platino.web.network.path.DefaultVPMPathManager;
import it.unina.cini.platino.web.network.path.types.VPMPathInfo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.libvirt.Domain;
import org.libvirt.DomainJobInfo;
import org.libvirt.LibvirtException;
import org.opendaylight.ovsdb.lib.standalone.DefaultOvsdbClient;
import org.opendaylight.ovsdb.lib.standalone.OvsdbException;

public class MigrationThread extends Thread {

	public static final int MIGRATION_IDLE = 0;
	public static final int MIGRATION_PROGRESS = 1;
	public static final int MIGRATION_SUCCESS = 2;
	public static final int MIGRATION_FAIL = -1;

	private int status;

	private Hypervisor src;
	private Hypervisor dstH;
	private String domainName;
	private Domain inMigrationDomain;

	private ServletContext ctx;

	private long startTime;
	private long elapsedTime;

	private String errorMessage;

	//TODO stupid workaround
	private OvsSwitch findOriginal(VPMGraph<OvsSwitch, LinkConnection> graph, OvsSwitch ovs){
		OvsSwitch found = null;

		Iterator<OvsSwitch> ves = graph.vertexSet().iterator();
		while((found == null) && ves.hasNext()){
			OvsSwitch temp = ves.next();
			if(temp.dpid.equals(ovs.dpid))
				found = temp;
		}

		return found;
	}

	public MigrationThread(Hypervisor src, Hypervisor dst, String domainName, ServletContext ctx){
		setMigrationStatus(MIGRATION_IDLE);
		this.src = src;
		this.dstH = dst;
		this.ctx = ctx;
		this.domainName = domainName;
		this.elapsedTime = -1; //value to say that migration is still in progress
		this.errorMessage = "none";
	}

	public void run(){
		HypervisorConnection srcConn = null;
		try {
			srcConn = HypervisorConnection.getConnectionWithTimeout(src, false, 3000);
			inMigrationDomain = srcConn.domainLookupByName(domainName);

			startTime = System.currentTimeMillis();//setting start time
			setMigrationStatus(MIGRATION_PROGRESS);

			Properties props = (Properties)ctx.getAttribute("properties");

			//getting dpids
			DefaultOvsdbClient ovsSrc = new DefaultOvsdbClient(src.getHostname(), Integer.parseInt(props.getProperty("ovs_manager_port")));
			String srcDpid = ovsSrc.getBridgeDpid(ovsSrc.getOvsdbNames()[0], props.getProperty("bridge_name"));

			DefaultVPMPathManager pathManager = (DefaultVPMPathManager)ctx.getAttribute(DefaultVPMPathManager.VPM_PATH_MANAGER);
			//adding a new path for the destination
			synchronized (pathManager) {
				VPMPathInfo existingPath = pathManager.getPath(srcDpid);
				VPMPathInfo newPath = null;
				
				if(existingPath != null){
					System.out.println("A path exists, creating a new path...");
					
					VPMGraph<OvsSwitch, LinkConnection> graph = ((VPMGraphHolder)ctx.getAttribute(VPMGraphHolder.VPM_GRAPH_HOLDER)).getGraph();
					//existing path found, we replicate if on the destination 

					DefaultOvsdbClient ovsDst = new DefaultOvsdbClient(dstH.getHostname(), Integer.parseInt(props.getProperty("ovs_manager_port")));
					String dstDpid = ovsDst.getBridgeDpid(ovsDst.getOvsdbNames()[0], props.getProperty("bridge_name"));

					newPath = pathManager.installShortestPath(
							graph, 
							findOriginal(graph, existingPath.path.getStartVertex()), 
							findOriginal(graph, new OvsSwitch(dstDpid,dstH.getHostname())), 
							existingPath.externalAddress);
						
				}
				
				if(srcConn.migrate(domainName, dstH))
					setMigrationStatus(MIGRATION_SUCCESS);
				else
					setMigrationStatus(MIGRATION_FAIL);
				
				if(existingPath != null){
					if(getMigrationStatus() == MIGRATION_SUCCESS){
						//delete previous path 
						System.out.println("Migration successful, deleting old path...");
						
						pathManager.uninstallPath(existingPath.path.getStartVertex().dpid, existingPath.path.getEndVertex().dpid);
						
					}else{
						//delete created path
						System.out.println("Migration failed, deleting new path...");
						pathManager.uninstallPath(newPath.path.getStartVertex().dpid, newPath.path.getEndVertex().dpid);
					}
				}
			}	
			//

			

			elapsedTime = System.currentTimeMillis() - startTime;

		} catch (LibvirtException e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			setMigrationStatus(MIGRATION_FAIL);
		} catch(IOException | OvsdbException e){
			e.printStackTrace();
			errorMessage = e.getMessage();
			setMigrationStatus(MIGRATION_FAIL);
		}finally{
			if(srcConn != null)
				try {
					srcConn.close();
				} catch (LibvirtException e) {
					e.printStackTrace();
				}
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
				infos = inMigrationDomain.getJobInfo();
			} catch (LibvirtException e) {
				//e.printStackTrace();
			}

		return infos;
	}

	public synchronized long getRemainingMb(){
		DomainJobInfo stats = getJobStats();
		if(stats != null)
			return stats.getMemRemaining();
		else
			return -1;
	}

	public synchronized long getTotalMb(){
		DomainJobInfo stats = getJobStats();
		if(stats != null)
			return stats.getMemTotal()/1024/1024;
		else
			return -1;
	}

	public synchronized long getProcessedMb(){
		DomainJobInfo stats = getJobStats();
		if(stats != null)
			return stats.getMemProcessed()/1024/1024;
		else
			return -1;
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

	public String getErrorMessage(){
		if(getMigrationStatus() == MigrationThread.MIGRATION_FAIL){
			return errorMessage;
		}else
			return "None";
	}

}
