package org.dllearner.autosparql.evaluation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dllearner.autosparql.server.search.QuestionProcessor;
import org.dllearner.autosparql.server.util.TreeHelper;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.sparqlquerygenerator.QueryTreeFactory;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.impl.QueryTreeFactoryImpl;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator.Strategy;
import org.dllearner.sparqlquerygenerator.util.QuestionBasedQueryTreeFilter;
import org.dllearner.sparqlquerygenerator.util.QuestionBasedStatementFilter;
import org.dllearner.sparqlquerygenerator.util.QuestionBasedStatementSelector;

import com.hp.hpl.jena.rdf.model.Model;

public class QueryTreeFilterEvaluation {
	
	private static double THRESHOLD = 0.4;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String question = "Give me all soccer clubs in the Premier League.";//"Give me all European Capitals!";
		String uri = "http://dbpedia.org/resource/Fulham_F.C.";//"http://dbpedia.org/resource/Vienna";
		
		System.out.println("Question: \"" + question + "\"");
		System.out.println("Resource: " + uri);
		
		String baseURI = "http://dbpedia.org/resource/";
		Map<String,String> prefixes = new HashMap<String,String>();
		prefixes.put("dbo","http://dbpedia.org/ontology/");
		prefixes.put("dbprop","http://dbpedia.org/property/");
		prefixes.put("rdfs","http://www.w3.org/2000/01/rdf-schema#");
		prefixes.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.put("skos","http://www.w3.org/2004/02/skos/core#");
		prefixes.put("geo","http://www.w3.org/2003/01/geo/wgs84_pos#");
		prefixes.put("georss","http://www.georss.org/georss/");
		prefixes.put("owl","http://www.w3.org/2002/07/owl#");
		prefixes.put("yago","http://dbpedia.org/class/yago/");
		prefixes.put("cyc","http://sw.opencyc.org/concept/");
		prefixes.put("foaf","http://xmlns.com/foaf/0.1/");
		
		
		QueryTreeFactory<String> treeFactory = new QueryTreeFactoryImpl();
		QuestionProcessor qProcessor = new QuestionProcessor();
		//filter used in CONSTRUCT query
		List<String> predicateFilters = Arrays.asList("http://dbpedia.org/ontology/wikiPageWikiLink",
				"http://dbpedia.org/property/wikiPageUsesTemplate", "http://dbpedia.org/ontology/wikiPageExternalLink",
				"http://dbpedia.org/property/reference");
		ModelGenerator modelGen = new ModelGenerator(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), 
				new HashSet<String>(predicateFilters), new ExtractionDBCache("construct-cache"));
		
		List<String> relevantWords = qProcessor.getRelevantWords(question);
		System.out.println("Extracted relevant words: " + relevantWords);
		
		Model model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
		
		QueryTree<String> tree = treeFactory.getQueryTree(uri, model);
		System.out.println("Tree without filtering:\n" + TreeHelper.getAbbreviatedTreeRepresentation(tree, baseURI, prefixes));
		
		QuestionBasedStatementFilter filter = new QuestionBasedStatementFilter(new HashSet<String>(relevantWords));
		filter.setThreshold(THRESHOLD);
		treeFactory.setStatementFilter(filter);
		
		QueryTree<String> filteredTree = treeFactory.getQueryTree(uri, model);
		System.out.println("Tree with filtering before creation:\n" + TreeHelper.getAbbreviatedTreeRepresentation(filteredTree, baseURI, prefixes));
		
//		QuestionBasedQueryTreeFilter treeFilter = new QuestionBasedQueryTreeFilter(new HashSet<String>(relevantWords));
//		filteredTree = treeFilter.getFilteredQueryTree(filteredTree);
//		System.out.println("Tree with filtering after creation:\n" + TreeHelper.getAbbreviatedTreeRepresentation(filteredTree, baseURI, prefixes));

	}

}
