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

package org.dllearner.kb.manipulator;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.RDFNodeTuple;

public class SimpleObjectFilterRule extends Rule{
	
	String objectFilter;

	public SimpleObjectFilterRule(Months month, String objectFilter) {
		super(month);
		this.objectFilter = objectFilter;
	}
	
	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		SortedSet<RDFNodeTuple> keep = new TreeSet<RDFNodeTuple>();
		for (RDFNodeTuple tuple : tuples) {
			if(!tuple.bPartContains(objectFilter)){
				keep.add(tuple);
			}else{
				logJamon();
			}
		}
		return  keep;
	}
	
	@Override
	public void logJamon(){
		JamonMonitorLogger.increaseCount(SimpleObjectFilterRule.class, "filteredTriples");
	}

	/*
	private boolean keepTuple(Node subject, RDFNodeTuple tuple) {
		
		for (int i = 0; i < filterRules.size(); i++) {
			Rule fr = filterRules.get(i);
			if (!(fr.keepTuple(subject, tuple))) {
				return false;
			}
		}
		return true;
	}*/
	
	
	
}
