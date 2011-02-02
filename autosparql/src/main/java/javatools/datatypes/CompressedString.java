package javatools.datatypes;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javatools.administrative.D;
import javatools.parsers.Char;
/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

  This class represents an equivalence class of Strings. The equivalence class
  stores one prototype String, but compresses the String with 7, 6 or less bits per character
  (the String-class uses 16). This makes the CompressedString
  less memory consuming than a normal String, but possibly the original String cannot be
  reconstructed. The less bits you use, the less space you will consume, the less likely 
  it will be that you can reconstruct the original String and the larger the equivalence class
  will be.
  Example:
   <PRE>
    System.out.println(new CompressedString("How are you?",7));
    System.out.println(new CompressedString("How are you?",6));
    System.out.println(new CompressedString("How are you?",5));
    System.out.println(new CompressedString("How are you?",4));
    System.out.println(new CompressedString("How are you?",3)); 
    -->
        How are you?
        HOW ARE YOU?
        HOW ARE YOU?
        HOW@ARE@YOU
        HOG@ABE@IOEO
        @GG@ABE@AGEG
   </PRE>
   CompressedStrings are useful for large Collections of Strings where only membership
   checks are made. If the Strings are sufficiently different, then a TreeSet can
   contain CompressedStrings with a low bit number and membership checks will be handled
   correctly.
   A CompressedString is equal to all the (uncompressed) Strings in its equivalence 
   class (violating the transitivity of equals on calls with non-CompressedStrings).
  */
public class CompressedString implements CharSequence, Comparable<CompressedString>{

  /** Number of bits per character*/
  protected byte BITSPERCHAR;
  /** The string*/
  protected byte[] data;
  /** StartBit (for subsequence)*/
  protected int startBit;
  /** Number of characters in this CompressedString*/
  protected int length;
  
  /** Returns a hash-code */
  public int hashCode() {
    return(Arrays.hashCode(data));
  }
  /** Returns the number of characters in this CompressedString*/
  public int length() {
    return length;
  }
  
  /** Returns the bytes used by this CompressedString */
  public int size() {
    return(data.length);
  }

  /** Returns the bits per character */
  public int bits() {
    return(BITSPERCHAR);
  }

  /** Returns the character at position index */
  public char charAt(int index) {
    if(index>=length) throw new IndexOutOfBoundsException(toString()+"@"+index);
    int bytepos=(index*BITSPERCHAR+startBit)>>3;
    int bitpos=(index*BITSPERCHAR+startBit)&7;
    int l=((int)data[bytepos]&255)>>bitpos;
    if(BITSPERCHAR>8-bitpos) l|=data[bytepos+1]<<(8-bitpos);
    l&=(1<<BITSPERCHAR)-1;
    if(BITSPERCHAR<7 && (l|64)<='Z' && (l|64)>='A') l|=64;
    if(BITSPERCHAR<=5 && l<32) l|=32;
    return((char)l);
  }

  /** Tells whether the argument (any CharSequence) 
   * is in the equivalence class of this CompressedString*/
  public boolean equals(Object o) {
    return(o!=null && (o instanceof CharSequence) && 
          compareTo((CharSequence)o)==0);
  }
  
  /** Normalizes (see Char.java) and upcases a CharSequence if necessary*/
  public CharSequence normalize(CharSequence s) {
    if(s instanceof CompressedString) return(s);
    if(BITSPERCHAR==8) return(s);
    if(BITSPERCHAR==7) return(Char.normalize(s.toString()));
    return(Char.normalize(s.toString()).toUpperCase());
  }
  
