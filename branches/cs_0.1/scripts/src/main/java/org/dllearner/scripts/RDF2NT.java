/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.**/
package org.dllearner.scripts;
import java.io.File;
import java.io.PrintStream;
public class RDF2NT {
	

	    public static void main(String[] args) {
	      
	        	//String ontopath=args[0];	        	
	        	String ontopath = "examples/semantic_bible/NTNcombined.owl";
	    		convertRDF2NT(ontopath);
	       
	        
	    }
	    
	    
	    /**
	     * converts .nt file to rdf, same file name, different ending
	     * @param inputOntopath path to nt file
	     */
	    public static void convertRDF2NT(String inputOntopath){ 
	    		 
	    	try {
    		//URI inputURI = new File(inputOntopath).toURI();
    		String outputOntopath="";
//    		 outputURI
    		String ending = inputOntopath.substring(inputOntopath.lastIndexOf(".") + 1);
    		outputOntopath = inputOntopath.replace("." + ending, ".nt" );
    		//URI outputURI = new File(ontopath).toURI();
    		/*
    		 java jena.rdfcat (options|input)*
 where options are:
   -out N3  (aliases n, n3, ttl)
   -out N-TRIPLE  (aliases t, ntriple)
   -out RDF/XML  (aliases x, rdf, xml, rdfxml)
   -out RDF/XML-ABBREV (default)
   -in N3  (aliases n, n3, ttl)
   -in N-TRIPLE  (aliases t, ntriple)
   -in RDF/XML  (aliases x, rdf, xml, rdfxml)
   -include
   -noinclude (default)

 input is one of:
   -n <filename> for n3 input  (aliases -n3, -N3, -ttl)
   -x <filename> for rdf/xml input  (aliases -rdf, -xml, -rdfxml)
   -t <filename> for n-triple input  (aliases -ntriple)
 or just a URL, a filename, or - for the standard input.

    		 */
    		PrintStream p = new PrintStream(new File(outputOntopath));
    		System.setOut(p);
    		jena.rdfcat.main(new String[]{"-x", inputOntopath,"-out", "ntriple"});
    		p.flush();
    		p.close();
    		
    		
	    	}
	        catch (Exception e) {
	            System.out.println("The ontology could not be created: " + e.getMessage());
	            e.printStackTrace();
	        }
	    	
	    }
	    
	}


