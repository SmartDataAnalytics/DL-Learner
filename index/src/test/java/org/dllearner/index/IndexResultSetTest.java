package org.dllearner.index;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class IndexResultSetTest
{
	
	private static final String	BERLIN_URI	= "http://dbpedia.org/resource/Berlin";
	private static final String	DRESDEN_URI	= "http://dbpedia.org/resource/Dresden";
	private static final String	LEIPZIG_URI	= "http://dbpedia.org/resource/Leipzig";
	
	IndexItem leipzig2 = new IndexItem(LEIPZIG_URI,"Leipzigig",0.9f);
	IndexItem dresden2 = new IndexItem(DRESDEN_URI,"Dresdne",0.1f);	
	IndexItem berlin = new IndexItem(BERLIN_URI,"Berlin",0.5f);
	
	List<IndexItem> cities = Arrays.asList(new IndexItem[] {leipzig2,dresden2,berlin});
	
	private IndexResultSet testSet()
	{
		IndexResultSet test = new IndexResultSet();
		test.add(new IndexItem(LEIPZIG_URI,"Leipzig",0.4f));
		test.add(new IndexItem(DRESDEN_URI,"Dresden",0.5f));
		return test;
	}

	@Test public void testClear()
	{
		IndexResultSet test = testSet();
		test.clear();
		assertTrue(test.isEmpty());
		assertTrue(test.uriToItem.isEmpty());
	}

	@Test public void testContainsString()
	{
		IndexResultSet test = testSet();
		assertTrue(test.contains(LEIPZIG_URI));
		assertFalse(test.contains(BERLIN_URI));
	}

	@Test public void testContainsIndexItem()
	{
		IndexResultSet test = testSet();
		assertTrue(test.contains(leipzig2));
		assertFalse(test.contains(berlin));
	}

	@Test public void testContainsObject()
	{
		IndexResultSet test = testSet();
		assertTrue(test.contains((Object)leipzig2));
		assertFalse(test.contains((Object)berlin));
		assertTrue(test.contains((Object)LEIPZIG_URI));
		assertFalse(test.contains((Object)BERLIN_URI));
	}

	@Test public void testGetString()
	{
		IndexResultSet test = testSet();
		assertNotNull(test.get(LEIPZIG_URI));
		assertNull(test.get(BERLIN_URI));
	}

	@Test public void testGetIndexItem()
	{
		IndexResultSet test = testSet();
		assertNotNull(test.get(leipzig2));
		assertNull(test.get(berlin));
	}

	@Test public void testAddIndexItem()
	{
		IndexResultSet test = testSet();
		assertFalse(test.contains(BERLIN_URI));
		test.add(berlin);
		assertTrue(test.contains(BERLIN_URI));
	}

	@Test public void testRemoveString()
	{
		IndexResultSet test = testSet();
		assertTrue(test.contains(LEIPZIG_URI));
		test.remove(LEIPZIG_URI);
		assertFalse(test.contains(LEIPZIG_URI));
	}

	@Test public void testRemoveIndexItem()
	{
		IndexResultSet test = testSet();
		assertTrue(test.contains(leipzig2));
		test.remove(leipzig2);
		assertFalse(test.contains(leipzig2));
	}

	@Test public void testRemoveObject()
	{
		IndexResultSet test = testSet();
		assertTrue(test.contains(leipzig2));
		assertTrue(test.contains(dresden2));
		test.remove((Object)leipzig2);
		test.remove((Object)DRESDEN_URI);
		assertFalse(test.contains(leipzig2));
		assertFalse(test.contains(dresden2));		
	}

	@Test public void testRemoveAllCollection()
	{
		IndexResultSet test = new IndexResultSet();
		test.removeAll(cities);
		assertTrue(test.isEmpty());
		assertFalse(test.contains(leipzig2));
	}

	@Test public void testAddAll()
	{
		IndexResultSet test = new IndexResultSet();
		test.addAll(cities);
		assertTrue(test.contains(leipzig2));
		assertTrue(test.contains(berlin));
		assertTrue(test.contains(dresden2));
	}

//	@Test public void testIterator()
//	{
//		IndexResultSet test = testSet();
//		Iterator<IndexItem> it = test.iterator();
//		assertTrue(it.hasNext());
//	}
	
//	@Test public void testHashCode()
//	{
//		fail("Not yet implemented");
//	}
//
//	@Test public void testEquals()
//	{
//		fail("Not yet implemented");
//	}
}