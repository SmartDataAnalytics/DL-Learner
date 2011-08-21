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

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class ObjectReplacementRule extends Rule{
	
	String oldObject;
	String newObject;


	public ObjectReplacementRule(Months month, String oldObject, String newObject) {
		super(month);
		this.oldObject = oldObject;
		this.newObject = newObject;
	}
	
	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		SortedSet<RDFNodeTuple> keep = new TreeSet<RDFNodeTuple>();
		for (RDFNodeTuple tuple : tuples) {
			if(tuple.bPartContains(oldObject)){
				String tmp = tuple.b.toString().replace(oldObject, newObject);
				tuple.b = new ResourceImpl(tmp);
				JamonMonitorLogger.increaseCount(ObjectReplacementRule.class, "replacedObjects");
			}
			keep.add(tuple);
		}
		return  keep;
	}

	@Override
	public void logJamon(){
		JamonMonitorLogger.increaseCount(ObjectReplacementRule.class, "replacedObjects");
	}
	
	
	
}
