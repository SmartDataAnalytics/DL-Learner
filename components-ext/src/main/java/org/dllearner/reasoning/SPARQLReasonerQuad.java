package org.dllearner.reasoning;

import org.apache.jena.query.ResultSet;
import org.dllearner.core.ComponentAnn;
import org.dllearner.kb.SparqlEndpointKS;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Specialised SPARQL Reasoner for specific SPARQL dialects
 */
@ComponentAnn(name = "SPARQL Reasoner (Quad)", shortName = "spr.quad", version = 0.1)
public class SPARQLReasonerQuad extends SPARQLReasoner {
	protected String buildSubsumptionHierarchyQuery() {
		return "SELECT * WHERE {"
				+ " ?sub a <http://www.w3.org/2002/07/owl#Class> . "
				+ " OPTIONAL { "
				// for ontoquad
				+ "{ ?sub <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?sup . FILTER(?sub != ?sup) . } "
				+ " UNION { ?sub <http://www.w3.org/2002/07/owl#equivalentClass> ?sup . FILTER(?sub != ?sup) . } "
				+ " . "
				+ " } "
				+ "}";
	}

	@Override
	protected String buildIndividualsQueryValues(OWLClassExpression description, Collection<OWLIndividual> indValues, boolean isCountQuery) {
		String query;
		String tp = converter.convert("?ind", description);

		// for ontoquad

		if (isCountQuery) {
			query = "SELECT (COUNT(?ind) as ?cnt) WHERE { \n"
			+ "SELECT DISTINCT ?ind WHERE { \n";
		} else {
			query = "SELECT DISTINCT ?ind WHERE { \n";
		}

		query += tp + "\n}";
		if (isCountQuery)
			query += "\n}";
		query += "\n " + "VALUES (?ind) { \n";

		for (OWLIndividual x:indValues) {
			query += "(<" + x.toStringID() + ">) ";
		}

		query += "\n}";

		return query;
	}

	@NotNull
	@Override
	protected String buildMeaningfulClassesQuery(OWLClassExpression index, SortedSet<OWLClassExpression> targetClasses) {
		String query = "SELECT DISTINCT ?concept WHERE {";
		query += converter.convert("?ind", index);
		query += "?ind a ?concept . ";
		query += "}";
		query += "VALUES ?concept {"
				+ targetClasses.stream().map(ce -> "<" + ce.asOWLClass().toStringID() + ">").collect(Collectors.joining(" "))
				+ "}";
		return query;
	}

	@Override
	public SortedSet<OWLClassExpression> getMeaningfulClasses(OWLClassExpression index, SortedSet<OWLClassExpression> targetClasses) {
		if (targetClasses.isEmpty())
			return new TreeSet<>();
		return super.getMeaningfulClasses(index, targetClasses);
	}

	@Override
	protected String buildApplicablePropertiesValuesQuery(OWLClassExpression domain, Collection<? extends OWLObjectProperty> objectProperties) {
		String domQuery = converter.convert("?dom", domain);
		String props = objectProperties.stream().map(op -> "<" + op.toStringID() + ">").collect(Collectors.joining(" "));
//		String prop1 = converter.convert("?p", objectProperties.iterator().next());

		String query = "SELECT DISTINCT ?p WHERE { " +
				"" + domQuery + " ?dom ?p ?o . \n" +
				"" + " }" +
				"" + " VALUES ?p { \n" + props + " }";
		return query;
	}



	private String getFromStatement() {
		return getSources().stream()
				.filter(SparqlEndpointKS.class::isInstance)
				.map(SparqlEndpointKS.class::cast)
				.filter(SparqlEndpointKS::isRemote)
				.map(ks -> ks.getDefaultGraphURIs().stream().map(uri -> "FROM <" + uri + ">").collect(Collectors.joining(" "))
						+ ks.getNamedGraphURIs().stream().map(uri -> "FROM NAMED <" + uri + ">").collect(Collectors.joining(" ")))
				.collect(Collectors.joining(" "));
	}

	@Override
	protected ResultSet executeSelectQuery(String queryString, long timeout, TimeUnit timeoutUnits) {
		String q2 = queryString.replaceFirst(
				"(^|\\s+)SELECT(\\s+.*?\\s+)WHERE(\\s+)",
				"$1" + "SELECT" + "$2" + " " + Matcher.quoteReplacement(getFromStatement()) + " "
				+ "WHERE" + "$3"
		);
		return super.executeSelectQuery(q2, timeout, timeoutUnits);
	}

	@Override
	protected boolean executeAskQuery(String queryString) {
		String q2 = queryString.replaceFirst(
				"(^|\\s+)ASK(\\s+)",
				"$1" + "ASK " + Matcher.quoteReplacement(getFromStatement()) + " $2"
		);
		return super.executeAskQuery(q2);
	}
}
