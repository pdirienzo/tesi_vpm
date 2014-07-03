package it.unina.cini.platino.tries;

import it.unina.cini.platino.storage.ImageCreationThread;
import it.unina.cini.platino.storage.StorageClient;

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
