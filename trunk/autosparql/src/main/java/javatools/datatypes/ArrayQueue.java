package javatools.datatypes;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import javatools.administrative.D;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 
  
This class implements a simple queue. The queue is not blocking (i.e. it will accept
as many elements as you give it). It is more efficient than a LinkedList. <BR>
Example:<BR>
<PRE>
    // Create a queue with some initial elements
    Queue&lt;Integer> a=new ArrayQueue&lt;Integer>(1,2,3,4,5,6,7,8);
    int counter=9;
    // Always add one new element and poll two
    while(a.size()!=0) {
      a.offer(counter++);
      D.p(a.poll());
      D.p(a.poll());      
    }
    -->
        1,2,3,...,14
</PRE>
*/
public class ArrayQueue<T> extends AbstractQueue<T> {
  /** Holds the queue elements*/
  protected List<T> data=new ArrayList<T>();
  /** Index of the first element*/
  protected int first=0;
  /** Index of the last element*/
  protected int last=0;
  /** Dummy blank objects used to enlarge the data array*/
  protected static Object[] blanks=new Object[10];
   
  public Iterator<T> iterator() {
    if(first<=last) return data.subList(first, last).iterator();
    return(new CombinedIterator<T>(data.subList(first, data.size()).iterator(),
          data.subList(0, last).iterator()));
  }

  public int size() {
    if(first<=last) return (last-first);
    return(data.size()-first+last);
  }

  @SuppressWarnings("unchecked")
  public boolean offer(T o) {
    data.set(last,o);    
    last=(last+1)%data.size();
    if(last==first) {
      // Enlarge the array
      data.addAll(last,Arrays.asList((T[])blanks));
      first+=blanks.length;
    }
    return true;
  }

  public T peek() {
    if(size()==0) return(null);
    return (data.get(first));
  }

  public T poll() {
    T result=peek();    
    if(size()!=0) first=(first+1)%data.size();
    return(result);
  }

  public ArrayQueue(T... initialData) {
    this(Arrays.asList(initialData));
  }
  
  public ArrayQueue(Collection<T> initialData) {
    data.add(null); // Ensure that the size is at least 1
    for(T element:initialData) {
      offer(element);
    }
  }
  
  /** Test routine */
  public static void main(String[] args) {
    Queue<Integer> a=new ArrayQueue<Integer>();
    a.offer(1);
    D.p(a.peek());
    //1,2,3,4,5,6,7,8);
    int counter=9;
    while(a.size()!=0) {
      a.offer(counter++);
      D.p(a.poll());
      D.p(a.poll());      
    }
  }


}
