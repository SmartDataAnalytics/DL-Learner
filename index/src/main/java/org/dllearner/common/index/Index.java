package org.dllearner.common.index;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

public abstract class Index
{
	static final int	DEFAULT_LIMIT	= 10;
		
	String enquote(String s) {if(!s.startsWith("(")) s='('+s+')'; return s;}

	public List<String> getResources(String queryString) {return getResources(queryString,DEFAULT_LIMIT);}
	public List<String> getResources(String queryString, int limit) {return getResources(enquote(queryString),DEFAULT_LIMIT,0);}
	
	public List<String> getResources(String queryString, int limit, int offset)
	{
		List<String> uris = new LinkedList<>();
		for(IndexItem item: getResourcesWithScores(queryString, limit, offset)) {uris.add(item.getUri());} // when upgrade to Java 8, use .stream().map
		return uris;
	}
	
	public SortedSet<IndexItem> getResourcesWithScores(String queryString) {return getResourcesWithScores(queryString,DEFAULT_LIMIT);}
	public SortedSet<IndexItem> getResourcesWithScores(String queryString, int limit) {return getResourcesWithScores(queryString,DEFAULT_LIMIT,0);}
	abstract public SortedSet<IndexItem> getResourcesWithScores(String queryString, int limit, int offset);
}