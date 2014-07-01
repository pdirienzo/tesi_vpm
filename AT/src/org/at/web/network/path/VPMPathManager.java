package org.at.web.network.path;

import java.io.IOException;

import org.at.network.types.LinkConnection;
import org.at.network.types.OvsSwitch;
import org.at.network.types.VPMGraph;
import org.at.web.network.path.types.VPMPathInfo;
import org.at.web.network.path.types.VPMSwitchInfo;

public interface VPMPathManager {
	
	public static final String VPM_PATH_MANAGER = "PathManager";
	public static final String PASSBY_FLOW = "RTP_PASSTHROUGH";
	public static final String TO_VNET_FLOW = "RTP_TOVNETS";
	public static final String FROM_VNET_FLOW = "RTP_FROMVNET";

	public VPMPathInfo installShortestPath(VPMGraph<OvsSwitch, LinkConnection> graph, 
			OvsSwitch src, OvsSwitch dest, String external) throws IOException;
	
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
