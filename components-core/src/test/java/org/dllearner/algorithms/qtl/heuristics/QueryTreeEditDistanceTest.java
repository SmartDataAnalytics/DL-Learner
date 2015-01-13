package org.dllearner.algorithms.qtl.heuristics;

import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.junit.Test;

public class QueryTreeEditDistanceTest {

    @Test
    public <T> void test1() {
        QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
        QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?");


    }

}
