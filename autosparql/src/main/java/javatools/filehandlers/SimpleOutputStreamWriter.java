package javatools.filehandlers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

A SimpleOutputStreamWriter writes the characters directly as bytes to an output stream
-- regardless of the encoding. See SimpleInputStreamReader for an explanation.
*/
public class SimpleOutputStreamWriter extends Writer {

  /** Holds the underlying OutputStrema*/
  public OutputStream out;
  
  public SimpleOutputStreamWriter(OutputStream o) {
    out=o;
  }

  public SimpleOutputStreamWriter(File f) throws IOException {
    this(new BufferedOutputStream(new FileOutputStream(f)));
  }

  public SimpleOutputStreamWriter(String s) throws IOException {
    this(new File(s));
  }

  @Override
  public void close() throws IOException {
    out.close();
  }

  @Override
  public void flush() throws IOException {
    out.flush();
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    for(int pos=off;pos<off+len;pos++) write(cbuf[pos]);
  }

  public void write(int c) throws IOException {
    out.write((byte)(c));
  }
}
