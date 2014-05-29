package org.at.web.network.path;

import com.mxgraph.view.mxGraph;

public class VPMPathInfo {
	public mxGraph path;
	public String externalAddress;
	
	public VPMPathInfo(mxGraph path, String externalAddress){
		this.path = path;
		this.externalAddress = externalAddress;
	}
}
