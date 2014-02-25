/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A small script which adds explicit declarations for each user defined DBpedia
 * datatype to the ontology, because otherwise they will be handled as classes
 * by the OWL API because a not that intelligent parser implementation.
 * 
 * @author Lorenz Buehmann
 * 
 */
public class FixDBpediaOntology {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		URL url = new URL("http://downloads.dbpedia.org/3.9/dbpedia_3.9.owl.bz2");

		// load into JENA model
		InputStream is = new BufferedInputStream(url.openStream());
		CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
		Model dbpediaModel = ModelFactory.createDefaultModel();
		dbpediaModel.read(in, null, "RDF/XML");

		//add the datatype declarations
		StmtIterator iterator = dbpediaModel.listStatements(null, RDFS.range, (RDFNode) null);
		while (iterator.hasNext()) {
			Statement st = iterator.next();
			if (st.getObject().asResource().getURI().startsWith("http://dbpedia.org/datatype/")) {
				dbpediaModel.add(dbpediaModel.createStatement(st.getObject().asResource(), RDF.type, RDFS.Datatype));
			}
		}

		//write to disk
		dbpediaModel.write(new FileOutputStream("src/test/resources/org/dllearner/algorithms/isle/dbpedia_3.9.owl"), "RDF/XML", null);
		
		// load into OWL API ontology
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology dbpediaOntology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/org/dllearner/algorithms/isle/dbpedia_3.9.owl"));
		System.out.println(dbpediaOntology.getClassesInSignature().size());
	}

}
