package org.dllearner.algorithms.ccel.utils;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.algorithms.distributed.MessageContainer;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class RefinementAndScoreContainer implements MessageContainer {
    private static final long serialVersionUID = 1L;

    private OWLClassExpression refinement;
    private OENode parentNode;
//    private ScoreTwoValued<OWLEntity> score;
    private double accuracy;

    public RefinementAndScoreContainer(OWLClassExpression refinement, OENode parentNode) {
        this.refinement = refinement;
        this.parentNode = parentNode;
    }

//    public void setScore(ScoreTwoValued<OWLEntity> score) {
//        this.score = score;
//    }
//
//    public ScoreTwoValued<OWLEntity> getScore() {
//        return this.score;
//    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getAccuracy() {
        return this.accuracy;
    }

    public OWLClassExpression getRefinement() {
        return this.refinement;
    }

    public OENode getParentNode() {
        return this.parentNode;
    }

    @Override
    public String toString() {
        return refinement.toString();
    }
}
