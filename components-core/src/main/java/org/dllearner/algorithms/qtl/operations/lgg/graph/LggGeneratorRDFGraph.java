package org.dllearner.algorithms.qtl.operations.lgg.graph;

import com.google.common.collect.Sets;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.dllearner.algorithms.qtl.util.SPARQLEndpointEx;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;

import java.util.Set;

/**
 * @author Lorenz Buehmann
 */
public class LggGeneratorRDFGraph {

    /**
     * Computes the syntactic LGG also known as cover graph of the two given graphs.
     *
     * @param g1 first RDF graph
     * @param g2 second RDF graph
     * @return the cover graph resp. syntactic LGG
     */
    public Model lgg(Model g1, Model g2) {
        Model lgg = ModelFactory.createDefaultModel();

        // iterate over predicates occuring in both graphs
        StmtIterator iter1 = g1.listStatements();
        StmtIterator iter2 = g1.listStatements();

        while(iter1.hasNext()) {
            Statement s1 = iter1.next();
            while(iter2.hasNext()) {
                Statement s2 = iter2.next();

                if(s1.getPredicate().equals(s2.getPredicate())) {
                    Statement s12 = lgg(lgg, s1, s2);
                    lgg.add(s12);
                }
            }
        }

        // remove redundant edges
        prune(lgg);

        return lgg;
    }

    private void prune(Model m) {
        StmtIterator iter = m.listStatements(null, RDF.type, (RDFNode) null);
        while(iter.hasNext()) {
            Statement st = iter.next();
            if(st.getObject().isAnon() && !m.contains(st.getObject().asResource(), null)) {
                iter.remove();
            };
        }
    }

    private Statement lgg(Model model, Statement s1, Statement s2) {
        Resource s = s1.getSubject().equals(s2.getObject())
                ? s1.getSubject()
                : model.createResource(AnonId.create(s1.getSubject().toString() + s2.getSubject().toString()));
        RDFNode o = s1.getObject().equals(s2.getObject())
                ? s1.getObject()
                : model.createResource(AnonId.create(s1.getObject().toString() + s2.getObject().toString()));
        return model.createStatement(s, s1.getPredicate(), o);
    }

    public static void main(String[] args) throws ComponentInitException {
        SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
        SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
        ks.init();

        Set<String> ignoredProperties = Sets.newHashSet(
                "http://dbpedia.org/ontology/abstract",
                "http://dbpedia.org/ontology/wikiPageID",
                "http://dbpedia.org/ontology/wikiPageRevisionID",
                "http://dbpedia.org/ontology/wikiPageID","http://www.w3.org/ns/prov#wasDerivedFrom", "http://dbpedia.org/ontology/wikiPageDisambiguates",
                "http://dbpedia.org/ontology/wikiPageExternalLink");

        ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
        cbdGen.setIgnoredProperties(ignoredProperties);
        cbdGen.setAllowedPropertyNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));
        cbdGen.setAllowedClassNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));

        int depth = 2;

        String ex1 = "http://dbpedia.org/resource/Taucha";
        String ex2 = "http://dbpedia.org/resource/Borna";

//        String ex1 = "http://dbpedia.org/resource/Leipzig";
//        String ex2 = "http://dbpedia.org/resource/Dresden";

        Model m1 = cbdGen.getConciseBoundedDescription(ex1, depth);
        Model m2 = cbdGen.getConciseBoundedDescription(ex2, depth);

        LggGeneratorRDFGraph lggGen = new LggGeneratorRDFGraph();
        Model lgg = lggGen.lgg(m1, m2);

        m1.write(System.out, Lang.TURTLE.getName(), "http://dbpedia.org/resource/");
        m2.write(System.out, Lang.TURTLE.getName(), "http://dbpedia.org/resource/");

        lgg.write(System.out, Lang.TURTLE.getName(), "http://dbpedia.org/resource/");

    }
}
