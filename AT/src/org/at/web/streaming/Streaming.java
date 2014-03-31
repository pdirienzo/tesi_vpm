package org.at.web.streaming;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.gstreamer.Caps;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.Pipeline;
import org.gstreamer.Element; 
import org.gstreamer.State;
import org.gstreamer.swing.VideoComponent;


public class Streaming {

	static Pipeline pipe=null;
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		//Streaming m=new Streaming();
//		//m.init(args);
//		args = Gst.init("SwingVideoTest", args); 
//        pipe = new Pipeline("pipeline"); 
//        // This is from VideoTest example and gives test image 
//        // final Element videosrc = 
//        ElementFactory.make("videotestsrc", "source"); 
//        // This gives black window with VideoComponent 
//        final Element videosrc = ElementFactory.make("v4l2src", "source"); 
//        final Element videofilter = ElementFactory.make("capsfilter", 
//"flt"); 
//        videofilter.setCaps(Caps.fromString("video/x-raw-yuv, width=640, height=480")); 
//        SwingUtilities.invokeLater(new Runnable() { 
//            public void run() { 
//                VideoComponent videoComponent = new VideoComponent(); 
//                // This gives only black window 
//                Element videosink = videoComponent.getElement(); 
//                // This gives 2nd window with stream from webcam 
//                // Element videosink = 
//ElementFactory.make("xvimagesink", "sink"); 
//                pipe.addMany(videosrc, videofilter, videosink); 
//                Element.linkMany(videosrc, videofilter, videosink); 
//                JFrame frame = new JFrame("Swing Video Test"); 
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
//                frame.add(videoComponent, BorderLayout.CENTER); 
//                videoComponent.setPreferredSize(new Dimension(640, 
//480)); 
//                frame.pack(); 
//                frame.setVisible(true); 
//                // Start the pipeline processing 
//                pipe.setState(State.PLAYING); 
//            } 
//        }); 
//	}
	
	GstreamerPipelineManager gstreamer = null;
	String myId=null;
	
	public static void main(String[] args){
		Streaming m=new Streaming();
		m.init(args);
	}
	
	private void init(String[] args) {
		System.out.println("Avvio gstreamer...");
		gstreamer = new GstreamerPipelineManager();
		args=gstreamer.init(args);
		System.out.println("args: "+args);
		try {
			myId="gstreamer_"+java.net.InetAddress.getLocalHost().getHostName();
			System.out.println("myID:"+myId);
			//myId="gstreamer_"+System.currentTimeMillis();
		} catch (UnknownHostException e) {
			myId="gstreamer_unknown";
		}
		
		System.out.println("Avviato...");
		//PipelineDestinationInfo gp=new PipelineDestinationInfo("127.0.0.1", 5000, 5001, "localhost");
		RtpItem gp=new RtpItem();
		gp.host="127.0.0.1";
		gp.rtpport=5000;
		gstreamer.addPipeline(myId, gp, null);
		gstreamer.playPipeline(myId);
		Gst.main();
		
		
	}

}
