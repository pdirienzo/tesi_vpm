package net.floodlightcontroller.vpm;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.util.AppCookie;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFType;
import org.openflow.util.HexString;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class VPMForwardingResource extends ServerResource {

	private IFloodlightProviderService floodlight;
	private IStaticFlowEntryPusherService pusher;
	
	@Override
	public void init(Context arg0, Request arg1, Response arg2) {
		super.init(arg0, arg1, arg2);
		floodlight = (IFloodlightProviderService)getContext().getAttributes().get(IFloodlightProviderService.class.getCanonicalName());
		pusher = (IStaticFlowEntryPusherService)getContext().getAttributes().get(IStaticFlowEntryPusherService.class.getCanonicalName());
	}
	
	@Get("reset")
	public String reset(){
		pusher.deleteAllFlows(); //deleting all flows
		
		//now adding the patch blocking flow rule
		for(Long dpid : floodlight.getAllSwitchDpids()){
			OFMatch match = new OFMatch();
			match.setInputPort(floodlight.getSwitch(dpid).getPort("patch1").getPortNumber());
			OFFlowMod fm = (OFFlowMod) floodlight.getOFMessageFactory().getMessage(OFType.FLOW_MOD);
			fm.setCookie(AppCookie.makeCookie(VPMForwarding.FORWARDING_APP_ID, 0))
			.setPriority((short)25)
			.setMatch(match);
			
			pusher.addFlow("BLOCK"+dpid, fm, HexString.toHexString(dpid));
		}
		
		return "{\"status\":\"ok\"}";
	}
}
