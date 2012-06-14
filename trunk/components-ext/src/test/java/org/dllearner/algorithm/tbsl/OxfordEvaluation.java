package org.dllearner.algorithm.tbsl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner2;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.VirtuosoClassesIndex;
import org.dllearner.common.index.VirtuosoPropertiesIndex;
import org.dllearner.common.index.VirtuosoResourcesIndex;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class OxfordEvaluation {
	
	private static final String QUERIES_FILE = OxfordEvaluation.class.getClassLoader().getResource("tbsl/evaluation.txt").getPath();
	
	public static void main(String[] args) throws Exception{
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());
		ExtractionDBCache cache = new ExtractionDBCache("cache");
		
		SPARQLIndex resourcesIndex = new VirtuosoResourcesIndex(endpoint, cache);
		SPARQLIndex classesIndex = new VirtuosoClassesIndex(endpoint, cache);
		SPARQLIndex propertiesIndex = new VirtuosoPropertiesIndex(endpoint, cache);
		MappingBasedIndex mappingIndex= new MappingBasedIndex(
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_class_mappings.txt").getPath(), 
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_resource_mappings.txt").getPath(),
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_dataproperty_mappings.txt").getPath(),
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_objectproperty_mappings.txt").getPath()
				);
		
		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(endpoint, resourcesIndex, classesIndex, propertiesIndex);
		learner.setMappingIndex(mappingIndex);
		learner.init();
		
		int learnedQuestions = 0;
		Map<String, String> question2QueryMap = new HashMap<String, String>();
		
		BufferedReader in = new BufferedReader(new FileReader(new File(QUERIES_FILE)));
		BufferedWriter out = new BufferedWriter(new FileWriter(new File("log/oxford_eval.txt")));
		
		int questionNr = 0;
		int errorCnt = 0;
		int noQueryCnt = 0;
		String question = null;
		while((question = in.readLine()) != null){
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
					out.write("****************************************\n" + question + "\n" + learnedQuery + "\n****************************************");
				} else {
					noQueryCnt++;
					out.write("****************************************\n" + question + "\nNO QUERY WITH NON-EMPTY RESULTSET FOUND\n****************************************");
				}
			} catch (Exception e) {
				e.printStackTrace();
				errorCnt++;
				out.write("****************************************\n" + question + "\nERROR: " + e.getClass() + "\n****************************************");
			}
			out.flush();
		}
		out.write("################################\n");
		out.write("Questions with answer: " + learnedQuestions + "\n");
		out.write("Questions with no answer (and no error): " + noQueryCnt + "\n");
		out.write("Questions with error: " + errorCnt + "\n");
		
		in.close();
		out.close();
		

	}

}
