package org.at.web.streaming;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.gstreamer.Bus;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GstObject;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.AppSink;
import org.gstreamer.elements.BaseSink;
import org.gstreamer.elements.BaseSrc;

public class GstreamerPipeline{

	private String id;
	private Pipeline pipe;
	private BaseSrc vrtpsrc;
	
	
	public int getVideoSrcRtpPort(){
		if (vrtpsrc==null) return 0;
		return (Integer) vrtpsrc.get("port");
	}
	
	
	public GstreamerPipeline(String id){
		this.id=id;
		
		pipe = new Pipeline();

		
	}
	
	AppSink.NEW_BUFFER newbufferlistener=new AppSink.NEW_BUFFER() {
		@Override
		public void newBuffer(AppSink elem) {
			//Dati RR rtcp ricevuti
			ByteBuffer buffer=elem.pullBuffer().getByteBuffer();
			final String nodeID=elem.getName().substring("vrtcpappsink_".length());
			System.out.println("Buffer array:"+buffer.hasArray()+", remaining:"+buffer.remaining());
			int remaining=buffer.remaining();
			byte dataBuffer[]=new byte[remaining];
			buffer.get(dataBuffer, 0, dataBuffer.length);
			
			
		}
	};
	AppSink.NEW_PREROLL newprerolllistener=new AppSink.NEW_PREROLL() {
		@Override
		public void newPreroll(AppSink elem) {
			// TODO Auto-generated method stub
//			System.out.println("newPreroll,NEW_PREROLL called");
			
		}
	};
	
	ArrayList<PipelineDestinationInfo> destinations=new ArrayList<PipelineDestinationInfo>();
	
	public boolean removeRtpRtcpSender(String host, int vrtpport, String id){
		PipelineDestinationInfo dest=new PipelineDestinationInfo(host,vrtpport,id);
		
		if (destinations.remove(dest)==false) return false;
		
		String nodeID=dest.toString();
		
		BaseSink multisinkrtp=(BaseSink) pipe.getElementByName("vrtpmultisink");
		
		//rimuovo le destinazioni dai multiudpsink
		multisinkrtp.emit("remove", host, vrtpport, (com.sun.jna.Pointer)null);
		
		
		Element item1=pipe.getElementByName("vrtcpudpsrc_"+nodeID);
		pipe.remove(item1);		
		//savePngSchema();
		
		return true;
	}
	
	public int addRtpSender(String host, int vrtpport, String id){
		System.out.println("addRtpRtcpSender, Current Thread: "+Thread.currentThread().getName());
		PipelineDestinationInfo dest=new PipelineDestinationInfo(host,vrtpport,id);
		
		if (destinations.contains(dest)==true){
			//la destinazione già esiste, che fare?
			
			//proviamo a toglierla
			removeRtpRtcpSender(host,vrtpport,id);
		}
		destinations.add(dest);
		//per la gestione di un client devi aggiungere il destinatario
		//ai multisink rtp e rtcp
		//System.out.println("########## addRtpRtcpSender");
		String nodeID=dest.toString();
		
		//BaseSink multisinkrtp=(BaseSink) pipe.getElementByName("vrtpmultisink");
		
		//multisinkrtp.emit("add", host, vrtpport, (com.sun.jna.Pointer)null);
		///*if (multisinkrtp.getState()!=State.PLAYING)*/ multisinkrtp.play();
		
		
		//poi devi configurare un udpsrc e un appsink per la ricezione dei pacchetti RR RTCP
		Element tee = pipe.getElementByName("t");
		Element vclientrtcpsrc= ElementFactory.make("udpsink", "vrtcpudpsink_"+nodeID);
		System.out.println("pipe: "+pipe.getName());
		vclientrtcpsrc.set("host",host);
		vclientrtcpsrc.set("port", vrtpport); //fai generare la porta in automatico
		//vclientrtcpsrc.pause(); //metti in pausa per creare la porta
		pipe.add(vclientrtcpsrc);
		
	//	vclientrtcpsrc.play();
		
		int srcport=(Integer) vclientrtcpsrc.get("port");
		System.out.println("Porta di ascolto RTCP per "+host+":"+vrtpport);
//		savePngSchema();
		return srcport;
		
	}
	public void setRRrtcpAddress(String host, int port){
		System.out.println("setRRrtcpAddress, Current Thread: "+Thread.currentThread().getName());
		
		System.out.println("Invio i dati RR rtcp a "+host+":"+port);
		BaseSink vrtcpsink=(BaseSink) pipe.getElementByName("vrtcpsink");
		Element.linkPads(pipe.getElementByName("rtpbin"), "send_rtcp_src_0", vrtcpsink, "sink");
		vrtcpsink.set("host", host);
		vrtcpsink.set("port", port);
		vrtcpsink.pause();
		vrtcpsink.play();
		
//		savePngSchema();
	}
	
	public void fillpipeline(Object source, Object destination){
		System.out.println("fillpipeline, Current Thread: "+Thread.currentThread().getName());
		
		if (source instanceof RtpItem){
			RtpItem rtpsrc=(RtpItem) source;
			RtpItem rtpdest=(RtpItem) destination;
			
			System.out.println("RtpItem: "+rtpsrc.host);
			String template="gstrtpbin name=rtpbin "+
                   //"udpsrc caps=\"application/x-rtp,media=(string)video,clock-rate=(int)90000,encoding-name=(string)H263-1998\"  "+
                   "v4l2src ! videoscale ! videorate ! video/x-raw-rgb, "
                   + "width=160,height=120,framerate=30/1 ! ffmpegcolorspace ! "
                   + "vp8enc threads=2 bitrate=90000 error-resilient=true ! "
                   + "rtpvp8pay ! tee name=t ! queue ! udpsink name=vrtcpsink ! t. ! queue ! udpsink";
			
			pipe=Pipeline.launch(template);

			pipe.getBus().connect(new Bus.ERROR() {
				@Override
				public void errorMessage(GstObject source, int code, String message) {
					// TODO Auto-generated method stub
					System.out.println("errorMessage, source="+source+", message="+message);
				}
			});
			
			Element rtpsink = pipe.getElementByName("vrtcpsink");
			System.out.println("rtpsink: "+rtpsink);
			rtpsink.set("port", rtpsrc.rtpport);
			rtpsink.set("host", rtpsrc.host);
//			
//			vrtcpsrc=(BaseSrc) pipe.getElementByName("vrtcpsrc");
//			vrtcpsrc.set("port", rtpsrc.rtcpport);
//			vrtcpsrc.pause(); //lo metto in pausa in modo da fargli assegnare la porta se non specificata
//			vrtcpsrc.ready();
			
			savePngSchema();
			return;
		}
		
		
		
	}
	private void savePngSchema() {
		System.out.println("Salvo lo schema della pipeline in png");
		System.out.println(pipe.toString());
		String dotFileName=this.id+"_"+System.currentTimeMillis();
		pipe.debugToDotFile(Pipeline.DEBUG_GRAPH_SHOW_ALL,dotFileName ,false);
		File f=new File(System.getenv("GST_DEBUG_DUMP_DOT_DIR"),dotFileName+".dot");
		try {
			Process p=java.lang.Runtime.getRuntime().exec("dot -Tpng -o"+f.getAbsolutePath()+".png "+f.getAbsolutePath());
			p.waitFor();
			f.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void play(){
		pipe.play();
	}

	public String getId() {
		return this.id;
	}

	public void stop() {
		pipe.stop();
	}
	
	public void pause() {
		pipe.pause();
	}
	
	
	
	
	
}
