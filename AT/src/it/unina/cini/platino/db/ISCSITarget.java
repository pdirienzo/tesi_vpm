package it.unina.cini.platino.db;

/**
 * An class representing an ISCSITarget row in the Database.
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
public class ISCSITarget {
	public int id;
	public String name;
	public String hostname;
	public String iqn;
	public int port;
	
	
	public ISCSITarget(int id, String name, String hostname, int port, String iqn){
		this.id = id;
		this.name = name;
		this.hostname = hostname;
		this.iqn = iqn;
		this.port = port;
	}
}
