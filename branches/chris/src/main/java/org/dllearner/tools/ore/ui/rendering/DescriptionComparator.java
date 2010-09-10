package org.dllearner.tools.ore.ui.rendering;

import java.util.Comparator;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObject;
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
 * Author: Simon Jupp<br>
 * Date: Jan 11, 2007<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class DescriptionComparator implements OWLClassExpressionVisitor, Comparator<OWLObject> {

    private int last = 0;

    public int compare(OWLObject o, OWLObject o1) {

        int first = 0;
        if(o instanceof OWLClassExpression) {
            ((OWLClassExpression) o).accept(this);
        }
        first = last;
        if (o1 instanceof OWLClassExpression) {
            ((OWLClassExpression) o1).accept(this);
        }


        return signum (first - last);
    }

    public int signum (int diff) {
        if (diff > 0) return 1;
        if (diff <0) return -1;
        else return 0;
    }

    public void visit(OWLClass desc) {
        last = 1;
    }

    public void visit(OWLObjectSomeValuesFrom desc) {
        last = 2;
    }

    public void visit(OWLObjectAllValuesFrom desc) {
        last = 3;
    }

    public void visit(OWLDataSomeValuesFrom desc) {
        last = 4;
    }

    public void visit(OWLDataAllValuesFrom desc) {
        last = 5;
    }

    public void visit(OWLObjectHasValue desc) {
        last = 6;
    }

    public void visit(OWLDataHasValue desc) {
        last = 7;
    }

    public void visit(OWLObjectMinCardinality desc) {
        last = 8;
    }

    public void visit(OWLDataMinCardinality desc) {
        last = 9;
    }

    public void visit(OWLObjectMaxCardinality desc) {
        last = 10;
    }

    public void visit(OWLDataMaxCardinality desc) {
        last = 11;
    }

    public void visit(OWLObjectExactCardinality desc) {
        last = 12;
    }

    public void visit(OWLDataExactCardinality desc) {
        last = 13;
    }

    public void visit(OWLObjectComplementOf desc) {
        last = 14;
    }

    public void visit(OWLObjectUnionOf desc) {
        last = 15;
    }

    public void visit(OWLObjectIntersectionOf desc) {
        last = 16;
    }

    public void visit(OWLObjectHasSelf desc) {
        last = 17;
    }

    public void visit(OWLObjectOneOf desc) {
        last = 18;
    }
}
