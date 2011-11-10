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
import java.util.Set;

import net.didion.jwnl.JWNLException;

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
	private static HashMap<String, String> hm = new HashMap<String, String>();
	

	//Konstruktor
	public SparqlObject() throws MalformedURLException{
		wordnet = new WordNet();
		//hm=hm_new;
		hm=ParseXmlHtml.parse_xml("/home/swalter/workspace/qaldEntity2",hm);
		hm=ParseXmlHtml.parse_xml("/home/swalter/workspace/qaldEntity1",hm);
		System.out.println("Loading SPARQL Templator");
    	btemplator = new BasicTemplator();
    	templator = new Templator();
    	System.out.println("Loading SPARQL Templator Done\n");
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
	 public void create_Sparql_query(String question) throws JWNLException, IOException{
		 	//create_Sparql_query_new(string);
			
		 ArrayList<ArrayList<String>> lstquery = new ArrayList<ArrayList<String>>();
		long startParsingTime = System.currentTimeMillis();
		lstquery=getQuery(question);
		long endParsingTime = System.currentTimeMillis();
		System.out.println("The Questionparsing took "+ (endParsingTime-startParsingTime)+ " ms");
		ArrayList<String> final_answer = new ArrayList<String>();
		
		//if(!lstquery.isEmpty()){
			//for each querry
			for(ArrayList<String> querylist : lstquery){
				
				/*
				 * #################################################################################################
				 */
				//only testfunction to save the generated queries in the tmp-folder
				String query="";
				query=querylist.get(0).toString();
				if(getIterationdepth()==-1){
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
				if(getIterationdepth()==0){
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
					final_answer.add(answer);
					/*System.out.println(query);
					if (query=="" || query==" "||query.length()==0) answer="Could not parse";
					System.out.println("Antwort: " + answer);
				    String out=tmp + "\n" + "Question: "+question + "\n"+"Query: " + query +"\n Anwer: "+answer+"\n\n##############################";
				    
				    BufferedWriter outfile = new BufferedWriter(
	                          new OutputStreamWriter(
	                          new FileOutputStream( "/tmp/answer.txt" ) ) );
	    
				    outfile.write(out);
				    outfile.close();	*/			    
				}
				/*
				 * #################################################################################################
				 */				
				//Iterration 1
				if(getIterationdepth()==1){
					
					//asking server
					String answer;
					answer=sendServerQuestionRequest(query);
					
					//if Emty answer, get properties an look up the right property with levensthein
					if(answer.contains("EmtyAnswer")){
						//TODO: get all information from the query
						//TODO: maybe put the query + information in an array list of arraylist. each arraylist contains the query, the variables and the uris. Then iterate over the List and get the query for sending to server
						String rescource="";
						
						//get the resource of the query. always the last Item in the array!
						//Funktioniert!
						String resource_tmp="";
						int tmp_length=querylist.size();
						resource_tmp=querylist.get(tmp_length-1);
						String[] array_tmp = resource_tmp.split(":");
						rescource=array_tmp[1];
						
								
						//the property we are looking for is always the second last in the array!
						//Funktioniert!
						String property_to_compare_with="";
						tmp_length=querylist.size();
						//second last
						property_to_compare_with=querylist.get(tmp_length-2);
						array_tmp = property_to_compare_with.split(":");
						property_to_compare_with=array_tmp[1];
						//System.out.println("property_to_compare_with: "+property_to_compare_with);
						
						
						 //contains uri AND string, every second is the string
						//Funktioniert
						 ArrayList<String> properties = new ArrayList<String>();
						 GetRessourcePropertys property = new GetRessourcePropertys();
						 Boolean goOnAfterProperty = true;
						 try {
							 //using uri now, not the string
							properties=property.getPropertys(hm.get(rescource.toLowerCase()));
							if (properties==null){
								final_answer.add("Error in getting Properties\n");
								goOnAfterProperty=false;
							}
							//System.out.println(properties);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							final_answer.add("Error in getting Properties\n");
							goOnAfterProperty=false;
							
						}
						if(goOnAfterProperty==true){
							 //property_to_compare_with mit der Liste der propertys vergleichen, und wenn der normalisierte Wert >= LvenstheinMin ist, einbauen und neue query erzeugen.
							 Levenshtein levensthein = new Levenshtein();
							 ArrayList<String> new_queries= new ArrayList<String>();
							 for(int i =1; i<=properties.size()-2;i=i+2){
								 //double tmp=levensthein.nld(property_to_compare_with.toLowerCase(), properties.get(i).toLowerCase());
								 double tmp=levensthein.computeLevenshteinDistance(property_to_compare_with.toLowerCase(), properties.get(i).toLowerCase());
								 //create new query
								 //System.out.println(tmp);
								 //if(tmp>=LvenstheinMin){
							     if(tmp<=3.0){
									 //System.out.println(tmp);
									 //alte property uri mit neuer ersetzen:
									 String query_tmp=query;
									 query_tmp=query_tmp.replace(hm.get(property_to_compare_with.toLowerCase()),properties.get(i-1));
									 //System.out.println("hm.get(property_to_compare_with.toLowerCase(): " + hm.get(property_to_compare_with.toLowerCase()));
									 new_queries.add(query_tmp);
								 }
								 
							 }
							 
							 System.out.println("Start Iterating Wordnet with "+property_to_compare_with+" and deept of "+explorationdepthwordnet);
							 ArrayList<String> semantics=new ArrayList<String>();
							 ArrayList<String> tmp_semantics=new ArrayList<String>();
							 ArrayList<String> result_SemanticsMatchProperties=new ArrayList<String>();
							 semantics.add(property_to_compare_with);
							 tmp_semantics=semantics;
							 Boolean goOnAfterWordnet = true;
							 for(int i=0;i<=explorationdepthwordnet;i++){
	
								 try {
									tmp_semantics=getSemantics(tmp_semantics);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									//e.printStackTrace();
									goOnAfterWordnet=false;
									final_answer.add("Error in searching Wordnet\n");
									
								}
								 //each word only one time
								 for(String k : tmp_semantics){
									 if(!semantics.contains(k)) semantics.add(k);
								 }
										 
							 }
							
							 if(goOnAfterWordnet==true){
								// ArrayList<String> new_queries= new ArrayList<String>();
								
								//TODO: Try, if it works, if you use only one loop: (b.lowerCase).contains(properties.get(h))
								for(int h=1;h<properties.size()-2;h=h+2){
									for(String b : semantics){
										//System.out.println(properties.get(h));
										//System.out.println(b);
										if(properties.get(h).contains(b.toLowerCase())){
											if(!result_SemanticsMatchProperties.contains(properties.get(h))){
												//create new query
											result_SemanticsMatchProperties.add(properties.get(h));
											 String query_tmp=query;
											 query_tmp=query_tmp.replace(hm.get(property_to_compare_with.toLowerCase()),properties.get(h-1));
											 //System.out.println("hm.get(property_to_compare_with.toLowerCase(): " + hm.get(property_to_compare_with.toLowerCase()));
											 new_queries.add(query_tmp);
											}
										}
									}
								}
								 
								for(String bla : new_queries){
									String answer_tmp;
									answer_tmp=sendServerQuestionRequest(bla);
									if(!answer_tmp.contains("EmtyAnswer")){
										final_answer.add(answer_tmp);
									}
								}
							 }
						}
					}
					
				}
				/*
				 * #################################################################################################
				 */				
				//Iterration 2
				if(getIterationdepth()==2){
					
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
				
			    out=out+ "\n"+answer+"\n";
			    
			}
		    System.out.println(question);
		    System.out.println(out);
		    BufferedWriter outfile = new BufferedWriter(
                      new OutputStreamWriter(
                      new FileOutputStream( "/tmp/answer" ) ) );

		    outfile.write(tmp+"\n"+question+" :\n"+out);
		    outfile.close();
		}
		 
		 // string=string.replaceAll("?", "");
			 //create_Sparql_query_old(string);
			 
		// }

	 /**
	* Method gets a String and takes the information from the templator to creat a Sparql query.
	* @param question question in natural language
	* @return ArrayList of Sparql queries.
	*/
	private ArrayList<ArrayList<String>> getQuery(String question) {
		ArrayList<ArrayList<String>> lstquery = new ArrayList<ArrayList<String>>();
	    Set<BasicQueryTemplate> querytemps = btemplator.buildBasicQueries(question);
	     	for (BasicQueryTemplate temp : querytemps) {
	     		
	     		/*System.out.println("temp.getQt();" + temp.getQt());
	    		System.out.println("temp.getSelTerms();" + temp.getSelTerms());
	    		System.out.println("temp.getVariablesAsStringList();" + temp.getVariablesAsStringList());
	    		System.out.println("temp.getConditions();" + temp.getConditions());
	    		System.out.println("temp.getSlots();" + temp.getSlots());*/
	     		ArrayList<String> lstquerynew = new ArrayList<String>();
	     		ArrayList<String> lstquerupsidedown = new ArrayList<String>();
	     		String query;
	     		String selTerms ="";
	     		for(SPARQL_Term terms :temp.getSelTerms()) selTerms=selTerms+(terms.toString())+" ";
	     		
	     		String conditions = "";
	     		for(Path condition: temp.getConditions()) conditions=conditions+(condition.toString())+".";
	     		
	     		String filters="";
	     		for(SPARQL_Filter tmp : temp.getFilters()) filters=filters+tmp+" ";
	     		//System.out.println("\n");
	     		System.out.println("\n");
	        	query="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+temp.getQt().toString()+" "+selTerms+" WHERE { "+  conditions.replace("--","") + "}"+filters;
	        	
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
	     		
	     		
	        	String query_upside_down = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+temp.getQt().toString()+" "+selTerms+" WHERE { "+  conditions_new.replace("--","") + "}"+filters;
	        	String[] slots= null;
	    		for(Slot slot : temp.getSlots()){
	    			
	    			String tmp= slot.toString();
	    			tmp= tmp.replace("UNSPEC","");
	    			tmp= tmp.replace("RESOURCE","");
	    			tmp= tmp.replace("{","");
	    			tmp= tmp.replace("}","");
	    			tmp=tmp.replace("  ","");
	    			//System.out.println(tmp);
	    			//damit auch wirklich nur ?y und nicht ?y0 ersetzt wird, einfach nach "?y " suchen.
	    			String[] array = tmp.split(":");
	    			String replace;
	    			if(array[0].length()<2)replace = "?"+array[0]+" ";
	    			else replace="?"+array[0];
	    			//System.out.println("replace: " + replace);
	    			//hier dann den hm wert von array[1] eintragen
	    			
	    			String hm_result=hm.get(array[1].toLowerCase());
	    		      try
	    		      {
	    		    	  if(hm_result.contains("Category:")) hm_result=hm_result.replace("Category:","");
	    		        }
	    		      catch ( Exception e )
	    		      {
	    		        //System.out.println( "Das war keine Zahl!" );
	    		      }
	    			
	    			query=query.replace(replace, "<"+hm_result+">");
	    			query_upside_down=query_upside_down.replace(replace, "<"+hm_result+">");
	    			
	    		}
	    		lstquerupsidedown.add(query_upside_down);
	    		lstquerynew.add(query);
	    		
	    		//slots hinzufügen
	    		for(Slot slot : temp.getSlots()){
	    			String tmp= slot.toString();
	    			tmp= tmp.replace("UNSPEC","");
	    			tmp= tmp.replace("RESOURCE","");
	    			tmp= tmp.replace("{","");
	    			tmp= tmp.replace("}","");
	    			tmp=tmp.replace("  ","");
	    			lstquerupsidedown.add(tmp);
		    		lstquerynew.add(tmp);
	    		}
	    		//System.out.println("Query: "+query);
	    		/*lstquery.add(query);
	    		lstquery.add(query_upside_down);*/
	    		lstquery.add(lstquerynew);
	    		lstquery.add(lstquerupsidedown);
	    		
	     	}
	     	
	     	return lstquery;
		}
	
	 
	 
	 
	 
	private void doIteration(String string1, String string2) throws JWNLException{
		 long startTime = System.currentTimeMillis();

		 String string2_uri;
		 string2_uri=hm.get(string2);
		 string2_uri=string2_uri.replace("Category:", "");
		 string2_uri=string2_uri.replace("category:", "");
		 System.out.println("Get Propertys of "+string2);
		 
		 //contains uri AND string, every second is the string
		 ArrayList<String> properties = new ArrayList<String>();
		 GetRessourcePropertys property = new GetRessourcePropertys();
		 try {
			 //using uri now, not the string
			properties=property.getPropertys(hm.get(string2));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 System.out.println("Start Iterating Wordnet with "+string1+" and deept of "+explorationdepthwordnet);
		 ArrayList<String> semantics=new ArrayList<String>();
		 ArrayList<String> tmp_semantics=new ArrayList<String>();
		 ArrayList<String> result_SemanticsMatchProperties=new ArrayList<String>();
		 semantics.add(string1);
		 tmp_semantics=semantics;
		 for(int i=0;i<=explorationdepthwordnet;i++){

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
			string1=b.toLowerCase();
			String anfrage;
			String string1_uri;
			string1_uri=hm.get(string1);
			if(string1_uri!=null){
				 anfrage="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>select ?x  where { <"+string2_uri+"> <"+string1_uri+"> ?x.}";
				 System.out.println("Answer with the property \" " + b + "\" :\n"+sendServerQuestionRequest(anfrage));
			}
		}
		long endTime2 = System.currentTimeMillis();
		System.out.println("Getting Properties, Semantics and Answer from server took "+(endTime2-startTime) +" ms");
	}
	

	
	
		private static ArrayList<String> getSemantics (ArrayList<String> semantics) throws IOException, JWNLException {
			ArrayList<String> result = new ArrayList<String>();
			for(String id :semantics){
				List<String> array =wordnet.getRelatedNouns(id);
				for(String i:array){
					if(!result.contains(i))result.add(i);
				}
				
					
			}
			return result;
		}
		
		
		
		
	private String sendServerQuestionRequest(String query){
		//SPARQL-Endpoint of Semantic Computing Group
		String tmp="http://greententacle.techfak.uni-bielefeld.de:5171/sparql?default-graph-uri=&query="+createServerRequest(query)+"&format=text%2Fhtml&debug=on&timeout=";
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
