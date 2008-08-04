package org.dllearner.test;

import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.statistics.SimpleClock;

public class FilterTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String qlong="SELECT * WHERE {  <http://dbpedia.org/resource/%22Big%22_Ron> ?predicate ?object.  FILTER(  (!isLiteral(?object))&&( ( !regex(str(?predicate), 'http://dbpedia.org/property/relatedInstance') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/website') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/owner') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) &&( !regex(str(?predicate), 'http://www.w3.org/2002/07/owl#sameAs') ) &&( !regex(str(?predicate), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/standard') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/wikipage') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/reference') ) &&( !regex(str(?predicate), 'http://www.w3.org/2004/02/skos/core') ))&&( ( !regex(str(?object), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia') ) &&( !regex(str(?object), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) &&( !regex(str(?object), 'http://dbpedia.org/resource/Template') ) &&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia/commons') ) &&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) &&( !regex(str(?object), 'http://dbpedia.org/resource/Category:') ) &&( !regex(str(?object), 'http://www.w3.org/2004/02/skos/core') ) &&( !regex(str(?object), 'http://www.geonames.org') ))).}";
		String qshort="SELECT * WHERE {  <http://dbpedia.org/resource/%22Big%22_Ron> ?predicate ?object.  FILTER  (!isLiteral(?object)).}";
		
		SimpleClock sc = new SimpleClock();
		SPARQLTasks st = new SPARQLTasks(Cache.getPersistentCache(), SparqlEndpoint.getEndpointDBpedia());
		
		for (int i = 0; i < 10; i++) {
			st.query(qshort);
		}
		sc.printAndSet("long ");
		
		
		
		for (int i = 0; i < 10; i++) {
			st.query(qlong);
		}
		
		sc.printAndSet("short ");
	}

}
