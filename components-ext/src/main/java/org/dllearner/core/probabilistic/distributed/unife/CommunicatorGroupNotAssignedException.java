/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dllearner.core.probabilistic.distributed.unife;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public class CommunicatorGroupNotAssignedException extends RuntimeException {

    public CommunicatorGroupNotAssignedException() {
        super();
    }
    
    public CommunicatorGroupNotAssignedException(String message) {
        super(message);
    }
    
    public CommunicatorGroupNotAssignedException(Throwable cause) {
        super(cause);
    }
}
