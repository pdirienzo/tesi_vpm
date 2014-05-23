package org.at.connections;

import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.at.db.Database;
import org.at.db.DatabaseEventDispatcher;
import org.at.network.types.VPMGraphHolder;
import org.at.web.network.NetworkTopology;
import org.at.web.network.path.PathHolder;
import org.at.web.network.path.VPMSwitchInfoHolder;

import java.util.Properties;

public class LimeContextServerListener implements ServletContextListener {

	private static final int MANAGER_RETRY_TIME = 5000;	
	private static final String PROPERTIES_PATH = "config/config.xml";
	
	@Override
	public void contextInitialized(ServletContextEvent c) {
		System.out.println("this game will be started");
		
		//Initializing db if this is the first app start
		try {
			Properties props = new Properties();
			props.loadFromXML(new FileInputStream(PROPERTIES_PATH));
			c.getServletContext().setAttribute("properties", props);
			
			c.getServletContext().setAttribute(VPMGraphHolder.VPM_GRAPH_HOLDER, 
					new VPMGraphHolder());
			c.getServletContext().setAttribute(PathHolder.VPM_PATHS, new PathHolder());
			
			c.getServletContext().setAttribute(VPMSwitchInfoHolder.SWITCH_INFO_HOLDER, 
					new VPMSwitchInfoHolder());
			
			c.getServletContext().setAttribute(NetworkTopology.FIRST_TIME, new Boolean(true));
			
			Database.initialize(Database.DEFAULT_DBPATH);
			HypervisorConnectionManager manager = new HypervisorConnectionManager(MANAGER_RETRY_TIME,props.getProperty("network_name"),
					props.getProperty("bridge_name"));
			DatabaseEventDispatcher.addListener(manager);
			manager.start();
			c.getServletContext().setAttribute(HypervisorConnectionManager.HYPERVISOR_CONNECTION_MANAGER, manager);
			
		} catch (IOException e) {
			System.err.println("Failed to initialize db: "+e.getLocalizedMessage());
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent c) {
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
