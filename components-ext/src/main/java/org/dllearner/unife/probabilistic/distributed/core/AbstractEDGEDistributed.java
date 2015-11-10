/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.unife.probabilistic.distributed.core;

import mpi.Intracomm;
import mpi.MPI;
import org.dllearner.unife.probabilistic.parameter.algorithms.AbstractEDGE;

/**
 *
 * @author Giuseppe Cota <giuseta@gmail.com>, Riccardo Zese
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
