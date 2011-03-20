package org.dllearner.autosparql.evaluation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dllearner.algorithm.qtl.QueryTreeFactory;
import org.dllearner.algorithm.qtl.datastructures.QueryTree;
import org.dllearner.algorithm.qtl.filters.QuestionBasedQueryTreeFilterAggressive;
import org.dllearner.algorithm.qtl.filters.QuestionBasedStatementFilter;
import org.dllearner.algorithm.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithm.qtl.util.ModelGenerator;
import org.dllearner.algorithm.qtl.util.ModelGenerator.Strategy;
import org.dllearner.algorithm.qtl.util.TreeHelper;
import org.dllearner.autosparql.server.search.QuestionProcessor;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.clarkparsia.owlapiv3.XSD;
import com.hp.hpl.jena.rdf.model.Model;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import de.simba.ner.WordnetQuery;
import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class QueryTreeFilterEvaluation {
	
	private static double THRESHOLD = 0.6;
//	private static String ENDPOINT_URL = "http://db0.aksw.org:8999/sparql";
//	private static String ENDPOINT_URL = "http://lod.openlinksw.com/sparql";
	private static String ENDPOINT_URL = "http://live.dbpedia.org/sparql";
	

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
//		String question = "List all episodes of the first season of the HBO television series The Sopranos!";//"Give me all soccer clubs in the Premier League.";//"Give me all European Capitals!";
//		String uri = "http://dbpedia.org/resource/46_Long";//"http://dbpedia.org/resource/Fulham_F.C.";//"http://dbpedia.org/resource/Vienna";
//		String question = "Which software has been developed by organizations in California?";//"Give me all soccer clubs in the Premier League.";//"Give me all European Capitals!";
//		String uri = "http://dbpedia.org/resource/University_of_California,_Berkeley";//"http://dbpedia.org/resource/Fulham_F.C.";//"http://dbpedia.org/resource/Vienna";
//		String question = "In which states of Germany reigns the political party CDU?";
//		String uri = "http://dbpedia.org/resource/Thuringia";
		String question = "Which people have as their given name Jimmy?";
		String uri = "http://dbpedia.org/resource/Jimmy_Coogan";
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
		
		Monitor mon = MonitorFactory.getTimeMonitor("Tree creation");
		QueryTreeFactory<String> treeFactory = new QueryTreeFactoryImpl();
		QuestionProcessor qProcessor = new QuestionProcessor();
		//filter used in CONSTRUCT query
		List<String> predicateFilters = Arrays.asList("http://dbpedia.org/ontology/wikiPageWikiLink",
				"http://dbpedia.org/property/wikiPageUsesTemplate", "http://dbpedia.org/ontology/wikiPageExternalLink",
				"http://dbpedia.org/property/reference");
		ModelGenerator modelGen = new ModelGenerator(
				new SparqlEndpoint(new URL(ENDPOINT_URL), Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList()), 
				new HashSet<String>(predicateFilters), new ExtractionDBCache("construct-cache2"));
		
		List<String> relevantWords = qProcessor.getRelevantWords(question);
		relevantWords.add("1");
		System.out.println("Extracted relevant words: " + relevantWords);
		WordnetQuery wq = new WordnetQuery("src/main/resources/de/simba/ner/dictionary");
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Set<String> synset = new HashSet<String>();
		NounSynset nounSynset;
		for(String w : relevantWords){
		        Synset[] synsets = database.getSynsets(w, SynsetType.NOUN);
		        for (int i = 0; i < synsets.length; i++) {
		            nounSynset = (NounSynset) (synsets[i]);
		            for(int j=0; j < nounSynset.getMemberMeronyms().length; j++)
		            {
		            	for(String s : nounSynset.getMemberMeronyms()[j].getWordForms()){
		            		synset.add(s);
		            	}
		            	
		            }
		        }
		}relevantWords.addAll(synset);
		System.out.println(synset);
		
		Model model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
		model.createResource("http://dbpedia.org/resource/46_Long").
		addProperty(model.createProperty("http://dbpedia.org/ontology/seasonNumber"), "1", XSD.INT.toStringID());
		
		
		mon.start();
		QueryTree<String> tree = treeFactory.getQueryTree(uri, model);
		mon.stop();
		System.out.println("Tree without filtering(" + mon.getLastValue() + "ms):\n" + TreeHelper.getAbbreviatedTreeRepresentation(tree, baseURI, prefixes));
		
		QuestionBasedStatementFilter filter = new QuestionBasedStatementFilter(new HashSet<String>(relevantWords));
		filter.setThreshold(THRESHOLD);
		treeFactory.setStatementFilter(filter);
		
		mon.start();
		QueryTree<String> filteredTree = treeFactory.getQueryTree(uri, model);
		mon.stop();
		System.out.println("Tree with filtering before creation(" + mon.getLastValue() + "ms):\n" + TreeHelper.getAbbreviatedTreeRepresentation(filteredTree, baseURI, prefixes));
		
//		filteredTree = treeFactory.getQueryTree(uri, model, 10);
//		System.out.println("Tree with dynamic filtering before creation:\n" + TreeHelper.getAbbreviatedTreeRepresentation(filteredTree, baseURI, prefixes));
		
		QuestionBasedQueryTreeFilterAggressive treeFilter = new QuestionBasedQueryTreeFilterAggressive(new HashSet<String>(relevantWords));
		filteredTree = treeFilter.getFilteredQueryTree(filteredTree);
		System.out.println("Tree with filtering after creation:\n" + TreeHelper.getAbbreviatedTreeRepresentation(filteredTree, baseURI, prefixes));

	}

}
