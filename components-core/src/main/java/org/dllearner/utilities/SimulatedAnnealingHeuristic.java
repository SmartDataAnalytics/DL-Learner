package org.dllearner.utilities;


import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.core.AbstractSearchTreeNode;
import org.dllearner.utilities.datastructures.SearchTree;

import java.util.Random;

public class SimulatedAnnealingHeuristic<N extends AbstractSearchTreeNode> {
    private double startTemperature;
    private double currentTemperature;
    /**
     * used to calculate sub-/superlinear cooling (which are defined as
     * decreasing slower/faster as the linear case and thus require us to keep
     * the linear cooling for reference.
     */
    private double currentLinearTemperature;
    private Cooling cooling;
    private long seed = 123;
    private Random rng;
    /*
     * Can take a value between 0 (no stretch, i.e. linear) and 1 (max stretch)
     */
    private double linearityStretch = 0.5;

    public SimulatedAnnealingHeuristic(double startTemperature, Cooling cooling) {
        this.startTemperature = startTemperature;
        this.currentTemperature = startTemperature;
        this.currentLinearTemperature = startTemperature;
        this.cooling = cooling;
        this.rng = new Random(seed);
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public void setLinearityStretch(double stretch) {
        this.linearityStretch = stretch;
    }

    /**
     * @param progress A double between 0 and 1 expressing the progress
     *                 regarding the overall execution time. Will be 0 when the
     *                 algorithm started and 1 when it ends.
     */
    public void cool(double progress) {
        currentLinearTemperature = startTemperature - (progress * startTemperature);

        if (cooling.equals(Cooling.LINEAR)) {
            currentTemperature = currentLinearTemperature;
        } else if (cooling.equals(Cooling.SUPERLINEAR)) {
            currentTemperature = superlinear(currentLinearTemperature);
        } else if (cooling.equals(Cooling.SUBLINEAR)) {
            currentTemperature = sublinear(currentLinearTemperature);
        } else {
            throw new RuntimeException("Cooling strategy not supported, yet");
        }
    }

    private double superlinear(double x) {
        double s = (startTemperature / 4) * linearityStretch;
        double shift = startTemperature / 2;

        //       v-- only difference compared to sublinear
        return x - s*(((-1 / Math.pow(shift, 2)) * Math.pow(x - shift, 2)) + 1);
    }

    private double sublinear(double x) {
        double s = (startTemperature / 4) * linearityStretch;
        double shift = startTemperature / 2;

        //       v-- only difference compared to superlinear
        return x + s*(((-1 / Math.pow(shift, 2)) * Math.pow(x - shift, 2)) + 1);
    }

    /**
     * Cools down the simulated annealing heuristic by one 'degree' or step
     */
    public void cool() {
        if (currentTemperature < 0) {
            currentTemperature = 0;
            currentLinearTemperature = 0;

        } else if (currentTemperature > 0){

            if (cooling.equals(Cooling.LINEAR)) {
                currentLinearTemperature--;
                currentTemperature--;
            } else if (cooling.equals(Cooling.SUPERLINEAR)) {
                currentLinearTemperature--;
                currentTemperature = superlinear(currentLinearTemperature);
            } else if (cooling.equals(Cooling.SUBLINEAR)) {
                currentLinearTemperature--;
                currentTemperature = sublinear(currentLinearTemperature);
            } else {
                throw new RuntimeException("Cooling characteristic not supported, yet");
            }
        }
    }

    /**
     * Heats up the simulated annealing heuristic by one 'degree' or step
     */
    public void heat() {
        if (currentTemperature > startTemperature) {
            currentTemperature = startTemperature;
            currentLinearTemperature = startTemperature;

        } else if (currentTemperature < startTemperature){

            if (cooling.equals(Cooling.LINEAR)) {
                currentLinearTemperature++;
                currentTemperature++;
            } else if (cooling.equals(Cooling.SUPERLINEAR)) {
                currentLinearTemperature++;
                currentTemperature = superlinear(currentLinearTemperature);
            } else if (cooling.equals(Cooling.SUBLINEAR)) {
                currentLinearTemperature++;
                currentTemperature = sublinear(currentLinearTemperature);
            } else {
                throw new RuntimeException("Cooling characteristic not supported, yet");
            }
        }
    }

    public N pickNode(SearchTree searchTree) {
        double currBestAccuracy = searchTree.best().getAccuracy();

        /*
         * 1 +-------.
         *   |       \\
         *   |        \|
         * 0 +---------+--->
         *   start     0
         *   temp.
         *
         * - currBestAccuracy is high --> currThreshold shrinks faster w.r.t.
         *   currentTemperature --> randomness decreases fast
         * - currBestAccuracy is low --> currThreshold shrinks only very slowly
         *   --> randomness decreases slowly
         */
        double currThreshold = Math.exp(-currBestAccuracy / currentTemperature);

        double rnd = rng.nextDouble();

        if (rnd < currThreshold) {
            // pick random
            int numNodes = searchTree.size();

            return (N) searchTree.getNodeSet().toArray()[rng.nextInt(numNodes)];

        } else {
            return (N) searchTree.best();
        }
    }

    public N pickNode(N candidate, SearchTree searchTree) {
        double candidateAccuracy = candidate.getAccuracy();

        /*
         * 1 +-------.
         *   |       \\
         *   |        \|
         * 0 +---------+--->
         *   start     0
         *   temp.
         *
         * - candidateAccuracy is high --> currThreshold shrinks faster w.r.t.
         *   currentTemperature --> randomness decreases fast
         * - candidateAccuracy is low --> currThreshold shrinks only very slowly
         *   --> randomness decreases slowly
         */
        double currThreshold = Math.exp(-candidateAccuracy / currentTemperature);

        double rnd = rng.nextDouble();

        if (rnd < currThreshold) {
            // pick random
            int numNodes = searchTree.size();

            return (N) searchTree.getNodeSet().toArray()[rng.nextInt(numNodes)];

        } else {
            return candidate;
        }
    }

    public static void main(String[] args) {
        SimulatedAnnealingHeuristic<OENode> h = new SimulatedAnnealingHeuristic<>(30, Cooling.SUPERLINEAR);
        h.setLinearityStretch(1);

//        int cntr = 0;
//        while (h.getCurrentTemperature() > 0) {
//            System.out.println(cntr + "," + h.getCurrentTemperature());
//            h.cool();
//            cntr++;
//        }
    }
}
