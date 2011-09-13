package org.dllearner.algorithm.tbsl.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sparql.BasicQueryTemplate;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.templator.BasicTemplator;
import org.dllearner.algorithm.tbsl.templator.Templator;


public class TestFrontend {
	
	// MODE ::= BASIC | LEIPZIG
	static String MODE = "BASIC";  

    public static void main(String[] args) {

        System.out.println("======= SPARQL Templator =================");
        System.out.println("Running in " + MODE + " mode.");
        System.out.println("\nType ':q' to quit.");

        while (true) {
            String s = getStringFromUser("input > ").trim(); 
            
            if (s.equals(":q")) {
                System.exit(0);
            }
            
            if (MODE.equals("BASIC")) {
            	BasicTemplator btemplator = new BasicTemplator();
            	Set<BasicQueryTemplate> querytemps = btemplator.buildBasicQueries(s);
            	for (BasicQueryTemplate temp : querytemps) {
            		System.out.println(temp.toString());
            	}
            }
            else if (MODE.equals("LEIPZG")) {
            	Templator templator = new Templator();
            	Set<Template> temps = templator.buildTemplates(s);           
            	for (Template temp : temps) {
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
