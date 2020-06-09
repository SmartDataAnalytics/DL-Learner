package org.dllearner.algorithms.spatial;

import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.OWLObjectUnionOfImplExt;
import org.dllearner.vocabulary.spatial.SpatialVocabulary;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;

public class SpatialOEHeuristicRuntime extends OEHeuristicRuntime {
    @ConfigOption(description = "penalty if a node description does not " +
            "contain spatial relations", defaultValue = "0.2")
    private double nonSpatialCEPenalty = 0.2;

    @Override
    public void init() throws ComponentInitException {
        super.init();
        setExpansionPenaltyFactor(0.05);
        setStartNodeBonus(0.0);
    }

    @Override
    public double getNodeScore(OENode node) {
        if (!containsSpatialExpressions(node.getDescription()))
            return super.getNodeScore(node) - nonSpatialCEPenalty;
        else
            return super.getNodeScore(node);
    }

    private boolean containsSpatialExpressions(OWLClassExpression ce) {
        if (ce instanceof OWLClass) {
            return false;

        } else if (ce instanceof OWLObjectIntersectionOf) {
            return ((OWLObjectIntersectionOf) ce).getOperands().stream()
                    .anyMatch(this::containsSpatialExpressions);

        } else if (ce instanceof OWLObjectSomeValuesFrom) {
            OWLObjectPropertyExpression prop = ((OWLObjectSomeValuesFrom) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectSomeValuesFrom) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else if (ce instanceof OWLObjectMinCardinality) {
            OWLObjectPropertyExpression prop = ((OWLObjectMinCardinality) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectMinCardinality) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else if (ce instanceof OWLObjectUnionOfImpl) {
            return ((OWLObjectUnionOf) ce).getOperands().stream()
                    .anyMatch(this::containsSpatialExpressions);

        } else if (ce instanceof OWLObjectUnionOfImplExt) {
            return ((OWLObjectUnionOfImplExt) ce).getOperands().stream()
                    .anyMatch(this::containsSpatialExpressions);

        } else if (ce instanceof OWLObjectAllValuesFrom) {
            OWLObjectPropertyExpression prop = ((OWLObjectAllValuesFrom) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectAllValuesFrom) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else if (ce instanceof OWLObjectMaxCardinality) {
            OWLObjectPropertyExpression prop =
                    ((OWLObjectMaxCardinality) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectMaxCardinality) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else {
            return false;
        }
    }
}
