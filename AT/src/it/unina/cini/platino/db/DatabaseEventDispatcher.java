package it.unina.cini.platino.db;

import java.util.ArrayList;
import java.util.List;

/**
 * A class dispatching DB related events such as a new hypervisor insertion.
 * It is called by the Database class methods to notify any listeners.
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
public class DatabaseEventDispatcher {
	
	public enum DBEvent {hypervisor_insert,hypervisor_delete}
	
	
	private static List<DatabaseListener> listeners = new ArrayList<DatabaseListener>();


	public static void addListener(DatabaseListener listener){
		listeners.add(listener);
	}
	
	public static void removeListener(DatabaseListener listener){
		listeners.remove(listener);
	}
	
	public static void dispatchEvent(DBEvent event,Hypervisor h){
		switch(event){
		case hypervisor_insert:
			for(DatabaseListener l : listeners)
				l.hypervisorInserted(h);
			break;
		case hypervisor_delete:
			for(DatabaseListener l : listeners)
				l.hypervisorDeleted(h);
			break;
		}
	}
}
