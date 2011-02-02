package javatools.datatypes;
import java.util.Arrays;
import java.util.NoSuchElementException;

import javatools.administrative.D;

/**
  This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
  It is licensed under the Creative Commons Attribution License 
  (see http://creativecommons.org/licenses/by/3.0) by 
  the YAGO-NAGA team (see http://mpii.de/yago-naga).
    
  
    
   
       
  This class implements a priority queue, whose elements are integers (ids) and whose
  priorities are doubles. 
*/

public class IdPriorityQueue extends IntSet {
  /** Holds the priorities*/
  protected double[] priorities;
  /** stores highest index*/
  protected int highestIndex=-1;

  /** Constructor with initial capacity*/
  public IdPriorityQueue(int capacity) {
    super(capacity);
    priorities=new double[capacity];
  }
  
  /** Constructor with capacity 10*/
  public IdPriorityQueue() {
    this(10);
  }
  
  /** Adds an id to the queue or updates its priority */
  public void add(int i, double priority) {   
    //  Use indexOf() also to set the addIndex
    int pos=indexOf(i);
    // If we found the element, update its priority
    if(pos!=-1) {
      if(highestIndex==pos && priorities[pos]<priority) highestIndex=-1;
      if(highestIndex!=-1 && priorities[highestIndex]<priority) highestIndex=pos;
      priorities[pos]=priority;
      return;
    }
    if(numElements==data.length) {      
      data=Arrays.copyOf(data,data.length+100);
      priorities=Arrays.copyOf(priorities,data.length+100);
    }
    data[addIndex]=i;
    isThere.set(addIndex);
    priorities[addIndex]=priority;
    if(addIndex>lastIndex) lastIndex=addIndex;
    numElements++;
    if(highestIndex!=-1 && priorities[highestIndex]<priorities[addIndex]) highestIndex=addIndex;    
  }

  /** Truncates the id to int, then adds (linear time)*/
  public void put(long id, double priority) {
    add((int)id,priority);
  } 
    
  /** Returns the index with highest priority*/
  protected int highestIndex() {
    if(numElements==0) throw new NoSuchElementException();
    if(highestIndex!=-1) return(highestIndex);
    int max=-1;
    for(int i=0;i<=lastIndex;i++) {
      if(isThere.get(i) && (max==-1 || priorities[i]>priorities[max])) max=i;
    }
    return highestIndex=max;
  }

  /** Returns and removes the id with highest priority*/
  public int poll() {
    int pos=highestIndex();
    isThere.set(pos,false);
    numElements--;
    highestIndex=-1;
    int toReturn=data[pos];
    shrink();
    return(toReturn);
  }

  /** Shrinks the set space if it is too large*/
  protected void shrink() {
    if(numElements<data.length/2 && data.length>300) { 
      IdPriorityQueue result=new IdPriorityQueue(numElements+100);
      result.addAll(this);
      this.data=result.data;
      this.isThere=result.isThere;
      this.priorities=result.priorities;
      this.lastIndex=result.lastIndex;
      this.numElements=result.numElements;
      this.highestIndex=-1;
    }
  }
  
  /** Returns the highest priority*/
  public double highestPriority() {
    return(priorities[highestIndex()]);
  }

  /** Returns the id with the highest priority*/
  public int peek() {
    return(data[highestIndex()]);
  }

  @Override
  public boolean remove(int i) {
    int pos=indexOf(i);
    if(pos==-1) return false;
    if(pos==highestIndex) highestIndex=-1;
    removeIndex(pos);
    shrink();
    return true;    
  }    
  
  @Override
  public void clear(int capacity) {    
    super.clear(capacity);
    priorities=new double[capacity];
    highestIndex=-1;
  }  
  
  // ------------ Wrappers -----------------
  
  public void addAll(IdPriorityQueue s) {
    for(int i=0;i<=s.lastIndex;i++) {
      if(s.isThere.get(i)) add(s.data[i],s.priorities[i]);
    }
  }
  
  @Override
  public boolean add(int v) {    
    throw new UnsupportedOperationException();
  }
  
  @Override
  public String toString() {
    StringBuilder result=new StringBuilder("[");
    for(int i=0;i<=lastIndex;i++) {
      if(!isThere.get(i)) continue;
      result.append(data[i]).append(" (").append(priorities[i]).append("), ");
    }
    if(result.length()>2) result.setLength(result.length()-2);
    return result.append(']').toString();
  }
    
  /** Test */
  public static void main(String[] args) {
    IdPriorityQueue q=new IdPriorityQueue();    
    for(int i=1;i<10;i++) q.add(i,1000-i*10);
    D.p(q);    
    q.add(2, 100);    
    D.p(q);
    q.remove(2);
    q.remove(3);
    q.add(3,40);
    D.p(q);
    while(!q.isEmpty()) {
      D.p(q);
      D.p(q.poll());
    }
  }
  
}
