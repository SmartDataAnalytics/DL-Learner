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
package org.dllearner.kb.sparql.test;

import org.dllearner.kb.sparql.configuration.SparqlEndpoint;
import org.dllearner.kb.sparql.query.SparqlQuery;

public class TestStaticQueries {

	// tests makeArticleQuery
	//
	//
	public static void main(String[] args) {
		String test1="http://dbpedia.org/resource/Angela_Merkel";
		String test2="http://dbpedia.org/resource/Leipzig";
		String test3="http://dbpedia.org/class/yago/Woman110787470";
		boolean one=false;
		boolean two=true;
		boolean three=false;
		try {
			if(one){
			//System.out.println(SparqlQuery.makeArticleQuery(test1,
				//	SparqlEndpoint.getEndpointByNumber(1)).getAsXMLString());
			//System.out.println(SparqlQuery.makeArticleQuery(test1,
				//	SparqlEndpoint.getEndpointByNumber(1)).getAsList());
			System.out.println(SparqlQuery.makeArticleQuery(test1,
					SparqlEndpoint.getEndpointByNumber(1)).getAsVectorOfTupels("predicate", "object"));
			}
		
			if(two){
			System.out.println(SparqlQuery.makeLabelQuery(test2,10,
					SparqlEndpoint.getEndpointByNumber(1)).getAsXMLString());
			System.out.println(SparqlQuery.makeLabelQuery(test2,10,
					SparqlEndpoint.getEndpointByNumber(1)).getAsList());
			System.out.println(SparqlQuery.makeLabelQuery(test2,10,
					SparqlEndpoint.getEndpointByNumber(1)).getAsVector("subject"));
			}
			if(three){
				System.out.println(SparqlQuery.makeConceptQuery(test3,
						SparqlEndpoint.getEndpointByNumber(1)).getAsXMLString());
				System.out.println(SparqlQuery.makeConceptQuery(test3,
						SparqlEndpoint.getEndpointByNumber(1)).getAsList());
				System.out.println(SparqlQuery.makeConceptQuery(test3,
						SparqlEndpoint.getEndpointByNumber(1)).getAsVector("subject"));
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
