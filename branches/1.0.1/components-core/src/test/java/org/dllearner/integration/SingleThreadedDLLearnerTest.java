package org.dllearner.integration;

import org.apache.log4j.Logger;
import org.dllearner.core.owl.Individual;
import org.dllearner.integration.threading.DLLearnerCallable;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 13, 2010
 * Time: 8:41:12 AM
 *
 * This test shows how to run the DL-Learner Code programmtically in a basic way.  We'll
 * also use it to test a series of basic functions - to ensure we don't break anything as we're developing.
 *
 */
public class SingleThreadedDLLearnerTest {

    private static Logger logger = Logger.getLogger(SingleThreadedDLLearnerTest.class);

    /**
     * Default Constructor
     */
    public SingleThreadedDLLearnerTest() {

    }


    /**
     * Run the test all the way through.
     */
    @Test
    public void runTest() throws Exception {

        DLLearnerCallable callable = new DLLearnerCallable();
        EvaluatedDescriptionPosNeg eDPN = (EvaluatedDescriptionPosNeg)callable.call();
        validateResults(eDPN);

    }

    protected void validateResults(EvaluatedDescriptionPosNeg eDPN) {
        Set<Individual> coveredPositives = eDPN.getCoveredPositives();
        Set<Individual> notCoveredPositives = eDPN.getNotCoveredPositives();

        Set<Individual> coveredNegatives = eDPN.getCoveredNegatives();
        Set<Individual> notCoveredNegatives = eDPN.getNotCoveredNegatives();

        assert coveredPositives.size() == 3;
        assert notCoveredPositives.isEmpty();

        assert coveredNegatives.isEmpty();
        assert notCoveredNegatives.size() == 4;
    }
}
