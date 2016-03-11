package gea.adapters;

import gea.properties.PropertyManagerException;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import oracle.jdbc.OraclePreparedStatement;

import org.json.JSONObject;

import gea.tasklist.Tasklist;
//DEFINIMOS LA CLASE DE CONEXION
public class OracleConnector {
	//DECLARAMOS LAS VARIABLES DE BASE DE DATOS
	private Connection con;
	private String user = null, password = null, db = null, host = null, port = null;
	private Statement stmt;
	private OraclePreparedStatement pstm;
	private ResultSet rset;
	private String query;
	//DECLARAMOS LOS CONSTRUCTORES
	public OracleConnector() throws IOException, PropertyManagerException{
		JSONObject config = Tasklist.getConfig();
		JSONObject db = config.getJSONObject("DATABASE");
		this.host = db.getString("HOST");
		this.port = db.getString("PORT");
		this.user = db.getString("USERNAME");
		this.password = db.getString("PASSWORD");
		this.db = db.getString("DATABASE");
	}
	public OracleConnector(String user,String password, String db, String host, String port){
		this.user = user;
		this.password = password;
		this.db = db;
		this.host = host;
		this.port = port;
	}
	//METODO DE CONECCION -  DEVUELVE EL CONECTOR
	public void connect(){
		if(this.host != null || this.port != null || this.user != null || this.password != null)
		{
			try{
				this.con = DriverManager.getConnection(
				"jdbc:oracle:thin:@"+this.host+":"+this.port+"/"+this.db, this.user,
				this.password);
				this.stmt = this.con.createStatement();
			}
			catch(Exception e){
				System.out.println("Existe un error al conectar con la base de datos - ERROR: 01");
				System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------");
				System.out.println("Host --> " + this.host);
				System.out.println("Port --> " + this.port);
				System.out.println("Db   --> " + this.db);
				System.out.println("User --> " + this.user);
				System.out.println("Pass --> " + this.password);
				System.out.println("============================================================================================================================================================");
				e.printStackTrace();
				System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------");
			}
		}
		else
		{
			System.out.println("Faltan datos para iniciar una conección con la DB Oracle. ERROR 02");
		}
	}
	public ResultSet query(String query){
		try{
			 this.rset = stmt.executeQuery(query);
			 return rset;
		}
		catch(Exception e){
			System.out.println("Consulta erronea: "+query+"\n\rRevise la consulta y vuelva a intentarlo\n\r"+e.getMessage());
			return null;
		}
	}
	//METODO PARA REALIZAR OPERACIONES DE UPDATE
	public boolean update(){
		try{
			 pstm.executeUpdate();
			 return true;
		}
		catch(Exception e){
			System.out.print("Consulta erronea: "+this.query+"\n\rRevise la consulta y vuelva a intentarlo\n\r"+e.getMessage());
			return false;
		}
	}
	public boolean update(String query){
		try{
			 stmt.executeUpdate(query);
			 return true;
		}
		catch(Exception e){
			System.out.print("Consulta erronea: "+query+"\n\rRevise la consulta y vuelva a intentarlo\n\r"+e.getMessage());
			return false;
		}
	}
	//METODO PARA REALIZAR OPERACIONES DE DELETE
		public boolean delete(String query){
			try{
				 stmt.executeUpdate(query);
				 return true;
			}
			catch(Exception e){
				System.out.print("Consulta erronea: "+query+"\n\rRevise la consulta y vuelva a intentarlo\n\r"+e.getMessage());
				return false;
			}
		}
	//METODO DE CIERRE DE CONECCION A LA DB
	public void closeLink(){
		try
		{
			this.con.close();
		}
		catch(Exception e){
			System.out.println("No se puede cerrar el conector, ya se encuentra cerrado o existe un error. ERROR 03");
		}
	}
	public void closeStmt(){
		try
		{
			this.stmt.close();
		}
		catch(Exception e){
			System.out.println("No se puede cerrar el Statement, ya se encuentra cerrado o existe un error. ERROR 04");
		}
	}
	public void closeRset(){
		try
		{
			this.rset.close();
		}
		catch(Exception e){
			System.out.println("No se puede cerrar la consulta, ya se encuentra cerrado o existe un error. ERROR 035");
		}
	}
	public void close(){
		try
		{
			this.closeStmt();
			this.closeRset();
			this.closeLink();
		}
		catch(Exception e){
		}
	}
	public void closeOP(){
		try
		{
			this.closeStmt();
			this.closeLink();
		}
		catch(Exception e){
		}
	}
	//METODOS DE TESTEO
	public void TestConectorOracle(){
		System.out.println("-------- Oracle JDBC Connection Testing ------");
		try {
 
			Class.forName("oracle.jdbc.driver.OracleDriver");
 
		} catch (ClassNotFoundException e) {
 
			System.out.println("Where is your Oracle JDBC Driver?");
			e.printStackTrace();
			return;
 
		}
 
		System.out.println("Oracle JDBC Driver Registered!");
 
		Connection connection = null;
 
		try {
 
			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:@"+this.host+":"+this.port+":"+this.db, this.user,
					this.password);
 
		} catch (SQLException e) {
 
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
 
		}
 
		if (connection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
	}
	//GETTERS AND SETTERS
	public void setCon(Connection con) {
		this.con = con;
	}
	public Connection getCon() {
		return this.con;
	}
	public void setDb(String db) {
		this.db = db;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setUser(String user) {
		this.user = user;
	}
	// NUEVAS FUNCIONES
	public OraclePreparedStatement prepare(String query){
		 try {
			this.query = query;
			this.pstm  = (OraclePreparedStatement)this.con.prepareStatement(query);
			return this.pstm;
		} catch (SQLException e) {
			System.out.println("IMPOSIBLE GENERAR STATMENT, ERROR DE CONSULTA: "+query+"\n\r"+e.getMessage());
			return null;
		}
	}
	public boolean commit(){
		try {
			this.con.commit();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
}
