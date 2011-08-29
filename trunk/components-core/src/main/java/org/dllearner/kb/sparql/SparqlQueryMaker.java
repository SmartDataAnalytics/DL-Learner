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

package org.dllearner.kb.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.utilities.datastructures.StringTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

/**
 * Can assemble sparql queries. can make queries for subject, predicate, object
 * according to the filter settings object SparqlQueryType, which gives the
 * predicate and object lists
 * 
 * @author Sebastian Hellmann
 * 
 */
public class SparqlQueryMaker {

	private static final String MODE_ALLOW = "allow";

	private static final String MODE_FORBID = "forbid";

	private static final String lineend = "\n";

	// allow false is forbid
	private boolean allowMode = false;

	private boolean assembled = false;

	private String filter = "";

	private Set<String> objectFilterList;

	private Set<String> predicateFilterList;
	
	private Set<StringTuple> predicateobjectFilterList;

	private boolean literals = false;

	public void setLiterals(boolean literals) {
		this.literals = literals;
	}

	public SparqlQueryMaker(Set<String> objectFilterList,
			Set<String> predicateFilterList, boolean literals) {
		super();
		this.objectFilterList = objectFilterList;
		this.predicateFilterList = predicateFilterList;
		this.predicateobjectFilterList = new TreeSet<StringTuple>();
		this.literals = literals;
	}

	public SparqlQueryMaker(boolean allowMode,
			Set<String> objectFilterList,
			Set<String> predicateFilterList, boolean literals) {

		this(objectFilterList, predicateFilterList, literals);
		this.allowMode = allowMode;
	}

	public SparqlQueryMaker(String mode, Set<String> objectFilterList,
			Set<String> predicateFilterList, boolean literals) {
		this(objectFilterList, predicateFilterList, literals);
		if (mode.equalsIgnoreCase(MODE_ALLOW)) {
			this.allowMode = true;
		} else if (mode.equalsIgnoreCase(MODE_FORBID)) {
			this.allowMode = false;
		} else {
			this.allowMode = false;
		}
	}

	public String makeSubjectQueryUsingFilters(String subject) {

		// String filter = internalFilterAssemblySubject();
		if (!assembled) {
			filter = internalFilterAssemblySubject("predicate", "object");
			filter = (filter.length() > 0) ? "FILTER( " + lineend + filter
					+ "). " : " ";
			assembled = true;
		}

		String returnString = "SELECT * WHERE { " + lineend + "<" + subject
				+ "> ?predicate ?object. " + lineend + filter + " } ";

		return returnString;
	}
	
	//QUALITY optimize
	public String makeClassQueryUsingFilters(String subject) {

		// String filter = internalFilterAssemblySubject();
		String tmpFilter = internalFilterAssemblySubject("predicate", "object");
			tmpFilter = (tmpFilter.length() > 0) ? "FILTER( " + lineend + tmpFilter
					+ "). " : " ";
		
		String returnString = "SELECT * WHERE {" +lineend + 
			"<" + subject + "> ?predicate ?object;" +
			"a ?object . "+lineend+
			tmpFilter + "}";
		
		//String returnString = "SELECT * WHERE {" +lineend + 
		//	"<" + subject + ">  <"+OWLVocabulary.RDF_TYPE+"> ?object. " +lineend+
		//	tmpFilter + "}";

		return returnString;
	}

	public String makeSubjectQueryLevel(String subject, int level) {
		
		// String filter = internalFilterAssemblySubject();
		// if (!assembled) {
		String filtertmp = "";
		filtertmp = internalFilterAssemblySubject("predicate0", "object0");
		filtertmp = (filtertmp.length() > 0) ? "FILTER( " + filtertmp + "). "
				: " ";

		StringBuffer sbuff = new StringBuffer(1400);
		sbuff.append("SELECT * WHERE { " + lineend + "{<" + subject
				+ "> ?predicate0 ?object0 ." + lineend);
		sbuff.append(filtertmp + "} " + lineend);

		// " + lineend + filter +" } ";
		for (int i = 1; i < level; i++) {
			sbuff.append("OPTIONAL { ");
			sbuff.append("?object" + (i - 1) + " ?predicate" + i + " ?object"
					+ i + " . " + lineend);

			filtertmp = internalFilterAssemblySubject("predicate" + i, "object"
					+ i);
			filtertmp = (filtertmp.length() > 0) ? "FILTER " + filtertmp + ". "
					: " ";

			sbuff.append(filtertmp + " }");
		}

		sbuff.append(lineend + "} ");

		return sbuff.toString();
	}

