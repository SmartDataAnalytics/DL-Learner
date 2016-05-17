/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.core.probabilistic.distributed.unife;

import java.math.BigDecimal;
import mpi.Intracomm;
import mpi.MPI;
import org.dllearner.core.ComponentInitException;
import org.dllearner.algorithms.probabilistic.parameter.unife.edge.AbstractEDGE;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public abstract class AbstractEDGEDistributed extends AbstractEDGE
        implements DistributedComponent {

    // default value MPI.COMM_WORLD
    protected Intracomm comm = MPI.COMM_WORLD;

    /**
     * @return the comm
     */
    public Intracomm getComm() {
        return comm;
    }

    /**
     * @param comm the comm to set
     */
    public void setComm(Intracomm comm) {
        this.comm = comm;
    }

}
