/**
 * This file is part of LEAP.
 *
 * LEAP was implemented as a plugin of DL-Learner http://dl-learner.org, but
 * some components can be used as stand-alone.
 *
 * LEAP is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * LEAP is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.core.probabilistic.unife;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.StoppableLearningAlgorithm;
import org.dllearner.core.config.ConfigOption;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract component representing a parameter Learner. A parameter learner
 * computes the probabilistic value of a single axiom
 *
 * @author Giuseppe Cota
 */
public abstract class AbstractParameterLearningAlgorithm implements ParameterLearningAlgorithm, StoppableLearningAlgorithm {

    protected boolean isRunning = false;
    protected boolean stop = false;

    /**
     * set of all the theories
     */
    //protected Set<KnowledgeSource> sources;
    /**
     * File URI (String) containing the target axioms, whose parameters we want
     * to learn
     */
    //protected String targetAxioms;
    protected Set<OWLAxiom> targetAxioms;
    protected AbstractLearningProblem learningProblem;

    @ConfigOption(description = "Force the reduction of the number of individuals in the initial ontology", defaultValue = "false")
    protected boolean reduceOntology = false;

    public AbstractParameterLearningAlgorithm() {

    }

    /**
     *
     * @param learningProblem
     * @param targetAxioms target axioms in manchester syntax
     */
//    public AbstractLearningParameter(AbstractLearningProblem learningProblem, String targetAxioms){
//        this.sources=learningProblem.getReasoner().getSources();
//        this.targetAxioms=targetAxioms;
//        this.learningProblem=learningProblem;
//    }
    public AbstractParameterLearningAlgorithm(AbstractLearningProblem learningProblem, Set<OWLAxiom> targetAxioms) {
        //this.sources = learningProblem.getReasoner().getSources();
        this.targetAxioms = targetAxioms;
        this.learningProblem = learningProblem;
    }

    /**
     * Sets the target axioms of which want to learn the parameters.
     *
     * @param targetAxioms
     */
    public void setTargetAxioms(Set<OWLAxiom> targetAxioms) {
        this.targetAxioms = targetAxioms;
    }

    /**
     * Gets the target axioms.
     *
     * @return target axioms
     */
    public Set<OWLAxiom> getTargetAxioms() {
        return targetAxioms;
    }

    /**
     * Gets the probabilistic parameter of an axiom
     *
     * @param ax
     * @return the probabilistic parameter of the axiom
     */
    public abstract BigDecimal getParameter(OWLAxiom ax) throws ParameterLearningException;

    /**
     * Gets the map of the time in milliseconds spent by various algorithms.
     *
     * @return
     */
    public abstract Map<String, Long> getTimeMap();

//    /**
//     * Gets the knowledge sources used by this parameters learner
//     *
//     * @return the underlying knowledge sources
//     */
//    public Set<KnowledgeSource> getSources() {
//        return sources;
//    }
//
//    /**
//     * Method to change the knowledge sources underlying the parameter learner.
//     * Changes only the knowledge sources contained in this class. Does not
//     * affect other classes
//     *
//     * @param sources The new knowledge sources.
//     */
//    public void changeSources(Set<KnowledgeSource> sources) {
//        this.sources = sources;
//    }

    /**
     * Gets the target axioms whose parameters we want to learn
     *
     * @return the underlying target axioms
     */
//    public String getTargetAxioms() {
//        return targetAxioms;
//    }
    /**
     * Changes the target axioms underlying the parameter learner
     *
     * @param the underlying target axioms
     */
//    public void changeTargetAxioms(String targetAxioms) {
//        this.targetAxioms = targetAxioms;
//    }
    /**
     * The learning problem variable, which must be used by all learning
     * algorithm implementations.
     */
    @Override
    public AbstractLearningProblem getLearningProblem() {
        return learningProblem;
    }

    @Override
    @Autowired
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

}
