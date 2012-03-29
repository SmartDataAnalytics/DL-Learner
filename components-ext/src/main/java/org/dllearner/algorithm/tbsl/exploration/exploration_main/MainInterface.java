package org.dllearner.algorithm.tbsl.exploration.exploration_main;

import java.io.IOException;
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
import org.dllearner.algorithm.tbsl.exploration.modules.IterationModule;
import org.dllearner.algorithm.tbsl.templator.BasicTemplator;

public class MainInterface {
	private int anzahlAbgeschickterQueries = 10;
	public static void startQuestioning(String question,BasicTemplator btemplator,SQLiteIndex myindex  ) throws ClassNotFoundException, SQLException, IOException{
		
		TemplateBuilder templateObject = new TemplateBuilder(btemplator, myindex);
		ArrayList<Template> template_list = new ArrayList<Template>();
		
		
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
			//t.printAll();
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
		
		/*
		 * If there is no answer, start IterationMode with Levensthein
		 */
		for(Template t : template_list){
			try{
				ArrayList<ArrayList<Hypothesis>> hypothesenSetList = IterationModule.doIteration(t.getElm(),t.getHypothesen(),t.getCondition(),"LEVENSTHEIN");
				t.setHypothesenLevensthein(hypothesenSetList);
			}
			catch (Exception e){
				
			}
		
		}
		
		/*
		 * Generate Queries for Levensthein Mode and test queries
		 */
		qp.clear();
		//generate QueryPair
		for(Template t : template_list){
			//t.printAll();
			ArrayList<QueryPair> qp_t = new ArrayList<QueryPair>();
			qp_t = Query.returnSetOfQueries(t, "LEVENSTHEIN");
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
		System.out.println("Anzahl: "+qp.size());
		/*
		 * still no answer, start IterationMode with Wordnet
		 */
		
		/*
		 * Generate Queries for Wordnet Mode and test queries. 
		 */
	}
	
	
	private void sortQueries(){
		
	}
}
