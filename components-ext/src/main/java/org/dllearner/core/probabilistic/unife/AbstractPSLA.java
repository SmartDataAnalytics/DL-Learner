/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.core.probabilistic.unife;

import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.StoppableLearningAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public abstract class AbstractPSLA implements ProbabilisticStructureLearningAlgorithm, StoppableLearningAlgorithm {

    /**
     * The learning algorithm used to compute the probabilistic parameters.
     */
    protected AbstractParameterLearningAlgorithm pla;

    /**
     * The learning algorithm used to extract new class expressions.
     */
    protected AbstractCELA cela;

    /**
     * The learning problem variable, which must be used by all learning
     * algorithm implementations.
     */
    protected AbstractLearningProblem learningProblem;

    protected String outFormat = "OWLXML";

    protected String outputFile = "learnedOntology.owl";

    protected boolean isRunning = false;
    protected boolean stop = false;

    public AbstractPSLA() {

    }

    /**
     * Each probabilistic structure learning algorithm gets a class expression
     * learning algorithm and a parameter learning algorithm
     *
     * @param cela
     * @param pla
     */
    public AbstractPSLA(AbstractCELA cela, AbstractParameterLearningAlgorithm pla) {
        this.cela = cela;
        this.pla = pla;
    }

    public AbstractCELA getClassExpressionLearningAlgorithm() {
        return cela;
    }

    @Autowired
    public void setClassExpressionLearningAlgorithm(AbstractCELA la) {
        this.cela = la;
    }

    @Override
    public ParameterLearningAlgorithm getLearningParameterAlgorithm() {
        return pla;
    }

    @Autowired
    @Override
    public void setLearningParameterAlgorithm(ParameterLearningAlgorithm pla) {
        this.pla = (AbstractParameterLearningAlgorithm) pla;
    }

    /**
     * The learning problem variable, which must be used by all learning
     * algorithm implementations.
     */
    @Override
    public AbstractLearningProblem getLearningProblem() {
        return learningProblem;
    }

    @Autowired
    @Override
    public void setLearningProblem(LearningProblem learningProblem) {
        this.learningProblem = (AbstractLearningProblem) learningProblem;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void stop() {
        stop = true;
    }

    /**
     * @return the outputFile
     */
    public String getOutputFile() {
        return outputFile;
    }

    /**
     * @param outputFile the outputFile to set
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

}
