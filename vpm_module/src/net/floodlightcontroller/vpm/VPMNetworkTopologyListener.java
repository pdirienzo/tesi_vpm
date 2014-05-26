package net.floodlightcontroller.vpm;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.openflow.util.HexString;

import com.google.common.net.HttpHeaders;

public class VPMNetworkTopologyListener implements ILinkDiscoveryListener {

	private static final String CALLBACK_URI = "http://192.168.1.179:8081/VPM/VPMToleranceManager";
	
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
	
	private void sendPost(String content){
		HttpRequest httpReq= new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, CALLBACK_URI);
		httpReq.setHeader(HttpHeaders.CONTENT_TYPE,"application/json");     
		//String params="";
		ChannelBuffer cb=ChannelBuffers.copiedBuffer(content,Charset.defaultCharset());
		httpReq.setHeader(HttpHeaders.CONTENT_LENGTH,cb.readableBytes());
		httpReq.setContent(cb);
				
	}

	@Override
	public void linkDiscoveryUpdate(List<LDUpdate> updateList) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		//updateList = findDuplicate(updateList);
		sb.append("{ \"result\": [");
		for (LDUpdate upd : updateList){
			if (upd.getOperation() == UpdateOperation.LINK_REMOVED){
				sb.append("{");
				String srcIP= ifps.getSwitch(upd.getSrc()).getInetAddress().toString();
				String dstIP= ifps.getSwitch(upd.getDst()).getInetAddress().toString();
				String srcPortName= ifps.getSwitch(upd.getSrc()).getPort(upd.getSrcPort()).getName();
				String dstPortName= ifps.getSwitch(upd.getDst()).getPort(upd.getDstPort()).getName();
				srcPortName = srcPortName + "/" + upd.getSrcPort();
				dstPortName = dstPortName + "/" + upd.getDstPort();
				String dstDpid = HexString.toHexString(upd.getDst());
				String srcDpid = HexString.toHexString(upd.getSrc());
				sb.append("\"src-ip\":\""+srcIP+"\",");
				sb.append("\"dst-ip\":\""+dstIP+"\",");
				sb.append("\"src-port\":\""+srcPortName+"\",");
				sb.append("\"dst-port\":\""+dstPortName+"\",");
				sb.append("\"src-dpid\":\""+srcDpid+"\",");
				sb.append("\"dst-dpid\":\""+dstDpid+"\"");
				sb.append("}");
			}
		}
		sb.append("]}");
		 
		System.out.println("LDUPDATE: "+sb);
		sendPost(sb.toString());
	}

}
