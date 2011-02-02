package javatools.administrative;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javatools.database.Database;
import javatools.database.MySQLDatabase;
import javatools.database.OracleDatabase;
import javatools.database.PostgresDatabase;
import javatools.datatypes.FinalSet;
import javatools.filehandlers.FileLines;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

Provides an interface for an ini-File. The ini-File may contain parameters of the form
<PRE>
parameterName = value
...
</PRE>
It may also contain comments or section headers (i.e. anything that does not match the
above pattern). Parameter names are not case sensitive. Initial and terminal spaces
are trimmed for both parameter names and values. Boolean parameters accept multiple
ways of expressing "true" (namely "on", "true", "yes" and "active").<P>

To avoid passing around object handles, this class does not function as an object!
There is only one "static object". Example:
<PRE>
  // Read data from my.ini
  Parameters.init("my.ini");
  // Abort with error message if the following parameters are not specified
  Parameters.ensureParameters(
     "firstPar - some help text for the first parameter",
     "secondPar - some help text for the secondparameter"
  );
  // Retrieve the value of a parameter
  String p=Parameters.get("firstPar");
</PRE>
You can load parameters from multiple files. These will overlay.
*/
public class Parameters {
  /** Thrown for an undefined Parameter */
  public static class UndefinedParameterException extends RuntimeException {
    public UndefinedParameterException(String s, File f) {
      super("The parameter "+s+" is undefined in "+f);
    }
  }
  /** Holds the filename of the ini-file */
  public static File iniFile=null;

  /** Contains the values for the parameters*/
  public static Map<String,String> values=null;

  /** Holds the pattern used for ini-file-entries */
  public static Pattern INIPATTERN=Pattern.compile(" *(\\w+) *= *(.*) *");

  /** Holds words that count as "no" for boolean parameters */
  public static FinalSet<String> no=new FinalSet<String>(new String [] {
        "inactive",
        "off",
        "false",
        "no",
        "none"
  });

  /** Returns a value for a file or folder parameter */
  public static File getFile(String s) throws UndefinedParameterException {
    return(new File(get(s)));
  }

  /** Returns a value for a file or folder parameter, returning the default value if undefined*/
  public static File getFile(String s, File defaultValue) throws UndefinedParameterException {
    return(isDefined(s)?new File(get(s)):defaultValue);
  }

  /** Returns a value for an integer parameter*/
  public static int getInt(String s) throws UndefinedParameterException {
    return(Integer.parseInt(get(s)));
  }

  /** Returns a value for an integer parameter returning the default value if undefined*/
  public static int getInt(String s, int defaultValue) throws UndefinedParameterException {
    return(isDefined(s)?Integer.parseInt(get(s)):defaultValue);
  }

  /** Returns a value for an integer parameter*/
  public static double getDouble(String s) throws UndefinedParameterException {
    return(Double.parseDouble(get(s)));
  }

  /** Returns a value for an integer parameter returning the default value if undefined*/
  public static double getDouble(String s, double defaultValue) throws UndefinedParameterException {
    return(isDefined(s)?Double.parseDouble(get(s)):defaultValue);
  }

  /** Returns a value for a boolean parameter */
  public static boolean getBoolean(String s) throws UndefinedParameterException  {
    String v=get(s);
    return(!no.contains(v.toLowerCase()));
  }

  /** Returns a value for a boolean parameter, returning a default value by default */
  public static boolean getBoolean(String s, boolean defaultValue) {
    String v=get(s,defaultValue?"yes":"no");
    return(!no.contains(v.toLowerCase()));
  }

  /** Returns a value for a list parameter */
  public static List<String> getList(String s) throws UndefinedParameterException  {
    if(!isDefined(s)) return(null);
    return(Arrays.asList(get(s).split("\\s*,\\s*")));
  }

  /** Returns a value for a parameter*/
  public static String get(String s) throws UndefinedParameterException  {
    if(values==null) throw new RuntimeException("Call init() before get()!");
    String pname=s.indexOf(' ')==-1?s:s.substring(0,s.indexOf(' '));
    String v=values.get(pname.toLowerCase());
    if(v==null) throw new UndefinedParameterException(s,iniFile);
    return(v);
  }

  /** Returns a value for a parameter, returning a default value by default */
  public static String get(String s, String defaultValue)  {
    if(values==null) throw new RuntimeException("Call init() before get()!");
    String pname=s.indexOf(' ')==-1?s:s.substring(0,s.indexOf(' '));
    String v=values.get(pname.toLowerCase());
    if(v==null) return(defaultValue);
    return(v);
  }

