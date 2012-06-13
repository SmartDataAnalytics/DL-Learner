package org.dllearner.algorithm.tbsl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner2;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.common.index.SPARQLClassesIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.SPARQLPropertiesIndex;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class OxfordEvaluation {
	
	private static final String QUERIES_FILE = "/home/lorenz/evaluation.txt";
	
	public static void main(String[] args) throws Exception{
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());
		ExtractionDBCache cache = new ExtractionDBCache("cache");
		
		SPARQLIndex resourcesIndex = new SPARQLIndex(endpoint, cache);
		SPARQLIndex classesIndex = new SPARQLClassesIndex(endpoint, cache);
		SPARQLIndex propertiesIndex = new SPARQLPropertiesIndex(endpoint, cache);
		MappingBasedIndex mappingIndex= new MappingBasedIndex(
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_class_mappings.txt").getPath(), 
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_resource_mappings.txt").getPath(),
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_objectproperty_mappings.txt").getPath(),
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_dataproperty_mappings.txt").getPath());
		
		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(endpoint, resourcesIndex, classesIndex, propertiesIndex);
		learner.setMappingIndex(mappingIndex);
		learner.init();
		
		int learnedQuestions = 0;
		Map<String, String> question2QueryMap = new HashMap<String, String>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(QUERIES_FILE)));
		
		int questionNr = 0;
		String question = null;
		while((question = br.readLine()) != null){
			question = question.replace("question:", "").trim();
			if(question.isEmpty()) continue;
			if(!question.toLowerCase().contains("Give me all") && Character.isLowerCase(question.charAt(0))){
				question = "Give me all " + question;
			}
			System.out.println("########################################################");
			questionNr++;
			System.out.println(question);
			try {
				learner.setQuestion(question);
				learner.learnSPARQLQueries();
				String learnedQuery = learner.getBestSPARQLQuery();
				if(learnedQuery != null){
					question2QueryMap.put(question, learnedQuery);
					learnedQuestions++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Generated SPARQL queries for " + learnedQuestions + " questions.");
		for(Entry<String, String> entry : question2QueryMap.entrySet()){
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
		}
	}

}
