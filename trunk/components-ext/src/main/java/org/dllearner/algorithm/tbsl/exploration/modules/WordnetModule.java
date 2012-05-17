package org.dllearner.algorithm.tbsl.exploration.modules;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;

import org.dllearner.algorithm.tbsl.exploration.Index.SQLiteIndex;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;
import org.dllearner.algorithm.tbsl.exploration.Utils.DebugMode;
import org.dllearner.algorithm.tbsl.exploration.Utils.Levenshtein;
import org.dllearner.algorithm.tbsl.exploration.exploration_main.Setting;
import org.dllearner.algorithm.tbsl.nlp.StanfordLemmatizer;
import org.dllearner.algorithm.tbsl.nlp.WordNet;

public class WordnetModule {
	
	private static int explorationdepthwordnet =0;
	
	public static ArrayList<Hypothesis> doWordnet(String variable, String property_to_compare_with, HashMap<String, String> properties, SQLiteIndex myindex,WordNet wordnet,StanfordLemmatizer lemmatiser) throws SQLException,
	JWNLException {
		ArrayList<Hypothesis> listOfNewHypothesen = new ArrayList<Hypothesis>();

		 System.out.println("Start Iterating Wordnet with "+property_to_compare_with+" and deept of "+explorationdepthwordnet);
		// StanfordLemmatizer lemmatiser = new StanfordLemmatizer();
		 ArrayList<String> semantics=new ArrayList<String>();
		 ArrayList<String> tmp_semantics=new ArrayList<String>();
		 ArrayList<String> result_SemanticsMatchProperties=new ArrayList<String>();
		 if(property_to_compare_with.contains("_")){
			 String[] fix = property_to_compare_with.split("_");
			 //here add also lemmatiser
			 for(String s: fix) {
				 semantics.add(s);
				 try{
					 semantics.add(lemmatiser.stem(s));
				 }
				 catch (Exception e){
					 
				 }
			 }
		 }
		 semantics.add(property_to_compare_with);
		 try{
			 semantics.add(lemmatiser.stem(property_to_compare_with));
		 }
		 catch (Exception e){
			 
		 }
		 System.out.println("Semantics: "+ semantics);
		 
		 for(String s: semantics){
			 //first check, if there is a singular form in the wordnet dictionary.. eg children -> child
			 //String _temp_=myindex.getWordnetHelp(property);
			 String _temp_=myindex.getWordnetHelp(s);
			 if(_temp_!=null){
				 //tmp_semantics=semantics;
				 tmp_semantics.add(_temp_);
				 tmp_semantics.add(s);
			 }
			 else tmp_semantics.add(s);

		 }
		 
		 System.out.println("tmp_semantics: "+ tmp_semantics);
		 Boolean goOnAfterWordnet = true;

		 for(int i=0;i<=explorationdepthwordnet;i++){

			 try {
				tmp_semantics=getSemantics(tmp_semantics,wordnet);
				System.out.println("tmp_semantics in Iteration: "+ tmp_semantics);
				if (tmp_semantics==null){
					goOnAfterWordnet=false;
					System.out.println("Error in searching Wordnet with word "+semantics+" \n End");

				}
				else{
				//each word only one time
				 for(String k : tmp_semantics){
					 if(!semantics.contains(k)) semantics.add(k);
				 }
				}
				
			} catch (IOException e) {
				
				goOnAfterWordnet=false;
				System.out.println("Error in searching Wordnet with word "+semantics+" \n End");
				
			}
					 
		 }
		
		 if(goOnAfterWordnet==true){
			
			 for (Entry<String, String> entry : properties.entrySet()) {
				    String key = entry.getKey();
				    String value = entry.getValue();
				    key=key.replace("\"","");
				    key=key.replace("@en","");
				    key=key.toLowerCase();
				    
				for(String b : semantics){
					if(key.equals(b)){

						
						Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", 1.0); 
						 
						
						 listOfNewHypothesen.add(h);
						
					}
					else if(key.contains(b.toLowerCase())||b.toLowerCase().contains(key)){
						
						System.out.println("Key:"+key);
						System.out.println("b:"+b);

						if(b.length()>4&&key.length()>4) {
							double score=0;
							if(b.length()>key.length()){
								score = 0.8+(key.length()/b.length());
							}
							else{
								score=0.8+(b.length()/key.length());
							}
							//0.95
							Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", score); 
							listOfNewHypothesen.add(h);
						}
						else{
							Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", 0.7); 
							listOfNewHypothesen.add(h);
						}
						 
						 
						 
					
						 
						 
					}
					
					else if(Levenshtein.nld(key.toLowerCase(), b.toLowerCase())>Setting.getLevenstheinMin()){
						Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", (Levenshtein.nld(key.toLowerCase(), b.toLowerCase()))); 
						 
						
						 listOfNewHypothesen.add(h);
					}
						
						
				}
			}
			 
		 }
		 
		
		 return listOfNewHypothesen;
	}
	
