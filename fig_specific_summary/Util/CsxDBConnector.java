package Util;
import java.sql.*;
/* citations metadata
+------------+---------------------+------+-----+---------+----------------+
| Field      | Type                | Null | Key | Default | Extra          |
+------------+---------------------+------+-----+---------+----------------+
| id         | bigint(20) unsigned | NO   | PRI | NULL    | auto_increment | 
| cluster    | bigint(20) unsigned | YES  | MUL | NULL    |                | 
| authors    | text                | YES  |     | NULL    |                | 
| title      | varchar(255)        | YES  | MUL | NULL    |                | 
| venue      | varchar(255)        | YES  | MUL | NULL    |                | 
| venueType  | varchar(20)         | YES  | MUL | NULL    |                | 
| year       | int(11)             | YES  | MUL | NULL    |                | 
| pages      | varchar(20)         | YES  |     | NULL    |                | 
| editors    | text                | YES  |     | NULL    |                | 
| publisher  | varchar(100)        | YES  | MUL | NULL    |                | 
| pubAddress | varchar(100)        | YES  |     | NULL    |                | 
| volume     | int(11)             | YES  |     | NULL    |                | 
| number     | int(11)             | YES  |     | NULL    |                | 
| tech       | varchar(100)        | YES  |     | NULL    |                | 
| raw        | text                | YES  |     | NULL    |                | 
| paperid    | varchar(100)        | NO   | MUL | NULL    |                | 
| self       | tinyint(4)          | NO   | MUL | 0       |                | 
+------------+---------------------+------+-----+---------+----------------+

papers schema
+-----------------+---------------------+------+-----+---------------------+-------+
| Field           | Type                | Null | Key | Default             | Extra |
+-----------------+---------------------+------+-----+---------------------+-------+
| id              | varchar(100)        | NO   | PRI | NULL                |       | 
| version         | int(10) unsigned    | NO   | MUL | NULL                |       | 
| cluster         | bigint(20) unsigned | YES  | MUL | NULL                |       | 
| title           | varchar(255)        | YES  | MUL | NULL                |       | 
| abstract        | text                | YES  |     | NULL                |       | 
| year            | int(11)             | YES  | MUL | NULL                |       | 
| venue           | varchar(255)        | YES  |     | NULL                |       | 
| venueType       | varchar(20)         | YES  |     | NULL                |       | 
| pages           | varchar(20)         | YES  |     | NULL                |       | 
| volume          | int(11)             | YES  |     | NULL                |       | 
| number          | int(11)             | YES  |     | NULL                |       | 
| publisher       | varchar(100)        | YES  |     | NULL                |       | 
| pubAddress      | varchar(100)        | YES  |     | NULL                |       | 
| tech            | varchar(100)        | YES  |     | NULL                |       | 
| public          | tinyint(4)          | NO   |     | 1                   |       | 
| ncites          | int(10) unsigned    | NO   | MUL | 0                   |       | 
| versionName     | varchar(20)         | YES  | MUL | NULL                |       | 
| crawlDate       | timestamp           | NO   | MUL | CURRENT_TIMESTAMP   |       | 
| repositoryID    | varchar(15)         | YES  | MUL | NULL                |       | 
| conversionTrace | varchar(255)        | YES  |     | NULL                |       | 
| selfCites       | int(10) unsigned    | NO   | MUL | 0                   |       | 
| versionTime     | timestamp           | NO   | MUL | 0000-00-00 00:00:00 |       | 
+-----------------+---------------------+------+-----+---------------------+-------+

 */



public class CsxDBConnector {
	Connection conn = null;
	Statement stmt = null;
	public CsxDBConnector()
	{
		try
        {
            String userName = "csx-read";
            String password = "csx-read";
            //String url = "jdbc:mysql://brick3.ist.psu.edu/citeseerx";
            String url = "jdbc:mysql://localhost/citeseerx";
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, userName, password);
            System.out.println ("Database connection established");
            stmt = conn.createStatement();
        }
        catch (Exception e)
        {	
            System.err.println ("Cannot connect to database server");
            e.printStackTrace();
        }
       
	}
	
	public void close()
	{
		if (conn != null)
        {
            try
            {
                conn.close ();
                System.out.println ("Database connection terminated");
            }
            catch (Exception e) 
            { e.printStackTrace(); 
            }
        }
	}
	
	public ResultSet executeQuery(String query)
	{ 	ResultSet r = null;
		try
		{	r = stmt.executeQuery(query);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return r;
	}
	
	
	public static void main(String args[])
	{
		CsxDBConnector db = new CsxDBConnector();
		try{
			ResultSet r = db.executeQuery("select * from citations where paperid = '10.1.1.7.788';");
			
			while(r.next())
			{
				Util.jout(r.getString("raw")+"\n");
			}
			r.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		db.close();
	}
}
