package net.floodlightcontroller.vpm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VPMTopologyManager implements IFloodlightModule {

	private IRestApiService restAPI;
	private Logger logger;
	
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		
		Collection<Class<? extends IFloodlightService>> dep =
				new ArrayList<Class<? extends IFloodlightService>>();
		
		dep.add(IRestApiService.class);
		return dep;
	
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		
		logger = LoggerFactory.getLogger(VPMTopologyManager.class);
		restAPI = context.getServiceImpl(IRestApiService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		
		restAPI.addRestletRoutable(new VPMTopologyWebRoutable());
		logger.info("Rest APIs added");
		
	}

}
