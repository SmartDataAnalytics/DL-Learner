package org.dllearner.reasoning.spatial;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

public class SpatialReasonerPostGISTest {
    private String dbName = "dl-learner-tests";
    private String dbUser = "dl-learner";
    private String dbUserPW = "dl-learner";

    private OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    private OWLDataFactory df = man.getOWLDataFactory();

    private OWLDataProperty wktLiteralDTypeProperty =
            df.getOWLDataProperty(IRI.create("http://www.opengis.net/ont/geosparql#asWKT"));
    private List<OWLObjectProperty> propertyPathToGeom =
            Lists.newArrayList(df.getOWLObjectProperty(IRI.create("http://www.opengis.net/ont/geosparql#hasGeometry")));

    @Rule
    public GenericContainer db = new PostgreSQLContainer("postgis/postgis:latest")
            .withDatabaseName(dbName)
            .withUsername(dbUser)
            .withPassword(dbUserPW)
            .withExposedPorts(5432);

    @Before
    public void setUp() {
        //
    }
}
