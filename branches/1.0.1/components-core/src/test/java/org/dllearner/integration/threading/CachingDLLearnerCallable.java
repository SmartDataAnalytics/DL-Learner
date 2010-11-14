package org.dllearner.integration.threading;

import org.dllearner.core.*;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;

import java.net.MalformedURLException;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 13, 2010
 * Time: 5:29:31 PM
 *
 * A Call to DL Learner that caches some things that are common to each thread - so we don't have to load them
 * over and over.
 */
public class CachingDLLearnerCallable extends DLLearnerCallable {

    /** Cache the reasoner as it's the expensive portion */
    private ReasonerComponent reasonerComponent;


    public CachingDLLearnerCallable() {

        try {
            /** Create the Knowledge Sources*/
            Set<KnowledgeSource> sources = createKnowledgeSources();
            /** Configure the Reasoner */
            reasonerComponent = createReasoner(sources);
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ComponentInitException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Override
    public EvaluatedDescription call() throws Exception {

        /** Configure the Learning Problem */
        LearningProblem learningProblem = createLearningProblem(reasonerComponent);

        /** Configure the Learning Algorithm */
        LearningAlgorithm learningAlgorithm = createLearningAlgorithm(reasonerComponent, learningProblem);

        learningAlgorithm.start();
        printConclusions(reasonerComponent, 1000);

        EvaluatedDescriptionPosNeg evaluatedDescription = (EvaluatedDescriptionPosNeg) learningAlgorithm.getCurrentlyBestEvaluatedDescription();
        return evaluatedDescription;
    }
}
