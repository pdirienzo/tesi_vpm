package org.at.connections;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class LimeContextServerListener implements ServletContextListener {

	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("this game will be started");
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("destroyed");
		
	}

	

}