  /** 1 if this CompressedString is lexically larger than the 
   * CompressedString of the argument, else -1 or 0*/
  public int compareTo(CharSequence s0) {
    if(this.length()==0) return(s0.length()==0?0:-1);
    if(s0.length()==0) return(1);
    final CompressedString s1=s0 instanceof CompressedString?
           (CompressedString)s0:
           new CompressedString(s0,this.BITSPERCHAR);
    final CompressedString s2=this;
    if(s1.BITSPERCHAR!=s2.BITSPERCHAR) return(this.toString().compareTo(s0.toString()));
    final int shift1=s1.startBit&7;
    final int shift2=s2.startBit&7;
    int i1=s1.startBit>>3;
    int i2=s2.startBit>>3;
    int num=(Math.min(s1.length, s2.length)*s1.BITSPERCHAR)>>3;
    while(--num>0){
      int c1=((s1.data[i1]>>shift1)|(s1.data[i1+1]<<(8-shift1)))&255;
      int c2=((s2.data[i2]>>shift2)|(s1.data[i2+1]<<(8-shift2)))&255;
      if(c1>c2) return(-1);
      if(c1<c2) return(1);
      i1++;
      i2++;
    }
    int c1=(s1.data[i1]>>shift1)&255;    
    int c2=(s2.data[i2]>>shift2)&255;    
    if(s1.length>s2.length) c1|=(s1.data[i1+1]<<(8-shift1))&255;
    if(s2.length>s1.length) c2|=(s2.data[i2+1]<<(8-shift2))&255;
    if(c1>c2) return(-1); 
    if(c1<c2) return(1);
    if(s1.length()>s2.length()) return(-1);
    if(s1.length()<s2.length()) return(1);
    return(0);
  }
  
  /** 1 if this CompressedString is lexically larger than the 
   * the argument, else -1 or 0*/ 
  public int compareTo(CompressedString s) {
    return(compareTo((CharSequence)s));
  }
  
  /** Duplicates a CompressedString (for subSequence)*/
  protected CompressedString(byte[] data, int startBit, int length) {
    this.data=data;
    this.startBit=startBit;
    this.length=length;
  }
  
  /** Returns a subsequence of this CompressedString (without duplicating the data)*/
  public CharSequence subSequence(int start, int end) {
     if(start<0 || end<0 || end>length || start>end) throw new IndexOutOfBoundsException(toString()+"@"+start+"-"+end);
     return(new CompressedString(data,startBit+start*7,end-start));
  }

  /** Compresses a CharSequence */
  public CompressedString(CharSequence s1, int bits) {
    if(bits>8 || bits<1) throw new IllegalArgumentException("CompressedString must have 0<bits<8");
    BITSPERCHAR=(byte)bits;
    CharSequence s=bits>=8?s1:normalize(s1);
    data=new byte[(s.length()*BITSPERCHAR+7)>>3];
    int bytepos=0;
    int bitpos=0;
    for(int i=0;i<s.length();i++) {
      int c=s.charAt(i)&((1<<BITSPERCHAR)-1);
      data[bytepos]|=(c<<bitpos);     
      bitpos+=BITSPERCHAR;
      if(bitpos>7 && bytepos<data.length-1) {
        bytepos++;
        data[bytepos]=(byte)(c>>(8-bitpos+BITSPERCHAR));
        bitpos-=8;
      }
    }
    this.length=s.length();
  }
  
  /** Compresses a CharSequence with  6 bits*/
  public CompressedString(CharSequence s1) {
    this(s1,6);
  }
  
  /** Tries to reconstruct the original String*/
  public String toString() {
    char[] result=new char[length];
    for(int i=0;i<length;i++) result[i]=charAt(i);
    return(new String(result));
  }

  /** Test routine*/
  public static void main(String[] args) {
    System.out.println(new CompressedString("How_are you? 8",8));
    System.out.println(new CompressedString("How_are you? 7",7));
    System.out.println(new CompressedString("How_are you? 6",6));
    System.out.println(new CompressedString("How_are you? 5",5));
    System.out.println(new CompressedString("How_are you? 4",4));
    System.out.println(new CompressedString("How_are you? 3",3));    
    Set<CharSequence> set=new TreeSet<CharSequence>();
    set.add(new CompressedString("hello"));
    D.p(set.contains(new CompressedString("hello")));
    D.p(set.contains(new CompressedString("HELLO")));
    D.p(set.contains(new CompressedString("blub"))); 
    D.p(new CompressedString("aaaabbas").compareTo("aaaabbasa"));
  }

}
