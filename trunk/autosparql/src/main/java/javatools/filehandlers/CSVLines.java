package javatools.filehandlers;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javatools.datatypes.PeekIterator;
import javatools.parsers.Char;

/**
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
  It is licensed under the Creative Commons Attribution License 
  (see http://creativecommons.org/licenses/by/3.0) by 
  the YAGO-NAGA team (see http://mpii.de/yago-naga).
    
  The class provides an iterator over the lines in a comma-separated file<BR>
  Example:
  <PRE>
      for(String[] columns : new CSVFile("c:\\autoexec.csv")) {
         System.out.println(column[0],column[1]);
      }
  </PRE>
  Recognizes column headers if introduced by '#'. Recognizes all types of encodings (see Char.decode()).
*/

public class CSVLines extends PeekIterator<List<String>> {
  /** number of chars for announce (or -1) */
  protected long announceChars=-1;
  /** Containes the Reader */
  protected Reader in;
  /** Holds the column names*/
  protected List<String> columns=null;
  /** Holds the next char in line */
  protected int nextChar;
  
  /** Constructs a CSVReader*/
  public CSVLines(File f) throws IOException {
    in=new FileReader(f);
    nextChar=in.read();
    if(nextChar=='/') nextChar=in.read();
    if(nextChar=='#' || nextChar=='%' || nextChar=='/') {
      nextChar=in.read();
      columns=next();
    }
  }

  /** Constructs a CSVReader*/
  public CSVLines(String file) throws IOException {
    this(new File(file));
  }
  
  /** Reads a component and the following comma*/
  protected String component() throws IOException {
    StringBuilder component=new StringBuilder();    
    // Skip whitespace
    while(Character.isWhitespace(nextChar)) {
      nextChar=in.read();
    }
    // For quoted components...
    if(nextChar=='"') {
      nextChar=in.read();
      while(nextChar!=-1) {
        if(nextChar=='"') {
          nextChar=in.read();
          if(nextChar!='"') break;
        }
        component.append((char)nextChar);       
        nextChar=in.read();
      }
      // Skip following whitespace
      while(nextChar!=10 && nextChar!=-1 && Character.isWhitespace(nextChar)) {
        nextChar=in.read();
      }      
    } else {
      // For unquoted components
      while(nextChar!=-1 && nextChar!=',' && nextChar!=10) {
        component.append((char)nextChar);
        nextChar=in.read();
      }
      while(Character.isWhitespace(Char.last(component))) component.setLength(component.length()-1);
    }
    // Skip following comma
    if(nextChar==',') nextChar=in.read();
    return(Char.decode(component.toString()));
  }
  
  @Override
  protected List<String> internalNext() throws Exception {
    if(nextChar==-1) return(null);
    List<String> line=new ArrayList<String>(columns==null?10:columns.size());
    while(nextChar!=-1 && nextChar!=10) {
      String c=component();
      line.add(c);
    }
    // Read new Line
    nextChar=in.read();
    return line;
  }
  
  /** returns the column names (or NULL)*/
  public List<String> columnNames() {
    return(columns);
  }
  
  @Override
  public void close() {   
    try {
      in.close();
    }catch(Exception e) {}
  }
  
  /** Test method*/
  public static void main(String[] args) throws Exception {
    for(List<String> cols : new CSVLines("./javatools/testdata/CSVTest.csv")) {
      System.out.println(cols);
    }
  }
}
