package org.dllearner.common.index;

import java.util.List;

public abstract class Index
{
	static final int	DEFAULT_LIMIT	= 10;
		
	String enquote(String s) {if(!s.startsWith("(")) s='('+s+')'; return s;}

	public List<String> getResources(String queryString) {return getResources(queryString,DEFAULT_LIMIT);}
	public List<String> getResources(String queryString, int limit) {return getResources(enquote(queryString),DEFAULT_LIMIT,0);}
	abstract public List<String> getResources(String queryString, int limit, int offset);
	
	public IndexResultSet getResourcesWithScores(String queryString) {return getResourcesWithScores(queryString,DEFAULT_LIMIT);}
	public IndexResultSet getResourcesWithScores(String queryString, int limit) {return getResourcesWithScores(queryString,DEFAULT_LIMIT,0);}
	abstract public IndexResultSet getResourcesWithScores(String queryString, int limit, int offset);
}