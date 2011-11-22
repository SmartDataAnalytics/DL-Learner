package org.dllearner.algorithm.tbsl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.dllearner.algorithm.tbsl.sparql.BasicQueryTemplate;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.templator.TemplatorHandler;


public class TestFrontend {

    static String[] GRAMMAR_FILES = {"src/main/resources/lexicon/english.lex"};
    static boolean BASIC_MODE = false; // true for BASIC mode, false for LEIPZIG mode
    
    public static void main(String[] args) {

    	TemplatorHandler handler = new TemplatorHandler(GRAMMAR_FILES);
  
        System.out.println("======= SPARQL Templator v0.1 =============");
        System.out.print("\nMode: "); 
        if (BASIC_MODE) { System.out.print("BASIC"); } else { System.out.print("LEIPZIG"); }
        System.out.println("\nType ':q' to quit.");

        while (true) {
            String s = getStringFromUser("input > ").trim(); 
            
            if (s.equals(":q")) {
                System.exit(0);
            }
         
            if (BASIC_MODE) {
            	for (BasicQueryTemplate temp : handler.buildBasicTemplates(s)) {
            		System.out.println(temp.toString());
            	}
            } else {                                
            	for (Template temp : handler.buildTemplates(s)) {
            		System.out.println(temp.toString());
            	}
            }
        }
    }

    public static String getStringFromUser(String msg) {
        String str = "";
        try {
        	System.out.println("\n===========================================\n");
            System.out.print(msg);
            str = new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
        }
        return str;
    }
}