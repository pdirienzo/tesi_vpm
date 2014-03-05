package org.at.tries;

import java.io.IOException;

import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.libvirt.Connect;
import org.libvirt.ConnectAuth;
import org.libvirt.ConnectAuthDefault;
import org.libvirt.LibvirtException;

public class TryLibvirt{

	public static void main(String[] args) throws LibvirtException, IOException {
		Hypervisor h = new Hypervisor("pasquale", "143.225.229.197", 16509);
		HypervisorConnection c = HypervisorConnection.getConnectionWithTimeout(h, false,
				3000);
		
		c.close();
	}

}
