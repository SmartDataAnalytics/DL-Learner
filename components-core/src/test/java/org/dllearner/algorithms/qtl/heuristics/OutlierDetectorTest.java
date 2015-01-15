package org.dllearner.algorithms.qtl.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OutlierDetectorTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    private void fillMatrix(RealMatrix matrix, double[] values) {
        int numRows = matrix.getRowDimension();
        int numCols = matrix.getColumnDimension();

        int rowCounter = 0;
        int valCounter = 0;

        while (rowCounter < numRows) {
            int colCounter = 0;
            while (colCounter < numCols) {
                matrix.setEntry(rowCounter, colCounter, values[valCounter]);

                valCounter++;
                colCounter++;
            }
            rowCounter++;
        }
    }

    private List<QueryTree<String>> buildResourceQueryTreeList(String[] names) {
        List<QueryTree<String>> queryTrees = new ArrayList<QueryTree<String>>();
        for (String name : names) {
            queryTrees.add(new QueryTreeImpl<String>(name, NodeType.RESOURCE));
        }
        return queryTrees;
    }

    /**
     * Test on a relatively small 3 x 3- distance matrix without an outlier
     *
     *      a     b     c
     *    ----  ----  -----
     * a) 0     0.24  0.18
     * b) 0.24  0     0.2
     * c) 0.18  0.2   0
     */
    @Test
    public void test01() {
        int numRowsCols = 3;
        double[] values = {
                0,    0.24, 0.18,
                0.24, 0,    0.2,
                0.18, 0.2,  0
        };
        RealMatrix distances = MatrixUtils.createRealMatrix(numRowsCols, numRowsCols);
        fillMatrix(distances, values);
        String[] names = {"a", "b", "c" };
        List<QueryTree<String>> queryTrees = buildResourceQueryTreeList(names);

        List<QueryTree<String>> outliers = OutlierDetector.getOutliers(queryTrees, distances);
        assertTrue(outliers.isEmpty());
    }

    /**
     * Test on a relatively small 3 x 3- distance matrix without one outlier: c
     *
     *      a     b     c
     *    ----  ----  -----
     * a) 0     0.24  0.75
     * b) 0.24  0     0.8
     * c) 0.75  0.8   0
     */
    @Test
    public void test02() {
        int numRowsCols = 3;
        double[] values = {
                0,    0.24, 0.75,
                0.24, 0,    0.8,
                0.75, 0.8,  0
        };
        RealMatrix distances = MatrixUtils.createRealMatrix(numRowsCols, numRowsCols);
        fillMatrix(distances, values);
        String[] names = {"a", "b", "c" };
        List<QueryTree<String>> queryTrees = buildResourceQueryTreeList(names);

        List<QueryTree<String>> outliers = OutlierDetector.getOutliers(queryTrees, distances);
        assertEquals(1, outliers.size());
        assertEquals(queryTrees.get(2), outliers.get(0));
    }

    /**
     * Test on a larger, 9 x 9- distance matrix without one outlier: c
     *
     *      a     b     c     d     e     f     g     h     i
     *    ----  ----  ----  ----  ----  ----  ----  ----  ----
     * a) 0     0.22  0.86  0.14  0.19  0.22  0.22  0.16  0.18
     * b) 0.22  0     0.81  0.2   0.22  0.19  0.23  0.21  0.22
     * c) 0.86  0.81  0     0.83  0.83  0.77  0.79  0.75  0.78  <*
     * d) 0.14  0.2   0.83  0     0.17  0.19  0.17  0.22  0.2
     * e) 0.19  0.22  0.83  0.17  0     0.22  0.22  0.19  0.21
     * f) 0.22  0.19  0.77  0.19  0.22  0     0.23  0.19  0.18
     * g) 0.22  0.23  0.79  0.17  0.22  0.23  0     0.22  0.19
     * h) 0.16  0.21  0.75  0.22  0.19  0.19  0.22  0     0.21
     * i) 0.18  0.22  0.78  0.2   0.21  0.18  0.19  0.21  0
     *                 ^
     *                 *
     */
    @Test
    public void test03() {
        int numRowsCols = 9;
        double[] values = {
                // a   b     c     d     e     f     g     h     i
                0,    0.22, 0.86, 0.14, 0.19, 0.22, 0.22, 0.16, 0.18,  // a
                0.22, 0,    0.81, 0.2,  0.22, 0.19, 0.23, 0.21, 0.22,  // b
                0.86, 0.81, 0,    0.83, 0.83, 0.77, 0.79, 0.75, 0.78,  // c
                0.14, 0.2,  0.83, 0,    0.17, 0.19, 0.17, 0.22, 0.2,   // d
                0.19, 0.22, 0.83, 0.17, 0,    0.22, 0.22, 0.19, 0.21,  // e
                0.22, 0.19, 0.77, 0.19, 0.22, 0,    0.23, 0.19, 0.18,  // f
                0.22, 0.23, 0.79, 0.17, 0.22, 0.23, 0,    0.22, 0.19,  // g
                0.16, 0.21, 0.75, 0.22, 0.19, 0.19, 0.22, 0,    0.21,  // h
                0.18, 0.22, 0.78, 0.2,  0.21, 0.18, 0.19, 0.21, 0      // i
        };
        RealMatrix distances = MatrixUtils.createRealMatrix(numRowsCols, numRowsCols);
        fillMatrix(distances, values);
        String[] names = {"a", "b", "c", "d", "e", "f", "g", "h", "i" };
        List<QueryTree<String>> queryTrees = buildResourceQueryTreeList(names);

        List<QueryTree<String>> outliers = OutlierDetector.getOutliers(queryTrees, distances);
        assertEquals(1, outliers.size());
        assertEquals(queryTrees.get(2), outliers.get(0));
    }
}
