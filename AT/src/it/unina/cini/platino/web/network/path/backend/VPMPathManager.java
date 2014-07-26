package it.unina.cini.platino.web.network.path.backend;

import it.unina.cini.platino.network.types.LinkConnection;
import it.unina.cini.platino.network.types.OvsSwitch;
import it.unina.cini.platino.network.types.VPMGraph;

import java.io.IOException;

/**
 * Interface which should be implemented by any class wanting to implement flow
 * installations among switches. Once you do your nicely custom PathManager implementation
 * you will have to register it in the VPM property file.
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
public interface VPMPathManager {
	
	public static final String VPM_PATH_MANAGER = "PathManager";
	public static final String PASSBY_FLOW = "RTP_PASSTHROUGH";
	public static final String TO_VNET_FLOW = "RTP_TOVNETS";
	public static final String FROM_VNET_FLOW = "RTP_FROMVNET";

	public VPMPathInfo installShortestPath(VPMGraph<OvsSwitch, LinkConnection> graph, 
			OvsSwitch src, OvsSwitch dest, String external, String portPrefix) throws IOException;
	
	public void uninstallPath(String rootDpid, String leafDpid) throws IOException;
	
	/**
	 * Returns the infos associated to the switch which has the dpid passed as parameter.
	 * 
	 * @param dpid
	 * @return the switchinfo structure associated to that switch or null if the latter is not present
	 */
	public VPMSwitchInfo getSwitchInfos(String dpid);
	
	/**
	 * Returns a PathInfo instance if path exists, null otherwise
	 * @param rootDpid
	 * @param leafDpid
	 * @return
	 */
	public VPMPathInfo getPath(String rootDpid, String leafDpid);
	
	/**
	 * Returns a PathInfo instance for a path ending on the specified leaf, null otherwise
	 * @param leafDpid
	 * @return
	 */
	public VPMPathInfo getPath(String leafDpid);
}
