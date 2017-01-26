/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.unife.core.probabilistic;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * 
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class OWLProbReasonerResult implements Comparable {

    private OWLAxiom axiom;
    
    private Double probability;

    public OWLProbReasonerResult(OWLAxiom axiom, Double probability) {
        this.axiom = axiom;
        this.probability = probability;
    }

    /**
     * @return the axiom
     */
    public OWLAxiom getAxiom() {
        return axiom;
    }

    /**
     * @return the probability
     */
    public Double getProbability() {
        return probability;
    }

    @Override
    public int compareTo(Object o) {
        return probability.compareTo(((OWLProbReasonerResult) o).probability);
    }
    
    
    
}
