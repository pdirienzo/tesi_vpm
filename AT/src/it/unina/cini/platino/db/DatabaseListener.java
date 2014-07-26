package it.unina.cini.platino.db;

/**
 * An interface to be implemented by any class which has interest to be notified
 * about DB events.
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
public interface DatabaseListener {
	public void hypervisorInserted(Hypervisor h);
	public void hypervisorDeleted(Hypervisor h);
}