	private String internalFilterAssemblySubject(String predicateVariable,
			String objectVariable) {
		predicateVariable = (predicateVariable.startsWith("?")) ? predicateVariable
				: "?" + predicateVariable;
		objectVariable = (objectVariable.startsWith("?")) ? objectVariable
				: "?" + objectVariable;

		List<String> terms = new ArrayList<String>();
		String not = (isAllowMode()) ? "" : "!";
		/*new filter type */
		
		for (StringTuple tuple : getPredicateObjectFilterList()) {
			List<String> tmpterms = new ArrayList<String>();
			tmpterms.add(not + "regex(str(" + predicateVariable + "), '" + tuple.a+ "')");
			tmpterms.add(not + "regex(str(" + objectVariable + "), '" + tuple.b+ "')");
			terms.add(assembleTerms(tmpterms, "&&"));
		}
		
		for (String pred : getPredicateFilterList()) {
			terms.add(not + "regex(str(" + predicateVariable + "), '" + pred
					+ "')");
		}
		for (String obj : getObjectFilterList()) {
			terms
					.add(not + "regex(str(" + objectVariable + "), '" + obj
							+ "')");
		}
		String assembled =  assembleTerms(terms, getOperator());
		
		terms = new ArrayList<String>();
		// the next line could be removed as it is included in assemble terms
		if(!assembled.isEmpty()){
			terms.add(assembled);
		}
		if (!isLiterals()) {
			terms.add("!isLiteral(" + objectVariable + ")");
		}
		return assembleTerms(terms, "&&");
		

	}
	
	private String getOperator(){
		return (isAllowMode())?"||":"&&";
		
	}

	
	private String assembleTerms(List<String> terms, String operator) {
		if((!operator.equals("||")) && (!operator.equals("&&"))){
			System.out.println("in SparqlQuerymaker assembleTerms recieved wrong operator");
			System.exit(0);
		}
		
		if (terms.isEmpty())
			return "";
		else if (terms.size() == 1)
			return (terms.get(0).isEmpty())?"": brackets(terms.get(0));
		else {
			StringBuffer sbuf = new StringBuffer(1400);
			String first = terms.remove(0);
			sbuf.append(brackets(first));
			for (String term : terms) {
				sbuf.append(lineend + operator);
				sbuf.append(brackets(term));
			}
			return brackets(sbuf.toString());
		}

	}

	private static String brackets(String s) {
		return "(" + s + ")";
	}

	public boolean isLiterals() {
		return literals;
	}

	public boolean isAllowMode() {
		return allowMode;
	}

	public Set<String> getObjectFilterList() {
		return objectFilterList;
	}

	public Set<String> getPredicateFilterList() {
		return predicateFilterList;
	}
	public Set<StringTuple> getPredicateObjectFilterList() {
		return predicateobjectFilterList;
	}

	public void addPredicateFilter(String newFilter) {
		assembled = false;
		predicateFilterList.add(newFilter);
	}
	
	public void addObjectFilter(String newFilter) {
		assembled = false;
		objectFilterList.add(newFilter);
	}
	public void addPredicateObjectFilter(String pred, String object) {
		assembled = false;
		predicateobjectFilterList.add(new StringTuple(pred, object));
	}
	
	public void combineWith(SparqlQueryMaker sqm){
		predicateFilterList.addAll(sqm.predicateFilterList);
		objectFilterList.addAll(sqm.objectFilterList);
	}

