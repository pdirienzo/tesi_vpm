package net.floodlightcontroller.vpm;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch.PortChangeType;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.ImmutablePort;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;

import org.openflow.util.HexString;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;


public class VPMNetworkTopologyListener implements ILinkDiscoveryListener,IOFSwitchListener {

	//private static final String CALLBACK_URI = "http://192.168.1.180:8080/VPM/VPMEventListener";
	private static final int TIMEOUT = 1000;

	private IFloodlightProviderService ifps = null;
	private Timer timer;
	private StringBuilder topologyUpdate;
	private boolean firstRequest;
	private int nRequests;

	private final ReentrantLock lock;

		
	public VPMNetworkTopologyListener(
			IFloodlightProviderService floodlightProvider) {

		this.firstRequest = true;
		this.nRequests = 0;
		this.ifps= floodlightProvider;
		this.timer = new Timer();
		this.topologyUpdate = new StringBuilder();
		this.lock = new ReentrantLock();
	}

	@Override
	public void linkDiscoveryUpdate(LDUpdate update) {
		// TODO Auto-generated method stub
		if (update.getOperation() == UpdateOperation.LINK_REMOVED){

			System.out.println("LDUpdate single one "+update.toString());
		}

	}
	

	@Override
	public void switchAdded(long switchId) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void switchActivated(long switchId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void switchPortChanged(long switchId, ImmutablePort port,
			PortChangeType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void switchChanged(long switchId) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void switchRemoved(long switchId) {
		// TODO Auto-generated method stub
		//synchronized(lock){
			lock.lock();
		//}
		try{
			if(firstRequest){
				topologyUpdate.append("{ \"type\": \"TOPOLOGY\"," +
						"\"op\":\"REMOVE\","
						+ "\"result\": [");
				timer.schedule(new FaultSender(), TIMEOUT);
				firstRequest = false;
			}
			
			topologyUpdate.append("{ \"type\":\"switch\",\"dpid\":\""
			+HexString.toHexString(switchId)+"\"},");
			nRequests++;

		}finally{
			//synchronized(lock){
				lock.unlock();
			//}
		}
	}

	@Override
	public void linkDiscoveryUpdate(List<LDUpdate> updateList) {

		//synchronized(lock){
			lock.lock();
		//}
		try{
			if(firstRequest){
				topologyUpdate.append("{ \"type\": \"TOPOLOGY\"," +
						"\"op\":\"REMOVE\","
						+ "\"result\": [");
				timer.schedule(new FaultSender(), TIMEOUT);
				firstRequest = false;
			}

			for (int i=0; i< updateList.size(); i++){
				LDUpdate upd = updateList.get(i);
				if (upd.getOperation() == UpdateOperation.LINK_REMOVED){
					nRequests++;
					topologyUpdate.append("{ \"type\" : \"link\",");
					if(ifps.getSwitch(upd.getSrc()) != null && ifps.getSwitch(upd.getDst()) != null){
						String srcIP= ifps.getSwitch(upd.getSrc()).getInetAddress().toString();
						String dstIP= ifps.getSwitch(upd.getDst()).getInetAddress().toString();
						topologyUpdate.append("\"src-ip\":\""+srcIP+"\",");
						topologyUpdate.append("\"dst-ip\":\""+dstIP+"\",");
					}
					else {
						topologyUpdate.append("\"src-ip\":\"\",");
						topologyUpdate.append("\"dst-ip\":\"\",");
					}
					String srcPortName = "" + upd.getSrcPort();
					String dstPortName = "" + upd.getDstPort();
					String dstDpid = HexString.toHexString(upd.getDst());
					String srcDpid = HexString.toHexString(upd.getSrc());
					
					topologyUpdate.append("\"src-port\":\""+srcPortName+"\",");
					topologyUpdate.append("\"dst-port\":\""+dstPortName+"\",");
					topologyUpdate.append("\"src-dpid\":\""+srcDpid+"\",");
					topologyUpdate.append("\"dst-dpid\":\""+dstDpid+"\"");
					topologyUpdate.append("},");
				}
			}

		}finally{
			//synchronized(lock){
				lock.unlock();
			//}
		}

	}

	private class FaultSender extends TimerTask{


		/*private void post(String url, String data) {
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
		}*/

		@Override
		public void run() {

			//synchronized(lock){
				lock.lock();
			//}

			try{
				topologyUpdate.deleteCharAt(topologyUpdate.length()-1);
				topologyUpdate.append("]}");

				if( nRequests > 0 ){ //sometimes we just get notifies about link creation 
					System.out.println("LDUPDATE: "+topologyUpdate);
					VPMNotificationService.notifyEvent(topologyUpdate.toString());//post(CALLBACK_URI,topologyUpdate.toString());
				}else
					System.out.println("it will not be sent as does not contain destruction of links");

				//resetting status
				topologyUpdate = new StringBuilder();
				firstRequest = true;
				nRequests = 0;
				timer = new Timer();

			}finally{
				//synchronized(lock){
					lock.unlock();
				//}
			}
		}

	}

	
}
