/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.core.probabilistic.unife;

import java.util.Set;
import org.semanticweb.owlapi.model.OWLAxiom;
import javax.annotation.Nonnull;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class OWLProbExplanationReasonerResult extends OWLProbReasonerResult {
    
    private Set<Set<OWLAxiom>> explanations;

    public OWLProbExplanationReasonerResult(@Nonnull OWLAxiom axiom, @Nonnull Double probability, Set<Set<OWLAxiom>> explanations) {
        super(axiom, probability);
        this.explanations = explanations;
    }

    /**
     * @return the explanations
     */
    public Set<Set<OWLAxiom>> getExplanations() {
        return explanations;
    }
    
}
