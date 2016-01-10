package org.dllearner.algorithms.properties;

import java.util.SortedSet;

import org.dllearner.core.ComponentAnn;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;

@ComponentAnn(name="data subproperty axiom learner", shortName="dplsubprop", version=0.1, description="A learning algorithm data subproperty axioms.")
public class SubDataPropertyOfAxiomLearner extends DataPropertyHierarchyAxiomLearner<OWLSubDataPropertyOfAxiom> {
	
	private final double BETA = 3.0;
	
	public SubDataPropertyOfAxiomLearner(SparqlEndpointKS ks){
		super(ks);
		
		super.beta = BETA;
		
		axiomType = AxiomType.SUB_DATA_PROPERTY;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLDataProperty> existingSuperProperties = reasoner.getSuperProperties(entityToDescribe);
		if (existingSuperProperties != null && !existingSuperProperties.isEmpty()) {
			for (OWLDataProperty supProp : existingSuperProperties) {
				existingAxioms.add(df.getOWLSubDataPropertyOfAxiom(entityToDescribe, supProp));
			}
			logger.info("Existing axioms:" + existingAxioms);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.DataPropertyHierarchyAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLDataProperty, org.semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public OWLSubDataPropertyOfAxiom getAxiom(OWLDataProperty property, OWLDataProperty otherProperty) {
		return df.getOWLSubDataPropertyOfAxiom(property, otherProperty);
	}
}
