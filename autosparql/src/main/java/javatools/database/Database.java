package javatools.database;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javatools.administrative.Announce;
import javatools.administrative.D;
import javatools.filehandlers.CSVLines;
import javatools.filehandlers.UTF8Reader;
import javatools.filehandlers.UTF8Writer;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
 * 
 * This abstract class provides a simple Wrapper for an SQL data base. It is
 * implemented by OracleDatabase, PostgresDatabase and MySQLDatabase. <BR>
 * Example:
 * 
 * <PRE>
 * 
 * Database d=new OracleDatabase("user","password"); 
 * for(String food : d.query("SELECT foodname FROM food", ResultIterator.StringWrapper)) {
 *   System.out.print(food);
 * } 
 * -> Pizza Spaghetti Saltimbocca
 * </PRE>
 *  
 * It is possible to execute multiple INSERT statements by a bulk loader: 
 * <PRE>
 *   d=new OracleDatabase(...);
 *   Database.Inserter i=d.newInserter(tableName, Types.INTEGER, Types.VARCHAR);
 *   i.insert(7,"Hallo");
 *   i.insert(8,"Ciao");   
 *   ...
 *   i.close();
 * </PRE>
 * 
 * The inserters are automatically flushed every 1000 insertions and when
 * closed. They are flushed and closed when the database is closed.
 * <P>
 * Unfortunately, the same datatype is called differently on different database
 * systems, behaves differently and is written down differently. There is an
 * ANSI standard, but of course nobody cares. This is why Database.java provides
 * a method getSQLType(int), which takes any of the SQL datatypes defined in
 * java.sql.Types (e.g. VARCHAR) and returns an object of the class
 * javatools.SQLType. This object then behaves according to the conventions of
 * the respective database system. Each implementation of Database.java should
 * return SQLType-objects tailored to the specific database (e.g. OracleDatabase
 * maps BOOLEAN to NUMBER(1) and replaces quotes in string literals by double
 * quotes). By default, the ANSI datatypes are returned.<BR>
 * Example:
 * 
 * <PRE>
 * 
 * Database d=new OracleDatabase("user","password");
 * d.getSQLType(java.sql.Types.VARCHAR).format("Bobby's") 
 * -> 'Bobby"s' 
 * 
 * d=new MySQLDatabase("user","password","database");
 * d.getSQLType(java.sql.Types.VARCHAR).format("Bobby's") 
 * -> 'Bobby\'s'
 * 
 * </PRE>
 * 
 * Technical issues: Implementations of Database.java can extend the ANSI type
 * classes given in SQLType.java and they can modify the maps java2SQL and
 * type2SQL provided by Database.java. See OracleDatabase.java for an example.
 * For each datatype class, there should only be one instance (per scale).
 * Unfortunately, Java does not allow instances to have different method
 * implementation, so that each datatype has to be a class. It would be
 * convenient to handle datatypes as enums, but enums cannot be extended. To
 * facilitate modifying the ANSI types for subclasses, the getSQLType-method is
 * non-static. Implementations of this class should have a noarg-constructor to
 * enable calls to getSQLType without establishing a database connection.

 */
public class Database {

  /** Handle for the database */
  protected Connection connection;

  /** Describes this database */
  protected String description = "Unconnected default database";

  /** The type of the resultSet (Forward only by default) */
  protected int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

  /** The concurrency type of the resultSet (read only by default) */
  protected int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;

  /** Returns the connection */
  public Connection getConnection() {
    return (connection);
  }

  /** Holds all active inserters to close them in the end*/
  protected List<Inserter> inserters=new ArrayList<Inserter>();  

  /** The mapping from Java to SQL */
  public Map<Class, SQLType> java2SQL = new HashMap<Class, SQLType>();
  {
    java2SQL.put(Boolean.class, SQLType.ansiboolean);
    java2SQL.put(boolean.class, SQLType.ansiboolean);
    java2SQL.put(String.class, SQLType.ansivarchar);
    java2SQL.put(java.util.Date.class, SQLType.ansitimestamp);
    java2SQL.put(java.util.Calendar.class, SQLType.ansitimestamp);
    java2SQL.put(int.class, SQLType.ansiinteger);
    java2SQL.put(Integer.class, SQLType.ansiinteger);
    java2SQL.put(long.class, SQLType.ansibigint);
    java2SQL.put(Long.class, SQLType.ansibigint);
    java2SQL.put(float.class, SQLType.ansifloat);
    java2SQL.put(Float.class, SQLType.ansifloat);
    java2SQL.put(double.class, SQLType.ansifloat);
    java2SQL.put(Double.class, SQLType.ansifloat);
    java2SQL.put(Character.class, SQLType.ansichar);
    java2SQL.put(char.class, SQLType.ansichar);
  };

