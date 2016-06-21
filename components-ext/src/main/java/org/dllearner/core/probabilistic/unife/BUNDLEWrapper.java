/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dllearner.core.probabilistic.unife;

import org.dllearner.core.ComponentInitException;
import org.dllearner.reasoning.unife.ProbabilisticReasonerType;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public class BUNDLEWrapper extends AbstractProbabilisticReasonerComponent implements OWLProbabilisticExplanationReasoner {

    @Override
    public ProbabilisticReasonerType getReasonerType() {
        return ProbabilisticReasonerType.BUNDLE;
    }

    @Override
    public void init() throws ComponentInitException {
        super.init();
        initialized = true;
        
    }

    @Override
    public OWLProbExplanationReasonerResult computeQuery(OWLAxiom axiom) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
