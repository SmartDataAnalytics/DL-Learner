package org.dllearner.algorithm.tbsl.exploration.Sparql;
import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;

import org.dllearner.algorithm.tbsl.exploration.sax.ParseXmlHtml;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.sparql.BasicQueryTemplate;
import org.dllearner.algorithm.tbsl.sparql.Path;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Filter;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Term;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.templator.BasicTemplator;
import org.dllearner.algorithm.tbsl.templator.Templator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



public class SparqlObject {
	//global Variable dict
	
	//start counting with 0
	static int explorationdepthwordnet=1;
	static int iterationdepth =0;
	static int numberofanswers=1;
	static double LvenstheinMin = 0.95;
	static WordNet wordnet;
	BasicTemplator btemplator;
	Templator templator;
/*	private static HashMap<String, String> hm = new HashMap<String, String>();
	private static HashMap<String, String> hm_new = new HashMap<String, String>();*/
	private static mySQLDictionary myindex; 
	

	//Konstruktor
	public SparqlObject() throws MalformedURLException, ClassNotFoundException, SQLException{
		this.wordnet = new WordNet();
		System.out.println("Loading SPARQL Templator");
    	this.btemplator = new BasicTemplator();
    	this.templator = new Templator();
    	System.out.println("Loading SPARQL Templator Done\n");
    	System.out.println("Start Indexing");
    	this.myindex = new mySQLDictionary();
    	
    	System.out.println("Done:Indexing");
    	setExplorationdepthwordnet(1);
    	//eigentlich immer mit 0 initialisieren
    	setIterationdepth(1);
    	setNumberofanswers(1);
	}
	
	/*
	 * #####################################
	 * Getter and Setter Methods
	 */
	
	public int getExplorationdepthwordnet() {
		return explorationdepthwordnet;
	}


	public void setExplorationdepthwordnet(int explorationdepthwordnet) {
		SparqlObject.explorationdepthwordnet = explorationdepthwordnet;
	}


	public int getIterationdepth() {
		return iterationdepth;
	}


	public void setIterationdepth(int iterationdepth) {
		SparqlObject.iterationdepth = iterationdepth;
	}


	public int getNumberofanswers() {
		return numberofanswers;
	}


	public void setNumberofanswers(int numberofanswers) {
		SparqlObject.numberofanswers = numberofanswers;
	}
	

