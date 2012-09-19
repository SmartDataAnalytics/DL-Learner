/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;



/**
 * this class was submitted to Jena Bug Tracker
 * 
 */
public class JenaConstruct {

	public static void main(String[] args) {
		try{
		URL url = new URL ("http://localhost/ontowiki/model/export/?m=http://ns.softwiki.de/req/&f=rdfxml");
		getWholeOWLAPIOntology(url);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
public static OWLOntology getWholeOWLAPIOntology(URL url){
		OWLOntology onto = null;
		try{
		File f = File.createTempFile("dllearneronto", ".rdf");
			try{
				//assemble
				
				//URL url = new URL (owbase+"model/export/?m="+namespace+"&f=rdfxml");
//				URLConnection u = url.openConnection();
//				String content = toString(u.getInputStream());
//				
//				
//				FileWriter fw = new FileWriter(f);
//				fw.write(content);
//				fw.close();
				IRI physicalIRI = IRI.create(url);
//				URI physicalURI = f.toURI();
				OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
				onto =  manager.loadOntologyFromOntologyDocument(physicalIRI);
				System.out.println(onto.getAxiomCount());
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				f.delete();
				
			}
		
		}catch (Exception e) {
			e.printStackTrace();
		}
		return onto;
	
}
	
	  public static String toString(InputStream in)
      throws IOException
{

			 BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		      String result = "";
		      String line;
		      while(null != (line = reader.readLine())){
		    	  if(line.startsWith("#")){continue;}
		              result += line + "\n";
		      }
		      return result;
}
	

}
