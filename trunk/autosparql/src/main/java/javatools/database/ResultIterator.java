package javatools.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import javatools.datatypes.PeekIterator;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

  This class wraps a ResultSet into an Iterator over a given Class.
  It requires a method that can wrap a row of a ResultSet into an object 
  of the given class.<BR>
  Example:
  <PRE>
  // We need this class to define how to construct an Employer from a table row
  public static class EmployerWrapper implements ResultWrapper&lt;Employer> {
  
     // Wraps the current row in a ResultSet into an Employer
     public Employer wrap(ResultSet r) {  
       return(new Employer(r.getString(1),r.getDouble(2)); 
     }
     
  }
  
  Database d=new OracleDatabase("scott","tiger");
  for(Employer e : d.query("SELECT * FROM employers WHERE salary>1000",
                           new EmployerConstructor())) {
     System.out.println(e);
  }
  </PRE>
 */ 
public class ResultIterator<T> extends PeekIterator<T> {  
      /** Wraps the current row in a ResultSet into a T*/
      public static interface ResultWrapper<T> {
        /** Wraps the current row in a ResultSet into a T*/
        public T wrap(ResultSet r) throws Exception ;
      }
      /** Holds the resultSet*/
      protected ResultSet resultSet;
      /** Holds the constructor to be used for each row */
      protected ResultWrapper<T> constructor;
      /** Creates a ResultIterator for a ResultSet*/
      public ResultIterator(ResultSet s, ResultWrapper<T> cons) {
        resultSet=s;
        constructor=cons;
      } 
      /** For subclasses*/
      protected ResultIterator() {        
      }
      
      public T internalNext() throws Exception {
        if(!resultSet.next()) return(null);
        return(constructor.wrap(resultSet));
      }

      /** Closes the resultset and the underlying statement*/
      public void close() {
        Database.close(resultSet);
      }

      /** Closes the resultset */
      public void finalize() {
        close();
      }

      /** ResultWrapper for a single String column */
      public static final ResultWrapper<String> StringWrapper=new ResultWrapper<String>() {
        public String wrap(ResultSet r) throws SQLException {
          return(r.getString(1));
        }
      };

      /** ResultWrapper for String columns */
      public static final ResultWrapper<String[]> StringsWrapper=new ResultWrapper<String[]>() {
        public String[] wrap(ResultSet r) throws SQLException {          
          String[] result=new String[r.getMetaData().getColumnCount()];
          for(int i=0;i<result.length;i++) result[i]=r.getString(i+1);
          return(result);
        }
      };
      
      /** ResultWrapper for a single Long column. Returns NULL for NULL */
      public static final ResultWrapper<Long> LongWrapper=new ResultWrapper<Long>() {
        public Long wrap(ResultSet r) throws SQLException {
          long l=r.getLong(1);
          return(r.wasNull()?null:l);
        }
      };

      /** ResultWrapper for a single Double column. Returns NULL for NULL */
      public static final ResultWrapper<Double> DoubleWrapper=new ResultWrapper<Double>() {
        public Double wrap(ResultSet r) throws SQLException {
          double l=r.getDouble(1);
          return(r.wasNull()?null:l);
        }
      };
      
      /** ResultWrapper for several Doubles. Returns NULL for NULL */
      public static final ResultWrapper<Double[]> DoublesWrapper=new ResultWrapper<Double[]>() {
        public Double[] wrap(ResultSet r) throws SQLException {
          Double[] result=new Double[r.getMetaData().getColumnCount()];
          for(int i=0;i<result.length;i++) {
            result[i]=r.getDouble(i+1);
            if(r.wasNull()) result[i]=null;
          }
          return(result);
        }
      };
      
      /** ResultWrapper for a single Integer column */
      public static final ResultWrapper<Integer> IntegerWrapper=new ResultWrapper<Integer>() {
        public Integer wrap(ResultSet r) throws SQLException {
          int l=r.getInt(1);
          return(r.wasNull()?null:l);
        }
      };
}
