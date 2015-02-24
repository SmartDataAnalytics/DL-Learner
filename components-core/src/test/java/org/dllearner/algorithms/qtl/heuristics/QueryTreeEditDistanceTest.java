package org.dllearner.algorithms.qtl.heuristics;

import static junit.framework.Assert.assertEquals;

import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.junit.Test;

public class QueryTreeEditDistanceTest {
    /*
     *  A     A
     */
    @Test
    public void test01() {
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);

        double expectedDistance = 0;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     * A1     A2
     */
    @Test
    public void test02() {
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A1", NodeType.RESOURCE);
        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("A2", NodeType.RESOURCE);

        double expectedDistance = 1;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     *  A     A
     *  |
     *  B
     */
    @Test
    public void test03() {
        // tree 1:
        // A
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        // B
        QueryTreeImpl<String> child1_1 = new QueryTreeImpl<String>("B", NodeType.RESOURCE);
        tree1.addChild(child1_1, "p");

        // tree 2:
        // A
        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);

        double expectedDistance = 1;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     *  A     A
     *        |
     *        B
     */
    @Test
    public void test04() {
        // tree 1:
        // A
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);

        // tree 2:
        // A
        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        // B
        QueryTreeImpl<String> child2_1 = new QueryTreeImpl<String>("B", NodeType.RESOURCE);
        tree2.addChild(child2_1, "p");

        double expectedDistance = 1;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     *   A       A
     *  / \      |
     * B   C     C
     *     |
     *     D
     */
    @Test
    public void test05() {
        // tree 1:
        // A
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        // B
        QueryTreeImpl<String> child1_1 = new QueryTreeImpl<String>("B", NodeType.RESOURCE);
        // C
        QueryTreeImpl<String> child1_2 = new QueryTreeImpl<String>("C", NodeType.RESOURCE);
        // D
        QueryTreeImpl<String> grandChild1_2_1 = new QueryTreeImpl<String>("D", NodeType.RESOURCE);

        child1_2.addChild(grandChild1_2_1, "r");
        tree1.addChild(child1_1, "p");
        tree1.addChild(child1_2, "q");

        // tree 2:
        // A
        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        // C
        QueryTreeImpl<String> child2_1 = new QueryTreeImpl<String>("C", NodeType.RESOURCE);

        tree2.addChild(child2_1, "q");

        double expectedDistance = 2;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     *   A       A
     *  / \
     * B   C
     *     |
     *     D
     */
    @Test
    public void test06() {
        // tree 1:
        // A
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        // B
        QueryTreeImpl<String> child1_1 = new QueryTreeImpl<String>("B", NodeType.RESOURCE);
        // C
        QueryTreeImpl<String> child1_2 = new QueryTreeImpl<String>("C", NodeType.RESOURCE);
        // D
        QueryTreeImpl<String> grandChild1_2_1 = new QueryTreeImpl<String>("D", NodeType.RESOURCE);

        child1_2.addChild(grandChild1_2_1, "r");
        tree1.addChild(child1_1, "p");
        tree1.addChild(child1_2, "q");

        // tree 2:
        // A
        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);

        double expectedDistance = 3;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    /*
     *    A         A       | 0
     *   /|\       /|\
     *  B C D     G C H     | 2
     *  |   |     |   |
     *  E   F     E   F     | 0
     *
     *  --> dist = 2
     */
    @Test
    public void test07() {
        // tree 1:
        // A
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        // B
        QueryTreeImpl<String> child1_1 = new QueryTreeImpl<String>("B", NodeType.RESOURCE);
        // C
        QueryTreeImpl<String> child1_2 = new QueryTreeImpl<String>("C", NodeType.RESOURCE);
        // D
        QueryTreeImpl<String> child1_3 = new QueryTreeImpl<String>("D", NodeType.RESOURCE);
        // E
        QueryTreeImpl<String> child1_1_1 = new QueryTreeImpl<String>("E", NodeType.RESOURCE);
        // F
        QueryTreeImpl<String> child1_3_1 = new QueryTreeImpl<String>("F", NodeType.RESOURCE);

        child1_1.addChild(child1_1_1, "s");
        child1_3.addChild(child1_3_1, "t");
        tree1.addChild(child1_1, "p");
        tree1.addChild(child1_2, "q");
        tree1.addChild(child1_3, "r");

        // tree 2:
        // A
        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        // G
        QueryTreeImpl<String> child2_1 = new QueryTreeImpl<String>("G", NodeType.RESOURCE);
        // C
        QueryTreeImpl<String> child2_2 = new QueryTreeImpl<String>("C", NodeType.RESOURCE);
        // H
        QueryTreeImpl<String> child2_3 = new QueryTreeImpl<String>("H", NodeType.RESOURCE);
        // E
        QueryTreeImpl<String> child2_1_1 = new QueryTreeImpl<String>("E", NodeType.RESOURCE);
        // F
        QueryTreeImpl<String> child2_3_1 = new QueryTreeImpl<String>("F", NodeType.RESOURCE);

        child2_1.addChild(child2_1_1, "s");
        child2_3.addChild(child2_3_1, "t");
        tree2.addChild(child2_1, "p");
        tree2.addChild(child2_2, "q");
        tree2.addChild(child2_3, "r");

        double expectedDistance = 2;
        assertEquals(expectedDistance, QueryTreeEditDistance.getDistance(tree1, tree2));
    }

    @Test
    public void test08() {
    	/*
    	 *  A	--p--> ?
    	 *  			--p--> A1
    	 */
    	QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
    	QueryTreeImpl<String> subTree1 = new QueryTreeImpl<String>("?", NodeType.VARIABLE);
    	subTree1.addChild(new QueryTreeImpl<String>("A1", NodeType.RESOURCE), "p");
    	tree1.addChild(subTree1, "p");

    	/*
    	 *  B	--p--> B1
    	 *  			 --p--> A1
    	 */
    	QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("B", NodeType.RESOURCE);
    	QueryTreeImpl<String> subTree2 = new QueryTreeImpl<String>("B1", NodeType.RESOURCE);
    	subTree2.addChild(new QueryTreeImpl<String>("A1", NodeType.RESOURCE), "p");
    	tree2.addChild(subTree2, "p");

    	/*
    	 *  C	--p--> ?
    	 *  			--p--> ?
    	 */
    	QueryTreeImpl<String> tree3 = new QueryTreeImpl<String>("C", NodeType.RESOURCE);
    	QueryTreeImpl<String> subTree3 = new QueryTreeImpl<String>("?", NodeType.VARIABLE);
    	subTree3.addChild(new QueryTreeImpl<String>("?", NodeType.VARIABLE), "p");
    	tree3.addChild(subTree3, "p");

        System.out.println(tree1.getStringRepresentation());
        System.out.println(tree2.getStringRepresentation());
        System.out.println(tree3.getStringRepresentation());

        double distance1_2 = QueryTreeEditDistance.getDistanceApprox(tree1, tree2);
        double distance1_3 = QueryTreeEditDistance.getDistanceApprox(tree1, tree3);
        double distance2_3 = QueryTreeEditDistance.getDistanceApprox(tree2, tree3);

        System.out.println("d(t1,t2) = " + distance1_2);
        System.out.println("d(t1,t3) = " + distance1_3);
        System.out.println("d(t2,t3) = " + distance2_3);


        boolean moreSimilar = distance1_2 < distance1_3;

        assertEquals(distance1_2 < distance1_3, moreSimilar);
    }
}