	public static SparqlQueryMaker getSparqlQueryMakerByName(String name) {

		if (name.equalsIgnoreCase("YAGO"))
			return getAllowYAGOFilter();
		else if (name.equalsIgnoreCase("SKOS"))
			return getAllowSKOSFilter();
		else if (name.equalsIgnoreCase("YAGOSKOS"))
			return getAllowYAGOandSKOSFilter();
		else if (name.equalsIgnoreCase("YAGOSPECIALHIERARCHY"))
			return getYagoSpecialHierarchyFilter();
		else if (name.equalsIgnoreCase("YAGOONLY"))
			return getAllowYAGO_ONLYFilter();
		else if (name.equalsIgnoreCase("TEST"))
			return getTestFilter();
		else if (name.equalsIgnoreCase("DBPEDIA-NAVIGATOR"))
			return getDBpediaNavigatorFilter();
		else
			return null;
	}
	
	private void addFiltersForDBpediaSKOS() {
		addPredicateFilter("http://www.w3.org/2004/02/skos/core");
		addObjectFilter("http://www.w3.org/2004/02/skos/core");
		addObjectFilter("http://dbpedia.org/resource/Category:");
		addObjectFilter("http://dbpedia.org/resource/Template");
	}
	
	private void addFiltersForDBpediaUMBEL() {
		addObjectFilter("http://umbel.org/umbel/");
	}
	@SuppressWarnings("unused")
	private void addFiltersForDBpediaOntology() {
		addObjectFilter("http://dbpedia.org/ontology/");
	}
	@SuppressWarnings("unused")
	private void addFiltersForDBpediaCyc() {
		addObjectFilter("http://sw.opencyc.org/2008/06/10/concept/");
	}
	
	private void addFiltersForYago() {
		addObjectFilter("http://dbpedia.org/class/yago");
		
	}
	
	private void addFiltersForOWLSameAs() {
		addPredicateFilter("http://www.w3.org/2002/07/owl#sameAs");
	}
	private void addFiltersForFOAF() {
		addPredicateFilter("http://xmlns.com/foaf/0.1/");
		addObjectFilter("http://xmlns.com/foaf/0.1/");
		
	}
	
	private void addFiltersForWordNet() {
		addObjectFilter("http://www.w3.org/2006/03/wn/wn20/instances/synset");
		
	}
	private void addFiltersForGeonames() {
		addObjectFilter("http://www.geonames.org");
		
	}
	private void addFiltersForFlickrwrappr() {
		addObjectFilter("http://www4.wiwiss.fu-berlin.de/flickrwrappr");
		
	}
	
	private void addFiltersForDBpedia() {
		addPredicateFilter("http://dbpedia.org/property/reference");
		addPredicateFilter("http://dbpedia.org/property/website");
		addPredicateFilter("http://dbpedia.org/property/wikipage");
		addPredicateFilter("http://dbpedia.org/property/wikiPageUsesTemplate");
		addPredicateFilter("http://dbpedia.org/property/relatedInstance");
		addPredicateFilter("http://dbpedia.org/property/owner");
		addPredicateFilter("http://dbpedia.org/property/standard");		
		addObjectFilter("http://upload.wikimedia.org/wikipedia/commons");
		addObjectFilter("http://upload.wikimedia.org/wikipedia");	
	}
	
	public static SparqlQueryMaker getAllowSKOSFilter() {
		SparqlQueryMaker sqm = new SparqlQueryMaker("forbid", new TreeSet<String>(), new TreeSet<String>(), false);
		sqm.combineWith(getAllowYAGOandSKOSFilter());
		sqm.addFiltersForYago();
				
		sqm.addPredicateFilter("http://www.w3.org/2004/02/skos/core#narrower");
		sqm.addObjectFilter("http://dbpedia.org/resource/Template");
		
		return sqm;
	}

	public static SparqlQueryMaker getAllowYAGOFilter() {
		SparqlQueryMaker sqm = new SparqlQueryMaker("forbid", new TreeSet<String>(), new TreeSet<String>(), false);
		sqm.combineWith(getAllowYAGOandSKOSFilter());
		sqm.addFiltersForDBpediaSKOS();
		return sqm;
	}
	
