package javatools.database;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javatools.administrative.D;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

The class PostgresDatabase implements the Database-interface for a
PostgreSQL data base. Make sure that the file "postgresql-<I>version</I>.jdbc3.jar" of the 
Postgres distribution is in the classpath. When using Eclipse, add 
the file via Project ->Properties ->JavaBuildPath ->Libraries 
->ExternalJARFile.<BR>
Example:
<PRE>
     Database d=new PostgresDatabase("user","password");     
     d.queryColumn("SELECT foodname FROM food WHERE origin=\"Italy\"")
     -> [ "Pizza Romana", "Spaghetti alla Bolognese", "Saltimbocca"]
     Database.describe(d.query("SELECT * FROM food WHERE origin=\"Italy\"")
     -> foodname |origin  |calories |
        ------------------------------
        Pizza Rom|Italy   |10000    |
        Spaghetti|Italy   |8000     |
        Saltimboc|Italy   |8000     |        
</PRE>
This class also provides SQL datatypes (extensions of SQLType.java) that
behave according to the conventions of Postgres. For example, VARCHAR string literals print 
inner quotes as doublequotes.*/
public class PostgresDatabase extends Database {

  /** Holds the default schema*/
  protected String schema=null;
  
  /** Constructs a non-functional OracleDatabase for use of getSQLType*/
  public PostgresDatabase() {
    java2SQL.put(String.class,varchar);
    type2SQL.put(Types.VARCHAR,varchar);    
  }
  
  /** Constructs a new Database from a user, a password and a host
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   * @throws SQLException */
  public PostgresDatabase(String user, String password, String database, String host, String port) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException  {
    this();
    if(password==null) password="";
    if(host==null) host="localhost";
    if(port==null) port="5432";
    Driver driver= (Driver)Class.forName("org.postgresql.Driver").newInstance();
    DriverManager.registerDriver( driver );
    connection = DriverManager.getConnection(
          "jdbc:postgresql://"+host+":"+port+(database==null?"":"/"+database),
        user,
        password
      );
    connection.setAutoCommit( true );
    description="Postgres database for "+user+" at "+host+":"+port+", database "+database+" schema "+schema;
  }  

  /** Constructs a new Database from a user, a password and a host*/
  public PostgresDatabase(String user, String password, String database, String host, String port,String schema) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException  {
    this(user,password,database,host,port);
    setSchema(schema);
  }
  /** Sets the default schema*/
  public void setSchema(String s) throws SQLException {
    executeUpdate("SET search_path TO "+s+", public");
    schema=s;
    description=description.substring(0,description.lastIndexOf(' '))+" "+schema;
  }
  
  public static class Varchar extends SQLType.ANSIvarchar {
    public Varchar(int size) {
      super(size);
    }  
    public Varchar() {
      super();
    } 
    public String toString() {
      return("VARCHAR("+scale+")");
    }
    public String format(Object o) {
      String s=o.toString().replace("'", "''").replace("\\", "\\\\");
      if(s.length()>scale) s=s.substring(0,scale);
      return("'"+s+"'");
    } 
  }
  public static Varchar varchar=new Varchar();
  /**  */
  public static void main(String[] args) throws Exception {
    Database d=new PostgresDatabase("postgres","postgres","postgres",null,null);
    //d.executeUpdate("CREATE table test (a integer, b varchar)");
    d.executeUpdate("INSERT into test values (1,2)");
    ResultSet s=d.query("select * from test");
    s.next();
    D.p(s.getString(1));
  }

}
