package org.at.tries;

import java.io.IOException;

import org.at.db.Hypervisor;
import org.at.libvirt.HypervisorConnection;
import org.libvirt.LibvirtException;

public class TryLibvirt {

	public static void main(String[] args) throws LibvirtException {
		Hypervisor h = new Hypervisor("pasquale", "127.0.0.1", 22);
		try {
			HypervisorConnection c = HypervisorConnection.getConnectionWithTimeout(
					h,true,
					3000);
			System.out.println("everything is ok");
			c.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
