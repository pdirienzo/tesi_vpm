package net.floodlightcontroller.vpm;

import java.util.ArrayList;
import java.util.List;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.openflow.util.HexString;


public class VPMNetworkTopologyListener implements ILinkDiscoveryListener {

	private static final String CALLBACK_URI = "http://192.168.1.179:8081/VPM/VPMToleranceManager";
	private static final int TIMEOUT = 3000;

	private IFloodlightProviderService ifps = null;
	public VPMNetworkTopologyListener(
			IFloodlightProviderService floodlightProvider) {
		// TODO Auto-generated constructor stub
		this.ifps= floodlightProvider;
	}

	@Override
	public void linkDiscoveryUpdate(LDUpdate update) {
		// TODO Auto-generated method stub
		if (update.getOperation() == UpdateOperation.LINK_REMOVED){
		
			System.out.println("LDUpdate single one "+update.toString());
		}
		
	}
	
	public List<LDUpdate> findDuplicate (List<LDUpdate> lupd){
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
	}
	private final static HttpClient client = createHttpClient();

	private static HttpClient createHttpClient(){
		RequestConfig config = RequestConfig.custom()
			    .setSocketTimeout(TIMEOUT)
			    .setConnectTimeout(TIMEOUT)
			    .build();
		
		HttpClientBuilder hcBuilder = HttpClients.custom();
		hcBuilder.setDefaultRequestConfig(config);
		
		return hcBuilder.build();
	}


	public static String post(String url, String data) {
		/* POST Method */
		final HttpPost post = new HttpPost(url);
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("data", data));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			return EntityUtils.toString(client.execute(post).getEntity());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void sendPost(String content){
//		HttpRequest httpReq= new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, CALLBACK_URI);
//		httpReq.setHeader(HttpHeaders.CONTENT_TYPE,"application/json");     
//		//String params="";
//		ChannelBuffer cb=ChannelBuffers.copiedBuffer(content,Charset.defaultCharset());
//		httpReq.setHeader(HttpHeaders.CONTENT_LENGTH,cb.readableBytes());
//		httpReq.setContent(cb);
//		
//		ClientBootstrap client = new ClientBootstrap(
//				new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
//						Executors.newCachedThreadPool()));
//		Channel channel = client.connect(new InetSocketAddress("192.168.1.179", 8081)).awaitUninterruptibly()
//				.getChannel();
//		
//		channel.write(httpReq);
//		channel.close();
//		client.releaseExternalResources();
		
	}

	@Override
	public void linkDiscoveryUpdate(List<LDUpdate> updateList) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		boolean send = false;
		//updateList = findDuplicate(updateList);
		sb.append("{ \"result\": [");
		
		for (int i=0; i< updateList.size(); i++){
			LDUpdate upd = updateList.get(i);
			if (upd.getOperation() == UpdateOperation.LINK_REMOVED){
				sb.append("{");
				String srcIP= ifps.getSwitch(upd.getSrc()).getInetAddress().toString();
				String dstIP= ifps.getSwitch(upd.getDst()).getInetAddress().toString();
				String srcPortName = "" + upd.getSrcPort();
				String dstPortName = "" + upd.getDstPort();
				String dstDpid = HexString.toHexString(upd.getDst());
				String srcDpid = HexString.toHexString(upd.getSrc());
				sb.append("\"src-ip\":\""+srcIP+"\",");
				sb.append("\"dst-ip\":\""+dstIP+"\",");
				sb.append("\"src-port\":\""+srcPortName+"\",");
				sb.append("\"dst-port\":\""+dstPortName+"\",");
				sb.append("\"src-dpid\":\""+srcDpid+"\",");
				sb.append("\"dst-dpid\":\""+dstDpid+"\"");
				sb.append("},");
				send = true;
			}
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("]}");
		 if (send){
			 
			 System.out.println("LDUPDATE: "+sb);
				post(CALLBACK_URI,sb.toString());
		 }
		
	}

}
