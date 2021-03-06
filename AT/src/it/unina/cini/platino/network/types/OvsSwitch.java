package it.unina.cini.platino.network.types;

/**
 * A class representing a VPM node
 * 
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 */
public class OvsSwitch {
	
	public static enum Type {
		ROOT(0),RELAY(1),LEAF(2),NULL(3);
		
		private int value;
		
		Type(int value){
			this.value = value;
		}
		
		public int getValue(){
			return value;
		}
		
		public String toString(){
			String str = null;
			
			switch(value){
			case 0:
				str = "ROOT";
				break;
			case 1:
				str = "RELAY";
				break;
			case 2:
				str = "LEAF";
				break;
			case 3:
				str = "NULL";
				break;
			}
			
			return str;
		}
	}
	
	public String ip;
	public String dpid;
	public Type type;
	
	public OvsSwitch(String dpid, String ip){
		this(dpid,ip,Type.NULL);
	}
	
	public OvsSwitch(String dpid,String ip, Type type){
		this.ip = ip;
		this.dpid = dpid;
		this.type = type;
	}
	
	public String toString(){
		return "ovs "+dpid+" /"+ip;
	}
	
	public boolean equals(Object o){
		OvsSwitch s = (OvsSwitch)o;
		return /*(s.ip.equals(this.ip)) &&*/ (s.dpid.equals(this.dpid));
	}
	
	@Override
	public int hashCode() {
		return (/*this.ip+*/this.dpid).hashCode();
	}
	
	public static void main(String[] args){
		System.out.println(OvsSwitch.Type.ROOT.name());
	}
}
