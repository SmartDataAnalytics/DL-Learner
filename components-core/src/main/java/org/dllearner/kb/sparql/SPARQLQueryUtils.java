/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.kb.sparql;

/**
 * @author Lorenz Buehmann
 *
 */
public class SPARQLQueryUtils {
	
	public static final String PREFIXES =
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> "
			+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ";
	
	// base entities
	public static final String SELECT_CLASSES_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 a owl:Class .}";
	public static final String SELECT_OBJECT_PROPERTIES_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 a owl:ObjectProperty .}";
	public static final String SELECT_DATA_PROPERTIES_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 a owl:DatatypeProperty .}";
	public static final String SELECT_INDIVIDUALS_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 a owl:NamedIndividual .}";
	
	public static final String SELECT_CLASSES_QUERY_ALT = PREFIXES + "SELECT DISTINCT ?var1 WHERE {[] a ?var1 .}";
	public static final String SELECT_INDIVIDUALS_QUERY_ALT = PREFIXES + "SELECT ?var1 WHERE {?var1 a [] . \n"
			+ "OPTIONAL { ?s1 a ?var1. } \n"
			+ "OPTIONAL { ?s2 ?var1 []. } \n"
			+ "FILTER ( !BOUND(?s1) && !BOUND(?s2) ) }";
	
	// extended
	public static final String SELECT_DATA_PROPERTIES_BY_RANGE_QUERY = PREFIXES
			+ "SELECT ?var1 WHERE {?var1 a owl:DatatypeProperty . ?var1 rdfs:range <%s> . }";

	// class hierarchy queries
	public static final String SELECT_TOP_LEVEL_OWL_CLASSES = PREFIXES +
			"SELECT ?var1\n" +
			"WHERE { ?var1 a owl:Class .\n" +
			"FILTER ( ?var1 != owl:Thing && ?var1 != owl:Nothing ) .\n" +
			"OPTIONAL { ?var1 rdfs:subClassOf ?super .\n" +
			"FILTER ( ?super != owl:Thing && ?super != ?var1 && ?super != rdfs:Resource) } .\n" +
			"FILTER ( !BOUND(?super) ) }";
	
	public static final String SELECT_LEAF_CLASSES_OWL = PREFIXES +
			"SELECT ?var1\n" +
			"WHERE { ?var1 a owl:Class .\n" +
			"FILTER ( ?var1 != owl:Thing && ?var1 != owl:Nothing ) .\n" +
			"OPTIONAL { ?sub rdfs:subClassOf ?var1 .\n" +
			"FILTER ( ?sub != owl:Nothing && ?sub != ?var1 ) } .\n" +
			"FILTER ( !BOUND(?sub) ) }";
	
	public static final String SELECT_LEAF_CLASSES = PREFIXES +
			"SELECT ?var1\n" +
			"WHERE { ?var1 a owl:Class .\n" +
			"FILTER ( ?var1 != owl:Thing && ?var1 != owl:Nothing ) .\n" +
			"OPTIONAL { ?var1 rdfs:subClassOf ?sub .\n" +
			"FILTER ( ?sub != owl:Nothing && ?sub != ?var1 ) } .\n" +
			"FILTER ( !BOUND(?sub) ) }";
	
	public static final String SELECT_SUPERCLASS_OF_QUERY_INF = PREFIXES + "SELECT ?var1 WHERE { " +
	"<%s> (rdfs:subClassOf|owl:equivalentClass|^owl:equivalentClass|(owl:intersectionOf/rdf:rest*/rdf:first))* ?var1 .}";
	
	public static final String SELECT_SUPERCLASS_OF_QUERY_RDFS = PREFIXES + "SELECT ?var1 WHERE { " +
			"<%s> rdfs:subClassOf* ?var1 .}";

	public static final String SELECT_SUBCLASS_OF_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 rdfs:subClassOf <%s> .}";
	public static final String SELECT_DIRECT_SUBCLASS_OF_QUERY = PREFIXES +
			"SELECT ?var1 {\n" +
			"		BIND( <%s> as ?concept )\n" +
			"		?var1 rdfs:subClassOf ?concept .\n" +
			"		OPTIONAL {\n" +
			"		?concept rdfs:subClassOf ?inbetweener .\n" +
			"		?var1 rdfs:subClassOf ?inbetweener .\n" +
			"		FILTER( ?inbetweener != ?concept && ?inbetweener != ?var1 )\n" +
			"		}\n" +
			"		FILTER( ! BOUND(?inbetweener) && ?var1 != ?concept)\n" +
			"		}";
	public static final String SELECT_SUPERCLASS_OF_QUERY = PREFIXES + "SELECT ?var1 WHERE {<%s> rdfs:subClassOf ?var1 .}";
	public static final String SELECT_DIRECT_SUPERCLASS_OF_QUERY = PREFIXES +
			"SELECT ?var1 {\n" +
			"		BIND( <%s> as ?concept )\n" +
			"		?concept rdfs:subClassOf ?var1 .\n" +
			"		OPTIONAL {\n" +
			"		?concept rdfs:subClassOf ?inbetweener .\n" +
			"		?inbetweener rdfs:subClassOf ?var1 .\n" +
			"		FILTER( ?inbetweener != ?concept && ?inbetweener != ?var1 )\n" +
			"		}\n" +
			"		FILTER( ! BOUND(?inbetweener) && ?var1 != ?concept)\n" +
			"		}";
	public static final String SELECT_EQUIVALENT_CLASSES_QUERY = PREFIXES + "SELECT ?var1 WHERE {"
			+ "{?var1 owl:equivalentClass <%s> .} UNION {<%s> owl:equivalentClass ?var1 .}}";
	public static final String SELECT_DISJOINT_CLASSES_QUERY = PREFIXES + "SELECT ?var1 WHERE {"
			+ "{?var1 owl:disjointWith <%s> .} UNION {<%s> owl:disjointWith ?var1 .}}";
	public static final String ASK_SUBCLASS_OF_QUERY = PREFIXES + "ASK {<%s> rdfs:subClassOf <%s> .}";
	public static final String ASK_EQUIVALENT_CLASSES_QUERY = PREFIXES + "ASK {"
			+ "{<%s> owl:equivalentClass <%s> .} UNION {<%s> owl:equivalentClass ?var1 .}}";
	public static final String ASK_DISJOINT_CLASSES_QUERY = PREFIXES + "SELECT ?var1 WHERE {"
			+ "{<%s> owl:disjointWith <%s> .} UNION {<%s> owl:disjointWith <%s> .}}";
	public static final String SELECT_SIBLING_CLASSES_QUERY = PREFIXES
			+ "SELECT ?var1 WHERE {<%s> rdfs:subClassOf ?sup . ?var1 rdfs:subClassOf ?sup. FILTER(!SAMETERM(?var1, <%s>))}";

	// property hierarchy
	public static final String SELECT_SUBPROPERTY_OF_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 rdfs:subPropertyOf <%s> .}";
	public static final String SELECT_SUPERPROPERTY_OF_QUERY = PREFIXES + "SELECT ?var1 WHERE {<%s> rdfs:subPropertyOf ?var1 .}";
	public static final String SELECT_EQUIVALENT_PROPERTIES_QUERY = PREFIXES + "SELECT ?var1 WHERE {"
			+ "{?var1 owl:equivalentProperty <%s> .} UNION {<%s> owl:equivalentProperty ?var1 .}}";
	public static final String SELECT_DISJOINT_PROPERTIES_QUERY = PREFIXES + "SELECT ?var1 WHERE {"
			+ "{?var1 owl:propertyDisjointWith <%s> .} UNION {<%s> owl:propertyDisjointWith ?var1 .}}";
	
	// instance queries
	public static final String SELECT_CLASS_INSTANCES_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 a <%s> .}";
	public static final String SELECT_INSTANCE_TYPES_QUERY = PREFIXES + "SELECT ?var1 WHERE { <%s> a ?var1 .}";
	public static final String SELECT_PROPERTY_RELATIONSHIPS_QUERY = PREFIXES + "SELECT ?var1 ?var2 WHERE { ?var1 <%s> ?var2 .}";
}
