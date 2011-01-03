package org.dllearner.autosparql.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dllearner.autosparql.client.model.Example;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.sparqlquerygenerator.QueryTreeFactory;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.impl.QueryTreeFactoryImpl;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGenerator;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator;
import org.dllearner.sparqlquerygenerator.util.ModelGenerator.Strategy;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class NBRTest {
	
	private static final String CACHE_DIR = "cache";
	
	@Test
	public void test1(){
		try {
			ExtractionDBCache cache = new ExtractionDBCache(CACHE_DIR);
			SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://db0.aksw.org:8999/sparql"),
					Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
			Set<String> predicateFilters = new HashSet<String>();
			predicateFilters.add("http://dbpedia.org/ontology/wikiPageWikiLink");
			predicateFilters.add("http://dbpedia.org/property/wikiPageUsesTemplate");
			
			ModelGenerator modelGen = new ModelGenerator(endpoint, predicateFilters, cache);
			QueryTreeFactory<String> treeFactory = new QueryTreeFactoryImpl();
			LGGGenerator<String> lggGen = new LGGGeneratorImpl<String>();
			NBR<String> nbrGen = new NBR<String>(endpoint, cache);
			
			List<QueryTree<String>> posTrees = new ArrayList<QueryTree<String>>();
			List<QueryTree<String>> negTrees = new ArrayList<QueryTree<String>>();
			List<String> knownResources = new ArrayList<String>();
			
			String uri = "http://dbpedia.org/resource/Foals";
			Model model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
			QueryTree<String> tree = treeFactory.getQueryTree(uri, model);
			posTrees.add(tree);
			
			uri = "http://dbpedia.org/resource/Hot_Chip";
			model = modelGen.createModel(uri, Strategy.CHUNKS, 2);
			tree = treeFactory.getQueryTree(uri, model);
			negTrees.add(tree);
			
			QueryTree<String> lgg = lggGen.getLGG(posTrees);
			
			Example example = nbrGen.getQuestion(lgg, negTrees, knownResources);
			
			
			
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	}

}
