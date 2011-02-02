package javatools.filehandlers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import javatools.administrative.Announce;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

Does the same as the MatchReader (see there), but deals with bytes instead of Chars
(see SimpleInputStreamReader).*/
public class ByteMatchReader extends MatchReader {

  /** Holds the stream to read from*/
  protected InputStream input;
  
  /** Constructs a MatchReader from a Reader and a Pattern */
  public ByteMatchReader(InputStream i, Pattern p) {
    input=i;
    pattern=p;
    next();
  }
  
  /** Constructs a MatchReader from a Reader and a Pattern */
  public ByteMatchReader(InputStream i, String p) {
    this(i,Pattern.compile(p));
  }

  /** Constructs a MatchReader that reads from a file, with progress message (main constructor)*/
  public ByteMatchReader(File f, Pattern p, String announceMsg) throws FileNotFoundException {
    if(announceMsg!=null) {
      Announce.progressStart(announceMsg, f.length());
      chars=0;
    }
    input=new BufferedInputStream(new FileInputStream(f));
    pattern=p;
    matcher=p.matcher(buffer);
  }

  /** Constructs a MatchReader that reads from a file, with progress message*/
  public ByteMatchReader(String f, Pattern p, String announceMsg) throws FileNotFoundException {
    this(new File(f), p, announceMsg);
  }

  /** Constructs a MatchReader that reads from a file, with progress message*/
  public ByteMatchReader(String f, String p, String announceMsg) throws FileNotFoundException {
    this(new File(f), Pattern.compile(p), announceMsg);
  }

  /** Constructs a MatchReader that reads from a file, with progress message*/
  public ByteMatchReader(File f, String p, String announceMsg) throws FileNotFoundException {
    this(f, Pattern.compile(p), announceMsg);
  }
  
  /** Constructs a MatchReader that reads from a file */
  public ByteMatchReader(File f, String p) throws FileNotFoundException {
    this(f, Pattern.compile(p),null);
  }

  /** Constructs a MatchReader that reads from a file */
  public ByteMatchReader(String f, String p) throws FileNotFoundException {
    this(new File(f), Pattern.compile(p),null);
  }

  /** Constructs a MatchReader that reads from a file */
  public ByteMatchReader(String f, Pattern p) throws FileNotFoundException {
    this(new File(f), p,null);
  }

  /** Constructs a MatchReader that reads from a file */
  public ByteMatchReader(File f, Pattern p) throws FileNotFoundException {
    this(f, p,null);
  }
  
  /** Reads 1 character */
  protected int read() throws IOException {
    return(input.read());
  }

  /** Closes the reader */
  public void close() {
    try {
      input.close();
    }
    catch(IOException e) {}
  }

    
  
}
