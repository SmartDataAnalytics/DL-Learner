package org.dllearner.test.sparql;

import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import org.dllearner.kb.sparql.ModelBasedSparqlQuery;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Nov 16, 2010
 * Time: 11:25:30 AM
 * <p/>
 * Test the model based Sparql query.
 */
public class ModelBasedSparqlQueryTest {


    @Test
    public void testMethods() {

        ModelBasedSparqlQuery query = new ModelBasedSparqlQuery();
        query.setModel(loadModel());
        query.setSparqlQueryString("SELECT * WHERE { ?s ?p ?o }");

        ResultSetRewindable rs = query.send();

        assert rs.size() == 17 : "Size: " + rs.size();

        query.setSparqlQueryString("SELECT * WHERE { ?s <http://example.com/father#hasChild> ?o }");
        rs = query.send();

        assert rs.size() == 4 : "Size: " + rs.size();

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
