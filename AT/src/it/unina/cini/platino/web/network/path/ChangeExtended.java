package it.unina.cini.platino.web.network.path;

import it.unina.cini.platino.web.network.path.backend.DefaultVPMPathManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * A servlet accepting changes of a flow's extended part. It is made to work with
 * our provided default path manager.
 * 
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 */
@WebServlet("/ChangeExtended")
public class ChangeExtended extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ChangeExtended() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void init() throws ServletException {
    	super.init();
    	File filepath = new File(DefaultVPMPathManager.EXTENDED_FILEPATH);
    	if(!(filepath).exists()){
    		filepath.getParentFile().mkdir();
    		try {
				PrintWriter pw = new PrintWriter(filepath);
				pw.println("{}");
				pw.flush();
				pw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		byte[] jsonData = Files.readAllBytes(Paths.get(DefaultVPMPathManager.EXTENDED_FILEPATH));
		JSONObject extended = new JSONObject(new String(jsonData));
		
		response.setContentType("application/json");
		PrintWriter pw = response.getWriter();
		pw.println(extended.toString());
		pw.flush();
		pw.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject jRes = new JSONObject();
		try{
			System.out.println(request.getParameter("extended"));
			PrintWriter pw = new PrintWriter(DefaultVPMPathManager.EXTENDED_FILEPATH);
		
			pw.println(request.getParameter("extended"));
			pw.flush();
			pw.close();
			jRes.put("status", "ok");
			jRes.put("details", "Extended field successfully edited!");
		}catch(IOException ex){
			jRes.put("status", "error");
			jRes.put("details", ex.getMessage());
		}finally{
			response.setContentType("application/json");
			PrintWriter pw = response.getWriter();
			pw.println(jRes.toString());
			pw.close();
		}
	}

}
