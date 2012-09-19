package org.dllearner.kb;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.dllearner.core.ComponentInitException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class LocalModelBasedSparqlEndpointKS extends SparqlEndpointKS {
	
	private OntModel model;
	
	public LocalModelBasedSparqlEndpointKS(OntModel model) {
		this.model = model;
	}
	
	public LocalModelBasedSparqlEndpointKS(String ontologyURL) throws MalformedURLException {
		this(new URL(ontologyURL));
	}
	
	public LocalModelBasedSparqlEndpointKS(URL ontologyURL) {
		Model baseModel = ModelFactory.createDefaultModel();
		 // use the FileManager to find the input file
		 InputStream in = FileManager.get().open(ontologyURL.toString());
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + ontologyURL + " not found");
		}
		// read the RDF/XML file
		baseModel.read(in, null);
		
		model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, baseModel);
	}
	
	@Override
	public void init() throws ComponentInitException {
		
	}
	
	public OntModel getModel() {
		return model;
	}
	
	@Override
	public boolean isRemote() {
		return false;
	}
	
	@Override
	public boolean supportsSPARQL_1_1() {
		return true;
	}

}
