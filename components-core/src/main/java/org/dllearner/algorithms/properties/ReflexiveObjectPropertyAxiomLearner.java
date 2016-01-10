package org.dllearner.algorithms.properties;

import org.dllearner.core.ComponentAnn;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;

import com.hp.hpl.jena.query.ParameterizedSparqlString;

@ComponentAnn(name = "reflexive object property axiom learner", shortName = "oplrefl", version = 0.1, description="A learning algorithm for reflexive object property domain axioms.")
public class ReflexiveObjectPropertyAxiomLearner extends ObjectPropertyCharacteristicsAxiomLearner<OWLReflexiveObjectPropertyAxiom> {

	public ReflexiveObjectPropertyAxiomLearner(SparqlEndpointKS ks) {
		super(ks);
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s WHERE {?s ?p ?s .}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s WHERE {?s ?p ?o . FILTER NOT EXISTS{?s ?p ?s .}");
		
		super.POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(?s) AS ?cnt) WHERE {?s ?p ?s .}");
		
		COUNT_QUERY = DISTINCT_SUBJECTS_COUNT_QUERY;
		
		axiomType = AxiomType.REFLEXIVE_OBJECT_PROPERTY;

	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	protected OWLReflexiveObjectPropertyAxiom getAxiom(OWLObjectProperty property) {
		return df.getOWLReflexiveObjectPropertyAxiom(property);
	}
	
	
}
