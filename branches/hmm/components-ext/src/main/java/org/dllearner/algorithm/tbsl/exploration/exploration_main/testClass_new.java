package org.dllearner.algorithm.tbsl.exploration.exploration_main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dllearner.algorithm.tbsl.exploration.Index.SQLiteIndex;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Elements;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Hypothesis;
import org.dllearner.algorithm.tbsl.exploration.Sparql.Template;
import org.dllearner.algorithm.tbsl.exploration.Sparql.TemplateBuilder;
import org.dllearner.algorithm.tbsl.exploration.Sparql.queryInformation;
import org.dllearner.algorithm.tbsl.exploration.Utils.Query;
import org.dllearner.algorithm.tbsl.exploration.Utils.QueryPair;
import org.dllearner.algorithm.tbsl.exploration.modules.IterationModule;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.templator.BasicTemplator;

public class testClass_new {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		// TODO Auto-generated method stub
		ArrayList<Template> temp_list_result = new ArrayList<Template>();
		
		/*
		 * nice
		 * 
		 */
		/*PrintStream out = new PrintStream(new FileOutputStream("/home/swalter/Dokumente/outputTEST.txt"));
		System.setOut(out);
		*/
		
		BasicTemplator btemplator = new BasicTemplator();
    	btemplator.UNTAGGED_INPUT = false;
		SQLiteIndex myindex = new SQLiteIndex();
		
		WordNet wordnet = new WordNet();
		
		TemplateBuilder testobject = new TemplateBuilder(btemplator, myindex);
		
		String filepath = "/home/swalter/Dokumente/Auswertung/";
		//String file="very_small.xml";
		String file="XMLDateien/dbpedia-train-tagged-new.xml";
		//line="/home/swalter/Dokumente/Auswertung/XMLDateien/dbpedia-train-tagged-new.xml";
		long start = System.currentTimeMillis();
		
		//String question = "Is the wife of president Obama called Michelle?";
		
		//String question = "Who is the mayor of Berlin?";
		
		/*
		 * Original eine resource zwei properties, nachher nur noch eine Resource und eine property
		 */
		//String question ="Who is the daughter of Bill Clinton married to?";
		
		long start_template = System.currentTimeMillis();
		//temp_list_result=testobject.createTemplates(question);
		
		Map<QueryPair,String> tm = new HashMap<QueryPair, String>();
		
		/*
		 * Generate Templates
		 */
		ArrayList<queryInformation> list_of_structs = new ArrayList<queryInformation>();
		list_of_structs=generateStruct(filepath+file);
		String result="";
		for(queryInformation s : list_of_structs){
			ArrayList<Template> temp_list = new ArrayList<Template>();
			if(!s.getQuery().contains("Natalie/NNP Portman/NNP")){
				
			
			temp_list=testobject.createTemplates(s.getQuery().replace("<[CDATA[", "").replace("]]>", ""));
			
			/*for(Template t : temp_list){
				temp_list_result.add(t);
				/*try {
					result+=t.getElm().printToString()+"\n";
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
			}*/
			//}*/
			}
		}
		
	/*	long stop_template = System.currentTimeMillis();
		
		long start_iteration = System.currentTimeMillis();
		long time_generatingElements=0;
		long time_part1=0;
		long time_part2=0;
		
		for(Template t : temp_list_result){
			time_generatingElements+=t.getTime_generateElements();
			time_part1+=t.getTime_part1();
			time_part2+=t.getTime_part2();
			try{
				//t.getElm().printAll();
				ArrayList<ArrayList<Hypothesis>> blub = IterationModule.doIteration(t.getElm(),t.getHypothesen(),t.getCondition(),"LEVENSTHEIN",myindex,wordnet);
				t.setHypothesenLevensthein(blub);
				
				//t.printAll();
			}
			catch (Exception e){
				
			}
		
		}*/
		/*long stop_iteration = System.currentTimeMillis();
		System.out.println("The Iteration and Levensthein Mode took "+ (stop_iteration-start_iteration)+"ms");
		System.out.println("The Iteration and Levensthein Mode took for one Template "+ (stop_iteration-start_iteration)/temp_list_result.size()+"ms");
		float time_template =stop_template-start_template;
		float time_Templator=temp_list_result.get(0).getTime_Templator();
		System.out.println("Generating the Template took  "+ time_template+"ms");
		System.out.println("Generating one Template took  "+ time_template/temp_list_result.size()+"ms");
		System.out.println("Templator took "+ time_Templator+"ms");
		System.out.println("Generating the Templates Without Parser "+ (time_template-time_Templator)+"ms");
		System.out.println("Average Time getting Properties etc "+ (time_generatingElements/temp_list_result.size())+"ms");
		System.out.println("Average Time part1 "+ (time_part1/temp_list_result.size())+"ms");
		System.out.println("Average Time part2 "+ (time_part2/temp_list_result.size())+"ms");
		System.out.println("Time getting Properties etc "+ (time_generatingElements)+"ms");
		System.out.println("DONE");*/
		
		
		
		
		/*
		 * Create Query for each Template
		 */
		
