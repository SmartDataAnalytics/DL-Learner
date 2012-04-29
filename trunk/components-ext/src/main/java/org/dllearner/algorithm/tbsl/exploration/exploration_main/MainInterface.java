package org.dllearner.algorithm.tbsl.exploration.exploration_main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.dllearner.algorithm.tbsl.exploration.Index.SQLiteIndex;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Template;
import org.dllearner.algorithm.tbsl.exploration.Sparql.TemplateBuilder;
import org.dllearner.algorithm.tbsl.exploration.Utils.DebugMode;
import org.dllearner.algorithm.tbsl.exploration.Utils.HeuristicSort;
import org.dllearner.algorithm.tbsl.exploration.Utils.LinearSort;
import org.dllearner.algorithm.tbsl.exploration.Utils.Query;
import org.dllearner.algorithm.tbsl.exploration.Utils.QueryPair;
import org.dllearner.algorithm.tbsl.exploration.Utils.ServerUtil;
import org.dllearner.algorithm.tbsl.exploration.modules.IterationModule;
import org.dllearner.algorithm.tbsl.nlp.StanfordLemmatizer;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.templator.BasicTemplator;
import org.dllearner.algorithms.gp.GP;

public class MainInterface {
	//private static int anzahlAbgeschickterQueries = 10;
	
	
	public static ArrayList<String> startQuestioning(String question,BasicTemplator btemplator,SQLiteIndex myindex, WordNet wordnet,StanfordLemmatizer lemmatiser) throws ClassNotFoundException, SQLException, IOException{
		
		/*
		 * true, if you have to push a button to get to the next module
		 * false, goes through all
		 */
		boolean wait = false;
		Setting.setThresholdSelect(0.5);
		
		if(Setting.isWaitModus())wait=true;
		
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
		String Question="";
		for(Template t : template_list){
			Question=t.getQuestion();
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
		qp=LinearSort.doSort(qp);	
		qp=HeuristicSort.doSort(qp, Question);
		//if(Setting.isDebugModus())printQueries(qp, "NORMAL", Question);
		printQueries(qp, "NORMAL", Question);
		Setting.setAnzahlAbgeschickterQueries(10);
		
		int anzahl=1;
		boolean go_on = true;
		for(QueryPair q : qp){
			if(anzahl<10&go_on &!q.getQuery().contains("ASK")){
				ArrayList<String> answer_tmp = new ArrayList<String>();
				System.out.println("Sending Query to Server: "+q.getQuery());
				answer_tmp=ServerUtil.requestAnswerFromServer(q.getQuery());
				if(answer_tmp.isEmpty()) go_on=true;

				else{
					go_on=false;
					//if(qp.size()<3)go_on=true;
					System.out.println("Got Answer from Server with this Query: "+ q.getQuery());
					//go_on=true;
					boolean contains_uri=false;
					for(String s : answer_tmp){
						if(s.contains("http")){
							contains_uri=true;
							break;
						}
					}
					for(String s : answer_tmp){
						if(checkAnswer(s)){
							if(!s.equals("0")){
								boolean double_result = false;
								for(String s_t : answers){
									if(s_t.contains(s)) double_result=true;
								}
								//TODO Test!!!!!!
								//if in one answer is an http, only add uri's 
								if(!double_result){
									if(contains_uri){
										if(s.contains("http"))answers.add(s);
									}
									else answers.add(s);
								}
							}
							
						}
					}
					//if(checkAnswer(answer_tmp))answers.addAll(answer_tmp);
				}
			}
			anzahl+=1;
		}
		
		System.out.println("\n Answer from Server: \n");
		for(String answer:answers){
			System.out.println(answer);
		}
		if(wait)DebugMode.waitForButton();

		
		/*
		 * If there is no answer, start IterationMode with Levensthein
		 */
			if(answers.isEmpty()&&Setting.getModuleStep()>=2){
			
				answers.clear();
				//Setting.setLevenstheinMin(0.65);
				//Setting.setAnzahlAbgeschickterQueries(10);
				answers.addAll(doStart(myindex, wordnet, lemmatiser, template_list,"LEVENSTHEIN","neu"));
				if(wait)DebugMode.waitForButton();
		}
		
		/*
		 * still no answer, start IterationMode with Wordnet
		 */
		
		if(answers.isEmpty()&&Setting.getModuleStep()>=3){
			
			answers.clear();
			//Setting.setAnzahlAbgeschickterQueries(10);
			answers.addAll(doStart(myindex, wordnet, lemmatiser, template_list,"WORDNET","neu"));
			if(wait)DebugMode.waitForButton();
		}
		
		if(answers.isEmpty()&&Setting.getModuleStep()>=4){
			
			answers.clear();
			//Setting.setAnzahlAbgeschickterQueries(10);
			//Setting.setThresholdSelect(0.2);
			answers.addAll(doStart(myindex, wordnet, lemmatiser, template_list,"RELATE","neu"));
			if(wait)DebugMode.waitForButton();
		}
		

		/*if(answers.isEmpty()){
			
			answers.clear();
			Setting.setLevenstheinMin(0.25);
			Setting.setAnzahlAbgeschickterQueries(20);
			answers.addAll(doStart(myindex, wordnet, lemmatiser, template_list,"SPECIAL","neu"));
			if(wait)DebugMode.waitForButton();
		}*/

		
		
		
		/*if(answers.isEmpty()){
			System.out.println("");
			//answers.add("No answers were found with the three Modules");
		}*/
		
		
		/*
		 * return answers!
		 */
		
		return answers;
	}






	


	
	
	

	private static ArrayList<String> doStart(SQLiteIndex myindex, WordNet wordnet,
			StanfordLemmatizer lemmatiser, ArrayList<Template> template_list, String type, String test) {
		ArrayList<String> answers = new ArrayList<String>();
		ArrayList<QueryPair> qp = new ArrayList<QueryPair>();
		boolean special=false;
		int anzahl;
		boolean go_on;
		if(type.contains("SPECIAL")){
			type ="LEVENSTHEIN";
			special=true;
		}
		
		System.out.println("No answer from direkt match, start "+type+"Modul");
		for(Template t : template_list){
			try{
				if(test.contains("alt")){
					ArrayList<ArrayList<Hypothesis>> hypothesenSetList = IterationModule.doIteration(t.getElm(),t.getHypothesen(),t.getCondition(),type,myindex,wordnet,lemmatiser);
					if(type.contains("WORDNET"))t.setHypothesenWordnet(hypothesenSetList);
					if(type.contains("LEVENSTHEIN"))t.setHypothesenLevensthein(hypothesenSetList);
					if(type.contains("RELATE"))t.setHypothesenRelate(hypothesenSetList);
				}
				
				if(test.contains("neu")){
					System.err.println("IN NEU!!!!!");
					ArrayList<ArrayList<Hypothesis>> hypothesenSetList  = new ArrayList<ArrayList<Hypothesis>>();
					
					
					for(ArrayList<Hypothesis> l_h : t.getHypothesen()){
						ArrayList<ArrayList<Hypothesis>> generated_hypothesis = new ArrayList<ArrayList<Hypothesis>>();
						generated_hypothesis= IterationModule.new_iteration(t.getElm(),l_h,t.getCondition(),type,myindex,wordnet,lemmatiser);
						for(ArrayList<Hypothesis> h_t : generated_hypothesis){
							ArrayList<Hypothesis> new_hypothesen_set = new ArrayList<Hypothesis>();
							for(Hypothesis bla : h_t){
								new_hypothesen_set.add(bla);
							}
							hypothesenSetList.add(new_hypothesen_set);
						}
						
						//hypothesenSetList.addAll(blub);
					}
					if(type.contains("WORDNET"))t.setHypothesenWordnet(hypothesenSetList);
					if(type.contains("LEVENSTHEIN"))t.setHypothesenLevensthein(hypothesenSetList);
					if(type.contains("RELATE"))t.setHypothesenRelate(hypothesenSetList);
					
				}
				
			}
			catch (Exception e){
				
			}
		
		}
		
		/*
		 * Generate Queries and test queries
		 */
		//generate QueryPair
		String Question="";
		for(Template t : template_list){
			//t.printAll();
			Question=t.getQuestion();
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
		qp=LinearSort.doSort(qp);	
		printQueries(qp, type, Question);
		/*
		 * Only for test!
		 */
		qp=HeuristicSort.doSort(qp, Question);
		
		System.out.println("Following Querries were created:");
		for(QueryPair z : qp){
			System.out.println(z.getQuery()+" "+z.getRank());
		}
		if(Setting.isDebugModus())printQueries(qp, type, Question);
		//printQueries(qp, type, Question);
		anzahl=1;
		go_on = true;
		int id=0;
		for(QueryPair q : qp){
			if(q.getRank()>Setting.getThresholdSelect()&go_on &!q.getQuery().contains("ASK")){
				ArrayList<String> answer_tmp = new ArrayList<String>();
				answer_tmp=ServerUtil.requestAnswerFromServer(q.getQuery());
				System.out.println("Sending Query to Server: "+q.getQuery());
				if(answer_tmp.isEmpty()) go_on=true;

				else{
					//else go_on=false;
					//go_on=true;
					go_on=false;
					if(special) go_on=true;
					System.out.println("Got Answer from Server with this Query: "+ q.getQuery());
					if(qp.size()>(id+1)){
						//&&anzahl<2
						if(q.getRank()==qp.get(id+1).getRank()){
							go_on=true;
						}
					}
					
						
					boolean contains_uri=false;
					for(String s : answer_tmp){
						if(s.contains("http")){
							contains_uri=true;
							break;
						}
					}
					/*System.out.println("\n Answer from Server Befor check answer: \n");
					for(String answer:answer_tmp){
						System.out.println(answer);
					}*/
					
					
					for(String s : answer_tmp){
						if(checkAnswer(s)){
							boolean double_result = false;
							for(String s_t : answers){
								if(s_t.contains(s)) double_result=true;
							}
							//TODO Test!!!!!!
							//if in one answer is an http, only add uri's 
							if(!double_result){
								if (Question.toLowerCase().contains("who")){
									if(!s.contains("http"))answers.add(s);
								}
								else if(contains_uri){
									if(s.contains("http"))answers.add(s);
								}
								else answers.add(s);
							}
						}
					}
					//if(checkAnswer(answer_tmp))answers.addAll(answer_tmp);
				}
			}
			
			else if(q.getRank()>Setting.getThresholdAsk()&go_on &q.getQuery().contains("ASK")){
				ArrayList<String> answer_tmp = new ArrayList<String>();
				answer_tmp=ServerUtil.requestAnswerFromServer(q.getQuery());
				System.out.println("Sending Query to Server: "+q.getQuery());
				if(answer_tmp.isEmpty()) go_on=true;

				else{
					//else go_on=false;
					//go_on=true;
					go_on=false;
					if(special) go_on=true;
					System.out.println("Got Answer from Server with this Query: "+ q.getQuery());
					if(qp.size()>(id+1)){
						if(q.getRank()==qp.get(id+1).getRank()){
							go_on=true;
						}
					}
					
						
					boolean contains_uri=false;
					for(String s : answer_tmp){
						if(s.contains("http")){
							contains_uri=true;
							break;
						}
					}
					
					
					for(String s : answer_tmp){
						if(checkAnswer(s)){
							boolean double_result = false;
							for(String s_t : answers){
								if(s_t.contains(s)) double_result=true;
							}
							//TODO Test!!!!!!
							//if in one answer is an http, only add uri's 
							if(!double_result){
								if(contains_uri){
									if(s.contains("http"))answers.add(s);
								}
								else answers.add(s);
							}
						}
					}
					//if(checkAnswer(answer_tmp))answers.addAll(answer_tmp);
				}
			}
			anzahl+=1;
			id+=1;
		}
		/*
		 * here Filter answer
		 */
		/*System.out.println("\n Answer from Server: \n");
		for(String answer:answers){
			System.out.println(answer);
		}*/
		//System.out.println("FILTER NOW!!");
		answers=filterAnswer(answers,Question);
		System.out.println("\n Answer from Server: \n");
		for(String answer:answers){
			System.out.println(answer);
		}
		
		return answers;
	}
	
	
	
	private static ArrayList<String> filterAnswer(ArrayList<String> answers, String Question){
		if(Question.toLowerCase().contains("who")){
			boolean contains_only_uri=true;
			for(String s: answers){
				if(!s.contains("http")) contains_only_uri=false;
			}
			if(contains_only_uri==false){
				ArrayList<String> new_answer= new ArrayList<String>();
				for(String s: answers){
					if(!s.contains("http")) {
						System.out.println("s :"+s);
						new_answer.add(s);
					}
				}
				
				return new_answer;
			}
			else{
				return answers;
			}
		}
		
		
		return answers;
	}
	private static boolean checkAnswer(String answer){
		if(answer.contains("File:")||answer.contains(".png")||answer.contains("upload.wikimedia.org")||answer.contains("dbpedia.org/datatype/")||answer.contains("http://www.w3.org/2001/XMLSchema")||answer.contains("flickerwrappr/photos/")) return false;
		else return true;
		
	}
	
	private static boolean checkQuery(String query){
		if(query.contains("wikiPageWiki")||query.contains("wikiPageExternal")||query.contains("wikiPageRedirects")|| query.contains("thumbnail")||query.contains("wikiPage")) return false;
		else return true;
		
	}
	
	private static void printQueries(ArrayList<QueryPair> qp, String type, String Question){
		String dateiname="/home/swalter/Dokumente/Auswertung/CreatedQueryListNLD"+Setting.getLevenstheinMin()+".txt";
		String result_string ="";
		//Open the file for reading
	     try {
	       BufferedReader br = new BufferedReader(new FileReader(dateiname));
	       String thisLine;
		while ((thisLine = br.readLine()) != null) { // while loop begins here
	         result_string+=thisLine+"\n";
	       } // end while 
	     } // end try
	     catch (IOException e) {
	       System.err.println("Error: " + e);
	     }
	     
	     File file = new File(dateiname);
	     BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	      String querylist="";
	      querylist="\n Modul: "+type+"\nfor Question: "+ Question+"\n";
	      int anzahl= 0;
	      /*
	       * write only the first 10 queries:
	       */
	      for(QueryPair q : qp){
	    	  if(anzahl<10){
	    		  querylist+=q.getQuery()+"  "+q.getRank()+"\n";
	    		  anzahl+=1;
	    	  }
	    	  
	      }
	     
	     

	        try {
				bw.write(result_string+querylist);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				bw.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
