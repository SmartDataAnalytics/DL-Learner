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
import org.dllearner.algorithm.tbsl.exploration.Utils.Levenshtein;
import org.dllearner.algorithm.tbsl.nlp.StanfordLemmatizer;
import org.dllearner.algorithm.tbsl.nlp.WordNet;

public class WordnetModule {
	
	private static int explorationdepthwordnet =2;
	
	public static ArrayList<Hypothesis> doWordnet(String variable, String property_to_compare_with, HashMap<String, String> properties, SQLiteIndex myindex,WordNet wordnet,StanfordLemmatizer lemmatiser) throws SQLException,
	JWNLException {
		ArrayList<Hypothesis> listOfNewHypothesen = new ArrayList<Hypothesis>();

		 System.out.println("Start Iterating Wordnet with "+property_to_compare_with+" and deept of "+explorationdepthwordnet);
		 ArrayList<String> semantics=new ArrayList<String>();
		 ArrayList<String> tmp_semantics=new ArrayList<String>();
		 ArrayList<String> result_SemanticsMatchProperties=new ArrayList<String>();
		 if(property_to_compare_with.contains("_")){
			 String[] fix = property_to_compare_with.split("_");
			 //here add also lemmatiser
			 for(String s: fix) semantics.add(s);
		 }
		 else semantics.add(property_to_compare_with);
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
			 /*
			 else{
				 semantics.clear();
				 semantics.add(_temp_);
				 tmp_semantics=semantics;
			 }*/
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
				    
				for(String b : semantics){
					if(key.contains(b.toLowerCase())||key.contains(lemmatiser.stem(b.toLowerCase()))||b.toLowerCase().contains(lemmatiser.stem(key))){
						if(!result_SemanticsMatchProperties.contains(key)){
						 result_SemanticsMatchProperties.add(key);
						 if(key.toLowerCase().contains(property_to_compare_with.toLowerCase())){
							 Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", 1.5); 
							 listOfNewHypothesen.add(h);
						 }
						 else{
							 double nld=Levenshtein.nld(property_to_compare_with.toLowerCase(), key);
							 Hypothesis h = new Hypothesis(variable, key, value, "PROPERTY", nld);
							 listOfNewHypothesen.add(h);
						 }
						
						}
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
		//also look at the stemmt part!
		/*for(String s: semanticsOrig){
			String bla=lemmatiser.stem(s);
			semantics.add(bla);
			semantics.add(s);
		}*/
		
		try{
			for(String id :semantics){
				//System.out.println("in String id : semantics");
				//System.out.println("ID :"+id);
				
				//add id also to the result, if its not already in there
				if(!result.contains(id))result.add(id);
				List<String> array_relatedNouns=null;
				List<String> array_bestsynonyms=null;
				
				//System.out.println("Wordnet Word: "+id);
				try{
					array_relatedNouns =wordnet.getRelatedNouns(id);
				}
				catch(Exception e){
					//array_relatedNouns.clear();
				}
				//System.out.println("array_relatedNouns: "+ array_relatedNouns);
				//System.out.println("after relatedNouns");

				try{
					array_bestsynonyms=wordnet.getBestSynonyms(POS.NOUN, id);
					//System.out.println("array_bestsynonyms: "+ array_bestsynonyms);
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
				
				
					
			}
		}
		catch(Exception e){
			return null;
		}

		if(!result.isEmpty()) return result;
		else{
			//System.out.println("Didnt find ")
			/*this is the case, if the first time nothing was found.
			 * but sometimes wordnet doesnt find anything e.g. die place... bzt you have also die and place
			 * so we try to find the seperate words and test them as well
			 */
			try{
				for(String id :semantics){
					//System.out.println("in String id : semantics TWO");
					String[] tmp_array=id.split(" ");
					//System.out.println("ID TWO:"+id);
					if(tmp_array.length>=2){
						for(String advanced_id : tmp_array){
							List<String> array_relatedNouns=null;
							List<String> array_bestsynonyms=null;
							//add id also to the result, if its not already in there
							if(!result.contains(advanced_id))result.add(advanced_id);
							
							try{
								array_relatedNouns =wordnet.getRelatedNouns(advanced_id);
							}
							catch(Exception e){
								//array_relatedNouns.clear();
							}
							//System.out.println("array_relatedNouns: "+ array_relatedNouns);
							//System.out.println("after relatedNouns");

							try{
								array_bestsynonyms=wordnet.getBestSynonyms(POS.NOUN, advanced_id);
							//	System.out.println("array_bestsynonyms: "+ array_bestsynonyms);
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