		//Map<QueryPair,String> tm = new HashMap<QueryPair, String>();
		
		/*for(Template t : temp_list_result){
			//t.printAll();
			ArrayList<QueryPair> qp = Query.returnSetOfQueries(t, "LEVENSTHEIN");
			for(QueryPair p : qp){
				tm.put(p, t.getQuestion());  
			}
		}*/
		
		
		
		/*
		 * Get Elements for Each Resource and Class
		 */
		
		/*
		System.out.println("Duration in ms: " + (stop - start));
		writeStringToFile(result,filepath,file,start,stop);		*/
		
		/*
		 * Write Results in File
		 */
		long stop = System.currentTimeMillis();
	//	writeQueriesInFile(tm,filepath,file,start,stop );
		writeTemplatesInFile(temp_list_result,filepath,file,start,stop );
        
	}
	

	private static void writeTemplatesInFile(ArrayList<Template> temp_list_result, String filepath,String given_file, float start, float stop ) throws IOException{
		File file = new File(filepath+"Ausgabe"+stop+".txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        int anzahl1 =1;
		for(Template t: temp_list_result){
			//t.printAll();
			String result ="";
			result+="###### Template ######\n";
			result+="question: "+t.getQuestion()+"\n";
			result+="condition: "+t.getCondition()+"\n";
			//System.out.println("hypotesen: "+hypothesen);
			int anzahl = 1;
			for(ArrayList<Hypothesis> x : t.getHypothesen()){
				result+="\nSet of Hypothesen"+anzahl+":\n";
				anzahl+=1;
				for ( Hypothesis z : x){
					result+="%%%%%%%%%%%"+"\n";
					result+="Variable: "+z.getVariable()+"\n";
					result+="Name: "+z.getName()+"\n";
					result+="Uri: " + z.getUri()+"\n";
					result+="Type: " + z.getType()+"\n";
					result+="Rank: "+z.getRank()+"\n";
						result+="%%%%%%%%%%%"+"\n";
				}
			}
			
			anzahl = 1;
			for(ArrayList<Hypothesis> x : t.getHypothesenLevensthein()){
				result+="\nSet of LevenstheinHypothesen"+anzahl+":\n";
				anzahl+=1;
				for ( Hypothesis z : x){
					result+="%%%%%%%%%%%"+"\n";
					result+="Variable: "+z.getVariable()+"\n";
					result+="Name: "+z.getName()+"\n";
					result+="Uri: " + z.getUri()+"\n";
					result+="Type: " + z.getType()+"\n";
					result+="Rank: "+z.getRank()+"\n";
						result+="%%%%%%%%%%%"+"\n";
				}
			}
			
			result+="\n";
			result+="queryType: "+t.getQueryType()+"\n";
			result+="selectTerm: "+t.getSelectTerm()+"\n";
			result+="having: "+t.getHaving()+"\n";
			result+="filter: "+t.getFilter()+"\n";
			result+="OrderBy: "+t.getOrderBy()+"\n";
			result+="limit: "+t.getLimit()+"\n";
			result+="Overalltime: "+t.getOverallTime()+"ms\n";
			result+="Time for Templator: "+t.getTime_Templator()+"ms\n";
			result+="////////////////////////////////////////////////////////////////////\n\n\n";
			bw.write(result+"\n");
			//bw.flush();
			System.out.println("Done "+anzahl1);
			anzahl1 +=1;
			
		}
		String result="";
		result+="Time over generating All Templates: "+(stop-start)+"ms\n";
		result+="Average Time for one Template: "+((stop-start)/temp_list_result.size())+"ms\n";
		result+="Overall created Templates: "+temp_list_result.size();
		//System.out.println(result);
		

        bw.write(result);
        bw.flush();
        bw.close();
	}
	
	
	private static void writeQueriesInFile(Map<QueryPair,String> tm, String filepath,String given_file, float start, float stop ) throws IOException{
		File file = new File(filepath+"Queries"+stop+given_file.replace(".xml", "")+".txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        String result="";
        /*
         * for(String key : map.keySet())
    {
      System.out.print("Key: " + key + " - ");
      System.out.print("Value: " + map.get(key) + "\n");
    }
         */
		for(QueryPair key : tm.keySet()){
			result+=tm.get(key)+": "+key.getQuery()+" "+key.getRank()+"\n";
		}
		result+="OverAll Time: "+(stop-start)+"ms\n";
        bw.write(result);
        bw.flush();
        bw.close();
	}
	
	private static void writeStringToFile(String result,String filepath,String given_file, float start, float stop ) throws IOException{
		File file = new File(filepath+"Sonstiges"+stop+given_file.replace(".xml", "")+".txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(result);
        bw.flush();
        bw.close();
	}
	
	
	
	

private static ArrayList<queryInformation> generateStruct(String filename) {
	System.out.println("In generate Struct");
	String XMLType=null;
	
	BufferedReader in = null;
	
    String tmp="";
	// Lies Textzeilen aus der Datei in einen Vector:
    try {
      in = new BufferedReader(
                          new InputStreamReader(
                          new FileInputStream(filename) ) );
      String s;
	while( null != (s = in.readLine()) ) {
        tmp=tmp+s;
        //System.out.println(tmp);
      }
    } catch( FileNotFoundException ex ) {
    } catch( Exception ex ) {
      System.out.println( ex );
    } finally {
      if( in != null )
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }	
    
    
   // System.out.println("XML read in");
	//System.out.println(tmp);
    String string=tmp;
    Pattern p = Pattern.compile (".*\\<question(.*)\\</question\\>.*");
    Matcher m = p.matcher (string);
    
    
    if(string.contains("id=\"dbpedia-train\"><question")){
    	string=string.replace("id=\"dbpedia-train\"><question", "");
    	XMLType="dbpedia-train";
    	System.out.println("dbpedia-train");
    }
    if(string.contains("id=\"dbpedia-test\"><question")){
    	string=string.replace("id=\"dbpedia-test\"><question", "");
    	XMLType="dbpedia-test";
    	System.out.println("dbpedia-test");
    }
    ArrayList<queryInformation> querylist = new ArrayList<queryInformation>();
    if(string.contains("</question><question")){
    	//System.out.println("true");
    }
    else System.out.println("false");
    String [] bla = string.split("</question><question");
    //System.out.println(bla.length);
    for(String s : bla){
    	//System.out.println("in bla");
    	String query="";
    	String type="";
   	 	boolean fusion=false;
   	 	boolean aggregation=false;
   	 	boolean yago=false;
   	 	String id="";
   	 
    	//Pattern p1= Pattern.compile("(id.*)\\</string\\>\\<keywords\\>.*\\</keywords\\>\\<query\\>.*");
   	 Pattern p1= Pattern.compile("(id.*)\\</string\\>\\<keywords\\>.*");
    	Matcher m1 = p1.matcher(s);
    	//System.out.println("");
    	while(m1.find()){
    		//System.out.println(m1.group(1));
    		Pattern p2= Pattern.compile(".*><string>(.*)");
	    	Matcher m2 = p2.matcher(m1.group(1));
	    	while(m2.find()){
	    		//System.out.println("Query: "+ m2.group(1));
	    		query=m2.group(1).replace("<![CDATA[", "");
	    		query=query.replace("CDATA", "");
	    		query=query.replace("CDATA", "");
	    		query=query.replace("![", "");
	    		query=query.replace("[", "");
	    		query=query.replace("<", "");
	    	}
	    	Pattern p3= Pattern.compile("id=\"(.*)\" answer.*");
	    	Matcher m3 = p3.matcher(m1.group(1));
	    	while(m3.find()){
	    		//System.out.println("Id: "+ m3.group(1));
	    		id=m3.group(1);
	    	}
	    	
	    	Pattern p4= Pattern.compile(".*answertype=\"(.*)\" fusion.*");
	    	Matcher m4 = p4.matcher(m1.group(1));
	    	while(m4.find()){
	    		//System.out.println("answertype: "+ m4.group(1));
	    		type=m4.group(1);
	    	}
	    	
	    	Pattern p5= Pattern.compile(".*fusion=\"(.*)\" aggregation.*");
	    	Matcher m5 = p5.matcher(m1.group(1));
	    	while(m5.find()){
	    		//System.out.println("fusion: "+ m5.group(1));
	    		if(m5.group(1).contains("true"))fusion=true;
	    		else fusion=false;
	    	}
	    	
	    	Pattern p6= Pattern.compile(".*aggregation=\"(.*)\" yago.*");
	    	Matcher m6 = p6.matcher(m1.group(1));
	    	while(m6.find()){
	    		//System.out.println("aggregation: "+ m6.group(1));
	    		if(m6.group(1).contains("true"))aggregation=true;
	    		else aggregation=false;
	    	}
	    	
	    	Pattern p7= Pattern.compile(".*yago=\"(.*)\" ><string>.*");
	    	Matcher m7 = p7.matcher(m1.group(1));
	    	while(m7.find()){
	    		//System.out.println("yago: "+ m7.group(1));
	    		if(m7.group(1).contains("true"))yago=true;
	    		else yago=false;
	    	}
	    	
	    	
	    	
    	}
    	queryInformation blaquery=new queryInformation(query, id,type,fusion,aggregation,yago,XMLType,false);
    	if(id!=""&&id!=null) querylist.add(blaquery);
    }
   /* for(queryInformation s : querylist){
    	System.out.println("");
    	if(s.getId()==""||s.getId()==null)System.out.println("NO");
		System.out.println("ID: "+s.getId());
		System.out.println("Query: "+s.getQuery());
		System.out.println("Type: "+s.getType());
		System.out.println("XMLType: "+s.getXMLtype());
	}*/
    return querylist;
}
}