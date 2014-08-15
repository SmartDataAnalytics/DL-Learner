package org.dllearner.common.index;

import org.junit.Test;

public class SPARQLModelIndexTest
{

	@Test public void testCreateClassIndex()
	{
		SPARQLModelIndex index = SPARQLModelIndex.createClassIndex("http://lodstats.aksw.org/sparql");
		System.out.println(index.getResources("City"));
		System.out.println(index.getResources("Citty"));
	}

}
