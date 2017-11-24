/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.core.probabilistic.unife;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.reasoning.unife.ProbabilisticReasonerType;
import org.dllearner.utils.unife.OWLUtils;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import unife.bundle.Bundle;
import unife.bundle.QueryResult;
import unife.utilities.GeneralUtils;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
@ComponentAnn(name = "BUNDLE", shortName = "bundle", version = 1.0)
public class BUNDLE extends AbstractProbabilisticReasonerComponent implements OWLProbabilisticExplanationReasoner {

    private Bundle bundle = new Bundle();

    @ConfigOption(description = "max time allowed for the inference (format: [0-9]h[0-9]m[0-9]s)", defaultValue = "0s (infinite timeout)")
    private String timeout = "0s";

    @ConfigOption(description = "the maximum number of explanations to find for each query", defaultValue = "" + Integer.MAX_VALUE)
    private int maxExplanations = Integer.MAX_VALUE;

    @ConfigOption(description = "accuracy used during the computation of the probabilistic values (number of digital places)", defaultValue = "5")
    private int accuracy = 5;

    @ConfigOption(description = "library used for BDD compilation", defaultValue = "buddy")
    private String bddFType = "buddy";

    @Override
    public ProbabilisticReasonerType getReasonerType() {
        return ProbabilisticReasonerType.BUNDLE;
    }

    @Override
    public void init() throws ComponentInitException {
        super.init();
        bundle.setBddFType(bddFType);
        bundle.setMaxExplanations(this.maxExplanations);
        bundle.setMaxTime(this.timeout);
        bundle.setLog(true);
        bundle.setAccuracy(this.accuracy);
        bundle.loadOntologies(ontology);
        bundle.init();
        initialized = true;
    }

    @Override
    public OWLProbExplanationReasonerResult computeQueryWithExplanations(OWLAxiom axiom) throws OWLException {
        QueryResult result = bundle.computeQuery(axiom);
        return new OWLProbExplanationReasonerResult(
                axiom,
                result.getQueryProbability().doubleValue(),
                GeneralUtils.safe(result.getExplanations()));
    }
    
    @Override
    public OWLProbReasonerResult computeQuery(OWLAxiom axiom) throws OWLException {
        return computeQueryWithExplanations(axiom);
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /**
     * @param maxExplanations the maxExplanations to set
     */
    public void setMaxExplanations(int maxExplanations) {
        this.maxExplanations = maxExplanations;
    }

    /**
     * @param accuracy the accuracy to set
     */
    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * @param bddFType the bddFType to set
     */
    public void setBddFType(String bddFType) {
        this.bddFType = bddFType;
    }

}
