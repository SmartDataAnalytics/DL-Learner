/**
 * 
 */
package org.dllearner.kb.sparql;

/**
 * @author Lorenz Buehmann
 *
 */
public class SPARQLQueryUtils {
	
	public static final String PREFIXES = 
			"PREFIX owl:<http://www.w3.org/2002/07/owl#> "
			+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>";
	
	// base entities
	public static final String SELECT_CLASSES_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 a owl:Class .}";
	public static final String SELECT_OBJECT_PROPERTIES_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 a owl:ObjectProperty .}";
	public static final String SELECT_DATA_PROPERTIES_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 a owl:DatatypeProperty .}";
	public static final String SELECT_INDIVIDUALS_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 a owl:NamedIndividual .}";
	
	// extended
	public static final String SELECT_DATA_PROPERTIES_BY_RANGE_QUERY = PREFIXES
			+ "SELECT ?var1 WHERE {?var1 a owl:DatatypeProperty . ?var1 rdfs:range <%s> . }";

	// class hierarchy queries
	public static final String SELECT_SUBCLASS_OF_QUERY = PREFIXES + "SELECT ?var1 WHERE {?var1 rdfs:subClassOf <%s> .}";
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
			"		FILTER( ! BOUND(?inbetweener) && ?super != ?concept)\n" + 
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