  /** Initializes the parameters from a file*/
  public static void init(File f) throws IOException {
    if(f.equals(iniFile)) return;
    values=new TreeMap<String,String>();
    iniFile=f;
    for(String l : new FileLines(iniFile)) {
      Matcher m=INIPATTERN.matcher(l);
      if(!m.matches()) continue;
      String s=m.group(2).trim();
      if(s.startsWith("\"")) s=s.substring(1);
      if(s.endsWith("\"")) s=s.substring(0,s.length()-1);
      values.put(m.group(1).toLowerCase(),s);
    }
  }
  
  /** Seeks the file in all given folders*/
  public static void init(String filename, File... folders) throws IOException {
    boolean found=false;
    for(File folder : folders) {
      if(new File(folder,filename).exists()) {
        if(found) throw new IOException("INI-file "+filename+"occurs twice in given folders");
        init(new File(folder,filename));
        found=true;
      }
    }
  }
  
  /** Tells whether a parameter is defined */
  public static boolean isDefined(String s) {
    if(values==null) throw new RuntimeException("Call init() before get()!");
    String pname=s.indexOf(' ')==-1?s:s.substring(0,s.indexOf(' '));
    return(values.containsKey(pname.toLowerCase()));
  }
  
  /** Initializes the parameters from a file*/
  public static void init(String file) throws IOException {
    init(new File(file));
  }
  
  /** Reports an error message and aborts if the parameters are undefined.
   * p may contain strings of the form "parametername explanation"*/
  public static void ensureParameters(String... p) {
    if(values==null) throw new RuntimeException("Call init() before ensureParameters()!");
    boolean OK=true;
    StringBuilder b=new StringBuilder("The following parameters are undefined in ").append(iniFile);
    for(String s : p) {
      if(!isDefined(s)) {
        b.append("\n       ").append(s);
        OK=false;
      }
    }
    if(OK) return;
    Announce.error(b.toString());
  }

  /** Parses the arguments of the main method and tells whether a parameter is on or off */
  public static boolean getBooleanArgument(String[] args,String... argnames) {
    String arg=" ";
    for(String s : args) arg+=s+' ';
    String p="\\W(";
    for(String s : argnames) p+=s+'|';
    if(p.endsWith("|")) p=p.substring(0, p.length()-1);
    p+=")\\W";
    Matcher m=Pattern.compile(p).matcher(arg);
    if(!m.find()) return(false);
    String next=arg.substring(m.end()).toLowerCase();
    if(next.indexOf(' ')!=-1) next=next.substring(0, next.indexOf(' '));
    if(next.equals("off")) return(false);    
    if(next.equals("0")) return(false);    
    if(next.equals("false")) return(false);
    String previous=arg.substring(0,m.start()).toLowerCase();
    if(previous.indexOf(' ')!=-1) previous=previous.substring(previous.lastIndexOf(' ')+1);    
    if(previous.equals("no")) return(false);
    return(true);
  }
  
  /** Deletes all current values*/
  public static void reset() {
    iniFile=null;
    values=null;
  }
  
  /** Returns the database defined in this ini-file*/
  public static Database getDatabase() throws Exception {
    Parameters.ensureParameters("databaseSystem - either Oracle, Postgres or MySQL",
        "databaseUser - the user name for the database (also: databaseDatabase, databaseInst,databasePort,databaseHost,databaseSchema)",
        "databasePassword - the password for the database"
    );
        
    // Retrieve the obligatory parameters
    String system=Parameters.get("databaseSystem").toUpperCase();
    String user=Parameters.get("databaseUser");    
    String password=Parameters.get("databasePassword");
    String host=null;
    String schema=null;
    String inst=null;
    String port=null;
    String database=null;
    
    // Retrieve the optional parameters
    try {
      host=Parameters.get("databaseHost");
    } catch(Exception e){};
    try {
      schema=Parameters.get("databaseSchema");
    } catch(Exception e){};
    try {
      port=Parameters.get("databasePort");    
    } catch(Exception e){};          
    try {
      inst=Parameters.get("databaseSID");
    } catch(Parameters.UndefinedParameterException e) {}
    try {
      database=Parameters.get("databaseDatabase");
    } catch(Parameters.UndefinedParameterException e) {}    
    
    // Initialize the database
    // ------ ORACLE ----------
    if(system.equals("ORACLE")) {
      return(new OracleDatabase(user,password,host,port,inst));
    }
    //  ------ MySQL----------
    if(system.equals("MYSQL")) {
      return(new MySQLDatabase(user,password,database,host,port));
    }
    //  ------ Postgres----------
    if(system.equals("POSTGRES")) {
      return(new PostgresDatabase(user,password,database,host,port,schema));
    }
    throw new RuntimeException("Unsupported database system "+system);
  }
  
  /** Returns all defined parameters*/
  public static Set<String> parameters() {
    return(values.keySet());
  }
  
  /** Test routine */
  public static void main(String argv[]) throws Exception {
    System.err.println("Enter the name of an ini-file: ");
    init(D.r());
    D.p(values);
  }
}
