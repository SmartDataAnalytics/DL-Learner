package org.dllearner.reasoning;

import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import org.dllearner.core.ComponentAnn;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

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
				+ Joiner.on(" ").join(
				FluentIterable.from(targetClasses)
						.transform(Functions.compose(TO_IRI_FUNCTION, OWLCLASS_TRANSFORM_FUNCTION)))
				+ "}";
		return query;
	}

	@Override
	public SortedSet<OWLClassExpression> getMeaningfulClasses(OWLClassExpression index, SortedSet<OWLClassExpression> targetClasses) {
		if (targetClasses.isEmpty())
			return new TreeSet<>();
		return super.getMeaningfulClasses(index, targetClasses);
	}
}
