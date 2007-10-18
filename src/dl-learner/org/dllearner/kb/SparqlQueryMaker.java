/**
 * Copyright (C) 2007, Sebastian Hellmann
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
package org.dllearner.kb;

/**
 * 
 * This class produces sparql queries
 * 
 * @author Sebastian Hellmann
 *
 */
public class SparqlQueryMaker {
	
	/**
	 * reads all the options and makes the sparql query accordingly
	 * @param subject
	 * @param sf special object encapsulating all options
	 * @return sparql query
	 */
	public String makeQueryFilter(String subject, SparqlFilter sf){
		
		
		String Filter="";
		if(!sf.useLiterals)Filter+="!isLiteral(?object)";
		for (String  p : sf.getPredFilter()) {
			Filter+="\n" + filterPredicate(p);
		}
		for (String  o : sf.getObjFilter()) {
			Filter+="\n" + filterObject(o);
		}
		
		String ret=		
		"SELECT * WHERE { \n" +
		"<"+
		subject+
		"> ?predicate ?object.\n";
		if (!(Filter.length()==0)) 
			ret+="FILTER( \n" +
				"(" +Filter+")).";
		ret+="}";
		//System.out.println(ret);
		return ret;
	}
	
	/**
	 * add a new object filter
	 * (objects are filtered out of sparql result)
	 * @param ns namespace
	 * @return
	 */
	public String filterObject(String ns){
		 return "&&( !regex((?object), '"+ns+"') )";
	}
	/**
	* add a new object filter
	 * (objects are filtered out of sparql result)
	 * @param ns namespace
	 * * @return
	 */
	public String filterPredicate(String ns){
		 return "&&( !regex(str(?predicate), '"+ns+"') )";
	}
	
	public String makeLabelQuery(String label,int limit){
		return  "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"+
				"SELECT DISTINCT ?subject\n"+
				"WHERE { ?subject rdfs:label ?object.FILTER regex(?object,\""+label+"\"@en)}\n"+
				"LIMIT "+limit;
	}
}
