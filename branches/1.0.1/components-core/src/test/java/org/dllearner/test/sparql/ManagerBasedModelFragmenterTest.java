package org.dllearner.test.sparql;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.dllearner.kb.sparql.ManagerBasedModelFragmenter;
import org.semanticweb.owlapi.model.OWLOntology;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 17, 2010
 * Time: 11:07:51 AM
 * <p/>
 * Test the Manager Based Model Fragmenter
 */
public class ManagerBasedModelFragmenterTest {


    @Test
    public void testMethods() throws Exception {

        ManagerBasedModelFragmenter fragmenter = new ManagerBasedModelFragmenter();

        /** Build the fragment for heinz*/
        String uri = "http://example.com/father#heinz";
        Set<String> instances = new HashSet<String>();
        instances.add(uri);

        Model model = loadModel();
        OWLOntology ontology = fragmenter.buildFragment(model, instances);

        assert ontology != null;

        System.out.println("");
    }


    /**
     * Load the father ontology from the classpath.
     *
     * @return The model loaded from the father.owl file.
     */
    protected Model loadModel() {
        Model m = ModelFactory.createDefaultModel();

        InputStream temporalSAIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/dllearner/integration/father.owl");
        BufferedInputStream bis = new BufferedInputStream(temporalSAIS);
        m.read(bis, "http://example.com/father");

        return m;
    }
}
