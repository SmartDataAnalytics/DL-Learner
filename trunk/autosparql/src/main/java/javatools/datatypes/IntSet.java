package javatools.datatypes;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;

import javatools.administrative.D;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 
     
This class implements a space-efficient integer set. All operations are linear time, though...<BR>
*/
public class IntSet implements Set<Integer> {
  /** Holds the integers*/
  protected int[] data;
  /** Tells whether the int is there*/
  protected BitSet isThere;
  /** Number of integers in data*/
  protected int numElements=0;
  /** Index that can be polled next. Handled exclusively by poll() */
  protected int pollIndex=0;
  /** Index pointing to an empty position. Handled exclusively by indexOf()*/
  protected int addIndex=0;
  /** Last index that contains a value*/
  protected int lastIndex=-1;
  
  public PeekIterator<Integer> iterator() {
    return new PeekIterator<Integer>() {
      int pos=0;
      @Override
      protected Integer internalNext() throws Exception {       
        while(true) {
          if(pos>lastIndex) return(null);
          if(isThere.get(pos)) return(data[pos]);
          pos++;
        }
      }
    };
  }

  /** Adds the element*/
  public boolean add(int v) {
    // Use contains() also to set the addIndex
    if(contains(v)) return(false);  
    if(numElements==data.length) data=Arrays.copyOf(data,data.length+100);
    data[addIndex]=v;
    isThere.set(addIndex,true);
    numElements++;
    if(addIndex>lastIndex) lastIndex=addIndex;
    return(true);
  } 
  
  public void clear(int capacity) {
    numElements=0;
    data=new int[capacity];
    isThere=new BitSet(capacity);
    lastIndex=-1;
  }
  
  public int poll() {
    if(numElements==0) throw new NoSuchElementException();
    shrink();
    while(true) {
      if(pollIndex>lastIndex) pollIndex=0;
      if(isThere.get(pollIndex))  return(removeIndex(pollIndex));
      pollIndex++;
    }
  }
  
  /** Finds the element, sets addIndex to a free position*/
  protected int indexOf(int v) {
    addIndex=lastIndex+1;
    for(int i=0;i<=lastIndex;i++) {
      if(!isThere.get(i)) addIndex=i;
      if(data[i]==v && isThere.get(i)) return(i);
    }
    return(-1);
  }

  /** Deletes empty space if necessary*/
  protected void shrink() {
    if(numElements<data.length/2 && data.length>300) {    
      IntSet result=new IntSet(numElements+100);
      result.addAll(this);
      this.data=result.data;
      this.isThere=result.isThere;
      this.lastIndex=result.lastIndex;
      this.numElements=result.numElements;
    }
  }
  
  /** Removes an item*/
  protected int removeIndex(int index) {
    isThere.set(index, false);
    numElements--; 
    return(data[index]);
  }
  
  // ----------- Wrapper methods -------------
  
  /** Creates a new IntSet from initial values.*/
  public static IntSet of(int... initial) {
    IntSet result=new IntSet();
    for(int i : initial) result.add(i);
    return(result);
  }

  /** Creates a new IntSet by copying the given set*/
  public IntSet(IntSet copy) {
    setTo(copy);
  }

  /** Creates a new IntSet by copying the given bit set*/
  public IntSet(BitSet copy) {
    clear(copy.cardinality());
    addAll(copy);
  }

  /** Creates a new IntSet*/
  public IntSet() {
    clear();
  }
  
  /** Creates a new IntSet with an initial capacity*/
  public IntSet(int capacity) {
    clear(capacity);
  }

  /** Overwrites the current IntSet with the given one*/
  public void setTo(IntSet copy) {
    if(copy==this) return;
    clear(copy.size());
    addAll(copy);
  }
    
  /** Removes all elements that are not in c*/
  public boolean retainAll(Collection<?> c) {
    boolean result=false;
    for(int i=0;i<=lastIndex;i++) {
      if(isThere.get(i) && !c.contains(data[i])) {
        removeIndex(i);
        result=true;
      }
    }
    shrink();
    return result;
  }
  
  /** Removes one element, returns TRUE if the set was modified*/
  public boolean remove(int i) {
    int pos=indexOf(i);
    if(pos==-1) return false;
    removeIndex(pos);
    shrink();
    return true;    
  } 

  /** Removes one element, returns TRUE if the set was modified*/
  public boolean remove(long i) {
    return(remove((int)i));
  }
  
  /** Removes one element (has to be a number), returns TRUE if the set was modified*/
  public boolean remove(Object o) {
    if(!(o instanceof Number)) return(false);
    return(remove(((Number)o).intValue()));
  }

