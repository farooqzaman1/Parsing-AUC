package Util;
import java.sql.*;



public class LocalDBConnector {

	Connection conn = null;
	Statement stat = null;
	
	public LocalDBConnector()
	{
		try{
			Class.forName("org.sqlite.JDBC");
			
			//conn = DriverManager.getConnection("jdbc:sqlite:../dict/algorithms.db.back4_19_2011");
			conn = DriverManager.getConnection("jdbc:sqlite:../../algorithm_dictionary/dict/algorithms.db");
			stat = conn.createStatement();
			Util.jout("Local Connection Successful!!\n");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try
		{
			conn.close();
			Util.jout("Local Connection closed!!\n");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public ResultSet executeQuery(String query)
	{
		ResultSet r = null;
		try
		{
			r = stat.executeQuery(query);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return r;
	}
	
	/**
	 * 
	 * @param query
	 * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
	 * or -1 in case of exception
	 */
	public int executeUpdate(String query)
	{
		int r = -1;
		try
		{
			r = stat.executeUpdate(query);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return r;
	}
	
	public static void main(String args[])
	{
		LocalDBConnector db = new LocalDBConnector();
		db.close();
		
	}
}
