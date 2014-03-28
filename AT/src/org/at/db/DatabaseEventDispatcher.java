package org.at.db;

import java.util.ArrayList;
import java.util.List;

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