	/*
	 * ##############################
	 *
	 */
	/*
	 * "Main" Method of this Class.
	 * 
	 */
	 public void create_Sparql_query(String question) throws JWNLException, IOException, SQLException{
		 	//create_Sparql_query_new(string);
			
		 ArrayList<ArrayList<String>> lstquery = new ArrayList<ArrayList<String>>();
		long startParsingTime = System.currentTimeMillis();
		lstquery=getQuery(question);
		long endParsingTime = System.currentTimeMillis();
		System.out.println("The Questionparsing took "+ (endParsingTime-startParsingTime)+ " ms");
		ArrayList<String> final_answer = new ArrayList<String>();
		
		if(lstquery.isEmpty()){
			saveNotParsedQuestions(question);
		}
			//for each querry
		//TODO: Add function that no qery is send to the server, if querylist==null
			for(ArrayList<String> querylist : lstquery){
				
				boolean startIterating=true;
				String query="";
				if(querylist.get(0).contains("ERROR"))startIterating=false;
				else query=querylist.get(0).toString();
				
				//TODO: Somewhere is an error, because sometimes there is an double _  a __ and thats not allowed.
				//fixing it now with an replace of "__" to ""
				query=query.replace("__", "");
				
				if(getIterationdepth()==-1&&startIterating==true){
				    String tmp = new String();
				    String s = null;
				    BufferedReader in = null;
	
				    // Liest Textzeilen aus der Datei in einen Vector:
				    try {
				      in = new BufferedReader(
				                          new InputStreamReader(
				                          new FileInputStream( "/tmp/testresult.txt" ) ) );
				      while( null != (s = in.readLine()) ) {
				        tmp=tmp+"\n"+s;
				      }
				    } catch( FileNotFoundException ex ) {
				    } catch( Exception ex ) {
				      System.out.println( ex );
				    } finally {
				      if( in != null )
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }
				    
				    String out=null;
				    if (query=="" || query==" "||query.length()==0) query="Could not parse";
				    out=tmp + "\n" + question + ":\n"+query+"\n";
				    
				    BufferedWriter outfile = new BufferedWriter(
	                          new OutputStreamWriter(
	                          new FileOutputStream( "/tmp/testresult.txt" ) ) );
	    
				    outfile.write(out);
				    outfile.close();
					
				}
		
				/*
				 * #################################################################################################
				 */				
				//Iteration 0
				if(getIterationdepth()==0&&startIterating==true){
				    String tmp = new String();
				    String s = null;
				    BufferedReader in = null;
	
				    // Lies Textzeilen aus der Datei in einen Vector:
				    try {
				      in = new BufferedReader(
				                          new InputStreamReader(
				                          new FileInputStream( "/tmp/answer.txt" ) ) );
				      while( null != (s = in.readLine()) ) {
				        tmp=tmp+"\n"+s;
				      }
				    } catch( FileNotFoundException ex ) {
				    } catch( Exception ex ) {
				      System.out.println( ex );
				    } finally {
				      if( in != null )
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }					
					String answer;
					answer=sendServerQuestionRequest(query);
					final_answer.add("Begin:\n"+query +"\n"+answer+" \n End");
					
				}
				/*
				 * #################################################################################################
				 */				
				//Iterration 1
				if(getIterationdepth()==1&&startIterating==true){
					
					//4, because of query + three conditions for the simple case
					if(querylist.size()==4)final_answer=simpleIteration1Case(querylist, query);
					
					//if we have more conditions, we need to change the way of replacing the uris got from wordnet etc
					
				}
				/*
				 * #################################################################################################
				 */				
				//Iterration 2
				if(getIterationdepth()==2&&startIterating==true){
					
				}
			}
			
			BufferedReader in = null;
			
		    String tmp="";
			// Lies Textzeilen aus der Datei in einen Vector:
		    try {
		      in = new BufferedReader(
		                          new InputStreamReader(
		                          new FileInputStream( "/tmp/answer" ) ) );
		      String s;
			while( null != (s = in.readLine()) ) {
		        tmp=tmp+"\n"+s;
		      }
		    } catch( FileNotFoundException ex ) {
		    } catch( Exception ex ) {
		      System.out.println( ex );
		    } finally {
		      if( in != null )
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }	
		    
		    String out="";
			for(String answer : final_answer){
				//only answered question
			//	if(!answer.contains("Error in searching Wordnet with word") && !answer.contains("EmtyAnswer")&& !answer.contains("Error in getting Properties"))out=out+ "\n"+answer+"\n";
			    
				/*
				//only questions with wordnet error
				if(answer.contains("Error in searching Wordnet with word"))out=out+ "\n"+answer+"\n";
				
				//only questions with emty answers
				if(answer.contains("EmtyAnswer"))out=out+ "\n"+answer+"\n";
*/				
				//only questions with Error in Properties
				if(answer.contains("Error in getting Properties"))out=out+ "\n"+answer+"\n";



			}
		    System.out.println(question);
		    System.out.println(out);
		    BufferedWriter outfile = new BufferedWriter(
                      new OutputStreamWriter(
                      new FileOutputStream( "/tmp/answer" ) ) );

		    outfile.write(tmp+"\n"+question+" :\n"+out);
		    outfile.close();
		}

	 
	 /**
	  * Is the function for the Case, you are in Iteration one and have only one triple of condition (s,p,o). 
	  * @param querylist
	  * @param query
	  * @return a list with answers from the Server
	  * @throws SQLException
	  * @throws JWNLException
	  */
	private ArrayList<String> simpleIteration1Case(ArrayList<String> querylist, String query) throws SQLException,
			JWNLException {
		//asking server
		String answer;
		ArrayList<String> final_answer=new ArrayList<String>();
		
		/*
		 * First try the original query on the server. If that doesnt work, try it with Iteration
		 */
		answer=sendServerQuestionRequest(query);

		if(answer.contains("EmtyAnswer")){
		
			String resource="";
			String property_to_compare_with="";
			String sideOfProperty="LEFT";

			
			int tmpcounter=0;
			for(String s : querylist){
				//we dont need the first one, because thats the query itself
				tmpcounter=tmpcounter+1;
				if(tmpcounter>=1){
					if(s.contains("LEFT")){
						sideOfProperty="LEFT";
						resource=s.replace("LEFT","");
					}
					if(s.contains("RIGHT")){
						sideOfProperty="RIGHT";
						resource=s.replace("RIGHT","");
					}
					if(s.contains("PROPERTY")){
						property_to_compare_with=s.replace("PROPERTY","");
					}
					
				}
			}
			System.out.println("Property to compare:: "+ property_to_compare_with);
			System.out.println("Resource: "+ resource);
			//contains uri AND string, every second is the string
			 HashMap<String,String> properties = new HashMap<String, String>();
			 GetRessourcePropertys property = new GetRessourcePropertys();
			 Boolean goOnAfterProperty = true;
			 try {
				 /*
				  * TODO: Have to check now, if we need a right Property or a left one
				  */
				 properties=property.getPropertys(getUriFromIndex(resource.toLowerCase(),0),sideOfProperty);
				if (properties==null){
					//final_answer.add("Error in getting Properties\n");
					
					final_answer.add("Begin:\n"+query +"\nError in getting Properties \n End");
					goOnAfterProperty=false;
				}
				//System.out.println(properties);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				
				final_answer.add("Begin:\n"+query +"\nError in getting Properties \n End");
				goOnAfterProperty=false;
				
			}
			if(goOnAfterProperty==true){
				 //property_to_compare_with mit der Liste der propertys vergleichen, und wenn der normalisierte Wert >= LvenstheinMin ist, einbauen und neue query erzeugen.
				 ArrayList<String> new_queries= new ArrayList<String>();
				 for (Entry<String, String> entry : properties.entrySet()) {
					    String key = entry.getKey();
					    String value = entry.getValue();
					 double tmp=Levenshtein.computeLevenshteinDistance(property_to_compare_with.toLowerCase(), key);
					 
					 /*
					  * TODO: Implement Normalised levensthein
					  */
				     if(tmp<=3.0){
						 //alte property uri mit neuer ersetzen:
						 String query_tmp=query;
						 String test = getUriFromIndex(property_to_compare_with.toLowerCase(),1);
						 //query_tmp=query_tmp.replace(test,properties.get(i-1));
						 query_tmp=query_tmp.replace(test,value);
						 new_queries.add(query_tmp);
					 }
					 
				 }
				 
				 System.out.println("Start Iterating Wordnet with "+property_to_compare_with+" and deept of "+explorationdepthwordnet);
				 ArrayList<String> semantics=new ArrayList<String>();
				 ArrayList<String> tmp_semantics=new ArrayList<String>();
				 ArrayList<String> result_SemanticsMatchProperties=new ArrayList<String>();
				 semantics.add(property_to_compare_with);
				 
				 //first check, if there is a singular form in the wordnet dictionary.. eg children -> child
				 String _temp_=myindex.getWordnetHelp(property_to_compare_with);
				 if(_temp_==null){
					 tmp_semantics=semantics;
				 }
				 else{
					 semantics.clear();
					 semantics.add(_temp_);
					 tmp_semantics=semantics;
				 }
				 Boolean goOnAfterWordnet = true;
				 for(int i=0;i<=explorationdepthwordnet;i++){

					 try {
						tmp_semantics=getSemantics(tmp_semantics);
						if (tmp_semantics==null){
							goOnAfterWordnet=false;
							final_answer.add("Begin:\n"+query +"\n Error in searching Wordnet with word "+semantics+" \n End");

						}
						else{
						//each word only one time
						 for(String k : tmp_semantics){
							 if(!semantics.contains(k)) semantics.add(k);
						 }
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						goOnAfterWordnet=false;
						final_answer.add("Begin:\n"+query +"\n Error in searching Wordnet with word "+semantics+" \n End");
						
					}
							 
				 }
				
				 if(goOnAfterWordnet==true){
					
					 for (Entry<String, String> entry : properties.entrySet()) {
						    String key = entry.getKey();
						    String value = entry.getValue();
						    
						for(String b : semantics){
							if(key.contains(b.toLowerCase())){
								if(!result_SemanticsMatchProperties.contains(key)){
									//create new query
								result_SemanticsMatchProperties.add(key);
								 String query_tmp=query;
								 String test = getUriFromIndex(property_to_compare_with.toLowerCase(),1);
								 query_tmp=query_tmp.replace(test,value);
								 System.out.println("New query after wordnet: "+ query_tmp);
								 new_queries.add(query_tmp);
								}
							}
						}
					}
					 
					for(String bla : new_queries){
						String answer_tmp;
						answer_tmp=sendServerQuestionRequest(bla);
						System.out.println("Antwort vom Server: "+answer_tmp);
						final_answer.add("Begin:\n"+bla +"\n"+answer_tmp+" \n End");
					}
				 }
			}
		}
		
		return final_answer;
	}
		 

	 /**
	  * Iterates thru the conditions and returns an array, where one can see, if the Property is left or right from the resource
	  * @param query
	  * @return returns an array, where one can see, if the Property is left or right from the resource
	  */
	 private static ArrayList<String> createLeftAndRightPropertyArray(String query){
		 query=query.replace("  ", " ");
		 	Pattern p = Pattern.compile (".*\\{(.*\\<http.*)\\}.*");
		    Matcher m = p.matcher (query);
		    ArrayList<String> lstquery = new ArrayList<String>();
		  	while (m.find()) {
		  		String tmp= m.group(1);
		  		tmp=tmp.replace("http://dbpedia.org/resource/","").replace("http://dbpedia.org/property/", "").replace("http://dbpedia.org/ontology/", "");
		  		
		  		//split on . for sign for end of conditions
		  		String[] firstArray=tmp.split("\\.");
		  		for(String i : firstArray){
		  			
		  			String[] secondArray=i.split(" ");
		  			//always in three counts
		  			int counter=0;
		  			for(String j : secondArray){
		  				counter=counter+1;
		  				//only one condition
		  				if(secondArray.length%3==0){
		  					if(counter==1&&j.contains("<")){
		  						//position of Property is right
		  						lstquery.add("RIGHT"+j.replace("<", "").replace(">",""));
		  					}
		  					else if(counter==3&&j.contains("<")){
		  						//position of Property is left
		  						lstquery.add("RIGHT"+j.replace("<", "").replace(">",""));
		  					}
		  					else if(counter==2){
		  						lstquery.add("PROPERTY"+j.replace("<", "").replace(">",""));
		  					}
		  					
		  					else if(j.contains("?")) lstquery.add("VARIABLE");
		  				}
		  				if(counter==0)counter=0;
		  				
		  				
		  			}
		  		}
		  	}
		  	
		  	return lstquery;
	 }
	 
	 /**
	* Method gets a String and takes the information from the templator to creat a Sparql query.
	* @param question question in natural language
	* @return ArrayList of Sparql queries.
	 * @throws SQLException 
	*/
	private ArrayList<ArrayList<String>> getQuery(String question) throws SQLException {
		ArrayList<ArrayList<String>> lstquery = new ArrayList<ArrayList<String>>();
	    Set<BasicQueryTemplate> querytemps = btemplator.buildBasicQueries(question);
	     	for (BasicQueryTemplate temp : querytemps) {
	     		
	     		ArrayList<String> lstquerynew = new ArrayList<String>();
	     		ArrayList<String> lstquerupsidedown = new ArrayList<String>();
	     		String query;
	     		String selTerms ="";
	     		for(SPARQL_Term terms :temp.getSelTerms()) selTerms=selTerms+(terms.toString())+" ";
	     		System.out.println(selTerms);
	     		
	     		String conditions = "";
	     		for(Path condition: temp.getConditions()) conditions=conditions+(condition.toString())+".";
	     		
	     		String filters="";
	     		for(SPARQL_Filter tmp : temp.getFilters()) filters=filters+tmp+" ";
	     		//System.out.println("\n");
	     		System.out.println("\n");
	        	query="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+temp.getQt().toString()+" "+selTerms+" WHERE {"+  conditions.replace("--","") + filters+"}";
	        	
	        	String conditions_new = "";
	     		for(Path condition: temp.getConditions()){
		     		//make conditions up-side-down
		     		String[] tmp_upside = condition.toString().split(" -- ");
		     		String tmp_conditions_new="";
		     		for(String con : tmp_upside) tmp_conditions_new = con +" "+tmp_conditions_new;
		     		//remove all dots befor end
		     		tmp_conditions_new=tmp_conditions_new.replace(".", "");
		     		//at the end ein .
		     		tmp_conditions_new = tmp_conditions_new + ".";
		     		
		     		//conditions_new=tmp_conditions_new;
		     		
	     			conditions_new=conditions_new + tmp_conditions_new;
	     		}
	     		

	     		
	     		System.out.println("Conditions: " + conditions);
	     		System.out.println("Conditions_new: " + conditions_new);
	     		
	     		
	        	String query_upside_down = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+temp.getQt().toString()+" "+selTerms+" WHERE {"+  conditions_new.replace("--","") +filters+ "}";
	        	String[] slots= null;
	        	int slotcounter=1;
	    		for(Slot slot : temp.getSlots()){
	    			
	    			//see below
	    			slotcounter=slotcounter+1;
	    			
	    			
	    			String slotstring=slot.toString().replace("UNSPEC","").replace("RESOURCE","").replace("{","").replace("}","");
	    			slotstring=slotstring.replace("  ","");
	    			//System.out.println(tmp);
	    			//damit auch wirklich nur ?y und nicht ?y0 ersetzt wird, einfach nach "?y " suchen.
	    			String[] array = slotstring.split(":");
	    			String replace;
	    			if(array[0].length()<2)replace = "?"+array[0]+" ";
	    			else replace="?"+array[0];

	    			
	    			//TODO: Hotfix: get rid of " PROPERTY "
	    			String _ThingGettingURIfor_=array[1];
	    			_ThingGettingURIfor_=_ThingGettingURIfor_.replace(" PROPERTY ","").toLowerCase();
	    			String hm_result=getUriFromIndex(_ThingGettingURIfor_,0);
	    		      try
	    		      {
	    		    	  if(hm_result.contains("Category:")) hm_result=hm_result.replace("Category:","");
	    		        }
	    		      catch ( Exception e )
	    		      {

	    		      }
	    			
	    		      /*always the middle slot is the property
	    		       * so count and always take the second of third to become a property
	    		       */
	    		      if(slotcounter%2==0){
	    		    	  hm_result=getUriFromIndex(_ThingGettingURIfor_,1);
	    		      }
	    		      //set back to 0 to start new
	    		      if(slotcounter==3) slotcounter=0;
	    			query=query.replace(replace, "<"+hm_result+">");
	    			query_upside_down=query_upside_down.replace(replace, "<"+hm_result+">");
	    			
	    		}
	    		lstquerupsidedown.add(query_upside_down);
	    		lstquerynew.add(query);
	    		
	    		
	    		
	    		ArrayList<String> lsttmp=createLeftAndRightPropertyArray(query);
	    		//if its lower than three, we dont have any conditions and dont need to check it.
	    		//also if the size%3 isnt 0, than something else is wrong and we dont need to test the query
	    		if(lsttmp.size()>=3&&lsttmp.size()%3==0)for(String i : lsttmp) lstquerynew.add(i);
	    		else{
	    			lstquerynew.clear();
	    			lstquerynew.add("ERROR");
	    		}
	    		
	    		lsttmp.clear();
	    		lsttmp=createLeftAndRightPropertyArray(query_upside_down);
	    		if(lsttmp.size()>=3&&lsttmp.size()%3==0)for(String i : lsttmp) lstquerupsidedown.add(i);
	    		else{
	    			lstquerupsidedown.clear();
	    			lstquerupsidedown.add("ERROR");
	    		}
	    		
	    		lstquery.add(lstquerynew);
	    		lstquery.add(lstquerupsidedown);
	     	}
	     	
	     	return lstquery;
		}
	
	 
	 
	private void saveNotParsedQuestions(String question) throws IOException{
		BufferedReader in = null;
		
	    String tmp="";
		// Lies Textzeilen aus der Datei in einen Vector:
	    try {
	      in = new BufferedReader(
	                          new InputStreamReader(
	                          new FileInputStream( "/tmp/notParsedQuestions" ) ) );
	      String s;
		while( null != (s = in.readLine()) ) {
	        tmp=tmp+"\n"+s;
	      }
	    } catch( FileNotFoundException ex ) {
	    } catch( Exception ex ) {
	      System.out.println( ex );
	    } finally {
	      if( in != null )
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }	
	    
	    String out="";
	    BufferedWriter outfile = new BufferedWriter(
                  new OutputStreamWriter(
                  new FileOutputStream( "/tmp/notParsedQuestions" ) ) );

	    outfile.write(tmp+"\n"+question);
	    outfile.close();
	}
	
	/**
	 *  
	 * @param string
	 * @param fall 1 Property 0 no Property
	 * @return
	 * @throws SQLException 
	 */
	private String getUriFromIndex(String string, int fall) throws SQLException{
		String result=null;
		//just to be sure its only 0 or 1
		if(fall!=0 && fall!=1) fall=0;
		if(fall==0){
			result=myindex.getResourceURI(string.toLowerCase());
			if(result==null)result=myindex.getPropertyURI(string.toLowerCase());
		}
		if(fall==1){
			result=myindex.getPropertyURI(string.toLowerCase());
			if(result==null){
				result=myindex.getResourceURI(string.toLowerCase());
				if(result!=null) result=result.replace("resource", "property");
			}
			
		}
		String tmp="";
		tmp=string.toLowerCase();
		tmp=tmp.replace("property","");
		tmp=tmp.replace(" ", "_");
		if(result==null) {
			if(fall==1)return "http://dbpedia.org/property/"+tmp;
			if(fall==0)return "http://dbpedia.org/resource/"+tmp;
			else{
				return result;
			}
		}
		else return result;
	}


	
	
		private static ArrayList<String> getSemantics (ArrayList<String> semantics) throws IOException, JWNLException {
			ArrayList<String> result = new ArrayList<String>();
			//result.clear();
			//try{
			try{
			for(String id :semantics){
				List<String> array_relatedNouns=null;
				List<String> array_bestsynonyms=null;
				List<String> array_siterterms=null;
				//array.clear();
					System.out.println("Wordnet Word: "+id);
					array_relatedNouns =wordnet.getRelatedNouns(id);

					array_bestsynonyms=wordnet.getBestSynonyms(POS.NOUN, id);
					
					array_siterterms=wordnet.getSisterTerms(POS.NOUN, id);
					
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
				if(array_siterterms!=null){
					for(String i:array_siterterms){
						if(!result.contains(i))result.add(i);
					}
				}
				
				
					
			}
			}
			catch(Exception e){
				if(result.isEmpty()) return null;
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
						String[] tmp_array=id.split(" ");
						if(tmp_array.length>=2){
							for(String tmp : tmp_array){
								List<String> array_relatedNouns=null;
								List<String> array_bestsynonyms=null;
								List<String> array_siterterms=null;
								//array.clear();
									//System.out.println("Wordnet Word: "+tmp);
									array_relatedNouns =wordnet.getRelatedNouns(tmp);
		
									array_bestsynonyms=wordnet.getBestSynonyms(POS.NOUN, tmp);
									
									array_siterterms=wordnet.getSisterTerms(POS.NOUN, tmp);
									
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
								if(array_siterterms!=null){
									for(String i:array_siterterms){
										if(!result.contains(i))result.add(i);
									}
								}
								
								}
						}
							
					}
					}
					catch(Exception e){
						if(result.isEmpty()) return null;
					}
				
			}
			
