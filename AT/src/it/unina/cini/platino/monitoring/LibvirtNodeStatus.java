package it.unina.cini.platino.monitoring;

import java.io.IOException;

import org.libvirt.LibvirtException;

import it.unina.cini.platino.libvirt.HypervisorConnection;

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