  /** The mapping from type codes (as defined in java.sql.Types) to SQL */
  public Map<Integer, SQLType> type2SQL = new HashMap<Integer, SQLType>();
  {
    type2SQL.put(Types.VARCHAR, SQLType.ansivarchar);
    type2SQL.put(Types.TIMESTAMP, SQLType.ansitimestamp);
    type2SQL.put(Types.DATE, SQLType.ansitimestamp);
    type2SQL.put(Types.INTEGER, SQLType.ansiinteger);
    type2SQL.put(Types.DOUBLE, SQLType.ansifloat);
    type2SQL.put(Types.FLOAT, SQLType.ansifloat);
    type2SQL.put(Types.BOOLEAN, SQLType.ansiboolean);
    type2SQL.put(Types.CHAR, SQLType.ansichar);
    type2SQL.put(Types.BIGINT, SQLType.ansibigint);
  };

  /**
   * Prepares the query internally for a call (e.g. adds a semicolon). This
   * implementation does nothing
   */
  protected String prepareQuery(String sql) {
    return (sql);
  }

  /** Returns the resultSetConcurrency */
  public int getResultSetConcurrency() {
    return resultSetConcurrency;
  }

  /** Sets the resultSetConcurrency */
  public void setResultSetConcurrency(int resultSetConcurrency) {
    this.resultSetConcurrency = resultSetConcurrency;
  }

  /** Returns the resultSetType */
  public int getResultSetType() {
    return resultSetType;
  }

  /** Sets the resultSetType */
  public void setResultSetType(int resultSetType) {
    this.resultSetType = resultSetType;
  }

  /**
   * Returns the results for a query as a ResultSet with given type and
   * concurrency. The preferred way to execute a query is by the query(String,
   * ResultIterator) method, because it ensures that the statement is closed
   * afterwards. If the query is an update query (i.e. INSERT/DELETE/UPDATE) the
   * method calls executeUpdate and returns null. The preferred way to execute
   * an update query is via the executeUpdate method, because it does not create
   * an open statement.
   */
  public ResultSet query(CharSequence sqlcs, int resultSetType, int resultSetConcurrency) throws SQLException {
    String sql = prepareQuery(sqlcs.toString());
    if (sql.toUpperCase().startsWith("INSERT") || sql.toUpperCase().startsWith("UPDATE") || sql.toUpperCase().startsWith("DELETE")
        || sql.toUpperCase().startsWith("CREATE") || sql.toUpperCase().startsWith("DROP") || sql.toUpperCase().startsWith("ALTER")) {
      executeUpdate(sql);
      return (null);
    }
    try {
      return (connection.createStatement(resultSetType, resultSetConcurrency).executeQuery(sql));
    } catch (SQLException e) {
      throw new SQLException(sql + "\n" + e.getMessage());
    }
  }

  /**
   * Returns the results for a query as a ResultSet with default type and
   * concurrency (read comments!). The preferred way to execute a query is by
   * the query(String, ResultWrapper) method, because it ensures that the
   * statement is closed afterwards. If you use the query(String) method
   * instead, be sure to call Database.close(ResultSet) on the result set,
   * because this ensures that the underlying statement is closed. The preferred
   * way to execute an update query (i.e. INSERT/DELETE/UPDATE) is via the
   * executeUpdate method, because it does not create an open statement. If
   * query(String) is called with an update query, this method calls
   * executeUpdate automatically and returns null.
   */
  public ResultSet query(CharSequence sql) throws SQLException {
    return (query(sql, resultSetType, resultSetConcurrency));
  }

  /** Executes an SQL update query, returns the number of rows added/deleted */
  public int executeUpdate(CharSequence sqlcs) throws SQLException {
    String sql = prepareQuery(sqlcs.toString());
    try {
      Statement s = connection.createStatement();
      int result = s.executeUpdate(sql);
      close(s);
      return (result);
    } catch (SQLException e) {
      throw new SQLException(sql + "\n" + e.getMessage());
    }
  }

