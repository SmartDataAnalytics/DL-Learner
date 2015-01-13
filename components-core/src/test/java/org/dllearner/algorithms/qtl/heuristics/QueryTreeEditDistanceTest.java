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
}
