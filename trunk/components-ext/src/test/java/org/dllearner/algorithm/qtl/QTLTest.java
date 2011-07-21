package org.dllearner.algorithm.qtl;

import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithm.qtl.exception.EmptyLGGException;
import org.dllearner.algorithm.qtl.exception.NegativeTreeCoverageExecption;
import org.dllearner.algorithm.qtl.exception.TimeOutException;
import org.dllearner.algorithm.qtl.filters.QuestionBasedQueryTreeFilterAggressive;
import org.dllearner.algorithm.qtl.filters.QuestionBasedStatementFilter;
import org.dllearner.algorithm.qtl.operations.NBR;
import org.dllearner.algorithm.qtl.operations.PostLGG;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

import scala.actors.threadpool.Arrays;

public class QTLTest {
	
	public static void main(String[] args) throws EmptyLGGException, NegativeTreeCoverageExecption, TimeOutException {
		Logger.getLogger(NBR.class).setLevel(Level.DEBUG);
		Logger.getLogger(PostLGG.class).setLevel(Level.DEBUG);
		List<String> predicateFilters = Arrays.asList(new String[]{"http://dbpedia.org/ontology/wikiPageWikiLink",
				"http://dbpedia.org/ontology/wikiPageExternalLink", "http://dbpedia.org/property/wikiPageUsesTemplate"});
		SPARQLEndpointEx endpoint = new SPARQLEndpointEx(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), "", "", new HashSet<String>(predicateFilters));
		
		QTL qtl = new QTL(endpoint, new ExtractionDBCache("cache"));
		
//		List<String> relevantWords = Arrays.asList(new String[]{"film", "star", "Brad Pitt"});
//		List<String> posExamples = Arrays.asList(new String[]{
//				"http://dbpedia.org/resource/Interview_with_the_Vampire:_The_Vampire_Chronicles",
//				"http://dbpedia.org/resource/Megamind"});
//		List<String> negExamples = Arrays.asList(new String[]{"http://dbpedia.org/resource/Shukriya:_Till_Death_Do_Us_Apart"});
		
		List<String> relevantWords = Arrays.asList(new String[]{"soccer club", "Premier League"});
		List<String> posExamples = Arrays.asList(new String[]{
				"http://dbpedia.org/resource/Arsenal_F.C.",
				"http://dbpedia.org/resource/Chelsea_F.C."});
		List<String> negExamples = Arrays.asList(new String[]{});
		
		QuestionBasedStatementFilter stmtFilter = new QuestionBasedStatementFilter(new HashSet<String>(relevantWords));
		qtl.addStatementFilter(stmtFilter);
		
		QuestionBasedQueryTreeFilterAggressive treeFilter = new QuestionBasedQueryTreeFilterAggressive(new HashSet<String>(relevantWords));
		qtl.addQueryTreeFilter(treeFilter);
		
		
		
		
		String suggestion = qtl.getQuestion(posExamples, negExamples);
		System.out.println(suggestion);
	}

}
