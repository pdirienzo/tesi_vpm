package org.at.connections;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.at.db.Database;
import org.at.db.DatabaseEventDispatcher;

public class LimeContextServerListener implements ServletContextListener {

	private static final int MANAGER_RETRY_TIME = 5000;
	private static final String JSP_URL = "http://localhost:8081/AT/prova.jsp?jsp_precompile=true";	
	
	@Override
	public void contextInitialized(ServletContextEvent c) {
		System.out.println("this game will be started");
		
		//Initializing db if this is the first app start
		try {
			Database.initialize(Database.DEFAULT_DBPATH);
			HypervisorConnectionManager manager = new HypervisorConnectionManager(MANAGER_RETRY_TIME);
			DatabaseEventDispatcher.addListener(manager);
			manager.start();
			c.getServletContext().setAttribute(HypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER, manager);
			preCompileSettingsJsp();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void preCompileSettingsJsp() throws IOException{
		URL url = new URL(JSP_URL);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("GET");
		conn.connect();
		System.out.println("Jsp compiled");
	} 
	
	@Override
	public void contextDestroyed(ServletContextEvent c) {
		System.out.println("destroyed");
		HypervisorConnectionManager manager = (HypervisorConnectionManager)c.getServletContext()
				.getAttribute(HypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER);
		try {
			manager.stop();
			System.out.println("DB connection closed");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	

}
