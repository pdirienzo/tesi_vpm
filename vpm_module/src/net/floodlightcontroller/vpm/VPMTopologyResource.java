package net.floodlightcontroller.vpm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.LinkInfo;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LinkDirection;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LinkType;
import net.floodlightcontroller.routing.Link;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class VPMTopologyResource extends ServerResource{

	@Get("json")
	public Set<VPMTopologyWithType> retrieve() {
		ILinkDiscoveryService ld = (ILinkDiscoveryService)getContext().getAttributes().
				get(ILinkDiscoveryService.class.getCanonicalName());
		IFloodlightProviderService switchService = (IFloodlightProviderService)getContext().getAttributes().
				get(IFloodlightProviderService.class.getCanonicalName());

		Map<Link, LinkInfo> links = new HashMap<Link, LinkInfo>();
		Set<VPMTopologyWithType> returnLinkSet = new HashSet<VPMTopologyWithType>();

		if (ld != null && switchService != null) {
			links.putAll(ld.getLinks());
			for (Link link: links.keySet()) {
				LinkInfo info = links.get(link);
				LinkType type = ld.getLinkType(link, info);
				if (type == LinkType.DIRECT_LINK || type == LinkType.TUNNEL) {
					VPMTopologyWithType lwt;

					long src = link.getSrc();
					String srcIp = switchService.getSwitch(src).getInetAddress().toString();
					long dst = link.getDst();
					String dstIp = switchService.getSwitch(dst).getInetAddress().toString();
					short srcPort = link.getSrcPort();
					String srcPortName = switchService.getSwitch(src).getPort(srcPort).getName();
					System.out.println("srcPortName: "+srcPortName);
					short dstPort = link.getDstPort();
					String dstPortName = switchService.getSwitch(dst).getPort(dstPort).getName();
					System.out.println("dstPortName: "+dstPortName);
					Link otherLink = new Link(dst, dstPort, src, srcPort);
					LinkInfo otherInfo = links.get(otherLink);
					
					LinkType otherType = null;
					if (otherInfo != null)
						otherType = ld.getLinkType(otherLink, otherInfo);
					if (otherType == LinkType.DIRECT_LINK ||
							otherType == LinkType.TUNNEL) {
						// This is a bi-direcitonal link.
						// It is sufficient to add only one side of it.
						if ((src < dst) || (src == dst && srcPort < dstPort)) {
								lwt = new VPMTopologyWithType(link, srcIp, dstIp,srcPortName,dstPortName,
									type,
									LinkDirection.BIDIRECTIONAL);
								returnLinkSet.add(lwt);
						}
					} else {
						// This is a unidirectional link.
						lwt = new VPMTopologyWithType(link, srcIp, dstIp,srcPortName,dstPortName,
								type,
								LinkDirection.UNIDIRECTIONAL);
						returnLinkSet.add(lwt);
					}
				}
			}
		}
		return returnLinkSet;
	}


}
