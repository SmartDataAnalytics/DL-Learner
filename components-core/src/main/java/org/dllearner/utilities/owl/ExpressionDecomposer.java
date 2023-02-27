package org.dllearner.utilities.owl;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;

// generates a list of all subexpressions (not proper, i.e. the concept itself is also included)
public class ExpressionDecomposer implements OWLClassExpressionVisitorEx<List<OWLClassExpression>> {

    public List<OWLClassExpression> decompose(OWLClassExpression expression) {
        return expression.accept(this);
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLClass owlClass) {
        return List.of(owlClass);
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLObjectIntersectionOf owlObjectIntersectionOf) {
        List<OWLClassExpression> result = new ArrayList<>();
        result.add(owlObjectIntersectionOf);

        for (OWLClassExpression op : owlObjectIntersectionOf.getOperandsAsList()) {
            result.addAll(op.accept(this));
        }

        return result;
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLObjectUnionOf owlObjectUnionOf) {
        List<OWLClassExpression> result = new ArrayList<>();
        result.add(owlObjectUnionOf);

        for (OWLClassExpression op : owlObjectUnionOf.getOperandsAsList()) {
            result.addAll(op.accept(this));
        }

        return result;
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLObjectComplementOf owlObjectComplementOf) {
        List<OWLClassExpression> result = new ArrayList<>();
        result.add(owlObjectComplementOf);

        result.addAll(owlObjectComplementOf.getOperand().accept(this));

        return result;
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLObjectSomeValuesFrom owlObjectSomeValuesFrom) {
        List<OWLClassExpression> result = new ArrayList<>();
        result.add(owlObjectSomeValuesFrom);

        result.addAll(owlObjectSomeValuesFrom.getFiller().accept(this));

        return result;
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLObjectAllValuesFrom owlObjectAllValuesFrom) {
        List<OWLClassExpression> result = new ArrayList<>();
        result.add(owlObjectAllValuesFrom);

        result.addAll(owlObjectAllValuesFrom.getFiller().accept(this));

        return result;
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLObjectHasValue owlObjectHasValue) {
        return List.of(owlObjectHasValue);
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLObjectMinCardinality owlObjectMinCardinality) {
        List<OWLClassExpression> result = new ArrayList<>();
        result.add(owlObjectMinCardinality);

        result.addAll(owlObjectMinCardinality.getFiller().accept(this));

        return result;
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLObjectExactCardinality owlObjectExactCardinality) {
        List<OWLClassExpression> result = new ArrayList<>();
        result.add(owlObjectExactCardinality);

        result.addAll(owlObjectExactCardinality.getFiller().accept(this));

        return result;
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLObjectMaxCardinality owlObjectMaxCardinality) {
        List<OWLClassExpression> result = new ArrayList<>();
        result.add(owlObjectMaxCardinality);

        result.addAll(owlObjectMaxCardinality.getFiller().accept(this));

        return result;
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLObjectHasSelf owlObjectHasSelf) {
        return List.of(owlObjectHasSelf);
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLObjectOneOf owlObjectOneOf) {
        return List.of(owlObjectOneOf);
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLDataSomeValuesFrom owlDataSomeValuesFrom) {
        return List.of(owlDataSomeValuesFrom);
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLDataAllValuesFrom owlDataAllValuesFrom) {
        return List.of(owlDataAllValuesFrom);
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLDataHasValue owlDataHasValue) {
        return List.of(owlDataHasValue);
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLDataMinCardinality owlDataMinCardinality) {
        return List.of(owlDataMinCardinality);
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLDataExactCardinality owlDataExactCardinality) {
        return List.of(owlDataExactCardinality);
    }

    @NotNull
    @Override
    public List<OWLClassExpression> visit(@NotNull OWLDataMaxCardinality owlDataMaxCardinality) {
        return List.of(owlDataMaxCardinality);
    }
}
