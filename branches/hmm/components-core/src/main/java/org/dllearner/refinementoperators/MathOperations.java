/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.refinementoperators;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Union;
import org.dllearner.utilities.owl.ConceptComparator;

/**
 * Math operations related to refinement operators.
 * 
 * @author Jens Lehmann
 *
 */
public class MathOperations {

	/**
	 * This function implements the getCombos method. Through the
	 * use of the upper limit, it is guaranteed that it
	 * will never return doublettes, so no special handling for
	 * them is necessary.
	 * 
	 * @see #getCombos(int)
	 * @param number Number to decompose.
	 * @param upperLimit Maximum number allowed in sum.
	 * @param bisher Numbers created so far.
	 * @param combosTmp Temporary list of combinations (filled during run).
	 */
	private static void decompose(int number, int upperLimit, LinkedList<Integer> bisher, List<List<Integer>> combosTmp) {
		
	    for (int i = Math.min(number, upperLimit); i >= 1; i--)
	    {
	    	
	    	LinkedList<Integer> newBisher = null;
	    	// für i==0 wird aus Effizienzgründen die bisherige Liste genommen
	    	if(i==0) {
	    		newBisher = bisher;
	    		newBisher.add(i);
	    	// für zahl - i == 1 muss gar keine Liste erstellt werden, da dann keine
	    	// Zerlegung mehr möglich ist
	    	} else if(number - i != 1) {
	    		newBisher = cloneList(bisher);
	    		newBisher.add(i);
	    	}
	    	
	        
	        if (number - i > 1)
	        {
	            // i wird hinzugefügt, d.h.
	            // - es muss nur noch zahl - i - 1 zerlegt werden (-1 wegen OR-Symbol)
	            // - es darf keine größere Zahl als i mehr vorkommen
	            // (dadurch gehen keine Kombinationen verloren)
	            decompose(number - i - 1, i, newBisher,combosTmp);
	        }
	        // Fall zahl == i, d.h. es muss nicht weiter zerlegt werden
	        else if(number - i == 0){
	        	combosTmp.add(newBisher);
	        }
	        

	    }	
	    
	    // numbers.add(bisher);
	}
	
	/**
	 * Given <code>number</code>, the functions returns all 
	 * combinations of natural numbers plus the number count 
	 * (which can be thought of as the number of interconnecting
	 * symbols between those numbers) adds up to <code>number</code>.
	 * 
	 * It uses an efficient algorithm to achieve this, which can 
	 * handle number=50 in less than a second and number=30 in 
	 * about 10 milliseconds on an average PC.
	 * 
	 * For illustrating the function, the return values of the first numbers
	 * are given:
	 * number = 1: [[1]]
	 * number = 2: [[2]]
	 * number = 3: [[3], [1, 1]]
	 * number = 4: [[4], [2, 1]]
	 * number = 5: [[5], [3, 1], [2, 2], [1, 1, 1]]
	 * number = 6: [[6], [4, 1], [3, 2], [2, 1, 1]]
	 * number = 7: [[7], [5, 1], [4, 2], [3, 3], [3, 1, 1], [2, 2, 1], [1, 1, 1, 1]]
	 * 
	 * @param number A natural number.
	 * @return A two dimensional list constructed as described above.
	 */
	public static List<List<Integer>> getCombos(int number) {
		// on Notebook: length 70 in 17 seconds, length 50 in 800ms, length 30 in 15ms		
		LinkedList<List<Integer>> combosTmp = new LinkedList<List<Integer>>();
		decompose(number, number, new LinkedList<Integer>(), combosTmp);
		return combosTmp;
	}
	
	/**
	 * Methods for computing combinations with the additional restriction
	 * that <code>maxValue</code> is the highest natural number, which can
	 * occur.
	 * @see #getCombos(int)
	 * @param length Length of construct.
	 * @param maxValue Maximum value which can occur in sum.
	 * @return A two dimensional list constructed in {@link #getCombos(int)}.
	 */
	public static List<List<Integer>> getCombos(int length, int maxValue) {		
		LinkedList<List<Integer>> combosTmp = new LinkedList<List<Integer>>();
		decompose(length, maxValue, new LinkedList<Integer>(), combosTmp);
		return combosTmp;
	}	
	
	@SuppressWarnings("unchecked")
	private static LinkedList<Integer> cloneList(LinkedList<Integer> list) {
		return (LinkedList<Integer>) list.clone();
	}
	
	/**
	 * Implements a cross product in the sense that each union description in the
	 * base set is extended by each description in the new set. 
	 * 
	 * Example:
	 * baseSet = {A1 OR A2, A1 or A3}
	 * newSet = {A1, EXISTS r.A3}
	 * 
	 * Returns:
	 * {A1 OR A2 OR A1, A1 OR A2 OR EXISTS r.A3, A1 OR A3 OR A1, A1 OR A3 OR EXISTS r.A3}
	 * 
	 * If the base set is empty, then the return value are union class descriptions
	 * for each value in newSet (a union with only one concept).
	 * 
	 * @param baseSet A set of union class descriptions.
	 * @param newSet The descriptions to add to each union class descriptions.
	 * @return The "cross product" of baseSet and newSet.
	 */
	public static SortedSet<Union> incCrossProduct(Set<Union> baseSet, Set<Description> newSet) {
		SortedSet<Union> retSet = new TreeSet<Union>(new ConceptComparator());
	
		if(baseSet.isEmpty()) {
			for(Description c : newSet) {
				Union md = new Union();
				md.addChild(c);
				retSet.add(md);
			}
			return retSet;
		}
		
		for(Union md : baseSet) {
			for(Description c : newSet) {
				Union mdNew = new Union(md.getChildren());
				mdNew.addChild(c);
				retSet.add(mdNew);
			}
		}
		
		return retSet;
	}	
	
	/**
	 * Returns true if the same property is used twice in an object some
	 * restriction, e.g. (EXISTS r.A1 AND A2 AND EXISTS r.A3) returns true,
	 * while (A1 OR A2) and (EXISTS r.A1 AND A2 AND EXISTS s.A3) return false.
	 * Note that the method does not work recursively, e.g. it return false 
	 * for EXISTS r.(EXISTS r.A1 AND A2 AND EXISTS r.A3).
	 * 
	 * @param d Description to test.
	 * @return See description.
	 */
	public static boolean containsDoubleObjectSomeRestriction(Description d) {
		Set<String> roles = new TreeSet<String>();
		for(Description c : d.getChildren()) {
			if(c instanceof ObjectSomeRestriction) {
				String role = ((ObjectSomeRestriction)c).getRole().getName();								
				boolean roleExists = !roles.add(role);
				if(roleExists)
					return true;
			}
		}
		return false;
	}
}
