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
		
		mxCodec codec = new mxCodec();
		
		String xml = mxXmlUtils.getXml(codec.encode(graph.getModel()));
		System.out.println("First: "+xml);
		mxGraph newGraph = new mxGraph();
		org.w3c.dom.Node node = mxXmlUtils.parseXml(xml);
		
		mxCodec decoder = new mxCodec(node.getOwnerDocument());
		decoder.decode(node.getFirstChild(),newGraph.getModel());
		
		
		System.out.println(mxXmlUtils.getXml(codec.encode(newGraph.getModel())));
		//System.out.println(xml);
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
		
		try{
			for(int i=0;i<switches.length;i++){
				graph.insertVertex(graph.getDefaultParent(), null, "<p>"+switches[i].dpid+"</p><p>"+switches[i].ip+"</p>", 20*i, 20, 80, 30);
			}
		
		/*
		for(int i=0;i<connections.length;i++){
			graph.insertEdge(graph.getDefaultParent(),null,connections[i].srcPort, arg3, arg4);
		}*/
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
