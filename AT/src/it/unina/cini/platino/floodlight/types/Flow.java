package it.unina.cini.platino.floodlight.types;

import java.util.Random;

/**
 * An class representing a Floodlight flow
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
