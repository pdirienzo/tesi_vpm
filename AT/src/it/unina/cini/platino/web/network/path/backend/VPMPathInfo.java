package it.unina.cini.platino.web.network.path.backend;

import it.unina.cini.platino.network.types.LinkConnection;
import it.unina.cini.platino.network.types.OvsSwitch;

import org.jgrapht.GraphPath;

/**
 * A data structure holding information about a specific path.
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
