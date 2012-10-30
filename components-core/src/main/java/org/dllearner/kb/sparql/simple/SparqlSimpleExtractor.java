package org.dllearner.kb.sparql.simple;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.OntologyFormat;
import org.dllearner.core.OntologyFormatUnsupportedException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.KB;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.dllearner.utilities.analyse.TypeOntology;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@ComponentAnn(name = "efficient SPARQL fragment extractor", shortName = "sparqls", version = 0.1)

public class SparqlSimpleExtractor extends AbstractKnowledgeSource implements OWLOntologyKnowledgeSource{


    @ConfigOption(name = "endpointURL", description = "URL of the SPARQL endpoint", required = true)
    private String endpointURL = null;
    private OntModel model = null;
    @ConfigOption(name = "instances", description = "List of the instances to use", required = true)
    private List<String> instances = null;
    @ConfigOption(name = "aboxfilter", description = "Filter for the tbox, can use variable ?s, ?p amd ?o", required = false)
    private String aboxfilter = null;
    @ConfigOption(name = "tboxfilter", description = "Filter for the tbox, can use variable ?example and ?class", required = false)
    private String tboxfilter = null;

    @ConfigOption(name = "recursionDepth", description = "recursion depth", required = true)
    private int recursionDepth = 0;

    @ConfigOption(name = "defaultGraphURI", description = "default graph URI", required = true)
    private String defaultGraphURI = null;
    @ConfigOption(name = "sparqlQuery", description = "Sparql Query", required = false)
    private String sparqlQuery = null;
    @ConfigOption(name = "ontologySchemaUrls", description = "List of Ontology Schema URLs", required = true)
    private List<String> ontologySchemaUrls = null;

    private SchemaIndexer indexer;

    private static Logger log = LoggerFactory.getLogger(SparqlSimpleExtractor.class);

    public SparqlSimpleExtractor() {
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    }

    /**
     * @param args
     * @throws ComponentInitException
     */
    public static void main(String[] args) throws ComponentInitException {
        SparqlSimpleExtractor extractor = new SparqlSimpleExtractor();
        extractor.setEndpointURL("http://live.dbpedia.org/sparql");
        extractor.setRecursionDepth(1);
        extractor.setDefaultGraphURI("http://dbpedia.org");
        List<String> instances = new ArrayList<String>(7);
        instances.add("http://dbpedia.org/resource/Democritus");
        instances.add("http://dbpedia.org/resource/Zeno_of_Elea");
        instances.add("http://dbpedia.org/resource/Plato");
        instances.add("http://dbpedia.org/resource/Socrates");
        instances.add("http://dbpedia.org/resource/Archytas");
        instances.add("http://dbpedia.org/resource/Pythagoras");
        instances.add("http://dbpedia.org/resource/Philolaus");

        extractor.setInstances(instances);
        extractor.init();
        List<String> individuals = new LinkedList<String>();
        individuals.add("People");
        individuals.add("Animals");
        extractor.setInstances(individuals);
        // System.out.println(extractor.createQuery());
    }

    public Set<String> difference(Set<String> alreadyQueriedIndividuals, OntModel model) {
        Set<String> candidates = new HashSet<String>();
        Set<String> result = new HashSet<String>();
        for (ResIterator it = model.listSubjects(); it.hasNext(); ) {
            candidates.add(it.next().getURI());
        }
        for (NodeIterator it = model.listObjects(); it.hasNext(); ) {
            RDFNode cur = it.next();
            if (cur.isURIResource() && !cur.isAnon()) {
                candidates.add(((Resource) cur).getURI());
            }
        }

        for (String candidate : candidates) {
            if (!alreadyQueriedIndividuals.contains(candidate)) {
//                System.out.println(candidate);
                result.add(candidate);
            }
        }

        return result;
    }


