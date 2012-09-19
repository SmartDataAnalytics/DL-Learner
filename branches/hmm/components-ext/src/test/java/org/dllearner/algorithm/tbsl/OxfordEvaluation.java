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

import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner2;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner3Test;
import org.dllearner.algorithm.tbsl.nlp.StanfordPartOfSpeechTagger;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.VirtuosoClassesIndex;
import org.dllearner.common.index.VirtuosoPropertiesIndex;
import org.dllearner.common.index.VirtuosoResourcesIndex;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class OxfordEvaluation {
	
	private static final String QUERIES_FILE = OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_eval_queries.txt").getPath();
	private static final String LOG_DIRECTORY = "log/oxford/";
	private static final String LOG_FILE = "evaluation.txt";
	
	public static void main(String[] args) throws Exception{
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());
		ExtractionDBCache cache = new ExtractionDBCache("cache");
		new File(LOG_DIRECTORY).mkdirs();
		
		SPARQLIndex resourcesIndex = new VirtuosoResourcesIndex(endpoint, cache);
		SPARQLIndex classesIndex = new VirtuosoClassesIndex(endpoint, cache);
		SPARQLIndex propertiesIndex = new VirtuosoPropertiesIndex(endpoint, cache);
		MappingBasedIndex mappingIndex= new MappingBasedIndex(
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_class_mappings.txt").getPath(), 
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_resource_mappings.txt").getPath(),
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_dataproperty_mappings.txt").getPath(),
				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_objectproperty_mappings.txt").getPath()
				);
		
//		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(endpoint, resourcesIndex, classesIndex, propertiesIndex);
		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2
				(SPARQLTemplateBasedLearner3Test.loadOxfordModel(),SPARQLTemplateBasedLearner3Test.getOxfordMappingIndex(), new StanfordPartOfSpeechTagger());
		
		learner.setMappingIndex(mappingIndex);
		learner.init();
		learner.setGrammarFiles(new String[]{"tbsl/lexicon/english.lex","tbsl/lexicon/english_oxford.lex"});
		
		int learnedQuestions = 0;
		Map<String, String> question2QueryMap = new HashMap<String, String>();
		
		Monitor mon = MonitorFactory.getTimeMonitor("tbsl");
		
		BufferedReader in = new BufferedReader(new FileReader(new File(QUERIES_FILE)));
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(LOG_DIRECTORY + LOG_FILE), false));
		BufferedWriter answerOut = new BufferedWriter(new FileWriter(new File(LOG_DIRECTORY + "questionsWithAnswer.txt"), false));
		BufferedWriter noAnswerOut = new BufferedWriter(new FileWriter(new File(LOG_DIRECTORY + "questionsWithNoAnswer.txt"), false));
		BufferedWriter templatesOut = new BufferedWriter(new FileWriter(new File(LOG_DIRECTORY + "questionsWithTemplate.txt"), false));
		BufferedWriter noTemplatesOut = new BufferedWriter(new FileWriter(new File(LOG_DIRECTORY + "questionsWithNoTemplate.txt"), false));
		
		int questionCnt = 0;
		int errorCnt = 0;
		int noTemplateFoundCnt = 0;
		int noQueryWithNonEmptyResultSetCnt = 0;
		String question = null;
		while((question = in.readLine()) != null){
			question = question.replace("question:", "").trim();
			if(question.isEmpty() || question.startsWith("//")) continue;
			//if(!question.toLowerCase().contains("Give me all") && Character.isLowerCase(question.charAt(0))){
			//	question = "Give me all " + question;
			//}
			System.out.println("########################################################");
			questionCnt++;
			System.out.println(question);
			try {
				out.write("****************************************\n");
				out.write("QUESTION: " + question + "\n");
				learner.setQuestion(question);
				mon.start();
				learner.learnSPARQLQueries();
				mon.stop();
				String learnedQuery = learner.getBestSPARQLQuery();
				if(learnedQuery != null){
					question2QueryMap.put(question, learnedQuery);
					learnedQuestions++;
					out.write("ANSWER FOUND: YES\n");
					out.write(learnedQuery + "\n");
					
					answerOut.write("****************************************\n");
					answerOut.write("QUESTION: " + question + "\n");
					answerOut.write("ANSWER FOUND: YES\n");
					answerOut.write(learnedQuery + "\n");
					answerOut.write("TIME NEEDED: " + mon.getLastValue() + "ms\n");
					answerOut.flush();
					
				} else {
					noQueryWithNonEmptyResultSetCnt++;
					out.write("ANSWER FOUND: NO\n");
					out.write("REASON: NO SPARQL QUERY WITH NON-EMPTY RESULTSET FOUND\n");
					out.write("SPARQL QUERY WITH HIGHEST SCORE TESTED:\n" + learner.getGeneratedQueries().first());
					
					noAnswerOut.write("****************************************\n");
					noAnswerOut.write("QUESTION: " + question + "\n");
					noAnswerOut.write("ANSWER FOUND: NO\n");
					noAnswerOut.write("REASON: NO SPARQL QUERY WITH NON-EMPTY RESULTSET FOUND\n");
					noAnswerOut.write("SPARQL QUERY WITH HIGHEST SCORE TESTED:\n" + learner.getGeneratedQueries().first() + "\n");
					noAnswerOut.write("TIME NEEDED: " + mon.getLastValue() + "ms\n");
					noAnswerOut.flush();
					
				}
				templatesOut.write(question + "\n");
				templatesOut.flush();
			} catch (Exception e) {
				mon.stop();
				e.printStackTrace();
				out.write("ANSWER FOUND: NO\n");
				if(e instanceof NoTemplateFoundException){
					noTemplateFoundCnt++;
					out.write("REASON: NO TEMPLATE FOUND");
					noTemplatesOut.write(question + "\n");
					noTemplatesOut.flush();
				} else {
					errorCnt++;
					out.write("REASON: ERROR OCCURED (" + e.getClass() + ")\n");
					if(e instanceof QueryExceptionHTTP || e instanceof QueryParseException){
						out.write("\nLast tested SPARQL query: " + learner.getCurrentlyExecutedQuery());
					}
				}
				
			} catch (Error e){
				mon.stop();
				e.printStackTrace();
				out.write("ANSWER FOUND: NO\n");
				errorCnt++;
				out.write("REASON: ERROR OCCURED (" + e.getClass() + ")\n");
			}
			out.write("\n****************************************");
			out.flush();
		}
		out.write("\n\n###################SUMMARY################\n");
		out.write("Questions tested:\t" + questionCnt + "\n");
		out.write("Overall time:\t" + mon.getTotal() + "ms\n");
		out.write("Avg. time per question:\t" + mon.getAvg() + "ms\n");
		out.write("Longest time:\t" + mon.getMax() + "ms\n");
		out.write("Shortest time:\t" + mon.getMin() + "ms\n");
		out.write("Questions with answer:\t" + learnedQuestions + "\n");
		out.write("Questions with no answer (and no error):\t" + noQueryWithNonEmptyResultSetCnt + "\n");
		out.write("Questions with no templates:\t" + noTemplateFoundCnt + "\n");
		out.write("Questions with other errors:\t" + errorCnt + "\n");
		
		in.close();
		out.close();
		templatesOut.close();
		noTemplatesOut.close();
		

	}

}