			if(!result.isEmpty()) return result;
			else return null;
		//	else{ return result;}
		}
		
		
		
		
	private String sendServerQuestionRequest(String query){
		//SPARQL-Endpoint of Semantic Computing Group
		String tmp="http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query="+createServerRequest(query)+"&format=text%2Fhtml&debug=on&timeout=";
		System.out.println(tmp);
		URL url;
	    InputStream is;
	    InputStreamReader isr;
	    BufferedReader r;
	    String str="";
	    String result="";

	    try {
	      url = new URL(tmp);
	      is = url.openStream();
	      isr = new InputStreamReader(is);
	      r = new BufferedReader(isr);
	      int counter=0;
	      do {
	        str = r.readLine();
	        if (str != null){
	        	result=result.concat(str);
	        		counter=counter+1;}
	      } while (str != null);
	      
	     //TODO:if counter = 5 or less, there is an empty answer from the Server! Still to Verify!
	     /* if(counter<=5){
	    	  System.out.println("Empty Answer from Server");  
	    	  return "noanswer";
	      }*/
	    } catch (MalformedURLException e) {
	      System.out.println("Must enter a valid URL");
	    } catch (IOException e) {
	      System.out.println("Can not connect");
	    }
	    
	    
	    
		return createAnswer(result);
	}
	
	
	private String createAnswer(String string){
		string=string.replace("table","");
		string=string.replace("<tr>", "");
		string=string.replace("</tr>", "");
		string=string.replace("</>","");
		string=string.replace("<th>l</th>","");
		string=string.replace("<th>x</th>","");
		string=string.replace("< class=\"sparql\" border=\"1\">","");
		string=string.replace("\n","");
		string=string.replace("    ","");
		string=string.replace("</td>","");
		string=string.replace("<td>","");
		string=string.replace("<th>callret-0</th>", "");
		string=string.replace("<th>y</th>","");
		while (string.contains("  ")) string=string.replace("  ","");
		if (string.length()==0) string="EmtyAnswer";
		//System.out.println("Stringlänge: "+string.length());
		return string;

	}
	
	
	private String createServerRequest(String query){
		String anfrage=null;
		anfrage=removeSpecialKeys(query);
	    anfrage=anfrage.replace("&lt;","<");
	    anfrage=anfrage.replace("%gt;",">");
	    anfrage=anfrage.replace("&amp;","&");
	    //anfrage=anfrage.replaceAll("#>","%23%3E%0D%0A%");
	    anfrage=anfrage.replace("#","%23");
	    anfrage=anfrage.replace(" ","+");
	    anfrage=anfrage.replace("/","%2F");
	    anfrage=anfrage.replace(":","%3A");
	    anfrage=anfrage.replace("?","%3F");
	    anfrage=anfrage.replace("$","%24");
	    //anfrage=anfrage.replaceAll("F&gt;+","F%3E%0D%0A");
	    anfrage=anfrage.replace(">","%3E");
	    anfrage=anfrage.replace("<","%3C");
	    anfrage=anfrage.replace("\"","%22");
	    anfrage=anfrage.replace("\n","%0D%0A%09");
	    anfrage=anfrage.replace("%%0D%0A%09","%09");
	    anfrage=anfrage.replace("=","%3D");
	    anfrage=anfrage.replace("@","%40");
	    anfrage=anfrage.replace("&","%26");
	    anfrage=anfrage.replace("(","%28");
	    anfrage=anfrage.replace(")","%29");
	    anfrage=anfrage.replace("%3E%0D%0A%25","%3E");
	    //anfrage=anfrage.replaceAll("\n",".%0D%0A%09");
		return anfrage;
	}
	
	private String removeSpecialKeys(String query){
		query=query.replace("\\","");
	    //query=query.replaceAll("\a","");
	    query=query.replace("\b","");
	    query=query.replace("\f","");
	    query=query.replace("\r","");
	    query=query.replace("\t","");
	   // query=query.replaceAll("\v","");
	    return query;
	}
	
	
	
	
	
}



