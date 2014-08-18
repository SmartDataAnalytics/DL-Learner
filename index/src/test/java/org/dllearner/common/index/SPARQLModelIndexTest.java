package org.dllearner.common.index;

import static org.junit.Assert.*;
import org.junit.Test;

public class SPARQLModelIndexTest
{

	@Test public void testCreateClassIndex()
	{
		SPARQLModelIndex index = SPARQLModelIndex.createClassIndex("http://linkedgeodata.org/sparql", "http://linkedgeodata.org");
		assertTrue(index.getResources("City").contains("http://linkedgeodata.org/ontology/City"));
	}

	@Test public void testCreatePropertyIndex()
	{
		SPARQLModelIndex index = SPARQLModelIndex.createPropertyIndex("http://linkedgeodata.org/sparql", "http://linkedgeodata.org");
//		System.out.println(index.getResources("Restaurant"));
		assertTrue(index.getResources("Restaurant").contains("http://linkedgeodata.org/ontology/Restaurant"));
	}

}