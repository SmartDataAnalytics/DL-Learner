package javatools.datatypes;
import java.util.NoSuchElementException;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

This class provides a stack for simple datatypes (int, float, boolean, double etc.).
It functions without wrapping/unwrapping. 
Example:
<PRE>
   SmallStack s=new SmallStack();
   s.push(7);
   D.p(s.popInt());
</PRE>
*/
public class SmallStack  {
  /** Holds the stack content*/
  protected long[] values=new long[10];
  /** Points to the top of the stack*/
  protected int nextFree=0;
  
  public SmallStack() {
   
  }
  public SmallStack(double d) {
    this();
    push(d);
  }
  public SmallStack(long d) {
    this();
    push(d);
  }
  public SmallStack(float d) {
    this();
    push(d);
  }
  
  public long push(long l) {
    if(nextFree==values.length) {
      long[] oldval=values;
      values=new long[values.length+10];
      System.arraycopy(oldval, 0, values, 0, nextFree);
    }
    return(values[nextFree++]=l);
  }
  
  public boolean push(boolean b) {
    push(b?1:0);
    return(b);
  }
  
  public double push(double d) {
    push(Double.doubleToRawLongBits(d));
    return(d);
  }
  
  public double push(float d) {
    push(Float.floatToIntBits(d));
    return(d);
  }
  
  public long peekLong() {
    if(nextFree==0) throw new NoSuchElementException("SmallStack is empty");
    return(values[nextFree]);
  }

  public boolean peekBoolean() {
    return(peekLong()==1);
  }

  public int peekInt() {
    return((int)peekLong());
  }

  public float peekFloat() {
    return(Float.intBitsToFloat((int)peekLong()));
  }

  public double peekDouble() {
    return(Double.longBitsToDouble(peekLong()));
  }

  public long popLong() {
    if(nextFree==0) throw new NoSuchElementException("SmallStack is empty");
    return(values[--nextFree]);
  }

  public boolean popBoolean() {
    return(popLong()==1);
  }

  public int popInt() {
    return((int)popLong());
  }

  public float popFloat() {
    return(Float.intBitsToFloat((int)popLong()));
  }

  public double popDouble() {
    return(Double.longBitsToDouble(popLong()));
  }

  public int size() {
    return(nextFree);
  }
  
  public boolean empty() {
    return(size()==0);
  }
  
  public int search(long l) {
    for(int i=0;i<nextFree;i++) if(values[i]==l) return(i);
    return(-1);
  }

  public int search(double d) {
    return(search(Double.doubleToRawLongBits(d)));
  }

  public int search(boolean d) {
    return(search(d?1:0));
  }
  
  public boolean equals(Object o) {
    if(!(o instanceof SmallStack)) return(false);
    SmallStack other=(SmallStack)o;
    if(nextFree!=other.nextFree) return(false);
    for(int i=0;i<nextFree;i++) {
      if(values[i]!=other.values[i]) return(false);
    }
    return(true);
  }
  
  public int hashCode() {
    int result=0;
    for(int i=0;i<nextFree;i++) {
      result+=values[i];
      result<<=1;
    }
    return(result);
  }

  public double[] toDoubleArray() {
    double[] result=new double[size()];
    for(int i=0;i<result.length;i++) result[i]=Double.longBitsToDouble(values[i]);
    return(result);
  }
  
  public long[] toLongArray() {
    long[] result=new long[size()];
    System.arraycopy(values, 0, result, 0, size());
    return(result);   
  }
  
  public int[] toIntArray() {
    int[] result=new int[size()];
    for(int i=0;i<result.length;i++) result[i]=(int)values[i];
    return(result);
  }

}
