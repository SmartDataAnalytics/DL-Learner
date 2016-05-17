/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.core.probabilistic.unife;

import org.dllearner.core.AbstractCELA;
import org.dllearner.core.LearningAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public interface ProbabilisticStructureLearningAlgorithm extends LearningAlgorithm {

    /**
     * Starts the algorithm. It runs until paused, stopped, or a termination
     * criterion has been reached.
     */
    //public abstract void start();

//    /**
//     * Get the underlying learning algorithm.
//     * @return Underlying learning algorithm.
//     */
//    public AbstractCELA getLearningAlgorithm();
//    
//    /**
//     * Set the learning algorithm used to generate new class expression and then
//     * new axioms.
//     * @param la The learning algorithm used to generate class expressions.
//     */
//    @Autowired
//    public void setLearningAlgorithm(AbstractCELA la);
    
    /**
     * Get the underlying learning parameter algorithm.
     * @return Underlying learning parameter algorithm.
     */
    public ParameterLearningAlgorithm getLearningParameterAlgorithm();
    
    /**
     * Set the parameter learner algorithm. 
     * @param pla The parameter learner to be used.
     */
    @Autowired
    public void setLearningParameterAlgorithm(ParameterLearningAlgorithm pla);
    
}
