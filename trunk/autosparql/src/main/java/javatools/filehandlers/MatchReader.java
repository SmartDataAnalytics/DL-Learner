package javatools.filehandlers;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javatools.administrative.Announce;
import javatools.administrative.D;
import javatools.datatypes.PeekIterator;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

The MatchReader reads MatchResults from a file.
It closes the Reader automatically if hasNext()==false.
The MatchReader buffers always 1000 characters of the file, i.e.
it can work with large files without caching them in total.
<BR>
Example:
<PRE>
  MatchReader matchReader=new MatchReader("some Filename", "some Pattern");
  for(MatchResult matchResult : matchReader) {
    System.out.println(matchResult.group(1));
  }
</PRE>
The file is automatically closed after the last match has been read. If you do
not read all matches, close the iterator manually by the method close().<P>

A single quote (') preceded by a backslash will not match a quote in the pattern.
*/

public class MatchReader extends PeekIterator<MatchResult> implements Closeable {
  /** Number of newly read chars */
  public final int BUFSIZE=1000;
  /** Maximal length of a String matching a pattern */
  public final int MAXPATTERNLENGTH=400;
  /** Holds the Reader */
  protected Reader in;
  /** Holds the current matcher */
  protected Matcher matcher;
  /** Holds the current buffer */
  protected StringBuilder buffer=new StringBuilder(BUFSIZE+MAXPATTERNLENGTH);
  /** Holds the pattern to be found */
  protected Pattern pattern;
  /** Char counter for Announce.progress */
  protected long chars=-1;
  /** Points to the index of the last match*/
  protected int lastMatchEnd=0;
  /** Holds the string that quotes are replaced by*/
  public static final String QUOTE="FabianSuchanek";
  /** TRUE if newlines are to be replaces by spaces*/
  protected boolean crossLines=false;
  
  /** Constructs a MatchReader from a Reader and a Pattern */
  public MatchReader(Reader i, Pattern p) {
    in=i;
    pattern=p;
    next();
  }
  
  /** Constructs a MatchReader from a Reader and a Pattern */
  public MatchReader(Reader i, String p) {
    this(i,Pattern.compile(p));
  }

  /** Constructs a MatchReader that reads from a file, with progress message (main constructor)*/
  public MatchReader(File f, Pattern p, String announceMsg) throws FileNotFoundException {
    pattern=p;
    matcher=p.matcher(buffer);
    if(announceMsg!=null) {
      Announce.progressStart(announceMsg, f.length());
      chars=0;
    }
    in=new BufferedReader(new FileReader(f));
  }

  /** Constructs a MatchReader that reads from a file, with progress message*/
  public MatchReader(String f, Pattern p, String announceMsg) throws FileNotFoundException {
    this(new File(f), p, announceMsg);
  }

  /** Constructs a MatchReader that reads from a file, with progress message*/
  public MatchReader(String f, String p, String announceMsg) throws FileNotFoundException {
    this(new File(f), Pattern.compile(p), announceMsg);
  }

  /** Constructs a MatchReader that reads from a file, with progress message*/
  public MatchReader(File f, String p, String announceMsg) throws FileNotFoundException {
    this(f, Pattern.compile(p), announceMsg);
  }
  
  /** Constructs a MatchReader that reads from a file */
  public MatchReader(File f, String p, boolean crossLines) throws FileNotFoundException {
    this(f, Pattern.compile(p),null);
    this.crossLines=crossLines;
  }

  /** Constructs a MatchReader that reads from a file */
  public MatchReader(File f, String p) throws FileNotFoundException {
    this(f, p,false);
  }

  /** Constructs a MatchReader that reads from a file */
  public MatchReader(String f, String p) throws FileNotFoundException {
    this(new File(f), Pattern.compile(p),null);
  }

  /** Constructs a MatchReader that reads from a file */
  public MatchReader(String f, Pattern p) throws FileNotFoundException {
    this(new File(f), p,null);
  }

  /** Constructs a MatchReader that reads from a file */
  public MatchReader(File f, Pattern p) throws FileNotFoundException {
    this(f, p,null);
  }
  
  /** For subclasses*/
  protected MatchReader(){
  }
  
  /** Reads 1 character (simplification for subclass ByteMatchReader) */
  protected int read() throws IOException {
    return(in.read());
  }
  
  /** Returns the next MatchResult */
  public MatchResult internalNext() {
    while(true) {
      // Try whether there is something in the current buffer
      if(matcher.find()) {
        lastMatchEnd=matcher.end();
        return(new MyMatchResult(matcher));
      }
      // Determine the part of the old buffer which we will keep
      if(lastMatchEnd<buffer.length()-MAXPATTERNLENGTH) lastMatchEnd=buffer.length()-MAXPATTERNLENGTH;
      buffer.delete(0,lastMatchEnd);
      // Read a new buffer
      int len;
      for(len=0;len<BUFSIZE;len++) {
        int c;
        try {
          c=read();
        }
        catch(IOException e) {
          return (null);
        }        
        if(c==-1) break;
        if((c==10 || c==13) && crossLines) c=' ';
        if(c=='\'' && buffer.length()>0 && buffer.charAt(buffer.length()-1)=='\\') {
          buffer.setLength(buffer.length()-1);
          buffer.append(QUOTE);
        }
        else buffer.append((char)c);
      }
      if(chars!=-1) Announce.progressAt(chars+=len);
      // Reached the end of file, no data read, close the file
      if(len==0) {
        close();
        if(chars!=-1) Announce.progressDone();
        return(null);
      }
      matcher=pattern.matcher(buffer);
      lastMatchEnd=0;
    }
  }

  /** Closes the reader */
  public void close() {
    try {
      in.close();
    }
    catch(IOException e) {}
  }

  /** Closes the reader */
  public void finalize() {
    close();
  }   

  /** A MatchResult that undoes the quotes */
  public static class MyMatchResult implements MatchResult {
    public MatchResult inner;
    public MyMatchResult(Matcher m) {
      inner=m.toMatchResult();
    }
    public int end() {
      return(inner.end());
    }

    public int end(int group) {
      return inner.end(group);
    }

    public String group() {
      return(group(0));
    }

    public String group(int group) {
      return inner.group(group).replace(QUOTE, "'");
    }

    public int groupCount() {
      return groupCount();
    }

    public int start() {
      return inner.start();
    }

    public int start(int group) {
      return inner.start(group);
    }
    
  }
  /** Test routine */
  public static void main(String[] args) throws Exception {
    for(MatchResult idAndEntity : new MatchReader("c:\\Fabian\\Data\\yago\\search\\pairings.txt",Pattern.compile("(\\d+)\tu:http://[^\\:\n]*/([^/]+)\n"), "Parsing url mappings")) {  
      D.p(idAndEntity.group(1), idAndEntity.group(2))      ;
      D.r();
    }
  }

}
