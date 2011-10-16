package org.dllearner.algorithm.tbsl.exploration.Sparql;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.didion.jwnl.JWNLException;

import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.sparql.BasicQueryTemplate;
import org.dllearner.algorithm.tbsl.sparql.Path;
import org.dllearner.algorithm.tbsl.sparql.SPARQL_Term;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.templator.BasicTemplator;
import org.dllearner.algorithm.tbsl.templator.Templator;



public class SparqlObject {
	//global Variable dict
	
	//start counting with 0
	static int iteration_deept=1;
	static WordNet wordnet;
	BasicTemplator btemplator;
	Templator templator;
	HashMap<String, String> hm;
	
	/*Set<BasicQueryTemplate> querytemps = btemplator.buildBasicQueries(line);
	            	for (BasicQueryTemplate temp : querytemps) {
	            		System.out.println(temp.toString());
	            	}
	            	
	            	*/
	//Konstruktor
	public SparqlObject(HashMap<String, String> hm_new) throws MalformedURLException{
		wordnet = new WordNet();
		hm=hm_new;
		System.out.println("Loading SPARQL Templator");
    	btemplator = new BasicTemplator();
    	templator = new Templator();
    	System.out.println("Loading SPARQL Templator Done\n");
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
		 
		 System.out.println("Start Iterating Wordnet with "+string1+" and deept of "+iteration_deept);
		 ArrayList<String> semantics=new ArrayList<String>();
		 ArrayList<String> tmp_semantics=new ArrayList<String>();
		 ArrayList<String> result_SemanticsMatchProperties=new ArrayList<String>();
		 semantics.add(string1);
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
	
	

	private void create_Sparql_query_new(String string) throws JWNLException{
		String[] array_new=new String[4];
		array_new=getyy0AndQuery(string);
		if(!array_new[0].contains("error")){
			 String result=null;
			 
			 //Version 1
			 result=sendServerQuestionRequest(array_new[0]);
			 if(result!="noanswer"){
				// System.out.println("Version1");
				 System.out.println(result); 
			 }
			 else{
				 doIteration(array_new[3],array_new[2]);
			 }
			 //Version2
			/* else{
				 result=sendServerQuestionRequest(array_new[1]);
				 if(result!="noanswer"){
					 System.out.println("Version2");
					 System.out.println(result); 
				 }
			 }*/
		}
		
	}
	 public void create_Sparql_query(String string) throws JWNLException{
		 	//create_Sparql_query_new(string);
			
     	Set<BasicQueryTemplate> querytemps = btemplator.buildBasicQueries(string.toLowerCase());
     	for (BasicQueryTemplate temp : querytemps) {
     		
     		System.out.println("temp.getQt();" + temp.getQt());
    		System.out.println("temp.getSelTerms();" + temp.getSelTerms());
    		System.out.println("temp.getVariablesAsStringList();" + temp.getVariablesAsStringList());
    		System.out.println("temp.getConditions();" + temp.getConditions());
    		System.out.println("temp.getSlots();" + temp.getSlots());
    		
     		String query;
     		SPARQL_Term selTerms = null;
     		for(SPARQL_Term terms :temp.getSelTerms()) selTerms=terms;
     		
     		Path conditions = null;
     		for(Path condition: temp.getConditions()) conditions=condition;
     		
     		
     		
     		//System.out.println("\n");
     		System.out.println("\n");
        	query=temp.getQt().toString()+" "+selTerms.toString()+" { "+  conditions.toString().replace("--","") + "} ";
        	
        	String[] slots= null;
    		for(Slot slot : temp.getSlots()){
    			
    			//hier muss dann noch die abfrage aus der hm raus, also das direkt die uri eingebettet wird.
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
    			query=query.replace(replace, "<"+hm.get(array[1])+">");
    			
    		}
    		System.out.println(query);
     	}
		 // string=string.replaceAll("?", "");
			 //create_Sparql_query_old(string);
			 
		 }




	private String[] getyy0AndQuery(String string) {
		String teststring="";
		String[] return_array = new String[4];
		Set<BasicQueryTemplate> querytemps = btemplator.buildBasicQueries(string);
		for (BasicQueryTemplate temp : querytemps) {
			teststring=temp.toString();
			System.out.println(teststring);
		}
		teststring=teststring.replace("\n", "");
		String[] array_tmp=teststring.split("\\}");
		
		//only for y and y0
		String sparqlquery="";
		String y="";
		String y0="";
		for(String i : array_tmp){
			i=i.replace("	", "");
			String[] tmp=null;
			if(i.contains("SELECT")) sparqlquery=i.concat("}");
			if(i.contains("y0")){ 
				y0=i;
				tmp=y0.split("\\{");
				y0=tmp[1];}
			if(i.contains("y")&& !i.contains("y0")){
				y=i;
				tmp=y.split("\\{");
				y=tmp[1];
			}
		}
		String uri1=hm.get(y0);
		String uri2=hm.get(y);
		if(uri1!=null && uri2!=null){
			uri1=uri1.replace("Category:", "");
			uri1=uri1.replace("category:", "");
			uri2=uri2.replace("Category:", "");
			uri2=uri2.replace("category:", "");
			String anfrage1;
			anfrage1="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>select ?x  where { <"+uri1+"> <"+uri2+"> ?x.}";
			String anfrage2;
			anfrage2="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>select ?x  where { <"+uri2+"> <"+uri1+"> ?x.}";
			return_array[0]=anfrage1;
			return_array[1]=anfrage2;
			return_array[2]=y0;
			return_array[3]=y;
		}
		else{
			return_array[0]="error";
		}
		return return_array;
		
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
	      
	      if(result.isEmpty()) System.out.println("HALOSHSS");
	      //TODO:if counter = 5 or less, there is an empty answer from the Server! Still to Verify!
	      if(counter<=5){
	    	  System.out.println("Empty Answer from Server");  
	    	  return "noanswer";
	      }
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
