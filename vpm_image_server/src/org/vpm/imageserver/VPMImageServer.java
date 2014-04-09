package org.vpm.imageserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Logger;

public class VPMImageServer {
	
	static Logger logger=Logger.getLogger("org.vpm.imageServer");
	private Properties props;
	private boolean active;
	private ServerSocket ss;
	
	private synchronized void setActiveState(boolean status){
		active = status;
	}
	
	private synchronized boolean isActive(){
		return active;
	}
	
	public int getServerPort(){
		return Integer.parseInt(
				props.getProperty("server_port"));
	}
	
	public VPMImageServer() throws IOException{
		props = new Properties();
		props.loadFromXML(new FileInputStream("config.xml"));
		active = false;
		ss = new ServerSocket(Integer.parseInt(
				props.getProperty("server_port")));
	}
	
	public void service() throws IOException{
		setActiveState(true);
		
		while(isActive()){
			Socket s = null;
			try{
				s = ss.accept(); //gives exception if 
				ClientHandler handler = new ClientHandler(
						s, props);
				handler.start();
			}catch(IOException ex){
				
			}
			
		}
	}
	
	public void stop() throws IOException{
		setActiveState(false);
		ss.close();
	}

	public static void main(String[] args) {
		VPMImageServer server;
		logger.info("Starting the image server...");
		try {
			server = new VPMImageServer();
			logger.info("Image server started, listening for incoming connections on port "
					+server.getServerPort());
			server.service();
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}

	}

}
