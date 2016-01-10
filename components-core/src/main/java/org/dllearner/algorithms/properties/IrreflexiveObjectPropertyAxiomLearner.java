package org.dllearner.algorithms.properties;

import org.dllearner.core.ComponentAnn;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import com.hp.hpl.jena.query.ParameterizedSparqlString;

@ComponentAnn(name = "irreflexive object property axiom learner", shortName = "oplirrefl", version = 0.1, description="A learning algorithm for irreflexive object property axioms.")
public class IrreflexiveObjectPropertyAxiomLearner extends ObjectPropertyCharacteristicsAxiomLearner<OWLIrreflexiveObjectPropertyAxiom> {

	public IrreflexiveObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		super(ks);
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s WHERE {?s ?p ?o . FILTER NOT EXISTS{?s ?p ?s .}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s WHERE {?s ?p ?s .}");
		
		super.POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(DISTINCT(?s)) AS ?cnt) WHERE {?s ?p ?o . FILTER NOT EXISTS {?s ?p ?s .} }");
		
		COUNT_QUERY = DISTINCT_SUBJECTS_COUNT_QUERY;
		
		axiomType = AxiomType.IRREFLEXIVE_OBJECT_PROPERTY;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	protected OWLIrreflexiveObjectPropertyAxiom getAxiom(OWLObjectProperty property) {
		return df.getOWLIrreflexiveObjectPropertyAxiom(property);
	}
}