	public static SparqlQueryMaker getAllowYAGO_ONLYFilter() {
		SparqlQueryMaker sqm = new SparqlQueryMaker("forbid", new TreeSet<String>(), new TreeSet<String>(), false);
		sqm.combineWith(getAllowYAGOandSKOSFilter());
		sqm.addFiltersForDBpediaSKOS();
		sqm.addFiltersForDBpediaUMBEL();
		return sqm;
	}

	public static SparqlQueryMaker getDBpediaNavigatorFilter() {
//		SparqlQueryMaker sqm = new SparqlQueryMaker("allow", new TreeSet<String>(), new TreeSet<String>(), false);
		SparqlQueryMaker sqm = new SparqlQueryMaker("allow", new TreeSet<String>(), new TreeSet<String>(), true);
//		sqm.addPredicateFilter("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
//		sqm.addPredicateFilter("http://www.w3.org/2000/01/rdf-schema#subClassOf");
//		sqm.addPredicateFilter("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
//		sqm.addPredicateFilter("http://www.w3.org/2003/01/geo/wgs84_pos#long");
//		sqm.addPredicateFilter("http://www.w3.org/2000/01/rdf-schema#label");
		
		String dbont = "http://dbpedia.org/ontology/";
		sqm.addPredicateFilter(dbont);
		sqm.addPredicateFilter(OWLVocabulary.RDFS_range);
		sqm.addPredicateFilter(OWLVocabulary.RDFS_domain);
		sqm.addPredicateObjectFilter(dbont, dbont);
		sqm.addPredicateObjectFilter(OWLVocabulary.RDF_TYPE, dbont);
		sqm.addPredicateObjectFilter(OWLVocabulary.RDFS_SUBCLASS_OF, dbont);
		sqm.setLiterals(true);
		
		// pred.add("http://dbpedia.org/property/wikipage");
		// pred.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		// pred.add("http://dbpedia.org/property/relatedInstance");
		// pred.add("http://dbpedia.org/property/owner");
		// pred.add("http://dbpedia.org/property/standard");
		return sqm;
	}

	public static SparqlQueryMaker getYagoSpecialHierarchyFilter() {
		SparqlQueryMaker sqm = new SparqlQueryMaker("forbid", new TreeSet<String>(), new TreeSet<String>(), false);
		sqm.combineWith(getAllowYAGOFilter());
		sqm.addPredicateFilter("http://dbpedia.org/property/monarch");
		return sqm;
	}



	public static SparqlQueryMaker getAllowYAGOandSKOSFilter() {
		SparqlQueryMaker sqm = new SparqlQueryMaker("forbid", new TreeSet<String>(), new TreeSet<String>(), false);
		sqm.addFiltersForFOAF();
		sqm.addFiltersForDBpedia();

		sqm.addFiltersForGeonames();
		sqm.addFiltersForWordNet();
		sqm.addFiltersForFlickrwrappr();
		sqm.addFiltersForOWLSameAs();
		
		sqm.addPredicateFilter("http://www.w3.org/2004/02/skos/core#narrower");
		sqm.addObjectFilter("http://dbpedia.org/resource/Template");
		return sqm;
	}

	public static SparqlQueryMaker getTestFilter() {
		SparqlQueryMaker sqm = new SparqlQueryMaker("forbid", new TreeSet<String>(), new TreeSet<String>(), true);
		sqm.combineWith(getAllowYAGOFilter());
		return sqm;
	}

	public static void main(String[] args) {

		String uri = "http://dbpedia.org/resource/Angela_Merkel";
		// System.out.println(getSparqlQueryMakerByName("YAGO").makeSubjectQueryUsingFilters(uri));
		// System.out.println(getSparqlQueryMakerByName("YAGO").makeSubjectQueryUsingFilters(uri).length());
		// System.out.println(getDBpediaNavigatorFilter().makeSubjectQueryUsingFilters(uri));
		System.out.println();
		System.out.println(getSparqlQueryMakerByName("YAGO")
				.makeSubjectQueryLevel(uri, 3));

	}


}
