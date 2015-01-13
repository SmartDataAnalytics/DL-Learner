package org.dllearner.algorithms.qtl.heuristics;

import java.util.List;
import java.util.Set;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.utilities.QueryUtils;

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
