package javatools.database;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.util.Calendar;

import javatools.administrative.D;
import javatools.parsers.DateParser;
import javatools.parsers.NumberFormatter;
import javatools.parsers.NumberParser;
/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

This abstract class provides a Wrapper for SQL datatypes. A datatype has a type code
(from java.sql.Types) and a scale. For example, INTEGER(17) is a datatype. A datatype 
can convert a datum to a suitable string. E.g. a TIMESTAMP datatype will convert the 
datum "2006-09-18" to <code>TIMESTAMP '2006-09-18 00:00:00.00'</code>. Of course,
the same SQL datatype is different for different database systems, so that different
databases have to implement different classes for the same datatype.<BR>
Example:
<PRE>
     Database d=new OracleDatabase("user","password");     
     d.getSQLType(java.sql.Types.VARCHAR).format("Bobby's")
     -> 'Bobby"s'
     d=new MySQLDatabase("user","password","database");     
     d.getSQLType(java.sql.Types.VARCHAR).format("Bobby's")
     -> 'Bobby\'s'
</PRE>
See <A HREF=http://troels.arvin.dk/db/rdbms/>here</A> for a comparison of database systems.
This class provides the ANSI implementations with generous conversion capabilities.<BR>
Example:
<PRE>
    SQLType.ANSItimestamp.format("13th of May 1980")
    --> TIMESTAMP '1980-05-13 00:00:00.00'
</PRE>

*/

public abstract class SQLType {
  /** Holds the type code as defined in java.sql.Types */
  protected int typeCode;
  /** Holds the scale as defined in java.sql.Types */
  protected int scale=0;  
  /** Formats an object to a valid SQL literal of the given type */
  public abstract String format(Object s);
  /** Returns the java.sql.Types type */
  public int getTypeCode() {
    return(typeCode);
  }
  /** Returns the scale java.sql.Types type */
  public int getScale() {
    return(scale);
  }
  
  //       ANSI Types
  public static class ANSIvarchar extends SQLType {
      public ANSIvarchar(int size) {
        typeCode=Types.VARCHAR;
        scale=size;
      }  
      public ANSIvarchar() {
        this(255);
      }        
      public String format(Object o) {
        String s=o.toString().replace("'", "\\'");
        if(s.length()>scale) s=s.substring(0,scale);
        return("'"+s+"'");
      }
      public String toString() {
        return("VARCHAR("+scale+")");
      }
  }
  public static ANSIvarchar ansivarchar=new ANSIvarchar();
  
  public static class ANSIchar extends SQLType {
    public ANSIchar(int size) {
      typeCode=Types.CHAR;
      scale=0;
    }  
    public ANSIchar() {
      this(0);
    }        
    public String format(Object o) {
      if(o==null) return("null");
      String s=o.toString();
      if(s.length()==0) return("null");
      char c=s.charAt(0);
      if(c=='\'') return("'\\''");
      else return("'"+c+"'");
    }
    public String toString() {
      return("CHAR");
    }
  }
  public static ANSIchar ansichar=new ANSIchar();

  public static class ANSItimestamp extends SQLType {
      public ANSItimestamp() {
        typeCode=Types.TIMESTAMP;
      }      
      public String format(Object o) {
        if(o instanceof String) {
          o=DateParser.asCalendar(DateParser.normalize(o.toString()));
        }
        if(o instanceof Calendar) {
          Calendar c=(Calendar)o;   
          // ISO time is YYYY-MM-DD 'T' HH:MM:SS.MMMM
          // SQL requires dropping the 'T'
          String s=NumberFormatter.ISOtime(c).replace("T ","");
          s=s.substring(0,s.indexOf('.'));
          return("TIMESTAMP '"+s+"'");
        }
        return(null);
      }
      public String toString() {
        return("TIMESTAMP");
      }
    }
  public static ANSItimestamp ansitimestamp=new ANSItimestamp();
  
  public static class ANSIinteger extends SQLType {
      public ANSIinteger(int size) {
        typeCode=Types.INTEGER;
        scale=size;
      }      
      public ANSIinteger() {
        this(0);
      }            
      public String format(Object o) {
        if(o instanceof Double) return(""+Math.rint((Double)o));
        if(o instanceof Float) return(""+Math.rint((Float)o));
        if(o instanceof Integer) return(""+((Integer)o).longValue());
        if(o instanceof Long) return(""+((Long)o).longValue());
        if(o instanceof String) return(""+NumberParser.getLong(o.toString()));
        return(null);
      }            
      public String toString() {
        if(scale==0) return("INTEGER");
        return("INTEGER("+scale+")");
      }      
    }
  public static ANSIinteger ansiinteger=new ANSIinteger();  
  
  public static class ANSIfloat extends SQLType {
      public ANSIfloat(int size) {
        typeCode=Types.FLOAT;
        scale=size;
      }  
      public ANSIfloat() {
        this(0);
      }        
      public String format(Object o) {
        if(o instanceof Double ||
           o instanceof Float ||
           o instanceof Integer ||
           o instanceof Long) return(o.toString());
        if(o instanceof String) return(NumberParser.getNumber(NumberParser.normalize(o.toString())));
        return(null);
      }            
      public String toString() {
        if(scale==0) return("FLOAT");
        return("FLOAT("+scale+")");
      }            
    };  
    
  public static ANSIfloat ansifloat=new ANSIfloat();  

  public static class ANSIboolean extends SQLType {
    public ANSIboolean() {
      typeCode=Types.BOOLEAN;
    }        
    public String format(Object o) {
      if(o instanceof Boolean) return(o.toString());
      if(o instanceof Float) return(Boolean.toString(((Float)o)!=0.0).toString());
      if(o instanceof Double) return(Boolean.toString(((Double)o)!=0.0));      
      if(o instanceof Integer) return(Boolean.toString(((Integer)o)!=0));
      if(o instanceof Long) return(Boolean.toString(((Long)o)!=0));      
      if(o instanceof String) return(Boolean.toString(Boolean.parseBoolean(o.toString())));
      return(null);
    }            
    public String toString() {
      return("BOOLEAN");
    }            
  };  

  public static ANSIboolean ansiboolean=new ANSIboolean();
  
  public static class ANSIBigint extends SQLType {
    public ANSIBigint() {
      typeCode=Types.BIGINT;
    }
    public String format(Object o) {
      if(o instanceof Double) return(""+Math.rint((Double)o));
      if(o instanceof Float) return(""+Math.rint((Float)o));
      if(o instanceof Integer) return(""+((Integer)o).longValue());
      if(o instanceof Long) return(""+((Long)o).longValue());
      if(o instanceof BigInteger) return(o.toString());
      if(o instanceof BigDecimal) return(((BigDecimal) o).toBigInteger().toString());
      if(o instanceof String) return(o.toString());
      return(null);
    }                
    public String toString() {
      return("BIGINT");
    }            
  }  
  public static ANSIBigint ansibigint=new ANSIBigint();  

  public static void main(String[] args) {
    D.p(ansifloat.format(0.0067));
  }
    
}
