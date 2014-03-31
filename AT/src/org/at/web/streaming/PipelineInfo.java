package org.at.web.streaming;

import java.util.ArrayList;

public class PipelineInfo {

	
	String id;

	ArrayList<PipelineDestinationInfo> destinations;

	public PipelineInfo(String id,ArrayList<PipelineDestinationInfo> destinations) {
		super();
		this.id = id;
		this.destinations=new ArrayList<PipelineDestinationInfo>();
		if (destinations!=null) this.destinations.addAll(destinations);
	}

	public PipelineInfo() {
		super();
	}

	public String getId() {
		return id;
	}

	public ArrayList<PipelineDestinationInfo> getDestinations() {
		return destinations;
	}

	
}

