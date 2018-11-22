package org.dllearner.refinementoperators.spatial;

import com.google.common.collect.Sets;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.spatial.DBConnectionSetting;
import org.dllearner.reasoning.spatial.SpatialReasoner;
import org.dllearner.reasoning.spatial.SpatialReasonerPostGIS;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.vocabulary.spatial.SpatialVocabulary;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectMinCardinalityImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO: Check handling of max expression length
 */
public class SpatialRhoDRDown extends RhoDRDown {
    private SpatialReasoner reasoner;
    private OWLClassExpressionLengthMetric lengthMetric =
            OWLClassExpressionLengthMetric.getDefaultMetric();

    // <getter/setter>
    public void setReasoner(SpatialReasoner reasoner) {
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

        else
            throw new RuntimeException(
                    "Class expression type " + ce.getClass() + " not covered");

        return refinements;
    }

    private Set<OWLClassExpression> spatiallyRefineOWLObjectMinCardinality(OWLObjectMinCardinality ce, int maxLength) {
        // TODO: Handle spatial sub object properties
        OWLObjectPropertyExpression property = ce.getProperty();
        OWLClassExpression filler = ce.getFiller();
        int minCardinality = ce.getCardinality();

        Set<OWLClassExpression> fillerRefinements =
                refine(filler,
                        maxLength-lengthMetric.objectCardinalityLength-lengthMetric.objectProperyLength);
        int refinedCardinality = minCardinality + 1;

        Set<OWLClassExpression> refinements = new HashSet<>();
        refinements.add(new OWLObjectMinCardinalityImpl(
                property, refinedCardinality, filler));

        for (OWLClassExpression fillerRefinement : fillerRefinements) {
            // First take the original cardinality...
            refinements.add(new OWLObjectMinCardinalityImpl(
                    property, minCardinality, fillerRefinement));
            // ...then the refined cardinality
            refinements.add(new OWLObjectMinCardinalityImpl(
                    property, refinedCardinality, fillerRefinement));
        }

        return refinements;
    }

    private Set<OWLClassExpression> spatiallyRefineOWLObjectSomeValuesFrom(OWLObjectSomeValuesFrom ce, int maxLength) {
        // TODO: handle spatial sub object properties
        OWLObjectPropertyExpression property = ce.getProperty();
        OWLClassExpression filler = ce.getFiller();

        Set<OWLClassExpression> refinements = new HashSet<>();

        Set<OWLClassExpression> fillerRefinements = refine(
                filler,
                maxLength-lengthMetric.objectProperyLength);

        for (OWLClassExpression fillerRefinement : fillerRefinements) {
            refinements.add(new OWLObjectSomeValuesFromImpl(property, fillerRefinement));
        }

        return refinements;
    }

    private Set<OWLClassExpression> spatiallyRefineOWLObjectIntersectionOf(OWLObjectIntersectionOf intersection, int maxLength) {
        Set<OWLClassExpression> refinements = new HashSet<>();
        if (reasoner.isSuperClassOf(SpatialVocabulary.SpatialFeature, intersection)) {
            // isInside
            refinements.add(new OWLObjectIntersectionOfImpl(Sets.newHashSet(
                    intersection,
                    new OWLObjectSomeValuesFromImpl(
                            SpatialVocabulary.isInside, SpatialVocabulary.SpatialFeature))));

            // isNear
            refinements.add(new OWLObjectIntersectionOfImpl(Sets.newHashSet(
                    intersection,
                    new OWLObjectSomeValuesFromImpl(
                            SpatialVocabulary.isNear, SpatialVocabulary.SpatialFeature))));
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

    private Set<OWLClassExpression> spatiallyRefineOWLClass(OWLClass cls) {
        Set<OWLClassExpression> refinements = new HashSet<>();
        if (reasoner.isSuperClassOf(SpatialVocabulary.SpatialFeature, cls)) {
            // isInside
            refinements.add(new OWLObjectIntersectionOfImpl(Sets.newHashSet(
                    cls,
                    new OWLObjectSomeValuesFromImpl(
                            SpatialVocabulary.isInside, SpatialVocabulary.SpatialFeature))));

            // isNear
            refinements.add(new OWLObjectIntersectionOfImpl(Sets.newHashSet(
                    cls,
                    new OWLObjectSomeValuesFromImpl(
                            SpatialVocabulary.isNear, SpatialVocabulary.SpatialFeature))));

        }

        return refinements;
    }

    public static void main(String[] args) throws ComponentInitException {
        String exampleFilePath =
                SpatialRhoDRDown.class.getClassLoader()
                        .getResource("test/example_data.owl").getFile();
        KnowledgeSource ks = new OWLFile(exampleFilePath);
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
        SpatialReasonerPostGIS spatialReasoner = new SpatialReasonerPostGIS(
                cwr, new DBConnectionSetting(
                "localhost",5432, "dllearner",
                "postgres", "postgres"));
        spatialReasoner.init();

        SpatialRhoDRDown refinementOperator = new SpatialRhoDRDown();
        refinementOperator.setReasoner((SpatialReasoner) spatialReasoner);
        refinementOperator.init();
        int maxLength = 3;

        System.out.println("============================================\nRefinements:");
        Set<OWLClassExpression> refinements =
                refinementOperator.refine(SpatialVocabulary.SpatialFeature, maxLength);
        refinements.forEach(System.out::println);

        for (OWLClassExpression ce : refinements) {
            if (OWLClassExpressionUtils.getLength(ce) <= maxLength) {
                Set<OWLClassExpression> refnemnts = refinementOperator.refine(ce, maxLength);
                refnemnts.forEach(System.out::println);
            }
        }
    }
}
