package org.dllearner.kb.sparql;

public class SparqlQueryMaker {

	private SparqlQueryType sparqlQueryType;

	public SparqlQueryMaker(SparqlQueryType SparqlQueryType) {
		this.sparqlQueryType = SparqlQueryType;
	}

	public String makeQueryUsingFilters(String subject) {
		String lineend = "\n";

		String Filter = "";
		if (!this.sparqlQueryType.isLiterals())
			Filter += "!isLiteral(?object))";
		for (String p : sparqlQueryType.getPredicatefilterlist()) {
			Filter += lineend + filterPredicate(p);
		}
		for (String o : sparqlQueryType.getObjectfilterlist()) {
			Filter += lineend + filterObject(o);
		}

		String ret = "SELECT * WHERE { " + lineend + "<" + subject + "> ?predicate ?object. "
				+ lineend + "FILTER( " + lineend + "(" + Filter + ").}";
		// System.out.println(ret);
		return ret;
	}

	public String filterObject(String ns) {
		return "&&( !regex(str(?object), '" + ns + "') )";
	}

	public String filterPredicate(String ns) {
		return "&&( !regex(str(?predicate), '" + ns + "') )";
	}
}
