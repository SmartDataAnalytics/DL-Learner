/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dllearner.core.probabilistic.distributed.unife;

import org.dllearner.core.Component;

/**
 * Marker interface for distributed components.
 * It is used to indicate the classes that are supposed to be used inside a
 * distributed environment that uses MPI to communicate.
 * 
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public interface DistributedComponent extends Component {
    
}
