package it.unina.cini.platino.db;

/**
 * An class representing an Volume Allocation row in the Database.
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 */
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
