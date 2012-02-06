package org.dllearner.scripts;

import static java.util.Arrays.asList;

import java.io.File;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

/**
 * This class merges several ontologies into a single one.
 * @author lorenz
 *
 */
public class OntologyMerger {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		if(args.length <= 0){
			System.exit(0);
		}
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("o", "output"), "The target output file for the merged ontology.").withRequiredArg();
		parser.acceptsAll(asList("i", "iri"), "The document IRI for the merged ontology.").withRequiredArg();
		
		OptionSet os = parser.parse(args);
		
		String targetFile = "merged.owl";
		if(os.has("o")){
			targetFile = (String) os.valueOf("o");;
		}
		String documentIRI = "http://www.semanticweb.com/merged";
		if(os.has("i")){
			targetFile = (String) os.valueOf("o");;
		}
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		for(String file : os.nonOptionArguments()){
			man.loadOntologyFromOntologyDocument(new File(file));
		}
		
		OWLOntologyMerger merger = new OWLOntologyMerger(man);
		OWLOntology merged = merger.createMergedOntology(man, IRI.create(documentIRI));
		
		man.saveOntology(merged, new RDFXMLOntologyFormat(), IRI.create(targetFile));

	}

}
