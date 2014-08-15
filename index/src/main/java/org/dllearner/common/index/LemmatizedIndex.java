//package org.dllearner.common.index;
//
//import java.util.List;
//
//import org.aksw.autosparql.commons.nlp.lemma.StanfordLemmatizer;
//import org.aksw.autosparql.commons.nlp.pling.PlingStemmer;
//import org.dllearner.common.index.Index;
//import org.dllearner.common.index.IndexResultSet;
//
///** Index decorator that uses tries lemmatizing on the input in case of failure to find.**/
//// TODO somehow get sentence for context or even better tag in the index
//// Options: set map from word to tag -> disadvantage: state based, bad for multithreading
//public class LemmatizedIndex extends Index
//{
//	protected Index index;
//	static StanfordLemmatizer lemmatizer = new StanfordLemmatizer();
//	
//	public LemmatizedIndex(Index index) {this.index=index;}
//	
//	public static String lemmatize(String s)
//	{return lemmatizer.stem(s);}
//
//	@Override public List<String> getResources(String queryString, int limit, int offset)
//	{
//		List<String> resources = index.getResources(queryString, limit, offset);
//		if(offset==0&&resources.isEmpty()) {resources = index.getResources(lemmatize(queryString), limit, offset);}
//		return resources;
//	}
//
//	@Override public IndexResultSet getResourcesWithScores(String queryString, int limit, int offset)
//	{
//		IndexResultSet rs = index.getResourcesWithScores(queryString, limit, offset);
//		if(offset==0&&rs.isEmpty()) {rs = index.getResourcesWithScores(PlingStemmer.stem(queryString), limit, offset);}
//		return rs;
//	}
//
//}