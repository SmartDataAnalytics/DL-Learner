package org.dllearner.algorithms.ccel.utils;

import java.io.Serializable;
import java.util.TreeSet;

import org.dllearner.algorithms.celoe.OENode;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class RefinementData implements Serializable {
    private static final long serialVersionUID = 1L;

    private OENode refinedNode;
    private TreeSet<OWLClassExpression> refinements;
    private int originalNodeLength;

    public RefinementData(OENode refinedNode,
            TreeSet<OWLClassExpression> refinements, int originalNodeLength) {
        this.refinedNode = refinedNode;
        this.refinements = refinements;
        this.originalNodeLength = originalNodeLength;
    }

    public OENode getRefinedNode() {
        return refinedNode;
    }

    public TreeSet<OWLClassExpression> getRefinements() {
        return refinements;
    }

    public int getOriginalNodeLength() {
        return originalNodeLength;
    }

    @Override
    public String toString() {
        String res = refinedNode.toString() + " with refinements ";
        int maxRefsToPrint = 20;

        int maxLoops = Math.min(refinements.size(), maxRefsToPrint);
        int loopCntr = 0;
        for (OWLClassExpression refinement : refinements) {
            loopCntr++;
            if (loopCntr >= maxLoops) break;

            res += refinement.toString();
            res += ", ";
        }
        res = res.substring(0, res.length()-2);
        if (refinements.size() > maxRefsToPrint) {
            res += "...";
        }
        return res;
    }
}
