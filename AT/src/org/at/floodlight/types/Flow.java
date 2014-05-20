package org.at.floodlight.types;

import java.util.Random;

public class Flow {
	public static short IPv4 = 0x0800;
	public static short ARP = 0x0806;
	
	public String switchDpid;
	private final int cookie = (new Random()).nextInt();
	public short etherType;
	public int priority;
	public int ingressPort;
	public boolean active = true;
	public String actions;
	
}
