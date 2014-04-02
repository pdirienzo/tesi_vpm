package org.at.floodlight.types;

public class OvsSwitch {
	public String ip;
	public String dpid;
	
	public OvsSwitch(String dpid,String ip){
		this.ip = ip;
		this.dpid = dpid;
	}
	
	public String toString(){
		return "ovs "+dpid+" /"+ip;
	}
	
	public boolean equals(Object o){
		OvsSwitch s = (OvsSwitch)o;
		return (s.ip.equals(this.ip)) && (s.dpid.equals(this.dpid));
	}
}
