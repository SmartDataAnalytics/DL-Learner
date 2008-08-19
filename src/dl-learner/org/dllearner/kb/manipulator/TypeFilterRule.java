/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.kb.manipulator;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

public class TypeFilterRule extends Rule{
	
	public static Logger logger = Logger.getLogger(TypeFilterRule.class);
	
	String predicateFilter;
	String objectFilter;
	String classCanonicalName;


	public TypeFilterRule(Months month, String predicateFilter, String objectFilter, Class<? extends Node> clazz) {
		super(month);
		this.predicateFilter = predicateFilter;
		this.objectFilter = objectFilter;
		this.classCanonicalName = clazz.getCanonicalName();
	}
	

	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		SortedSet<RDFNodeTuple> keep = new TreeSet<RDFNodeTuple>();
		for (RDFNodeTuple tuple : tuples) {
			String a = tuple.a.toString();
			String b = tuple.b.toString();
			//System.out.println(a+b);
			boolean remove = (tuple.aPartContains(predicateFilter) &&
					tuple.bPartContains(objectFilter) && 
					// QUALITY this might be dead wrong
					(classCanonicalName.equalsIgnoreCase(subject.getClass().getCanonicalName())) 
					);
			if(!remove){
				keep.add(tuple);
			}else{
				logger.warn("Removed: "+subject+"::"+tuple);
			}
			
		}
		return  keep;
	}
	

	/*
	if (t.a.equals(type) && t.b.equals(classns)
			&& node instanceof ClassNode) {
		toRemove.add(t);
	}

	// all with type class
	if (t.b.equals(classns) && node instanceof ClassNode) {
		toRemove.add(t);
	}

	// remove all instances with owl:type thing
	if (t.a.equals(type) && t.b.equals(thing)
			&& node instanceof InstanceNode) {
		toRemove.add(t);
	}
	*/
	
}
