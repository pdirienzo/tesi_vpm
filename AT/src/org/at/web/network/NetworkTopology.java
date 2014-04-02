package org.at.web.network;

import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

/**
 * Servlet implementation class GetNetworkTopology
 */
@WebServlet("/GetNetworkTopology")
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
    	System.out.println(mxConstants.NONE);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put("startArrow", mxConstants.NONE);
		style.put("endArrow",mxConstants.NONE);
		mxGraph graph = new mxGraph();
		mxStylesheet stylesheet = graph.getStylesheet();
		stylesheet.putCellStyle("MyStyle", style);
		graph.insertVertex(graph.getDefaultParent(), null, "Hello", 20, 20, 80, 30);
		graph.insertVertex(graph.getDefaultParent(), null, "World!", 200, 150, 80, 30);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
