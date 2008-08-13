package org.dllearner.kb.extraction;

/**
 * this class collects old source code and will be removed after refactoring 
 * @author Sebastian Hellmann
 *
 */
public class Scrap {
	
	/*
	public String makeRoleQueryUsingFilters(String role) {

		String Filter = internalFilterAssemblyRole();
		String ret = "SELECT * WHERE { " + lineend + " ?subject <" + role
				+ "> ?object. " + lineend + "FILTER( " + lineend + "(" + Filter
				+ ").}";
		// System.out.println(ret);

		return ret;
	}
	*/
	
	/*
	public String makeRoleQueryUsingFilters(String role, boolean domain) {

		String Filter = internalFilterAssemblyRole();
		String ret = "";
		if (domain) {
			ret = "SELECT * WHERE { " + lineend + "?subject <" + role
					+ "> ?object; a []. " + lineend + "FILTER( " + lineend
					+ "(" + Filter + ").}";
			// "ORDER BY ?subject";
			// System.out.println(ret);
		} else {
			ret = "SELECT * WHERE { " + lineend + "?object a [] . "
					+ "?subject <" + role + "> ?object . " + lineend
					+ "FILTER( " + lineend + "(" + Filter + ").}";
			// "ORDER BY ?object";

		}
		// System.out.println(ret);

		return ret;
	}*/
	
	
	/*
	 * 
	 private String internalFilterAssemblyRole() {

		String Filter = "";
		if (!this.sparqlQueryType.isLiterals())
			Filter += "!isLiteral(?object))";
		for (String s : sparqlQueryType.getObjectfilterlist()) {
			Filter += lineend + filterSubject(s);
		}
		for (String o : sparqlQueryType.getObjectfilterlist()) {
			Filter += lineend + filterObject(o);
		}
		return Filter;
	}*/
	
	
	/*
	 * creates a query with the specified filters for all triples with subject
	 * 
	 * @param subject
	 *            the searched subject
	 * @param sf
	 *            special object encapsulating all options
	 * @return sparql query
	 
	/*
	 * public static String makeQueryFilter(String subject, oldSparqlFilter sf) {
	 * 
	 * String Filter = ""; if (!sf.useLiterals) Filter += "!isLiteral(?object)";
	 * for (String p : sf.getPredFilter()) { Filter += "\n" +
	 * filterPredicate(p); } for (String o : sf.getObjFilter()) { Filter += "\n" +
	 * filterObject(o); }
	 * 
	 * String ret = "SELECT * WHERE { \n" + "<" + subject + "> ?predicate
	 * ?object.\n"; if (!(Filter.length() == 0)) ret += "FILTER( \n" + "(" +
	 * Filter + "))."; ret += "}"; // System.out.println(ret); return ret; }
	 */

	/*
	 * moved to SparqlQuery  remove here creates a query for subjects with
	 * the specified label @param label a phrase that is part of the label of a
	 * subject @param limit this limits the amount of results @return
	 * 
	 * @Deprecated public static String makeLabelQuery(String label,int limit){
	 *             // maybe use http://xmlns:com/foaf/0.1/page return
	 *             "SELECT DISTINCT ?subject\n"+ "WHERE { ?subject
	 *             <http://www.w3.org/2000/01/rdf-schema#label> ?object.?object
	 *             bif:contains '\""+label+"\"'@en}\n"+ "LIMIT "+limit; }
	 * 
	 * 
	 * creates a query for all subjects that are of the type concept @param
	 * concept the type that subjects are searched for @return
	 * 
	 * 
	 * moved to SparqlQuery  remove here
	 * @Deprecated public static String makeConceptQuery(String concept){ return
	 *             "SELECT DISTINCT ?subject\n"+ "WHERE { ?subject a
	 *             <"+concept+">}\n"; } moved to SparqlQuery  remove here
	 * @Deprecated public static String makeArticleQuery(String subject){ return
	 *             "SELECT ?predicate,?object\n"+ "WHERE { <"+subject+">
	 *             ?predicate ?object}\n"; }
	 */


}
