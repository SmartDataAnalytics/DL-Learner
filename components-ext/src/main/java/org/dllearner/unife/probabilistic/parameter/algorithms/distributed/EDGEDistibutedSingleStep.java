/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.unife.probabilistic.parameter.algorithms.distributed;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.unife.probabilistic.distributed.core.AbstractEDGEDistributed;
import unife.edge.EDGEMPISingleStep;

/**
 *
 * @author Giuseppe Cota <giuseta@gmail.com>, Riccardo Zese
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
