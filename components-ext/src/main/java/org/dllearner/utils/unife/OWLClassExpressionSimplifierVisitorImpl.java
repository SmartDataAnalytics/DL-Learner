/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.utils.unife;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import javax.annotation.Nonnull;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
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
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class OWLClassExpressionSimplifierVisitorImpl
        implements OWLClassExpressionSimplifierVisitor {

    private Stack<OWLClassExpression> stack = new Stack<>();

    private OWLDataFactory factory;

    public OWLClassExpressionSimplifierVisitorImpl(@Nonnull OWLOntologyManager manager) {
        factory = manager.getOWLDataFactory();
    }

    public OWLClassExpression getOWLClassExpression() {
        return stack.pop();
    }

    public static OWLClassExpression getOWLClassExpression(OWLClassExpression ce, OWLOntologyManager manager) {
        OWLClassExpressionSimplifierVisitor simplifier = new OWLClassExpressionSimplifierVisitorImpl(manager);
        ce.accept(simplifier);
        return simplifier.getOWLClassExpression();
    }

    @Override
    public void visit(OWLClass ce) {
        stack.push(ce);
    }

    @Override
    public void visit(OWLObjectIntersectionOf ce) {
        if (ce.getOperands().size() < 2) {
            if (ce.getOperands().size() <= 0) {
                throw new RuntimeException(ce + " wrong number of operands");
            }
            stack.push(ce.getOperandsAsList().get(0));
        } else {
            Set<OWLClassExpression> operands = new HashSet<>();
            for (OWLClassExpression c : ce.getOperands()) {
                c.accept(this);
                operands.add(stack.pop());
            }
            stack.push(factory.getOWLObjectIntersectionOf(operands));
        }
    }

    @Override
    public void visit(OWLObjectUnionOf ce) {
        if (ce.getOperands().size() < 2) {
            if (ce.getOperands().size() <= 0) {
                throw new RuntimeException(ce + " wrong number of operands");
            }
            stack.push(ce.getOperandsAsList().get(0));
        } else {
            Set<OWLClassExpression> operands = new HashSet<>();
            for (OWLClassExpression c : ce.getOperands()) {
                c.accept(this);
                operands.add(stack.pop());
            }
            stack.push(factory.getOWLObjectUnionOf(operands));
        }
    }

    @Override
    public void visit(OWLObjectComplementOf ce) {
        ce.getOperand().accept(this);
        OWLClassExpression ceNew = stack.pop();
        stack.push(factory.getOWLObjectComplementOf(ceNew));
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom ce) {
        ce.getFiller().accept(this);
        OWLClassExpression ceNew = stack.pop();
        stack.push(factory.getOWLObjectSomeValuesFrom(ce.getProperty(), ceNew));
    }

    @Override
    public void visit(OWLObjectAllValuesFrom ce) {
        ce.getFiller().accept(this);
        OWLClassExpression ceNew = stack.pop();
        stack.push(factory.getOWLObjectAllValuesFrom(ce.getProperty(), ceNew));
    }

    @Override
    public void visit(OWLObjectHasValue ce) {
//        stack.push(factory.getOWLObjectHasValue(ce.getProperty(), ce.getFiller()));
        stack.push(ce);
    }

    @Override
    public void visit(OWLObjectMinCardinality ce) {
        ce.getFiller().accept(this);
        OWLClassExpression cen = stack.pop();
        stack.push(factory.getOWLObjectMinCardinality(ce.getCardinality(), ce.getProperty(), cen));
    }

    @Override
    public void visit(OWLObjectExactCardinality ce) {
        ce.getFiller().accept(this);
        OWLClassExpression cen = stack.pop();
        stack.push(factory.getOWLObjectExactCardinality(ce.getCardinality(), ce.getProperty(), cen));
    }

    @Override
    public void visit(OWLObjectMaxCardinality ce) {
        ce.getFiller().accept(this);
        OWLClassExpression cen = stack.pop();
        stack.push(factory.getOWLObjectMaxCardinality(ce.getCardinality(), ce.getProperty(), cen));
    }

    @Override
    public void visit(OWLObjectHasSelf ce) {
//        stack.push(factory.getOWLObjectHasSelf(ce.getProperty()));
        stack.push(ce);
    }

    @Override
    public void visit(OWLObjectOneOf ce) {
//        stack.push(factory.getOWLObjectOneOf(ce.getIndividuals()));
        stack.push(ce);
    }

    @Override
    public void visit(OWLDataSomeValuesFrom ce) {
//        stack.push(factory.getOWLDataSomeValuesFrom(ce.getProperty(), ce.getFiller()));
        stack.push(ce);
    }

    @Override
    public void visit(OWLDataAllValuesFrom ce) {
        stack.push(ce);
    }

    @Override
    public void visit(OWLDataHasValue ce) {
        stack.push(ce);
    }

    @Override
    public void visit(OWLDataMinCardinality ce) {
        stack.push(ce);
    }

    @Override
    public void visit(OWLDataExactCardinality ce) {
        stack.push(ce);
    }

    @Override
    public void visit(OWLDataMaxCardinality ce) {
        stack.push(ce);
    }
}
