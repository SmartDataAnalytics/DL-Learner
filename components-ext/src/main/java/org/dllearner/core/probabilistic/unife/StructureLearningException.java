/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dllearner.core.probabilistic.unife;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public class StructureLearningException extends RuntimeException {
    
    public StructureLearningException() {
        super();
    }
    
    public StructureLearningException(String message) {
        super(message);
    }
    
    public StructureLearningException(Throwable cause) {
        super(cause);
    }
    
}
