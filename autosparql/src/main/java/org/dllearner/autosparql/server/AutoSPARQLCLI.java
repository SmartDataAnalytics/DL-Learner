package org.dllearner.autosparql.server;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.log4j.spi.LoggerFactory;
import org.dllearner.autosparql.server.search.Search;
import org.dllearner.autosparql.server.search.SolrSearch;

public class AutoSPARQLCLI {
	
	private static final String SOLR_SERVER_URL = "http://139.18.2.173:8080/apache-solr-1.4.1/dbpedia_resources/";
	
	private Scanner scanner;
	private Search search;
	
	public AutoSPARQLCLI(){
		scanner = new Scanner(System.in);
		search = new SolrSearch(SOLR_SERVER_URL);
	}
	
	private String readFromCLI(){
		return scanner.nextLine();
	}
	
	public void start(){
		System.out.print("Query:");
		
		String query = readFromCLI();
		
		List<String> suggestions = search.getResources(query, 10);
		
		for(int i = 0; i < suggestions.size(); i++){
			System.out.println(i+1 + ": \t" + suggestions.get(i));
		}
		
		List<String> posExamples = new ArrayList<String>();
		System.out.println("Choose positive examples from list(Abort with \"n\")");
		
		String input = scanner.next();
		while(!input.equalsIgnoreCase("n")){
			if(input.matches("[1-9,10]")){
				posExamples.add(suggestions.get(Integer.valueOf(input)+1));
			}
			input = scanner.next();
		}
		
		while(posExamples.size() < 2){
			System.out.format("We need %d more positive example(s).\n", 2-posExamples.size());
			System.out.print("http://dbpedia.org/resource/");
			String example = readFromCLI();
			posExamples.add(example);
			
		}
			
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LogManager.getLogManager().getLogger("global").setLevel(Level.WARNING);
		new AutoSPARQLCLI().start();

	}

}
