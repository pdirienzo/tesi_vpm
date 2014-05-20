package net.floodlightcontroller.vpm;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

public class VPMTopologyWebRoutable implements RestletRoutable{

	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
		
		router.attach("/links/json",VPMTopologyResource.class);
		router.attach("/portInfo/json",VPMTopologyGetPortResource.class);
		return router;
	}

	@Override
	public String basePath() {
		return "/vpm/topology";
	}

}