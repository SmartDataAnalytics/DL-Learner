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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class DBPediaNavigatorCityLocatorRule extends Rule{
	
	Map<String,String> map=new HashMap<String,String>();


	public DBPediaNavigatorCityLocatorRule(Months month){
		super(month);
		map.put("http://dbpedia.org/class/custom/City_in_Saxony", "http://dbpedia.org/class/custom/City_in_Europe");
		map.put("http://dbpedia.org/class/custom/City_in_Egypt", "http://dbpedia.org/class/custom/City_in_Africa");
		map.put("http://dbpedia.org/class/custom/City_in_Europe", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_Asia", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_Australia", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_North_America", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_South_America", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_Africa", "http://dbpedia.org/class/yago/City108524735");
		map.put("http://dbpedia.org/class/custom/City_in_World", "http://dbpedia.org/class/yago/City108524735");
	}
	

	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		
		String uri;
		if(( uri = map.get(subject.getURIString().toString()) ) == null) {
			return tuples;
		}else {
			tuples.add(new RDFNodeTuple(new ResourceImpl(OWLVocabulary.RDFS_SUBCLASS_OF), new ResourceImpl(uri)));
			return tuples;
		}
		
	
	}
	
	@Override
	public void logJamon(){
		
	}
	
	
	
	
	
}
