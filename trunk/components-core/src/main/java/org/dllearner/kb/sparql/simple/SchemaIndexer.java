package org.dllearner.kb.sparql.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.nlp2rdf.ontology.ClassIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class SchemaIndexer {
    private static Logger log = LoggerFactory.getLogger(SchemaIndexer.class);
    private OntModel model;

    //static instantiation
    private static ClassIndexer classIndexer = new ClassIndexer();
    //remember ontologies
    private static Set<String> alreadyIndexed = new HashSet<String>();
    //set or list of urls for the ontologies
    private List<String> ontologySchemaUrls;

    public SchemaIndexer() {
    }

    public synchronized void init() {

        for (String url : ontologySchemaUrls) {
            if (!alreadyIndexed.add(url)) {
                try {
                    Monitor m0 = MonitorFactory.start("Indexer parsing ontology");
                    model = ModelFactory.createOntologyModel();
                    model.read(url, null);
                    classIndexer.index(model);
                    m0.stop();
                    log.debug("indexed " + url + " " + url);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }


    }


    /*public void init() {
       classIndexer = new ClassIndexer();
       model = ModelFactory.createOntologyModel();
       try {
           Monitor m0 = MonitorFactory.start("Indexer parsing ontology");
           model.read(new FileInputStream(ontologySchema), null);
           m0.stop();
       } catch (FileNotFoundException e) {
           log.error(e.getMessage(), e);
       }
       classIndexer.index(model);
   } */

    public OntModel getHierarchyForURI(String classUri) {
        if (classIndexer == null) {
            this.init();
        }
        return classIndexer.getHierarchyForClassURI(classUri);
    }

    public static void main(String... args) {
        SchemaIndexer i = new SchemaIndexer();
        System.out.println(i.getHierarchyForURI("http://dbpedia.org/ontology/Software"));
    }

    public void setOntologySchemaUrls(List<String> ontologySchemaUrls) {
        this.ontologySchemaUrls = ontologySchemaUrls;
    }
}