  /** Removes all elements in c*/
  public boolean removeAll(Collection<?> c) {
    boolean result=false;
    for(Object o : c) result|=remove(o);
    return result;
  }

  /** Removes all elements in c*/
  public boolean removeAll(IntSet c) {
    boolean result=false;
    for(int i=0;i<=c.lastIndex;i++) {
      if(c.isThere.get(i)) result|=remove(c.data[i]);
    }
    return result;
  }

  /** Returns an array of the elements of this set*/
  public Object[] toArray() {
    Object[] result=new Object[numElements];
    int j=0;
    for(int i=0;i<=lastIndex;i++) {
      if(isThere.get(i)) result[j++]=new Integer(data[i]); 
    }
    return result;
  }

  /** Not supported*/
  public <T> T[] toArray(T[] a) {    
    throw new UnsupportedOperationException("IntSet.toArray(T[])");
  }

  @Override
  public String toString() {
    StringBuilder res=new StringBuilder("[");
    for(int i=0;i<=lastIndex;i++) {
      if(isThere.get(i)) res.append(data[i]).append(", ");
    }
    if(numElements>0) res.setLength(res.length()-2);    
    return res.append("]").toString();
  }    

  public int size() {
    return numElements;
  }

  /** Adds this element*/
  public boolean add(long i) {
    return(add((int)i));
  }
  
  /** Adds this element*/
  public boolean add(Number e) {
    return add(e.intValue());
  }

  /** Adds all elements*/
  public boolean addAll(Collection<? extends Integer> c) {
    boolean returnValue=false;
    for(Integer i : c) returnValue|=add(i);
    return(returnValue);
  }

  /** Adds all elements*/
  public boolean addAll(IntSet s) {
    boolean returnValue=false;
    for(int index=0;index<=s.lastIndex;index++) {
      if(s.isThere.get(index)) returnValue|=add(s.data[index]);
    }
    return(returnValue);
  }

  /** Adds all elements*/
  public boolean addAll(BitSet s) {
    boolean returnValue=false;
    for(int i=s.nextSetBit(0);i>=0;i=s.nextSetBit(i+1)) {      
      returnValue|=add(i);
    }
    return(returnValue);
  }

  /** Removes all elements of s from this set*/
  public boolean removeAll(BitSet s) {
    boolean returnValue=false;
    for(int i=s.nextSetBit(0);i>=0;i=s.nextSetBit(i+1)) {      
      returnValue|=remove(i);
    }
    return(returnValue);
  }

  /** Adds all elements of this set to s*/
  public void addTo(BitSet s) {
    for(int i=0;i<=lastIndex;i++) {      
      if(isThere.get(i)) s.set(data[i]);
    }
  }

  /** Adds all elements, returns THIS*/  
  public IntSet enhancedBy(IntSet addMe) {
    addAll(addMe);
    return(this);
  }

  /** Adds addMe, returns THIS*/
  public IntSet enhancedBy(int addMe) {
    add(addMe);
    return(this);
  }
  
  /** Removes the given elements, returns THIS*/
  public IntSet shrunkBy(IntSet addMe) {
    removeAll(addMe);
    return(this);
  }
  
  /** Removes the given element, returns THIS*/
  public IntSet shrunkBy(int addMe) {
    remove(addMe);
    return(this);
  }
  
  /** TRUE if the set contains i*/
  public boolean contains(int i) {
    return(indexOf(i)!=-1);
  }

  /** TRUE if the set contains i*/
  public boolean contains(long i) {
    return(contains((int)i));
  }
  
  /** TRUE if the set contains o*/
  public boolean contains(Object o) {
    if(!(o instanceof Number)) return(false);
    return(contains(((Number)o).intValue()));
  }

  /** TRUE if the set contains all elements of c*/
  public boolean containsAll(Collection<?> c) {
    for(Object o : c) if(!contains(o)) return(false);
    return true;
  }

  /** TRUE if the set is empty*/
  public boolean isEmpty() {
    return numElements==0;
  }

  /** Removes all elements*/
  public void clear() {
    clear(10);
  }

  /** Adds the element*/
  public boolean add(Integer e) {  
    return add((Number)e);
  }   
  
  /** Test method*/
  public static void main(String[] args) {
    IntSet s=new IntSet();
    for(int i=0;i<500;i++) {
      s.add(i);         
    }
    D.p(s);
    for(int i=0;i<500;i+=2) s.remove(i);
    D.p(s);    
    while(!s.isEmpty()) {
      D.p(s.poll());
    }
  }
  
}
