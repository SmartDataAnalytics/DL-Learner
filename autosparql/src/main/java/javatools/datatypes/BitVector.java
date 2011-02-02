package javatools.datatypes;
import java.util.AbstractList;
import java.util.Arrays;

import javatools.administrative.D;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 
     
This class implements an efficient boolean (bit) list .
Same as java.util.BitSet
<BR>
*/

public class BitVector extends AbstractList<Boolean>{
  protected long[] data;
  protected int size=0;
  
  public BitVector(int capacity) {
    data=new long[capacity/64+1];
  }

  public BitVector(BitVector v) {
    data=Arrays.copyOf(v.data, v.data.length);
    size=v.size;
  }
  
  public BitVector() {
    this(10);
  }
  
  public boolean add(Boolean e) {
    return(add(e.booleanValue()));
  }

  public boolean add(boolean b) {
    if(size>=data.length*64-1) {
      long[] newdta=new long[data.length+1];
      System.arraycopy(data,0,newdta,0,data.length);
      data=newdta;
    }
    size++;
    set(size-1,b);
    return(true);
  }

  public void addZeroes(int num) {
    if(size+num>data.length*64-1) {
      long[] newdta=new long[(size+num)/64+1];
      System.arraycopy(data,0,newdta,0,data.length);
      data=newdta;
    }
    size+=num;
  }
  
  public boolean add(int b) {
    return(add(b!=0));
  }

  public void set(int pos,boolean b) {
    if(pos<0 || pos>size-1) throw new ArrayIndexOutOfBoundsException("Pos:"+pos+" Size:"+size);
    long add=1L<<(pos%64);
    if(b) data[pos/64]|=add;
    else data[pos/64]&=~add;
  }

  public void setPosBit(int pos,int bit) {
    set(pos,bit!=0);
  }
  
  protected static Boolean[] bools=new Boolean[]{Boolean.FALSE,Boolean.TRUE};
  
  @Override
  public Boolean get(int index) {  
    long l=data[index/64];
    return bools[(int)(l>>(index%64)&1)];
  }

  public int getBit(int index) {  
    long l=data[index/64];
    return (int)(l>>(index%64)&1);
  }

  @Override
  public int size() {
    return size;
  }
  
  public void clear() {
    data=new long[1];
    size=0;
  }
  
  @Override
  public String toString() {
    StringBuilder b=new StringBuilder(size);   
    for(int i=0;i<size;i++) b.append(getBit(i));
    return(b.toString());
  }
  /**  */
  public static void main(String[] args) {
    BitVector v=new BitVector();
    for(int i=0;i<70;i++) v.add(true);
    D.p(v);
    for(int i=0;i<70;i+=2) v.set(i,false);
    D.p(v);
  }

}
