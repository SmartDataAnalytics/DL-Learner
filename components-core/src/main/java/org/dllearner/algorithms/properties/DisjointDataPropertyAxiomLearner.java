package org.dllearner.algorithms.properties;

import java.util.SortedSet;

import org.dllearner.core.ComponentAnn;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;

import com.hp.hpl.jena.query.ParameterizedSparqlString;

@ComponentAnn(name = "disjoint data properties axiom learner", shortName = "dpldisjoint", version = 0.1, description="A learning algorithm for disjoint data properties axioms.")
public class DisjointDataPropertyAxiomLearner extends DataPropertyHierarchyAxiomLearner<OWLDisjointDataPropertiesAxiom> {
	
	public DisjointDataPropertyAxiomLearner(SparqlEndpointKS ks) {
		super(ks);

		super.posExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o. FILTER NOT EXISTS{?s ?p_dis ?o}}");
		super.negExamplesQueryTemplate = new ParameterizedSparqlString(
				"SELECT DISTINCT ?s ?o WHERE {?s ?p ?o; ?p_dis ?o.}");
		
		axiomType = AxiomType.DISJOINT_DATA_PROPERTIES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLDataProperty> existingDisjointProperties = reasoner.getDisjointProperties(entityToDescribe);
		if (existingDisjointProperties != null && !existingDisjointProperties.isEmpty()) {
			for (OWLDataProperty disProp : existingDisjointProperties) {
				existingAxioms.add(df.getOWLDisjointDataPropertiesAxiom(entityToDescribe, disProp));
			}
			logger.info("Existing axioms:" + existingAxioms);
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.DataPropertyHierarchyAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLDataProperty, org.semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public OWLDisjointDataPropertiesAxiom getAxiom(OWLDataProperty property, OWLDataProperty otherProperty) {
		return df.getOWLDisjointDataPropertiesAxiom(property, otherProperty);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.DataPropertyHierarchyAxiomLearner#computeScore(int, int, int)
	 */
	@Override
	public double computeScore(int candidatePopularity, int popularity, int overlap) {
		return 1 - super.computeScore(candidatePopularity, popularity, overlap);
	}
}