/**
 * Cluster function
 */
/*
int length=array.length;
int [] result_array= new int[length];
for(int p =0;p<length;p++){
	 result_array[p]=0;
}
int zaehler=1;

//looking for max 3 word as one index
for(int z=length-1;z>=0;z=z-1){
	 if(z-2>=0){
		 String tmp1 = array[z];
		 String tmp2 = array[z-1];
		 String tmp3 = array[z-2]; 
		 
		 String tmpstring3=(((tmp3.concat(" ")).concat(tmp2)).concat(" ")).concat(tmp1);
		 String tmpstring2=(tmp2.concat(" ")).concat(tmp1);
		 String tmpstring1=tmp1;
		 
		 //always looking for the "biggest" match
		 if(hm.get(tmpstring3)!=null){
			 result_array[z]=zaehler;
			 result_array[z-1]=zaehler;
			 result_array[z-2]=zaehler;
			 zaehler++;
		 }
		 else{
			 if(hm.get(tmpstring2)!=null){
				 result_array[z]=zaehler;
				 result_array[z-1]=zaehler;
				 zaehler++;
			 }
			 else{
				 if(hm.get(tmpstring1)!=null){
					 result_array[z]=zaehler;
					 zaehler++;
				 }
			 }
		 }
		 
	 }
	 else{
		 if(z-1>=0){
			 String tmp1 = array[z];
			 String tmp2 = array[z-1];
			 
			 String tmpstring2=(tmp2.concat(" ")).concat(tmp1);
			 String tmpstring1=tmp1;
			 
			 //always looking for the "biggest" match

			 if(hm.get(tmpstring2)!=null){
				 result_array[z]=zaehler;
				result_array[z-1]=zaehler;
				zaehler++;
			}
			else{
				if(hm.get(tmpstring1)!=null){
					result_array[z]=zaehler;
					zaehler++;
				}
			}
		   }
		 if(z==0){
			 if(hm.get(array[z])!=null){
				result_array[z]=zaehler;
				zaehler++; 
			 }
		 }
	   }
	 }

System.out.println("###### Cluster ######");
for(int p =0;p<length;p++){
	 System.out.println(result_array[p]);
}
System.out.println("######");
*/

