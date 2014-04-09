package org.at.storage;

import java.io.IOException;

import org.json.JSONObject;

public class ImageCreationThread extends Thread{
	public static final String IDLE = "idle";
	public static final String PROGRESS = "progress";
	public static final String DONE = "done";
	public static final String ERROR = "error";
	
	private String status;
	private String errorDetails;
	
	private JSONObject data;
	private StorageClient client;
	
	public synchronized String getProgressStatus(){
		return status;
	}
	
	private synchronized void setStatus(String status){
		this.status = status;
	}
	
	public String getErrorDetails(){
		return errorDetails;
	}
	
	public ImageCreationThread(StorageClient client, JSONObject data){
		setStatus(IDLE);
		this.data = data;
		this.client = client;
	}
	
	
	public void run(){
		setStatus(PROGRESS);
		try {
			client.createImage(data);
			setStatus(DONE);
		} catch (IOException e) {
			setStatus(ERROR);
			errorDetails = e.getMessage();
		}
	}

}
