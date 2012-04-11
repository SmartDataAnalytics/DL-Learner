package org.dllearner.kb.sparql.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.nlp2rdf.ontology.ClassIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class SchemaIndexer {
    private static Logger log = LoggerFactory.getLogger(SchemaIndexer.class);
    private ClassIndexer classIndexer = null;
    private OntModel model;
    private File ontologySchema;

    public SchemaIndexer() {
    }

    public void init() {
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
    }

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

    public File getOntologySchema() {
        return ontologySchema;
    }

    public void setOntologySchema(File ontologySchema) {
        this.ontologySchema = ontologySchema;
    }


}