package it.unina.cini.platino.web.network.path.types;

import it.unina.cini.platino.network.types.LinkConnection;
import it.unina.cini.platino.network.types.OvsSwitch;

import org.jgrapht.GraphPath;

public class VPMPathInfo {
	public GraphPath<OvsSwitch, LinkConnection> path;
	public String externalAddress;
	public String portPrefix;
	
	public VPMPathInfo(GraphPath<OvsSwitch, LinkConnection> path, String externalAddress, String portPrefix){
		this.path = path;
		this.externalAddress = externalAddress;
		this.portPrefix = portPrefix;
	}
}
