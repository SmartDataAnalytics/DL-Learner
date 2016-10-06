/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.decisiontrees.utils;

import java.util.ArrayList;
import java.util.List;

public class SetUtils{

	
	//************************** OPERAZIONI SUI SOTTOINSIEMI DEL FRAME OF DISCERNEMENT     **********************************/
	 

	/**
	 * Verifies if an element is in the list 
	 * @param elem
	 * @param list
	 * @return
	 */
	public static <T> boolean isIn(T elem, List<T> list){
		for(Object current:list){
			if (elem.equals(current)){
				
				return true;
			}
			
		}
		return false;
		
	}

	/**
	 * Returns the intersection between two lists
	 * @param <T>
	 * @param list1
	 * @param list2
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public  static <T> List<T> intersection(List<T> list1, List<T> list2){
		// check the maximum lenght between list1 and  list1
		if(list1.size()<=list2.size()){
			List<T> intersection= new ArrayList<>();
			// if  the element of list2 are contained in lista 2, list2 is the intersection
			for(Object elem: list2){
				if(isIn((T)elem, list1)){
					intersection.add((T)elem);
				}
					
				
			}
			
			return intersection;
		}
		else
			return intersection(list2,list1); // recursive call 
	}
	
	/**
	 * Returns the list that is the union of the two list (without duplicates) 	 
	 * @param list1
	 * @param list2
	 * @return
	 */
	public static <T> List<T> union(List<T> list1,List<T> list2){
		List<T> result= intersection(list1, list2);// take the common elements between  list 2 and list 2
		
		for(T elem:list1){ //add the element that are in list1 but not in result yet
			if(!isIn(elem,list1)){
				result.add(elem);
			}
		}
			
		for(T elem:list2){ //add the elment that are in list2 but not in the result yet
			if(!isIn(elem,list2)){
				result.add(elem);
			}
		}
		return result;
		
	}
	/**
	 * Check if the two lists contain exactly the same elements
	 * @param <T>
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static <T> boolean areEquals(List<T>l1, List<T> l2){
		return l1.containsAll(l2) && l2.containsAll(l1);

	}
	
	/**
	 * Find an element in a power set 
	 * @param classes
	 * @param powerSet
	 * @return
	 */
	public static <T> int find(List<T> classes, List<List<T>> powerSet){
		int pos=0;
		for(List<T> elem:powerSet){
			
			if(areEquals(classes,elem))
				return pos;
			else
				pos++;
		}
		return -1; // nel caso in cui non lo trova
		
		
	}
	public static <T> int find(List<T> classes, List<T>[] powerSet){
		int pos=0;
		for(List<T> elem:powerSet){
			
			if(areEquals(classes,elem))
				return pos;
			else
				pos++;
		}
		return -1; // nel caso in cui non lo trova
		
		
	}
	public static <T> T extractValueFromSingleton(List<T> list){
		
		if (list.size()!=1)
			throw new RuntimeException("It is not a singleton");
		return (list.get(0));
		
		
	}
	public static <T> void insertValueinSingleton(List<T> list,T elem){
		
		if (list.size()!=0)
			throw new RuntimeException("The list is not empty");
		list.add(elem);
		
		
	}
	/**
	 * Returns sublists having a predetermined length
	 * @param powerSet
	 * @param cardinality
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static <T> List[] getSubsets(List<T>[] powerSet, int cardinality){
		if(cardinality>Math.log10(powerSet.length)/Math.log10(2))
			throw new RuntimeException("La cardinalit� � maggiore di"+(Math.log10(powerSet.length)/Math.log10(2)));
		
		List<List<T>> sottoinsiemi= new ArrayList<>();
		for(List<T> elem:powerSet){
			if(elem.size()==cardinality)
				sottoinsiemi.add(elem);
			
		}
		//converto tutto in un array
		List[] result= new List[sottoinsiemi.size()];
		for(int i=0;i<result.length;i++){
			result[i]=sottoinsiemi.get(i);
			
		}
		return result;
	} 
	

}
