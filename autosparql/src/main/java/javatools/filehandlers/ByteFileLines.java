package javatools.filehandlers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javatools.administrative.Announce;
import javatools.administrative.D;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

Does the same as FileLines (see there) but reads bytes (see SimpleInputStreamReader).
*/
public class ByteFileLines extends FileLines {

  /** The stream to read the lines from */
  public InputStream in;
  
  /** Constructs FileLines from a filename */
  public ByteFileLines(String f) throws IOException {
    this(f,null);
  }
  /** Constructs FileLines from a file */
  public ByteFileLines(File f) throws IOException {
    this(f,null);
  }
  /** Constructs FileLines from a filename, shows progress bar */
  public ByteFileLines(String f, String announceMsg) throws IOException {
    this(new File(f),announceMsg);
  }
  /** Constructs FileLines from a file, shows progress bar  (main constructor 1) */
  public ByteFileLines(File f, String announceMsg) throws IOException {
    if(announceMsg!=null) {
      Announce.progressStart(announceMsg, f.length());
      announceChars=0;
    }
    in=new BufferedInputStream(new FileInputStream(f));
  }  
  /** Constructs FileLines from a Reader */
  public ByteFileLines(InputStream i)  {
    this(new BufferedInputStream(i));
  }  
  /** Constructs FileLines from a BufferedReader (main constructor 2) */
  public ByteFileLines(BufferedInputStream i) {
    in=i;
  }
  
  @Override
  public String internalNext() {
    StringBuffer next=new StringBuffer(100);
    try {
      int c;
      do{
        if((c=in.read())==-1) {
          close();
          if(announceChars!=-1) Announce.progressDone();
          return(null);
        }
      } while(c==(char)10 || c==(char)13);
      do{
        next.append((char)c);
        c=in.read();
      } while(c!=(char)10 && c!=(char)13 && c!=-1);      
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    if(announceChars!=-1) Announce.progressAt(announceChars+=next.length());
    return(next.toString()); 
  }
  
  @Override
  public void close() {
    try {
      in.close();
    } catch (IOException e) {}
  }
  
  public static void main(String[] args) throws Exception {
    for(String l : new ByteFileLines("c:\\fabian\\service\\autoexec.bat")) {
      D.p(l);
    }
  }
}
