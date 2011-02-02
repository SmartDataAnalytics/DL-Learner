package javatools.database;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

The class MySQLDatabase implements the Database-interface for a
MySQL data base. Make sure that the file 
"mysql-connector-java-<i>version</i>-bin.jar" from the "MySQL Connector/J" 
(see the <A HREF=http://dev.mysql.com/downloads/ TARGET=_blank>MySQL-website</A>)
is in the classpath. When using Eclipse, add the file via Project 
->Properties ->JavaBuildPath ->Libraries ->ExternalJARFile.<BR>
Example:
<PRE>
     Database d=new MySQLDatabase("user","password","database");     
     d.queryColumn("SELECT foodname FROM food WHERE origin=\"Italy\"")
     -> [ "Pizza Romana", "Spaghetti alla Bolognese", "Saltimbocca"]
     Database.describe(d.query("SELECT * FROM food WHERE origin=\"Italy\"")
     -> foodname |origin  |calories |
        ------------------------------
        Pizza Rom|Italy   |10000    |
        Spaghetti|Italy   |8000     |
        Saltimboc|Italy   |8000     |        
</PRE>
*/
public class MySQLDatabase extends Database {  
  
  /** Constructs a new MySQLDatabase from a user and a password,
   * all other arguments may be null*/
  public MySQLDatabase(String user, String password, String database, String host, String port) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException  {
    Driver driver=(Driver)Class.forName("com.mysql.jdbc.Driver").newInstance();
    DriverManager.registerDriver( driver );
    if(host==null) host="localhost";
    if(database==null) database="test";
    if(port==null) port="";
    else port=":"+port; 
    connection = DriverManager.getConnection(
          "jdbc:mysql://"+host+port+"/"+database+"?user="+user+"&password="+password);
    connection.setAutoCommit( true );  
  }
  
  public static void main(String[] args) throws Exception {
  }
}
