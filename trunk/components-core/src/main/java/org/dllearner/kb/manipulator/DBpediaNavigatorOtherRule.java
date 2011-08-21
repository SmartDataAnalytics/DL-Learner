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

import org.dllearner.kb.extraction.Node;
import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;


public class DBpediaNavigatorOtherRule extends Rule{
	
	
	public DBpediaNavigatorOtherRule(Months month){
		super(month);
	}
	// Set<String> classproperties;
	
	@Override
	public  SortedSet<RDFNodeTuple> applyRule(Node subject, SortedSet<RDFNodeTuple> tuples){
		float lat=0;
		float lng=0;
		RDFNode clazz = null;
		RDFNodeTuple typeTuple = null;
		for (RDFNodeTuple tuple : tuples) {
						
			if (tuple.a.toString().equals(OWLVocabulary.RDF_TYPE)){
				clazz = tuple.b;
				typeTuple = tuple;
			}
			
			
			if (tuple.a.toString().equals("http://www.w3.org/2003/01/geo/wgs84_pos#lat") && tuple.b.isLiteral()){
				lat = ((Literal) tuple.b).getFloat();
				//lat=Float.parseFloat(tuple.b.toString().substring(0,tuple.b.toString().indexOf("^^")));
			}
			if (tuple.a.toString().equals("http://www.w3.org/2003/01/geo/wgs84_pos#long") && tuple.b.isLiteral()) {
				lng = ((Literal) tuple.b).getFloat();
				//lng=Float.parseFloat(tuple.b.toString().substring(0,tuple.b.toString().indexOf("^^")));
			}
				
		}//end for
		if (clazz.toString().equals("http://dbpedia.org/class/yago/City108524735")){
			String newType = getTypeToCoordinates(lat, lng);
			tuples.add(new RDFNodeTuple(new ResourceImpl(OWLVocabulary.RDF_TYPE),new ResourceImpl(newType)));
			//tuples.add(new StringTuple("http://www.w3.org/1999/02/22-rdf-syntax-ns#type",newType));
			tuples.remove(typeTuple);
		}
		
		return tuples;
	}

	public static String getTypeToCoordinates(float lat, float lng){
		if (lat<71.08&&lat>33.39&&lng>-24.01&&lng<50.8){
			if (lat>50&&lat<52&&lng>12&&lng<13){
				return "http://dbpedia.org/class/custom/City_in_Saxony";
			}
			else return "http://dbpedia.org/class/custom/City_in_Europe";
		}
		else if (lng>-17.5&&lng<52.04&&lat>-36&&lat<36.6){
			if (lat>21.45&&lat<31.51&&lng>24.7&&lng<37.26){
				return "http://dbpedia.org/class/custom/City_in_Egypt";
			}
			else return "http://dbpedia.org/class/custom/City_in_Africa";
		}
		else if (((lng>27.4&&lng<180)||(lng<-168.75))&&lat>-11.2){
			return "http://dbpedia.org/class/custom/City_in_Asia";
		}
		else if (lng>113.9&&lng<179.65&&lat<-10.8&&lat>-47.04){
			return "http://dbpedia.org/class/custom/City_in_Australia";
		}
		else if (lng>-168.4&&lng<-19.7&&lat>6.6){
			return "http://dbpedia.org/class/custom/City_in_North_America";
		}
		else if (lng>-81.56&&lng<-34.1&&lat<6.6){
			return "http://dbpedia.org/class/custom/City_in_South_America";
		}	
		else return "http://dbpedia.org/class/custom/City_in_World";
	}
	
	@Override
	public void logJamon(){
		
	}

}