  /** Returns the results for a query as a ResultIterator */
  public <T> ResultIterator<T> query(CharSequence sql, ResultIterator.ResultWrapper<T> rc) throws SQLException {
    return (new ResultIterator<T>(query(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY), rc));
  }

  /** Returns a single value (or null) */
  public <T> T queryValue(CharSequence sql, ResultIterator.ResultWrapper<T> rc) throws SQLException {
    ResultIterator<T> results = new ResultIterator<T>(query(sql), rc);    
    T result = results.nextOrNull();
    results.close();
    return (result);
  }

  /** Returns TRUE if the resultset is not empty */
  public boolean exists(CharSequence sql) throws SQLException {
    ResultSet rs=query(sql);
    boolean result=rs.next();
    close(rs);
    return(result);
  }
  
  /** The minal column width for describe() */
  public static final int MINCOLUMNWIDTH = 3;

  /** The screen width for describe() */
  public static final int SCREENWIDTH = 120;

  /** Appends something to a StringBuilder with a fixed length */
  protected static void appendFixedLen(StringBuilder b, Object o, int len) {
    String s = o == null ? "null" : o.toString();
    if (s.length() > len) s = s.substring(0, len);
    b.append(s);
    for (int i = s.length(); i < len; i++)
      b.append(' ');
  }

  /**
   * Returns a String-representation of a ResultSet, maximally maxrows rows (or
   * all for -1)
   */
  public static String describe(ResultSet r, int maxrows) throws SQLException {
    StringBuilder b = new StringBuilder();
    int columns = r.getMetaData().getColumnCount();
    int width = SCREENWIDTH / columns - 1;
    if (width < MINCOLUMNWIDTH) {
      columns = SCREENWIDTH / (MINCOLUMNWIDTH + 1);
      width = MINCOLUMNWIDTH;
    }
    int screenwidth = (width + 1) * columns;
    for (int column = 1; column <= columns; column++) {
      appendFixedLen(b, r.getMetaData().getColumnLabel(column), width);
      b.append('|');
    }
    b.append('\n');
    for (int i = 0; i < screenwidth; i++)
      b.append('-');
    b.append('\n');
    for (; maxrows != 0; maxrows--) {
      if (!r.next()) {
        for (int i = 0; i < screenwidth; i++)
          b.append('-');
        b.append('\n');
        break;
      }
      for (int column = 1; column <= columns; column++) {
        appendFixedLen(b, r.getObject(column), width);
        b.append('|');
      }
      b.append('\n');
    }
    if (maxrows == 0 && r.next()) b.append("...\n");
    close(r);
    return (b.toString());
  }

  /** Returns a String-representation of a ResultSet */
  public static String describe(ResultSet r) throws SQLException {
    return (describe(r, -1));
  }

  /** Closes a connection */
  public static void close(Connection connection) {
    try {
      if ((connection != null) && !connection.isClosed()) connection.close();
    } catch (SQLException e) {
    }
  }

  /** Closes a statement */
  public static void close(Statement statement) {
    try {
      if (statement != null) statement.close();
    } catch (SQLException e) {
    }
  }

  /** Closes a result set */
  public static void close(ResultSet rs) {
    try {
      close(rs.getStatement());
    } catch (SQLException e) {
    }
    try {
      if (rs != null) rs.close();
    } catch (SQLException e) {
    }
  }

  /** Closes the connection */
  public void close() {
    while(inserters.size()!=0) inserters.get(0).close();     
    close(connection);
  }

  /** Closes the connection */
  public void finalize() {
    try {
      close();
    } catch (Exception e) {
    }
    ;
  }

  /** Returns an SQLType for the given Type as defined in java.sql.Types */
  public SQLType getSQLType(int t) {
    return (type2SQL.get(t));
  }

  /**
   * Returns an SQLType for the given Type as defined in java.sql.Types with a
   * scale
   */
  public SQLType getSQLType(int t, int scale) {
    SQLType s = getSQLType(t);
    s.scale = scale;
    return (s);
  }

  /** Returns an SQLType for the given class */
  public SQLType getSQLType(Class c) {
    return (java2SQL.get(c));
  }

  /** Formats an object appropriately (provided that its class is in java2SQL) */
  public String format(Object o) {
    SQLType t = getSQLType(o.getClass());
    if (t == null) t = getSQLType(String.class);
    return (t.format(o.toString()));
  }

