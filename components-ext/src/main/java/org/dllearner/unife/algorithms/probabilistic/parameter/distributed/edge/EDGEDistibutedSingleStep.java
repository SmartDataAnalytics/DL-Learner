/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.unife.algorithms.probabilistic.parameter.distributed.edge;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.unife.core.probabilistic.distributed.AbstractEDGEDistributed;
import org.dllearner.unife.core.probabilistic.ParameterLearningException;
import org.semanticweb.owlapi.model.OWLAxiom;
import unife.math.ApproxDouble;
import unife.edge.EDGEMPISingleStep;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
@ComponentAnn(name = "EDGEDistributedSingleStep", shortName = "edgesingle", version = 1.0)
public class EDGEDistibutedSingleStep extends AbstractEDGEDistributed {
    
    public EDGEDistibutedSingleStep() {
        edge = new EDGEMPISingleStep();
    }

    @Override
    public void init() throws ComponentInitException {
        super.init();
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void start() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApproxDouble getParameter(OWLAxiom ax) throws ParameterLearningException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
