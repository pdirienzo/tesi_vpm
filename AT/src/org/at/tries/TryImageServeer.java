package org.at.tries;

import org.at.storage.ImageCreationThread;
import org.at.storage.StorageClient;
import org.json.JSONObject;

public class TryImageServeer {

	public static void main(String[] args) throws InterruptedException {
		StorageClient client = new StorageClient("192.168.1.4", 8123);
		JSONObject params = new JSONObject();
		params.put("vmname", "linx");
		params.put("hypervisor", "H1");
		params.put("flavour", "test");
		ImageCreationThread t = new ImageCreationThread(client, params);
		//t.start();
		
		Thread.sleep(1000);
		while(t.getProgressStatus().equals(ImageCreationThread.PROGRESS)){
			Thread.sleep(1000);
		}
		
		System.out.println(t.getProgressStatus());
	}

}
