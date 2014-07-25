package net.floodlightcontroller.vpm.json;

import com.fasterxml.jackson.annotation.JsonProperty;


public class VPMEventForwarding {

	public String type;
	@JsonProperty("switch")
	public String switchid;
	public String vnet;
	public String op;
	
	public VPMEventForwarding(String type, String sid, String vnet, String op){
		
		this.op = op;
		this.type=type;
		this.switchid = sid;
		this.vnet=vnet;
		
	}
	
	@Override
	public String toString() {
		return "Event [type=" + this.type + ", switchid=" + this.switchid + ", " +
				"vnet=" + this.vnet + ", op = "+ this.op +"]";
	}
}