  /**
   * Creates or rewrites an SQL table. Attributes is an alternating sequence of
   * a name (String) and a type (from java.sql.Type).
   */
  public void createTable(String name, Object... attributes) throws SQLException {
    try {
      executeUpdate("DROP TABLE " + name);
    } catch (SQLException e) {
    }
    StringBuilder b = new StringBuilder("CREATE TABLE ").append(name).append(" (");
    for (int i = 0; i < attributes.length; i += 2) {
      b.append(attributes[i]).append(' ');
      if(attributes[i + 1] instanceof Integer) {
        b.append(getSQLType((Integer) attributes[i + 1])).append(", ");
      } else {
        b.append(getSQLType((Class) attributes[i + 1])).append(", ");
      }
    }
    b.setLength(b.length() - 2);
    b.append(')');
    executeUpdate(b.toString());
  }

  /** Creates an index name*/
  public String indexName(String table, String... attributes) {
    StringBuilder indexName=new StringBuilder(table);
    for (String a : attributes) indexName.append(a);    
    indexName.append("Index");
    return(indexName.toString());
  }
  
  /** Returns the command to create one index on a table */
  public String createIndexCommand(String table, boolean unique, String... attributes) {
    StringBuilder sql = new StringBuilder("CREATE ");
    if (unique) sql.append("UNIQUE ");
    sql.append("INDEX ");
    sql.append(indexName(table, attributes));
    sql.append(" ON ").append(table).append(" (");
    for (String a : attributes)
      sql.append(a).append(", ");
    sql.setLength(sql.length() - 2);
    sql.append(")");
    return(sql.toString());
  }
  
  public void createIndex(String table, boolean unique, String... attributes) throws SQLException {
    String comand=createIndexCommand(table, unique, attributes);
    try {
      executeUpdate("DROP INDEX " + indexName(table,attributes)+"Index");
    } catch (SQLException e) {     
    }    
    executeUpdate(comand);
  }

  /** Creates non-unique single indices on a table */
  public void createIndices(String table, String... attributes) throws SQLException {
    for (String a : attributes) {
      createIndex(table, false, a);
    }
  }

  public String toString() {
    return (description);
  }

  /** Makes an SQL query limited to n results */
  public String limit(String sql, int n) {
    return(sql+" LIMIT "+n);
  }
  
  /** Runs a user-interface and closes */
  public void runInterface() {
    Announce.message("Connected to", this);
    while (true) {
      D.p("Enter an SQL query (possibly of multiple lines), followed by a blank line (or just a blank line to quit):");
      StringBuilder sql = new StringBuilder();
      String s;
      while ((s = D.r()).length() != 0)
        sql.append(s).append("\n");
      if (sql.length() == 0) break;
      sql.setLength(sql.length() - 1);
      Announce.doing("Querying database");
      if (sql.length() == 0) break;
      try {
        ResultSet result = query(sql.toString());
        Announce.done();
        if (result != null) D.p(describe(result, 50));
      } catch (SQLException e) {
        Announce.failed();
        e.printStackTrace(System.err);
        Announce.message("\n\n... but don't give up, try again!");
      }
    }
    Announce.doing("Closing database");
    close();
    Announce.done();
  }

  /** Represents a bulk loader*/
  public class Inserter implements Closeable {
    /** Holds the prepared statement*/
    protected PreparedStatement preparedStatement;
    /** Table where the data will be inserted*/
    protected String tableName;
    /** Column types*/
    protected SQLType[] columnTypes;
    /** Counts how many commands are in the batch*/
    protected int batchCounter = 0;
    /** Tells after how many commands we will flush the batch*/
    protected static final int BatchSize = 1000;
    
    /** Creates a bulk loader for a table with column types given by Java classes*/ 
    public Inserter(String table, Class... columnTypes) throws SQLException {
      this.columnTypes = new SQLType[columnTypes.length];
      for (int i = 0; i < columnTypes.length; i++) {
        this.columnTypes[i] = getSQLType(columnTypes[i]);
      }
      tableName = table;
      table = "INSERT INTO " + table + " VALUES(";
      for (int i = 0; i < columnTypes.length - 1; i++)
        table = table + "?, ";
      table += "?)";
      preparedStatement = connection.prepareStatement(table);
      inserters.add(this);      
    }
    
