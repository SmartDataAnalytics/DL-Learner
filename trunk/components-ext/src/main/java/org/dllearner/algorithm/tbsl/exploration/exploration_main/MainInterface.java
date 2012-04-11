package org.dllearner.algorithm.tbsl.exploration.exploration_main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.dllearner.algorithm.tbsl.exploration.Index.SQLiteIndex;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Template;
import org.dllearner.algorithm.tbsl.exploration.Sparql.TemplateBuilder;
import org.dllearner.algorithm.tbsl.exploration.Utils.LinearSort;
import org.dllearner.algorithm.tbsl.exploration.Utils.Query;
import org.dllearner.algorithm.tbsl.exploration.Utils.QueryPair;
import org.dllearner.algorithm.tbsl.exploration.Utils.ServerUtil;
import org.dllearner.algorithm.tbsl.exploration.modules.IterationModule;
import org.dllearner.algorithm.tbsl.nlp.StanfordLemmatizer;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.templator.BasicTemplator;

public class MainInterface {
	private static int anzahlAbgeschickterQueries = 10;
	
	
	public static ArrayList<String> startQuestioning(String question,BasicTemplator btemplator,SQLiteIndex myindex, WordNet wordnet,StanfordLemmatizer lemmatiser) throws ClassNotFoundException, SQLException, IOException{
		
		TemplateBuilder templateObject = new TemplateBuilder(btemplator, myindex);
		ArrayList<Template> template_list = new ArrayList<Template>();
		
		/*
		 * Array List with the answers from the queries
		 */
		ArrayList<String> answers = new ArrayList<String>();
		
		
		/*
		 * generate Templates!
		 */
		template_list=templateObject.createTemplates(question);
		
		/*
		 * generate Queries and test the first Time
		 */
		ArrayList<QueryPair> qp = new ArrayList<QueryPair>();
		
		//generate QueryPair
		for(Template t : template_list){
			t.printAll();
			ArrayList<QueryPair> qp_t = new ArrayList<QueryPair>();
			qp_t = Query.returnSetOfQueries(t, "NORMAL");
			for(QueryPair p : qp_t){
				//if(!qp.contains(p)) qp.add(p);
				boolean contain = false;
				for(QueryPair p_t : qp){
					if(p_t.getRank()==p.getRank()){
						if(p_t.getQuery().contains(p.getQuery())) contain=true;
					}
				}
				if(!contain)qp.add(p);
			}
		}
		
		//sort QueryPairs
		LinearSort.doSort(qp);	
		int anzahl=1;
		boolean go_on = true;
		for(QueryPair q : qp){
			if(anzahl<anzahlAbgeschickterQueries&go_on){
				ArrayList<String> answer_tmp = new ArrayList<String>();
				System.out.println(q.getQuery());
				answer_tmp=ServerUtil.requestAnswerFromServer(q.getQuery());
				if(answer_tmp.isEmpty()) go_on=true;

				else{
					//else go_on=false;
					go_on=true;
					for(String s : answer_tmp){
						if(checkAnswer(s)){
							boolean test = false;
							for(String s_t : answers){
								if(s_t.contains(s)) test=true;
							}
							if(!test)answers.add(s);
						}
					}
					//if(checkAnswer(answer_tmp))answers.addAll(answer_tmp);
				}
			}
			anzahl+=1;
		}
		
		for(String answer:answers){
			System.out.println(answer);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		System.out.println("\n\n");
		System.out.println("Press Any Key to continue");
		line = in.readLine();

		
		/*
		 * If there is no answer, start IterationMode with Levensthein
		 */
			if(answers.isEmpty()){
			
				answers.clear();
				answers.addAll(doStart(myindex, wordnet, lemmatiser, template_list,"LEVENSTHEIN"));
				System.out.println("\n\n");
				System.out.println("Press Any Key to continue");
				line = in.readLine();
		}
		
		/*
		 * still no answer, start IterationMode with Wordnet
		 */
		
		if(answers.isEmpty()){
			
			answers.clear();
			answers.addAll(doStart(myindex, wordnet, lemmatiser, template_list,"WORDNET"));
			System.out.println("\n\n");
			System.out.println("Press Any Key to continue");
			line = in.readLine();
		}
		
		
		
		/*if(answers.isEmpty()){
			System.out.println("No answers were found with the three Modules");
			//answers.add("No answers were found with the three Modules");
		}*/
		
		
		/*
		 * return answers!
		 */
		
		return answers;
	}


	
	
	

	private static ArrayList<String> doStart(SQLiteIndex myindex, WordNet wordnet,
			StanfordLemmatizer lemmatiser, ArrayList<Template> template_list, String type) {
		ArrayList<String> answers = new ArrayList<String>();
		ArrayList<QueryPair> qp = new ArrayList<QueryPair>();
		int anzahl;
		boolean go_on;
		System.out.println("No answer from direkt match, start "+type+"Modul");
		for(Template t : template_list){
			try{
				ArrayList<ArrayList<Hypothesis>> hypothesenSetList = IterationModule.doIteration(t.getElm(),t.getHypothesen(),t.getCondition(),type,myindex,wordnet,lemmatiser);
				if(type.contains("WORDNET"))t.setHypothesenWordnet(hypothesenSetList);
				if(type.contains("LEVENSTHEIN"))t.setHypothesenLevensthein(hypothesenSetList);
			}
			catch (Exception e){
				
			}
		
		}
		
		/*
		 * Generate Queries and test queries
		 */
		//generate QueryPair
		for(Template t : template_list){
			//t.printAll();
			ArrayList<QueryPair> qp_t = new ArrayList<QueryPair>();
			qp_t = Query.returnSetOfQueries(t, type);
			for(QueryPair p : qp_t){
				//if(!qp.contains(p)) qp.add(p);
				boolean contain = false;
				for(QueryPair p_t : qp){
					if(p_t.getRank()==p.getRank()){
						if(p_t.getQuery().contains(p.getQuery())) contain=true;
					}
				}
				if(!contain&&checkQuery(p.getQuery()))qp.add(p);
			}
		}
		
		//sort QueryPairs
		LinearSort.doSort(qp);	
		anzahl=1;
		go_on = true;
		for(QueryPair q : qp){
			if(anzahl<anzahlAbgeschickterQueries&go_on){
				ArrayList<String> answer_tmp = new ArrayList<String>();
				answer_tmp=ServerUtil.requestAnswerFromServer(q.getQuery());
				System.out.println(q.getQuery() + " Rank: "+q.getRank());
				if(answer_tmp.isEmpty()) go_on=true;

				else{
					//else go_on=false;
					go_on=true;
					//go_on=false;
					for(String s : answer_tmp){
						if(checkAnswer(s)){
							boolean test = false;
							for(String s_t : answers){
								if(s_t.contains(s)) test=true;
							}
							if(!test)answers.add(s);
						}
					}
					//if(checkAnswer(answer_tmp))answers.addAll(answer_tmp);
				}
			}
			anzahl+=1;
		}
		for(String answer:answers){
			System.out.println(answer);
		}
		
		return answers;
	}
	
	
	
	
	private static boolean checkAnswer(String answer){
		if(answer.contains("File:")||answer.contains(".png")||answer.contains("upload.wikimedia.org")||answer.contains("dbpedia.org/datatype/")||answer.contains("http://www.w3.org/2001/XMLSchema")||answer.contains("flickerwrappr/photos/")) return false;
		else return true;
		
	}
	
	private static boolean checkQuery(String query){
		if(query.contains("wikiPageWiki")||query.contains("wikiPageExternal")||query.contains("wikiPageRedirects")|| query.contains("thumbnail")) return false;
		else return true;
		
	}
}
