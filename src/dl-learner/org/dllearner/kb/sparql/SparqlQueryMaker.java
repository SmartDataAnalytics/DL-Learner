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
package org.dllearner.kb.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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

	private SortedSet<String> objectFilterList;

	private SortedSet<String> predicateFilterList;

	private boolean literals = false;

	public SparqlQueryMaker(SortedSet<String> objectFilterList,
			SortedSet<String> predicateFilterList, boolean literals) {
		super();
		this.objectFilterList = objectFilterList;
		this.predicateFilterList = predicateFilterList;
		this.literals = literals;
	}

	public SparqlQueryMaker(boolean allowMode,
			SortedSet<String> objectFilterList,
			SortedSet<String> predicateFilterList, boolean literals) {

		this(objectFilterList, predicateFilterList, literals);
		this.allowMode = allowMode;
	}

	public SparqlQueryMaker(String mode, SortedSet<String> objectFilterList,
			SortedSet<String> predicateFilterList, boolean literals) {
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
	
	public String makeClassQueryUsingFilters(String subject) {

		// String filter = internalFilterAssemblySubject();
		String tmpFilter = internalFilterAssemblySubject("predicate", "object");
			tmpFilter = (tmpFilter.length() > 0) ? "FILTER( " + lineend + tmpFilter
					+ "). " : " ";
		
		String returnString = "SELECT * WHERE {" +lineend + 
			"<" + subject + "> ?predicate ?object;" +
			"a ?object . "+lineend+
			tmpFilter + "}";


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
		if (!isLiterals()) {
			terms.add("!isLiteral(" + objectVariable + ")");
		}
		String not = (isAllowMode()) ? "" : "!";
		for (String pred : getPredicateFilterList()) {
			terms.add(not + "regex(str(" + predicateVariable + "), '" + pred
					+ "')");
		}
		for (String obj : getObjectFilterList()) {
			terms
					.add(not + "regex(str(" + objectVariable + "), '" + obj
							+ "')");
		}

		return assembleTerms(terms);

	}

	private String assembleTerms(List<String> terms) {
		if (terms.isEmpty())
			return "";
		else if (terms.size() == 1)
			return brackets(terms.get(0));
		else {
			StringBuffer sbuf = new StringBuffer(1400);
			String operator = (isAllowMode()) ? "||" : "&&";
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

	public SortedSet<String> getObjectFilterList() {
		return objectFilterList;
	}

	public SortedSet<String> getPredicateFilterList() {
		return predicateFilterList;
	}

	public void addPredicateFilter(String newFilter) {
		assembled = false;
		predicateFilterList.add(newFilter);
	}

	public static SparqlQueryMaker getSparqlQueryMakerByName(String name) {

		if (name.equalsIgnoreCase("YAGO"))
			return getYAGOFilter();
		else if (name.equalsIgnoreCase("SKOS"))
			return getSKOSFilter();
		else if (name.equalsIgnoreCase("YAGOSKOS"))
			return getYAGOSKOS();
		else if (name.equalsIgnoreCase("YAGOSPECIALHIERARCHY"))
			return getYagoSpecialHierarchyFilter();
		else if (name.equalsIgnoreCase("TEST"))
			return test();
		else if (name.equalsIgnoreCase("DBPEDIA-NAVIGATOR"))
			return getDBpediaNavigatorFilter();
		else
			return null;
	}

	public static SparqlQueryMaker getYAGOFilter() {
		SortedSet<String> pred = new TreeSet<String>();
		pred.add("http://www.w3.org/2004/02/skos/core");
		pred.add("http://www.w3.org/2002/07/owl#sameAs");
		pred.add("http://xmlns.com/foaf/0.1/");
		pred.add("http://dbpedia.org/property/reference");
		pred.add("http://dbpedia.org/property/website");
		pred.add("http://dbpedia.org/property/wikipage");
		pred.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		pred.add("http://dbpedia.org/property/relatedInstance");
		pred.add("http://dbpedia.org/property/owner");
		pred.add("http://dbpedia.org/property/standard");

		SortedSet<String> obj = new TreeSet<String>();
		// obj.add("http://dbpedia.org/resource/Category:Wikipedia_");
		// obj.add("http://dbpedia.org/resource/Category:Articles_");
		obj.add("http://dbpedia.org/resource/Category:");
		obj.add("http://dbpedia.org/resource/Template");
		obj.add("http://xmlns.com/foaf/0.1/");
		obj.add("http://upload.wikimedia.org/wikipedia/commons");
		obj.add("http://upload.wikimedia.org/wikipedia");
		obj.add("http://www.geonames.org");
		obj.add("http://www.w3.org/2006/03/wn/wn20/instances/synset");
		obj.add("http://www4.wiwiss.fu-berlin.de/flickrwrappr");
		obj.add("http://www.w3.org/2004/02/skos/core");

		return new SparqlQueryMaker("forbid", obj, pred, false);
	}

	public static SparqlQueryMaker getDBpediaNavigatorFilter() {
		SortedSet<String> pred = new TreeSet<String>();
		pred.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		pred.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		pred.add("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
		pred.add("http://www.w3.org/2003/01/geo/wgs84_pos#long");
		// pred.add("http://dbpedia.org/property/wikipage");
		// pred.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		// pred.add("http://dbpedia.org/property/relatedInstance");
		// pred.add("http://dbpedia.org/property/owner");
		// pred.add("http://dbpedia.org/property/standard");
		return new SparqlQueryMaker("allow", new TreeSet<String>(), pred, true);
	}

	public static SparqlQueryMaker getYagoSpecialHierarchyFilter() {
		SortedSet<String> pred = new TreeSet<String>();
		pred.add("http://www.w3.org/2004/02/skos/core");
		pred.add("http://www.w3.org/2002/07/owl#sameAs");
		pred.add("http://xmlns.com/foaf/0.1/");

		pred.add("http://dbpedia.org/property/reference");
		pred.add("http://dbpedia.org/property/website");
		pred.add("http://dbpedia.org/property/wikipage");
		pred.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		pred.add("http://dbpedia.org/property/relatedInstance");
		pred.add("http://dbpedia.org/property/monarch");

		SortedSet<String> obj = new TreeSet<String>();
		obj.add("http://dbpedia.org/resource/Category:Wikipedia_");
		obj.add("http://dbpedia.org/resource/Category:Articles_");
		obj.add("http://dbpedia.org/resource/Template");
		obj.add("http://xmlns.com/foaf/0.1/");
		obj.add("http://upload.wikimedia.org/wikipedia/commons");
		obj.add("http://upload.wikimedia.org/wikipedia");
		obj.add("http://www.geonames.org");
		obj.add("http://www.w3.org/2006/03/wn/wn20/instances/synset");
		obj.add("http://www4.wiwiss.fu-berlin.de/flickrwrappr");
		obj.add("http://www.w3.org/2004/02/skos/core");

		return new SparqlQueryMaker("forbid", obj, pred, false);
	}

	public static SparqlQueryMaker getSKOSFilter() {
		SortedSet<String> pred = new TreeSet<String>();
		// pred.add("http://www.w3.org/2004/02/skos/core");
		pred.add("http://www.w3.org/2002/07/owl#sameAs");
		pred.add("http://xmlns.com/foaf/0.1/");

		pred.add("http://dbpedia.org/property/reference");
		pred.add("http://dbpedia.org/property/website");
		pred.add("http://dbpedia.org/property/wikipage");
		pred.add("http://www.w3.org/2004/02/skos/core#narrower");
		pred.add("http://dbpedia.org/property/wikiPageUsesTemplate");

		SortedSet<String> obj = new TreeSet<String>();
		// obj.add("http://dbpedia.org/resource/Category:Wikipedia_");
		// obj.add("http://dbpedia.org/resource/Category:Articles_");
		obj.add("http://xmlns.com/foaf/0.1/");
		obj.add("http://upload.wikimedia.org/wikipedia/commons");
		obj.add("http://upload.wikimedia.org/wikipedia");

		obj.add("http://www.geonames.org");
		obj.add("http://www.w3.org/2006/03/wn/wn20/instances/synset");
		obj.add("http://www4.wiwiss.fu-berlin.de/flickrwrappr");

		obj.add("http://dbpedia.org/class/yago");
		obj.add("http://dbpedia.org/resource/Template");

		return new SparqlQueryMaker("forbid", obj, pred, false);
	}

	public static SparqlQueryMaker getYAGOSKOS() {
		SortedSet<String> pred = new TreeSet<String>();
		// pred.add("http://www.w3.org/2004/02/skos/core");
		pred.add("http://www.w3.org/2002/07/owl#sameAs");
		pred.add("http://xmlns.com/foaf/0.1/");

		pred.add("http://dbpedia.org/property/reference");
		pred.add("http://dbpedia.org/property/website");
		pred.add("http://dbpedia.org/property/wikipage");
		// pred.add("http://www.w3.org/2004/02/skos/core#narrower");
		pred.add("http://dbpedia.org/property/wikiPageUsesTemplate");

		SortedSet<String> obj = new TreeSet<String>();
		// obj.add("http://dbpedia.org/resource/Category:Wikipedia_");
		// obj.add("http://dbpedia.org/resource/Category:Articles_");
		obj.add("http://xmlns.com/foaf/0.1/");
		obj.add("http://upload.wikimedia.org/wikipedia/commons");
		obj.add("http://upload.wikimedia.org/wikipedia");

		obj.add("http://www.geonames.org");
		obj.add("http://www.w3.org/2006/03/wn/wn20/instances/synset");
		obj.add("http://www4.wiwiss.fu-berlin.de/flickrwrappr");

		// obj.add("http://dbpedia.org/class/yago");
		obj.add("http://dbpedia.org/resource/Template");

		return new SparqlQueryMaker("forbid", obj, pred, false);
	}

	public static SparqlQueryMaker test() {
		SortedSet<String> pred = new TreeSet<String>();
		pred.add("http://www.w3.org/2004/02/skos/core");
		pred.add("http://www.w3.org/2002/07/owl#sameAs");
		pred.add("http://xmlns.com/foaf/0.1/");
		// pred.add("http://dbpedia.org/property/reference");
		// pred.add("http://dbpedia.org/property/website");
		// pred.add("http://dbpedia.org/property/wikipage");
		pred.add("http://dbpedia.org/property/wikiPageUsesTemplate");
		pred.add("http://dbpedia.org/property/relatedInstance");

		SortedSet<String> obj = new TreeSet<String>();
		// obj.add("http://dbpedia.org/resource/Category:Wikipedia_");
		// obj.add("http://dbpedia.org/resource/Category:Articles_");
		obj.add("http://dbpedia.org/resource/Category:");
		obj.add("http://dbpedia.org/resource/Template");
		obj.add("http://xmlns.com/foaf/0.1/");
		obj.add("http://upload.wikimedia.org/wikipedia/commons");
		obj.add("http://upload.wikimedia.org/wikipedia");
		obj.add("http://www.geonames.org");
		obj.add("http://www.w3.org/2006/03/wn/wn20/instances/synset");
		obj.add("http://www4.wiwiss.fu-berlin.de/flickrwrappr");
		obj.add("http://www.w3.org/2004/02/skos/core");
		return new SparqlQueryMaker("forbid", obj, pred, false);
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

	/*
	 * private String internalFilterAssemblySubject() {
	 * 
	 * boolean emptyPredicateFilter = getPredicateFilterList().isEmpty();
	 * boolean emptyObjectFilter = getObjectFilterList().isEmpty();
	 * 
	 * String filterString = ""; if (!isLiterals()) { filterString +=
	 * "(!isLiteral(?object))"; if (!getPredicateFilterList().isEmpty()) {
	 * filterString += "&&("; }
	 *  } else if (!emptyPredicateFilter) { filterString += "("; } boolean
	 * firstRun = true; for (String p : getPredicateFilterList()) { filterString +=
	 * lineend; filterString += (firstRun) ? handlePredicate(p).substring(2) :
	 * handlePredicate(p); firstRun = false; } if (!emptyPredicateFilter) {
	 * filterString += ")"; } if ((!emptyPredicateFilter || !isLiterals()) &&
	 * !emptyObjectFilter) { filterString += "&&("; }else if
	 * (!emptyObjectFilter) { filterString += "("; }
	 * 
	 * firstRun = true; for (String o : getObjectFilterList()) { filterString +=
	 * lineend; filterString += (firstRun) ? handleObject(o).substring(2) :
	 * handleObject(o) ; firstRun = false; } if (!emptyObjectFilter){
	 * filterString += ")"; }
	 * 
	 * return filterString; }
	 */

	/*
	 * private String filterSubject(String ns) { return "&&(
	 * !regex(str(?subject), '" + ns + "') )"; }
	 * 
	 * 
	 * private String handlePredicate (String ns) { return (isAllowMode()) ?
	 * allowPredicate(ns) : filterPredicate(ns) ; }
	 * 
	 * private String handleObject (String ns) { return (isAllowMode()) ?
	 * allowObject(ns) : filterObject(ns) ; }
	 * 
	 * private static String filterPredicate(String ns) { return "&&(
	 * !regex(str(?predicate), '" + ns + "') )"; }
	 * 
	 * private static String filterObject(String ns) { return "&&(
	 * !regex(str(?object), '" + ns + "') )"; }
	 * 
	 * private static String allowPredicate(String ns) { return "||(
	 * regex(str(?predicate), '" + ns + "') )"; }
	 * 
	 * private static String allowObject(String ns) { return "||(
	 * regex(str(?object), '" + ns + "') )"; }
	 */

}
