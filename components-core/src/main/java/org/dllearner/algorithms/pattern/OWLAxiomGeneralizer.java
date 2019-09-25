package org.dllearner.algorithms.pattern;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorExAdapter;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Lorenz Buehmann
 */
public class OWLAxiomGeneralizer extends OWLAxiomVisitorExAdapter<Set<OWLAxiom>> {

    private OWLDataFactory df = new OWLDataFactoryImpl();

    OWLClassExpressionGeneralizer ceGeneralizer = new OWLClassExpressionGeneralizer(df);

    public OWLAxiomGeneralizer() {
        super(Collections.EMPTY_SET);
    }

    public Set<OWLAxiom> generalize(OWLAxiom ax) {
        return ax.accept(this);
    }

    @Nonnull
    @Override
    protected Set<OWLAxiom> doDefault(@Nonnull OWLAxiom axiom) {
        return Collections.EMPTY_SET;
//        return Collections.singleton(axiom);
    }

    @Override
    public Set<OWLAxiom> visit(OWLSubClassOfAxiom axiom) {
        OWLClassExpression sub = axiom.getSubClass();
        OWLClassExpression sup = axiom.getSuperClass();

        Set<OWLClassExpression> supGens = ceGeneralizer.generalize(sup);

        return supGens.stream()
                .map(supGen -> df.getOWLSubClassOfAxiom(sub, supGen))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Set<OWLAxiom> visit(OWLObjectPropertyDomainAxiom axiom) {
        return ceGeneralizer.generalize(axiom.getDomain()).stream()
                .map(dom -> df.getOWLObjectPropertyDomainAxiom(axiom.getProperty(), dom))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Set<OWLAxiom> visit(OWLObjectPropertyRangeAxiom axiom) {
        return ceGeneralizer.generalize(axiom.getRange()).stream()
                .map(dom -> df.getOWLObjectPropertyRangeAxiom(axiom.getProperty(), dom))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Set<OWLAxiom> visit(OWLDataPropertyDomainAxiom axiom) {
        return ceGeneralizer.generalize(axiom.getDomain()).stream()
                .map(dom -> df.getOWLDataPropertyDomainAxiom(axiom.getProperty(), dom))
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
