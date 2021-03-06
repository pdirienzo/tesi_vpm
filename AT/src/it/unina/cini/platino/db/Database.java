package it.unina.cini.platino.db;

import it.unina.cini.platino.db.DatabaseEventDispatcher.DBEvent;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A class which gives access to an SQLLite DB. DB type can be changed
 * by just changing the JDBC driver.
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
public class Database {
	public static final String JDBC_DRIVER = "jdbc:sqlite:";
	public static final String DEFAULT_DBPATH = "./data/vpm.db";
	public static final String DATABASE = Database.class.getCanonicalName();
	
	private String dbPath;

	private Connection connection = null;

	public Database(){
		this(DEFAULT_DBPATH);
	}

	public Database(String dbPath){
		this.dbPath = dbPath;
	}

	public synchronized static void initialize(String dbPath) throws IOException{
		boolean newDb = (new File(dbPath)).exists();
		if(!newDb){
			(new File(dbPath)).getParentFile().mkdir();
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
			connection = DriverManager.getConnection(JDBC_DRIVER+dbPath);  
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
		s1.execute("create table controller(ip varchar, port varchar);");

		Statement s2 = connection.createStatement();
		s2.execute("create table storage (id integer primary key autoincrement, iscsi_name text not null, iscsi_hostname text not null,"+
		"iscsi_port integer default 3260, iscsi_iqn text not null, unique(iscsi_name,iscsi_hostname,iscsi_iqn) );");
		
		Statement s3 = connection.createStatement();
		s3.execute("create table storage_allocations (id integer primary key autoincrement, iscsi integer not null, iscsi_volume text not null, hypervisor integer not null,"+
				"vm text not null, foreign key(iscsi) references storage(id), foreign key(hypervisor) references host(id),"
				+ "unique(iscsi,iscsi_volume) );");

		Statement s4 = connection.createStatement();
		s4.execute(
				"create table host(id integer primary key autoincrement, nome varchar not null," +
				"ip varchar , port varchar);");
		
		s1.close();
		s2.close();
		s3.close();
		s4.close();
	}
	
	//******************** ISCSI Part ****************************************
	public List<ISCSITarget> getAllISCSITargets() throws IOException{
		List<ISCSITarget> l = null; 
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery(
					"select * from storage;");
			l = new ArrayList<ISCSITarget>();
			while(rs.next()){
				ISCSITarget target = new ISCSITarget(rs.getInt(1),rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5));
				l.add(target);
			}
			rs.close();
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

		return l;
	}
	
	public void insertISCSITarget(ISCSITarget target) throws IOException{
		try {
			Statement s = connection.createStatement();
			s.execute("insert into storage (iscsi_name, iscsi_hostname, iscsi_port, iscsi_iqn) values(\""+ 
					target.name + "\" ,\"" + target.hostname +"\" ," +target.port + ", \"" + target.iqn +"\");"); 

			ResultSet rs = s.executeQuery("SELECT last_insert_rowid()");
			rs.next();
			target.id = rs.getInt(1);

			rs.close();
			s.close();
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}
	
	public ISCSITarget getTargetById(int id) throws IOException{
		ISCSITarget result = null;
		try{
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery("select * from storage where id="+id);
			if(rs.next()){
				result = new ISCSITarget(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5));
				rs.close();
				s.close();
			}else{
				throw new IOException("can't find iscsi for host H"+id);
			}
		}catch (SQLException ex) {
			ex.printStackTrace();
			throw new IOException(ex.getMessage());
		}
		
		return result;
	}
	
	public void deleteISCSITarget(ISCSITarget target) throws IOException{
		try {

			Statement s = connection.createStatement();
			s.execute("delete from storage where id = \""+target.id+"\";");
			s.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}
	
	public void insertVolumeAllocation(VolumeAllocation alloc) throws IOException{
		try {
			Statement s = connection.createStatement();
			s.execute("insert into storage_allocations (iscsi, iscsi_volume, hypervisor, vm) values("+ 
					alloc.iscsiID + " ,\"" + alloc.volume +"\" , " + alloc.hostID +",\"" + alloc.vmName + "\");"); 

			ResultSet rs = s.executeQuery("SELECT last_insert_rowid()");
			rs.next();
			alloc.id = rs.getInt(1);

			rs.close();
			s.close();
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}
	
	public VolumeAllocation getAllocationFromVolume(String volumeName) throws IOException{
		VolumeAllocation vol = null;
		
		try{
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery(
				"select * from storage_allocations where iscsi_volume='"+volumeName+"'");
			if(rs.next())
				vol = new VolumeAllocation(rs.getInt(1),rs.getInt(2), rs.getString(3), rs.getInt(4),rs.getString(5));
			rs.close();
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
		return vol;
	}
	
	/**
	 * Returns allocations for specified iscsiID
	 * @param iscsiID
	 * @return
	 * @throws IOException
	 */
	public List<VolumeAllocation> getISCSIVolumes(int iscsiID) throws IOException{
		List<VolumeAllocation> l = null; 
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery(
					"select * from storage_allocations where iscsi="+iscsiID+";");
			l = new ArrayList<VolumeAllocation>();
			while(rs.next()){
				VolumeAllocation alloc = new VolumeAllocation(rs.getInt(1),rs.getInt(2), rs.getString(3), rs.getInt(4),rs.getString(5));
				l.add(alloc);
			}
			rs.close();
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

		return l;
	}
	
	public void deleteVolumeAllocation(int hypervisorID, String guest) throws IOException{
		try {

			Statement s = connection.createStatement();
			s.execute("delete from storage_allocations where hypervisor = " + hypervisorID + " and vm=\""+guest+"\";");
			s.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}
	
	//******************* Hypervisor Part ***********************************

	public void insertHypervisor(Hypervisor h) throws IOException{
		try {
			Statement s = connection.createStatement();
			s.execute("insert into host (nome,ip,port) values(\""+ 
					h.getName() + "\" ,\"" + h.getHostname() +"\"," + h.getPort() +");"); 

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
