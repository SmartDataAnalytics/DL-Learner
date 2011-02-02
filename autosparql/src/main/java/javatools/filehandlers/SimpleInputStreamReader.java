package javatools.filehandlers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

A SimpleInputStreamReader reads the bytes from an InputStream and passes them
on as characters -- regardless of the encoding.
<BR>
Example:
<PRE>
    // It does not work like this
    Reader r=new InputStreamReader(new ByteArrayInputStream(new byte[]{(byte)144}));
    System.out.println(r.read());
    r.close();
     -----> 65533
     
    // But it does like this
    r=new SimpleInputStreamReader(new ByteArrayInputStream(new byte[]{(byte)144}));
    System.out.println(r.read());
    r.close();
     -----> 144
     
</PRE>
*/

public class SimpleInputStreamReader extends Reader {
  public InputStream in;
  
  public SimpleInputStreamReader(InputStream i) {
    in=i;
  }

  public SimpleInputStreamReader(File f) throws FileNotFoundException {
    this(new FileInputStream(f));
  }

  public SimpleInputStreamReader(String f) throws FileNotFoundException {
    this(new File(f));
  }
  
  @Override
  public void close() throws IOException {
    in.close();
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    byte[] bbuf=new byte[len];
    int result=in.read(bbuf, 0, len);
    for(int i=0;i<result;i++) cbuf[off+i]=(char)bbuf[i];
    return result;
  }

  public int read() throws IOException {
    return(in.read());
  }
  
  /**  */
  public static void main(String[] args) throws Exception {
    // It does not work like this
    Reader r=new InputStreamReader(new ByteArrayInputStream(new byte[]{(byte)144}));
    System.out.println(r.read());
    r.close();
    
    // But it does like this
    r=new SimpleInputStreamReader(new ByteArrayInputStream(new byte[]{(byte)144}));
    System.out.println(r.read());
    r.close();
  }

}
