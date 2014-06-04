package org.at.web.network.path.types;

import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.jgrapht.GraphPath;

public class VPMPathInfo {
	public GraphPath<OvsSwitch, LinkConnection> path;
	public String externalAddress;
	
	public VPMPathInfo(GraphPath<OvsSwitch, LinkConnection> path, String externalAddress){
		this.path = path;
		this.externalAddress = externalAddress;
	}
}
