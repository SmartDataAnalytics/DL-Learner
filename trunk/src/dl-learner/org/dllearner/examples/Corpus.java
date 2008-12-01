package org.dllearner.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.dllearner.examples.corpus.Sentence;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.SimpleURIMapper;

public class Corpus {

	static BufferedReader br = null;
	static File file;
	public static String namespace = "http://www.test.de/test";
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static OWLDataFactory factory;
	static OWLOntology currentOntology;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		file= new File("ling/test.export");
		init();
		try{
		Sentence sentence = nextSentence();
		sentence.processSentence();
		
		/*for (String line : sentence) {
			System.out.println(line);
		}*/
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		saveOntology();
	}
	
	
	
	public static Sentence nextSentence()throws IOException {
		List<String> retList = new ArrayList<String>();
		int retID = 0;
		String line = "";
		boolean proceed = true;
		while (proceed 	) {
			line = br.readLine();
			if (line == null){
				break;
			}else if(line.startsWith("#EOS")){
				
				proceed = false;
			}else if(line.startsWith("%%")||line.startsWith("#BOS")){
				if(line.startsWith("#BOS")){
					StringTokenizer s = new StringTokenizer(line);
					s.nextToken();
					String id = s.nextToken();
					retID = Integer.parseInt(id);
				}
				proceed = true;;
			}else{
			retList.add(line);
			}
		}
		
		return new Sentence(retID, retList);
		
	}
	
	
	
	public static void init(){
		try{
		br = new BufferedReader(new FileReader(file));
		URI	ontologyURI = URI.create(namespace);
	//URI physicalURI = new File("cache/"+System.currentTimeMillis()+".owl").toURI();
		URI physicalURI = new File("cache/tiger.owl").toURI();
		 SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
		 manager.addURIMapper(mapper);
		 try{
		 currentOntology = manager.createOntology(ontologyURI);
		 }catch(OWLOntologyCreationException e){
			 //logger.error("FATAL failed to create Ontology " + ontologyURI);
			 e.printStackTrace();
		 }
		 factory = manager.getOWLDataFactory();
		
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void addAxiom(OWLAxiom axiom){
		 AddAxiom addAxiom = new AddAxiom(currentOntology, axiom);
		 try{
		 manager.applyChange(addAxiom);
		 }catch (OWLOntologyChangeException e) {
			//TODO
			 e.printStackTrace();
		}
	 }
	
	 public static void saveOntology(){
		 try{
		 manager.saveOntology(currentOntology);
		 //manager.s
		 }catch (Exception e) {
			e.printStackTrace();
			
		}
	 }

}
