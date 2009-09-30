package org.dllearner.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;



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
				URI physicalURI = url.toURI();
//				URI physicalURI = f.toURI();
				OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
				onto =  manager.loadOntologyFromPhysicalURI(physicalURI);
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
