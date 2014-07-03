package it.unina.cini.platino.db;

public class VolumeAllocation {
	public int id;
	public int iscsiID;
	public String volume;
	public int hostID;
	public String vmName;
	
	public VolumeAllocation(int id, int iscsiID, String volume, int hostID, String vmID){
		this.iscsiID = iscsiID;
		this.volume = volume;
		this.hostID = hostID;
		this.vmName = vmID;
	}
	
	public VolumeAllocation(int iscsiID, String volume, int hostID, String vmID){
		this(0, iscsiID, volume, hostID, vmID);
	}
	
	
}
