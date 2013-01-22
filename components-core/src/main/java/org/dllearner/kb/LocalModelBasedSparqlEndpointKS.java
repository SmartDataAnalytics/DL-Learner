package org.dllearner.kb;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

@ComponentAnn(name = "Local Endpoint", shortName = "local_sparql", version = 0.9)
public class LocalModelBasedSparqlEndpointKS extends SparqlEndpointKS {
	
	private OntModel model;
	private String fileName;
	 private String baseDir;
	
	public LocalModelBasedSparqlEndpointKS() {
	}
	
	public LocalModelBasedSparqlEndpointKS(String ontologyURL) throws MalformedURLException {
		this(new URL(ontologyURL));
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
	
	public LocalModelBasedSparqlEndpointKS(URL ontologyURL) {
		this.fileName = ontologyURL.toString();
	}
	
	public LocalModelBasedSparqlEndpointKS(OntModel model) {
		this.model = model;
	}
	
	@Override
	public void init() throws ComponentInitException {
		Model baseModel = ModelFactory.createDefaultModel();System.out.println("Base: " + baseDir);
		 // use the FileManager to find the input file
		 InputStream in = FileManager.get().open(baseDir + File.separator + fileName);
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + fileName + " not found");
		}
		// read the RDF/XML file
		baseModel.read(in, null);
		
		model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, baseModel);
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
