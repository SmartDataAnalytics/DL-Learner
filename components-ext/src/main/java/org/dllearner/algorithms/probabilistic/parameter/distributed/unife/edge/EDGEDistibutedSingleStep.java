/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.algorithms.probabilistic.parameter.distributed.unife.edge;

import java.math.BigDecimal;
import java.util.Map;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.probabilistic.distributed.unife.DistributedComponent;
import org.dllearner.core.probabilistic.unife.ParameterLearningException;
import org.dllearner.core.probabilistic.distributed.unife.AbstractEDGEDistributed;
import org.dllearner.algorithms.probabilistic.parameter.unife.edge.AbstractEDGE;
import org.semanticweb.owlapi.model.OWLAxiom;
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

}
