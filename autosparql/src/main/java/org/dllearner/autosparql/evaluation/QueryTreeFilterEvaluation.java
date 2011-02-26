package org.dllearner.autosparql.evaluation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.dllearner.autosparql.server.search.QuestionProcessor;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.sparqlquerygenerator.QueryTreeFactory;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.impl.QueryTreeFactoryImpl;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator.Strategy;
import org.dllearner.sparqlquerygenerator.util.QuestionBasedStatementFilter;

import com.hp.hpl.jena.rdf.model.Model;

public class QueryTreeFilterEvaluation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String question = "Give me all European Capitals!";
		String uri = "http://dbpedia.org/resource/Vienna";
		
		System.out.println("Question: \"" + question + "\"");
		System.out.println("Resource: " + uri);
		
		
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
		System.out.println("Tree without filtering:\n" + tree.getStringRepresentation());
		
		treeFactory.setStatementFilter(new QuestionBasedStatementFilter(new HashSet<String>(relevantWords)));
		QueryTree<String> filteredTree = treeFactory.getQueryTree(uri, model);
		System.out.println("Tree with filtering:\n" + filteredTree.getStringRepresentation());

	}

}
