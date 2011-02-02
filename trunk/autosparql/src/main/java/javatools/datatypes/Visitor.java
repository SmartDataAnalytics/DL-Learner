package javatools.datatypes;
/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

This interface is for the common visitor design pattern
*/
public interface Visitor<T>   {
  /** Visits a T. Returns TRUE if the visit is to be continued */
  public boolean visit(T v) throws Exception;
}
