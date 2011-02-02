package javatools.datatypes;
import java.util.Iterator;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

This class allows to use an iterator in a for-each-loop.<BR>
Example:
<PRE>
   for(String s : new Scanner("Scan this string")) {
      // Compiletime error, because Scanner is an Iterator but not Iterable
   }
   
   for(String s : new IterableForIterator&lt;String>(new Scanner("Scan this string"))) {
     // works fine
   }
   
</PRE>
 */ 
public class IterableForIterator<T> implements Iterable<T> {
  public Iterator<T> iterator;
  
  public IterableForIterator(Iterator<T> iterator) {
    this.iterator=iterator;
  }

  public Iterator<T> iterator() {
    return iterator;
  }

}
