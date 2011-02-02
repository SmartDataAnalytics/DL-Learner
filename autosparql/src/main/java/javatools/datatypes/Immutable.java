package javatools.datatypes;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Iterator;

/** 
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License 
 (see http://creativecommons.org/licenses/by/3.0) by 
 the YAGO-NAGA team (see http://mpii.de/yago-naga).
 
 
 


 This class provides a wrapper for immutable lists and sets  
 */
public class Immutable {

  public static class List<E> extends AbstractList<E> {

    protected java.util.List<E> list;

    public List(java.util.List<E> l) {
      list = l;
    }

    public E get(int index) {
      return list.get(index);
    }

    @Override
    public int size() {
      return list.size();
    }
  }
  
  public static class Set<E> extends AbstractSet<E> {
    protected java.util.Set<E> set;
    public Set(java.util.Set<E> s) {
      set=s;
    }
    @Override
    public Iterator<E> iterator() {      
      return set.iterator();
    }

    @Override
    public int size() {
      return set.size();
    }
    
  }
}
