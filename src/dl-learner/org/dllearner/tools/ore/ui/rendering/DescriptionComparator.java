package org.dllearner.tools.ore.ui.rendering;

import org.semanticweb.owl.model.*;

import java.util.Comparator;

/**
 * Author: Simon Jupp<br>
 * Date: Jan 11, 2007<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class DescriptionComparator implements OWLDescriptionVisitor, Comparator<OWLObject> {

    private int last = 0;

    public int compare(OWLObject o, OWLObject o1) {

        int first = 0;
        if(o instanceof OWLDescription) {
            ((OWLDescription) o).accept(this);
        }
        first = last;
        if (o1 instanceof OWLDescription) {
            ((OWLDescription) o1).accept(this);
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

    public void visit(OWLObjectSomeRestriction desc) {
        last = 2;
    }

    public void visit(OWLObjectAllRestriction desc) {
        last = 3;
    }

    public void visit(OWLDataSomeRestriction desc) {
        last = 4;
    }

    public void visit(OWLDataAllRestriction desc) {
        last = 5;
    }

    public void visit(OWLObjectValueRestriction desc) {
        last = 6;
    }

    public void visit(OWLDataValueRestriction desc) {
        last = 7;
    }

    public void visit(OWLObjectMinCardinalityRestriction desc) {
        last = 8;
    }

    public void visit(OWLDataMinCardinalityRestriction desc) {
        last = 9;
    }

    public void visit(OWLObjectMaxCardinalityRestriction desc) {
        last = 10;
    }

    public void visit(OWLDataMaxCardinalityRestriction desc) {
        last = 11;
    }

    public void visit(OWLObjectExactCardinalityRestriction desc) {
        last = 12;
    }

    public void visit(OWLDataExactCardinalityRestriction desc) {
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

    public void visit(OWLObjectSelfRestriction desc) {
        last = 17;
    }

    public void visit(OWLObjectOneOf desc) {
        last = 18;
    }
}
