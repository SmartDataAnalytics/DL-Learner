/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.unife.probabilistic.core;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.unife.reasoning.ProbabilisticReasonerType;
import unife.bundle.logging.BundleLoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Giuseppe Cota <giuseta@gmail.com>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
public abstract class AbstractProbabilisticReasonerComponent extends AbstractComponent implements ProbabilisticReasoner {

    public static Logger logger = Logger.getLogger(AbstractProbabilisticReasonerComponent.class.getName(),
            new BundleLoggerFactory());

    /**
     * The underlying knowledge sources.
     */
    protected Set<KnowledgeSource> sources;

    public AbstractProbabilisticReasonerComponent() {

    }

    /**
     * Constructs a new reasoner component.
     *
     * @param sources The underlying knowledge sources.
     */
    public AbstractProbabilisticReasonerComponent(Set<KnowledgeSource> sources) {
        this.sources = sources;
    }

    public AbstractProbabilisticReasonerComponent(KnowledgeSource source) {
        this(Collections.singleton(source));
    }

    /**
     * Gets the knowledge sources used by this reasoner.
     *
     * @return The underlying knowledge sources.
     */
    public Set<KnowledgeSource> getSources() {
        return sources;
    }

    public void setSources(Set<KnowledgeSource> sources) {
        this.sources = sources;
    }

    public void setSources(KnowledgeSource... sources) {
        this.sources = new HashSet<KnowledgeSource>(Arrays.asList(sources));
    }

    /**
     * Method to exchange the reasoner underlying the learning problem.
     * Implementations, which do not only use the provided sources class
     * variable, must make sure that a call to this method indeed changes them.
     *
     * @param sources The new knowledge sources.
     */
    public void changeSources(Set<KnowledgeSource> sources) {
        this.sources = sources;
    }

    /**
     * Gets the type of the underlying probabilistic reasoner. Although rarely necessary,
     * applications can use this to adapt their behaviour to the reasoner.
     *
     * @return The probabilistic reasoner type.
     */
    public abstract ProbabilisticReasonerType getReasonerType();
}