    /** Creates a bulk loader with column types from java.sql.Type */
    public Inserter(String table, int... columnTypes) throws SQLException {
      this.columnTypes = new SQLType[columnTypes.length];
      for (int i = 0; i < columnTypes.length; i++) {
        this.columnTypes[i] = getSQLType(columnTypes[i]);
      }
      tableName = table;
      table = "INSERT INTO " + table + " VALUES(";
      for (int i = 0; i < columnTypes.length - 1; i++)
        table = table + "?, ";
      table += "?)";
      preparedStatement = connection.prepareStatement(table);
      inserters.add(this);      
    }
    
    /** Returns the table name*/
    public String getTableName() {
      return tableName;
    }
    
    /** Inserts a row*/
    public void insert(Object... values) throws SQLException {
      try {
        for (int i = 0; i < values.length; i++) {    	 
          preparedStatement.setObject(i+1, values[i], columnTypes[i].getTypeCode());
        }
        preparedStatement.addBatch();
      }catch(SQLException e) {
        throw new SQLException("Bulk-insert into "+tableName+" "+Arrays.toString(values)+ "\n" + e.getMessage());
      }
      if (batchCounter++ % BatchSize == 0) flush();      
    }

    /** Flushes the batch*/
    public void flush() throws SQLException {
      try {
        preparedStatement.executeBatch();
        preparedStatement.clearBatch();
      } catch(SQLException e) {
        String details=e.getNextException()==null?"":e.getNextException().getMessage();
        throw new SQLException(e.getMessage()+"\n\n"+details);
      }
    }

    /** Flushes and closes*/
    public void close() {
      try {
        flush();
      } catch (SQLException e) {
      }
      try {
        preparedStatement.close();
      } catch (SQLException e) {
      }
      inserters.remove(this);
    }

    @Override
    protected void finalize() {
      close();      
    }
  }

  /** Returns an inserter for a table with specific column types*/
  public Inserter newInserter(String table, Class... argumentTypes) throws SQLException{
    return(new Inserter(table,argumentTypes));
  }
  
  /** Returns an inserter for a table with specific column types given as java.sql.Type constants*/
  public Inserter newInserter(String table, int... argumentTypes) throws SQLException{
    return(new Inserter(table,argumentTypes));
  }
  
  /** Produces a CSV version of the table*/
  public void makeCSV(String table,File output, char separator) throws IOException, SQLException {
    makeCSVForQuery("SELECT * FROM "+table,output, separator);
  }
  
  /** Produces a CSV version of the query*/
  public void makeCSVForQuery(String selectCommand,File output, char separator) throws IOException, SQLException {
    ResultSet r=query(selectCommand);
    Writer out=new UTF8Writer(output);
    int columns = r.getMetaData().getColumnCount();
    for (int column = 1; column <= columns; column++) {
      out.write(r.getMetaData().getColumnLabel(column));
      if(column==columns) out.write("\n");
      else out.write(separator+" ");
    }
    while(r.next()) {
      for (int column = 1; column <= columns; column++) {
        Object o=r.getObject(column);
        out.write(o==null?"null":o.toString());
        if(column==columns) out.write("\n");
        else out.write(separator+" ");
      }
    }
    close(r);
    out.close();
  }

  /** Loads a CSV file into a table*/
  public void loadCSV(String table,File input,boolean clearTable,char separator) throws IOException, SQLException {
    if(clearTable) executeUpdate("DELETE FROM "+table);
    ResultSet r=query(limit("SELECT * FROM "+table,1));
    int[] types=new int[r.getMetaData().getColumnCount()];
    for(int i=0;i<types.length;i++) types[i]=r.getMetaData().getColumnType(i+1);
    close(r);
    Inserter bulki=newInserter(table, types);
    boolean start=true;
    for(List<String> values : new CSVLines(input)) {
      if(start) {
        if(values.size()!=types.length) {          
          throw new SQLException("File "+input.getName()+" has "+values.size()+" columns, but table "+table+" has "+types.length);
        }
        start=false;
        continue;
      }
      if(values.size()!=types.length) {
        Announce.warning("Line cannot be read from file",input.getName(),"into table",table,":\n",values);
        continue;
      }
      bulki.insert((Object[])values.toArray());
    }
    bulki.close();
  }

  /** Test routine */
  public static void main(String[] args) throws Exception {  
  }
}
