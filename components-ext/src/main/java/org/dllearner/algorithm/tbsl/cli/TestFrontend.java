package org.dllearner.algorithm.tbsl.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.templator.Templator;


public class TestFrontend {

    public static void main(String[] args) {

    	Templator templator = new Templator();
  
        System.out.println("======= SPARQL Templator v0.1 =============");       
        System.out.println("\nType ':q' to quit.");

        while (true) {
            String s = getStringFromUser("input > ").trim(); 
            
            if (s.equals(":q")) {
                System.exit(0);
            }
         
            Set<Template> temps = templator.buildTemplates(s);
            
            for (Template temp : temps) {
            	System.out.println(temp.toString());
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
