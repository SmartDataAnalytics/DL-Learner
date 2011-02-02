package javatools.filehandlers;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import javatools.administrative.Announce;
import javatools.datatypes.PeekIterator;
/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  
The class provides an iterator over the lines in a file<BR>
Example:
<PRE>
    for(String s : new FileLines("c:\\autoexec.bat")) {
       System.out.println(s);
    }
</PRE>
If desired, the iterator can make a nice progress bar by calling
Announce.progressStart/At/Done automatically in the right order.
If there are no more lines, the file is closed. If you do not use all lines of the
iterator, close the iterator manually.
*/
public class FileLines extends PeekIterator<String> implements Iterable<String>, Iterator<String>, Closeable {
  /** number of chars for announce (or -1) */
  protected long announceChars=-1;
  /** Containes the Reader */
  protected BufferedReader br;

  /** Constructs FileLines from a filename */
  public FileLines(String f) throws IOException {
    this(f,null);
  }
  /** Constructs FileLines from a file */
  public FileLines(File f) throws IOException {
    this(f,null);
  }
  /** Constructs FileLines from a filename, shows progress bar */
  public FileLines(String f, String announceMsg) throws IOException {
    this(new File(f),announceMsg);
  }
  /** Constructs FileLines from a file, shows progress bar  (main constructor 1) */
  public FileLines(File f, String announceMsg) throws IOException {
    if(announceMsg!=null) {
      Announce.progressStart(announceMsg, f.length());
      announceChars=0;
    }
    br=new BufferedReader(new FileReader(f));
  }  
  /** Constructs FileLines from a Reader */
  public FileLines(Reader f)  {
    this(new BufferedReader(f));
  }  
  /** Constructs FileLines from a BufferedReader (main constructor 2) */
  public FileLines(BufferedReader r)  {
    br=r;
  }
  
  /** For subclasses*/
  protected FileLines() {
    
  }
  /** Unsupported, throws an UnsupportedOperationException */
  public void remove() throws UnsupportedOperationException {
    throw new UnsupportedOperationException("FileLines does not support \"remove\"");
  }

  /** Returns next line. In case of an IOException, the exception is wrapped in a RuntimeException */
  public String internalNext() {
    String next;
    try {
      next=br.readLine();
      if(announceChars!=-1 && next!=null) Announce.progressAt(announceChars+=next.length());
    } catch(IOException e) {
      throw new RuntimeException(e);
    }    
    return(next);
  }
  
  /** Returns a simple identifier */
  public String toString() {
    return("FileLines of "+br);
  }
  /** Returns this */
  public Iterator<String> iterator() {
    return this;
  }
  /** Closes the reader */
  public void close() {
    try {
      br.close();
    } catch (IOException e) { }
    if(announceChars!=-1) Announce.progressDone();
    announceChars=-1;
  }

  /** Closes the reader */
  public void finalize() {
    close();
  }
  
  /** Reads until one of the strings is found, returns its index or -1.
   * @throws IOException */
  public static int find(Reader in, String... findMe) throws IOException {
    int[] pos=new int[findMe.length];
    while(true) {
      int c=in.read();
      if(c==-1) return(-1);
      if(c==-2) continue; // Let's be compliant with HTMLReader: Skip tags
      for(int i=0;i<findMe.length;i++) {
        if(c==findMe[i].charAt(pos[i])) pos[i]++;
        else pos[i]=0;
        if(pos[i]==findMe[i].length()) return(i);        
      }
    }
  }

  /** Reads until one of the strings is found, returns its index or -1.
   * @throws IOException */
  public static int findIgnoreCase(Reader in, String... findMe) throws IOException {
    int[] pos=new int[findMe.length];    
    for(int i=0;i<findMe.length;i++) findMe[i]=findMe[i].toUpperCase();
    while(true) {
      int c=in.read();
      if(c==-1) return(-1);
      if(c==-2) continue; // Let's be compliant with HTMLReader: Skip tags
      c=Character.toUpperCase(c);
      for(int i=0;i<findMe.length;i++) {
        if(c==findMe[i].charAt(pos[i])) pos[i]++;
        else pos[i]=0;
        if(pos[i]==findMe[i].length()) return(i);        
      }
    }
  }
  
  /** Maximum chars to read by readTo (or -1)*/
  public static int maxChars=-1;
  
  /** Reads to a specific character, returns the text in between 
   * @throws IOException */
  public static CharSequence readTo(Reader in, char... limit) throws IOException {    
    StringBuilder result=new StringBuilder();
    int c;
    outer: 
    while((c=in.read())!=-1) {
      for(int i=0;i<limit.length;i++) if(c==limit[i]) break outer;
      result.append((char)c);
      if(maxChars!=-1 && result.length()>maxChars) break;      
    }
    return(result);
  }
  
  /** Reads to a specific String, returns the text up to there, including the limit 
   * @throws IOException */
  public static CharSequence readTo(Reader in, String limit) throws IOException {    
    StringBuilder result=new StringBuilder();
    int c;
    int position=0;    
    while((c=in.read())!=-1) {      
      result.append((char)c);
      if(c==limit.charAt(position)) {
        if(++position==limit.length()) break;
      } else {
        position=0;
      }      
      if(maxChars!=-1 && result.length()>maxChars) break;      
    }
    return(result);
  }
}
