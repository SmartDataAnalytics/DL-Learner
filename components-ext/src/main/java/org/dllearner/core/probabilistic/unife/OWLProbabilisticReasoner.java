/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.core.probabilistic.unife;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public interface OWLProbabilisticReasoner extends ProbabilisticReasoner {
    
    public OWLProbReasonerResult computeQuery(OWLAxiom axiom)
            throws OWLException;
    
}
