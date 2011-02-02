package javatools.database;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

The class OracleDatabase implements the Database-interface for an 
Oracle SQL data base. Make sure that the file "classes12.jar" of the 
Oracle distribution is in the classpath. When using Eclipse, add 
the file via Project ->Properties ->JavaBuildPath ->Libraries 
->ExternalJARFile.<BR>
Example:
<PRE>
     Database d=new OracleDatabase("user","password");     
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
behave according to the conventions of Oracle. For example, the ANSI SQL datatype
BOOLEAN is mapped to NUMBER(1). Furthermore, VARCHAR string literals print 
inner quotes as doublequotes.
<P>
Oracle (and only Oracle!) often complains "ORA-01000: maximum open cursors exceeded".
Try the following:
<UL>
<LI> Avoid query() whenever possible and use executeQuery(), queryValue and 
query/ResultIterator instead, because these close the open resources automatically.
<LI> If you use query(), be sure to call Database.close(ResultSet) afterwards.
<LI> Reset the connection from time to time by calling resetConnection(). (This
is an Oracle-specific trick).
<LI> Increase the number of cursors in Oracle by saying 
dabatase.executeUpdate("ALTER SYSTEM SET open_cursors=1000000 scope=both")
</UL>
The simplest solution, though, is to use another database. Postgres and MySQL can 
be downloaded for free, PostgresDatabase.java and MySQLDatabase.java provide the 
respective Java-adapters.
*/
public class OracleDatabase extends Database {
  
  /** Prepares the query internally for a call (deletes trailing semicolon)*/ 
  protected String prepareQuery(String sql) {
    if(sql.endsWith(";")) return(sql.substring(0,sql.length()-1));
    else return(sql);
  }
  
  /** Constructs a non-functional OracleDatabase for use of getSQLType*/
  public OracleDatabase()  {
    java2SQL.put(Boolean.class,bool);    
    java2SQL.put(boolean.class,bool);    
    java2SQL.put(String.class,varchar);
    java2SQL.put(Long.class,bigint);
    java2SQL.put(long.class,bigint);
    type2SQL.put(Types.VARCHAR,varchar);
    type2SQL.put(Types.BOOLEAN,bool); 
    type2SQL.put(Types.BIGINT,bigint);
  }
  
  /** Constructs a new OracleDatabase from a user, a password and a host*/
  public OracleDatabase(String user, String password, String host) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException  {
    this(user,password,host,null);
  }

  /** Constructs a new OracleDatabase from a user, a password and a host*/
  public OracleDatabase(String user, String password, String host, String port) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException  {
    this(user,password,host,null,null);
  }

  /** Constructs a new OracleDatabase from a user, a password and a host
   * @throws ClassNotFoundException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   * @throws SQLException */
  public OracleDatabase(String user, String password, String host, String port, String inst) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException  {
    this();
    if(password==null) password="";
    if(host==null) host="localhost";
    if(port==null) port="1521";
    if(inst==null) inst="oracle";
    connectionString="jdbc:oracle:thin:"+user+"/"+password+"@"+host+":"+port+":"+inst;
    Driver driver;
    driver= (Driver)Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
    DriverManager.registerDriver( driver );
    resetConnection();
    description="ORACLE database "+user+"/"+password+" at "+host+":"+port+" instance "+inst;
  }  
  
  /** Constructs a new OracleDatabase from a user and a password on the localhost*/
  public OracleDatabase(String user, String password) throws Exception {
    this(user,password,"localhost");
  }

  /** Holds the String by which the connection can be reset*/
  protected String connectionString;

  /** Resets the connection. */
  public void resetConnection() throws SQLException {
    close(connection);    
    connection = DriverManager.getConnection(connectionString);
    connection.setAutoCommit( true );    
  }
  
  /** Makes an SQL query limited to n results */
  public String limit(String sql, int n) {
    n++;
    Matcher m=Pattern.compile("\bwhere\b",Pattern.CASE_INSENSITIVE).matcher(sql);
    if(!m.find()) return(sql+" WHERE ROWNUM<"+n); 
    return(sql.substring(0,m.end())+" ROWNUM<"+n+" AND "+sql.substring(m.end()));
  }
  
  // -------------------------------------------------------------------------------
  // ------------------ Datatypes --------------------------------------------------
  // -------------------------------------------------------------------------------

  public static class Varchar extends SQLType.ANSIvarchar {
    public Varchar(int size) {
      super(size);
    }  
    public Varchar() {
      super();
    } 
    public String toString() {
      return("VARCHAR2("+scale+")");
    }
    public String format(Object o) {
      String s=o.toString().replace("'", "''");
      if(s.length()>scale) s=s.substring(0,scale);
      return("'"+s+"'");
    } 
  }
  public static Varchar varchar=new Varchar();

  public static class Bool extends SQLType.ANSIboolean {
    public Bool() {
      super();
      typeCode=java.sql.Types.INTEGER;
    }        
    public String format(Object o) {
      if(super.format(o).equals("true")) return("1");
      else return("0");
    }            
    public String toString() {
      return("NUMBER(1)");
    }            
  }  
  public static Bool bool=new Bool();  

  public static class Bigint extends SQLType.ANSIBigint {
    public String toString() {
      return("NUMBER(37)");
    }            
  }  
  public static Bigint bigint=new Bigint();  

};  
