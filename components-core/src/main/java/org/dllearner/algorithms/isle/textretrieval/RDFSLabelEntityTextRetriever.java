/**
 * 
 */
package org.dllearner.algorithms.isle.textretrieval;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.dllearner.core.owl.Entity;
import org.dllearner.kb.OWLAPIOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.base.Charsets;
import com.google.common.io.Files;


/**
 * @author Lorenz Buehmann
 *
 */
public class RDFSLabelEntityTextRetriever extends AnnotationEntityTextRetriever{
	
	public RDFSLabelEntityTextRetriever(OWLOntology ontology) {
		super(ontology, new OWLDataFactoryImpl().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
	}
	
	public RDFSLabelEntityTextRetriever(OWLAPIOntology ontology) {
		super(ontology, new OWLDataFactoryImpl().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
	}
	
	public static void main(String[] args) throws Exception {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = man.loadOntology(IRI.create("http://www.semanticbible.com/2006/11/NTNames.owl"));
		
		RDFSLabelEntityTextRetriever labelRetriever = new RDFSLabelEntityTextRetriever(ontology);
		Map<Entity, Set<String>> relevantText = labelRetriever.getRelevantText(ontology);
		SortedMap<String, String> uri2Labels = new TreeMap<String, String>();
		
		for (Entry<Entity, Set<String>> entry : relevantText.entrySet()) {
			Entity key = entry.getKey();
			Set<String> value = entry.getValue();
			uri2Labels.put(key.getName(), value.iterator().next());
		}
		
		StringBuilder csv = new StringBuilder();
		for (Entry<String, String> entry : uri2Labels.entrySet()) {
			String uri = entry.getKey();
			String label = entry.getValue();
			csv.append(uri).append(",").append(label).append("\n");
		}
		Files.write(csv, new File("semantic-bible-labels.csv"), Charsets.UTF_8);
	}
}
