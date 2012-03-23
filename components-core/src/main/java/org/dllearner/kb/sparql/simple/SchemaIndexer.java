package org.dllearner.kb.sparql.simple;

import org.nlp2rdf.ontology.ClassIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class SchemaIndexer {
    private static Logger log = LoggerFactory.getLogger(SchemaIndexer.class);
    private ClassIndexer classIndexer=null;
    
    public SchemaIndexer(){}
    
    public void init(){
        classIndexer=new ClassIndexer();
        OntModel model = ModelFactory.createOntologyModel();
        model.read(SchemaIndexer.class.getResourceAsStream("dbpedia_3-3.6.owl"), null);
        classIndexer.index(model);
    }
    
    public OntModel getHierarchyForURI(String classUri){
        if(classIndexer==null){
            this.init();
        }
        return classIndexer.getHierarchyForClassURI(classUri);
    }
    
    
}