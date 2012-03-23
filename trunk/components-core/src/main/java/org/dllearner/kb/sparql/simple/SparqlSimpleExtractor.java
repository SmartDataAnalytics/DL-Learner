package org.dllearner.kb.sparql.simple;

import java.util.LinkedList;
import java.util.List;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.config.ConfigOption;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@ComponentAnn(name = "efficient SPARQL fragment extractor", shortName = "sparqls", version = 0.1)
public class SparqlSimpleExtractor implements KnowledgeSource {
    
    @ConfigOption(name = "endpointURL", description = "URL of the SPARQL endpoint", required = true)
    private String endpointURL = null;
    private OntModel model = null;
    @ConfigOption(name = "instances", description = "List of the instances to use", required = true)
    private List<String> instances = null;
    @ConfigOption(name = "filters", description = "List of the filters to use", required = true)
    private List<String> filters = null;
    @ConfigOption(name = "recursionDepth", description = "recursion depth", required = true)
    private int recursionDepth = 0;
    @ConfigOption(name = "defaultGraphURI", description = "default graph URI", required = true)
    private String defaultGraphURIs=null;
    private OWLOntology owlOntology;
    
    private static Logger log = LoggerFactory.getLogger(SparqlSimpleExtractor.class);
    
    public SparqlSimpleExtractor() {
        model = ModelFactory.createOntologyModel();
    }
    
    /**
     * @param args
     * @throws ComponentInitException
     */
    public static void main(String[] args) throws ComponentInitException {
        SparqlSimpleExtractor extractor = new SparqlSimpleExtractor();
        // extractor.init();
        List<String> individuals = new LinkedList<String>();
        individuals.add("People");
        individuals.add("Animals");
        extractor.setInstances(individuals);
        // System.out.println(extractor.createQuery());
    }
    
    @Override
    public void init() throws ComponentInitException {
        if (endpointURL == null) {
            throw new ComponentInitException("Parameter endpoint URL is required");
        }
        if (instances == null) {
            throw new ComponentInitException("Parameter instances is required");
        }
        if (recursionDepth == 0) {
            throw new ComponentInitException(
                    "A value bigger than 0 is required for parameter recursionDepth");
        }
        ABoxQueryGenerator aGenerator = new ABoxQueryGenerator();
        QueryExecutor executor = new QueryExecutor();
        String queryString;
        for (int i = 0; i < recursionDepth - 1; i++) {
            queryString=aGenerator.createQuery(instances, model, filters);
            log.info("SPARQL: {}", queryString);
            executor.executeQuery(queryString, endpointURL, model,defaultGraphURIs);   
        }
        queryString = aGenerator.createLastQuery(instances, model, filters);
        log.info("SPARQL: {}", queryString);
        
        executor.executeQuery(queryString, endpointURL, model, defaultGraphURIs);
        TBoxQueryGenerator tGenerator = new TBoxQueryGenerator();
        queryString = tGenerator.createQuery(model, filters, instances);
        executor.executeQuery(queryString, endpointURL, model,defaultGraphURIs);
        JenaToOwlapiConverter converter = new JenaToOwlapiConverter();
        owlOntology=converter.convert(this.model);
    }
    
    public String getEndpointURL() {
        return endpointURL;
    }
    
    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }
    
    public Model getModel() {
        return model;
    }
    
    public void setModel(OntModel model) {
        this.model = model;
    }
    
    /**
     * @return the filters
     */
    public List<String> getFilters() {
        return filters;
    }
    
    /**
     * @param filters
     *            the filters to set
     */
    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    /**
     * @return the instances
     */
    public List<String> getInstances() {
        return instances;
    }

    /**
     * @param instances the instances to set
     */
    public void setInstances(List<String> instances) {
        this.instances = instances;
    }

    /**
     * @return the recursionDepth
     */
    public int getRecursionDepth() {
        return recursionDepth;
    }

    /**
     * @param recursionDepth the recursionDepth to set
     */
    public void setRecursionDepth(int recursionDepth) {
        this.recursionDepth = recursionDepth;
    }

    /**
     * @return the defaultGraphURI
     */
    public String getDefaultGraphURIs() {
        return defaultGraphURIs;
    }

    /**
     * @param defaultGraphURI the defaultGraphURI to set
     */
    public void setDefaultGraphURIs(String defaultGraphURI) {
        this.defaultGraphURIs = defaultGraphURI;
    }

    /**
     * @return
     */
    public OWLOntology getOWLOntology() {
       return owlOntology;
    }
    
}
