package org.dllearner.refinementoperators.spatial;

import com.google.common.collect.Sets;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.OWLObjectUnionOfImplExt;
import org.dllearner.reasoning.spatial.SpatialReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.vocabulary.spatial.SpatialVocabulary;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.*;

import java.util.*;

public class SpatialRhoDRDown extends RhoDRDown {
    private SpatialReasoner reasoner;
    private OWLClassExpressionLengthMetric lengthMetric =
            OWLClassExpressionLengthMetric.getDefaultMetric();

    // <getter/setter>
    public void setReasoner(SpatialReasoner reasoner) {
        // The spatial reasoner needs to be set here, since RhoDRDown won't be
        // able to get the domains/ranges of the virtual spatial properties
        // otherwise.
        super.setReasoner((AbstractReasonerComponent) reasoner);

        this.reasoner = reasoner;
    }
    // </getter/setter>

    // <interface methods>
    @Override
    public Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength) {
        Set<OWLClassExpression> refinements = super.refine(description, maxLength);
        refinements.addAll(spatiallyRefine(description, maxLength));

        return refinements;
    }

    @Override
    public void init() throws ComponentInitException {
        super.init();
    }
    // </interface methods>

    private Set<OWLClassExpression> spatiallyRefine(OWLClassExpression ce, int maxLength) {
        Set<OWLClassExpression> refinements = new HashSet<>();
        if (ce instanceof OWLClass)
            refinements.addAll(spatiallyRefineOWLClass((OWLClass) ce));

        else if (ce instanceof OWLObjectIntersectionOf)
            refinements.addAll(
                    spatiallyRefineOWLObjectIntersectionOf((OWLObjectIntersectionOf) ce, maxLength));

        else if (ce instanceof OWLObjectSomeValuesFrom)
            refinements.addAll(
                    spatiallyRefineOWLObjectSomeValuesFrom((OWLObjectSomeValuesFrom) ce, maxLength));

        else if (ce instanceof OWLObjectMinCardinality)
            refinements.addAll(
                    spatiallyRefineOWLObjectMinCardinality((OWLObjectMinCardinality) ce, maxLength));

        else if (ce instanceof OWLObjectUnionOfImplExt)
            refinements.addAll(
                    spatiallyRefineOWLObjectUnionOf(
                            ((OWLObjectUnionOfImplExt) ce).getOperands(), maxLength));
        else if (ce instanceof OWLObjectAllValuesFrom)
            refinements.addAll(
                    spatiallyRefineOWLObjectAllValuesFrom((OWLObjectAllValuesFrom) ce, maxLength));
        else if (ce instanceof OWLObjectMaxCardinality)
            refinements.addAll(
                    spatiallyRefineOWLObjectMaxCardinality((OWLObjectMaxCardinality) ce, maxLength));
        else
            throw new RuntimeException(
                    "Class expression type " + ce.getClass() + " not covered");

        return refinements;
    }

    private Set<OWLClassExpression> spatiallyRefineOWLClass(OWLClass cls) {
        Set<OWLClassExpression> refinements = new HashSet<>();
        if (reasoner.isSuperClassOf(SpatialVocabulary.SpatialFeature, cls)) {
            // TODO: isConnectedWith
            // TODO: overlapsWith
            // TODO: isPartOf
            // TODO: hasPart
            // TODO: isProperPartOf
            // TODO: hasProperPart
            // TODO: partiallyOverlaps
            // TODO: isTangentialProperPartOf
            // TODO: isNonTangentialProperPartOf
            // TODO: isSpatiallyIdenticalWith
            // TODO: hasTangentialProperPart
            // TODO: hasNonTangentialProperPart
            // TODO: isExternallyConnectedWith
            // TODO: isDisconnectedFrom
            // TODO: isNear
            // TODO: startsNear
            // TODO: endsNear
            // TODO: crosses
            // TODO: runsAlong
        }

        return refinements;
    }

    private Set<OWLClassExpression> spatiallyRefineOWLObjectIntersectionOf(
            OWLObjectIntersectionOf intersection, int maxLength) {

        Set<OWLClassExpression> refinements = new HashSet<>();
        if (reasoner.isSuperClassOf(SpatialVocabulary.SpatialFeature, intersection)) {
            // TODO: isConnectedWith
            // TODO: overlapsWith
            // TODO: isPartOf
            // TODO: hasPart
            // TODO: isProperPartOf
            // TODO: hasProperPart
            // TODO: partiallyOverlaps
            // TODO: isTangentialProperPartOf
            // TODO: isNonTangentialProperPartOf
            // TODO: isSpatiallyIdenticalWith
            // TODO: hasTangentialProperPart
            // TODO: hasNonTangentialProperPart
            // TODO: isExternallyConnectedWith
            // TODO: isDisconnectedFrom
            // TODO: isNear
            // TODO: startsNear
            // TODO: endsNear
            // TODO: crosses
            // TODO: runsAlong
        }

        List<OWLClassExpression> operandsList = intersection.getOperandsAsList();
        assert operandsList.size() == 2;

        OWLClassExpression firstOperand = operandsList.get(0);
        OWLClassExpression secondOperand = operandsList.get(1);

        int tmpMaxLength = maxLength - OWLClassExpressionUtils.getLength(secondOperand);
        for (OWLClassExpression refinement1 : refine(firstOperand, tmpMaxLength)) {
            refinements.add(new OWLObjectIntersectionOfImpl(Sets.newHashSet(
                    refinement1, secondOperand)));
        }

        tmpMaxLength = maxLength - OWLClassExpressionUtils.getLength(firstOperand);
        for (OWLClassExpression refinement2 : refine(secondOperand, tmpMaxLength)) {
            refinements.add(new OWLObjectIntersectionOfImpl(Sets.newHashSet(
                    firstOperand, refinement2)));
        }

        return refinements;
    }

    private Set<OWLClassExpression> spatiallyRefineOWLObjectSomeValuesFrom(
            OWLObjectSomeValuesFrom ce, int maxLength) {

        OWLObjectPropertyExpression property = ce.getProperty();
        // FIXME: extend this to also support general object property expressions
        assert !property.isAnonymous();

        // reasoner.getSubProperties( ) should already handle spatial properties
        SortedSet<OWLObjectProperty> properties =
                reasoner.getSubProperties(property.asOWLObjectProperty());
        properties.add(property.asOWLObjectProperty());

        OWLClassExpression filler = ce.getFiller();

        Set<OWLClassExpression> refinements = new HashSet<>();

        // FIXME: replace lengthMetric.objectProperyLength with actual prop length
        Set<OWLClassExpression> fillerRefinements = refine(
                filler,
                maxLength-lengthMetric.objectProperyLength);

        for (OWLObjectProperty p : properties) {
            for (OWLClassExpression fillerRefinement : fillerRefinements) {
                refinements.add(new OWLObjectSomeValuesFromImpl(p, fillerRefinement));
            }
        }

        return refinements;
    }

    private Set<OWLClassExpression> spatiallyRefineOWLObjectMinCardinality(
            OWLObjectMinCardinality ce, int maxLength) {

        OWLObjectPropertyExpression property = ce.getProperty();

        // FIXME: extend this to also support general object property expressions
        assert !property.isAnonymous();

        SortedSet<OWLObjectProperty> properties =
                reasoner.getSubProperties(property.asOWLObjectProperty());
        properties.add(property.asOWLObjectProperty());

        OWLClassExpression filler = ce.getFiller();
        int minCardinality = ce.getCardinality();

        // FIXME: replace lengthMetric.objectProperyLength with actual prop length
        Set<OWLClassExpression> fillerRefinements =
                refine(filler,
                        maxLength-lengthMetric.objectCardinalityLength-lengthMetric.objectProperyLength);
        int refinedCardinality = minCardinality + 1;

        Set<OWLClassExpression> refinements = new HashSet<>();
        refinements.add(new OWLObjectMinCardinalityImpl(
                property, refinedCardinality, filler));

        for (OWLObjectProperty p : properties) {
            for (OWLClassExpression fillerRefinement : fillerRefinements) {
                // First take the original cardinality...
                refinements.add(new OWLObjectMinCardinalityImpl(
                        p, minCardinality, fillerRefinement));
                // ...then the refined cardinality
                refinements.add(new OWLObjectMinCardinalityImpl(
                        p, refinedCardinality, fillerRefinement));
            }
        }

        return refinements;
    }

    /**
     * I'm using the operands set instead of the union class expression object
     * here since we have two different union types: {@link OWLObjectUnionOfImplExt}
     * and {@link OWLObjectUnionOf}.
     */
    private Set<OWLClassExpression> spatiallyRefineOWLObjectUnionOf(
            Set<OWLClassExpression> unionOperands, int maxLength) {

        Set<OWLClassExpression> refinements = new HashSet<>();

        if (reasoner.isSuperClassOf(
                SpatialVocabulary.SpatialFeature, new OWLObjectUnionOfImpl(unionOperands))) {

            // TODO: isConnectedWith
            // TODO: overlapsWith
            // TODO: isPartOf
            // TODO: hasPart
            // TODO: isProperPartOf
            // TODO: hasProperPart
            // TODO: partiallyOverlaps
            // TODO: isTangentialProperPartOf
            // TODO: isNonTangentialProperPartOf
            // TODO: isSpatiallyIdenticalWith
            // TODO: hasTangentialProperPart
            // TODO: hasNonTangentialProperPart
            // TODO: isExternallyConnectedWith
            // TODO: isDisconnectedFrom
            // TODO: isNear
            // TODO: startsNear
            // TODO: endsNear
            // TODO: crosses
            // TODO: runsAlong
        }

        List<OWLClassExpression> unionOperandsList = new ArrayList<>(unionOperands);
        assert unionOperandsList.size() == 2;

        OWLClassExpression firstOperand = unionOperandsList.get(0);
        OWLClassExpression secondOperand = unionOperandsList.get(1);

        int tmpMaxLength = maxLength - OWLClassExpressionUtils.getLength(secondOperand);
        for (OWLClassExpression refinement1 : refine(firstOperand, tmpMaxLength)) {
            refinements.add(new OWLObjectIntersectionOfImpl(Sets.newHashSet(
                    refinement1, secondOperand)));
        }

        tmpMaxLength = maxLength - OWLClassExpressionUtils.getLength(firstOperand);
        for (OWLClassExpression refinement2 : refine(secondOperand, tmpMaxLength)) {
            refinements.add(new OWLObjectIntersectionOfImpl(Sets.newHashSet(
                    firstOperand, refinement2)));
        }

        return refinements;
    }

    private Set<OWLClassExpression> spatiallyRefineOWLObjectAllValuesFrom(
            OWLObjectAllValuesFrom ce, int maxLength) {

        OWLObjectPropertyExpression property = ce.getProperty();
        // FIXME: extend this to also support general object property expressions
        assert !property.isAnonymous();

        Set<OWLObjectProperty> properties =
                reasoner.getSubProperties(property.asOWLObjectProperty());
        properties.add(property.asOWLObjectProperty());

        OWLClassExpression filler = ce.getFiller();

        Set<OWLClassExpression> refinements = new HashSet<>();

        // FIXME: replace lengthMetric.objectProperyLength with actual prop length
        Set<OWLClassExpression> fillerRefinements = refine(
                filler,
                maxLength-lengthMetric.objectProperyLength);

        for (OWLObjectProperty p : properties) {
            for (OWLClassExpression fillerRefinement : fillerRefinements) {
                refinements.add(new OWLObjectAllValuesFromImpl(p, fillerRefinement));
            }
        }

        return refinements;
    }

    private Set<OWLClassExpression> spatiallyRefineOWLObjectMaxCardinality(
            OWLObjectMaxCardinality ce, int maxLength) {

        OWLObjectPropertyExpression property = ce.getProperty();
        // FIXME: extend this to also support general object property expressions
        assert !property.isAnonymous();

        Set<OWLObjectProperty> properties =
                reasoner.getSubProperties(property.asOWLObjectProperty());
        properties.add(property.asOWLObjectProperty());

        OWLClassExpression filler = ce.getFiller();
        int maxCardinality = ce.getCardinality();
        int refinedMaxCardinality = Math.max(0, maxCardinality - 1);

        Set<OWLClassExpression> refinements = new HashSet<>();
        refinements.add(
                new OWLObjectMaxCardinalityImpl(property, refinedMaxCardinality, filler));

        // FIXME: replace lengthMetric.objectProperyLength with actual prop length
        Set<OWLClassExpression> fillerRefinements = refine(
                filler,
                maxLength-lengthMetric.objectProperyLength);

        for (OWLObjectProperty p : properties) {
            for (OWLClassExpression fillerRefinement : fillerRefinements) {
                refinements.add(
                        new OWLObjectMaxCardinalityImpl(p, maxCardinality, fillerRefinement));
                refinements.add(
                        new OWLObjectMaxCardinalityImpl(p, refinedMaxCardinality, fillerRefinement));
            }
        }

        return refinements;
    }
}