    @Override
    public void init() throws ComponentInitException {

        if (endpointURL == null) {
            throw new ComponentInitException(
                    "Parameter endpoint URL is required");
        }
        if (instances == null) {
            throw new ComponentInitException("Parameter instances is required");
        }
        if (recursionDepth == 0) {
            throw new ComponentInitException(
                    "A value bigger than 0 is required for parameter recursionDepth");
        }
        if (ontologySchemaUrls == null) {
            throw new ComponentInitException(
                    "An ontology schema description file (ontologyFile) in RDF is required");
        }
        
        Monitor monComp = MonitorFactory.start("Simple SPARQL Component")
                .start();
        Monitor monIndexer = MonitorFactory.start("Schema Indexer").start();
        indexer = new SchemaIndexer();
        indexer.setOntologySchemaUrls(ontologySchemaUrls);
        indexer.init();
        monIndexer.stop();
        
        TypeOntology typeOntology = new TypeOntology();

        Monitor monQueryingABox;
        QueryExecutor executor = new QueryExecutor();
        String queryString;
        Set<String> instancesSet = new HashSet<String>(instances);
        Set<String> alreadyQueried = new HashSet<String>();
        Monitor typizeModel;
        if (sparqlQuery == null) {
            ABoxQueryGenerator aGenerator = new ABoxQueryGenerator();
            for (int i = 0; i < recursionDepth; i++) {
                if (instancesSet.isEmpty()) {
                    log.warn("no new instances found more recursions (recursion {} )  {} new instances", i,instancesSet.size());

                }

                log.info("processing (recursion  {} ) {} new instances",i,instancesSet.size());
                queryString = aGenerator.createQuery(instancesSet, aboxfilter);
//                System.out.println(queryString);
                log.debug("SPARQL: {}", queryString);

                monQueryingABox = MonitorFactory.start("ABox query time");
                try{
                executor.executeQuery(queryString, endpointURL, model, defaultGraphURI);
                } catch (Throwable t){
                	t.printStackTrace();
                }
                monQueryingABox.stop();

                typizeModel=MonitorFactory.start("Typize the model");
                model=typeOntology.addTypetoJena(model, instances, null);
                typizeModel.stop();
                
                alreadyQueried.addAll(instancesSet);
                instancesSet = difference(alreadyQueried, model);


            }

            log.info("recursion depth: {} reached, {} new instances",recursionDepth,instancesSet.size());
            
            //queryString = aGenerator.createLastQuery(instances, model, filters);
            //log.debug("SPARQL: {}", queryString);

            //monQueryingABox = MonitorFactory.start("ABox query time");
            //Monitor monQueryingABox2 = MonitorFactory.start("ABox query time last query");
            //executor.executeQuery(queryString, endpointURL, model, defaultGraphURI);
            //monQueryingABox.stop();
            //monQueryingABox2.stop();

        } else {
            monQueryingABox = MonitorFactory.getTimeMonitor("ABox query time").start();
            executor.executeQuery(sparqlQuery, endpointURL, model, null);
            monQueryingABox.stop();
        }


        TBoxQueryGenerator tGenerator = new TBoxQueryGenerator();

        //TODO check if all instances are queried. model.listIndividuals().toSet()
        queryString = tGenerator.createQuery(alreadyQueried, tboxfilter);

        Monitor monQueryingTBox = MonitorFactory.start("TBox query time");

        executor.executeQuery(queryString, endpointURL, model, defaultGraphURI);
        monQueryingTBox.stop();

        Monitor monIndexing = MonitorFactory.start("Querying index and conversion");
        Set<OntClass> classes = model.listClasses().toSet();
        for (OntClass ontClass : classes) {
            OntModel hierarchy = indexer.getHierarchyForURI(ontClass.getURI());
            if (hierarchy != null) {
                model.add(hierarchy);
                log.debug("{}", model);
            }
        }

        monIndexing.stop();
        monComp.stop();
//        log.info("*******Simple SPARQL Extractor********");
//        /*for (Monitor monitor : MonitorFactory.getRootMonitor().getMonitors()) {
//            log.info("* {} *", monitor);
//        }*/
//        log.info(JamonMonitorLogger.getStringForAllSortedByLabel());
//        log.info("**************************************");
    }

    public String getEndpointURL() {
        return endpointURL;
    }

    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    public String getDefaultGraphURI() {
        return defaultGraphURI;
    }

    public void setDefaultGraphURI(String defaultGraphURI) {
        this.defaultGraphURI = defaultGraphURI;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(OntModel model) {
        this.model = model;
    }

    public String getAboxfilter() {
        return aboxfilter;
    }

    public void setAboxfilter(String aboxfilter) {
        this.aboxfilter = aboxfilter;
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

    public List<String> getOntologySchemaUrls() {
        return ontologySchemaUrls;
    }

    public void setOntologySchemaUrls(List<String> ontologySchemaUrls) {
        this.ontologySchemaUrls = ontologySchemaUrls;
    }

    public String getTboxfilter() {
        return tboxfilter;
    }

    public void setTboxfilter(String tboxfilter) {
        this.tboxfilter = tboxfilter;
    }


	@Override
	public KB toKB() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toDIG(URI kbURI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void export(File file, OntologyFormat format)
			throws OntologyFormatUnsupportedException {
		// TODO Auto-generated method stub
		
	}

    @Override
    public OWLOntology createOWLOntology(OWLOntologyManager manager) {
        JenaToOwlapiConverter converter = new JenaToOwlapiConverter();
        return converter.convert(this.model,manager);
    }

}
