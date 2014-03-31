package org.at.web.streaming;

import java.util.ArrayList;

import org.gstreamer.Gst;

public class GstreamerPipelineManager{
	
	private ArrayList<GstreamerPipeline> pipelines;

	public String[] init(String[] args) {
		String[] newArgs=Gst.init("Test", args);
		//System.out.println("GST_DEBUG_DUMP_DOT_DIR:"+System.getenv("GST_DEBUG_DUMP_DOT_DIR"));
		pipelines=new ArrayList<GstreamerPipeline>();
		return newArgs;
	}

	public synchronized GstreamerPipeline addPipeline(String id, Object source, Object destination) {
		GstreamerPipeline pipeline=new GstreamerPipeline(id);
		pipeline.fillpipeline(source, destination);
		this.pipelines.add(pipeline);
		
		return pipeline;
	}
	
	
	public synchronized void playPipeline(String id){
		GstreamerPipeline pipe=getPipeline(id);
		if (pipe==null) return;
		
		pipe.play();
	}
	public synchronized void stopPipeline(String id){
		GstreamerPipeline pipe=getPipeline(id);
		if (pipe==null) return;
		
		pipe.stop();
	}
	public synchronized void removePipeline(String id){
		GstreamerPipeline pipe=getPipeline(id);
		if (pipe==null) return;
		pipe.stop();
		this.pipelines.remove(pipe);
	}
	
	
	public synchronized GstreamerPipeline getPipeline(String id){
		for (GstreamerPipeline pipe : pipelines) {
			if (pipe.getId().equalsIgnoreCase(id)) return pipe;
		}
		return null;
	}

	public synchronized ArrayList<GstreamerPipeline> getPipelines(){
		return pipelines;
	}
	

}