	private static ArrayList<String> getSemantics (ArrayList<String> semanticsOrig,WordNet wordnet) throws IOException, JWNLException {
		ArrayList<String> result = new ArrayList<String>();
		
		//System.out.println("in function get Semantics!");
		
		ArrayList<String> semantics = new ArrayList<String>();
		semantics=semanticsOrig;
		
		try{
			for(String id :semantics){
				if(!result.contains(id))result.add(id);
				List<String> array_relatedNouns=null;
				List<String> array_bestsynonyms=null;
				List<String> array_bestsynonyms_verb=null;
				List<String> array_bestsynonyms_adj=null;
				
				try{
					array_relatedNouns =wordnet.getRelatedNouns(id);
				}
				catch(Exception e){
					//array_relatedNouns.clear();
				}
				try{
					array_bestsynonyms=wordnet.getBestSynonyms(POS.NOUN, id);
					array_bestsynonyms_verb=wordnet.getBestSynonyms(POS.VERB, id);
					array_bestsynonyms_adj=wordnet.getBestSynonyms(POS.ADJECTIVE, id);
				}
				catch(Exception e){
					//
				}

					
					
				if(array_relatedNouns!=null){
					for(String i:array_relatedNouns){
						if(!result.contains(i))result.add(i);
					}
				}
				if(array_bestsynonyms!=null){
					for(String i:array_bestsynonyms){
						if(!result.contains(i))result.add(i);
					}
				}
				if(array_bestsynonyms_verb!=null){
					for(String i:array_bestsynonyms_verb){
						if(!result.contains(i))result.add(i);
					}
				}
				if(array_bestsynonyms_adj!=null){
					for(String i:array_bestsynonyms_adj){
						if(!result.contains(i))result.add(i);
					}
				}
				
				
					
			}
		}
		catch(Exception e){
			return null;
		}

		if(!result.isEmpty()) return result;
		else{
			try{
				for(String id :semantics){
					String[] tmp_array=id.split(" ");
					if(tmp_array.length>=2){
						for(String advanced_id : tmp_array){
							List<String> array_relatedNouns=null;
							List<String> array_bestsynonyms=null;
							List<String> array_bestsynonyms_verb=null;
							List<String> array_bestsynonyms_adj=null;
							//add id also to the result, if its not already in there
							if(!result.contains(advanced_id))result.add(advanced_id);
							
							try{
								array_relatedNouns =wordnet.getRelatedNouns(advanced_id);
							}
							catch(Exception e){
								//array_relatedNouns.clear();
							}
							try{
								array_bestsynonyms=wordnet.getBestSynonyms(POS.NOUN, advanced_id);
								array_bestsynonyms_verb=wordnet.getBestSynonyms(POS.VERB, advanced_id);
								array_bestsynonyms_adj=wordnet.getBestSynonyms(POS.ADJECTIVE, advanced_id);
							}
							catch(Exception e){
								//
							}
								
							if(array_relatedNouns!=null){
								for(String i:array_relatedNouns){
									if(!result.contains(i))result.add(i);
								}
							}
							if(array_bestsynonyms!=null){
								for(String i:array_bestsynonyms){
									if(!result.contains(i))result.add(i);
								}
							}
							if(array_bestsynonyms_verb!=null){
								for(String i:array_bestsynonyms_verb){
									if(!result.contains(i))result.add(i);
								}
							}
							if(array_bestsynonyms_adj!=null){
								for(String i:array_bestsynonyms_adj){
									if(!result.contains(i))result.add(i);
								}
							}

							
							}
					}
						
				}
				}
				catch(Exception e){
					if(result.isEmpty()) return semanticsOrig;
				}
			
		}
		
		if(!result.isEmpty()) return result;
		else return null;
	//	else{ return result;}
	}
	
	
	

}
