package org.at.db;

public class VolumeAllocation {
	public int id;
	public int iscsiID;
	public String volume;
	public int hostID;
	public int vmID;
	
	public VolumeAllocation(int id, int iscsiID, String volume, int hostID, int vmID){
		this.iscsiID = iscsiID;
		this.volume = volume;
		this.hostID = hostID;
		this.vmID = vmID;
	}
	
	public VolumeAllocation(int iscsiID, String volume, int hostID, int vmID){
		this(0, iscsiID, volume, hostID, vmID);
	}
	
	
}
