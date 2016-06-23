/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.core.probabilistic.unife;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;

/**
 * This marker interface for those probabilistic reasoner that returns the
 * set of explanations as well.
 * 
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public interface OWLProbabilisticExplanationReasoner extends OWLProbabilisticReasoner {
    
    public OWLProbExplanationReasonerResult computeQueryWithExplanations(OWLAxiom axiom) 
            throws OWLException;
            
}
