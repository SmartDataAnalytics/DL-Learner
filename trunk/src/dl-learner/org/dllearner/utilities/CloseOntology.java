package org.dllearner.utilities;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.OWLAPIReasoner;

public class CloseOntology {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String ontopath="examples/krkworking/test.owl";
		ontopath = args[0];
		File file = new File(ontopath);
		
		//System.out.println(file.getAbsolutePath());
		System.out.println(file.toURI());
		
		
		try{
		URI inputURI = file.toURI();
		URI outputURI;
		OWLFile owlFile=new OWLFile();
		owlFile.setURL(inputURI.toURL());
		
		Set<KnowledgeSource> ks = new HashSet<KnowledgeSource>();
		ks.add(owlFile);
		OWLAPIReasoner owlapireasoner = new OWLAPIReasoner(ks);
		owlapireasoner.init();
		OntologyCloserOWLAPI oc= new OntologyCloserOWLAPI(owlapireasoner);
		oc.testForTransitiveProperties(true);
		String ending = ontopath.substring(ontopath.lastIndexOf(".") + 1);
		/*ontopath=ontopath.replace("."+ending, "_test."+ending);
		file=new File(ontopath);
		URI outputURI = file.toURI();
		oc.writeOWLFile(outputURI);*/
		System.out.println("Attempting to close");
		oc.applyNumberRestrictionsConcise();
		System.out.println("Finished, preparing output");
		//String ending = ontopath.substring(ontopath.lastIndexOf(".") + 1);
		ontopath=ontopath.replace("."+ending, "_closedConcise."+ending);
		file=new File(ontopath);
		outputURI = file.toURI();
		oc.writeOWLFile(outputURI);
		
		//ontopath=ontopath.replace("_closed."+ending, "_ClosedConcise."+ending);
		//file=new File(ontopath);
		//outputURI = file.toURI();
		//oc.writeOWLFile(outputURI);
		
		
		//System.out.println(ontopath);
		
		/*manager.saveOntology(ontology, new OWLXMLOntologyFormat(),
						physicalURI2);

		manager.removeOntology(ontology.getURI());
		*/
		
		}catch (Exception e) {e.printStackTrace();}
	}

}
