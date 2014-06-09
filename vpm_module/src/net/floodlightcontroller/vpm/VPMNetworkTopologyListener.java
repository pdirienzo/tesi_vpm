package net.floodlightcontroller.vpm;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;

import org.openflow.util.HexString;


public class VPMNetworkTopologyListener implements ILinkDiscoveryListener {

	private static final String CALLBACK_URI = "http://192.168.1.179:8081/VPM/VPMToleranceManager";
	private static final int TIMEOUT = 1000;

	private IFloodlightProviderService ifps = null;
	private Timer timer;
	private StringBuilder linkUpdate;
	private boolean firstRequest;
	private int nRequests;

	private final ReentrantLock lock;

	public VPMNetworkTopologyListener(
			IFloodlightProviderService floodlightProvider) {

		this.firstRequest = true;
		this.nRequests = 0;
		this.ifps= floodlightProvider;
		this.timer = new Timer();
		this.linkUpdate = new StringBuilder();
		this.lock = new ReentrantLock();
	}

	@Override
	public void linkDiscoveryUpdate(LDUpdate update) {
		// TODO Auto-generated method stub
		if (update.getOperation() == UpdateOperation.LINK_REMOVED){

			System.out.println("LDUpdate single one "+update.toString());
		}

	}

	/*public List<LDUpdate> findDuplicate (List<LDUpdate> lupd){
		List<LDUpdate> ld = new ArrayList<LDUpdate>();
		while (lupd.size()>0){
			LDUpdate l = lupd.remove(0);
			for (LDUpdate link : lupd){

				if (l.getSrc() == link.getDst() && l.getSrcPort() == link.getDstPort() &&
						l.getDst() == link.getSrc() && l.getDstPort() == link.getSrcPort()){
					lupd.remove(link);
					break;
				}

			}
			ld.add(l);
		}
		return ld;
	}*/

	@Override
	public void linkDiscoveryUpdate(List<LDUpdate> updateList) {

		synchronized(lock){
			lock.lock();
		}

		try{
			if(firstRequest){
				linkUpdate.append("{ \"result\": [");
				timer.schedule(new FaultSender(), TIMEOUT);
				firstRequest = false;
			}

			for (int i=0; i< updateList.size(); i++){
				LDUpdate upd = updateList.get(i);
				if (upd.getOperation() == UpdateOperation.LINK_REMOVED){
					nRequests++;

					linkUpdate.append("{");
					String srcIP= ifps.getSwitch(upd.getSrc()).getInetAddress().toString();
					String dstIP= ifps.getSwitch(upd.getDst()).getInetAddress().toString();
					String srcPortName = "" + upd.getSrcPort();
					String dstPortName = "" + upd.getDstPort();
					String dstDpid = HexString.toHexString(upd.getDst());
					String srcDpid = HexString.toHexString(upd.getSrc());
					linkUpdate.append("\"src-ip\":\""+srcIP+"\",");
					linkUpdate.append("\"dst-ip\":\""+dstIP+"\",");
					linkUpdate.append("\"src-port\":\""+srcPortName+"\",");
					linkUpdate.append("\"dst-port\":\""+dstPortName+"\",");
					linkUpdate.append("\"src-dpid\":\""+srcDpid+"\",");
					linkUpdate.append("\"dst-dpid\":\""+dstDpid+"\"");
					linkUpdate.append("},");
				}
			}

		}finally{
			synchronized(lock){
				lock.unlock();
			}
		}

	}

	private class FaultSender extends TimerTask{


		private void post(String url, String data) {
			/* POST Method */
			try {
				HttpURLConnection conn = (HttpURLConnection)((new URL(url)).openConnection());
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
				dos.writeBytes("data="+data);
				dos.flush();
				dos.close();
				System.out.println(conn.getResponseCode());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {

			synchronized(lock){
				lock.lock();
			}

			try{
				linkUpdate.deleteCharAt(linkUpdate.length()-1);
				linkUpdate.append("]}");

				if( nRequests > 0 ){ //sometimes we just get notifies about link creation 
					System.out.println("LDUPDATE: "+linkUpdate);
					post(CALLBACK_URI,linkUpdate.toString());
				}else
					System.out.println("it will not be sent as does not contain destruction of links");

				//resetting status
				linkUpdate = new StringBuilder();
				firstRequest = true;
				nRequests = 0;
				timer = new Timer();

			}finally{
				synchronized(lock){
					lock.unlock();
				}
			}
		}

	}

}
