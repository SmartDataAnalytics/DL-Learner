package org.dllearner.algorithms.qtl.heuristics;

import java.util.List;
import java.util.Set;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;

public class QueryTreeEditDistance {



    public static <T> double getDistance(QueryTree<T> tree1, QueryTree<T> tree2) {
        //
        double distance = 0;

        // compare root node
        distance += tree1.getUserObject().equals(tree2.getUserObject()) ? 0 : 1;

        // compare children
        Set<Object> edges = tree1.getEdges();
        for (Object edge : edges) {
            List<QueryTree<T>> children1 = tree1.getChildren(edge);

            for (QueryTree<T> queryTree : children1) {
                // TODO
            }
        }

        return distance;
    }
}
