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
package org.dllearner.kb.sparql;

/**
 * 
 * 
 * encapsulates all the options 
 * see the documentation for more help
 * 
 * @author Sebastian Hellmann
 * @author Sebastian Knappe
 *
 */
public class SparqlFilter {
	public int mode=0;
	//  0 yago, 1 only cat, 2 skos+cat
	String[] PredFilter=null;
	String[] ObjFilter=null;
	public boolean useLiterals=false;
	
	
	String[] yagoPredFilterDefault={
			"http://www.w3.org/2004/02/skos/core",
			"http://xmlns.com/foaf/0.1/",	
			"http://dbpedia.org/property/wikipage-",
			"http://www.w3.org/2002/07/owl#sameAs",
			"http://dbpedia.org/property/reference"	};
	String[] yagoObjFilterDefault={
			"http://dbpedia.org/resource/Category:Articles_",
			"http://dbpedia.org/resource/Category:Wikipedia_",
			"http://xmlns.com/foaf/0.1/",
			"http://dbpedia.org/resource/Category",
			"http://dbpedia.org/resource/Template",
			"http://upload.wikimedia.org/wikipedia/commons"};
	
	String[] onlyCatPredFilterDefault={
			"http://www.w3.org/2004/02/skos/core",
			"http://xmlns.com/foaf/0.1/",	
			"http://dbpedia.org/property/wikipage-",
			"http://www.w3.org/2002/07/owl#sameAs",
			"http://dbpedia.org/property/reference"	};
	String[] onlyCatObjFilterDefault={
			"http://dbpedia.org/resource/Category:Articles_",
			"http://dbpedia.org/resource/Category:Wikipedia_",
			"http://xmlns.com/foaf/0.1/",
			"http://dbpedia.org/class/yago",
			"http://dbpedia.org/resource/Template",
			"http://upload.wikimedia.org/wikipedia/commons"};
	
	String[] skosPredFilterDefault={
			"http://www.w3.org/2004/02/skos/core#narrower",
			"http://xmlns.com/foaf/0.1/",	
			"http://dbpedia.org/property/wikipage-",
			"http://www.w3.org/2002/07/owl#sameAs",
			"http://dbpedia.org/property/reference"	};
	String[] skosObjFilterDefault={
			"http://dbpedia.org/resource/Category:Articles_",
			"http://dbpedia.org/resource/Category:Wikipedia_",
			"http://xmlns.com/foaf/0.1/",
			"http://dbpedia.org/class/yago",
			"http://dbpedia.org/resource/Template",
			"http://upload.wikimedia.org/wikipedia/commons"};
	
	public SparqlFilter(int mode, String[] pred, String[] obj) {
		if (mode==-1 && (pred==null || obj==null))
			{mode=0;}
		this.mode=mode;
		
		switch (mode){
		case 0: //yago
			ObjFilter=yagoObjFilterDefault;
			PredFilter=yagoPredFilterDefault;
			break;
		case 1: // only Categories
			ObjFilter=onlyCatObjFilterDefault;
			PredFilter=onlyCatPredFilterDefault;			
			break;
		case 2: // there are some other changes to, which are made directly in other functions
			ObjFilter=skosObjFilterDefault;
			PredFilter=skosPredFilterDefault;			
			break;
		default:
			ObjFilter=obj;
			PredFilter=pred;
			break;
		}
	}
	
	public SparqlFilter(int mode, String[] pred, String[] obj,boolean uselits) throws Exception{
		this(mode,pred,obj);
		this.useLiterals=uselits;
	}
	
	public String[] getObjFilter(){
		return this.ObjFilter;
	}
	
	public String[] getPredFilter(){
		return this.PredFilter;
	}		
}