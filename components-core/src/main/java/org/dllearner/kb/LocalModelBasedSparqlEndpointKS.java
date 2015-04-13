package org.dllearner.kb;

import java.net.MalformedURLException;
import java.net.URL;

import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.dllearner.core.ComponentAnn;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@ComponentAnn(name = "Local Endpoint", shortName = "local_sparql", version = 0.9)
public class LocalModelBasedSparqlEndpointKS extends SparqlEndpointKS {
	
	private OntModel model;
	private String fileName;
	private String baseDir;
	private boolean enableReasoning = false;
	
	public LocalModelBasedSparqlEndpointKS() {
	}
	
	public LocalModelBasedSparqlEndpointKS(String ontologyURL) throws MalformedURLException {
		this(new URL(ontologyURL));
	}
	
	public LocalModelBasedSparqlEndpointKS(URL ontologyURL) {
		this.fileName = ontologyURL.toString();
	}
	
	public LocalModelBasedSparqlEndpointKS(OntModel model) {
		this.model = model;
	}
	
	public LocalModelBasedSparqlEndpointKS(Model model) {
		this.model = ModelFactory.createOntologyModel(enableReasoning ? OntModelSpec.OWL_MEM_RDFS_INF : OntModelSpec.OWL_MEM, model);
	}
	
//	@Override
//	public void init() throws ComponentInitException {
//		Model baseModel = ModelFactory.createDefaultModel();
//		 // use the FileManager to find the input file
//		 InputStream in = FileManager.get().open(baseDir + File.separator + fileName);
//		if (in == null) {
//		    throw new IllegalArgumentException(
//		                                 "File: " + fileName + " not found");
//		}
//		// read the RDF/XML file
//		baseModel.read(in, null);
//		
//		model = ModelFactory.createOntologyModel(enableReasoning ? OntModelSpec.OWL_MEM : OntModelSpec.OWL_MEM_RDFS_INF, baseModel);
//	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.kb.SparqlEndpointKS#buildQueryExecutionFactory()
	 */
	@Override
	protected QueryExecutionFactory buildQueryExecutionFactory() {
		QueryExecutionFactory qef = new QueryExecutionFactoryModel(model);
		
		// we are working on an in-memory model, but still should enable caching by default
		qef = CacheUtilsH2.createQueryExecutionFactory(qef, cacheDir, true, cacheTTL);
		
		return qef;
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
    
    public void setEnableReasoning(boolean enableReasoning) {
		this.enableReasoning = enableReasoning;
	}
    
    public boolean isEnableReasoning() {
		return enableReasoning;
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
