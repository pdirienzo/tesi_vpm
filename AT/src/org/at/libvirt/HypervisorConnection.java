package org.at.libvirt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.at.db.Hypervisor;
import org.libvirt.CPUStatistic;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;
import org.libvirt.Network;

public class HypervisorConnection extends Connect{

	public static final int DEFAULT_TIMEOUT = 3000;
	public static final String TLS = "qemu";
	public static final String SSH = "qemu+ssh";
	public static final String TCP = "qemu+tcp";
	
	public static final String NET_NAME = "vpm-network";

	private static final String DEFAULT_CONN_METHOD = TLS;
	
	private Hypervisor hypervisor;
	private Network net; //custom network for our vms
	
	/**
	 * The most complete call as it allows to specify everything
	 * @param h
	 * @param method
	 * @param readOnly
	 * @throws LibvirtException
	 */
	private HypervisorConnection(Hypervisor h, String method, boolean readOnly) throws LibvirtException{
			super(method+"://"+h.getName()+"@"+h.getHostAddress()+":"+h.getPort()+
				"/system", readOnly);
			this.hypervisor = h;
	}

	/**
	 * Creates an hypervisor with default connection method (tls)
	 * @param h
	 * @param readOnly
	 * @throws LibvirtException
	 */
	private HypervisorConnection(Hypervisor h,boolean readOnly) throws LibvirtException{
		this(h,DEFAULT_CONN_METHOD,readOnly);
	}
	
	public Hypervisor getHypervisor(){
		return hypervisor;
	}
	
	/***************************** Timed functions ****************************
	 * This is just a workaround in order to prevent the libvirt Connect class
	 * to try to connect forever to a shutted down hypervisor. This class has
	 * not an embedded timeout function and the default timeout is really 
	 * loooooooooooooooooooooooooooooooooooooooooooooooooooooooong.
	 * 
	 * So, in order to solve this and allow the user to specify a custom timeout,
	 * we do this:
	 * 1-we create a simple java socket and try to connect it to the hypervisor in 
	 * order to check if it is online. Java is nice and allows to set a custom timeout.
	 * 2-if the hypervisor answers back it means it is online. We immediately close the
	 * socket and connect to the hypervisor with the Connect class. 
	 * 
	 * It is just a workaround as it opens one more connection just for checking,
	 * but real problem is in the native api calls so for an integrated timeout
	 * handling we can just wait for a new version.
	 * @author pasquale
	 *
	 */
	
	/**
	 * the dummy, additional connection
	 * @param h
	 * @param timeout timeout in millis
	 * @throws IOException when times out
	 */
	private static void checkConnection(Hypervisor h, int timeout) throws IOException{
		Socket s = new Socket();
		InetSocketAddress addr = new InetSocketAddress(
				InetAddress.getByName(h.getHostAddress()), (int)h.getPort());
		
		s.connect(addr, timeout);
		s.close();
	}

	/**
	 * returns a connection or throws ioexception if timeout expires
	 * @param h
	 * @param method
	 * @param readOnly
	 * @param timeout in millis
	 * @return
	 * @throws IOException
	 * @throws LibvirtException
	 */
	public static HypervisorConnection getConnectionWithTimeout(
			Hypervisor h, String method, boolean readOnly, int timeout) throws IOException, LibvirtException{
		
		checkConnection(h, timeout);
		return new HypervisorConnection(h,method,readOnly);
	}
	
	/**
	 * returns a connection or throws ioexception if timeout expires
	 * @param h
	 * @param readOnly
	 * @param timeout in millis
	 * @return
	 * @throws IOException
	 * @throws LibvirtException
	 */
	public static HypervisorConnection getConnectionWithTimeout(Hypervisor h,
			boolean readOnly,int timeout) throws IOException, LibvirtException{
		
		checkConnection(h, timeout);
		return new HypervisorConnection(h,readOnly);
	}

	
	//********************************* END TIMED ********************************
	
	
	public List<Domain> getAllDomains() throws LibvirtException{
		List<Domain> domains = getRunningDomains();
		domains.addAll(getShuttedDownDomains());
		
		return domains;
	}
	
	public List<Domain> getShuttedDownDomains() throws LibvirtException{
		List<Domain> domains = new ArrayList<Domain>();
		String[] names = super.listDefinedDomains();
		for(int i=0; i<names.length;i++)
			domains.add(super.domainLookupByName(names[i]));
		
		return domains;
	}
	
	public long getMemUsage(String domainName) throws LibvirtException{
		Domain d = domainLookupByName(domainName);
		DomainInfo infos = d.getInfo();
		System.out.println(infos.memory+"/"+infos.maxMem);
		float perc = 100* ((float)infos.memory/(float)infos.maxMem);
		
		return (long)perc;
	}

	public List<Domain> getRunningDomains() throws LibvirtException{
		List<Domain> domains = new ArrayList<Domain>();

		//getting running domains...
		int[] ids = super.listDomains();
		for(int i = 0;i<ids.length;i++)
			domains.add(super.domainLookupByID(ids[i]));

		return domains;
	}
	
	// ********************* managment apis ***************************
	public void bootDomain(String name) throws LibvirtException{
		Domain d = domainLookupByName(name);
		d.create();
	}
	
	/**
	 * Creates a new network from an xmlfile. This will just create but not define it
	 * @param xmlFilePath
	 * @return
	 * @throws LibvirtException
	 * @throws IOException
	 */
	public Network createNetworkFromFile(String xmlFilePath) throws LibvirtException, IOException{
		try(BufferedReader reader = new BufferedReader(new FileReader(new File(xmlFilePath)))){
			StringBuilder xmlDescr = new StringBuilder();
			String read = null;
			while((read =reader.readLine())!=null)
				xmlDescr.append(read);
			
			if((net=super.networkLookupByName(NET_NAME)) == null)
				net = super.networkCreateXML(xmlDescr.toString());
		}
		
		return net;
	}
	
	/**
	 * This shutdowns our custom network
	 * @throws LibvirtException
	 */
	public void networkShutdown() throws LibvirtException{
		if(net != null)
			net.destroy();
	}
	
	public void shutdownDomain(String name) throws LibvirtException{
		Domain d = domainLookupByName(name);
		//d.shutdown();
		d.destroy();
	}
	
	public CPUStatistic[] getCpuStatistics() throws LibvirtException{
		return super.getCPUStats(-1, 0);
	}

	/**
	 * Migreates a domain belonging to this hypervisor to selected hypervisor
	 * 
	 * @param domainName
	 * @param destConn
	 * @throws LibvirtException 
	 * @throws IOException 
	 */
	public boolean migrate(String domainName,Hypervisor destination) throws LibvirtException, IOException{
		HypervisorConnection destConn = 
				HypervisorConnection.getConnectionWithTimeout(destination, false,
						HypervisorConnection.DEFAULT_TIMEOUT);
		Domain domain = super.domainLookupByName(domainName);
		Domain newDomain = null;
		newDomain = domain.migrate(destConn, 1, null, null, 0);
		destConn.close();
		return (newDomain != null);
	}
	

}
