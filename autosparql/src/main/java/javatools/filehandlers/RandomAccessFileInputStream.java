package javatools.filehandlers;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
  It is licensed under the Creative Commons Attribution License 
  (see http://creativecommons.org/licenses/by/3.0) by 
  the YAGO-NAGA team (see http://mpii.de/yago-naga).
    
  
    
   

  The class wraps a RandomAccessFile into a Reader
*/

public class RandomAccessFileInputStream extends InputStream {

  protected RandomAccessFile raf;
  
  @Override
  public void close() throws IOException {
    raf.close();
  }
  
  @Override
  public int read() throws IOException {   
    return raf.read();
  }

  public RandomAccessFileInputStream(RandomAccessFile f) {
    raf=f;
  }
}
