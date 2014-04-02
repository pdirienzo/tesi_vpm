package org.at.web.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		newGraph.getModel().beginUpdate();
		
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
		System.out.println("called get");
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put("startArrow", mxConstants.NONE);
		style.put("endArrow",mxConstants.NONE);
		mxGraph graph = new mxGraph();
		mxStylesheet stylesheet = graph.getStylesheet();
		stylesheet.putCellStyle("MyStyle", style);
		graph.insertVertex(graph.getDefaultParent(), null, "Hello", 20, 20, 80, 30);
		graph.insertVertex(graph.getDefaultParent(), null, "World!", 200, 150, 80, 30);
		
		PrintWriter out = new PrintWriter(response.getOutputStream());
		out.println("ciao");
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
