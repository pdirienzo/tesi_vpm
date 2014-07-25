package it.unina.cini.platino.web.network.path;

import it.unina.cini.platino.network.types.LinkConnection;
import it.unina.cini.platino.network.types.OvsSwitch;
import it.unina.cini.platino.network.types.VPMGraph;
import it.unina.cini.platino.web.network.path.types.VPMPathInfo;
import it.unina.cini.platino.web.network.path.types.VPMSwitchInfo;

import java.io.IOException;

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
