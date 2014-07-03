package it.unina.cini.platino.db;

public interface DatabaseListener {
	public void hypervisorInserted(Hypervisor h);
	public void hypervisorDeleted(Hypervisor h);
}
