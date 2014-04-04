package org.at.web.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.at.db.Database;
import org.at.floodlight.FloodlightController;
import org.at.floodlight.types.LinkConnection;
import org.at.floodlight.types.OvsSwitch;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

/**
 * Servlet implementation class GetNetworkTopology
 */
@WebServlet("/NetworkTopology")
public class NetworkTopology extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NetworkTopology() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public static void main(String[] args){
		mxGraph graph = new mxGraph();
		
		graph.getModel().beginUpdate();
		
		try{
			Object v1 = graph.insertVertex(graph.getDefaultParent(), null, "Hello", 20, 20, 80, 30);
			Object v2 = graph.insertVertex(graph.getDefaultParent(), null, "World!", 200, 150, 80, 30);
			graph.insertEdge(graph.getDefaultParent(),null,"",v1,v2);
		}finally{
			graph.getModel().endUpdate();
		}
		
		for(Object cell : graph.getChildCells(graph.getDefaultParent(), true, false)){
			graph.getView().getState(cell).getLabel();
			mxCell c = (mxCell)cell;
			
			System.out.println(c.isEdge()+" "+c.getAttribute("Label")+"---"+graph.getView().getState(c).getLabel());
		}
		/*
		mxCodec codec = new mxCodec();
		
		String xml = mxXmlUtils.getXml(codec.encode(graph.getModel()));
		System.out.println("First: "+xml);
		mxGraph newGraph = new mxGraph();
		org.w3c.dom.Node node = mxXmlUtils.parseXml(xml);
		
		mxCodec decoder = new mxCodec(node.getOwnerDocument());
		decoder.decode(node.getFirstChild(),newGraph.getModel());
		
		
		System.out.println(mxXmlUtils.getXml(codec.encode(newGraph.getModel())));*/
		//System.out.println(xml);
	}
    
    private mxCell getVertexFromOvs(String dpid, mxGraph graph,mxCell[] vertexes){
    	int i=0;
    	mxCell result = null;
    	while((result==null) && i<vertexes.length){
    		String label = graph.getView().getState(vertexes[i]).getLabel();
    		if(dpid.equals(label))
    			result = vertexes[i];
    		else
    			i++;
    	}
    	
    	return result;
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Database d = new Database();
		d.connect();
		FloodlightController controller = new FloodlightController(d.getController());
		d.close();
		
		OvsSwitch[] switches = controller.getSwitches();
		LinkConnection[] connections = controller.getSwitchConnections();
		
		mxGraph graph = new mxGraph();
		
		graph.getModel().beginUpdate();
		
		mxCell[] vertexes = new mxCell[switches.length];
		try{
			for(int i=0;i<switches.length;i++){
				double width=(double) switches[i].dpid.length() + 20;
				vertexes[i] = (mxCell)graph.insertVertex(graph.getDefaultParent(), null, switches[i].dpid, 20*i, 20, 
						width, 30);
			}
		
		}finally{
			graph.getModel().endUpdate();
		}
		
		graph.getModel().beginUpdate();
		try{
		//now edges
		for(int i=0;i<connections.length;i++){
			mxCell src = getVertexFromOvs(connections[i].dpidSrc, graph, vertexes);
			mxCell dst = getVertexFromOvs(connections[i].dpidDst, graph, vertexes);
			graph.insertEdge(graph.getDefaultParent(), null, "", src, dst);
		}
		}finally{
			graph.getModel().endUpdate();
		}
		
		
		mxCodec codec = new mxCodec();
		PrintWriter out = new PrintWriter(response.getOutputStream());
		out.println(mxXmlUtils.getXml(codec.encode(graph.getModel())));
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = new PrintWriter(response.getOutputStream());
		System.out.println(request.getParameter("xml"));
		out.println("ok");
		out.close();
	}

}
