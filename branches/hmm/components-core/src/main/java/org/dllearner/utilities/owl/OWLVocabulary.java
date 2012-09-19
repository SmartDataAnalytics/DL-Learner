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

package org.dllearner.utilities.owl;

public class OWLVocabulary {

	public static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	public static final String RDF_FIRST = "http://www.w3.org/1999/02/22-rdf-syntax-ns#first";
	public static final String RDF_REST = "http://www.w3.org/1999/02/22-rdf-syntax-ns#rest";
	public static final String RDF_NIL = "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil";
					
	public static final String RDFS_SUBCLASS_OF = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
	public static final String RDFS_CLASS = "http://www.w3.org/2000/01/rdf-schema#Class";
	public static final String RDFS_IS_DEFINED_BY = "http://www.w3.org/2000/01/rdf-schema#isDefinedBy";
	
	public static final String RDFS_COMMENT = "http://www.w3.org/2000/01/rdf-schema#comment";
	public static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
	public static final String RDFS_DESCRIPTION = "http://www.w3.org/2000/01/rdf-schema#description";
	
	public static final String RDFS_SUB_PROPERTY_OF = "http://www.w3.org/2000/01/rdf-schema#subPropertyOf";
	public static final String RDFS_domain = "http://www.w3.org/2000/01/rdf-schema#domain";
	public static final String RDFS_range = "http://www.w3.org/2000/01/rdf-schema#range";
	
	
	
	public static final String OWL_SAME_AS = "http://www.w3.org/2002/07/owl#sameAs";
	public static final String OWL_DIFFERENT_FROM = "http://www.w3.org/2002/07/owl#differentFrom";
	
	public static final String OWL_OBJECTPROPERTY = "http://www.w3.org/2002/07/owl#ObjectProperty";
	public static final String OWL_DATATYPPROPERTY = "http://www.w3.org/2002/07/owl#DataTypeProperty";
	public static final String OWL_CLASS = "http://www.w3.org/2002/07/owl#Class";
	
	public static final String OWL_SUBCLASS_OF = "http://www.w3.org/2002/07/owl#subClassOf";
	public static final String OWL_DISJOINT_WITH = "http://www.w3.org/2002/07/owl#disjointWith";
	public static final String OWL_EQUIVALENT_CLASS = "http://www.w3.org/2002/07/owl#equivalentClass";
	
	public static final String OWL_intersectionOf = "http://www.w3.org/2002/07/owl#intersectionOf";
	public static final String OWL_unionOf = "http://www.w3.org/2002/07/owl#unionOf";
	public static final String OWL_complementOf = "http://www.w3.org/2002/07/owl#complementOf";

	public static final String OWL_RESTRICTION =  "http://www.w3.org/2002/07/owl#Restriction";
	public static final String OWL_ON_PROPERTY = 	"http://www.w3.org/2002/07/owl#onProperty";
	
	public static final String OWL_ALL_VALUES_FROM =  "http://www.w3.org/2002/07/owl#allValuesFrom";
	public static final String OWL_SOME_VALUES_FROM =  "http://www.w3.org/2002/07/owl#allValuesFrom";
	public static final String OWL_HAS_VALUE =  "http://www.w3.org/2002/07/owl#hasValue";
	
	public static final String OWL_maxCardinality  = 	"http://www.w3.org/2002/07/owl#maxCardinality";
	public static final String OWL_minCardinality = 	"http://www.w3.org/2002/07/owl#minCardinality";
	public static final String OWL_cardinality = 	"http://www.w3.org/2002/07/owl#cardinality";

	
	public static final String OWL_FunctionalProperty = "http://www.w3.org/2002/07/owl#FunctionalProperty";
	public static final String OWL_InverseFunctionalProperty = "http://www.w3.org/2002/07/owl#InverseFunctionalProperty";
	public static final String OWL_TransitiveProperty = "http://www.w3.org/2002/07/owl#TransitiveProperty";
	public static final String OWL_SymmetricProperty = "http://www.w3.org/2002/07/owl#SymmetricProperty";
	
	
	public static final String OWL_equivalentProperty = "http://www.w3.org/2002/07/owl#equivalentProperty";
	public static final String OWL_inverseOf = "http://www.w3.org/2002/07/owl#inverseOf";


	
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
