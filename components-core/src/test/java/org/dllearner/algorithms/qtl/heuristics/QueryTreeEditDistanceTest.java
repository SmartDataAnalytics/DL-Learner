package org.dllearner.algorithms.qtl.heuristics;

import static junit.framework.Assert.assertEquals;

import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.junit.Test;

public class QueryTreeEditDistanceTest {
    @Test
    public <T> void test01() {
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        double expectedDistance = 0;
        assertEquals(QueryTreeEditDistance.getDistance(tree1, tree2), expectedDistance);
    }

    @Test
    public <T> void test02() {
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A1", NodeType.RESOURCE);
        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("A2", NodeType.RESOURCE);
        double expectedDistance = 1;
        assertEquals(QueryTreeEditDistance.getDistance(tree1, tree2), expectedDistance);
    }

    @Test
    public void test03() {
        QueryTreeImpl<String> subtree1 = new QueryTreeImpl<String>("B", NodeType.RESOURCE);
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        tree1.addChild(subtree1);

        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);

        double expectedDistance = 1;
        assertEquals(QueryTreeEditDistance.getDistance(tree1, tree2), expectedDistance);
    }

    @Test
    public void test04() {
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);

        QueryTreeImpl<String> subtree2 = new QueryTreeImpl<String>("B", NodeType.RESOURCE);
        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        tree2.addChild(subtree2);

        double expectedDistance = 1;
        assertEquals(QueryTreeEditDistance.getDistance(tree1, tree2), expectedDistance);
    }
    
    @Test
    public void test05() {
    	QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);

        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("B", NodeType.RESOURCE);
        tree2.addChild(new QueryTreeImpl<String>("B1", NodeType.RESOURCE), "p");
        
        double distance1_2 = QueryTreeEditDistance.getDistanceApprox(tree1, tree2);
        double distance2_1 = QueryTreeEditDistance.getDistanceApprox(tree2, tree1);
        
        assertEquals(distance1_2, distance2_1);
    }
    
    @Test
    public void test06() {
    	QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);

        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("B", NodeType.RESOURCE);
        tree2.addChild(new QueryTreeImpl<String>("B1", NodeType.RESOURCE), "p");
        
        QueryTreeImpl<String> tree3 = new QueryTreeImpl<String>("C", NodeType.RESOURCE);
        tree3.addChild(new QueryTreeImpl<String>("C1", NodeType.RESOURCE), "p");
        tree3.addChild(new QueryTreeImpl<String>("C2", NodeType.RESOURCE), "p");

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
    
    @Test
    public void test07() {
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
