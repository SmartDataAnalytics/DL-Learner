package org.dllearner.algorithms.ocel;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 13, 2010
 * Time: 5:00:35 PM
 *
 *
 * This interface represents an algorithm that determines whether an algorithm should terminate or not.
 */
public interface ITerminationDeterminator {

    /**
     * This method returns true if the algorithm should stop.
     *
     * @param startTime The time the algorithm started, in ms.
     * @param conceptsTested The number of concepts tested so far.
     * @param solutionsTested The number of solutions tested so far.
     * @return True if the algorithm should stop.
     *
     */
    public boolean isTerminationCriteriaReached(long startTime, int conceptsTested, int solutionsTested);
}
