package org.at.db;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class DbServlet extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		super.init();
		System.out.println("DB servlet started, creating connection...");
		try {
			Database d = new Database();//creating database with default path
			d.connect();
			getServletContext().setAttribute("database", d);
			System.out.println("Connection created and db added to the context");
		} catch (IOException e) {
			throw new ServletException(e.getMessage());
		}
		
	}
	
	@Override
	public void destroy() {
		super.destroy();
		System.out.println("DB servlet destroyed, closing connection to the db...");
		Database d = (Database)getServletContext().getAttribute("database");
		d.close();
		System.out.println("DB connection closed");
	}

}
