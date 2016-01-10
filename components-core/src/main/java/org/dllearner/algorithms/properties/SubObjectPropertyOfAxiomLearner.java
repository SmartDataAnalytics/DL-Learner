package org.dllearner.algorithms.properties;

import java.util.SortedSet;

import org.dllearner.core.ComponentAnn;
import org.dllearner.kb.SparqlEndpointKS;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

@ComponentAnn(name = "object subproperty axiom learner", shortName = "oplsubprop", version = 0.1, description="A learning algorithm object subproperty axioms.")
public class SubObjectPropertyOfAxiomLearner extends ObjectPropertyHierarchyAxiomLearner<OWLSubObjectPropertyOfAxiom>{

	private final double BETA = 3.0;

	public SubObjectPropertyOfAxiomLearner(SparqlEndpointKS ks) {
		super(ks);
		
		setBeta(BETA);
		
		axiomType = AxiomType.SUB_OBJECT_PROPERTY;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		SortedSet<OWLObjectProperty> existingSuperProperties = reasoner.getSuperProperties(entityToDescribe);
		if (existingSuperProperties != null && !existingSuperProperties.isEmpty()) {
			for (OWLObjectProperty supProp : existingSuperProperties) {
				existingAxioms.add(df.getOWLSubObjectPropertyOfAxiom(entityToDescribe, supProp));
			}
			logger.info("Existing axioms:" + existingAxioms);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.properties.ObjectPropertyHierarchyAxiomLearner#getAxiom(org.semanticweb.owlapi.model.OWLObjectProperty, org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	public OWLSubObjectPropertyOfAxiom getAxiom(OWLObjectProperty property, OWLObjectProperty otherProperty) {
		return df.getOWLSubObjectPropertyOfAxiom(property, otherProperty);
	}
}
