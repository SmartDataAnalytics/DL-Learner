package org.dllearner.index;

import java.util.LinkedList;
import java.util.List;

public abstract class Index
{
	static final int	DEFAULT_LIMIT	= 10;
		
	String enquote(String s) {return s;}//dont remember what this was used for {if(!s.startsWith("(")) s='('+s+')'; return s;}

	public final List<String> getResources(String queryString) {return getResources(queryString,DEFAULT_LIMIT);}
	
	public final List<String> getResources(String queryString, int limit)
	{
		List<String> uris = new LinkedList<>();
		for(IndexItem item: getResourcesWithScores(enquote(queryString), limit)) {uris.add(item.getUri());} // when upgrade to Java 8, use .stream().map
		return uris;
	}
	
	public final IndexResultSet getResourcesWithScores(String queryString) {return getResourcesWithScores(queryString,DEFAULT_LIMIT);}
	public abstract IndexResultSet getResourcesWithScores(String queryString, int limit);
}