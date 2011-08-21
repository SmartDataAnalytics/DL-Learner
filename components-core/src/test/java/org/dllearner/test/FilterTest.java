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

package org.dllearner.test;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.datastructures.StringTuple;
import org.dllearner.utilities.statistics.SimpleClock;

public class FilterTest {

	
	private static SPARQLTasks st;
	private static String subject = "http://dbpedia.org/resource/%22Big%22_Ron";
	static int  howmany = 150;
	static SimpleClock sc = new SimpleClock();
	
	static String qlong="SELECT * WHERE {  <http://dbpedia.org/resource/%22Big%22_Ron> ?predicate ?object.  FILTER(  (!isLiteral(?object))&&( ( !regex(str(?predicate), 'http://dbpedia.org/property/relatedInstance') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/website') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/owner') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) &&( !regex(str(?predicate), 'http://www.w3.org/2002/07/owl#sameAs') ) &&( !regex(str(?predicate), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/standard') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/wikipage') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/reference') ) &&( !regex(str(?predicate), 'http://www.w3.org/2004/02/skos/core') ))&&( ( !regex(str(?object), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia') ) &&( !regex(str(?object), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) &&( !regex(str(?object), 'http://dbpedia.org/resource/Template') ) &&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia/commons') ) &&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) &&( !regex(str(?object), 'http://dbpedia.org/resource/Category:') ) &&( !regex(str(?object), 'http://www.w3.org/2004/02/skos/core') ) &&( !regex(str(?object), 'http://www.geonames.org') ))).}";
	/*static String qextralong="SELECT * WHERE {  <http://dbpedia.org/resource/%22Big%22_Ron> ?predicate ?object.  FILTER(  (!isLiteral(?object))&&( ( !regex(str(?predicate), 'http://dbpedia.org/property/relatedInstance') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/website') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/owner') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) &&( !regex(str(?predicate), 'http://www.w3.org/2002/07/owl#sameAs') ) &&( !regex(str(?predicate), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/standard') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/wikipage') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/reference') ) &&( !regex(str(?predicate), 'http://www.w3.org/2004/02/skos/core') ))&&( ( !regex(str(?object), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia') ) &&( !regex(str(?object), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) &&( !regex(str(?object), 'http://dbpedia.org/resource/Template') ) &&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia/commons') ) &&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) &&( !regex(str(?object), 'http://dbpedia.org/resource/Category:') ) &&( !regex(str(?object), 'http://www.w3.org/2004/02/skos/core') ) &&( !regex(str(?object), 'http://www.geonames.org') )))." +
			"OPTIONAL { ?object ?p2 ?o2.  FILTER(  (!isLiteral(?o2))&&( ( !regex(str(?p2), 'http://dbpedia.org/property/relatedInstance') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/website') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/owner') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) &&( !regex(str(?p2), 'http://www.w3.org/2002/07/owl#sameAs') ) &&( !regex(str(?p2), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/standard') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/wikipage') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/reference') ) &&( !regex(str(?p2), 'http://www.w3.org/2004/02/skos/core') ))&&( ( !regex(str(?o2), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?o2), 'http://upload.wikimedia.org/wikipedia') ) &&( !regex(str(?o2), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) &&( !regex(str(?o2), 'http://dbpedia.org/resource/Template') ) &&( !regex(str(?o2), 'http://upload.wikimedia.org/wikipedia/commons') ) &&( !regex(str(?o2), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) &&( !regex(str(?o2), 'http://dbpedia.org/resource/Category:') ) &&( !regex(str(?o2), 'http://www.w3.org/2004/02/skos/core') ) &&( !regex(str(?o2), 'http://www.geonames.org') ))).}}";
	*/
	static String qextralong="SELECT * WHERE { { <http://dbpedia.org/resource/Angela_Merkel> ?predicate ?object.  FILTER(  (!isLiteral(?object))&&( ( !regex(str(?predicate), 'http://dbpedia.org/property/relatedInstance') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/website') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/owner') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) &&( !regex(str(?predicate), 'http://www.w3.org/2002/07/owl#sameAs') ) &&( !regex(str(?predicate), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/standard') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/wikipage') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/reference') ) &&( !regex(str(?predicate), 'http://www.w3.org/2004/02/skos/core') ))&&( ( !regex(str(?object), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia') ) &&( !regex(str(?object), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) &&( !regex(str(?object), 'http://dbpedia.org/resource/Template') ) &&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia/commons') ) &&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) &&( !regex(str(?object), 'http://dbpedia.org/resource/Category:') ) &&( !regex(str(?object), 'http://www.w3.org/2004/02/skos/core') ) &&( !regex(str(?object), 'http://www.geonames.org') ))).}" +
	"OPTIONAL { ?object ?p2 ?o2.  FILTER(  (!isLiteral(?o2))&&( ( !regex(str(?p2), 'http://dbpedia.org/property/relatedInstance') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/website') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/owner') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) &&( !regex(str(?p2), 'http://www.w3.org/2002/07/owl#sameAs') ) &&( !regex(str(?p2), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/standard') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/wikipage') ) &&( !regex(str(?p2), 'http://dbpedia.org/property/reference') ) &&( !regex(str(?p2), 'http://www.w3.org/2004/02/skos/core') ))&&( ( !regex(str(?o2), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?o2), 'http://upload.wikimedia.org/wikipedia') ) &&( !regex(str(?o2), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) &&( !regex(str(?o2), 'http://dbpedia.org/resource/Template') ) &&( !regex(str(?o2), 'http://upload.wikimedia.org/wikipedia/commons') ) &&( !regex(str(?o2), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) &&( !regex(str(?o2), 'http://dbpedia.org/resource/Category:') ) &&( !regex(str(?o2), 'http://www.w3.org/2004/02/skos/core') ) &&( !regex(str(?o2), 'http://www.geonames.org') ))).}}";

	
	static String qshort="SELECT * WHERE {  <http://dbpedia.org/resource/%22Big%22_Ron> ?predicate ?object.  FILTER  (!isLiteral(?object)).}";
	static String sshort="SELECT * WHERE {  <http://dbpedia.org/resource/%22Big%22_Ron> ?predicate ?object }";
	static String qextrashort="SELECT * WHERE {  <http://dbpedia.org/resource/Angela_Merkel> ?predicate ?object.  FILTER  (!isLiteral(?object)). OPTIONAL { ?object ?p2 ?o2  FILTER  (!isLiteral(?o2))}}";
	
	
	/**
	 * @param args
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
	//	System.out.println(qextralong);
	
	//	System.out.println(qextrashort);
		
		 st = new SPARQLTasks( SparqlEndpoint.getEndpointDBpedia());
		 st.queryAsTuple(subject, true);
		 st.query(qlong);
		 st.query(qextralong);
		 st.query(sshort);
		// st.query(qextrashort);
		 System.out.println(qextrashort);
		 System.exit(0);
		// st.query(qextrashort);
		//System.out.println(qextralong);
		
		//String qlong="SELECT * WHERE {  <http://dbpedia.org/resource/%22Big%22_Ron> ?predicate ?object.  FILTER(  (!isLiteral(?object))&&( ( !regex(str(?predicate), 'http://dbpedia.org/property/relatedInstance') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/website') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/owner') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) &&( !regex(str(?predicate), 'http://www.w3.org/2002/07/owl#sameAs') ) &&( !regex(str(?predicate), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/standard') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/wikipage') ) &&( !regex(str(?predicate), 'http://dbpedia.org/property/reference') ) &&( !regex(str(?predicate), 'http://www.w3.org/2004/02/skos/core') ))&&( ( !regex(str(?object), 'http://xmlns.com/foaf/0.1/') ) &&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia') ) &&( !regex(str(?object), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) &&( !regex(str(?object), 'http://dbpedia.org/resource/Template') ) &&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia/commons') ) &&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) &&( !regex(str(?object), 'http://dbpedia.org/resource/Category:') ) &&( !regex(str(?object), 'http://www.w3.org/2004/02/skos/core') ) )).}";
		
		
		//testLong();
		
		//testShortWithFilter();
		//testShort();
		testLong();
		testExtraLong();
		//testExtraShort();
		//testShort();
		//testLong();
		//testShortWithFilter();
		
		
		/*sc.reset();
		for (int i = 0; i < howmany; i++) {
			st.query(sshort);
		}
		sc.printAndSet("supershort ");
		*/
		
		
		
	}
	
	@SuppressWarnings("deprecation")
	static void testShort(){
		SortedSet<StringTuple> tupleset = new TreeSet<StringTuple>();
		sc.reset();
		for (int i = 0; i < howmany; i++) {
			
			tupleset = st.queryAsTuple(subject, true);
			
		}
		SetManipulation.printSet("before", tupleset);
	
		sc.printAndSet("SHORT ");
		
	}
	
	static void  testLong(){
		sc.reset();
		for (int i = 0; i < howmany; i++) {
			st.query(qlong);
		}
		sc.printAndSet("long ");
	}
	
	static void  testExtraLong(){
		sc.reset();
		for (int i = 0; i < howmany; i++) {
			st.query(qextralong);
		}
		sc.printAndSet("extraLong ");
	}
	
	static void  testExtraShort(){
		sc.reset();
		for (int i = 0; i < howmany; i++) {
			st.query(qextrashort);
		}
		sc.printAndSet("qextrashort ");
	}
	
	@SuppressWarnings("deprecation")
	static void  testShortWithFilter(){
		SortedSet<StringTuple> tupleset = new TreeSet<StringTuple>();
		SortedSet<StringTuple> afterfilter= new TreeSet<StringTuple>();
		/*RuleExecutor re = new  RuleExecutor();
		
	
		re.addFilterRule(new SimplePredicateFilterRule( "http://dbpedia.org/property/relatedInstance" ));
		re.addFilterRule(new SimplePredicateFilterRule( "http://dbpedia.org/property/website"));
		re.addFilterRule(new SimplePredicateFilterRule("http://dbpedia.org/property/owner" ));
		re.addFilterRule(new SimplePredicateFilterRule("http://dbpedia.org/property/wikiPageUsesTemplate" ));
		re.addFilterRule(new SimplePredicateFilterRule("http://www.w3.org/2002/07/owl#sameAs" ));
		re.addFilterRule(new SimplePredicateFilterRule("http://xmlns.com/foaf/0.1/" ));
		re.addFilterRule(new SimplePredicateFilterRule("http://dbpedia.org/property/standard"));
		re.addFilterRule(new SimplePredicateFilterRule("http://dbpedia.org/property/wikipage"));
		re.addFilterRule(new SimplePredicateFilterRule("http://dbpedia.org/property/reference"));
		re.addFilterRule(new SimplePredicateFilterRule("http://www.w3.org/2004/02/skos/core"));
		re.addFilterRule(new SimpleObjectFilterRule("http://xmlns.com/foaf/0.1/" ));
		re.addFilterRule(new SimpleObjectFilterRule( "http://upload.wikimedia.org/wikipedia"));
		re.addFilterRule(new SimpleObjectFilterRule( "http://www4.wiwiss.fu-berlin.de/flickrwrappr"));
		re.addFilterRule(new SimpleObjectFilterRule("http://dbpedia.org/resource/Template" ));
		re.addFilterRule(new SimpleObjectFilterRule( "http://upload.wikimedia.org/wikipedia/commons"));
		re.addFilterRule(new SimpleObjectFilterRule("http://www.w3.org/2006/03/wn/wn20/instances/synset" ));
		re.addFilterRule(new SimpleObjectFilterRule("http://dbpedia.org/resource/Category:" ));
		re.addFilterRule(new SimpleObjectFilterRule( "http://www.w3.org/2004/02/skos/core" ));
		re.addFilterRule(new SimpleObjectFilterRule("http://www.geonames.org"));
		
		*/
		
		sc.reset();
		for (int i = 0; i < howmany; i++) {
			
			tupleset = st.queryAsTuple(subject, true);
			//afterfilter = re.filterTuples(subject,tupleset);
		}
		sc.printAndSet("SHORT with filter");
		SetManipulation.printSet("before", tupleset);
		SetManipulation.printSet("after", afterfilter);
	}

}
