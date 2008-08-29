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
package org.dllearner.utilities.owl;

public class OWLVocabulary {

	public static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	public static final String RDFS_SUBCLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
	public static final String RDFS_CLASS = "http://www.w3.org/2000/01/rdf-schema#Class";
	
	
	public static final String OWL_SAME_AS = "http://www.w3.org/2002/07/owl#sameAs";
	public static final String OWL_OBJECTPROPERTY = "http://www.w3.org/2002/07/owl#ObjectProperty";
	public static final String OWL_DATATYPPROPERTY = "http://www.w3.org/2002/07/owl#DataTypeProperty";
	public static final String OWL_CLASS = "http://www.w3.org/2002/07/owl#Class";
	public static final String OWL_SUBCLASS_OF = "http://www.w3.org/2002/07/owl#subClassOf";
	
	public static final String OWL_THING = "http://www.w3.org/2002/07/owl#Thing";
	public static final String OWL_NOTHING = "http://www.w3.org/2002/07/owl#Nothing";
	
	//OWL2 Namespace: http://www.w3.org/2006/12/owl2#
	
	public static boolean isStringClassVocab (String possClass){
		return (RDFS_CLASS.equalsIgnoreCase(possClass)
		|| OWL_CLASS.equalsIgnoreCase(possClass));
		
	}
	
	public static boolean isStringSubClassVocab (String possSubClass){
		return (RDFS_SUBCLASS_OF.equalsIgnoreCase(possSubClass)
		|| OWL_SUBCLASS_OF.equalsIgnoreCase(possSubClass));
		
	}
//	public static final String RDF_TYPE = "";
//	public static final String RDF_TYPE = "";
//	public static final String RDF_TYPE = "";
//	public static final String RDF_TYPE = "";
//	public static final String RDF_TYPE = "";
//	public static final String RDF_TYPE = "";
}
