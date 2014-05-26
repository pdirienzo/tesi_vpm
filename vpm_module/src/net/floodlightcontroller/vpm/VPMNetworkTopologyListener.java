package net.floodlightcontroller.vpm;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.container.osgi.NettyBundleActivator;
import org.openflow.util.HexString;
import org.restlet.engine.adapter.HttpRequest;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;

public class VPMNetworkTopologyListener implements ILinkDiscoveryListener {

	private IFloodlightProviderService ifps = null;
	public VPMNetworkTopologyListener(
			IFloodlightProviderService floodlightProvider) {
		// TODO Auto-generated constructor stub
		this.ifps= floodlightProvider;
	}

	@Override
	public void linkDiscoveryUpdate(LDUpdate update) {
		// TODO Auto-generated method stub
		if (update.getOperation() == UpdateOperation.LINK_REMOVED){
		
			System.out.println("LDUpdate single one "+update.toString());
		}
		
	}
	
	public List<LDUpdate> findDuplicate (List<LDUpdate> lupd){
		List<LDUpdate> ld = new ArrayList<LDUpdate>();
		while (lupd.size()>0){
			LDUpdate l = lupd.remove(0);
			for (LDUpdate link : lupd){
			
				if (l.getSrc() == link.getDst() && l.getSrcPort() == link.getDstPort() &&
						l.getDst() == link.getSrc() && l.getDstPort() == link.getSrcPort()){
					lupd.remove(link);
					break;
				}
			
			}
			ld.add(l);
		}
		return ld;
	}

	@Override
	public void linkDiscoveryUpdate(List<LDUpdate> updateList) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		//updateList = findDuplicate(updateList);
		sb.append("{ \"result\": [");
		for (LDUpdate upd : updateList){
			if (upd.getOperation() == UpdateOperation.LINK_REMOVED){
				sb.append("{");
				String srcIP= ifps.getSwitch(upd.getSrc()).getInetAddress().toString();
				String dstIP= ifps.getSwitch(upd.getDst()).getInetAddress().toString();
				String srcPortName= ifps.getSwitch(upd.getSrc()).getPort(upd.getSrcPort()).getName();
				String dstPortName= ifps.getSwitch(upd.getDst()).getPort(upd.getDstPort()).getName();
				srcPortName = srcPortName + "/" + upd.getSrcPort();
				dstPortName = dstPortName + "/" + upd.getDstPort();
				String dstDpid = HexString.toHexString(upd.getDst());
				String srcDpid = HexString.toHexString(upd.getSrc());
				sb.append("\"src-ip\":\""+srcIP+"\",");
				sb.append("\"dst-ip\":\""+dstIP+"\",");
				sb.append("\"src-port\":\""+srcPortName+"\",");
				sb.append("\"dst-port\":\""+dstPortName+"\",");
				sb.append("\"src-dpid\":\""+srcDpid+"\",");
				sb.append("\"dst-dpid\":\""+dstDpid+"\"");
				sb.append("}");
			}
		}
		sb.append("]}");
		 
		System.out.println("LDUPDATE: "+sb);
		
	}

}
