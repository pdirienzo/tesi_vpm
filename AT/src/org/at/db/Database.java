package org.at.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.at.db.DatabaseEventDispatcher.DBEvent;
import org.at.network.types.OvsSwitch;

public class Database {
	public static final String DEFAULT_DBPATH = "./data/lime.db";
	private String dbPath;

	private Connection connection = null;

	public Database(){
		this(DEFAULT_DBPATH);
	}

	public Database(String dbPath){
		this.dbPath = dbPath;
	}

	public synchronized static void initialize(String dbPath) throws IOException{
		boolean newDb = !((new File(dbPath)).exists());
		if(newDb){
			Database d = new Database();
			d.connect();
			try {
				d.createTables();
			} catch (SQLException e) {
				throw new IOException();
			}
			d.close();
		}
	}

	public void connect() throws IOException{	
		//boolean newDb = !((new File(dbPath)).exists());

		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"+dbPath);  
			connection.setAutoCommit(true);

			/*if(newDb) {
				createTables();
			}*/

		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public void close() {
		try {
			if(connection != null){
				connection.close();
				connection = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void createTables() throws SQLException{
		Statement s1 = connection.createStatement();
		s1.execute(
				"create table host(id integer primary key autoincrement, nome varchar not null," +
				"ip varchar , port varchar );");

		Statement s2 = connection.createStatement();
		s2.execute("create table controller(ip varchar, port varchar);");

		/*Statement s3 = connection.createStatement();
		s3.execute("create table switches (dpid text primary key," +
				"type integer not null);");*/

		s1.close();
		s2.close();
		//s3.close();
	}

	/******************* Hypervisor Part ***********************************/

	public void insertHypervisor(Hypervisor h) throws IOException{
		try {
			Statement s = connection.createStatement();
			s.execute("insert into host (nome,ip,port) values(\""+ 
					h.getName() + "\" ,\"" + h.getHostAddress() +"\" , \"" + h.getPort() +"\");"); 

			ResultSet rs = s.executeQuery("SELECT last_insert_rowid()");
			rs.next();
			h.setId(rs.getInt(1));

			rs.close();
			s.close();
			DatabaseEventDispatcher.dispatchEvent(DBEvent.hypervisor_insert, h);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}


	/**
	 * Gets an hypervisor object using the id
	 * @param hostName
	 * @return
	 */
	public Hypervisor getHypervisorById(String stringId) throws IOException{
		//removing the "H"
		int id = Integer.parseInt(stringId.substring(1, stringId.length()));

		Hypervisor h = null;
		Statement s;
		try {
			s = connection.createStatement();
			ResultSet rs = s.executeQuery(
					"select* from host where id=\""+id+ "\";");
			while(rs.next()){ //is just one
				h = new Hypervisor(rs.getInt(1),rs.getString(2), rs.getString(3), rs.getLong(4));
			}

			rs.close();
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

		return h;
	}

	/**
	 * Gets an hypervisor object using the hostname
	 * @param hostName
	 * @return
	 */
	public Hypervisor getHypervisorByIp(String hostName) throws IOException{
		Hypervisor h = null;
		Statement s;
		try {
			s = connection.createStatement();
			ResultSet rs = s.executeQuery(
					"select* from host where ip=\""+hostName+ "\";");
			while(rs.next()){ //is just one
				h = new Hypervisor(rs.getInt(1),rs.getString(2), rs.getString(3), rs.getLong(4));
			}

			rs.close();
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

		return h;
	}

	public void deleteHypervisor(String hostAddress) throws IOException{
		try {
			Hypervisor h = getHypervisorByIp(hostAddress);

			Statement s = connection.createStatement();
			s.execute("delete from host where ip = \""+hostAddress+"\";");
			s.close();
			DatabaseEventDispatcher.dispatchEvent(DBEvent.hypervisor_delete, h);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public boolean hypervisorExists(String hostname)throws IOException{
		return ( (getHypervisorByIp(hostname)) != null );
	}

	public List<Hypervisor> getAllHypervisors() throws IOException{
		List<Hypervisor> l = null; 
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery(
					"select * from host;");
			l = new ArrayList<Hypervisor>();
			while(rs.next()){
				Hypervisor h = new Hypervisor(rs.getInt(1),rs.getString(2), rs.getString(3), rs.getLong(4));
				l.add(h);
			}
			rs.close();
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

		return l;
	}

	/********************* Controller Part ***********************/


	public void insertController(Controller c) throws IOException{
		try {
			//as we can have at most 1 controller we clean the table first
			Statement s = connection.createStatement();
			s.execute("delete from controller where 1");

			Statement s2 = connection.createStatement();
			s2.execute("insert into controller values(\"" +
					c.getHostAddress() +"\" , \"" + c.getPort() +"\");");

			s.close();
			s2.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public Controller getController()throws IOException{
		Controller c = null;
		try {
			Statement s = connection.createStatement();
			ResultSet resultSet = s.executeQuery(
					"select ip, port from controller;");
			while(resultSet.next()){ //it's only one 
				c = new Controller(resultSet.getString(1),resultSet.getLong(2));
			}

			resultSet.close();
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

		return c;
	}

	public void deleteController() throws IOException{
		try {
			Statement s = connection.createStatement();
			s.execute("delete from controller where 1");
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

	}
	
	//************************** switches part **********************************
	
	/*public void insertSwitch(OvsSwitch sw) throws IOException{
		Statement s;
		try {
			s = connection.createStatement();
			s.execute("insert into switches (dpid,type) values ('"
					+sw.dpid+"',"+sw.type.getValue()+");");
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}	
	}
	
	public void setSwitchType(OvsSwitch sw) throws IOException{
		
	}*/
}
