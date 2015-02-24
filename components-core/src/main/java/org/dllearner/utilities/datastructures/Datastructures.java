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

package org.dllearner.utilities.datastructures;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

/**
 * Conversion between different data structures.
 * 
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 *
 */
public class Datastructures {

	public static boolean strToBool(String str) {
		if (str.equals("true"))
			return true;
		else if (str.equals("false"))
			return false;
		else
			throw new Error("Cannot convert to boolean.");
	}

	/**
	 * easy conversion
	 * 
	 * @param s
	 */
	public static String[] setToArray(Set<String> s) {
		if(s==null)return null;
		String[] ret=new String[s.size()];
		int i=0;
		for (Iterator<String> iter = s.iterator(); iter.hasNext();) {
			ret[i] = iter.next();
			i++;
			
		}
		return ret;
		
	}
	
	public static String[] sortedSet2StringListIndividuals(Set<OWLIndividual> individuals){
		
		String[] ret=new String[individuals.size()];
		Iterator<OWLIndividual> i=individuals.iterator();
		int a=0;
		while (i.hasNext()){
			ret[a++]=i.next().toStringID();
		}
		Arrays.sort(ret);
		return ret;
	}
	
	public static String[] sortedSet2StringListRoles(Set<OWLObjectProperty> s){
		
		String[] ret=new String[s.size()];
		Iterator<OWLObjectProperty> i=s.iterator();
		int a=0;
		while (i.hasNext()){
			ret[a++]=i.next().toStringID();
		}
		Arrays.sort(ret);
		return ret;
	}
	
	public static String[] sortedSet2StringListConcepts(Set<OWLClass> s){
		
		String[] ret=new String[s.size()];
		Iterator<OWLClass> i=s.iterator();
		int a=0;
		while (i.hasNext()){
			ret[a++]=i.next().toStringID();
		}
		Arrays.sort(ret);
		return ret;
	}	
	
	public static Set<String> individualSetToStringSet(Set<OWLIndividual> individuals) {
		Set<String> ret = new TreeSet<String>();
		for(OWLIndividual ind : individuals) {
			ret.add(ind.toStringID());
		}
		return ret;
	}	
	
	public static Set<String> individualListToStringSet(List<OWLIndividual> individuals) {
		Set<String> ret = new TreeSet<String>();
		for(OWLIndividual ind : individuals) {
			ret.add(ind.toStringID());
		}
		return ret;
	}		
}
