package org.dllearner.algorithm.tbsl.util;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;
import org.nlp2rdf.ontology.olia.OLiAManager;
import org.nlp2rdf.ontology.olia.OLiAOntology;

import java.util.Set;

/**
 */
public class NifExamples {
    private static Logger log = Logger.getLogger(NifExamples.class);

    public static void main(String[] args) {
        OLiAManager m = new OLiAManager();
        OLiAOntology brown = m.getOLiAOntology("http://purl.org/olia/brown-link.rdf");

        System.out.println(brown);
        String posTag = "BED";
        String oliaIndividual = null;
        if ((oliaIndividual = brown.getIndividualURIForTag(posTag)) != null) {
            log.info("The OLia Annotation individual can be null, if the ontology has a gap");
            log.info(oliaIndividual + "");
        }
        //adding pos classes from olia and olia-top
        Set<String> classes = brown.getClassURIsForTag(posTag);
        log.info("Classes found for the POS tag " + posTag);
        log.info("" + classes);

        for (String classUri : classes) {
            log.info("found: " + classUri + " for: " + posTag);
            OntModel hierarchy = brown.getHierarchy(classUri);
            for (ExtendedIterator<OntClass> it = hierarchy.listClasses(); it.hasNext(); ) {
                OntClass oc = it.next();
                log.info("flattended: " + oc);
            }
        }
    }
}
