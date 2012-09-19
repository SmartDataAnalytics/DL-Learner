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

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;


public class DBpediaNavigatorFilterRule extends Rule{
	
	
	public DBpediaNavigatorFilterRule(Months month){
		super(month);
	}
	// Set<String> classproperties;
	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
//		RDFNode clazz = null;
		RDFNodeTuple typeTuple = null;
		List<RDFNodeTuple> toRemove=new LinkedList<RDFNodeTuple>();
		for (RDFNodeTuple tuple : tuples) {
						
			if (tuple.a.toString().equals(OWLVocabulary.RDF_TYPE)){
//				clazz = tuple.b;
				typeTuple = tuple;
			}
			
			if (tuple.a.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && !(tuple.b.toString().startsWith("http://dbpedia.org/class/yago"))){
				toRemove.add(typeTuple);
			}
			/*if (tuple.a.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && !(tuple.b.toString().startsWith("http://dbpedia.org/ontology"))){
				toRemove.add(typeTuple);
			}*/
		}//end for
		for (RDFNodeTuple tuple : toRemove)
			tuples.remove(tuple);
		return tuples;
	}

	@Override
	public void logJamon(){
		
	}

}
