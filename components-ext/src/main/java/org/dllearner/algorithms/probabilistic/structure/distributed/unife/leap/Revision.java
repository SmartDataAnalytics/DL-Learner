/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.algorithms.probabilistic.structure.distributed.unife.leap;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
class Revision implements Comparable<Revision>, Serializable {

    private LinkedHashSet<OWLAxiom> targetAxioms;

    private Set<OWLSubClassOfAxiom> learnedAxioms;

    private List<Boolean> boolVars;

    private BigDecimal LL;

    public Revision(LinkedHashSet<OWLAxiom> targetAxioms, List<Boolean> boolVars,
            Set<OWLSubClassOfAxiom> learnedAxioms, BigDecimal LL) {
        this.targetAxioms = targetAxioms;
        this.boolVars = boolVars;
        this.learnedAxioms = learnedAxioms;
        this.LL = LL;
    }

    public Revision(LinkedHashSet<OWLAxiom> targetAxioms, List<Boolean> boolVars) {
        this.targetAxioms = targetAxioms;
        this.boolVars = boolVars;
        this.LL = new BigDecimal(Long.MIN_VALUE);
    }
    
    public Revision() {
        this.LL = new BigDecimal(Long.MIN_VALUE);
    }

    /**
     * @return the targetAxioms
     */
    public LinkedHashSet<OWLAxiom> getTargetAxioms() {
        return targetAxioms;
    }

    @Override
    public int compareTo(Revision o) {
//        Logger logger = Logger.getLogger(CLIDistributedLEAP.class.getName(), new BundleLoggerFactory());
//        logger.info(this.LL.doubleValue());
//        logger.info(o.LL.doubleValue());
        int diffLL = this.LL.compareTo(o.LL);
        if (diffLL != 0) {
            return -diffLL; // the elements are in descending order of LL
        } else {
            int numUsedAxiomsThis = 0;
            int numUsedAxiomsOther = 0;
            for (Boolean bool : boolVars) {
                if (bool == true) {
                    numUsedAxiomsThis++;
                }
            }
            for (Boolean bool : o.boolVars) {
                if (bool == true) {
                    numUsedAxiomsOther++;
                }
            }
            return numUsedAxiomsThis - numUsedAxiomsOther;
        }
    }

    /**
     * @return the LL
     */
    public BigDecimal getLL() {
        return LL;
    }

    /**
     * @param LL the LL to set
     */
    public void setLL(BigDecimal LL) {
        this.LL = LL;
    }

    /**
     * @return the learnedAxioms
     */
    public Set<OWLSubClassOfAxiom> getLearnedAxioms() {
        return learnedAxioms;
    }

    List<Boolean> getBoolVars() {
        return boolVars;
    }

}
