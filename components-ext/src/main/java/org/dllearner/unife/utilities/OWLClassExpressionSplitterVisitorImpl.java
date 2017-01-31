/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.unife.utilities;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class OWLClassExpressionSplitterVisitorImpl implements OWLClassExpressionSplitterVisitor {

    private Stack<OWLClassExpression> stack = new Stack<>();

    @Override
    public Set<OWLClassExpression> getOWLClassExpressions() {
        return new HashSet<OWLClassExpression>(stack);
    }

    public static Set<OWLClassExpression> getOWLClassExpression(OWLClassExpression ce) {
        OWLClassExpressionSplitterVisitor splitter = new OWLClassExpressionSplitterVisitorImpl();
        ce.accept(splitter);
        return splitter.getOWLClassExpressions();
    }

    @Override
    public void visit(OWLObjectUnionOf owlouo) {
        if (owlouo.getOperands().size() <= 0) {
            throw new RuntimeException(owlouo + " wrong number of operands");
        } else {
            for (OWLClassExpression ce : owlouo.getOperands()) {
                ce.accept(this);
            }
        }
    }

    @Override
    public void visit(OWLClass owlc) {
        stack.push(owlc);
    }

    @Override
    public void visit(OWLObjectIntersectionOf owloio) {
        stack.push(owloio);
    }

    @Override
    public void visit(OWLObjectComplementOf owloco) {
        stack.push(owloco);
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom owlsvf) {
        stack.push(owlsvf);
    }

    @Override
    public void visit(OWLObjectAllValuesFrom owlvf) {
        stack.push(owlvf);
    }

    @Override
    public void visit(OWLObjectHasValue owlohv) {
        stack.push(owlohv);
    }

    @Override
    public void visit(OWLObjectMinCardinality owlomc) {
        stack.push(owlomc);
    }

    @Override
    public void visit(OWLObjectExactCardinality owloec) {
        stack.push(owloec);
    }

    @Override
    public void visit(OWLObjectMaxCardinality owlomc) {
        stack.push(owlomc);
    }

    @Override
    public void visit(OWLObjectHasSelf owlohs) {
        stack.push(owlohs);
    }

    @Override
    public void visit(OWLObjectOneOf owlooo) {
        stack.push(owlooo);
    }

    @Override
    public void visit(OWLDataSomeValuesFrom o) {
        stack.push(o);
    }

    @Override
    public void visit(OWLDataAllValuesFrom owldvf) {
        stack.push(owldvf);
    }

    @Override
    public void visit(OWLDataHasValue owldhv) {
        stack.push(owldhv);
    }

    @Override
    public void visit(OWLDataMinCardinality owldmc) {
        stack.push(owldmc);
    }

    @Override
    public void visit(OWLDataExactCardinality owldec) {
        stack.push(owldec);
    }

    @Override
    public void visit(OWLDataMaxCardinality owldmc) {
        stack.push(owldmc);
    }

}
