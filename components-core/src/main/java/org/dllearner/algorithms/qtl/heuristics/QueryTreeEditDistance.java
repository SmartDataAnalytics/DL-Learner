package org.dllearner.algorithms.qtl.heuristics;

import java.util.List;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorImpl;

public class QueryTreeEditDistance {

    public static <T> double getDistance(QueryTree<T> tree1, QueryTree<T> tree2) {
        double distance = 0;

        // compare root node
        // null vs. null
        if (tree1 == null && tree2 == null) {
            return distance;

        // null vs. node
        } else if (tree1 == null) {
            distance += 1;
            for (QueryTree<T> child : tree2.getChildren()) {
                distance += getDistance(null, child);
            }
            return distance;

        // node vs. null
        } else if (tree2 == null) {
            distance += 1;
            for (QueryTree<T> child : tree1.getChildren()) {
                distance += getDistance(child, null);
            }
            return distance;

        // node vs. node
        } else {
            distance += tree1.getUserObject().equals(tree2.getUserObject()) ? 0 : 1;
        }

        List<QueryTree<T>> tree1queue = tree1.getChildren();
        List<QueryTree<T>> tree2queue = tree2.getChildren();

        // make tree1queue the longer queue
        if (tree1queue.size() < tree2queue.size()) {
            List<QueryTree<T>> tmp = tree1queue;
            tree1queue = tree2queue;
            tree2queue = tmp;
        }

        while (tree1queue.size() > 0) {
            double minDistance = Double.MAX_VALUE;
            QueryTree<T> minDistanceTree1Child = null;
            QueryTree<T> minDistanceTree2Child = null;

            // try all combinations of tree 1 and tree 2 children and chose
            // those with the smallest distance as 'match'
            for (QueryTree<T> queryTree1 : tree1queue) {
                double tmpDistance;

                // case 1: tree 2 queue is empty:
                if (tree2queue.isEmpty()) {
                    tmpDistance = getDistance(queryTree1, null);

                    if (tmpDistance < minDistance) {
                        minDistance = tmpDistance;
                        minDistanceTree1Child = queryTree1;
                    }

                // case 2: tree 2 queue is not empty:
                } else {
                    for (QueryTree<T> queryTree2 : tree2queue) {
                        tmpDistance = getDistance(queryTree1, queryTree2);

                        if (tmpDistance <= minDistance) {
                            minDistance = tmpDistance;
                            minDistanceTree1Child = queryTree1;
                            minDistanceTree2Child = queryTree2;
                        }
                    }
                }
            }

            distance += minDistance;
            tree1queue.remove(minDistanceTree1Child);
            if (minDistanceTree2Child != null) {
                tree2queue.remove(minDistanceTree2Child);
            }
        }

        return distance;
    }

    /**
     * Returns a distance between <code>tree1</code> and <code>tree2</code> based
     *  on the LGG of both.
     * @param tree1
     * @param tree2
     * @return
     */
	public static <T> double getDistanceApprox(QueryTree<T> tree1, QueryTree<T> tree2) {
		LGGGenerator<T> lggGenerator = new LGGGeneratorImpl<T>();


		// compute the LGG of tree1 and tree2
		QueryTree<T> lgg = lggGenerator.getLGG(tree1, tree2);

		// we define the distance as the maximum difference between the complexity
		// of tree1 to lgg and tree2 to lgg
		double complexityLGG = QueryTreeUtils.getComplexity(lgg);
		double complexityTree1 = QueryTreeUtils.getComplexity(tree1);
		double complexityTree2 = QueryTreeUtils.getComplexity(tree2);

		double distance = Math.max(
				complexityTree1 - complexityLGG,
				complexityTree2 - complexityLGG);

//		System.out.println(tree1.getStringRepresentation());
//		System.out.println(complexityTree1);
//		System.out.println(tree2.getStringRepresentation());
//		System.out.println(complexityTree2);
//		System.out.println(lgg.getStringRepresentation());
//		System.out.println(complexityLGG);
		return distance;
	}
}
