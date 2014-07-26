package it.unina.cini.platino.monitoring;

import java.io.IOException;

import org.libvirt.LibvirtException;

import it.unina.cini.platino.libvirt.HypervisorConnection;

/**
 * An implementation of NodeStatus interface making use of Libvirt's APIs
 * 
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
public class LibvirtNodeStatus implements NodeStatus{

	private HypervisorConnection conn;
	
	public LibvirtNodeStatus(HypervisorConnection conn){
		this.conn = conn;
	}

	@Override
	public float getOverallCPUUsage(int measurementInterval) throws IOException {
		// TODO Auto-generated method stub
		try {
			return conn.getCPUOverallUsage(measurementInterval);
		} catch (LibvirtException e) {
			throw new IOException(e);
		}
	}
}
