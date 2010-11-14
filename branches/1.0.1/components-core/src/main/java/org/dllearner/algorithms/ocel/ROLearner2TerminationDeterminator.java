package org.dllearner.algorithms.ocel;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 13, 2010
 * Time: 5:02:26 PM
 * <p/>
 * Implementation taken from the ROLLearner 2 code.
 */
public class ROLearner2TerminationDeterminator implements ITerminationDeterminator {

    private static Logger logger = Logger.getLogger(ROLearner2TerminationDeterminator.class);

    // setting to true indicates that we need to stop the algorithm.
    private boolean stop = false;
    private long maxExecutionTimeInSeconds = 0;
    private long minExecutionTimeInSeconds = 0;

    private long maxClassDescriptionTests = 0;
    private int guaranteeXgoodDescriptions = 1;


    /**
     * In this function it is calculated whether the algorithm should stop.
     * This is not always depends whether an actual solution was found
     * The algorithm stops if:
     * 1. the object attribute stop is set to true (possibly by an outside source)
     * 2. the maximimum execution time is reached
     * 3. the maximum number of class description tests is reached
     * <p/>
     * Continuation criteria and result improvement
     * The algorithm continues (although it would normally stop) if
     * 1. Minimum execution time is not reached (default 0)
     * 2. not enough good solutions are found (default 1)
     * otherwise it stops
     *
     * @param startTime the time the algorithm started, in milliseconds
     * @return true if the algorithm should stop, this is mostly indepent of the question if a solution was found
     */
    @Override
    public boolean isTerminationCriteriaReached(long startTime, int conceptsTested, int solutionsTested) {


        if (this.stop) {
            return true;
        }
//		System.out.println("ssssss");
        long totalTimeNeeded = System.currentTimeMillis() - startTime;
        long maxMilliSeconds = maxExecutionTimeInSeconds * 1000;
        long minMilliSeconds = minExecutionTimeInSeconds * 1000;
        boolean result = false;

        //ignore default
        if (maxExecutionTimeInSeconds == 0)
            result = false;
            //alreadyReached
            //test
        else if (maxMilliSeconds < totalTimeNeeded) {
            stop = true;
            logger.info("Maximum time (" + maxExecutionTimeInSeconds
                    + " seconds) reached, stopping now...");
            return true;
        }

        //ignore default
        if (maxClassDescriptionTests == 0)
            result = false;
            //test
        else if (conceptsTested >= maxClassDescriptionTests) {
            logger.info("Maximum Class Description tests (" + maxClassDescriptionTests
                    + " tests [actual: " + conceptsTested + "]) reached, stopping now...");
            return true;
        }


        if (solutionsTested >= guaranteeXgoodDescriptions) {
            if (guaranteeXgoodDescriptions != 1) {
                logger.info("Minimum number (" + guaranteeXgoodDescriptions
                        + ") of good descriptions reached.");
            }
            result = true;
        }


        if (minMilliSeconds < totalTimeNeeded) {
            if (minExecutionTimeInSeconds != 0) {
                logger.info("Minimum time (" + minExecutionTimeInSeconds
                        + " seconds) reached.");
            }
            result = result && true;
        } else {
            result = false;
        }

        return result;

    }
}
