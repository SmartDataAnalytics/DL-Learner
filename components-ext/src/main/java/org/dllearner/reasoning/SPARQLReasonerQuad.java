package org.dllearner.reasoning;

import org.dllearner.core.ComponentAnn;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.Collection;

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
			query = "SELECT (COUNT(DISTINCT ?ind) as ?cnt) WHERE { \n";
		} else {
			query = "SELECT DISTINCT ?ind WHERE { \n";
		}

		query += tp + "\n}"
				+ "\n " + "VALUES (?ind) { \n";

		for (OWLIndividual x:indValues) {
			query += "(<" + x.toStringID() + ">) ";
		}

		query += "\n}";

		return query;
	}
}
