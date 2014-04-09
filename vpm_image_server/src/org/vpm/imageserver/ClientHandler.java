package org.vpm.imageserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class ClientHandler extends Thread{
	private static final String METHOD_CREATE = "create";
	private static final String METHOD_DESTROY = "destroy";
	private static final String METHOD_LIST_FLAVOURS = "list_flavours";
	
	private Socket socket;
	private Properties props;
	private Logger logger;
	
	public ClientHandler(Socket clientSocket,Properties props){
		socket = clientSocket;
		this.props = props;
		logger=Logger.getLogger("org.vpm.imageServer.Thread");
		logger.info("Incoming connection from "+clientSocket.getInetAddress().getCanonicalHostName()
				+":"+clientSocket.getPort());
	}
	
	@Override
	public void run(){
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			JSONObject request = new JSONObject(inFromClient.readLine());
			JSONObject response = new JSONObject();
			
			logger.info("Processing request "+request.getString("method"));
			
			switch(request.getString("method")){
			case METHOD_LIST_FLAVOURS:
				response.put("status", "ok");
				response.put("flavours", getFlavourList());
				break;
			case METHOD_CREATE:
				try{
					response.put("disk_path", createDisk(request.getJSONObject("params")));
					response.put("status", "ok");
				}catch(IOException ex){
					response.put("status", "error");
					response.put("details", ex.getMessage());
				}
				
				break;
			case METHOD_DESTROY:
				response.put("status", "ok");
				break;
			
			default:
				response.put("status", "error");
				response.put("details", "Unrecognized method");
			}
			
			//sending response to the client
			PrintWriter outToClient = new PrintWriter(socket.getOutputStream(),true);
			outToClient.println(response.toString());
			outToClient.close();
			
		} catch (IOException e) {
			logger.severe(e.getMessage());
			
		}finally{
			try {
				socket.close();
				logger.info("Connection with "+socket.getInetAddress().getCanonicalHostName()
				+":"+socket.getPort()+" closed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String createDisk(JSONObject details) throws IOException{
		String result = null;
		
		File source = new File(props.getProperty("flavours_folder")+"/"+
				details.getString("flavour")+".flavour");
		
		String imagePath = props.getProperty("image_folder")+"/"+
				details.getString("hypervisor")+"_"+details.getString("vmname")+".img";
		
		File dest = new File(imagePath);
		
		if(dest.exists())
			throw new IOException("Image file already exists");
		
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
			
			result = dest.getAbsolutePath();
			logger.info("Created a new image disk: "+result);
		} finally {
			inputChannel.close();
			outputChannel.close();
		}

		return result;
	}
	
	private FileFilter notHidden = new FileFilter() {
	    @Override
	    public boolean accept(File file) {
	        return !file.isHidden();
	    }
	};
	
	private JSONArray getFlavourList(){
		JSONArray flavours = new JSONArray();
		File folder = new File(props.getProperty("flavours_folder"));
		for(File f : folder.listFiles(notHidden)){
			JSONObject file = new JSONObject();
			file.put("name", f.getName().split("\\.")[0]);
			float sizeGb = (float)f.length()/1024/1024/1024;
			file.put("size", String.format(Locale.US,"%.2f",sizeGb));
			flavours.put(file);
		}
		return flavours;
	}

}
