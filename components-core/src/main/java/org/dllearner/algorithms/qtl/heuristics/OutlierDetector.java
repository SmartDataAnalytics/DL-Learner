package org.dllearner.algorithms.qtl.heuristics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.linear.RealMatrix;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;

public class OutlierDetector {

    public static <T> List<QueryTree<T>> getOutliers(
            List<QueryTree<T>> queryTrees, RealMatrix distanceMatrix) {

        List<QueryTree<T>> outliers = new ArrayList<QueryTree<T>>();

        List<Set<QueryTree<T>>> clusters = getClusters(queryTrees, distanceMatrix);

        // for debugging
        int numClusters = clusters.size();

        /*
         * heuristic to find the outlier: all distance values should be greater
         * than the mean
         */
        double mean = calculateMean(distanceMatrix);

        int numRows = distanceMatrix.getRowDimension();
        int clusterCounter = 0;
        while (clusterCounter < numClusters) {
            Set<QueryTree<T>> cluster = clusters.get(clusterCounter);

            for (QueryTree<T> queryTree : cluster) {

                // get column index for given query tree
                int colIdx = queryTrees.indexOf(queryTree);
                boolean allGreaterThanMean = true;

                int rowCounter = 0;
                // iterate over column values
                while (rowCounter < numRows) {
                    // skip diagonal 0 values
                    if (rowCounter == colIdx) {
                        rowCounter++;
                        continue;
                    }

                    double val = distanceMatrix.getEntry(rowCounter, colIdx);
                    if (val < mean) {
                        allGreaterThanMean = false;
                        break;
                    }

                    rowCounter++;
                }

                if (allGreaterThanMean) {
                    outliers.add(queryTree);
                } else {
                    break;
                }
            }

            clusterCounter++;
        }

        return outliers;
    }

    private static <T> List<Set<QueryTree<T>>> getClusters(List<QueryTree<T>> queryTrees, RealMatrix distanceMatrix) {
        List<Set<QueryTree<T>>> clusters = new ArrayList<Set<QueryTree<T>>>();

        // infer from matrix
        double eps = 0.5;

        int qt1Counter = 0;
        Set<QueryTree<T>> added = new HashSet<QueryTree<T>>();

        while (qt1Counter < queryTrees.size()) {
            QueryTree<T> queryTree1 = queryTrees.get(qt1Counter);

            if (added.contains(queryTree1)) {
                qt1Counter++;
                continue;
            }

            added.add(queryTree1);

            int qt2Counter = 0;
            HashSet<QueryTree<T>> cluster = new HashSet<QueryTree<T>>();
            cluster.add(queryTrees.get(qt1Counter));

            while (qt2Counter < queryTrees.size()) {
                if (qt1Counter == qt2Counter) {
                    qt2Counter++;
                    continue;
                }

                QueryTree<T> queryTree2 = queryTrees.get(qt2Counter);

                if (added.contains(queryTree2)) {
                    qt2Counter++;
                    continue;
                }


                double distance = distanceMatrix.getEntry(qt1Counter, qt2Counter);
//                System.out.println("distance between " + qt1Counter + " and " + qt2Counter + ": " + distance);
                if (distance < eps) {
                    cluster.add(queryTrees.get(qt2Counter));
                    added.add(queryTree2);
                }
                qt2Counter++;
            }

            clusters.add(cluster);

            qt1Counter++;
        }

        return clusters;
    }

    private static double calculateMean(RealMatrix mtrx) {
        int numRows = mtrx.getRowDimension();
        int numCols = mtrx.getColumnDimension();

        double sum = 0;
        int count = 0;

        int rowCounter = 0;
        int colCounter = 0;

        while (rowCounter < numRows) {
            while (colCounter < numCols) {
                double currVal = mtrx.getEntry(rowCounter, colCounter);
                if (currVal > 0) {
                    sum += currVal;
                    count++;
                }
                colCounter++;
            }

            rowCounter++;
        }

        return sum/count;
    }
}
