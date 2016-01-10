package org.dllearner.algorithms.properties;

import org.dllearner.core.ComponentAnn;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;

import com.hp.hpl.jena.query.ParameterizedSparqlString;

@ComponentAnn(name="symmetric object property axiom learner", shortName="oplsymm", version=0.1, description="A learning algorithm for symmetric object property axioms.")
public class SymmetricObjectPropertyAxiomLearner extends ObjectPropertyCharacteristicsAxiomLearner<OWLSymmetricObjectPropertyAxiom> {
	
	public SymmetricObjectPropertyAxiomLearner(SparqlEndpointKS ks){
		super(ks);
		
		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o WHERE {?s ?p ?o . ?o ?p ?s .}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT ?s ?o WHERE {?s ?p ?o . FILTER NOT EXISTS{?o ?p ?s .}");
		
		super.POS_FREQUENCY_QUERY = new ParameterizedSparqlString(
					"SELECT (COUNT(*) AS ?cnt) WHERE {?s ?p ?o . ?o ?p ?s .}");
		
		axiomType = AxiomType.SYMMETRIC_OBJECT_PROPERTY;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyCharacteristicsAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	protected OWLSymmetricObjectPropertyAxiom getAxiom(OWLObjectProperty property) {
		return df.getOWLSymmetricObjectPropertyAxiom(property);
	}
}
