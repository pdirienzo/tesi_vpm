package it.unina.cini.platino.monitoring;

import java.io.IOException;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class LibvirtGuestStatus implements NodeStatus{

	private Domain domain;
	
	public LibvirtGuestStatus(Domain domain){
		this.domain = domain;
	}

	@Override
	public float getOverallCPUUsage(int measurementInterval) throws IOException {
		// TODO Auto-generated method stub
		try {
			return domain.getCPUOverallUsage(measurementInterval);
		} catch (LibvirtException e) {
			throw new IOException(e);
		}
	}
}