/*
 * 	private void create_Sparql_query_old(String string) throws JWNLException {
		String[] array= string.split(" ");

		 
		 //look, if the first word is a who!
		 if(array[0].contains("who")){
			 int position=0;
			 for(int i=0;i<array.length;i++){
				 if (array[i].contains("of")){
					 position=i;
					 break;
				 }
			 }
			 String vor_of=array[position-1];
			 String nach_of="";
			 //if there is only one element after of
			 if(array.length-position-1==1){
				 nach_of=array[position+1];
			 }
			 else{
				 for(int i=position+1; i<array.length;i++){
					 nach_of=(nach_of.concat(array[i])).concat(" ");
				 }
				 
				 //delete last emty space
				 nach_of = nach_of.substring(0, nach_of.length()-1);
			 }
			 String uri_vor_of=" ";
			 String uri_nach_of=" ";
			 
			 uri_vor_of=hm.get(vor_of);
			 uri_nach_of=hm.get(nach_of);
			 if(uri_vor_of!=null && uri_nach_of!=null){
				 uri_nach_of=uri_nach_of.replace("Category:", "");
				 uri_nach_of=uri_nach_of.replace("category:", "");
				 
				 String anfrage;
				 anfrage="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>select ?x  where { <"+uri_nach_of+"> <"+uri_vor_of+"> ?x.}";
				 
				 //if there is no answer from the server, start searching with wordnet
				 String result="";
				 result=sendServerQuestionRequest(anfrage);
				 if(result!="noanswer"){
					 System.out.println(result); 
				 }
				 else{
					 long startTime = System.currentTimeMillis();

					 System.out.println("Get Propertys of "+nach_of);
					 
					 //contains uri AND string, every second is the string
					 ArrayList<String> properties = new ArrayList<String>();
					 GetRessourcePropertys property = new GetRessourcePropertys();
					 try {
						 //using uri now, not the string
						properties=property.getPropertys(hm.get(nach_of));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 
					 System.out.println("Start Iterating Wordnet with "+vor_of+" and deept of "+iteration_deept);
					 ArrayList<String> semantics=new ArrayList<String>();
					 ArrayList<String> tmp_semantics=new ArrayList<String>();
					 ArrayList<String> result_SemanticsMatchProperties=new ArrayList<String>();
					 semantics.add(vor_of);
					 tmp_semantics=semantics;
					 for(int i=0;i<=iteration_deept;i++){

						 try {
							tmp_semantics=getSemantics(tmp_semantics);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							
						}
						 //each word only one time
						 for(String k : tmp_semantics){
							 if(!semantics.contains(k)) semantics.add(k);
						 }
								 
					 }
					 long endTime = System.currentTimeMillis();
					System.out.println("Getting Properties and Semantics took "+(endTime-startTime) +" ms\n");
					
					//TODO: Try, if it works, if you use only one loop: (b.lowerCase).contains(properties.get(h))
					for(int h=1;h<properties.size()-2;h=h+2){
						for(String b : semantics){
							//System.out.println(properties.get(h));
							//System.out.println(b);
							if(properties.get(h).contains(b.toLowerCase())){
								if(!result_SemanticsMatchProperties.contains(properties.get(h)))
								result_SemanticsMatchProperties.add(properties.get(h));
							}
						}
					}
					for(String b : result_SemanticsMatchProperties){
						vor_of=b.toLowerCase();
						uri_vor_of=hm.get(vor_of);
						if(uri_vor_of!=null){
							 anfrage="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>select ?x  where { <"+uri_nach_of+"> <"+uri_vor_of+"> ?x.}";
							 System.out.println("Answer with the property \" " + b + "\" :\n"+sendServerQuestionRequest(anfrage));
						}
					}
					long endTime2 = System.currentTimeMillis();
					System.out.println("Getting Properties, Semantics and Answer from server took "+(endTime2-startTime) +" ms");
				 }
			 }
			 
		 }
	}
*/
