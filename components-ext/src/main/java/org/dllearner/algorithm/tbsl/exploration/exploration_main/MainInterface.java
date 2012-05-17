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
import org.dllearner.algorithm.tbsl.exploration.Sparql.Elements;
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
		//Setting.setThresholdSelect(0.5);
		
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
		
		answers = singleSteps(myindex, wordnet, lemmatiser, wait, template_list);
		
		return answers;
	}













	private static ArrayList<String> singleSteps(SQLiteIndex myindex, WordNet wordnet,
			StanfordLemmatizer lemmatiser, boolean wait,
			ArrayList<Template> template_list)
			throws IOException {
		
		ArrayList<String> answers = new ArrayList<String>();
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
		qp=HeuristicSort.doHeuristicSort(qp, Question);
		//if(Setting.isDebugModus())printQueries(qp, "NORMAL", Question);
		//printQueries(qp, "NORMAL", Question);
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

		if(answers.isEmpty()&&Setting.getModuleStep()>=2){
			
			answers.clear();
			//Setting.setLevenstheinMin(0.65);
			//Setting.setAnzahlAbgeschickterQueries(10);
			answers.addAll(doStart(myindex, wordnet, lemmatiser, template_list,"LEVENSTHEIN","neu"));
			if(wait)DebugMode.waitForButton();
		}
		
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
		
		if(answers.isEmpty()&&Setting.getModuleStep()>=5){
			System.out.println("NO Answer from Server =>Start Query Manipulation");
			answers.clear();
			answers.addAll(stufe5(myindex,wordnet,lemmatiser,wait,template_list));
			if(wait)DebugMode.waitForButton();
		}
		
		
		
		
		
		
		
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
		/*if(type.contains("SPECIAL")){
			type ="LEVENSTHEIN";
			special=true;
		}*/
		
		System.out.println("No answer from direkt match, start "+type+"Modul");
		for(Template t : template_list){
			try{
				/*if(test.contains("alt")){
					ArrayList<ArrayList<Hypothesis>> hypothesenSetList = IterationModule.doIteration(t.getElm(),t.getHypothesen(),t.getCondition(),type,myindex,wordnet,lemmatiser);
					if(type.contains("WORDNET"))t.setHypothesenWordnet(hypothesenSetList);
					if(type.contains("LEVENSTHEIN"))t.setHypothesenLevensthein(hypothesenSetList);
					if(type.contains("RELATE"))t.setHypothesenRelate(hypothesenSetList);
				}*/
				
				//if(test.contains("neu")){
				//	System.err.println("IN NEU!!!!!");
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
				
			//}
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
		//printQueries(qp, type, Question);
		qp=HeuristicSort.doHeuristicSort(qp, Question);
		
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
					
					System.out.println("Got Answer from Server with this Query: "+ q.getQuery());
					if(qp.size()>(id+1)){
						//&&anzahl<2
						if(q.getRank()==qp.get(id+1).getRank()){
							go_on=true;
						}
					}
					/*
					 * in which queries with an answer, dont accept a second Query, is there is already an answer. 
					 */
					if(Question.toLowerCase().contains("which")) go_on=false;
					if(Question.toLowerCase().contains("who")) go_on=false;
					
						
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
	
	
	
	private static ArrayList<String> stufe5(SQLiteIndex myindex, WordNet wordnet,StanfordLemmatizer lemmatiser, boolean wait,ArrayList<Template> template_list){
		
		ArrayList<Template> new_template_list=new ArrayList<Template>();
		ArrayList<String> answers=new ArrayList<String>();
		/*
		 * iterate over Templates to create new one's but only if you have [isa][resource] and condition.size=2;
		 */
		for(Template t: template_list){
			//t.printAll();
			if(t.getCondition().size()==1){
				System.out.println("Nur eine Condition");
				ArrayList<String> condition=t.getCondition().get(0);
				boolean go_on=false;
				if(condition.get(1).toLowerCase().equals("isa")) go_on=true;
				System.out.println("go_on:"+go_on);
				if(go_on){
					
					String resource_variable=condition.get(0);
					String class_variable=condition.get(2);
					Hypothesis resource_h = null;
					Hypothesis class_h = null;
					boolean go_on_resource=false;
					for(ArrayList<Hypothesis> h_l :t.getHypothesen()){
						for(Hypothesis h : h_l){
							if(h.getVariable().equals(resource_variable)){
								if(h.getType().toLowerCase().contains("resource")) {
									go_on_resource=true;
									resource_h=h;
								}
							}
							if(h.getVariable().equals(class_variable)){
								class_h=h;
							}
						}
						
					}
					System.out.println("go_on_resource:"+go_on_resource);
					if(go_on_resource){
						
						/*
						 * manipulate Class variable to make a property from it
						 */
						class_h.setType("PROPERTY");
						class_h.setUri(class_h.getUri().toLowerCase());
						class_h.setVariable("?y");
						resource_h.setVariable("?x");
						ArrayList<ArrayList<Hypothesis>> new_hypothesen_list = new ArrayList<ArrayList<Hypothesis>>();
						ArrayList<Hypothesis> small_h_list = new ArrayList<Hypothesis>();
						small_h_list.add(resource_h);
						small_h_list.add(class_h);
						new_hypothesen_list.add(small_h_list);
						
						ArrayList<String> condition_new = new ArrayList<String>();
						condition_new.add("?x");
						condition_new.add("?y");
						condition_new.add("?z");
						
						ArrayList<ArrayList<String>> new_c_list = new ArrayList<ArrayList<String>>();
						new_c_list.add(condition_new);
						
						Template new_Template = new Template(new_c_list, t.getQueryType(), "","" , "?z", "", "", t.getQuestion());
						
						new_Template.setHypothesen(new_hypothesen_list);
						Elements elm = new Elements(new_Template.getCondition(),new_Template.getHypothesen());
		     			if(elm.isElementEmty()==false){
		     				//elm.printAll();
		     				new_Template.setElm(elm);
		     				new_template_list.add(new_Template);
		     			}
		     			
		     			Template template_reverse_conditions = new Template(new_Template.getCondition(),new_Template.getQueryType(), new_Template.getHaving(), new_Template.getFilter(), new_Template.getSelectTerm(), new_Template.getOrderBy(), new_Template.getLimit(), new_Template.getQuestion());
		     			template_reverse_conditions.setHypothesen(new_hypothesen_list);
		     			
		     			ArrayList<ArrayList<String>> condition_template_reverse_conditions = template_reverse_conditions.getCondition();
		     			ArrayList<ArrayList<String>> condition_reverse_new= new ArrayList<ArrayList<String>>();


		     				for (ArrayList<String> x : condition_template_reverse_conditions){
		         				ArrayList<String> new_list = new ArrayList<String>();
		         				new_list.add(x.get(2));
		             			new_list.add(x.get(1));
		             			new_list.add(x.get(0));
		             			condition_reverse_new.add(new_list);
		         			}
		     				
		     				
		     				template_reverse_conditions.setCondition(condition_reverse_new);
		     			
		     				Elements elm_reverse = new Elements(template_reverse_conditions.getCondition(),template_reverse_conditions.getHypothesen());
			     			if(elm_reverse.isElementEmty()==false){
			     				//elm.printAll();
			     				template_reverse_conditions.setElm(elm_reverse);
			     				new_template_list.add(template_reverse_conditions);
			     			}
		     			
						
						
					}
					
					
					
					
				}
			}
			
			
			
			
			/*
			 * only if condition.size==2
			 */
			if(t.getCondition().size()==2){
				System.out.println("Yeah, found two Conditions!");
				
				/*
				 * now look if one have the [isa][resource] or [resource][isa] case
				 */
				ArrayList<String> condition1=new ArrayList<String>();
				ArrayList<String> condition2=new ArrayList<String>();
				
				condition1=t.getCondition().get(0);
				condition2=t.getCondition().get(1);
				System.out.println("condition1:"+condition1);
				System.out.println("condition2:"+condition2);
				
				boolean go_on=false;
				
				if(condition1.get(1).toLowerCase().contains("isa")&&!condition2.get(1).toLowerCase().contains("isa")){
					String resource1_variable=condition2.get(0);
					String resource2_variable=condition2.get(2);
					for(ArrayList<Hypothesis> h_l :t.getHypothesen()){
						for(Hypothesis h : h_l){
							if(h.getVariable().equals(resource2_variable)||h.getVariable().equals(resource1_variable)){
								if(h.getType().toLowerCase().contains("resource")) go_on=true;
							}
						}
						
					}
					
					/*if(condition2.get(0).contains("resource/")||condition2.get(2).contains("resource/")){
						go_on=true;
					}
					else go_on=false;*/
				}
				
				else if(condition2.get(1).toLowerCase().contains("isa")){
					
					String resource1_variable=condition1.get(0);
					String resource2_variable=condition1.get(2);
					for(ArrayList<Hypothesis> h_l :t.getHypothesen()){
						for(Hypothesis h : h_l){
							if(h.getVariable().equals(resource2_variable)||h.getVariable().equals(resource1_variable)){
								if(h.getType().toLowerCase().contains("resource")) go_on=true;
							}
						}
						
					}
					
					
					/*
					 * in the conditions there is for sure no resource!!!
					 */
					/*if(condition1.get(0).contains("resource/")||condition1.get(2).contains("resource/")){
						go_on=true;
					}
					else go_on=false;*/
				}
				else go_on=false;
				
				
				System.out.println("Go_on:"+go_on);
				if(go_on==true){
					
					/*
					 * use now only the conditions WITHOUT the class
					 */
					ArrayList<ArrayList<Hypothesis>> new_hypothesen_list = new ArrayList<ArrayList<Hypothesis>>();
					
					String resource_variable=null;
					for(ArrayList<Hypothesis> h_l :t.getHypothesen()){
						ArrayList<Hypothesis> t_h_l = new ArrayList<Hypothesis>();
						
						for(Hypothesis h : h_l){
							if(!h.getType().toLowerCase().contains("isa"))t_h_l.add(h);
							if(h.getType().toLowerCase().contains("resource"))resource_variable=h.getVariable();
						}
						
						if(t_h_l.size()>0) new_hypothesen_list.add(t_h_l);
					}
					
					/*
					 * New HypothesenList
					 */
					System.out.println("New Hypothesen List");
					for(ArrayList<Hypothesis> h_blub : new_hypothesen_list){
						for(Hypothesis blub:h_blub) blub.printAll();
					}
					/*
					 * create new ArrayList for Conditions, only with the
					 */
					ArrayList<String> new_condition= new ArrayList<String>();
					if(!condition1.get(1).toLowerCase().contains("isa")) new_condition=condition1;
					else new_condition=condition2;
					
					String new_SelectTerm=null;
					
					if(new_condition.get(0).contains(resource_variable)) new_SelectTerm=new_condition.get(2);
					else new_SelectTerm=new_condition.get(0);
					
					ArrayList<ArrayList<String>> new_c_list = new ArrayList<ArrayList<String>>();
					new_c_list.add(new_condition);
					/*
					 * Template template_reverse_conditions = new Template(template.getCondition(),template.getQueryType(), template.getHaving(), template.getFilter(), template.getSelectTerm(), template.getOrderBy(), template.getLimit(), template.getQuestion());
					 */
					Template new_Template = new Template(new_c_list, t.getQueryType(), "","" , new_SelectTerm, "", "", t.getQuestion());
					new_Template.setHypothesen(new_hypothesen_list);
					/*
					 * Elements can still be the same
					 */
					new_Template.setElm(t.getElm());
					new_template_list.add(new_Template);
					//new_Template.printAll();
					
				}
				
				
			}
		}
		
		/*
		 * if there are new templates, start rescursive call;
		 */
		if(new_template_list.size()>0){
			System.out.println("Generated new Templates");
			try {
				return singleSteps(myindex, wordnet, lemmatiser, wait,new_template_list);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				return answers;
			}
		}
		
		return answers;
	}
}
