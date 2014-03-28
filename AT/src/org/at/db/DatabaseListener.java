package org.at.db;

public interface DatabaseListener {
	public void hypervisorInserted(Hypervisor h);
	public void hypervisorDeleted(Hypervisor h);
}
