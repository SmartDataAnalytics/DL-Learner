package org.dllearner.gui;

/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;

/**
 * config
 * 
 * this class save all together used variables
 * 
 * @author Tilo Hielscher
 * 
 */

public class Config {
    private ComponentManager cm = ComponentManager.getInstance();
    private KnowledgeSource source;
    private String uri;
    private ReasonerComponent reasoner;
    private ReasoningService rs;
    private Set<String> posExampleSet = new HashSet<String>();
    private Set<String> negExampleSet = new HashSet<String>();
    private LearningProblem lp;
    private LearningAlgorithm la;
    private boolean[] isInit = new boolean[4];
 
    
    protected ComponentManager getComponentManager() {
	return cm;
    }

    protected void setComponentManager(ComponentManager input) {
	cm = input;
    }

    protected String getURI() {
	return uri;
    }

    protected void setURI(String input) {
	uri = input;
    }

    protected ReasonerComponent getReasoner() {
	return reasoner;
    }

    protected void setReasoner(ReasonerComponent input) {
	reasoner = input;
    }

    protected ReasoningService getReasoningService() {
	return rs;
    }

    protected void setReasoningService(ReasoningService input) {
	rs = input;
    }

    protected KnowledgeSource getKnowledgeSource() {
	return source;
    }

    protected void setKnowledgeSource(KnowledgeSource input) {
	source = input;
    }

    protected void setPosExampleSet(Set<String> posExampleSet) {
	this.posExampleSet = posExampleSet;
    }

    protected Set<String> getPosExampleSet() {
	return this.posExampleSet;
    }

    protected void setNegExampleSet(Set<String> negExampleSet) {
	this.negExampleSet = negExampleSet;
    }

    protected Set<String> getNegExampleSet() {
	return this.negExampleSet;
    }

    protected void setLearningProblem(LearningProblem input) {
	lp = input;
    }

    protected LearningProblem getLearningProblem() {
	return lp;
    }

    protected void setLearningAlgorithm(LearningAlgorithm input) {
	la = input;
    }

    protected LearningAlgorithm getLearningAlgorithm() {
	return la;
    }

    /*
     * KnowledgeSource.init has run?
     * return true, if it was
     */
    protected boolean isInitKnowledgeSource() {
	return isInit[0];
    }
    
    /*
     * set true if you run KnowwledgeSource.init
     */
    protected void setInitKnowledgeSource(Boolean is) {
	isInit[0] = is;
	for (int i=1; i<4; i++) 
	    isInit[i] = false;
    }

    /*
     * Reasoner.init has run?
     * return true, if it was
     */
    protected boolean isInitReasoner() {
	return isInit[1];
    }
    
    /*
     * set true if you run Reasoner.init
     */
    protected void setInitReasoner(Boolean is) {
	isInit[1] = is;
	for (int i=2; i<4; i++)
	    isInit[i] = false;
    }

    /*
     * LearningProblem.init has run?
     * return true, if it was
     */
    protected boolean isInitLearningProblem() {
	return isInit[2];
    }
    
    /*
     * set true if you run LearningProblem.init
     */
    protected void setInitLearningProblem(Boolean is) {
	isInit[2] = is;
	for (int i=3; i<4; i++)
	    isInit[i] = false;
    }

    /*
     * LearningAlgorithm.init() has run?
     * return true, if it was
     */
    protected boolean isLearningAlgorithm() {
	return isInit[3];
    }
    
    /*
     * set true if you run LearningAlgorithm.init
     */
    protected void setLearningAlgorithm(Boolean is) {
	isInit[3] = is;
    }

}
