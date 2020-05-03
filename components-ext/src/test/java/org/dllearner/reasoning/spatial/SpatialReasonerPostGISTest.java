package org.dllearner.reasoning.spatial;

import com.google.common.collect.Lists;
import org.apache.commons.compress.utils.Sets;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.utils.spatial.SpatialKBPostGISHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubObjectPropertyOfAxiomImpl;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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
    private List<OWLProperty> geometryPropertyPath;

    private SpatialKBPostGISHelper getKBHelper() {
        return new SpatialKBPostGISHelper(propertyPathToGeom, wktLiteralDTypeProperty);
    }

    private OWLNamedIndividual i(String localPart) {
        return df.getOWLNamedIndividual(IRI.create(SpatialKBPostGISHelper.ns + localPart));
    }

    @Rule
    public GenericContainer db = new PostgreSQLContainer("postgis/postgis:latest")
            .withDatabaseName(dbName)
            .withUsername(dbUser)
            .withPassword(dbUserPW)
            .withExposedPorts(5432);

    @Before
    public void setUp() {
        geometryPropertyPath = new ArrayList<>();
        geometryPropertyPath.addAll(propertyPathToGeom);
        geometryPropertyPath.add(wktLiteralDTypeProperty);
    }

    @Test
    public void testGetIndividualsConnectedWith() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7999 51.0610)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7999 51.0610)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7888 51.0606)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7888 51.0610,13.7900 51.0600,13.7940 51.0610,13.7954 51.0602)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8019 51.0579,13.7923 51.0555,13.7934 51.0590,13.7954 51.0602)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8041 51.0642,13.8030 51.0632,13.8041 51.0627,13.8052 51.0636)");

        // polygons
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.7888 51.0606,13.7958 51.0622,13.8013 51.0611,13.7976 51.0594,13.7888 51.0606))");

        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7962 51.0589,13.7971 51.0600,13.7995 51.0586,13.7962 51.0589))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.7774 51.0660,13.7774 51.0646,13.7812 51.0648,13.7808 51.0663,13.7774 51.0660))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);

        Set<OWLIndividual> result =
                reasoner.getIndividualsConnectedWith(feature001).collect(Collectors.toSet());

        assertTrue("f1-f1", result.contains(feature001));
        assertTrue("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertTrue("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature002).collect(Collectors.toSet());

        assertTrue("f2-f1", result.contains(feature001));
        assertTrue("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertTrue("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature003).collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertTrue("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertTrue("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature004).collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertTrue("f4-f4", result.contains(feature004));
        assertTrue("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertTrue("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature005).collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertTrue("f5-f4", result.contains(feature004));
        assertTrue("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertTrue("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature006).collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertTrue("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature007).collect(Collectors.toSet());

        assertTrue("f7-f1", result.contains(feature001));
        assertTrue("f7-f2", result.contains(feature002));
        assertTrue("f7-f3", result.contains(feature003));
        assertTrue("f7-f4", result.contains(feature004));
        assertTrue("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertTrue("f7-f7", result.contains(feature007));
        assertTrue("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature008).collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertTrue("f8-f7", result.contains(feature007));
        assertTrue("f8-f8", result.contains(feature008));
        assertFalse("f8-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature009).collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertFalse("f9-f8", result.contains(feature007));
        assertTrue("f9-f9", result.contains(feature009));
    }

    @Test
    public void testIsConnectedWith() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7999 51.0610)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7999 51.0610)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7888 51.0606)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7888 51.0610,13.7900 51.0600,13.7940 51.0610,13.7954 51.0602)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8019 51.0579,13.7923 51.0555,13.7934 51.0590,13.7954 51.0602)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8041 51.0642,13.8030 51.0632,13.8041 51.0627,13.8052 51.0636)");

        // polygons
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.7888 51.0606,13.7958 51.0622,13.8013 51.0611,13.7976 51.0594,13.7888 51.0606))");

        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7962 51.0589,13.7971 51.0600,13.7995 51.0586,13.7962 51.0589))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.7774 51.0660,13.7774 51.0646,13.7812 51.0648,13.7808 51.0663,13.7774 51.0660))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);

        assertTrue("f1-f1", reasoner.isConnectedWith(feature001, feature001));
        assertTrue("f1-f2", reasoner.isConnectedWith(feature001, feature002));
        assertFalse("f1-f3", reasoner.isConnectedWith(feature001, feature003));
        assertFalse("f1-f4", reasoner.isConnectedWith(feature001, feature004));
        assertFalse("f1-f5", reasoner.isConnectedWith(feature001, feature005));
        assertFalse("f1-f6", reasoner.isConnectedWith(feature001, feature006));
        assertTrue("f1-f7", reasoner.isConnectedWith(feature001, feature007));
        assertFalse("f1-f8", reasoner.isConnectedWith(feature001, feature008));
        assertFalse("f1-f9", reasoner.isConnectedWith(feature001, feature009));

        assertTrue("f2-f1", reasoner.isConnectedWith(feature002, feature001));
        assertTrue("f2-f2", reasoner.isConnectedWith(feature002, feature002));
        assertFalse("f2-f3", reasoner.isConnectedWith(feature002, feature003));
        assertFalse("f2-f4", reasoner.isConnectedWith(feature002, feature004));
        assertFalse("f2-f5", reasoner.isConnectedWith(feature002, feature005));
        assertFalse("f2-f6", reasoner.isConnectedWith(feature002, feature006));
        assertTrue("f2-f7", reasoner.isConnectedWith(feature002, feature007));
        assertFalse("f2-f8", reasoner.isConnectedWith(feature002, feature008));
        assertFalse("f2-f9", reasoner.isConnectedWith(feature002, feature009));

        assertFalse("f3-f1", reasoner.isConnectedWith(feature003, feature001));
        assertFalse("f3-f2", reasoner.isConnectedWith(feature003, feature002));
        assertTrue("f3-f3", reasoner.isConnectedWith(feature003, feature003));
        assertFalse("f3-f4", reasoner.isConnectedWith(feature003, feature004));
        assertFalse("f3-f5", reasoner.isConnectedWith(feature003, feature005));
        assertFalse("f3-f6", reasoner.isConnectedWith(feature003, feature006));
        assertTrue("f3-f7", reasoner.isConnectedWith(feature003, feature007));
        assertFalse("f3-f8", reasoner.isConnectedWith(feature003, feature008));
        assertFalse("f3-f9", reasoner.isConnectedWith(feature003, feature009));

        assertFalse("f4-f1", reasoner.isConnectedWith(feature004, feature001));
        assertFalse("f4-f2", reasoner.isConnectedWith(feature004, feature002));
        assertFalse("f4-f3", reasoner.isConnectedWith(feature004, feature003));
        assertTrue("f4-f4", reasoner.isConnectedWith(feature004, feature004));
        assertTrue("f4-f5", reasoner.isConnectedWith(feature004, feature005));
        assertFalse("f4-f6", reasoner.isConnectedWith(feature004, feature006));
        assertTrue("f4-f7", reasoner.isConnectedWith(feature004, feature007));
        assertFalse("f4-f8", reasoner.isConnectedWith(feature004, feature008));
        assertFalse("f4-f9", reasoner.isConnectedWith(feature004, feature009));

        assertFalse("f5-f1", reasoner.isConnectedWith(feature005, feature001));
        assertFalse("f5-f2", reasoner.isConnectedWith(feature005, feature002));
        assertFalse("f5-f3", reasoner.isConnectedWith(feature005, feature003));
        assertTrue("f5-f4", reasoner.isConnectedWith(feature005, feature004));
        assertTrue("f5-f5", reasoner.isConnectedWith(feature005, feature005));
        assertFalse("f5-f6", reasoner.isConnectedWith(feature005, feature006));
        assertTrue("f5-f7", reasoner.isConnectedWith(feature005, feature007));
        assertFalse("f5-f8", reasoner.isConnectedWith(feature005, feature008));
        assertFalse("f5-f9", reasoner.isConnectedWith(feature005, feature009));

        assertFalse("f6-f1", reasoner.isConnectedWith(feature006, feature001));
        assertFalse("f6-f2", reasoner.isConnectedWith(feature006, feature002));
        assertFalse("f6-f3", reasoner.isConnectedWith(feature006, feature003));
        assertFalse("f6-f4", reasoner.isConnectedWith(feature006, feature004));
        assertFalse("f6-f5", reasoner.isConnectedWith(feature006, feature005));
        assertFalse("f6-f6", reasoner.isConnectedWith(feature006, feature006));
        assertFalse("f6-f7", reasoner.isConnectedWith(feature006, feature007));
        assertFalse("f6-f8", reasoner.isConnectedWith(feature006, feature008));
        assertFalse("f6-f9", reasoner.isConnectedWith(feature006, feature009));

        assertTrue("f7-f1", reasoner.isConnectedWith(feature007, feature001));
        assertTrue("f7-f2", reasoner.isConnectedWith(feature007, feature002));
        assertTrue("f7-f3", reasoner.isConnectedWith(feature007, feature003));
        assertTrue("f7-f4", reasoner.isConnectedWith(feature007, feature004));
        assertTrue("f7-f5", reasoner.isConnectedWith(feature007, feature005));
        assertFalse("f7-f6", reasoner.isConnectedWith(feature007, feature006));
        assertTrue("f7-f7", reasoner.isConnectedWith(feature007, feature007));
        assertTrue("f7-f8", reasoner.isConnectedWith(feature007, feature008));
        assertFalse("f7-f9", reasoner.isConnectedWith(feature007, feature009));

        assertFalse("f8-f1", reasoner.isConnectedWith(feature008, feature001));
        assertFalse("f8-f2", reasoner.isConnectedWith(feature008, feature002));
        assertFalse("f8-f3", reasoner.isConnectedWith(feature008, feature003));
        assertFalse("f8-f4", reasoner.isConnectedWith(feature008, feature004));
        assertFalse("f8-f5", reasoner.isConnectedWith(feature008, feature005));
        assertFalse("f8-f6", reasoner.isConnectedWith(feature008, feature006));
        assertTrue("f8-f7", reasoner.isConnectedWith(feature008, feature007));
        assertTrue("f8-f8", reasoner.isConnectedWith(feature008, feature008));
        assertFalse("f8-f9", reasoner.isConnectedWith(feature008, feature009));

        assertFalse("f9-f1", reasoner.isConnectedWith(feature009, feature001));
        assertFalse("f9-f2", reasoner.isConnectedWith(feature009, feature002));
        assertFalse("f9-f3", reasoner.isConnectedWith(feature009, feature003));
        assertFalse("f9-f4", reasoner.isConnectedWith(feature009, feature004));
        assertFalse("f9-f5", reasoner.isConnectedWith(feature009, feature005));
        assertFalse("f9-f6", reasoner.isConnectedWith(feature009, feature006));
        assertFalse("f9-f7", reasoner.isConnectedWith(feature009, feature007));
        assertFalse("f9-f8", reasoner.isConnectedWith(feature009, feature007));
        assertTrue("f9-f9", reasoner.isConnectedWith(feature009, feature009));
    }

    @Test
    public void testGetIsConnectedWithMembers() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7999 51.0610)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7999 51.0610)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7888 51.0606)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7888 51.0610,13.7900 51.0600,13.7940 51.0610,13.7954 51.0602)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8019 51.0579,13.7923 51.0555,13.7934 51.0590,13.7954 51.0602)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8041 51.0642,13.8030 51.0632,13.8041 51.0627,13.8052 51.0636)");

        // polygons
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.7888 51.0606,13.7958 51.0622,13.8013 51.0611,13.7976 51.0594,13.7888 51.0606))");

        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7962 51.0589,13.7971 51.0600,13.7995 51.0586,13.7962 51.0589))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.7774 51.0660,13.7774 51.0646,13.7812 51.0648,13.7808 51.0663,13.7774 51.0660))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);

        Map<OWLIndividual, SortedSet<OWLIndividual>> result =
                reasoner.getIsConnectedWithMembers();

        assertTrue("f1-f1", result.get(feature001).contains(feature001));
        assertTrue("f1-f2", result.get(feature001).contains(feature002));
        assertFalse("f1-f3", result.get(feature001).contains(feature003));
        assertFalse("f1-f4", result.get(feature001).contains(feature004));
        assertFalse("f1-f5", result.get(feature001).contains(feature005));
        assertFalse("f1-f6", result.get(feature001).contains(feature006));
        assertTrue("f1-f7", result.get(feature001).contains(feature007));
        assertFalse("f1-f8", result.get(feature001).contains(feature008));
        assertFalse("f1-f9", result.get(feature001).contains(feature009));

        assertTrue("f2-f1", result.get(feature002).contains(feature001));
        assertTrue("f2-f2", result.get(feature002).contains(feature002));
        assertFalse("f2-f3", result.get(feature002).contains(feature003));
        assertFalse("f2-f4", result.get(feature002).contains(feature004));
        assertFalse("f2-f5", result.get(feature002).contains(feature005));
        assertFalse("f2-f6", result.get(feature002).contains(feature006));
        assertTrue("f2-f7", result.get(feature002).contains(feature007));
        assertFalse("f2-f8", result.get(feature002).contains(feature008));
        assertFalse("f2-f9", result.get(feature002).contains(feature009));

        assertFalse("f3-f1", result.get(feature003).contains(feature001));
        assertFalse("f3-f2", result.get(feature003).contains(feature002));
        assertTrue("f3-f3", result.get(feature003).contains(feature003));
        assertFalse("f3-f4", result.get(feature003).contains(feature004));
        assertFalse("f3-f5", result.get(feature003).contains(feature005));
        assertFalse("f3-f6", result.get(feature003).contains(feature006));
        assertTrue("f3-f7", result.get(feature003).contains(feature007));
        assertFalse("f3-f8", result.get(feature003).contains(feature008));
        assertFalse("f3-f9", result.get(feature003).contains(feature009));

        assertFalse("f4-f1", result.get(feature004).contains(feature001));
        assertFalse("f4-f2", result.get(feature004).contains(feature002));
        assertFalse("f4-f3", result.get(feature004).contains(feature003));
        assertTrue("f4-f4", result.get(feature004).contains(feature004));
        assertTrue("f4-f5", result.get(feature004).contains(feature005));
        assertFalse("f4-f6", result.get(feature004).contains(feature006));
        assertTrue("f4-f7", result.get(feature004).contains(feature007));
        assertFalse("f4-f8", result.get(feature004).contains(feature008));
        assertFalse("f4-f9", result.get(feature004).contains(feature009));

        assertFalse("f5-f1", result.get(feature005).contains(feature001));
        assertFalse("f5-f2", result.get(feature005).contains(feature002));
        assertFalse("f5-f3", result.get(feature005).contains(feature003));
        assertTrue("f5-f4", result.get(feature005).contains(feature004));
        assertTrue("f5-f5", result.get(feature005).contains(feature005));
        assertFalse("f5-f6", result.get(feature005).contains(feature006));
        assertTrue("f5-f7", result.get(feature005).contains(feature007));
        assertFalse("f5-f8", result.get(feature005).contains(feature008));
        assertFalse("f5-f9", result.get(feature005).contains(feature009));

        assertFalse("f6-f1", result.get(feature006).contains(feature001));
        assertFalse("f6-f2", result.get(feature006).contains(feature002));
        assertFalse("f6-f3", result.get(feature006).contains(feature003));
        assertFalse("f6-f4", result.get(feature006).contains(feature004));
        assertFalse("f6-f5", result.get(feature006).contains(feature005));
        assertTrue("f6-f6", result.get(feature006).contains(feature006));
        assertFalse("f6-f7", result.get(feature006).contains(feature007));
        assertFalse("f6-f8", result.get(feature006).contains(feature008));
        assertFalse("f6-f9", result.get(feature006).contains(feature009));

        assertTrue("f7-f1", result.get(feature007).contains(feature001));
        assertTrue("f7-f2", result.get(feature007).contains(feature002));
        assertTrue("f7-f3", result.get(feature007).contains(feature003));
        assertTrue("f7-f4", result.get(feature007).contains(feature004));
        assertTrue("f7-f5", result.get(feature007).contains(feature005));
        assertFalse("f7-f6", result.get(feature007).contains(feature006));
        assertTrue("f7-f7", result.get(feature007).contains(feature007));
        assertTrue("f7-f8", result.get(feature007).contains(feature008));
        assertFalse("f7-f9", result.get(feature007).contains(feature009));

        assertFalse("f8-f1", result.get(feature008).contains(feature001));
        assertFalse("f8-f2", result.get(feature008).contains(feature002));
        assertFalse("f8-f3", result.get(feature008).contains(feature003));
        assertFalse("f8-f4", result.get(feature008).contains(feature004));
        assertFalse("f8-f5", result.get(feature008).contains(feature005));
        assertFalse("f8-f6", result.get(feature008).contains(feature006));
        assertTrue("f8-f7", result.get(feature008).contains(feature007));
        assertTrue("f8-f8", result.get(feature008).contains(feature008));
        assertFalse("f8-f9", result.get(feature008).contains(feature009));

        assertFalse("f9-f1", result.get(feature009).contains(feature001));
        assertFalse("f9-f2", result.get(feature009).contains(feature002));
        assertFalse("f9-f3", result.get(feature009).contains(feature003));
        assertFalse("f9-f4", result.get(feature009).contains(feature004));
        assertFalse("f9-f5", result.get(feature009).contains(feature005));
        assertFalse("f9-f6", result.get(feature009).contains(feature006));
        assertFalse("f9-f7", result.get(feature009).contains(feature007));
        assertFalse("f9-f8", result.get(feature009).contains(feature007));
        assertTrue("f9-f9", result.get(feature009).contains(feature009));
    }

    @Test
    public void testGetIndividualsOverlappingWith() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7999 51.0610)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7999 51.0610)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7990 51.0599)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7999 51.0610,13.7990 51.0599,13.8000 51.0594,13.8015 51.05998)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8000 51.0594,13.8015 51.05998)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8000 51.0594,13.8015 51.05998,13.8027 51.0594,13.8016 51.0585)");

        // polygons
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.8027 51.0594,13.8015 51.05998,13.8022 51.0607,13.8039 51.0600,13.8027 51.0594))");

        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7990 51.0599,13.7981 51.0588,13.7962 51.0595,13.7990 51.0599))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
            "POLYGON((13.7968 51.0604,13.7966 51.0589,13.7946 51.0595,13.7954 51.0605,13.7968 51.0604))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsOverlappingWith(feature001)
                        .collect(Collectors.toSet());

        assertTrue("f1-f1", result.contains(feature001));
        assertTrue("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertTrue("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsOverlappingWith(feature002)
                        .collect(Collectors.toSet());

        assertTrue("f2-f1", result.contains(feature001));
        assertTrue("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertTrue("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsOverlappingWith(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertTrue("f3-f3", result.contains(feature003));
        assertTrue("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertTrue("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsOverlappingWith(feature004)
                        .collect(Collectors.toSet());

        assertTrue("f4-f1", result.contains(feature001));
        assertTrue("f4-f2", result.contains(feature002));
        assertTrue("f4-f3", result.contains(feature003));
        assertTrue("f4-f4", result.contains(feature004));
        assertTrue("f4-f5", result.contains(feature005));
        assertTrue("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));  // this is maybe debatable
        assertFalse("f4-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsOverlappingWith(feature005)
                        .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertTrue("f5-f4", result.contains(feature004));
        assertTrue("f5-f5", result.contains(feature005));
        assertTrue("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsOverlappingWith(feature006)
                        .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertTrue("f6-f4", result.contains(feature004));
        assertTrue("f6-f5", result.contains(feature005));
        assertTrue("f6-f6", result.contains(feature006));
        assertTrue("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsOverlappingWith(feature007)
                        .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertTrue("f7-f6", result.contains(feature006));
        assertTrue("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsOverlappingWith(feature008)
                        .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertTrue("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertTrue("f8-f8", result.contains(feature008));
        assertTrue("f8-f9", result.contains(feature009));


        result =
                reasoner.getIndividualsOverlappingWith(feature009)
                        .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertTrue("f9-f8", result.contains(feature008));
        assertTrue("f9-f9", result.contains(feature009));
    }

    @Test
    public void testOverlapsWith() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7999 51.0610)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7999 51.0610)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7990 51.0599)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7999 51.0610,13.7990 51.0599,13.8000 51.0594,13.8015 51.05998)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8000 51.0594,13.8015 51.05998)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8000 51.0594,13.8015 51.05998,13.8027 51.0594,13.8016 51.0585)");

        // polygons
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.8027 51.0594,13.8015 51.05998,13.8022 51.0607,13.8039 51.0600,13.8027 51.0594))");

        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7990 51.0599,13.7981 51.0588,13.7962 51.0595,13.7990 51.0599))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.7968 51.0604,13.7966 51.0589,13.7946 51.0595,13.7954 51.0605,13.7968 51.0604))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertTrue("f1-f1", reasoner.overlapsWith(feature001, feature001));
        assertTrue("f1-f2", reasoner.overlapsWith(feature001, feature002));
        assertFalse("f1-f3", reasoner.overlapsWith(feature001, feature003));
        assertTrue("f1-f4", reasoner.overlapsWith(feature001, feature004));
        assertFalse("f1-f5", reasoner.overlapsWith(feature001, feature005));
        assertFalse("f1-f6", reasoner.overlapsWith(feature001, feature006));
        assertFalse("f1-f7", reasoner.overlapsWith(feature001, feature007));
        assertFalse("f1-f8", reasoner.overlapsWith(feature001, feature008));
        assertFalse("f1-f9", reasoner.overlapsWith(feature001, feature009));

        assertTrue("f2-f1", reasoner.overlapsWith(feature002, feature001));
        assertTrue("f2-f2", reasoner.overlapsWith(feature002, feature002));
        assertFalse("f2-f3", reasoner.overlapsWith(feature002, feature003));
        assertTrue("f2-f4", reasoner.overlapsWith(feature002, feature004));
        assertFalse("f2-f5", reasoner.overlapsWith(feature002, feature005));
        assertFalse("f2-f6", reasoner.overlapsWith(feature002, feature006));
        assertFalse("f2-f7", reasoner.overlapsWith(feature002, feature007));
        assertFalse("f2-f8", reasoner.overlapsWith(feature002, feature008));
        assertFalse("f2-f9", reasoner.overlapsWith(feature002, feature009));

        assertFalse("f3-f1", reasoner.overlapsWith(feature003, feature001));
        assertFalse("f3-f2", reasoner.overlapsWith(feature003, feature002));
        assertTrue("f3-f3", reasoner.overlapsWith(feature003, feature003));
        assertTrue("f3-f4", reasoner.overlapsWith(feature003, feature004));
        assertFalse("f3-f5", reasoner.overlapsWith(feature003, feature005));
        assertFalse("f3-f6", reasoner.overlapsWith(feature003, feature006));
        assertFalse("f3-f7", reasoner.overlapsWith(feature003, feature007));
        assertTrue("f3-f8", reasoner.overlapsWith(feature003, feature008));
        assertFalse("f3-f9", reasoner.overlapsWith(feature003, feature009));

        assertTrue("f4-f1", reasoner.overlapsWith(feature004, feature001));
        assertTrue("f4-f2", reasoner.overlapsWith(feature004, feature002));
        assertTrue("f4-f3", reasoner.overlapsWith(feature004, feature003));
        assertTrue("f4-f4", reasoner.overlapsWith(feature004, feature004));
        assertTrue("f4-f5", reasoner.overlapsWith(feature004, feature005));
        assertTrue("f4-f6", reasoner.overlapsWith(feature004, feature006));
        assertFalse("f4-f7", reasoner.overlapsWith(feature004, feature007));
        assertFalse("f4-f8", reasoner.overlapsWith(feature004, feature008));  // this is maybe debatable
        assertFalse("f4-f9", reasoner.overlapsWith(feature004, feature009));

        assertFalse("f5-f1", reasoner.overlapsWith(feature005, feature001));
        assertFalse("f5-f2", reasoner.overlapsWith(feature005, feature002));
        assertFalse("f5-f3", reasoner.overlapsWith(feature005, feature003));
        assertTrue("f5-f4", reasoner.overlapsWith(feature005, feature004));
        assertTrue("f5-f5", reasoner.overlapsWith(feature005, feature005));
        assertTrue("f5-f6", reasoner.overlapsWith(feature005, feature006));
        assertFalse("f5-f7", reasoner.overlapsWith(feature005, feature007));
        assertFalse("f5-f8", reasoner.overlapsWith(feature005, feature008));
        assertFalse("f5-f9", reasoner.overlapsWith(feature005, feature009));

        assertFalse("f6-f1", reasoner.overlapsWith(feature006, feature001));
        assertFalse("f6-f2", reasoner.overlapsWith(feature006, feature002));
        assertFalse("f6-f3", reasoner.overlapsWith(feature006, feature003));
        assertTrue("f6-f4",  reasoner.overlapsWith(feature006, feature004));
        assertTrue("f6-f5",  reasoner.overlapsWith(feature006, feature005));
        assertTrue("f6-f6",  reasoner.overlapsWith(feature006, feature006));
        assertTrue("f6-f7",  reasoner.overlapsWith(feature006, feature007));
        assertFalse("f6-f8", reasoner.overlapsWith(feature006, feature008));
        assertFalse("f6-f9", reasoner.overlapsWith(feature006, feature009));

        assertFalse("f7-f1", reasoner.overlapsWith(feature007, feature001));
        assertFalse("f7-f2", reasoner.overlapsWith(feature007, feature002));
        assertFalse("f7-f3", reasoner.overlapsWith(feature007, feature003));
        assertFalse("f7-f4", reasoner.overlapsWith(feature007, feature004));
        assertFalse("f7-f5", reasoner.overlapsWith(feature007, feature005));
        assertTrue("f7-f6", reasoner.overlapsWith(feature007, feature006));
        assertTrue("f7-f7", reasoner.overlapsWith(feature007, feature007));
        assertFalse("f7-f8", reasoner.overlapsWith(feature007, feature008));
        assertFalse("f7-f9", reasoner.overlapsWith(feature007, feature009));

        assertFalse("f8-f1", reasoner.overlapsWith(feature008, feature001));
        assertFalse("f8-f2", reasoner.overlapsWith(feature008, feature002));
        assertTrue("f8-f3", reasoner.overlapsWith(feature008, feature003));
        assertFalse("f8-f4", reasoner.overlapsWith(feature008, feature004));
        assertFalse("f8-f5", reasoner.overlapsWith(feature008, feature005));
        assertFalse("f8-f6", reasoner.overlapsWith(feature008, feature006));
        assertFalse("f8-f7", reasoner.overlapsWith(feature008, feature007));
        assertTrue("f8-f8", reasoner.overlapsWith(feature008, feature008));
        assertTrue("f8-f9", reasoner.overlapsWith(feature008, feature009));

        assertFalse("f9-f1", reasoner.overlapsWith(feature009, feature001));
        assertFalse("f9-f2", reasoner.overlapsWith(feature009, feature002));
        assertFalse("f9-f3", reasoner.overlapsWith(feature009, feature003));
        assertFalse("f9-f4", reasoner.overlapsWith(feature009, feature004));
        assertFalse("f9-f5", reasoner.overlapsWith(feature009, feature005));
        assertFalse("f9-f6", reasoner.overlapsWith(feature009, feature006));
        assertFalse("f9-f7", reasoner.overlapsWith(feature009, feature007));
        assertTrue("f9-f8", reasoner.overlapsWith(feature009, feature008));
        assertTrue("f9-f9", reasoner.overlapsWith(feature009, feature009));
    }

    @Test
    public void testgetOverlapsWithMembers() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7999 51.0610)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7999 51.0610)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7990 51.0599)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7999 51.0610,13.7990 51.0599,13.8000 51.0594,13.8015 51.05998)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8000 51.0594,13.8015 51.05998)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8000 51.0594,13.8015 51.05998,13.8027 51.0594,13.8016 51.0585)");

        // polygons
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.8027 51.0594,13.8015 51.05998,13.8022 51.0607,13.8039 51.0600,13.8027 51.0594))");

        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7990 51.0599,13.7981 51.0588,13.7962 51.0595,13.7990 51.0599))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.7968 51.0604,13.7966 51.0589,13.7946 51.0595,13.7954 51.0605,13.7968 51.0604))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Map<OWLIndividual, SortedSet<OWLIndividual>> result =
                reasoner.getOverlapsWithMembers();

        assertTrue("f1-f1", result.get(feature001).contains(feature001));
        assertTrue("f1-f2", result.get(feature001).contains(feature002));
        assertFalse("f1-f3", result.get(feature001).contains(feature003));
        assertTrue("f1-f4", result.get(feature001).contains(feature004));
        assertFalse("f1-f5", result.get(feature001).contains(feature005));
        assertFalse("f1-f6", result.get(feature001).contains(feature006));
        assertFalse("f1-f7", result.get(feature001).contains(feature007));
        assertFalse("f1-f8", result.get(feature001).contains(feature008));
        assertFalse("f1-f9", result.get(feature001).contains(feature009));

        assertTrue("f2-f1", result.get(feature002).contains(feature001));
        assertTrue("f2-f2", result.get(feature002).contains(feature002));
        assertFalse("f2-f3", result.get(feature002).contains(feature003));
        assertTrue("f2-f4", result.get(feature002).contains(feature004));
        assertFalse("f2-f5", result.get(feature002).contains(feature005));
        assertFalse("f2-f6", result.get(feature002).contains(feature006));
        assertFalse("f2-f7", result.get(feature002).contains(feature007));
        assertFalse("f2-f8", result.get(feature002).contains(feature008));
        assertFalse("f2-f9", result.get(feature002).contains(feature009));

        assertFalse("f3-f1", result.get(feature003).contains(feature001));
        assertFalse("f3-f2", result.get(feature003).contains(feature002));
        assertTrue("f3-f3", result.get(feature003).contains(feature003));
        assertTrue("f3-f4", result.get(feature003).contains(feature004));
        assertFalse("f3-f5", result.get(feature003).contains(feature005));
        assertFalse("f3-f6", result.get(feature003).contains(feature006));
        assertFalse("f3-f7", result.get(feature003).contains(feature007));
        assertTrue("f3-f8", result.get(feature003).contains(feature008));
        assertFalse("f3-f9", result.get(feature003).contains(feature009));

        assertTrue("f4-f1", result.get(feature004).contains(feature001));
        assertTrue("f4-f2", result.get(feature004).contains(feature002));
        assertTrue("f4-f3", result.get(feature004).contains(feature003));
        assertTrue("f4-f4", result.get(feature004).contains(feature004));
        assertTrue("f4-f5", result.get(feature004).contains(feature005));
        assertTrue("f4-f6", result.get(feature004).contains(feature006));
        assertFalse("f4-f7", result.get(feature004).contains(feature007));
        assertFalse("f4-f8", result.get(feature004).contains(feature008));  // this is maybe debatable
        assertFalse("f4-f9", result.get(feature004).contains(feature009));

        assertFalse("f5-f1", result.get(feature005).contains(feature001));
        assertFalse("f5-f2", result.get(feature005).contains(feature002));
        assertFalse("f5-f3", result.get(feature005).contains(feature003));
        assertTrue("f5-f4", result.get(feature005).contains(feature004));
        assertTrue("f5-f5", result.get(feature005).contains(feature005));
        assertTrue("f5-f6", result.get(feature005).contains(feature006));
        assertFalse("f5-f7", result.get(feature005).contains(feature007));
        assertFalse("f5-f8", result.get(feature005).contains(feature008));
        assertFalse("f5-f9", result.get(feature005).contains(feature009));

        assertFalse("f6-f1", result.get(feature006).contains(feature001));
        assertFalse("f6-f2", result.get(feature006).contains(feature002));
        assertFalse("f6-f3", result.get(feature006).contains(feature003));
        assertTrue("f6-f4", result.get(feature006).contains(feature004));
        assertTrue("f6-f5", result.get(feature006).contains(feature005));
        assertTrue("f6-f6", result.get(feature006).contains(feature006));
        assertTrue("f6-f7", result.get(feature006).contains(feature007));
        assertFalse("f6-f8", result.get(feature006).contains(feature008));
        assertFalse("f6-f9", result.get(feature006).contains(feature009));

        assertFalse("f7-f1", result.get(feature007).contains(feature001));
        assertFalse("f7-f2", result.get(feature007).contains(feature002));
        assertFalse("f7-f3", result.get(feature007).contains(feature003));
        assertFalse("f7-f4", result.get(feature007).contains(feature004));
        assertFalse("f7-f5", result.get(feature007).contains(feature005));
        assertTrue("f7-f6", result.get(feature007).contains(feature006));
        assertTrue("f7-f7", result.get(feature007).contains(feature007));
        assertFalse("f7-f8", result.get(feature007).contains(feature008));
        assertFalse("f7-f9", result.get(feature007).contains(feature009));

        assertFalse("f8-f1", result.get(feature008).contains(feature001));
        assertFalse("f8-f2", result.get(feature008).contains(feature002));
        assertTrue("f8-f3", result.get(feature008).contains(feature003));
        assertFalse("f8-f4", result.get(feature008).contains(feature004));
        assertFalse("f8-f5", result.get(feature008).contains(feature005));
        assertFalse("f8-f6", result.get(feature008).contains(feature006));
        assertFalse("f8-f7", result.get(feature008).contains(feature007));
        assertTrue("f8-f8", result.get(feature008).contains(feature008));
        assertTrue("f8-f9", result.get(feature008).contains(feature009));

        assertFalse("f9-f1", result.get(feature009).contains(feature001));
        assertFalse("f9-f2", result.get(feature009).contains(feature002));
        assertFalse("f9-f3", result.get(feature009).contains(feature003));
        assertFalse("f9-f4", result.get(feature009).contains(feature004));
        assertFalse("f9-f5", result.get(feature009).contains(feature005));
        assertFalse("f9-f6", result.get(feature009).contains(feature006));
        assertFalse("f9-f7", result.get(feature009).contains(feature007));
        assertTrue("f9-f8", result.get(feature009).contains(feature008));
        assertTrue("f9-f9", result.get(feature009).contains(feature009));
    }

    @Test
    public void testGetIndividualsPartOf() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7987 51.0600)");

        OWLIndividual feature002 = i("feature001");
        OWLIndividual geom002 = i("geom001");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7987 51.0600)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7937 51.0602)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8008 51.0576,13.8013 51.0586,13.8018 51.0575,13.8018 51.0585,13.8026 51.0575,13.8024 51.0585)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7976 51.0597,13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597,13.8006 51.0589))");

        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.7929 51.0617,13.7940 51.0618,13.7940 51.0609,13.7927 51.0610,13.7929 51.0617))");

        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7927 51.0606,13.7918 51.0610,13.7920 51.0619,13.7936 51.0623,13.7957 51.0616,13.7950 51.0607,13.7927 51.0606))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsPartOf(feature001)
                        .collect(Collectors.toSet());

        assertTrue("f1-f1", result.contains(feature001));
        assertTrue("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));
        assertFalse("f1-f10", result.contains(feature010));
        assertFalse("f1-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsPartOf(feature002)
                        .collect(Collectors.toSet());

        assertTrue("f2-f1", result.contains(feature001));
        assertTrue("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));
        assertFalse("f2-f10", result.contains(feature010));
        assertFalse("f2-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsPartOf(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertTrue("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertFalse("f3-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsPartOf(feature004)
                        .collect(Collectors.toSet());

        assertTrue("f4-f1", result.contains(feature001));
        assertTrue("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertTrue("f4-f4", result.contains(feature004));
        assertTrue("f4-f5", result.contains(feature005));
        assertTrue("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsPartOf(feature005)
                        .collect(Collectors.toSet());

        assertTrue("f5-f1", result.contains(feature001));
        assertTrue("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertTrue("f5-f4", result.contains(feature004));
        assertTrue("f5-f5", result.contains(feature005));
        assertTrue("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertFalse("f5-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsPartOf(feature006)
                        .collect(Collectors.toSet());

        assertTrue("f6-f1", result.contains(feature001));
        assertTrue("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertTrue("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertFalse("f6-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsPartOf(feature007)
                        .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertTrue("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertFalse("f7-f10", result.contains(feature010));
        assertFalse("f7-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsPartOf(feature008)
                        .collect(Collectors.toSet());

        assertTrue("f8-f1", result.contains(feature001));
        assertTrue("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertTrue("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertTrue("f8-f8", result.contains(feature008));
        assertTrue("f8-f9", result.contains(feature009));
        assertFalse("f8-f10", result.contains(feature010));
        assertFalse("f8-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsPartOf(feature009)
                        .collect(Collectors.toSet());

        assertTrue("f9-f1", result.contains(feature001));
        assertTrue("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertTrue("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertTrue("f9-f8", result.contains(feature008));
        assertTrue("f9-f9", result.contains(feature009));
        assertFalse("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsPartOf(feature010)
                        .collect(Collectors.toSet());

        assertFalse("f10-f1", result.contains(feature001));
        assertFalse("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertFalse("f10-f6", result.contains(feature006));
        assertFalse("f10-f7", result.contains(feature007));
        assertFalse("f10-f8", result.contains(feature008));
        assertFalse("f10-f9", result.contains(feature009));
        assertTrue("f10-f10", result.contains(feature010));
        assertFalse("f10-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsPartOf(feature011)
                        .collect(Collectors.toSet());

        assertFalse("f11-f1", result.contains(feature001));
        assertFalse("f11-f2", result.contains(feature002));
        assertFalse("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertFalse("f11-f5", result.contains(feature005));
        assertFalse("f11-f6", result.contains(feature006));
        assertFalse("f11-f7", result.contains(feature007));
        assertFalse("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertTrue("f11-f10", result.contains(feature010));
        assertTrue("f11-f11", result.contains(feature011));
    }

    @Test
    public void testIsPartOf() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7987 51.0600)");

        OWLIndividual feature002 = i("feature001");
        OWLIndividual geom002 = i("geom001");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7987 51.0600)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7937 51.0602)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8008 51.0576,13.8013 51.0586,13.8018 51.0575,13.8018 51.0585,13.8026 51.0575,13.8024 51.0585)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7976 51.0597,13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597,13.8006 51.0589))");

        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.7929 51.0617,13.7940 51.0618,13.7940 51.0609,13.7927 51.0610,13.7929 51.0617))");

        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7927 51.0606,13.7918 51.0610,13.7920 51.0619,13.7936 51.0623,13.7957 51.0616,13.7950 51.0607,13.7927 51.0606))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertTrue("f1-f1", reasoner.isPartOf(feature001, feature001));
        assertTrue("f1-f2", reasoner.isPartOf(feature001, feature002));
        assertFalse("f1-f3", reasoner.isPartOf(feature001, feature003));
        assertTrue("f1-f4", reasoner.isPartOf(feature001, feature004));
        assertTrue("f1-f5", reasoner.isPartOf(feature001, feature005));
        assertTrue("f1-f6", reasoner.isPartOf(feature001, feature006));
        assertFalse("f1-f7", reasoner.isPartOf(feature001, feature007));
        assertTrue("f1-f8", reasoner.isPartOf(feature001, feature008));
        assertTrue("f1-f9", reasoner.isPartOf(feature001, feature009));
        assertFalse("f1-f10", reasoner.isPartOf(feature001, feature010));
        assertFalse("f1-f11", reasoner.isPartOf(feature001, feature011));

        assertTrue("f2-f1", reasoner.isPartOf(feature002, feature001));
        assertTrue("f2-f2", reasoner.isPartOf(feature002, feature002));
        assertFalse("f2-f3", reasoner.isPartOf(feature002, feature003));
        assertTrue("f2-f4", reasoner.isPartOf(feature002, feature004));
        assertTrue("f2-f5", reasoner.isPartOf(feature002, feature005));
        assertTrue("f2-f6", reasoner.isPartOf(feature002, feature006));
        assertFalse("f2-f7", reasoner.isPartOf(feature002, feature007));
        assertTrue("f2-f8", reasoner.isPartOf(feature002, feature008));
        assertTrue("f2-f9", reasoner.isPartOf(feature002, feature009));
        assertFalse("f2-f10", reasoner.isPartOf(feature002, feature010));
        assertFalse("f2-f11", reasoner.isPartOf(feature002, feature011));

        assertFalse("f3-f1", reasoner.isPartOf(feature003, feature001));
        assertFalse("f3-f2", reasoner.isPartOf(feature003, feature002));
        assertTrue("f3-f3", reasoner.isPartOf(feature003, feature003));
        assertFalse("f3-f4", reasoner.isPartOf(feature003, feature004));
        assertFalse("f3-f5", reasoner.isPartOf(feature003, feature005));
        assertFalse("f3-f6", reasoner.isPartOf(feature003, feature006));
        assertFalse("f3-f7", reasoner.isPartOf(feature003, feature007));
        assertFalse("f3-f8", reasoner.isPartOf(feature003, feature008));
        assertFalse("f3-f9", reasoner.isPartOf(feature003, feature009));
        assertFalse("f3-f10", reasoner.isPartOf(feature003, feature010));
        assertFalse("f3-f11", reasoner.isPartOf(feature003, feature011));

        assertFalse("f4-f1", reasoner.isPartOf(feature004, feature001));
        assertFalse("f4-f2", reasoner.isPartOf(feature004, feature002));
        assertFalse("f4-f3", reasoner.isPartOf(feature004, feature003));
        assertTrue("f4-f4", reasoner.isPartOf(feature004, feature004));
        assertTrue("f4-f5", reasoner.isPartOf(feature004, feature005));
        assertFalse("f4-f6", reasoner.isPartOf(feature004, feature006));
        assertFalse("f4-f7", reasoner.isPartOf(feature004, feature007));
        assertFalse("f4-f8", reasoner.isPartOf(feature004, feature008));
        assertFalse("f4-f9", reasoner.isPartOf(feature004, feature009));
        assertFalse("f4-f10", reasoner.isPartOf(feature004, feature010));
        assertFalse("f4-f11", reasoner.isPartOf(feature004, feature011));

        assertFalse("f5-f1", reasoner.isPartOf(feature005, feature001));
        assertFalse("f5-f2", reasoner.isPartOf(feature005, feature002));
        assertFalse("f5-f3", reasoner.isPartOf(feature005, feature003));
        assertTrue("f5-f4", reasoner.isPartOf(feature005, feature004));
        assertTrue("f5-f5", reasoner.isPartOf(feature005, feature005));
        assertFalse("f5-f6", reasoner.isPartOf(feature005, feature006));
        assertFalse("f5-f7", reasoner.isPartOf(feature005, feature007));
        assertFalse("f5-f8", reasoner.isPartOf(feature005, feature008));
        assertFalse("f5-f9", reasoner.isPartOf(feature005, feature009));
        assertFalse("f5-f10", reasoner.isPartOf(feature005, feature010));
        assertFalse("f5-f11", reasoner.isPartOf(feature005, feature011));

        assertFalse("f6-f1", reasoner.isPartOf(feature006, feature001));
        assertFalse("f6-f2", reasoner.isPartOf(feature006, feature002));
        assertFalse("f6-f3", reasoner.isPartOf(feature006, feature003));
        assertTrue("f6-f4", reasoner.isPartOf(feature006, feature004));
        assertTrue("f6-f5", reasoner.isPartOf(feature006, feature005));
        assertTrue("f6-f6", reasoner.isPartOf(feature006, feature006));
        assertFalse("f6-f7", reasoner.isPartOf(feature006, feature007));
        assertTrue("f6-f8", reasoner.isPartOf(feature006, feature008));
        assertTrue("f6-f9", reasoner.isPartOf(feature006, feature009));
        assertFalse("f6-f10", reasoner.isPartOf(feature006, feature010));
        assertFalse("f6-f11", reasoner.isPartOf(feature006, feature011));

        assertFalse("f7-f1", reasoner.isPartOf(feature007, feature001));
        assertFalse("f7-f2", reasoner.isPartOf(feature007, feature002));
        assertFalse("f7-f3", reasoner.isPartOf(feature007, feature003));
        assertFalse("f7-f4", reasoner.isPartOf(feature007, feature004));
        assertFalse("f7-f5", reasoner.isPartOf(feature007, feature005));
        assertFalse("f7-f6", reasoner.isPartOf(feature007, feature006));
        assertTrue("f7-f7", reasoner.isPartOf(feature007, feature007));
        assertFalse("f7-f8", reasoner.isPartOf(feature007, feature008));
        assertFalse("f7-f9", reasoner.isPartOf(feature007, feature009));
        assertFalse("f7-f10", reasoner.isPartOf(feature007, feature010));
        assertFalse("f7-f11", reasoner.isPartOf(feature007, feature011));

        assertFalse("f8-f1", reasoner.isPartOf(feature008, feature001));
        assertFalse("f8-f2", reasoner.isPartOf(feature008, feature002));
        assertFalse("f8-f3", reasoner.isPartOf(feature008, feature003));
        assertFalse("f8-f4", reasoner.isPartOf(feature008, feature004));
        assertFalse("f8-f5", reasoner.isPartOf(feature008, feature005));
        assertFalse("f8-f6", reasoner.isPartOf(feature008, feature006));
        assertFalse("f8-f7", reasoner.isPartOf(feature008, feature007));
        assertTrue("f8-f8", reasoner.isPartOf(feature008, feature008));
        assertTrue("f8-f9", reasoner.isPartOf(feature008, feature009));
        assertFalse("f8-f10", reasoner.isPartOf(feature008, feature010));
        assertFalse("f8-f11", reasoner.isPartOf(feature008, feature011));

        assertFalse("f9-f1", reasoner.isPartOf(feature009, feature001));
        assertFalse("f9-f2", reasoner.isPartOf(feature009, feature002));
        assertFalse("f9-f3", reasoner.isPartOf(feature009, feature003));
        assertFalse("f9-f4", reasoner.isPartOf(feature009, feature004));
        assertFalse("f9-f5", reasoner.isPartOf(feature009, feature005));
        assertFalse("f9-f6", reasoner.isPartOf(feature009, feature006));
        assertFalse("f9-f7", reasoner.isPartOf(feature009, feature007));
        assertTrue("f9-f8", reasoner.isPartOf(feature009, feature008));
        assertTrue("f9-f9", reasoner.isPartOf(feature009, feature009));
        assertFalse("f9-f10", reasoner.isPartOf(feature009, feature010));
        assertFalse("f9-f11", reasoner.isPartOf(feature009, feature011));

        assertFalse("f10-f1", reasoner.isPartOf(feature010, feature001));
        assertFalse("f10-f2", reasoner.isPartOf(feature010, feature002));
        assertFalse("f10-f3", reasoner.isPartOf(feature010, feature003));
        assertFalse("f10-f4", reasoner.isPartOf(feature010, feature004));
        assertFalse("f10-f5", reasoner.isPartOf(feature010, feature005));
        assertFalse("f10-f6", reasoner.isPartOf(feature010, feature006));
        assertFalse("f10-f7", reasoner.isPartOf(feature010, feature007));
        assertFalse("f10-f8", reasoner.isPartOf(feature010, feature008));
        assertFalse("f10-f9", reasoner.isPartOf(feature010, feature009));
        assertTrue("f10-f10", reasoner.isPartOf(feature010, feature010));
        assertTrue("f10-f11", reasoner.isPartOf(feature010, feature011));

        assertFalse("f11-f1", reasoner.isPartOf(feature011, feature001));
        assertFalse("f11-f2", reasoner.isPartOf(feature011, feature002));
        assertFalse("f11-f3", reasoner.isPartOf(feature011, feature003));
        assertFalse("f11-f4", reasoner.isPartOf(feature011, feature004));
        assertFalse("f11-f5", reasoner.isPartOf(feature011, feature005));
        assertFalse("f11-f6", reasoner.isPartOf(feature011, feature006));
        assertFalse("f11-f7", reasoner.isPartOf(feature011, feature007));
        assertFalse("f11-f8", reasoner.isPartOf(feature011, feature008));
        assertFalse("f11-f9", reasoner.isPartOf(feature011, feature009));
        assertFalse("f11-f10", reasoner.isPartOf(feature011, feature010));
        assertTrue("f11-f10", reasoner.isPartOf(feature011, feature011));
    }

    @Test
    public void testGetIsPartOfMembers() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7987 51.0600)");

        OWLIndividual feature002 = i("feature001");
        OWLIndividual geom002 = i("geom001");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7987 51.0600)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7937 51.0602)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8008 51.0576,13.8013 51.0586,13.8018 51.0575,13.8018 51.0585,13.8026 51.0575,13.8024 51.0585)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7976 51.0597,13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597,13.8006 51.0589))");

        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.7929 51.0617,13.7940 51.0618,13.7940 51.0609,13.7927 51.0610,13.7929 51.0617))");

        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7927 51.0606,13.7918 51.0610,13.7920 51.0619,13.7936 51.0623,13.7957 51.0616,13.7950 51.0607,13.7927 51.0606))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Map<OWLIndividual, SortedSet<OWLIndividual>> members =
                reasoner.getIsPartOfMembers();

        assertTrue("f1-f1", members.get(feature001).contains(feature001));
        assertTrue("f1-f2", members.get(feature001).contains(feature002));
        assertFalse("f1-f3", members.get(feature001).contains(feature003));
        assertTrue("f1-f4", members.get(feature001).contains(feature004));
        assertTrue("f1-f5", members.get(feature001).contains(feature005));
        assertTrue("f1-f6", members.get(feature001).contains(feature006));
        assertFalse("f1-f7", members.get(feature001).contains(feature007));
        assertTrue("f1-f8", members.get(feature001).contains(feature008));
        assertTrue("f1-f9", members.get(feature001).contains(feature009));
        assertFalse("f1-f10", members.get(feature001).contains(feature010));
        assertFalse("f1-f11", members.get(feature001).contains(feature011));

        assertTrue("f2-f1", members.get(feature002).contains(feature001));
        assertTrue("f2-f2", members.get(feature002).contains(feature002));
        assertFalse("f2-f3", members.get(feature002).contains(feature003));
        assertTrue("f2-f4", members.get(feature002).contains(feature004));
        assertTrue("f2-f5", members.get(feature002).contains(feature005));
        assertTrue("f2-f6", members.get(feature002).contains(feature006));
        assertFalse("f2-f7", members.get(feature002).contains(feature007));
        assertTrue("f2-f8", members.get(feature002).contains(feature008));
        assertTrue("f2-f9", members.get(feature002).contains(feature009));
        assertFalse("f2-f10", members.get(feature002).contains(feature010));
        assertFalse("f2-f11", members.get(feature002).contains(feature011));

        assertFalse("f3-f1", members.get(feature003).contains(feature001));
        assertFalse("f3-f2", members.get(feature003).contains(feature002));
        assertTrue("f3-f3", members.get(feature003).contains(feature003));
        assertFalse("f3-f4", members.get(feature003).contains(feature004));
        assertFalse("f3-f5", members.get(feature003).contains(feature005));
        assertFalse("f3-f6", members.get(feature003).contains(feature006));
        assertFalse("f3-f7", members.get(feature003).contains(feature007));
        assertFalse("f3-f8", members.get(feature003).contains(feature008));
        assertFalse("f3-f9", members.get(feature003).contains(feature009));
        assertFalse("f3-f10", members.get(feature003).contains(feature010));
        assertFalse("f3-f11", members.get(feature003).contains(feature011));

        assertFalse("f4-f1", members.get(feature004).contains(feature001));
        assertFalse("f4-f2", members.get(feature004).contains(feature002));
        assertFalse("f4-f3", members.get(feature004).contains(feature003));
        assertTrue("f4-f4", members.get(feature004).contains(feature004));
        assertTrue("f4-f5", members.get(feature004).contains(feature005));
        assertFalse("f4-f6", members.get(feature004).contains(feature006));
        assertFalse("f4-f7", members.get(feature004).contains(feature007));
        assertFalse("f4-f8", members.get(feature004).contains(feature008));
        assertFalse("f4-f9", members.get(feature004).contains(feature009));
        assertFalse("f4-f10", members.get(feature004).contains(feature010));
        assertFalse("f4-f11", members.get(feature004).contains(feature011));

        assertFalse("f5-f1", members.get(feature005).contains(feature001));
        assertFalse("f5-f2", members.get(feature005).contains(feature002));
        assertFalse("f5-f3", members.get(feature005).contains(feature003));
        assertTrue("f5-f4", members.get(feature005).contains(feature004));
        assertTrue("f5-f5", members.get(feature005).contains(feature005));
        assertFalse("f5-f6", members.get(feature005).contains(feature006));
        assertFalse("f5-f7", members.get(feature005).contains(feature007));
        assertFalse("f5-f8", members.get(feature005).contains(feature008));
        assertFalse("f5-f9", members.get(feature005).contains(feature009));
        assertFalse("f5-f10", members.get(feature005).contains(feature010));
        assertFalse("f5-f11", members.get(feature005).contains(feature011));

        assertFalse("f6-f1", members.get(feature006).contains(feature001));
        assertFalse("f6-f2", members.get(feature006).contains(feature002));
        assertFalse("f6-f3", members.get(feature006).contains(feature003));
        assertTrue("f6-f4", members.get(feature006).contains(feature004));
        assertTrue("f6-f5", members.get(feature006).contains(feature005));
        assertTrue("f6-f6", members.get(feature006).contains(feature006));
        assertFalse("f6-f7", members.get(feature006).contains(feature007));
        assertTrue("f6-f8", members.get(feature006).contains(feature008));
        assertTrue("f6-f9", members.get(feature006).contains(feature009));
        assertFalse("f6-f10", members.get(feature006).contains(feature010));
        assertFalse("f6-f11", members.get(feature006).contains(feature011));

        assertFalse("f7-f1", members.get(feature007).contains(feature001));
        assertFalse("f7-f2", members.get(feature007).contains(feature002));
        assertFalse("f7-f3", members.get(feature007).contains(feature003));
        assertFalse("f7-f4", members.get(feature007).contains(feature004));
        assertFalse("f7-f5", members.get(feature007).contains(feature005));
        assertFalse("f7-f6", members.get(feature007).contains(feature006));
        assertTrue("f7-f7", members.get(feature007).contains(feature007));
        assertFalse("f7-f8", members.get(feature007).contains(feature008));
        assertFalse("f7-f9", members.get(feature007).contains(feature009));
        assertFalse("f7-f10", members.get(feature007).contains(feature010));
        assertFalse("f7-f11", members.get(feature007).contains(feature011));

        assertFalse("f8-f1", members.get(feature008).contains(feature001));
        assertFalse("f8-f2", members.get(feature008).contains(feature002));
        assertFalse("f8-f3", members.get(feature008).contains(feature003));
        assertFalse("f8-f4", members.get(feature008).contains(feature004));
        assertFalse("f8-f5", members.get(feature008).contains(feature005));
        assertFalse("f8-f6", members.get(feature008).contains(feature006));
        assertFalse("f8-f7", members.get(feature008).contains(feature007));
        assertTrue("f8-f8", members.get(feature008).contains(feature008));
        assertTrue("f8-f9", members.get(feature008).contains(feature009));
        assertFalse("f8-f10", members.get(feature008).contains(feature010));
        assertFalse("f8-f11", members.get(feature008).contains(feature011));

        assertFalse("f9-f1", members.get(feature009).contains(feature001));
        assertFalse("f9-f2", members.get(feature009).contains(feature002));
        assertFalse("f9-f3", members.get(feature009).contains(feature003));
        assertFalse("f9-f4", members.get(feature009).contains(feature004));
        assertFalse("f9-f5", members.get(feature009).contains(feature005));
        assertFalse("f9-f6", members.get(feature009).contains(feature006));
        assertFalse("f9-f7", members.get(feature009).contains(feature007));
        assertTrue("f9-f8", members.get(feature009).contains(feature008));
        assertTrue("f9-f9", members.get(feature009).contains(feature009));
        assertFalse("f9-f10", members.get(feature009).contains(feature010));
        assertFalse("f9-f11", members.get(feature009).contains(feature011));

        assertFalse("f10-f1", members.get(feature010).contains(feature001));
        assertFalse("f10-f2", members.get(feature010).contains(feature002));
        assertFalse("f10-f3", members.get(feature010).contains(feature003));
        assertFalse("f10-f4", members.get(feature010).contains(feature004));
        assertFalse("f10-f5", members.get(feature010).contains(feature005));
        assertFalse("f10-f6", members.get(feature010).contains(feature006));
        assertFalse("f10-f7", members.get(feature010).contains(feature007));
        assertFalse("f10-f8", members.get(feature010).contains(feature008));
        assertFalse("f10-f9", members.get(feature010).contains(feature009));
        assertTrue("f10-f10", members.get(feature010).contains(feature010));
        assertTrue("f10-f11", members.get(feature010).contains(feature011));

        assertFalse("f11-f1", members.get(feature011).contains(feature001));
        assertFalse("f11-f2", members.get(feature011).contains(feature002));
        assertFalse("f11-f3", members.get(feature011).contains(feature003));
        assertFalse("f11-f4", members.get(feature011).contains(feature004));
        assertFalse("f11-f5", members.get(feature011).contains(feature005));
        assertFalse("f11-f6", members.get(feature011).contains(feature006));
        assertFalse("f11-f7", members.get(feature011).contains(feature007));
        assertFalse("f11-f8", members.get(feature011).contains(feature008));
        assertFalse("f11-f9", members.get(feature011).contains(feature009));
        assertFalse("f11-f10", members.get(feature011).contains(feature010));
        assertTrue("f11-f10", members.get(feature011).contains(feature011));
    }

    @Test
    public void getIndividualsHavingPart() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7987 51.0600)");

        OWLIndividual feature002 = i("feature001");
        OWLIndividual geom002 = i("geom001");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7987 51.0600)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7937 51.0602)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8008 51.0576,13.8013 51.0586,13.8018 51.0575,13.8018 51.0585,13.8026 51.0575,13.8024 51.0585)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7976 51.0597,13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597,13.8006 51.0589))");

        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.7929 51.0617,13.7940 51.0618,13.7940 51.0609,13.7927 51.0610,13.7929 51.0617))");

        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7927 51.0606,13.7918 51.0610,13.7920 51.0619,13.7936 51.0623,13.7957 51.0616,13.7950 51.0607,13.7927 51.0606))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result = reasoner.getIndividualsHavingPart(feature001)
                .collect(Collectors.toSet());

        assertTrue("f1-f1", result.contains(feature001));
        assertTrue("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertTrue("f1-f4", result.contains(feature004));
        assertTrue("f1-f5", result.contains(feature005));
        assertTrue("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertTrue("f1-f8", result.contains(feature008));
        assertTrue("f1-f9", result.contains(feature009));
        assertFalse("f1-f10", result.contains(feature010));
        assertFalse("f1-f11", result.contains(feature011));

        result = reasoner.getIndividualsHavingPart(feature002)
                .collect(Collectors.toSet());

        assertTrue("f2-f1", result.contains(feature001));
        assertTrue("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertTrue("f2-f4", result.contains(feature004));
        assertTrue("f2-f5", result.contains(feature005));
        assertTrue("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertTrue("f2-f8", result.contains(feature008));
        assertTrue("f2-f9", result.contains(feature009));
        assertFalse("f2-f10", result.contains(feature010));
        assertFalse("f2-f11", result.contains(feature011));

        result = reasoner.getIndividualsHavingPart(feature003)
                .collect(Collectors.toSet());

        assertFalse("f3-f1",  result.contains(feature001));
        assertFalse("f3-f2",  result.contains(feature002));
        assertTrue("f3-f3",  result.contains(feature003));
        assertFalse("f3-f4",  result.contains(feature004));
        assertFalse("f3-f5",  result.contains(feature005));
        assertFalse("f3-f6",  result.contains(feature006));
        assertFalse("f3-f7",  result.contains(feature007));
        assertFalse("f3-f8",  result.contains(feature008));
        assertFalse("f3-f9",  result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertFalse("f3-f11", result.contains(feature011));

        result = reasoner.getIndividualsHavingPart(feature004)
                .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertTrue("f4-f4", result.contains(feature004));
        assertTrue("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));

        result = reasoner.getIndividualsHavingPart(feature005)
                .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertTrue("f5-f4", result.contains(feature004));
        assertTrue("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertFalse("f5-f11", result.contains(feature011));

        result = reasoner.getIndividualsHavingPart(feature006)
                .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertTrue("f6-f4", result.contains(feature004));
        assertTrue("f6-f5", result.contains(feature005));
        assertTrue("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertTrue("f6-f8", result.contains(feature008));
        assertTrue("f6-f9", result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertFalse("f6-f11", result.contains(feature011));

        result = reasoner.getIndividualsHavingPart(feature007)
                .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertTrue("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertFalse("f7-f10", result.contains(feature010));
        assertFalse("f7-f11", result.contains(feature011));

        result = reasoner.getIndividualsHavingPart(feature008)
                .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertTrue("f8-f8", result.contains(feature008));
        assertTrue("f8-f9", result.contains(feature009));
        assertFalse("f8-f10", result.contains(feature010));
        assertFalse("f8-f11", result.contains(feature011));

        result = reasoner.getIndividualsHavingPart(feature009)
                .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertTrue("f9-f8", result.contains(feature008));
        assertTrue("f9-f9", result.contains(feature009));
        assertFalse("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));

        result = reasoner.getIndividualsHavingPart(feature010)
                .collect(Collectors.toSet());

        assertFalse("f10-f1", result.contains(feature001));
        assertFalse("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertFalse("f10-f6", result.contains(feature006));
        assertFalse("f10-f7", result.contains(feature007));
        assertFalse("f10-f8", result.contains(feature008));
        assertFalse("f10-f9", result.contains(feature009));
        assertTrue("f10-f10", result.contains(feature010));
        assertTrue("f10-f11", result.contains(feature011));


        result = reasoner.getIndividualsHavingPart(feature011)
                .collect(Collectors.toSet());

        assertFalse("f11-f1", result.contains(feature001));
        assertFalse("f11-f2", result.contains(feature002));
        assertFalse("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertFalse("f11-f5", result.contains(feature005));
        assertFalse("f11-f6", result.contains(feature006));
        assertFalse("f11-f7", result.contains(feature007));
        assertFalse("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertFalse("f11-f10", result.contains(feature010));
        assertFalse("f11-f11", result.contains(feature011));
    }

    @Test
    public void testHasPart() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7987 51.0600)");

        OWLIndividual feature002 = i("feature001");
        OWLIndividual geom002 = i("geom001");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7987 51.0600)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7937 51.0602)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8008 51.0576,13.8013 51.0586,13.8018 51.0575,13.8018 51.0585,13.8026 51.0575,13.8024 51.0585)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7976 51.0597,13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597,13.8006 51.0589))");

        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.7929 51.0617,13.7940 51.0618,13.7940 51.0609,13.7927 51.0610,13.7929 51.0617))");

        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7927 51.0606,13.7918 51.0610,13.7920 51.0619,13.7936 51.0623,13.7957 51.0616,13.7950 51.0607,13.7927 51.0606))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertTrue("f1-f1", reasoner.hasPart(feature001, feature001));
        assertTrue("f1-f2", reasoner.hasPart(feature001, feature002));
        assertFalse("f1-f3", reasoner.hasPart(feature001, feature003));
        assertFalse("f1-f4", reasoner.hasPart(feature001, feature004));
        assertFalse("f1-f5", reasoner.hasPart(feature001, feature005));
        assertFalse("f1-f6", reasoner.hasPart(feature001, feature006));
        assertFalse("f1-f7", reasoner.hasPart(feature001, feature007));
        assertFalse("f1-f8", reasoner.hasPart(feature001, feature008));
        assertFalse("f1-f9", reasoner.hasPart(feature001, feature009));
        assertFalse("f1-f10", reasoner.hasPart(feature001, feature010));
        assertFalse("f1-f11", reasoner.hasPart(feature001, feature011));

        assertTrue("f2-f1",   reasoner.hasPart(feature002, feature001));
        assertTrue("f2-f2",   reasoner.hasPart(feature002, feature002));
        assertFalse("f2-f3", reasoner.hasPart(feature002, feature003));
        assertFalse("f2-f4", reasoner.hasPart(feature002, feature004));
        assertFalse("f2-f5", reasoner.hasPart(feature002, feature005));
        assertFalse("f2-f6", reasoner.hasPart(feature002, feature006));
        assertFalse("f2-f7", reasoner.hasPart(feature002, feature007));
        assertFalse("f2-f8", reasoner.hasPart(feature002, feature008));
        assertFalse("f2-f9", reasoner.hasPart(feature002, feature009));
        assertFalse("f2-f10", reasoner.hasPart(feature002, feature010));
        assertFalse("f2-f11", reasoner.hasPart(feature002, feature011));

        assertFalse("f3-f1",  reasoner.hasPart(feature003, feature001));
        assertFalse("f3-f2", reasoner.hasPart(feature003, feature002));
        assertTrue("f3-f3", reasoner.hasPart(feature003, feature003));
        assertFalse("f3-f4", reasoner.hasPart(feature003, feature004));
        assertFalse("f3-f5", reasoner.hasPart(feature003, feature005));
        assertFalse("f3-f6", reasoner.hasPart(feature003, feature006));
        assertFalse("f3-f7", reasoner.hasPart(feature003, feature007));
        assertFalse("f3-f8", reasoner.hasPart(feature003, feature008));
        assertFalse("f3-f9", reasoner.hasPart(feature003, feature009));
        assertFalse("f3-f10", reasoner.hasPart(feature003, feature010));
        assertFalse("f3-f11", reasoner.hasPart(feature003, feature011));

        assertTrue("f4-f1",  reasoner.hasPart(feature004, feature001));
        assertTrue("f4-f2", reasoner.hasPart(feature004, feature002));
        assertFalse("f4-f3", reasoner.hasPart(feature004, feature003));
        assertTrue("f4-f4", reasoner.hasPart(feature004, feature004));
        assertTrue("f4-f5", reasoner.hasPart(feature004, feature005));
        assertTrue("f4-f6", reasoner.hasPart(feature004, feature006));
        assertFalse("f4-f7", reasoner.hasPart(feature004, feature007));
        assertFalse("f4-f8", reasoner.hasPart(feature004, feature008));
        assertFalse("f4-f9", reasoner.hasPart(feature004, feature009));
        assertFalse("f4-f10", reasoner.hasPart(feature004, feature010));
        assertFalse("f4-f11", reasoner.hasPart(feature004, feature011));

        assertTrue("f5-f1",  reasoner.hasPart(feature005, feature001));
        assertTrue("f5-f2", reasoner.hasPart(feature005, feature002));
        assertFalse("f5-f3", reasoner.hasPart(feature005, feature003));
        assertTrue("f5-f4", reasoner.hasPart(feature005, feature004));
        assertTrue("f5-f5", reasoner.hasPart(feature005, feature005));
        assertTrue("f5-f6", reasoner.hasPart(feature005, feature006));
        assertFalse("f5-f7", reasoner.hasPart(feature005, feature007));
        assertFalse("f5-f8", reasoner.hasPart(feature005, feature008));
        assertFalse("f5-f9", reasoner.hasPart(feature005, feature009));
        assertFalse("f5-f10", reasoner.hasPart(feature005, feature010));
        assertFalse("f5-f11", reasoner.hasPart(feature005, feature011));

        assertTrue("f6-f1",  reasoner.hasPart(feature006, feature001));
        assertTrue("f6-f2", reasoner.hasPart(feature006, feature002));
        assertFalse("f6-f3", reasoner.hasPart(feature006, feature003));
        assertFalse("f6-f4", reasoner.hasPart(feature006, feature004));
        assertFalse("f6-f5", reasoner.hasPart(feature006, feature005));
        assertTrue("f6-f6", reasoner.hasPart(feature006, feature006));
        assertFalse("f6-f7", reasoner.hasPart(feature006, feature007));
        assertFalse("f6-f8", reasoner.hasPart(feature006, feature008));
        assertFalse("f6-f9", reasoner.hasPart(feature006, feature009));
        assertFalse("f6-f10", reasoner.hasPart(feature006, feature010));
        assertFalse("f6-f11", reasoner.hasPart(feature006, feature011));

        assertFalse("f7-f1",  reasoner.hasPart(feature007, feature001));
        assertFalse("f7-f2", reasoner.hasPart(feature007, feature002));
        assertFalse("f7-f3", reasoner.hasPart(feature007, feature003));
        assertFalse("f7-f4", reasoner.hasPart(feature007, feature004));
        assertFalse("f7-f5", reasoner.hasPart(feature007, feature005));
        assertFalse("f7-f6", reasoner.hasPart(feature007, feature006));
        assertTrue("f7-f7", reasoner.hasPart(feature007, feature007));
        assertFalse("f7-f8", reasoner.hasPart(feature007, feature008));
        assertFalse("f7-f9", reasoner.hasPart(feature007, feature009));
        assertFalse("f7-f10", reasoner.hasPart(feature007, feature010));
        assertFalse("f7-f11", reasoner.hasPart(feature007, feature011));

        assertTrue("f8-f1",  reasoner.hasPart(feature008, feature001));
        assertTrue("f8-f2", reasoner.hasPart(feature008, feature002));
        assertFalse("f8-f3", reasoner.hasPart(feature008, feature003));
        assertFalse("f8-f4", reasoner.hasPart(feature008, feature004));
        assertFalse("f8-f5", reasoner.hasPart(feature008, feature005));
        assertTrue("f8-f6", reasoner.hasPart(feature008, feature006));
        assertFalse("f8-f7", reasoner.hasPart(feature008, feature007));
        assertTrue("f8-f8", reasoner.hasPart(feature008, feature008));
        assertTrue("f8-f9", reasoner.hasPart(feature008, feature009));
        assertFalse("f8-f10", reasoner.hasPart(feature008, feature010));
        assertFalse("f8-f11", reasoner.hasPart(feature008, feature011));

        assertTrue("f9-f1",  reasoner.hasPart(feature009, feature001));
        assertTrue("f9-f2", reasoner.hasPart(feature009, feature002));
        assertFalse("f9-f3", reasoner.hasPart(feature009, feature003));
        assertFalse("f9-f4", reasoner.hasPart(feature009, feature004));
        assertFalse("f9-f5", reasoner.hasPart(feature009, feature005));
        assertTrue("f9-f6", reasoner.hasPart(feature009, feature006));
        assertFalse("f9-f7", reasoner.hasPart(feature009, feature007));
        assertTrue("f9-f8", reasoner.hasPart(feature009, feature008));
        assertTrue("f9-f9", reasoner.hasPart(feature009, feature009));
        assertFalse("f9-f10", reasoner.hasPart(feature009, feature010));
        assertFalse("f9-f11", reasoner.hasPart(feature009, feature011));

        assertFalse("f10-f1", reasoner.hasPart(feature010, feature001));
        assertFalse("f10-f2", reasoner.hasPart(feature010, feature002));
        assertFalse("f10-f3", reasoner.hasPart(feature010, feature003));
        assertFalse("f10-f4", reasoner.hasPart(feature010, feature004));
        assertFalse("f10-f5", reasoner.hasPart(feature010, feature005));
        assertFalse("f10-f6", reasoner.hasPart(feature010, feature006));
        assertFalse("f10-f7", reasoner.hasPart(feature010, feature007));
        assertFalse("f10-f8", reasoner.hasPart(feature010, feature008));
        assertFalse("f10-f9", reasoner.hasPart(feature010, feature009));
        assertTrue("f10-f10", reasoner.hasPart(feature010, feature010));
        assertFalse("f10-f11", reasoner.hasPart(feature010, feature011));

        assertFalse("f11-f1", reasoner.hasPart(feature011, feature001));
        assertFalse("f11-f2", reasoner.hasPart(feature011, feature002));
        assertFalse("f11-f3", reasoner.hasPart(feature011, feature003));
        assertFalse("f11-f4", reasoner.hasPart(feature011, feature004));
        assertFalse("f11-f5", reasoner.hasPart(feature011, feature005));
        assertFalse("f11-f6", reasoner.hasPart(feature011, feature006));
        assertFalse("f11-f7", reasoner.hasPart(feature011, feature007));
        assertFalse("f11-f8", reasoner.hasPart(feature011, feature008));
        assertFalse("f11-f9", reasoner.hasPart(feature011, feature009));
        assertTrue("f11-f10", reasoner.hasPart(feature011, feature010));
        assertTrue("f11-f11", reasoner.hasPart(feature011, feature011));
    }

    @Test
    public void testGetHasPartMembers() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7987 51.0600)");

        OWLIndividual feature002 = i("feature001");
        OWLIndividual geom002 = i("geom001");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7987 51.0600)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7937 51.0602)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7984 51.0608,13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.7987 51.0600,13.8003 51.0603,13.8004 51.0592)");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8008 51.0576,13.8013 51.0586,13.8018 51.0575,13.8018 51.0585,13.8026 51.0575,13.8024 51.0585)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.7976 51.0597,13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8006 51.0589,13.8011 51.0605,13.7991 51.0607,13.7976 51.0597,13.8006 51.0589))");

        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.7929 51.0617,13.7940 51.0618,13.7940 51.0609,13.7927 51.0610,13.7929 51.0617))");

        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7927 51.0606,13.7918 51.0610,13.7920 51.0619,13.7936 51.0623,13.7957 51.0616,13.7950 51.0607,13.7927 51.0606))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = reasoner.getHasPartMembers();

        assertTrue("f1-f1", members.get(feature001).contains(feature001));
        assertTrue("f1-f2", members.get(feature001).contains(feature002));
        assertFalse("f1-f3", members.get(feature001).contains(feature003));
        assertFalse("f1-f4", members.get(feature001).contains(feature004));
        assertFalse("f1-f5", members.get(feature001).contains(feature005));
        assertFalse("f1-f6", members.get(feature001).contains(feature006));
        assertFalse("f1-f7", members.get(feature001).contains(feature007));
        assertFalse("f1-f8", members.get(feature001).contains(feature008));
        assertFalse("f1-f9", members.get(feature001).contains(feature009));
        assertFalse("f1-f10", members.get(feature001).contains(feature010));
        assertFalse("f1-f11", members.get(feature001).contains(feature011));

        assertTrue("f2-f1",   members.get(feature002).contains(feature001));
        assertTrue("f2-f2",   members.get(feature002).contains(feature002));
        assertFalse("f2-f3", members.get(feature002).contains(feature003));
        assertFalse("f2-f4", members.get(feature002).contains(feature004));
        assertFalse("f2-f5", members.get(feature002).contains(feature005));
        assertFalse("f2-f6", members.get(feature002).contains(feature006));
        assertFalse("f2-f7", members.get(feature002).contains(feature007));
        assertFalse("f2-f8", members.get(feature002).contains(feature008));
        assertFalse("f2-f9", members.get(feature002).contains(feature009));
        assertFalse("f2-f10", members.get(feature002).contains(feature010));
        assertFalse("f2-f11", members.get(feature002).contains(feature011));

        assertFalse("f3-f1",  members.get(feature003).contains(feature001));
        assertFalse("f3-f2", members.get(feature003).contains(feature002));
        assertTrue("f3-f3", members.get(feature003).contains(feature003));
        assertFalse("f3-f4", members.get(feature003).contains(feature004));
        assertFalse("f3-f5", members.get(feature003).contains(feature005));
        assertFalse("f3-f6", members.get(feature003).contains(feature006));
        assertFalse("f3-f7", members.get(feature003).contains(feature007));
        assertFalse("f3-f8", members.get(feature003).contains(feature008));
        assertFalse("f3-f9", members.get(feature003).contains(feature009));
        assertFalse("f3-f10", members.get(feature003).contains(feature010));
        assertFalse("f3-f11", members.get(feature003).contains(feature011));

        assertTrue("f4-f1",  members.get(feature004).contains(feature001));
        assertTrue("f4-f2", members.get(feature004).contains(feature002));
        assertFalse("f4-f3", members.get(feature004).contains(feature003));
        assertTrue("f4-f4", members.get(feature004).contains(feature004));
        assertTrue("f4-f5", members.get(feature004).contains(feature005));
        assertTrue("f4-f6", members.get(feature004).contains(feature006));
        assertFalse("f4-f7", members.get(feature004).contains(feature007));
        assertFalse("f4-f8", members.get(feature004).contains(feature008));
        assertFalse("f4-f9", members.get(feature004).contains(feature009));
        assertFalse("f4-f10", members.get(feature004).contains(feature010));
        assertFalse("f4-f11", members.get(feature004).contains(feature011));

        assertTrue("f5-f1",  members.get(feature005).contains(feature001));
        assertTrue("f5-f2", members.get(feature005).contains(feature002));
        assertFalse("f5-f3", members.get(feature005).contains(feature003));
        assertTrue("f5-f4", members.get(feature005).contains(feature004));
        assertTrue("f5-f5", members.get(feature005).contains(feature005));
        assertTrue("f5-f6", members.get(feature005).contains(feature006));
        assertFalse("f5-f7", members.get(feature005).contains(feature007));
        assertFalse("f5-f8", members.get(feature005).contains(feature008));
        assertFalse("f5-f9", members.get(feature005).contains(feature009));
        assertFalse("f5-f10", members.get(feature005).contains(feature010));
        assertFalse("f5-f11", members.get(feature005).contains(feature011));

        assertTrue("f6-f1",  members.get(feature006).contains(feature001));
        assertTrue("f6-f2", members.get(feature006).contains(feature002));
        assertFalse("f6-f3", members.get(feature006).contains(feature003));
        assertFalse("f6-f4", members.get(feature006).contains(feature004));
        assertFalse("f6-f5", members.get(feature006).contains(feature005));
        assertTrue("f6-f6", members.get(feature006).contains(feature006));
        assertFalse("f6-f7", members.get(feature006).contains(feature007));
        assertFalse("f6-f8", members.get(feature006).contains(feature008));
        assertFalse("f6-f9", members.get(feature006).contains(feature009));
        assertFalse("f6-f10", members.get(feature006).contains(feature010));
        assertFalse("f6-f11", members.get(feature006).contains(feature011));

        assertFalse("f7-f1",  members.get(feature007).contains(feature001));
        assertFalse("f7-f2", members.get(feature007).contains(feature002));
        assertFalse("f7-f3", members.get(feature007).contains(feature003));
        assertFalse("f7-f4", members.get(feature007).contains(feature004));
        assertFalse("f7-f5", members.get(feature007).contains(feature005));
        assertFalse("f7-f6", members.get(feature007).contains(feature006));
        assertTrue("f7-f7", members.get(feature007).contains(feature007));
        assertFalse("f7-f8", members.get(feature007).contains(feature008));
        assertFalse("f7-f9", members.get(feature007).contains(feature009));
        assertFalse("f7-f10", members.get(feature007).contains(feature010));
        assertFalse("f7-f11", members.get(feature007).contains(feature011));

        assertTrue("f8-f1",  members.get(feature008).contains(feature001));
        assertTrue("f8-f2", members.get(feature008).contains(feature002));
        assertFalse("f8-f3", members.get(feature008).contains(feature003));
        assertFalse("f8-f4", members.get(feature008).contains(feature004));
        assertFalse("f8-f5", members.get(feature008).contains(feature005));
        assertTrue("f8-f6", members.get(feature008).contains(feature006));
        assertFalse("f8-f7", members.get(feature008).contains(feature007));
        assertTrue("f8-f8", members.get(feature008).contains(feature008));
        assertTrue("f8-f9", members.get(feature008).contains(feature009));
        assertFalse("f8-f10", members.get(feature008).contains(feature010));
        assertFalse("f8-f11", members.get(feature008).contains(feature011));

        assertTrue("f9-f1",  members.get(feature009).contains(feature001));
        assertTrue("f9-f2", members.get(feature009).contains(feature002));
        assertFalse("f9-f3", members.get(feature009).contains(feature003));
        assertFalse("f9-f4", members.get(feature009).contains(feature004));
        assertFalse("f9-f5", members.get(feature009).contains(feature005));
        assertTrue("f9-f6", members.get(feature009).contains(feature006));
        assertFalse("f9-f7", members.get(feature009).contains(feature007));
        assertTrue("f9-f8", members.get(feature009).contains(feature008));
        assertTrue("f9-f9", members.get(feature009).contains(feature009));
        assertFalse("f9-f10", members.get(feature009).contains(feature010));
        assertFalse("f9-f11", members.get(feature009).contains(feature011));

        assertFalse("f10-f1", members.get(feature010).contains(feature001));
        assertFalse("f10-f2", members.get(feature010).contains(feature002));
        assertFalse("f10-f3", members.get(feature010).contains(feature003));
        assertFalse("f10-f4", members.get(feature010).contains(feature004));
        assertFalse("f10-f5", members.get(feature010).contains(feature005));
        assertFalse("f10-f6", members.get(feature010).contains(feature006));
        assertFalse("f10-f7", members.get(feature010).contains(feature007));
        assertFalse("f10-f8", members.get(feature010).contains(feature008));
        assertFalse("f10-f9", members.get(feature010).contains(feature009));
        assertTrue("f10-f10", members.get(feature010).contains(feature010));
        assertFalse("f10-f11", members.get(feature010).contains(feature011));

        assertFalse("f11-f1", members.get(feature011).contains(feature001));
        assertFalse("f11-f2", members.get(feature011).contains(feature002));
        assertFalse("f11-f3", members.get(feature011).contains(feature003));
        assertFalse("f11-f4", members.get(feature011).contains(feature004));
        assertFalse("f11-f5", members.get(feature011).contains(feature005));
        assertFalse("f11-f6", members.get(feature011).contains(feature006));
        assertFalse("f11-f7", members.get(feature011).contains(feature007));
        assertFalse("f11-f8", members.get(feature011).contains(feature008));
        assertFalse("f11-f9", members.get(feature011).contains(feature009));
        assertTrue("f11-f10", members.get(feature011).contains(feature010));
        assertTrue("f11-f11", members.get(feature011).contains(feature011));
    }

    @Test
    public void testGetIndividualsProperPartOf() throws ComponentInitException {
        //
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0596)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0596)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7994 51.0595)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.8002 51.0600,13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8002 51.0600,13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8028 51.0569,13.8030 51.0574,13.8042 51.0571,13.8037 51.0566,13.8031 51.0569,13.8033 51.0572,13.8038 51.0570,13.8036 51.05689)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.8011 51.0596,13.8021 51.0589,13.8011 51.0586,13.7986 51.0593,13.7995 51.0598,13.8011 51.0596,13.8011 51.0596))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8011 51.0596,13.8021 51.0589,13.8011 51.0586,13.7986 51.0593,13.7995 51.0598,13.8011 51.0596,13.8011 51.0596))");

        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.7998 51.0591,13.8003 51.0594,13.8014 51.0591,13.7998 51.0591))");

        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8012 51.0582,13.8017 51.0578,13.8026 51.0582,13.8012 51.0582))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result = reasoner.getIndividualsProperPartOf(feature001)
                .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));
        assertFalse("f1-f10", result.contains(feature010));
        assertFalse("f1-f11", result.contains(feature011));

        result = reasoner.getIndividualsProperPartOf(feature002)
                .collect(Collectors.toSet());

        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));
        assertFalse("f2-f10", result.contains(feature010));
        assertFalse("f2-f11", result.contains(feature011));

        result = reasoner.getIndividualsProperPartOf(feature003)
                .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertFalse("f3-f11", result.contains(feature011));

        result = reasoner.getIndividualsProperPartOf(feature004)
                .collect(Collectors.toSet());

        assertTrue("f4-f1", result.contains(feature001));
        assertTrue("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertFalse("f4-f5", result.contains(feature005));
        assertTrue("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));

        result = reasoner.getIndividualsProperPartOf(feature005)
                .collect(Collectors.toSet());

        assertTrue("f5-f1", result.contains(feature001));
        assertTrue("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertFalse("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertTrue("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertFalse("f5-f11", result.contains(feature011));

        result = reasoner.getIndividualsProperPartOf(feature006)
                .collect(Collectors.toSet());

        assertTrue("f6-f1", result.contains(feature001));
        assertTrue("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertFalse("f6-f11", result.contains(feature011));

        result = reasoner.getIndividualsProperPartOf(feature007)
                .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertFalse("f7-f10", result.contains(feature010));
        assertFalse("f7-f11", result.contains(feature011));

        result = reasoner.getIndividualsProperPartOf(feature008)
                .collect(Collectors.toSet());

        assertTrue("f8-f1", result.contains(feature001));
        assertTrue("f8-f2", result.contains(feature002));
        assertTrue("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertTrue("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertFalse("f8-f8", result.contains(feature008));
        assertFalse("f8-f9", result.contains(feature009));
        assertTrue("f8-f10", result.contains(feature010));
        assertFalse("f8-f11", result.contains(feature011));

        result = reasoner.getIndividualsProperPartOf(feature009)
                .collect(Collectors.toSet());

        assertTrue("f9-f1", result.contains(feature001));
        assertTrue("f9-f2", result.contains(feature002));
        assertTrue("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertTrue("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertFalse("f9-f8", result.contains(feature008));
        assertFalse("f9-f9", result.contains(feature009));
        assertTrue("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));

        result = reasoner.getIndividualsProperPartOf(feature010)
                .collect(Collectors.toSet());

        assertFalse("f10-f1", result.contains(feature001));
        assertFalse("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertFalse("f10-f6", result.contains(feature006));
        assertFalse("f10-f7", result.contains(feature007));
        assertFalse("f10-f8", result.contains(feature008));
        assertFalse("f10-f9", result.contains(feature009));
        assertFalse("f10-f10", result.contains(feature010));
        assertFalse("f10-f11", result.contains(feature011));

        result = reasoner.getIndividualsProperPartOf(feature011)
                .collect(Collectors.toSet());

        assertFalse("f11-f1", result.contains(feature001));
        assertFalse("f11-f2", result.contains(feature002));
        assertFalse("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertFalse("f11-f5", result.contains(feature005));
        assertFalse("f11-f6", result.contains(feature006));
        assertFalse("f11-f7", result.contains(feature007));
        assertFalse("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertFalse("f11-f10", result.contains(feature010));
        assertFalse("f11-f11", result.contains(feature011));
    }

    @Test
    public void testIsProperPartOf() throws ComponentInitException {
        //
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0596)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0596)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7994 51.0595)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.8002 51.0600,13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8002 51.0600,13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8028 51.0569,13.8030 51.0574,13.8042 51.0571,13.8037 51.0566,13.8031 51.0569,13.8033 51.0572,13.8038 51.0570,13.8036 51.05689)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.8011 51.0596,13.8021 51.0589,13.8011 51.0586,13.7986 51.0593,13.7995 51.0598,13.8011 51.0596,13.8011 51.0596))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8011 51.0596,13.8021 51.0589,13.8011 51.0586,13.7986 51.0593,13.7995 51.0598,13.8011 51.0596,13.8011 51.0596))");

        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.7998 51.0591,13.8003 51.0594,13.8014 51.0591,13.7998 51.0591))");

        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8012 51.0582,13.8017 51.0578,13.8026 51.0582,13.8012 51.0582))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertFalse("f1-f1", reasoner.isProperPartOf(feature001, feature001));
        assertFalse("f1-f2", reasoner.isProperPartOf(feature001, feature002));
        assertFalse("f1-f3", reasoner.isProperPartOf(feature001, feature003));
        assertTrue("f1-f4", reasoner.isProperPartOf(feature001, feature004));
        assertTrue("f1-f5", reasoner.isProperPartOf(feature001, feature005));
        assertTrue("f1-f6", reasoner.isProperPartOf(feature001, feature006));
        assertFalse("f1-f7", reasoner.isProperPartOf(feature001, feature007));
        assertTrue("f1-f8", reasoner.isProperPartOf(feature001, feature008));
        assertTrue("f1-f9", reasoner.isProperPartOf(feature001, feature009));
        assertFalse("f1-f10", reasoner.isProperPartOf(feature001, feature010));
        assertFalse("f1-f11", reasoner.isProperPartOf(feature001, feature011));

        assertFalse("f2-f1", reasoner.isProperPartOf(feature002, feature001));
        assertFalse("f2-f2", reasoner.isProperPartOf(feature002, feature002));
        assertFalse("f2-f3", reasoner.isProperPartOf(feature002, feature003));
        assertTrue("f2-f4", reasoner.isProperPartOf(feature002, feature004));
        assertTrue("f2-f5", reasoner.isProperPartOf(feature002, feature005));
        assertTrue("f2-f6", reasoner.isProperPartOf(feature002, feature006));
        assertFalse("f2-f7", reasoner.isProperPartOf(feature002, feature007));
        assertTrue("f2-f8", reasoner.isProperPartOf(feature002, feature008));
        assertTrue("f2-f9", reasoner.isProperPartOf(feature002, feature009));
        assertFalse("f2-f10", reasoner.isProperPartOf(feature002, feature010));
        assertFalse("f2-f11", reasoner.isProperPartOf(feature002, feature011));

        assertFalse("f3-f1", reasoner.isProperPartOf(feature003, feature001));
        assertFalse("f3-f2", reasoner.isProperPartOf(feature003, feature002));
        assertFalse("f3-f3", reasoner.isProperPartOf(feature003, feature003));
        assertFalse("f3-f4", reasoner.isProperPartOf(feature003, feature004));
        assertFalse("f3-f5", reasoner.isProperPartOf(feature003, feature005));
        assertFalse("f3-f6", reasoner.isProperPartOf(feature003, feature006));
        assertFalse("f3-f7", reasoner.isProperPartOf(feature003, feature007));
        assertTrue("f3-f8", reasoner.isProperPartOf(feature003, feature008));
        assertTrue("f3-f9", reasoner.isProperPartOf(feature003, feature009));
        assertFalse("f3-f10", reasoner.isProperPartOf(feature003, feature010));
        assertFalse("f3-f11", reasoner.isProperPartOf(feature003, feature011));

        assertFalse("f4-f1", reasoner.isProperPartOf(feature004, feature001));
        assertFalse("f4-f2", reasoner.isProperPartOf(feature004, feature002));
        assertFalse("f4-f3", reasoner.isProperPartOf(feature004, feature003));
        assertFalse("f4-f4", reasoner.isProperPartOf(feature004, feature004));
        assertFalse("f4-f5", reasoner.isProperPartOf(feature004, feature005));
        assertFalse("f4-f6", reasoner.isProperPartOf(feature004, feature006));
        assertFalse("f4-f7", reasoner.isProperPartOf(feature004, feature007));
        assertFalse("f4-f8", reasoner.isProperPartOf(feature004, feature008));
        assertFalse("f4-f9", reasoner.isProperPartOf(feature004, feature009));
        assertFalse("f4-f10", reasoner.isProperPartOf(feature004, feature010));
        assertFalse("f4-f11", reasoner.isProperPartOf(feature004, feature011));

        assertFalse("f5-f1", reasoner.isProperPartOf(feature005, feature001));
        assertFalse("f5-f2", reasoner.isProperPartOf(feature005, feature002));
        assertFalse("f5-f3", reasoner.isProperPartOf(feature005, feature003));
        assertFalse("f5-f4", reasoner.isProperPartOf(feature005, feature004));
        assertFalse("f5-f5", reasoner.isProperPartOf(feature005, feature005));
        assertFalse("f5-f6", reasoner.isProperPartOf(feature005, feature006));
        assertFalse("f5-f7", reasoner.isProperPartOf(feature005, feature007));
        assertFalse("f5-f8", reasoner.isProperPartOf(feature005, feature008));
        assertFalse("f5-f9", reasoner.isProperPartOf(feature005, feature009));
        assertFalse("f5-f10", reasoner.isProperPartOf(feature005, feature010));
        assertFalse("f5-f11", reasoner.isProperPartOf(feature005, feature011));

        assertFalse("f6-f1", reasoner.isProperPartOf(feature006, feature001));
        assertFalse("f6-f2", reasoner.isProperPartOf(feature006, feature002));
        assertFalse("f6-f3", reasoner.isProperPartOf(feature006, feature003));
        assertTrue("f6-f4", reasoner.isProperPartOf(feature006, feature004));
        assertTrue("f6-f5", reasoner.isProperPartOf(feature006, feature005));
        assertFalse("f6-f6", reasoner.isProperPartOf(feature006, feature006));
        assertFalse("f6-f7", reasoner.isProperPartOf(feature006, feature007));
        assertTrue("f6-f8", reasoner.isProperPartOf(feature006, feature008));
        assertTrue("f6-f9", reasoner.isProperPartOf(feature006, feature009));
        assertFalse("f6-f10", reasoner.isProperPartOf(feature006, feature010));
        assertFalse("f6-f11", reasoner.isProperPartOf(feature006, feature011));

        assertFalse("f7-f1", reasoner.isProperPartOf(feature007, feature001));
        assertFalse("f7-f2", reasoner.isProperPartOf(feature007, feature002));
        assertFalse("f7-f3", reasoner.isProperPartOf(feature007, feature003));
        assertFalse("f7-f4", reasoner.isProperPartOf(feature007, feature004));
        assertFalse("f7-f5", reasoner.isProperPartOf(feature007, feature005));
        assertFalse("f7-f6", reasoner.isProperPartOf(feature007, feature006));
        assertFalse("f7-f7", reasoner.isProperPartOf(feature007, feature007));
        assertFalse("f7-f8", reasoner.isProperPartOf(feature007, feature008));
        assertFalse("f7-f9", reasoner.isProperPartOf(feature007, feature009));
        assertFalse("f7-f10", reasoner.isProperPartOf(feature007, feature010));
        assertFalse("f7-f11", reasoner.isProperPartOf(feature007, feature011));

        assertFalse("f8-f1", reasoner.isProperPartOf(feature008, feature001));
        assertFalse("f8-f2", reasoner.isProperPartOf(feature008, feature002));
        assertFalse("f8-f3", reasoner.isProperPartOf(feature008, feature003));
        assertFalse("f8-f4", reasoner.isProperPartOf(feature008, feature004));
        assertFalse("f8-f5", reasoner.isProperPartOf(feature008, feature005));
        assertFalse("f8-f6", reasoner.isProperPartOf(feature008, feature006));
        assertFalse("f8-f7", reasoner.isProperPartOf(feature008, feature007));
        assertFalse("f8-f8", reasoner.isProperPartOf(feature008, feature008));
        assertFalse("f8-f9", reasoner.isProperPartOf(feature008, feature009));
        assertFalse("f8-f10", reasoner.isProperPartOf(feature008, feature010));
        assertFalse("f8-f11", reasoner.isProperPartOf(feature008, feature011));

        assertFalse("f9-f1", reasoner.isProperPartOf(feature009, feature001));
        assertFalse("f9-f2", reasoner.isProperPartOf(feature009, feature002));
        assertFalse("f9-f3", reasoner.isProperPartOf(feature009, feature003));
        assertFalse("f9-f4", reasoner.isProperPartOf(feature009, feature004));
        assertFalse("f9-f5", reasoner.isProperPartOf(feature009, feature005));
        assertFalse("f9-f6", reasoner.isProperPartOf(feature009, feature006));
        assertFalse("f9-f7", reasoner.isProperPartOf(feature009, feature007));
        assertFalse("f9-f8", reasoner.isProperPartOf(feature009, feature008));
        assertFalse("f9-f9", reasoner.isProperPartOf(feature009, feature009));
        assertFalse("f9-f10", reasoner.isProperPartOf(feature009, feature010));
        assertFalse("f9-f11", reasoner.isProperPartOf(feature009, feature011));

        assertFalse("f10-f1", reasoner.isProperPartOf(feature010, feature001));
        assertFalse("f10-f2", reasoner.isProperPartOf(feature010, feature002));
        assertFalse("f10-f3", reasoner.isProperPartOf(feature010, feature003));
        assertFalse("f10-f4", reasoner.isProperPartOf(feature010, feature004));
        assertFalse("f10-f5", reasoner.isProperPartOf(feature010, feature005));
        assertFalse("f10-f6", reasoner.isProperPartOf(feature010, feature006));
        assertFalse("f10-f7", reasoner.isProperPartOf(feature010, feature007));
        assertTrue("f10-f8", reasoner.isProperPartOf(feature010, feature008));
        assertTrue("f10-f9", reasoner.isProperPartOf(feature010, feature009));
        assertFalse("f10-f10", reasoner.isProperPartOf(feature010, feature010));
        assertFalse("f10-f11", reasoner.isProperPartOf(feature010, feature011));

        assertFalse("f11-f1", reasoner.isProperPartOf(feature011, feature001));
        assertFalse("f11-f2", reasoner.isProperPartOf(feature011, feature002));
        assertFalse("f11-f3", reasoner.isProperPartOf(feature011, feature003));
        assertFalse("f11-f4", reasoner.isProperPartOf(feature011, feature004));
        assertFalse("f11-f5", reasoner.isProperPartOf(feature011, feature005));
        assertFalse("f11-f6", reasoner.isProperPartOf(feature011, feature006));
        assertFalse("f11-f7", reasoner.isProperPartOf(feature011, feature007));
        assertFalse("f11-f8", reasoner.isProperPartOf(feature011, feature008));
        assertFalse("f11-f9", reasoner.isProperPartOf(feature011, feature009));
        assertFalse("f11-f10", reasoner.isProperPartOf(feature011, feature010));
        assertFalse("f11-f11", reasoner.isProperPartOf(feature011, feature011));
    }

    @Test
    public void testGetIsProperPartOfMembers() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0596)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0596)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7994 51.0595)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.8002 51.0600,13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8002 51.0600,13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8028 51.0569,13.8030 51.0574,13.8042 51.0571,13.8037 51.0566,13.8031 51.0569,13.8033 51.0572,13.8038 51.0570,13.8036 51.05689)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.8011 51.0596,13.8021 51.0589,13.8011 51.0586,13.7986 51.0593,13.7995 51.0598,13.8011 51.0596,13.8011 51.0596))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8011 51.0596,13.8021 51.0589,13.8011 51.0586,13.7986 51.0593,13.7995 51.0598,13.8011 51.0596,13.8011 51.0596))");

        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.7998 51.0591,13.8003 51.0594,13.8014 51.0591,13.7998 51.0591))");

        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8012 51.0582,13.8017 51.0578,13.8026 51.0582,13.8012 51.0582))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = reasoner.getIsProperPartOfMembers();

        assertFalse("f1-f1", members.get(feature001).contains(feature001));
        assertFalse("f1-f2", members.get(feature001).contains(feature002));
        assertFalse("f1-f3", members.get(feature001).contains(feature003));
        assertTrue("f1-f4", members.get(feature001).contains(feature004));
        assertTrue("f1-f5", members.get(feature001).contains(feature005));
        assertTrue("f1-f6", members.get(feature001).contains(feature006));
        assertFalse("f1-f7", members.get(feature001).contains(feature007));
        assertTrue("f1-f8", members.get(feature001).contains(feature008));
        assertTrue("f1-f9", members.get(feature001).contains(feature009));
        assertFalse("f1-f10", members.get(feature001).contains(feature010));
        assertFalse("f1-f11", members.get(feature001).contains(feature011));

        assertFalse("f2-f1", members.get(feature002).contains(feature001));
        assertFalse("f2-f2", members.get(feature002).contains(feature002));
        assertFalse("f2-f3", members.get(feature002).contains(feature003));
        assertTrue("f2-f4", members.get(feature002).contains(feature004));
        assertTrue("f2-f5", members.get(feature002).contains(feature005));
        assertTrue("f2-f6", members.get(feature002).contains(feature006));
        assertFalse("f2-f7", members.get(feature002).contains(feature007));
        assertTrue("f2-f8", members.get(feature002).contains(feature008));
        assertTrue("f2-f9", members.get(feature002).contains(feature009));
        assertFalse("f2-f10", members.get(feature002).contains(feature010));
        assertFalse("f2-f11", members.get(feature002).contains(feature011));

        assertFalse("f3-f1", members.get(feature003).contains(feature001));
        assertFalse("f3-f2", members.get(feature003).contains(feature002));
        assertFalse("f3-f3", members.get(feature003).contains(feature003));
        assertFalse("f3-f4", members.get(feature003).contains(feature004));
        assertFalse("f3-f5", members.get(feature003).contains(feature005));
        assertFalse("f3-f6", members.get(feature003).contains(feature006));
        assertFalse("f3-f7", members.get(feature003).contains(feature007));
        assertTrue("f3-f8", members.get(feature003).contains(feature008));
        assertTrue("f3-f9", members.get(feature003).contains(feature009));
        assertFalse("f3-f10", members.get(feature003).contains(feature010));
        assertFalse("f3-f11", members.get(feature003).contains(feature011));

        assertTrue("f4-fX", members.get(feature004) == null);
//        assertFalse("f4-f1", members.get(feature004).contains(feature001));
//        assertFalse("f4-f2", members.get(feature004).contains(feature002));
//        assertFalse("f4-f3", members.get(feature004).contains(feature003));
//        assertFalse("f4-f4", members.get(feature004).contains(feature004));
//        assertFalse("f4-f5", members.get(feature004).contains(feature005));
//        assertFalse("f4-f6", members.get(feature004).contains(feature006));
//        assertFalse("f4-f7", members.get(feature004).contains(feature007));
//        assertFalse("f4-f8", members.get(feature004).contains(feature008));
//        assertFalse("f4-f9", members.get(feature004).contains(feature009));
//        assertFalse("f4-f10", members.get(feature004).contains(feature010));
//        assertFalse("f4-f11", members.get(feature004).contains(feature011));

        assertTrue("f5-fX", members.get(feature005) == null);
//        assertFalse("f5-f1", members.get(feature005).contains(feature001));
//        assertFalse("f5-f2", members.get(feature005).contains(feature002));
//        assertFalse("f5-f3", members.get(feature005).contains(feature003));
//        assertFalse("f5-f4", members.get(feature005).contains(feature004));
//        assertFalse("f5-f5", members.get(feature005).contains(feature005));
//        assertFalse("f5-f6", members.get(feature005).contains(feature006));
//        assertFalse("f5-f7", members.get(feature005).contains(feature007));
//        assertFalse("f5-f8", members.get(feature005).contains(feature008));
//        assertFalse("f5-f9", members.get(feature005).contains(feature009));
//        assertFalse("f5-f10", members.get(feature005).contains(feature010));
//        assertFalse("f5-f11", members.get(feature005).contains(feature011));

        assertFalse("f6-f1", members.get(feature006).contains(feature001));
        assertFalse("f6-f2", members.get(feature006).contains(feature002));
        assertFalse("f6-f3", members.get(feature006).contains(feature003));
        assertTrue("f6-f4", members.get(feature006).contains(feature004));
        assertTrue("f6-f5", members.get(feature006).contains(feature005));
        assertFalse("f6-f6", members.get(feature006).contains(feature006));
        assertFalse("f6-f7", members.get(feature006).contains(feature007));
        assertTrue("f6-f8", members.get(feature006).contains(feature008));
        assertTrue("f6-f9", members.get(feature006).contains(feature009));
        assertFalse("f6-f10", members.get(feature006).contains(feature010));
        assertFalse("f6-f11", members.get(feature006).contains(feature011));

        assertTrue("f7-fX", members.get(feature007) == null);
//        assertFalse("f7-f1", members.get(feature007).contains(feature001));
//        assertFalse("f7-f2", members.get(feature007).contains(feature002));
//        assertFalse("f7-f3", members.get(feature007).contains(feature003));
//        assertFalse("f7-f4", members.get(feature007).contains(feature004));
//        assertFalse("f7-f5", members.get(feature007).contains(feature005));
//        assertFalse("f7-f6", members.get(feature007).contains(feature006));
//        assertFalse("f7-f7", members.get(feature007).contains(feature007));
//        assertFalse("f7-f8", members.get(feature007).contains(feature008));
//        assertFalse("f7-f9", members.get(feature007).contains(feature009));
//        assertFalse("f7-f10", members.get(feature007).contains(feature010));
//        assertFalse("f7-f11", members.get(feature007).contains(feature011));

        assertTrue("f8-fX", members.get(feature008) == null);
//        assertFalse("f8-f1", members.get(feature008).contains(feature001));
//        assertFalse("f8-f2", members.get(feature008).contains(feature002));
//        assertFalse("f8-f3", members.get(feature008).contains(feature003));
//        assertFalse("f8-f4", members.get(feature008).contains(feature004));
//        assertFalse("f8-f5", members.get(feature008).contains(feature005));
//        assertFalse("f8-f6", members.get(feature008).contains(feature006));
//        assertFalse("f8-f7", members.get(feature008).contains(feature007));
//        assertFalse("f8-f8", members.get(feature008).contains(feature008));
//        assertFalse("f8-f9", members.get(feature008).contains(feature009));
//        assertFalse("f8-f10", members.get(feature008).contains(feature010));
//        assertFalse("f8-f11", members.get(feature008).contains(feature011));

        assertTrue("f9-fX", members.get(feature009) == null);
//        assertFalse("f9-f1", members.get(feature009).contains(feature001));
//        assertFalse("f9-f2", members.get(feature009).contains(feature002));
//        assertFalse("f9-f3", members.get(feature009).contains(feature003));
//        assertFalse("f9-f4", members.get(feature009).contains(feature004));
//        assertFalse("f9-f5", members.get(feature009).contains(feature005));
//        assertFalse("f9-f6", members.get(feature009).contains(feature006));
//        assertFalse("f9-f7", members.get(feature009).contains(feature007));
//        assertFalse("f9-f8", members.get(feature009).contains(feature008));
//        assertFalse("f9-f9", members.get(feature009).contains(feature009));
//        assertFalse("f9-f10", members.get(feature009).contains(feature010));
//        assertFalse("f9-f11", members.get(feature009).contains(feature011));

        assertFalse("f10-f1", members.get(feature010).contains(feature001));
        assertFalse("f10-f2", members.get(feature010).contains(feature002));
        assertFalse("f10-f3", members.get(feature010).contains(feature003));
        assertFalse("f10-f4", members.get(feature010).contains(feature004));
        assertFalse("f10-f5", members.get(feature010).contains(feature005));
        assertFalse("f10-f6", members.get(feature010).contains(feature006));
        assertFalse("f10-f7", members.get(feature010).contains(feature007));
        assertTrue("f10-f8", members.get(feature010).contains(feature008));
        assertTrue("f10-f9", members.get(feature010).contains(feature009));
        assertFalse("f10-f10", members.get(feature010).contains(feature010));
        assertFalse("f10-f11", members.get(feature010).contains(feature011));

        assertTrue("f11-fX", members.get(feature011) == null);
//        assertFalse("f11-f1", members.get(feature011).contains(feature001));
//        assertFalse("f11-f2", members.get(feature011).contains(feature002));
//        assertFalse("f11-f3", members.get(feature011).contains(feature003));
//        assertFalse("f11-f4", members.get(feature011).contains(feature004));
//        assertFalse("f11-f5", members.get(feature011).contains(feature005));
//        assertFalse("f11-f6", members.get(feature011).contains(feature006));
//        assertFalse("f11-f7", members.get(feature011).contains(feature007));
//        assertFalse("f11-f8", members.get(feature011).contains(feature008));
//        assertFalse("f11-f9", members.get(feature011).contains(feature009));
//        assertFalse("f11-f10", members.get(feature011).contains(feature010));
//        assertFalse("f11-f11", members.get(feature011).contains(feature011));
    }

//    @Test
    public void testGetIndividualsHavingProperPart() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0596)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0596)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7994 51.0595)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.8002 51.0600,13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8002 51.0600,13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8028 51.0569,13.8030 51.0574,13.8042 51.0571,13.8037 51.0566,13.8031 51.0569,13.8033 51.0572,13.8038 51.0570,13.8036 51.05689)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.8011 51.0596,13.8021 51.0589,13.8011 51.0586,13.7986 51.0593,13.7995 51.0598,13.8011 51.0596,13.8011 51.0596))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8011 51.0596,13.8021 51.0589,13.8011 51.0586,13.7986 51.0593,13.7995 51.0598,13.8011 51.0596,13.8011 51.0596))");

        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.7998 51.0591,13.8003 51.0594,13.8014 51.0591,13.7998 51.0591))");

        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8012 51.0582,13.8017 51.0578,13.8026 51.0582,13.8012 51.0582))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsHavingProperPart(feature001)
                        .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertTrue("f1-f4", result.contains(feature004));
        assertTrue("f1-f5", result.contains(feature005));
        assertTrue("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertTrue("f1-f8", result.contains(feature008));
        assertTrue("f1-f9", result.contains(feature009));
        assertFalse("f1-f10", result.contains(feature010));
        assertFalse("f1-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsHavingProperPart(feature002)
                        .collect(Collectors.toSet());

        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertTrue("f2-f4", result.contains(feature004));
        assertTrue("f2-f5", result.contains(feature005));
        assertTrue("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertTrue("f2-f8", result.contains(feature008));
        assertTrue("f2-f9", result.contains(feature009));
        assertFalse("f2-f10", result.contains(feature010));
        assertFalse("f2-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsHavingProperPart(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertTrue("f3-f8", result.contains(feature008));
        assertTrue("f3-f9", result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertFalse("f3-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsHavingProperPart(feature004)
                        .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertFalse("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsHavingProperPart(feature005)
                        .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertFalse("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertFalse("f5-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsHavingProperPart(feature006)
                        .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertTrue("f6-f4",  result.contains(feature004));
        assertTrue("f6-f5",  result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertTrue("f6-f8",  result.contains(feature008));
        assertTrue("f6-f9",  result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertFalse("f6-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsHavingProperPart(feature007)
                        .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertFalse("f7-f10", result.contains(feature010));
        assertFalse("f7-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsHavingProperPart(feature008)
                        .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertFalse("f8-f8", result.contains(feature008));
        assertFalse("f8-f9", result.contains(feature009));
        assertFalse("f8-f10", result.contains(feature010));
        assertFalse("f8-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsHavingProperPart(feature009)
                        .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertFalse("f9-f8", result.contains(feature008));
        assertFalse("f9-f9", result.contains(feature009));
        assertFalse("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsHavingProperPart(feature010)
                        .collect(Collectors.toSet());

        assertFalse("f10-f1", result.contains(feature001));
        assertFalse("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertFalse("f10-f6", result.contains(feature006));
        assertFalse("f10-f7", result.contains(feature007));
        assertTrue("f10-f8", result.contains(feature008));
        assertTrue("f10-f9", result.contains(feature009));
        assertFalse("f10-f10", result.contains(feature010));
        assertFalse("f10-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsHavingProperPart(feature011)
                        .collect(Collectors.toSet());

        assertFalse("f11-f1", result.contains(feature001));
        assertFalse("f11-f2", result.contains(feature002));
        assertFalse("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertFalse("f11-f5", result.contains(feature005));
        assertFalse("f11-f6", result.contains(feature006));
        assertFalse("f11-f7", result.contains(feature007));
        assertFalse("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertFalse("f11-f10", result.contains(feature010));
        assertFalse("f11-f11", result.contains(feature011));
    }

    @Test
    public void testGetHasProperPartMembers() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0596)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0596)");

        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7994 51.0595)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.8002 51.0600,13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8002 51.0600,13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8011 51.0596,13.8021 51.0589,13.8011 51.0586)");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8028 51.0569,13.8030 51.0574,13.8042 51.0571,13.8037 51.0566,13.8031 51.0569,13.8033 51.0572,13.8038 51.0570,13.8036 51.05689)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.8011 51.0596,13.8021 51.0589,13.8011 51.0586,13.7986 51.0593,13.7995 51.0598,13.8011 51.0596,13.8011 51.0596))");

        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8011 51.0596,13.8021 51.0589,13.8011 51.0586,13.7986 51.0593,13.7995 51.0598,13.8011 51.0596,13.8011 51.0596))");

        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.7998 51.0591,13.8003 51.0594,13.8014 51.0591,13.7998 51.0591))");

        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8012 51.0582,13.8017 51.0578,13.8026 51.0582,13.8012 51.0582))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Map<OWLIndividual, SortedSet<OWLIndividual>> members =
                reasoner.getHasProperPartMembers();

        assertNull("f1-fX", members.get(feature001));
//        assertFalse("f1-f1", members.get(feature001).contains(feature001));
//        assertFalse("f1-f2", members.get(feature001).contains(feature002));
//        assertFalse("f1-f3", members.get(feature001).contains(feature003);
//        assertFalse("f1-f4", members.get(feature001).contains(feature004));
//        assertFalse("f1-f5", members.get(feature001).contains(feature005));
//        assertFalse("f1-f6", members.get(feature001).contains(feature006));
//        assertFalse("f1-f7", members.get(feature001).contains(feature007));
//        assertFalse("f1-f8", members.get(feature001).contains(feature008));
//        assertFalse("f1-f9", members.get(feature001).contains(feature009));
//        assertFalse("f1-f10", members.get(feature001).contains(feature010));
//        assertFalse("f1-f11", members.get(feature001).contains(feature011));

        assertNull("f2-fX", members.get(feature002));
//        assertFalse("f2-f1", members.get(feature002).contains(feature001));
//        assertFalse("f2-f2", members.get(feature002).contains(feature002));
//        assertFalse("f2-f3", members.get(feature002).contains(feature003);
//        assertFalse("f2-f4", members.get(feature002).contains(feature004));
//        assertFalse("f2-f5", members.get(feature002).contains(feature005));
//        assertFalse("f2-f6", members.get(feature002).contains(feature006));
//        assertFalse("f2-f7", members.get(feature002).contains(feature007));
//        assertFalse("f2-f8", members.get(feature002).contains(feature008));
//        assertFalse("f2-f9", members.get(feature002).contains(feature009));
//        assertFalse("f2-f10", members.get(feature002).contains(feature010));
//        assertFalse("f2-f11", members.get(feature002).contains(feature011));

        assertNull("f3-fX", members.get(feature003));
//        assertFalse("f3-f1", members.get(feature003).contains(feature001));
//        assertFalse("f3-f2", members.get(feature003).contains(feature002));
//        assertFalse("f3-f3", members.get(feature003).contains(feature003);
//        assertFalse("f3-f4", members.get(feature003).contains(feature004));
//        assertFalse("f3-f5", members.get(feature003).contains(feature005));
//        assertFalse("f3-f6", members.get(feature003).contains(feature006));
//        assertFalse("f3-f7", members.get(feature003).contains(feature007));
//        assertFalse("f3-f8", members.get(feature003).contains(feature008));
//        assertFalse("f3-f9", members.get(feature003).contains(feature009));
//        assertFalse("f3-f10", members.get(feature003).contains(feature010));
//        assertFalse("f3-f11", members.get(feature003).contains(feature011));

        assertTrue("f4-f1", members.get(feature004).contains(feature001));
        assertTrue("f4-f2", members.get(feature004).contains(feature002));
        assertFalse("f4-f3", members.get(feature004).contains(feature003));
        assertFalse("f4-f4", members.get(feature004).contains(feature004));
        assertFalse("f4-f5", members.get(feature004).contains(feature005));
        assertTrue("f4-f6",  members.get(feature004).contains(feature006));
        assertFalse("f4-f7", members.get(feature004).contains(feature007));
        assertFalse("f4-f8", members.get(feature004).contains(feature008));
        assertFalse("f4-f9", members.get(feature004).contains(feature009));
        assertFalse("f4-f10", members.get(feature004).contains(feature010));
        assertFalse("f4-f11", members.get(feature004).contains(feature011));

        assertTrue("f5-f1", members.get(feature005).contains(feature001));
        assertTrue("f5-f2", members.get(feature005).contains(feature002));
        assertFalse("f5-f3", members.get(feature005).contains(feature003));
        assertFalse("f5-f4", members.get(feature005).contains(feature004));
        assertFalse("f5-f5", members.get(feature005).contains(feature005));
        assertTrue("f5-f6",  members.get(feature005).contains(feature006));
        assertFalse("f5-f7", members.get(feature005).contains(feature007));
        assertFalse("f5-f8", members.get(feature005).contains(feature008));
        assertFalse("f5-f9", members.get(feature005).contains(feature009));
        assertFalse("f5-f10", members.get(feature005).contains(feature010));
        assertFalse("f5-f11", members.get(feature005).contains(feature011));

        assertTrue("f6-f1", members.get(feature006).contains(feature001));
        assertTrue("f6-f2", members.get(feature006).contains(feature002));
        assertFalse("f6-f3", members.get(feature006).contains(feature003));
        assertFalse("f6-f4", members.get(feature006).contains(feature004));
        assertFalse("f6-f5", members.get(feature006).contains(feature005));
        assertFalse("f6-f6", members.get(feature006).contains(feature006));
        assertFalse("f6-f7", members.get(feature006).contains(feature007));
        assertFalse("f6-f8", members.get(feature006).contains(feature008));
        assertFalse("f6-f9", members.get(feature006).contains(feature009));
        assertFalse("f6-f10", members.get(feature006).contains(feature010));
        assertFalse("f6-f11", members.get(feature006).contains(feature011));

        assertNull("f7-fX", members.get(feature007));
//        assertFalse("f7-f1", members.get(feature007).contains(feature001));
//        assertFalse("f7-f2", members.get(feature007).contains(feature002));
//        assertFalse("f7-f3", members.get(feature007).contains(feature003);
//        assertFalse("f7-f4", members.get(feature007).contains(feature004));
//        assertFalse("f7-f5", members.get(feature007).contains(feature005));
//        assertFalse("f7-f6", members.get(feature007).contains(feature006));
//        assertFalse("f7-f7", members.get(feature007).contains(feature007));
//        assertFalse("f7-f8", members.get(feature007).contains(feature008));
//        assertFalse("f7-f9", members.get(feature007).contains(feature009));
//        assertFalse("f7-f10", members.get(feature007).contains(feature010));
//        assertFalse("f7-f11", members.get(feature007).contains(feature011));

        assertTrue("f8-f1", members.get(feature008).contains(feature001));
        assertTrue("f8-f2", members.get(feature008).contains(feature002));
        assertTrue("f8-f3", members.get(feature008).contains(feature003));
        assertFalse("f8-f4", members.get(feature008).contains(feature004));
        assertFalse("f8-f5", members.get(feature008).contains(feature005));
        assertTrue("f8-f6",  members.get(feature008).contains(feature006));
        assertFalse("f8-f7", members.get(feature008).contains(feature007));
        assertFalse("f8-f8", members.get(feature008).contains(feature008));
        assertFalse("f8-f9", members.get(feature008).contains(feature009));
        assertTrue("f8-f10", members.get(feature008).contains(feature010));
        assertFalse("f8-f11", members.get(feature008).contains(feature011));

        assertTrue("f9-f1", members.get(feature009).contains(feature001));
        assertTrue("f9-f2", members.get(feature009).contains(feature002));
        assertTrue("f9-f3", members.get(feature009).contains(feature003));
        assertFalse("f9-f4", members.get(feature009).contains(feature004));
        assertFalse("f9-f5", members.get(feature009).contains(feature005));
        assertTrue("f9-f6",  members.get(feature009).contains(feature006));
        assertFalse("f9-f7", members.get(feature009).contains(feature007));
        assertFalse("f9-f8", members.get(feature009).contains(feature008));
        assertFalse("f9-f9", members.get(feature009).contains(feature009));
        assertTrue("f9-f10", members.get(feature009).contains(feature010));
        assertFalse("f9-f11", members.get(feature009).contains(feature011));

        assertNull("f10-fX", members.get(feature010));
//        assertFalse("f10-f1", members.get(feature010).contains(feature001));
//        assertFalse("f10-f2", members.get(feature010).contains(feature002));
//        assertFalse("f10-f3", members.get(feature010).contains(feature003);
//        assertFalse("f10-f4", members.get(feature010).contains(feature004));
//        assertFalse("f10-f5", members.get(feature010).contains(feature005));
//        assertFalse("f10-f6", members.get(feature010).contains(feature006));
//        assertFalse("f10-f7", members.get(feature010).contains(feature007));
//        assertFalse("f10-f8", members.get(feature010).contains(feature008));
//        assertFalse("f10-f9", members.get(feature010).contains(feature009));
//        assertFalse("f10-f10", members.get(feature010).contains(feature010));
//        assertFalse("f10-f11", members.get(feature010).contains(feature011));

        assertNull("f11-fX", members.get(feature011));
//        assertFalse("f11-f1", members.get(feature011).contains(feature001));
//        assertFalse("f11-f2", members.get(feature011).contains(feature002));
//        assertFalse("f11-f3", members.get(feature011).contains(feature003);
//        assertFalse("f11-f4", members.get(feature011).contains(feature004));
//        assertFalse("f11-f5", members.get(feature011).contains(feature005));
//        assertFalse("f11-f6", members.get(feature011).contains(feature006));
//        assertFalse("f11-f7", members.get(feature011).contains(feature007));
//        assertFalse("f11-f8", members.get(feature011).contains(feature008));
//        assertFalse("f11-f9", members.get(feature011).contains(feature009));
//        assertFalse("f11-f10", members.get(feature011).contains(feature010));
//        assertFalse("f11-f11", members.get(feature011).contains(feature011));
    }

    @Test
    public void testPartiallyOverlaps() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        // -- on area boundary
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8022 51.0591)");

        // -- inside area
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002,"POINT(13.8022 51.0586)");

        // -- same as feature002
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003,"POINT(13.8022 51.0586)");

        // -- outside
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.8005 51.0593)");


        // line strings
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8022 51.0591,13.8029 51.0588,13.8038 51.0589,13.8037 51.0581,13.8043 51.05837)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8022 51.0591,13.8029 51.0588,13.8038 51.0589,13.8037 51.0581,13.8043 51.05837)");

        // -- does not overlap with above line string
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8038 51.0589,13.8037 51.0581,13.8043 51.05837)");

        // -- does overlap with feature005
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8038 51.0589,13.8037 51.0581,13.8043 51.05837,13.8043 51.0577)");

        // off
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "LINESTRING(13.8038 51.0565,13.8042 51.0571,13.8050 51.0563,13.8041 51.0564,13.8042 51.0567)");

        // crosses feature011/012
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "LINESTRING(13.8007 51.0587,13.8031 51.0577)");

        // areas
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8013 51.0589,13.8022 51.0591," +
                        "13.8029 51.0588,13.8030 51.0581,13.8017 51.0579," +
                        "13.8009 51.0583,13.8013 51.0589))");

        // -- same as feature011
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.8013 51.0589,13.8022 51.0591," +
                        "13.8029 51.0588,13.8030 51.0581,13.8017 51.0579," +
                        "13.8009 51.0583,13.8013 51.0589))");

        // -- overlaps with feature011/12
        OWLIndividual feature013 = i("feature013");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.8009 51.0578,13.8016 51.0582," +
                        "13.8026 51.0576,13.8019 51.0573,13.8009 51.0578))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7973 51.0591,13.7972 51.0586,13.7983 51.0587,13.7973 51.0591))");


        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertFalse("f1-f1", reasoner.partiallyOverlapsWith(feature001, feature001));
        assertFalse("f1-f2", reasoner.partiallyOverlapsWith(feature001, feature002));
        assertFalse("f1-f3", reasoner.partiallyOverlapsWith(feature001, feature003));
        assertFalse("f1-f4", reasoner.partiallyOverlapsWith(feature001, feature004));
        assertFalse("f1-f5", reasoner.partiallyOverlapsWith(feature001, feature005));
        assertFalse("f1-f6", reasoner.partiallyOverlapsWith(feature001, feature006));
        assertFalse("f1-f7", reasoner.partiallyOverlapsWith(feature001, feature007));
        assertFalse("f1-f8", reasoner.partiallyOverlapsWith(feature001, feature008));
        assertFalse("f1-f9", reasoner.partiallyOverlapsWith(feature001, feature009));
        assertFalse("f1-f10", reasoner.partiallyOverlapsWith(feature001, feature010));
        assertFalse("f1-f11", reasoner.partiallyOverlapsWith(feature001, feature011));
        assertFalse("f1-f12", reasoner.partiallyOverlapsWith(feature001, feature012));
        assertFalse("f1-f13", reasoner.partiallyOverlapsWith(feature001, feature013));
        assertFalse("f1-f14", reasoner.partiallyOverlapsWith(feature001, feature014));

        assertFalse("f2-f1", reasoner.partiallyOverlapsWith(feature002, feature001));
        assertFalse("f2-f2", reasoner.partiallyOverlapsWith(feature002, feature002));
        assertFalse("f2-f3", reasoner.partiallyOverlapsWith(feature002, feature003));
        assertFalse("f2-f4", reasoner.partiallyOverlapsWith(feature002, feature004));
        assertFalse("f2-f5", reasoner.partiallyOverlapsWith(feature002, feature005));
        assertFalse("f2-f6", reasoner.partiallyOverlapsWith(feature002, feature006));
        assertFalse("f2-f7", reasoner.partiallyOverlapsWith(feature002, feature007));
        assertFalse("f2-f8", reasoner.partiallyOverlapsWith(feature002, feature008));
        assertFalse("f2-f9", reasoner.partiallyOverlapsWith(feature002, feature009));
        assertFalse("f2-f10", reasoner.partiallyOverlapsWith(feature002, feature010));
        assertFalse("f2-f11", reasoner.partiallyOverlapsWith(feature002, feature011));
        assertFalse("f2-f12", reasoner.partiallyOverlapsWith(feature002, feature012));
        assertFalse("f2-f13", reasoner.partiallyOverlapsWith(feature002, feature013));
        assertFalse("f2-f14", reasoner.partiallyOverlapsWith(feature002, feature014));

        assertFalse("f3-f1", reasoner.partiallyOverlapsWith(feature003, feature001));
        assertFalse("f3-f2", reasoner.partiallyOverlapsWith(feature003, feature002));
        assertFalse("f3-f3", reasoner.partiallyOverlapsWith(feature003, feature003));
        assertFalse("f3-f4", reasoner.partiallyOverlapsWith(feature003, feature004));
        assertFalse("f3-f5", reasoner.partiallyOverlapsWith(feature003, feature005));
        assertFalse("f3-f6", reasoner.partiallyOverlapsWith(feature003, feature006));
        assertFalse("f3-f7", reasoner.partiallyOverlapsWith(feature003, feature007));
        assertFalse("f3-f8", reasoner.partiallyOverlapsWith(feature003, feature008));
        assertFalse("f3-f9", reasoner.partiallyOverlapsWith(feature003, feature009));
        assertFalse("f3-f10", reasoner.partiallyOverlapsWith(feature003, feature010));
        assertFalse("f3-f11", reasoner.partiallyOverlapsWith(feature003, feature011));
        assertFalse("f3-f12", reasoner.partiallyOverlapsWith(feature003, feature012));
        assertFalse("f3-f13", reasoner.partiallyOverlapsWith(feature003, feature013));
        assertFalse("f3-f14", reasoner.partiallyOverlapsWith(feature003, feature014));

        assertFalse("f4-f1", reasoner.partiallyOverlapsWith(feature004, feature001));
        assertFalse("f4-f2", reasoner.partiallyOverlapsWith(feature004, feature002));
        assertFalse("f4-f3", reasoner.partiallyOverlapsWith(feature004, feature003));
        assertFalse("f4-f4", reasoner.partiallyOverlapsWith(feature004, feature004));
        assertFalse("f4-f5", reasoner.partiallyOverlapsWith(feature004, feature005));
        assertFalse("f4-f6", reasoner.partiallyOverlapsWith(feature004, feature006));
        assertFalse("f4-f7", reasoner.partiallyOverlapsWith(feature004, feature007));
        assertFalse("f4-f8", reasoner.partiallyOverlapsWith(feature004, feature008));
        assertFalse("f4-f9", reasoner.partiallyOverlapsWith(feature004, feature009));
        assertFalse("f4-f10", reasoner.partiallyOverlapsWith(feature004, feature010));
        assertFalse("f4-f11", reasoner.partiallyOverlapsWith(feature004, feature011));
        assertFalse("f4-f12", reasoner.partiallyOverlapsWith(feature004, feature012));
        assertFalse("f4-f13", reasoner.partiallyOverlapsWith(feature004, feature013));
        assertFalse("f4-f14", reasoner.partiallyOverlapsWith(feature004, feature014));

        assertFalse("f5-f1", reasoner.partiallyOverlapsWith(feature005, feature001));
        assertFalse("f5-f2", reasoner.partiallyOverlapsWith(feature005, feature002));
        assertFalse("f5-f3", reasoner.partiallyOverlapsWith(feature005, feature003));
        assertFalse("f5-f4", reasoner.partiallyOverlapsWith(feature005, feature004));
        assertFalse("f5-f5", reasoner.partiallyOverlapsWith(feature005, feature005));
        assertFalse("f5-f6", reasoner.partiallyOverlapsWith(feature005, feature006));
        assertFalse("f5-f7", reasoner.partiallyOverlapsWith(feature005, feature007));
        assertTrue("f5-f8", reasoner.partiallyOverlapsWith(feature005, feature008));
        assertFalse("f5-f9", reasoner.partiallyOverlapsWith(feature005, feature009));
        assertFalse("f5-f10", reasoner.partiallyOverlapsWith(feature010, feature010));
        assertTrue("f5-f11", reasoner.partiallyOverlapsWith(feature005, feature011));
        assertTrue("f5-f12", reasoner.partiallyOverlapsWith(feature005, feature012));
        assertFalse("f5-f13", reasoner.partiallyOverlapsWith(feature005, feature013));
        assertFalse("f5-f14", reasoner.partiallyOverlapsWith(feature005, feature014));

        assertFalse("f6-f1", reasoner.partiallyOverlapsWith(feature006, feature001));
        assertFalse("f6-f2", reasoner.partiallyOverlapsWith(feature006, feature002));
        assertFalse("f6-f3", reasoner.partiallyOverlapsWith(feature006, feature003));
        assertFalse("f6-f4", reasoner.partiallyOverlapsWith(feature006, feature004));
        assertFalse("f6-f5", reasoner.partiallyOverlapsWith(feature006, feature005));
        assertFalse("f6-f6", reasoner.partiallyOverlapsWith(feature006, feature006));
        assertFalse("f6-f7", reasoner.partiallyOverlapsWith(feature006, feature007));
        assertTrue("f6-f8", reasoner.partiallyOverlapsWith(feature006, feature008));
        assertFalse("f6-f9", reasoner.partiallyOverlapsWith(feature006, feature009));
        assertFalse("f6-f10", reasoner.partiallyOverlapsWith(feature006, feature010));
        assertTrue("f6-f11", reasoner.partiallyOverlapsWith(feature006, feature011));
        assertTrue("f6-f12", reasoner.partiallyOverlapsWith(feature006, feature012));
        assertFalse("f6-f13", reasoner.partiallyOverlapsWith(feature006, feature013));
        assertFalse("f6-f14", reasoner.partiallyOverlapsWith(feature006, feature014));

        assertFalse("f7-f1", reasoner.partiallyOverlapsWith(feature007, feature001));
        assertFalse("f7-f2", reasoner.partiallyOverlapsWith(feature007, feature002));
        assertFalse("f7-f3", reasoner.partiallyOverlapsWith(feature007, feature003));
        assertFalse("f7-f4", reasoner.partiallyOverlapsWith(feature007, feature004));
        assertFalse("f7-f5", reasoner.partiallyOverlapsWith(feature007, feature005));
        assertFalse("f7-f6", reasoner.partiallyOverlapsWith(feature007, feature006));
        assertFalse("f7-f7", reasoner.partiallyOverlapsWith(feature007, feature007));
        assertFalse("f7-f8", reasoner.partiallyOverlapsWith(feature007, feature008));
        assertFalse("f7-f9", reasoner.partiallyOverlapsWith(feature007, feature009));
        assertFalse("f7-f10", reasoner.partiallyOverlapsWith(feature007, feature010));
        assertFalse("f7-f11", reasoner.partiallyOverlapsWith(feature007, feature011));
        assertFalse("f7-f12", reasoner.partiallyOverlapsWith(feature007, feature012));
        assertFalse("f7-f13", reasoner.partiallyOverlapsWith(feature007, feature013));
        assertFalse("f7-f14", reasoner.partiallyOverlapsWith(feature007, feature014));

        assertFalse("f8-f1", reasoner.partiallyOverlapsWith(feature008, feature001));
        assertFalse("f8-f2", reasoner.partiallyOverlapsWith(feature008, feature002));
        assertFalse("f8-f3", reasoner.partiallyOverlapsWith(feature008, feature003));
        assertFalse("f8-f4", reasoner.partiallyOverlapsWith(feature008, feature004));
        assertTrue("f8-f5", reasoner.partiallyOverlapsWith(feature008, feature005));
        assertTrue("f8-f6", reasoner.partiallyOverlapsWith(feature008, feature006));
        assertFalse("f8-f7", reasoner.partiallyOverlapsWith(feature008, feature007));
        assertFalse("f8-f8", reasoner.partiallyOverlapsWith(feature008, feature008));
        assertFalse("f8-f9", reasoner.partiallyOverlapsWith(feature008, feature009));
        assertFalse("f8-f10", reasoner.partiallyOverlapsWith(feature008, feature010));
        assertFalse("f8-f11", reasoner.partiallyOverlapsWith(feature008, feature011));
        assertFalse("f8-f12", reasoner.partiallyOverlapsWith(feature008, feature012));
        assertFalse("f8-f13", reasoner.partiallyOverlapsWith(feature008, feature013));
        assertFalse("f8-f14", reasoner.partiallyOverlapsWith(feature008, feature014));

        assertFalse("f9-f1", reasoner.partiallyOverlapsWith(feature009, feature001));
        assertFalse("f9-f2", reasoner.partiallyOverlapsWith(feature009, feature002));
        assertFalse("f9-f3", reasoner.partiallyOverlapsWith(feature009, feature003));
        assertFalse("f9-f4", reasoner.partiallyOverlapsWith(feature009, feature004));
        assertFalse("f9-f5", reasoner.partiallyOverlapsWith(feature009, feature005));
        assertFalse("f9-f6", reasoner.partiallyOverlapsWith(feature009, feature006));
        assertFalse("f9-f7", reasoner.partiallyOverlapsWith(feature009, feature007));
        assertFalse("f9-f8", reasoner.partiallyOverlapsWith(feature009, feature008));
        assertFalse("f9-f9", reasoner.partiallyOverlapsWith(feature009, feature009));
        assertFalse("f9-f10", reasoner.partiallyOverlapsWith(feature009, feature010));
        assertFalse("f9-f11", reasoner.partiallyOverlapsWith(feature009, feature011));
        assertFalse("f9-f12", reasoner.partiallyOverlapsWith(feature009, feature012));
        assertFalse("f9-f13", reasoner.partiallyOverlapsWith(feature009, feature013));
        assertFalse("f9-f14", reasoner.partiallyOverlapsWith(feature009, feature014));

        assertFalse("f10-f1", reasoner.partiallyOverlapsWith(feature010, feature001));
        assertFalse("f10-f2", reasoner.partiallyOverlapsWith(feature010, feature002));
        assertFalse("f10-f3", reasoner.partiallyOverlapsWith(feature010, feature003));
        assertFalse("f10-f4", reasoner.partiallyOverlapsWith(feature010, feature004));
        assertFalse("f10-f5", reasoner.partiallyOverlapsWith(feature010, feature005));
        assertFalse("f10-f6", reasoner.partiallyOverlapsWith(feature010, feature006));
        assertFalse("f10-f7", reasoner.partiallyOverlapsWith(feature010, feature007));
        assertFalse("f10-f8", reasoner.partiallyOverlapsWith(feature010, feature008));
        assertFalse("f10-f9", reasoner.partiallyOverlapsWith(feature010, feature009));
        assertFalse("f10-f10", reasoner.partiallyOverlapsWith(feature010, feature010));
        assertTrue("f10-f11", reasoner.partiallyOverlapsWith(feature010, feature011));
        assertTrue("f10-f12", reasoner.partiallyOverlapsWith(feature010, feature012));
        assertFalse("f10-f13", reasoner.partiallyOverlapsWith(feature010, feature013));
        assertFalse("f10-f14", reasoner.partiallyOverlapsWith(feature010, feature014));

        assertFalse("f11-f1", reasoner.partiallyOverlapsWith(feature011, feature001));
        assertFalse("f11-f2", reasoner.partiallyOverlapsWith(feature011, feature002));
        assertFalse("f11-f3", reasoner.partiallyOverlapsWith(feature011, feature003));
        assertFalse("f11-f4", reasoner.partiallyOverlapsWith(feature011, feature004));
        assertTrue("f11-f5", reasoner.partiallyOverlapsWith(feature011, feature005));
        assertTrue("f11-f6", reasoner.partiallyOverlapsWith(feature011, feature006));
        assertFalse("f11-f7", reasoner.partiallyOverlapsWith(feature011, feature007));
        assertFalse("f11-f8", reasoner.partiallyOverlapsWith(feature011, feature008));
        assertFalse("f11-f9", reasoner.partiallyOverlapsWith(feature011, feature009));
        assertTrue("f11-f10", reasoner.partiallyOverlapsWith(feature011, feature010));
        assertFalse("f11-f11", reasoner.partiallyOverlapsWith(feature011, feature011));
        assertFalse("f11-f12", reasoner.partiallyOverlapsWith(feature011, feature012));
        assertTrue("f11-f13", reasoner.partiallyOverlapsWith(feature011, feature013));
        assertFalse("f11-f14", reasoner.partiallyOverlapsWith(feature011, feature014));

        assertFalse("f12-f1", reasoner.partiallyOverlapsWith(feature012, feature001));
        assertFalse("f12-f2", reasoner.partiallyOverlapsWith(feature012, feature002));
        assertFalse("f12-f3", reasoner.partiallyOverlapsWith(feature012, feature003));
        assertFalse("f12-f4", reasoner.partiallyOverlapsWith(feature012, feature004));
        assertTrue("f12-f5", reasoner.partiallyOverlapsWith(feature012, feature005));
        assertTrue("f12-f6", reasoner.partiallyOverlapsWith(feature012, feature006));
        assertFalse("f12-f7", reasoner.partiallyOverlapsWith(feature012, feature007));
        assertFalse("f12-f8", reasoner.partiallyOverlapsWith(feature012, feature008));
        assertFalse("f12-f9", reasoner.partiallyOverlapsWith(feature012, feature009));
        assertTrue("f12-f10", reasoner.partiallyOverlapsWith(feature012, feature010));
        assertFalse("f12-f11", reasoner.partiallyOverlapsWith(feature012, feature011));
        assertFalse("f12-f12", reasoner.partiallyOverlapsWith(feature012, feature012));
        assertTrue("f12-f13", reasoner.partiallyOverlapsWith(feature012, feature013));
        assertFalse("f12-f14", reasoner.partiallyOverlapsWith(feature012, feature014));

        assertFalse("f13-f1", reasoner.partiallyOverlapsWith(feature013, feature001));
        assertFalse("f13-f2", reasoner.partiallyOverlapsWith(feature013, feature002));
        assertFalse("f13-f3", reasoner.partiallyOverlapsWith(feature013, feature003));
        assertFalse("f13-f4", reasoner.partiallyOverlapsWith(feature013, feature004));
        assertFalse("f13-f5", reasoner.partiallyOverlapsWith(feature013, feature005));
        assertFalse("f13-f6", reasoner.partiallyOverlapsWith(feature013, feature006));
        assertFalse("f13-f7", reasoner.partiallyOverlapsWith(feature013, feature007));
        assertFalse("f13-f8", reasoner.partiallyOverlapsWith(feature013, feature008));
        assertFalse("f13-f9", reasoner.partiallyOverlapsWith(feature013, feature009));
        assertFalse("f13-f10", reasoner.partiallyOverlapsWith(feature013, feature010));
        assertTrue("f13-f11", reasoner.partiallyOverlapsWith(feature013, feature011));
        assertTrue("f13-f12", reasoner.partiallyOverlapsWith(feature013, feature012));
        assertFalse("f13-f13", reasoner.partiallyOverlapsWith(feature013, feature013));
        assertFalse("f13-f14", reasoner.partiallyOverlapsWith(feature013, feature014));

        assertFalse("f14-f1", reasoner.partiallyOverlapsWith(feature014, feature001));
        assertFalse("f14-f2", reasoner.partiallyOverlapsWith(feature014, feature002));
        assertFalse("f14-f3", reasoner.partiallyOverlapsWith(feature014, feature003));
        assertFalse("f14-f4", reasoner.partiallyOverlapsWith(feature014, feature004));
        assertFalse("f14-f5", reasoner.partiallyOverlapsWith(feature014, feature005));
        assertFalse("f14-f6", reasoner.partiallyOverlapsWith(feature014, feature006));
        assertFalse("f14-f7", reasoner.partiallyOverlapsWith(feature014, feature007));
        assertFalse("f14-f8", reasoner.partiallyOverlapsWith(feature014, feature008));
        assertFalse("f14-f9", reasoner.partiallyOverlapsWith(feature014, feature009));
        assertFalse("f14-f10", reasoner.partiallyOverlapsWith(feature014, feature010));
        assertFalse("f14-f11", reasoner.partiallyOverlapsWith(feature014, feature011));
        assertFalse("f14-f12", reasoner.partiallyOverlapsWith(feature014, feature012));
        assertFalse("f14-f13", reasoner.partiallyOverlapsWith(feature014, feature013));
        assertFalse("f14-f14", reasoner.partiallyOverlapsWith(feature014, feature014));
    }

    @Test
    public void testGetIndividualsPartiallyOverlappingWith() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        // -- on area boundary
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8022 51.0591)");

        // -- inside area
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002,"POINT(13.8022 51.0586)");

        // -- same as feature002
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003,"POINT(13.8022 51.0586)");

        // -- outside
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.8005 51.0593)");


        // line strings
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8022 51.0591,13.8029 51.0588,13.8038 51.0589,13.8037 51.0581,13.8043 51.05837)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8022 51.0591,13.8029 51.0588,13.8038 51.0589,13.8037 51.0581,13.8043 51.05837)");

        // -- does not overlap with above line string
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8038 51.0589,13.8037 51.0581,13.8043 51.05837)");

        // -- does overlap with feature005
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8038 51.0589,13.8037 51.0581,13.8043 51.05837,13.8043 51.0577)");

        // off
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "LINESTRING(13.8038 51.0565,13.8042 51.0571,13.8050 51.0563,13.8041 51.0564,13.8042 51.0567)");

        // crosses feature011/012
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "LINESTRING(13.8007 51.0587,13.8031 51.0577)");

        // areas
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8013 51.0589,13.8022 51.0591," +
                        "13.8029 51.0588,13.8030 51.0581,13.8017 51.0579," +
                        "13.8009 51.0583,13.8013 51.0589))");

        // -- same as feature011
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.8013 51.0589,13.8022 51.0591," +
                        "13.8029 51.0588,13.8030 51.0581,13.8017 51.0579," +
                        "13.8009 51.0583,13.8013 51.0589))");

        // -- overlaps with feature011/12
        OWLIndividual feature013 = i("feature013");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.8009 51.0578,13.8016 51.0582," +
                        "13.8026 51.0576,13.8019 51.0573,13.8009 51.0578))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7973 51.0591,13.7972 51.0586,13.7983 51.0587,13.7973 51.0591))");

        // -- added for NTPP case to have a line being NTPP of another line
        OWLIndividual feature015 = i("feature015");
        OWLIndividual geom015 = i("geom015");
        kbHelper.addSpatialFeature(feature015, geom015,
                "LINESTRING(13.8036 51.0579,13.8029 51.0574," +
                        "13.8040 51.0577,13.8032 51.0572)");


        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature001)
                        .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));
        assertFalse("f1-f10", result.contains(feature010));
        assertFalse("f1-f11", result.contains(feature011));
        assertFalse("f1-f12", result.contains(feature012));
        assertFalse("f1-f13", result.contains(feature013));
        assertFalse("f1-f14", result.contains(feature014));
        assertFalse("f1-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature002)
                        .collect(Collectors.toSet());

        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));
        assertFalse("f2-f10", result.contains(feature010));
        assertFalse("f2-f11", result.contains(feature011));
        assertFalse("f2-f12", result.contains(feature012));
        assertFalse("f2-f13", result.contains(feature013));
        assertFalse("f2-f14", result.contains(feature014));
        assertFalse("f2-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertFalse("f3-f11", result.contains(feature011));
        assertFalse("f3-f12", result.contains(feature012));
        assertFalse("f3-f13", result.contains(feature013));
        assertFalse("f3-f14", result.contains(feature014));
        assertFalse("f3-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature004)
                        .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertFalse("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));
        assertFalse("f4-f12", result.contains(feature012));
        assertFalse("f4-f13", result.contains(feature013));
        assertFalse("f4-f14", result.contains(feature014));
        assertFalse("f4-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature005)
                        .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertFalse("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertTrue("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertTrue("f5-f11", result.contains(feature011));
        assertTrue("f5-f12", result.contains(feature012));
        assertFalse("f5-f13", result.contains(feature013));
        assertFalse("f5-f14", result.contains(feature014));
        assertFalse("f5-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature006)
                        .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertTrue("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertTrue("f6-f11", result.contains(feature011));
        assertTrue("f6-f12", result.contains(feature012));
        assertFalse("f6-f13", result.contains(feature013));
        assertFalse("f6-f14", result.contains(feature014));
        assertFalse("f6-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature007)
                        .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertFalse("f7-f10", result.contains(feature010));
        assertFalse("f7-f11", result.contains(feature011));
        assertFalse("f7-f12", result.contains(feature012));
        assertFalse("f7-f13", result.contains(feature013));
        assertFalse("f7-f14", result.contains(feature014));
        assertFalse("f7-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature008)
                        .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertTrue("f8-f5", result.contains(feature005));
        assertTrue("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertFalse("f8-f8", result.contains(feature008));
        assertFalse("f8-f9", result.contains(feature009));
        assertFalse("f8-f10", result.contains(feature010));
        assertFalse("f8-f11", result.contains(feature011));
        assertFalse("f8-f12", result.contains(feature012));
        assertFalse("f8-f13", result.contains(feature013));
        assertFalse("f8-f14", result.contains(feature014));
        assertFalse("f8-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature009)
                        .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertFalse("f9-f8", result.contains(feature008));
        assertFalse("f9-f9", result.contains(feature009));
        assertFalse("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));
        assertFalse("f9-f12", result.contains(feature012));
        assertFalse("f9-f13", result.contains(feature013));
        assertFalse("f9-f14", result.contains(feature014));
        assertFalse("f9-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature010)
                        .collect(Collectors.toSet());

        assertFalse("f10-f1", result.contains(feature001));
        assertFalse("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertFalse("f10-f6", result.contains(feature006));
        assertFalse("f10-f7", result.contains(feature007));
        assertFalse("f10-f8", result.contains(feature008));
        assertFalse("f10-f9", result.contains(feature009));
        assertFalse("f10-f10", result.contains(feature010));
        assertTrue("f10-f11", result.contains(feature011));
        assertTrue("f10-f12", result.contains(feature012));
        assertFalse("f10-f13", result.contains(feature013));
        assertFalse("f10-f14", result.contains(feature014));
        assertFalse("f10-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature011)
                        .collect(Collectors.toSet());

        assertFalse("f11-f1", result.contains(feature001));
        assertFalse("f11-f2", result.contains(feature002));
        assertFalse("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertTrue("f11-f5", result.contains(feature005));
        assertTrue("f11-f6", result.contains(feature006));
        assertFalse("f11-f7", result.contains(feature007));
        assertFalse("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertTrue("f11-f10", result.contains(feature010));
        assertFalse("f11-f11", result.contains(feature011));
        assertFalse("f11-f12", result.contains(feature012));
        assertTrue("f11-f13", result.contains(feature013));
        assertFalse("f11-f14", result.contains(feature014));
        assertFalse("f11-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature012)
                        .collect(Collectors.toSet());

        assertFalse("f12-f1", result.contains(feature001));
        assertFalse("f12-f2", result.contains(feature002));
        assertFalse("f12-f3", result.contains(feature003));
        assertFalse("f12-f4", result.contains(feature004));
        assertTrue("f12-f5", result.contains(feature005));
        assertTrue("f12-f6", result.contains(feature006));
        assertFalse("f12-f7", result.contains(feature007));
        assertFalse("f12-f8", result.contains(feature008));
        assertFalse("f12-f9", result.contains(feature009));
        assertTrue("f12-f10", result.contains(feature010));
        assertFalse("f12-f11", result.contains(feature011));
        assertFalse("f12-f12", result.contains(feature012));
        assertTrue("f12-f13", result.contains(feature013));
        assertFalse("f12-f14", result.contains(feature014));
        assertFalse("f12-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature013)
                        .collect(Collectors.toSet());

        assertFalse("f13-f1", result.contains(feature001));
        assertFalse("f13-f2", result.contains(feature002));
        assertFalse("f13-f3", result.contains(feature003));
        assertFalse("f13-f4", result.contains(feature004));
        assertFalse("f13-f5", result.contains(feature005));
        assertFalse("f13-f6", result.contains(feature006));
        assertFalse("f13-f7", result.contains(feature007));
        assertFalse("f13-f8", result.contains(feature008));
        assertFalse("f13-f9", result.contains(feature009));
        assertFalse("f13-f10", result.contains(feature010));
        assertTrue("f13-f11", result.contains(feature011));
        assertTrue("f13-f12", result.contains(feature012));
        assertFalse("f13-f13", result.contains(feature013));
        assertFalse("f13-f14", result.contains(feature014));
        assertFalse("f13-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature014)
                        .collect(Collectors.toSet());

        assertFalse("f14-f1", result.contains(feature001));
        assertFalse("f14-f2", result.contains(feature002));
        assertFalse("f14-f3", result.contains(feature003));
        assertFalse("f14-f4", result.contains(feature004));
        assertFalse("f14-f5", result.contains(feature005));
        assertFalse("f14-f6", result.contains(feature006));
        assertFalse("f14-f7", result.contains(feature007));
        assertFalse("f14-f8", result.contains(feature008));
        assertFalse("f14-f9", result.contains(feature009));
        assertFalse("f14-f10", result.contains(feature010));
        assertFalse("f14-f11", result.contains(feature011));
        assertFalse("f14-f12", result.contains(feature012));
        assertFalse("f14-f13", result.contains(feature013));
        assertFalse("f14-f14", result.contains(feature014));
        assertFalse("f14-f15", result.contains(feature015));

        result =
                reasoner.getIndividualsPartiallyOverlappingWith(feature015)
                        .collect(Collectors.toSet());

        assertFalse("f15-f1", result.contains(feature001));
        assertFalse("f15-f2", result.contains(feature002));
        assertFalse("f15-f3", result.contains(feature003));
        assertFalse("f15-f4", result.contains(feature004));
        assertFalse("f15-f5", result.contains(feature005));
        assertFalse("f15-f6", result.contains(feature006));
        assertFalse("f15-f7", result.contains(feature007));
        assertFalse("f15-f8", result.contains(feature008));
        assertFalse("f15-f9", result.contains(feature009));
        assertFalse("f15-f10", result.contains(feature010));
        assertFalse("f15-f11", result.contains(feature011));
        assertFalse("f15-f12", result.contains(feature012));
        assertFalse("f15-f13", result.contains(feature013));
        assertFalse("f15-f14", result.contains(feature014));
        assertFalse("f15-f15", result.contains(feature015));
    }

    @Test
    public void testGetPartiallyOverlapsWithMembers() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        // -- on area boundary
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8022 51.0591)");

        // -- inside area
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002,"POINT(13.8022 51.0586)");

        // -- same as feature002
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003,"POINT(13.8022 51.0586)");

        // -- outside
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.8005 51.0593)");


        // line strings
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8022 51.0591,13.8029 51.0588,13.8038 51.0589,13.8037 51.0581,13.8043 51.05837)");

        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8022 51.0591,13.8029 51.0588,13.8038 51.0589,13.8037 51.0581,13.8043 51.05837)");

        // -- does not overlap with above line string
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8038 51.0589,13.8037 51.0581,13.8043 51.05837)");

        // -- does overlap with feature005
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8038 51.0589,13.8037 51.0581,13.8043 51.05837,13.8043 51.0577)");

        // off
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "LINESTRING(13.8038 51.0565,13.8042 51.0571,13.8050 51.0563,13.8041 51.0564,13.8042 51.0567)");

        // crosses feature011/012
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "LINESTRING(13.8007 51.0587,13.8031 51.0577)");

        // areas
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8013 51.0589,13.8022 51.0591," +
                        "13.8029 51.0588,13.8030 51.0581,13.8017 51.0579," +
                        "13.8009 51.0583,13.8013 51.0589))");

        // -- same as feature011
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.8013 51.0589,13.8022 51.0591," +
                        "13.8029 51.0588,13.8030 51.0581,13.8017 51.0579," +
                        "13.8009 51.0583,13.8013 51.0589))");

        // -- overlaps with feature011/12
        OWLIndividual feature013 = i("feature013");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.8009 51.0578,13.8016 51.0582," +
                        "13.8026 51.0576,13.8019 51.0573,13.8009 51.0578))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7973 51.0591,13.7972 51.0586,13.7983 51.0587,13.7973 51.0591))");


        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Map<OWLIndividual, SortedSet<OWLIndividual>> members =
                reasoner.getPartiallyOverlapsWithMembers();

        assertNull("f1-fX", members.get(feature001));
//        assertFalse("f1-f1", members.get(feature001).contains(feature001));
//        assertFalse("f1-f2", members.get(feature001).contains(feature002));
//        assertFalse("f1-f3", members.get(feature001).contains(feature003));
//        assertFalse("f1-f4", members.get(feature001).contains(feature004));
//        assertFalse("f1-f5", members.get(feature001).contains(feature005));
//        assertFalse("f1-f6", members.get(feature001).contains(feature006));
//        assertFalse("f1-f7", members.get(feature001).contains(feature007));
//        assertFalse("f1-f8", members.get(feature001).contains(feature008));
//        assertFalse("f1-f9", members.get(feature001).contains(feature009));
//        assertFalse("f1-f10", members.get(feature001).contains(feature010));
//        assertFalse("f1-f11", members.get(feature001).contains(feature011));
//        assertFalse("f1-f12", members.get(feature001).contains(feature012));
//        assertFalse("f1-f13", members.get(feature001).contains(feature013));
//        assertFalse("f1-f14", members.get(feature001).contains(feature014));

        assertNull("f2-fX", members.get(feature002));
//        assertFalse("f2-f1", members.get(feature002).contains(feature001));
//        assertFalse("f2-f2", members.get(feature002).contains(feature002));
//        assertFalse("f2-f3", members.get(feature002).contains(feature003));
//        assertFalse("f2-f4", members.get(feature002).contains(feature004));
//        assertFalse("f2-f5", members.get(feature002).contains(feature005));
//        assertFalse("f2-f6", members.get(feature002).contains(feature006));
//        assertFalse("f2-f7", members.get(feature002).contains(feature007));
//        assertFalse("f2-f8", members.get(feature002).contains(feature008));
//        assertFalse("f2-f9", members.get(feature002).contains(feature009));
//        assertFalse("f2-f10", members.get(feature002).contains(feature010));
//        assertFalse("f2-f11", members.get(feature002).contains(feature011));
//        assertFalse("f2-f12", members.get(feature002).contains(feature012));
//        assertFalse("f2-f13", members.get(feature002).contains(feature013));
//        assertFalse("f2-f14", members.get(feature002).contains(feature014));

        assertNull("f3-fX", members.get(feature003));
//        assertFalse("f3-f1", members.get(feature003).contains(feature001));
//        assertFalse("f3-f2", members.get(feature003).contains(feature002));
//        assertFalse("f3-f3", members.get(feature003).contains(feature003));
//        assertFalse("f3-f4", members.get(feature003).contains(feature004));
//        assertFalse("f3-f5", members.get(feature003).contains(feature005));
//        assertFalse("f3-f6", members.get(feature003).contains(feature006));
//        assertFalse("f3-f7", members.get(feature003).contains(feature007));
//        assertFalse("f3-f8", members.get(feature003).contains(feature008));
//        assertFalse("f3-f9", members.get(feature003).contains(feature009));
//        assertFalse("f3-f10", members.get(feature003).contains(feature010));
//        assertFalse("f3-f11", members.get(feature003).contains(feature011));
//        assertFalse("f3-f12", members.get(feature003).contains(feature012));
//        assertFalse("f3-f13", members.get(feature003).contains(feature013));
//        assertFalse("f3-f14", members.get(feature003).contains(feature014));

        assertNull("f4-fX", members.get(feature004));
//        assertFalse("f4-f1", members.get(feature004).contains(feature001));
//        assertFalse("f4-f2", members.get(feature004).contains(feature002));
//        assertFalse("f4-f3", members.get(feature004).contains(feature003));
//        assertFalse("f4-f4", members.get(feature004).contains(feature004));
//        assertFalse("f4-f5", members.get(feature004).contains(feature005));
//        assertFalse("f4-f6", members.get(feature004).contains(feature006));
//        assertFalse("f4-f7", members.get(feature004).contains(feature007));
//        assertFalse("f4-f8", members.get(feature004).contains(feature008));
//        assertFalse("f4-f9", members.get(feature004).contains(feature009));
//        assertFalse("f4-f10", members.get(feature004).contains(feature010));
//        assertFalse("f4-f11", members.get(feature004).contains(feature011));
//        assertFalse("f4-f12", members.get(feature004).contains(feature012));
//        assertFalse("f4-f13", members.get(feature004).contains(feature013));
//        assertFalse("f4-f14", members.get(feature004).contains(feature014));

        assertFalse("f5-f1", members.get(feature005).contains(feature001));
        assertFalse("f5-f2", members.get(feature005).contains(feature002));
        assertFalse("f5-f3", members.get(feature005).contains(feature003));
        assertFalse("f5-f4", members.get(feature005).contains(feature004));
        assertFalse("f5-f5", members.get(feature005).contains(feature005));
        assertFalse("f5-f6", members.get(feature005).contains(feature006));
        assertFalse("f5-f7", members.get(feature005).contains(feature007));
        assertTrue("f5-f8", members.get(feature005).contains(feature008));
        assertFalse("f5-f9", members.get(feature005).contains(feature009));
        assertFalse("f5-f10", members.get(feature010).contains(feature010));
        assertTrue("f5-f11", members.get(feature005).contains(feature011));
        assertTrue("f5-f12", members.get(feature005).contains(feature012));
        assertFalse("f5-f13", members.get(feature005).contains(feature013));
        assertFalse("f5-f14", members.get(feature005).contains(feature014));

        assertFalse("f6-f1", members.get(feature006).contains(feature001));
        assertFalse("f6-f2", members.get(feature006).contains(feature002));
        assertFalse("f6-f3", members.get(feature006).contains(feature003));
        assertFalse("f6-f4", members.get(feature006).contains(feature004));
        assertFalse("f6-f5", members.get(feature006).contains(feature005));
        assertFalse("f6-f6", members.get(feature006).contains(feature006));
        assertFalse("f6-f7", members.get(feature006).contains(feature007));
        assertTrue("f6-f8", members.get(feature006).contains(feature008));
        assertFalse("f6-f9", members.get(feature006).contains(feature009));
        assertFalse("f6-f10", members.get(feature006).contains(feature010));
        assertTrue("f6-f11", members.get(feature006).contains(feature011));
        assertTrue("f6-f12", members.get(feature006).contains(feature012));
        assertFalse("f6-f13", members.get(feature006).contains(feature013));
        assertFalse("f6-f14", members.get(feature006).contains(feature014));

        assertNull("f7-fX", members.get(feature007));
//        assertFalse("f7-f1", members.get(feature007).contains(feature001));
//        assertFalse("f7-f2", members.get(feature007).contains(feature002));
//        assertFalse("f7-f3", members.get(feature007).contains(feature003));
//        assertFalse("f7-f4", members.get(feature007).contains(feature004));
//        assertFalse("f7-f5", members.get(feature007).contains(feature005));
//        assertFalse("f7-f6", members.get(feature007).contains(feature006));
//        assertFalse("f7-f7", members.get(feature007).contains(feature007));
//        assertFalse("f7-f8", members.get(feature007).contains(feature008));
//        assertFalse("f7-f9", members.get(feature007).contains(feature009));
//        assertFalse("f7-f10", members.get(feature007).contains(feature010));
//        assertFalse("f7-f11", members.get(feature007).contains(feature011));
//        assertFalse("f7-f12", members.get(feature007).contains(feature012));
//        assertFalse("f7-f13", members.get(feature007).contains(feature013));
//        assertFalse("f7-f14", members.get(feature007).contains(feature014));

        assertFalse("f8-f1", members.get(feature008).contains(feature001));
        assertFalse("f8-f2", members.get(feature008).contains(feature002));
        assertFalse("f8-f3", members.get(feature008).contains(feature003));
        assertFalse("f8-f4", members.get(feature008).contains(feature004));
        assertTrue("f8-f5", members.get(feature008).contains(feature005));
        assertTrue("f8-f6", members.get(feature008).contains(feature006));
        assertFalse("f8-f7", members.get(feature008).contains(feature007));
        assertFalse("f8-f8", members.get(feature008).contains(feature008));
        assertFalse("f8-f9", members.get(feature008).contains(feature009));
        assertFalse("f8-f10", members.get(feature008).contains(feature010));
        assertFalse("f8-f11", members.get(feature008).contains(feature011));
        assertFalse("f8-f12", members.get(feature008).contains(feature012));
        assertFalse("f8-f13", members.get(feature008).contains(feature013));
        assertFalse("f8-f14", members.get(feature008).contains(feature014));

        assertNull("f9-fX", members.get(feature009));
//        assertFalse("f9-f1", members.get(feature009).contains(feature001));
//        assertFalse("f9-f2", members.get(feature009).contains(feature002));
//        assertFalse("f9-f3", members.get(feature009).contains(feature003));
//        assertFalse("f9-f4", members.get(feature009).contains(feature004));
//        assertFalse("f9-f5", members.get(feature009).contains(feature005));
//        assertFalse("f9-f6", members.get(feature009).contains(feature006));
//        assertFalse("f9-f7", members.get(feature009).contains(feature007));
//        assertFalse("f9-f8", members.get(feature009).contains(feature008));
//        assertFalse("f9-f9", members.get(feature009).contains(feature009));
//        assertFalse("f9-f10", members.get(feature009).contains(feature010));
//        assertFalse("f9-f11", members.get(feature009).contains(feature011));
//        assertFalse("f9-f12", members.get(feature009).contains(feature012));
//        assertFalse("f9-f13", members.get(feature009).contains(feature013));
//        assertFalse("f9-f14", members.get(feature009).contains(feature014));

        assertFalse("f10-f1", members.get(feature010).contains(feature001));
        assertFalse("f10-f2", members.get(feature010).contains(feature002));
        assertFalse("f10-f3", members.get(feature010).contains(feature003));
        assertFalse("f10-f4", members.get(feature010).contains(feature004));
        assertFalse("f10-f5", members.get(feature010).contains(feature005));
        assertFalse("f10-f6", members.get(feature010).contains(feature006));
        assertFalse("f10-f7", members.get(feature010).contains(feature007));
        assertFalse("f10-f8", members.get(feature010).contains(feature008));
        assertFalse("f10-f9", members.get(feature010).contains(feature009));
        assertFalse("f10-f10", members.get(feature010).contains(feature010));
        assertTrue("f10-f11", members.get(feature010).contains(feature011));
        assertTrue("f10-f12", members.get(feature010).contains(feature012));
        assertFalse("f10-f13", members.get(feature010).contains(feature013));
        assertFalse("f10-f14", members.get(feature010).contains(feature014));

        assertFalse("f11-f1", members.get(feature011).contains(feature001));
        assertFalse("f11-f2", members.get(feature011).contains(feature002));
        assertFalse("f11-f3", members.get(feature011).contains(feature003));
        assertFalse("f11-f4", members.get(feature011).contains(feature004));
        assertTrue("f11-f5", members.get(feature011).contains(feature005));
        assertTrue("f11-f6", members.get(feature011).contains(feature006));
        assertFalse("f11-f7", members.get(feature011).contains(feature007));
        assertFalse("f11-f8", members.get(feature011).contains(feature008));
        assertFalse("f11-f9", members.get(feature011).contains(feature009));
        assertTrue("f11-f10", members.get(feature011).contains(feature010));
        assertFalse("f11-f11", members.get(feature011).contains(feature011));
        assertFalse("f11-f12", members.get(feature011).contains(feature012));
        assertTrue("f11-f13", members.get(feature011).contains(feature013));
        assertFalse("f11-f14", members.get(feature011).contains(feature014));

        assertFalse("f12-f1", members.get(feature012).contains(feature001));
        assertFalse("f12-f2", members.get(feature012).contains(feature002));
        assertFalse("f12-f3", members.get(feature012).contains(feature003));
        assertFalse("f12-f4", members.get(feature012).contains(feature004));
        assertTrue("f12-f5", members.get(feature012).contains(feature005));
        assertTrue("f12-f6", members.get(feature012).contains(feature006));
        assertFalse("f12-f7", members.get(feature012).contains(feature007));
        assertFalse("f12-f8", members.get(feature012).contains(feature008));
        assertFalse("f12-f9", members.get(feature012).contains(feature009));
        assertTrue("f12-f10", members.get(feature012).contains(feature010));
        assertFalse("f12-f11", members.get(feature012).contains(feature011));
        assertFalse("f12-f12", members.get(feature012).contains(feature012));
        assertTrue("f12-f13", members.get(feature012).contains(feature013));
        assertFalse("f12-f14", members.get(feature012).contains(feature014));

        assertFalse("f13-f1", members.get(feature013).contains(feature001));
        assertFalse("f13-f2", members.get(feature013).contains(feature002));
        assertFalse("f13-f3", members.get(feature013).contains(feature003));
        assertFalse("f13-f4", members.get(feature013).contains(feature004));
        assertFalse("f13-f5", members.get(feature013).contains(feature005));
        assertFalse("f13-f6", members.get(feature013).contains(feature006));
        assertFalse("f13-f7", members.get(feature013).contains(feature007));
        assertFalse("f13-f8", members.get(feature013).contains(feature008));
        assertFalse("f13-f9", members.get(feature013).contains(feature009));
        assertFalse("f13-f10", members.get(feature013).contains(feature010));
        assertTrue("f13-f11", members.get(feature013).contains(feature011));
        assertTrue("f13-f12", members.get(feature013).contains(feature012));
        assertFalse("f13-f13", members.get(feature013).contains(feature013));
        assertFalse("f13-f14", members.get(feature013).contains(feature014));

        assertNull("f14-fX", members.get(feature014));
//        assertFalse("f14-f1", members.get(feature014).contains(feature001));
//        assertFalse("f14-f2", members.get(feature014).contains(feature002));
//        assertFalse("f14-f3", members.get(feature014).contains(feature003));
//        assertFalse("f14-f4", members.get(feature014).contains(feature004));
//        assertFalse("f14-f5", members.get(feature014).contains(feature005));
//        assertFalse("f14-f6", members.get(feature014).contains(feature006));
//        assertFalse("f14-f7", members.get(feature014).contains(feature007));
//        assertFalse("f14-f8", members.get(feature014).contains(feature008));
//        assertFalse("f14-f9", members.get(feature014).contains(feature009));
//        assertFalse("f14-f10", members.get(feature014).contains(feature010));
//        assertFalse("f14-f11", members.get(feature014).contains(feature011));
//        assertFalse("f14-f12", members.get(feature014).contains(feature012));
//        assertFalse("f14-f13", members.get(feature014).contains(feature013));
//        assertFalse("f14-f14", members.get(feature014).contains(feature014));
    }

    @Test
    public void testIsTangentialProperPartOf() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0591)");

        // -- same as feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0591)");

        // -- TPP of feature010/11, but not of feature006/7
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8016 51.0589)");

        // -- off
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.7983 51.0591)");

        // -- inside feature010/11, but not TPP
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005, "POINT(13.8015 51.0583)");

        // line strings
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- same as feature006
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- inside but not TPP
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8009 51.0588,13.8003 51.0585,13.8009 51.0583)");

        // -- off
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "LINESTRING(13.8025 51.0577,13.8036 51.0579," +
                        "13.8029 51.0574,13.8040 51.0577," +
                        "13.8032 51.0572,13.8043 51.0574)");

        // areas
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- same as feature010
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- TPP
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.8000 51.0585,13.8008 51.0581," +
                        "13.8007 51.0578,13.7996 51.0583,13.8000 51.0585))");

        // -- NTPP, contains feature005
        OWLIndividual feature013 = i("feature13");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.8011 51.0585,13.8016 51.0586," +
                        "13.8018 51.0582,13.8013 51.0581,13.8011 51.0585))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7991 51.0602,13.7991 51.0597," +
                        "13.7999 51.0599,13.7991 51.0602))");

        // -- added for NTPP case to have a line being NTPP of another line
        OWLIndividual feature015 = i("feature015");
        OWLIndividual geom015 = i("geom015");
        kbHelper.addSpatialFeature(feature015, geom015,
                "LINESTRING(13.8036 51.0579,13.8029 51.0574," +
                        "13.8040 51.0577,13.8032 51.0572)");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertFalse("f1-f1", reasoner.isTangentialProperPartOf(feature001, feature001));
        assertFalse("f1-f2", reasoner.isTangentialProperPartOf(feature001, feature002));
        assertFalse("f1-f3", reasoner.isTangentialProperPartOf(feature001, feature003));
        assertFalse("f1-f4", reasoner.isTangentialProperPartOf(feature001, feature004));
        assertFalse("f1-f5", reasoner.isTangentialProperPartOf(feature001, feature005));
        assertTrue("f1-f6", reasoner.isTangentialProperPartOf(feature001, feature006));
        assertTrue("f1-f7", reasoner.isTangentialProperPartOf(feature001, feature007));
        assertFalse("f1-f8", reasoner.isTangentialProperPartOf(feature001, feature008));
        assertFalse("f1-f9", reasoner.isTangentialProperPartOf(feature001, feature009));
        assertTrue("f1-f10", reasoner.isTangentialProperPartOf(feature001, feature010));
        assertTrue("f1-f11", reasoner.isTangentialProperPartOf(feature001, feature011));
        assertFalse("f1-f12", reasoner.isTangentialProperPartOf(feature001, feature012));
        assertFalse("f1-f13", reasoner.isTangentialProperPartOf(feature001, feature013));
        assertFalse("f1-f14", reasoner.isTangentialProperPartOf(feature001, feature014));
        assertFalse("f1-f15", reasoner.isTangentialProperPartOf(feature001, feature015));


        assertFalse("f2-f1", reasoner.isTangentialProperPartOf(feature002, feature001));
        assertFalse("f2-f2", reasoner.isTangentialProperPartOf(feature002, feature002));
        assertFalse("f2-f3", reasoner.isTangentialProperPartOf(feature002, feature003));
        assertFalse("f2-f4", reasoner.isTangentialProperPartOf(feature002, feature004));
        assertFalse("f2-f5", reasoner.isTangentialProperPartOf(feature002, feature005));
        assertTrue("f2-f6", reasoner.isTangentialProperPartOf(feature002, feature006));
        assertTrue("f2-f7", reasoner.isTangentialProperPartOf(feature002, feature007));
        assertFalse("f2-f8", reasoner.isTangentialProperPartOf(feature002, feature008));
        assertFalse("f2-f9", reasoner.isTangentialProperPartOf(feature002, feature009));
        assertTrue("f2-f10", reasoner.isTangentialProperPartOf(feature002, feature010));
        assertTrue("f2-f11", reasoner.isTangentialProperPartOf(feature002, feature011));
        assertFalse("f2-f12", reasoner.isTangentialProperPartOf(feature002, feature012));
        assertFalse("f2-f13", reasoner.isTangentialProperPartOf(feature002, feature013));
        assertFalse("f2-f14", reasoner.isTangentialProperPartOf(feature002, feature014));
        assertFalse("f2-f15", reasoner.isTangentialProperPartOf(feature002, feature015));

        assertFalse("f3-f1", reasoner.isTangentialProperPartOf(feature003, feature001));
        assertFalse("f3-f2", reasoner.isTangentialProperPartOf(feature003, feature002));
        assertFalse("f3-f3", reasoner.isTangentialProperPartOf(feature003, feature003));
        assertFalse("f3-f4", reasoner.isTangentialProperPartOf(feature003, feature004));
        assertFalse("f3-f5", reasoner.isTangentialProperPartOf(feature003, feature005));
        assertFalse("f3-f6", reasoner.isTangentialProperPartOf(feature003, feature006));
        assertFalse("f3-f7", reasoner.isTangentialProperPartOf(feature003, feature007));
        assertFalse("f3-f8", reasoner.isTangentialProperPartOf(feature003, feature008));
        assertFalse("f3-f9", reasoner.isTangentialProperPartOf(feature003, feature009));
        assertTrue("f3-f10", reasoner.isTangentialProperPartOf(feature003, feature010));
        assertTrue("f3-f11", reasoner.isTangentialProperPartOf(feature003, feature011));
        assertFalse("f3-f12", reasoner.isTangentialProperPartOf(feature003, feature012));
        assertFalse("f3-f13", reasoner.isTangentialProperPartOf(feature003, feature013));
        assertFalse("f3-f14", reasoner.isTangentialProperPartOf(feature003, feature014));
        assertFalse("f3-f15", reasoner.isTangentialProperPartOf(feature003, feature015));

        assertFalse("f4-f1", reasoner.isTangentialProperPartOf(feature004, feature001));
        assertFalse("f4-f2", reasoner.isTangentialProperPartOf(feature004, feature002));
        assertFalse("f4-f3", reasoner.isTangentialProperPartOf(feature004, feature003));
        assertFalse("f4-f4", reasoner.isTangentialProperPartOf(feature004, feature004));
        assertFalse("f4-f5", reasoner.isTangentialProperPartOf(feature004, feature005));
        assertFalse("f4-f6", reasoner.isTangentialProperPartOf(feature004, feature006));
        assertFalse("f4-f7", reasoner.isTangentialProperPartOf(feature004, feature007));
        assertFalse("f4-f8", reasoner.isTangentialProperPartOf(feature004, feature008));
        assertFalse("f4-f9", reasoner.isTangentialProperPartOf(feature004, feature009));
        assertFalse("f4-f10", reasoner.isTangentialProperPartOf(feature004, feature010));
        assertFalse("f4-f11", reasoner.isTangentialProperPartOf(feature004, feature011));
        assertFalse("f4-f12", reasoner.isTangentialProperPartOf(feature004, feature012));
        assertFalse("f4-f13", reasoner.isTangentialProperPartOf(feature004, feature013));
        assertFalse("f4-f14", reasoner.isTangentialProperPartOf(feature004, feature014));
        assertFalse("f4-f15", reasoner.isTangentialProperPartOf(feature004, feature015));

        assertFalse("f5-f1", reasoner.isTangentialProperPartOf(feature005, feature001));
        assertFalse("f5-f2", reasoner.isTangentialProperPartOf(feature005, feature002));
        assertFalse("f5-f3", reasoner.isTangentialProperPartOf(feature005, feature003));
        assertFalse("f5-f4", reasoner.isTangentialProperPartOf(feature005, feature004));
        assertFalse("f5-f5", reasoner.isTangentialProperPartOf(feature005, feature005));
        assertFalse("f5-f6", reasoner.isTangentialProperPartOf(feature005, feature006));
        assertFalse("f5-f7", reasoner.isTangentialProperPartOf(feature005, feature007));
        assertFalse("f5-f8", reasoner.isTangentialProperPartOf(feature005, feature008));
        assertFalse("f5-f9", reasoner.isTangentialProperPartOf(feature005, feature009));
        assertFalse("f5-f10", reasoner.isTangentialProperPartOf(feature005, feature010));
        assertFalse("f5-f11", reasoner.isTangentialProperPartOf(feature005, feature011));
        assertFalse("f5-f12", reasoner.isTangentialProperPartOf(feature005, feature012));
        assertFalse("f5-f13", reasoner.isTangentialProperPartOf(feature005, feature013));
        assertFalse("f5-f14", reasoner.isTangentialProperPartOf(feature005, feature014));
        assertFalse("f5-f15", reasoner.isTangentialProperPartOf(feature005, feature015));

        assertFalse("f6-f1", reasoner.isTangentialProperPartOf(feature006, feature001));
        assertFalse("f6-f2", reasoner.isTangentialProperPartOf(feature006, feature002));
        assertFalse("f6-f3", reasoner.isTangentialProperPartOf(feature006, feature003));
        assertFalse("f6-f4", reasoner.isTangentialProperPartOf(feature006, feature004));
        assertFalse("f6-f5", reasoner.isTangentialProperPartOf(feature006, feature005));
        assertFalse("f6-f6", reasoner.isTangentialProperPartOf(feature006, feature006));
        assertFalse("f6-f7", reasoner.isTangentialProperPartOf(feature006, feature007));
        assertFalse("f6-f8", reasoner.isTangentialProperPartOf(feature006, feature008));
        assertFalse("f6-f9", reasoner.isTangentialProperPartOf(feature006, feature009));
        assertTrue("f6-f10", reasoner.isTangentialProperPartOf(feature006, feature010));
        assertTrue("f6-f11", reasoner.isTangentialProperPartOf(feature006, feature011));
        assertFalse("f6-f12", reasoner.isTangentialProperPartOf(feature006, feature012));
        assertFalse("f6-f13", reasoner.isTangentialProperPartOf(feature006, feature013));
        assertFalse("f6-f14", reasoner.isTangentialProperPartOf(feature006, feature014));
        assertFalse("f6-f15", reasoner.isTangentialProperPartOf(feature006, feature015));

        assertFalse("f7-f1", reasoner.isTangentialProperPartOf(feature007, feature001));
        assertFalse("f7-f2", reasoner.isTangentialProperPartOf(feature007, feature002));
        assertFalse("f7-f3", reasoner.isTangentialProperPartOf(feature007, feature003));
        assertFalse("f7-f4", reasoner.isTangentialProperPartOf(feature007, feature004));
        assertFalse("f7-f5", reasoner.isTangentialProperPartOf(feature007, feature005));
        assertFalse("f7-f6", reasoner.isTangentialProperPartOf(feature007, feature006));
        assertFalse("f7-f7", reasoner.isTangentialProperPartOf(feature007, feature007));
        assertFalse("f7-f8", reasoner.isTangentialProperPartOf(feature007, feature008));
        assertFalse("f7-f9", reasoner.isTangentialProperPartOf(feature007, feature009));
        assertTrue("f7-f10", reasoner.isTangentialProperPartOf(feature007, feature010));
        assertTrue("f7-f11", reasoner.isTangentialProperPartOf(feature007, feature011));
        assertFalse("f7-f12", reasoner.isTangentialProperPartOf(feature007, feature012));
        assertFalse("f7-f13", reasoner.isTangentialProperPartOf(feature007, feature013));
        assertFalse("f7-f14", reasoner.isTangentialProperPartOf(feature007, feature014));
        assertFalse("f7-f15", reasoner.isTangentialProperPartOf(feature007, feature015));

        assertFalse("f8-f1", reasoner.isTangentialProperPartOf(feature008, feature001));
        assertFalse("f8-f2", reasoner.isTangentialProperPartOf(feature008, feature002));
        assertFalse("f8-f3", reasoner.isTangentialProperPartOf(feature008, feature003));
        assertFalse("f8-f4", reasoner.isTangentialProperPartOf(feature008, feature004));
        assertFalse("f8-f5", reasoner.isTangentialProperPartOf(feature008, feature005));
        assertFalse("f8-f6", reasoner.isTangentialProperPartOf(feature008, feature006));
        assertFalse("f8-f7", reasoner.isTangentialProperPartOf(feature008, feature007));
        assertFalse("f8-f8", reasoner.isTangentialProperPartOf(feature008, feature008));
        assertFalse("f8-f9", reasoner.isTangentialProperPartOf(feature008, feature009));
        assertFalse("f8-f10", reasoner.isTangentialProperPartOf(feature008, feature010));
        assertFalse("f8-f11", reasoner.isTangentialProperPartOf(feature008, feature011));
        assertFalse("f8-f12", reasoner.isTangentialProperPartOf(feature008, feature012));
        assertFalse("f8-f13", reasoner.isTangentialProperPartOf(feature008, feature013));
        assertFalse("f8-f14", reasoner.isTangentialProperPartOf(feature008, feature014));
        assertFalse("f8-f15", reasoner.isTangentialProperPartOf(feature008, feature015));

        assertFalse("f9-f1", reasoner.isTangentialProperPartOf(feature009, feature001));
        assertFalse("f9-f2", reasoner.isTangentialProperPartOf(feature009, feature002));
        assertFalse("f9-f3", reasoner.isTangentialProperPartOf(feature009, feature003));
        assertFalse("f9-f4", reasoner.isTangentialProperPartOf(feature009, feature004));
        assertFalse("f9-f5", reasoner.isTangentialProperPartOf(feature009, feature005));
        assertFalse("f9-f6", reasoner.isTangentialProperPartOf(feature009, feature006));
        assertFalse("f9-f7", reasoner.isTangentialProperPartOf(feature009, feature007));
        assertFalse("f9-f8", reasoner.isTangentialProperPartOf(feature009, feature008));
        assertFalse("f9-f9", reasoner.isTangentialProperPartOf(feature009, feature009));
        assertFalse("f9-f10", reasoner.isTangentialProperPartOf(feature009, feature010));
        assertFalse("f9-f11", reasoner.isTangentialProperPartOf(feature009, feature011));
        assertFalse("f9-f12", reasoner.isTangentialProperPartOf(feature009, feature012));
        assertFalse("f9-f13", reasoner.isTangentialProperPartOf(feature009, feature013));
        assertFalse("f9-f14", reasoner.isTangentialProperPartOf(feature009, feature014));
        assertFalse("f9-f15", reasoner.isTangentialProperPartOf(feature009, feature015));

        assertFalse("f10-f1", reasoner.isTangentialProperPartOf(feature010, feature001));
        assertFalse("f10-f2", reasoner.isTangentialProperPartOf(feature010, feature002));
        assertFalse("f10-f3", reasoner.isTangentialProperPartOf(feature010, feature003));
        assertFalse("f10-f4", reasoner.isTangentialProperPartOf(feature010, feature004));
        assertFalse("f10-f5", reasoner.isTangentialProperPartOf(feature010, feature005));
        assertFalse("f10-f6", reasoner.isTangentialProperPartOf(feature010, feature006));
        assertFalse("f10-f7", reasoner.isTangentialProperPartOf(feature010, feature007));
        assertFalse("f10-f8", reasoner.isTangentialProperPartOf(feature010, feature008));
        assertFalse("f10-f9", reasoner.isTangentialProperPartOf(feature010, feature009));
        assertFalse("f10-f10", reasoner.isTangentialProperPartOf(feature010, feature010));
        assertFalse("f10-f11", reasoner.isTangentialProperPartOf(feature010, feature011));
        assertFalse("f10-f12", reasoner.isTangentialProperPartOf(feature010, feature012));
        assertFalse("f10-f13", reasoner.isTangentialProperPartOf(feature010, feature013));
        assertFalse("f10-f14", reasoner.isTangentialProperPartOf(feature010, feature014));
        assertFalse("f10-f15", reasoner.isTangentialProperPartOf(feature010, feature015));

        assertFalse("f11-f1", reasoner.isTangentialProperPartOf(feature011, feature001));
        assertFalse("f11-f2", reasoner.isTangentialProperPartOf(feature011, feature002));
        assertFalse("f11-f3", reasoner.isTangentialProperPartOf(feature011, feature003));
        assertFalse("f11-f4", reasoner.isTangentialProperPartOf(feature011, feature004));
        assertFalse("f11-f5", reasoner.isTangentialProperPartOf(feature011, feature005));
        assertFalse("f11-f6", reasoner.isTangentialProperPartOf(feature011, feature006));
        assertFalse("f11-f7", reasoner.isTangentialProperPartOf(feature011, feature007));
        assertFalse("f11-f8", reasoner.isTangentialProperPartOf(feature011, feature008));
        assertFalse("f11-f9", reasoner.isTangentialProperPartOf(feature011, feature009));
        assertFalse("f11-f10", reasoner.isTangentialProperPartOf(feature011, feature010));
        assertFalse("f11-f11", reasoner.isTangentialProperPartOf(feature011, feature011));
        assertFalse("f11-f12", reasoner.isTangentialProperPartOf(feature011, feature012));
        assertFalse("f11-f13", reasoner.isTangentialProperPartOf(feature011, feature013));
        assertFalse("f11-f14", reasoner.isTangentialProperPartOf(feature011, feature014));
        assertFalse("f11-f15", reasoner.isTangentialProperPartOf(feature011, feature015));

        assertFalse("f12-f1", reasoner.isTangentialProperPartOf(feature012, feature001));
        assertFalse("f12-f2", reasoner.isTangentialProperPartOf(feature012, feature002));
        assertFalse("f12-f3", reasoner.isTangentialProperPartOf(feature012, feature003));
        assertFalse("f12-f4", reasoner.isTangentialProperPartOf(feature012, feature004));
        assertFalse("f12-f5", reasoner.isTangentialProperPartOf(feature012, feature005));
        assertFalse("f12-f6", reasoner.isTangentialProperPartOf(feature012, feature006));
        assertFalse("f12-f7", reasoner.isTangentialProperPartOf(feature012, feature007));
        assertFalse("f12-f8", reasoner.isTangentialProperPartOf(feature012, feature008));
        assertFalse("f12-f9", reasoner.isTangentialProperPartOf(feature012, feature009));
        assertTrue("f12-f10", reasoner.isTangentialProperPartOf(feature012, feature010));
        assertTrue("f12-f11", reasoner.isTangentialProperPartOf(feature012, feature011));
        assertFalse("f12-f12", reasoner.isTangentialProperPartOf(feature012, feature012));
        assertFalse("f12-f13", reasoner.isTangentialProperPartOf(feature012, feature013));
        assertFalse("f12-f14", reasoner.isTangentialProperPartOf(feature012, feature014));
        assertFalse("f12-f15", reasoner.isTangentialProperPartOf(feature012, feature015));

        assertFalse("f13-f1", reasoner.isTangentialProperPartOf(feature013, feature001));
        assertFalse("f13-f2", reasoner.isTangentialProperPartOf(feature013, feature002));
        assertFalse("f13-f3", reasoner.isTangentialProperPartOf(feature013, feature003));
        assertFalse("f13-f4", reasoner.isTangentialProperPartOf(feature013, feature004));
        assertFalse("f13-f5", reasoner.isTangentialProperPartOf(feature013, feature005));
        assertFalse("f13-f6", reasoner.isTangentialProperPartOf(feature013, feature006));
        assertFalse("f13-f7", reasoner.isTangentialProperPartOf(feature013, feature007));
        assertFalse("f13-f8", reasoner.isTangentialProperPartOf(feature013, feature008));
        assertFalse("f13-f9", reasoner.isTangentialProperPartOf(feature013, feature009));
        assertFalse("f13-f10", reasoner.isTangentialProperPartOf(feature013, feature010));
        assertFalse("f13-f11", reasoner.isTangentialProperPartOf(feature013, feature011));
        assertFalse("f13-f12", reasoner.isTangentialProperPartOf(feature013, feature012));
        assertFalse("f13-f13", reasoner.isTangentialProperPartOf(feature013, feature013));
        assertFalse("f13-f14", reasoner.isTangentialProperPartOf(feature013, feature014));
        assertFalse("f13-f15", reasoner.isTangentialProperPartOf(feature013, feature015));

        assertFalse("f14-f1", reasoner.isTangentialProperPartOf(feature014, feature001));
        assertFalse("f14-f2", reasoner.isTangentialProperPartOf(feature014, feature002));
        assertFalse("f14-f3", reasoner.isTangentialProperPartOf(feature014, feature003));
        assertFalse("f14-f4", reasoner.isTangentialProperPartOf(feature014, feature004));
        assertFalse("f14-f5", reasoner.isTangentialProperPartOf(feature014, feature005));
        assertFalse("f14-f6", reasoner.isTangentialProperPartOf(feature014, feature006));
        assertFalse("f14-f7", reasoner.isTangentialProperPartOf(feature014, feature007));
        assertFalse("f14-f8", reasoner.isTangentialProperPartOf(feature014, feature008));
        assertFalse("f14-f9", reasoner.isTangentialProperPartOf(feature014, feature009));
        assertFalse("f14-f10", reasoner.isTangentialProperPartOf(feature014, feature010));
        assertFalse("f14-f11", reasoner.isTangentialProperPartOf(feature014, feature011));
        assertFalse("f14-f12", reasoner.isTangentialProperPartOf(feature014, feature012));
        assertFalse("f14-f13", reasoner.isTangentialProperPartOf(feature014, feature013));
        assertFalse("f14-f14", reasoner.isTangentialProperPartOf(feature014, feature014));
        assertFalse("f14-f15", reasoner.isTangentialProperPartOf(feature014, feature015));

        assertFalse("f15-f1", reasoner.isTangentialProperPartOf(feature015, feature001));
        assertFalse("f15-f2", reasoner.isTangentialProperPartOf(feature015, feature002));
        assertFalse("f15-f3", reasoner.isTangentialProperPartOf(feature015, feature003));
        assertFalse("f15-f4", reasoner.isTangentialProperPartOf(feature015, feature004));
        assertFalse("f15-f5", reasoner.isTangentialProperPartOf(feature015, feature005));
        assertFalse("f15-f6", reasoner.isTangentialProperPartOf(feature015, feature006));
        assertFalse("f15-f7", reasoner.isTangentialProperPartOf(feature015, feature007));
        assertFalse("f15-f8", reasoner.isTangentialProperPartOf(feature015, feature008));
        assertFalse("f15-f9", reasoner.isTangentialProperPartOf(feature015, feature009));
        assertFalse("f15-f10", reasoner.isTangentialProperPartOf(feature015, feature010));
        assertFalse("f15-f11", reasoner.isTangentialProperPartOf(feature015, feature011));
        assertFalse("f15-f12", reasoner.isTangentialProperPartOf(feature015, feature012));
        assertFalse("f15-f13", reasoner.isTangentialProperPartOf(feature015, feature013));
        assertFalse("f15-f14", reasoner.isTangentialProperPartOf(feature015, feature014));
        assertFalse("f15-f15", reasoner.isTangentialProperPartOf(feature015, feature015));
    }

    @Test
    public void testGetIndividualsTangentialProperPartOf() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0591)");

        // -- same as feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0591)");

        // -- TPP of feature010/11, but not of feature006/7
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8016 51.0589)");

        // -- off
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.7983 51.0591)");

        // -- inside feature010/11, but not TPP
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005, "POINT(13.8015 51.0583)");

        // line strings
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- same as feature006
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- inside but not TPP
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8009 51.0588,13.8003 51.0585,13.8009 51.0583)");

        // -- off
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "LINESTRING(13.8025 51.0577,13.8036 51.0579," +
                        "13.8029 51.0574,13.8040 51.0577," +
                        "13.8032 51.0572,13.8043 51.0574)");

        // areas
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- same as feature010
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- TPP
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.8000 51.0585,13.8008 51.0581," +
                        "13.8007 51.0578,13.7996 51.0583,13.8000 51.0585))");

        // -- NTPP, contains feature005
        OWLIndividual feature013 = i("feature13");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.8011 51.0585,13.8016 51.0586," +
                        "13.8018 51.0582,13.8013 51.0581,13.8011 51.0585))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7991 51.0602,13.7991 51.0597," +
                        "13.7999 51.0599,13.7991 51.0602))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsTangentialProperPartOf(feature001)
                        .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));
        assertFalse("f1-f10", result.contains(feature010));
        assertFalse("f1-f11", result.contains(feature011));
        assertFalse("f1-f12", result.contains(feature012));
        assertFalse("f1-f13", result.contains(feature013));
        assertFalse("f1-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature002)
                        .collect(Collectors.toSet());
        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));
        assertFalse("f2-f10", result.contains(feature010));
        assertFalse("f2-f11", result.contains(feature011));
        assertFalse("f2-f12", result.contains(feature012));
        assertFalse("f2-f13", result.contains(feature013));
        assertFalse("f2-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertFalse("f3-f11", result.contains(feature011));
        assertFalse("f3-f12", result.contains(feature012));
        assertFalse("f3-f13", result.contains(feature013));
        assertFalse("f3-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature004)
                        .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertFalse("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));
        assertFalse("f4-f12", result.contains(feature012));
        assertFalse("f4-f13", result.contains(feature013));
        assertFalse("f4-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature005)
                        .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertFalse("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertFalse("f5-f11", result.contains(feature011));
        assertFalse("f5-f12", result.contains(feature012));
        assertFalse("f5-f13", result.contains(feature013));
        assertFalse("f5-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature006)
                        .collect(Collectors.toSet());

        assertTrue("f6-f1", result.contains(feature001));
        assertTrue("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertFalse("f6-f11", result.contains(feature011));
        assertFalse("f6-f12", result.contains(feature012));
        assertFalse("f6-f13", result.contains(feature013));
        assertFalse("f6-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature007)
                        .collect(Collectors.toSet());

        assertTrue("f7-f1", result.contains(feature001));
        assertTrue("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertFalse("f7-f10", result.contains(feature010));
        assertFalse("f7-f11", result.contains(feature011));
        assertFalse("f7-f12", result.contains(feature012));
        assertFalse("f7-f13", result.contains(feature013));
        assertFalse("f7-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature008)
                        .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertFalse("f8-f8", result.contains(feature008));
        assertFalse("f8-f9", result.contains(feature009));
        assertFalse("f8-f10", result.contains(feature010));
        assertFalse("f8-f11", result.contains(feature011));
        assertFalse("f8-f12", result.contains(feature012));
        assertFalse("f8-f13", result.contains(feature013));
        assertFalse("f8-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature009)
                        .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertFalse("f9-f8", result.contains(feature008));
        assertFalse("f9-f9", result.contains(feature009));
        assertFalse("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));
        assertFalse("f9-f12", result.contains(feature012));
        assertFalse("f9-f13", result.contains(feature013));
        assertFalse("f9-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature010)
                        .collect(Collectors.toSet());

        assertTrue("f10-f1", result.contains(feature001));
        assertTrue("f10-f2", result.contains(feature002));
        assertTrue("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertTrue("f10-f6", result.contains(feature006));
        assertTrue("f10-f7", result.contains(feature007));
        assertFalse("f10-f8", result.contains(feature008));
        assertFalse("f10-f9", result.contains(feature009));
        assertFalse("f10-f10", result.contains(feature010));
        assertFalse("f10-f11", result.contains(feature011));
        assertTrue("f10-f12", result.contains(feature012));
        assertFalse("f10-f13", result.contains(feature013));
        assertFalse("f10-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature011)
                        .collect(Collectors.toSet());

        assertTrue("f11-f1", result.contains(feature001));
        assertTrue("f11-f2", result.contains(feature002));
        assertTrue("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertFalse("f11-f5", result.contains(feature005));
        assertTrue("f11-f6", result.contains(feature006));
        assertTrue("f11-f7", result.contains(feature007));
        assertFalse("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertFalse("f11-f10", result.contains(feature010));
        assertFalse("f11-f11", result.contains(feature011));
        assertTrue("f11-f12", result.contains(feature012));
        assertFalse("f11-f13", result.contains(feature013));
        assertFalse("f11-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature012)
                        .collect(Collectors.toSet());

        assertFalse("f12-f1", result.contains(feature001));
        assertFalse("f12-f2", result.contains(feature002));
        assertFalse("f12-f3", result.contains(feature003));
        assertFalse("f12-f4", result.contains(feature004));
        assertFalse("f12-f5", result.contains(feature005));
        assertFalse("f12-f6", result.contains(feature006));
        assertFalse("f12-f7", result.contains(feature007));
        assertFalse("f12-f8", result.contains(feature008));
        assertFalse("f12-f9", result.contains(feature009));
        assertFalse("f12-f10", result.contains(feature010));
        assertFalse("f12-f11", result.contains(feature011));
        assertFalse("f12-f12", result.contains(feature012));
        assertFalse("f12-f13", result.contains(feature013));
        assertFalse("f12-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature013)
                        .collect(Collectors.toSet());

        assertFalse("f13-f1", result.contains(feature001));
        assertFalse("f13-f2", result.contains(feature002));
        assertFalse("f13-f3", result.contains(feature003));
        assertFalse("f13-f4", result.contains(feature004));
        assertFalse("f13-f5", result.contains(feature005));
        assertFalse("f13-f6", result.contains(feature006));
        assertFalse("f13-f7", result.contains(feature007));
        assertFalse("f13-f8", result.contains(feature008));
        assertFalse("f13-f9", result.contains(feature009));
        assertFalse("f13-f10", result.contains(feature010));
        assertFalse("f13-f11", result.contains(feature011));
        assertFalse("f13-f12", result.contains(feature012));
        assertFalse("f13-f13", result.contains(feature013));
        assertFalse("f13-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature014)
                        .collect(Collectors.toSet());

        assertFalse("f14-f1", result.contains(feature001));
        assertFalse("f14-f2", result.contains(feature002));
        assertFalse("f14-f3", result.contains(feature003));
        assertFalse("f14-f4", result.contains(feature004));
        assertFalse("f14-f5", result.contains(feature005));
        assertFalse("f14-f6", result.contains(feature006));
        assertFalse("f14-f7", result.contains(feature007));
        assertFalse("f14-f8", result.contains(feature008));
        assertFalse("f14-f9", result.contains(feature009));
        assertFalse("f14-f10", result.contains(feature010));
        assertFalse("f14-f11", result.contains(feature011));
        assertFalse("f14-f12", result.contains(feature012));
        assertFalse("f14-f13", result.contains(feature013));
        assertFalse("f14-f14", result.contains(feature014));
    }

    @Test
    public void testIsNonTangentialProperPartOf() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0591)");

        // -- same as feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0591)");

        // -- TPP of feature010/11, but not of feature006/7
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8016 51.0589)");

        // -- off
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.7983 51.0591)");

        // -- inside feature010/11, but not TPP
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005, "POINT(13.8015 51.0583)");

        // line strings
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- same as feature006
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- inside but not TPP
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8009 51.0588,13.8003 51.0585,13.8009 51.0583)");

        // -- off
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "LINESTRING(13.8025 51.0577,13.8036 51.0579," +
                        "13.8029 51.0574,13.8040 51.0577," +
                        "13.8032 51.0572,13.8043 51.0574)");

        // areas
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- same as feature010
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- TPP
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.8000 51.0585,13.8008 51.0581," +
                        "13.8007 51.0578,13.7996 51.0583,13.8000 51.0585))");

        // -- NTPP, contains feature005
        OWLIndividual feature013 = i("feature13");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.8011 51.0585,13.8016 51.0586," +
                        "13.8018 51.0582,13.8013 51.0581,13.8011 51.0585))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7991 51.0602,13.7991 51.0597," +
                        "13.7999 51.0599,13.7991 51.0602))");

        // -- added for NTPP case to have a line being NTPP of another line
        OWLIndividual feature015 = i("feature015");
        OWLIndividual geom015 = i("geom015");
        kbHelper.addSpatialFeature(feature015, geom015,
                "LINESTRING(13.8036 51.0579,13.8029 51.0574," +
                        "13.8040 51.0577,13.8032 51.0572)");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertFalse("f1-f1", reasoner.isNonTangentialProperPartOf(feature001, feature001));
        assertFalse("f1-f2", reasoner.isNonTangentialProperPartOf(feature001, feature002));
        assertFalse("f1-f3", reasoner.isNonTangentialProperPartOf(feature001, feature003));
        assertFalse("f1-f4", reasoner.isNonTangentialProperPartOf(feature001, feature004));
        assertFalse("f1-f5", reasoner.isNonTangentialProperPartOf(feature001, feature005));
        assertFalse("f1-f6", reasoner.isNonTangentialProperPartOf(feature001, feature006));
        assertFalse("f1-f7", reasoner.isNonTangentialProperPartOf(feature001, feature007));
        assertFalse("f1-f8", reasoner.isNonTangentialProperPartOf(feature001, feature008));
        assertFalse("f1-f9", reasoner.isNonTangentialProperPartOf(feature001, feature009));
        assertFalse("f1-f10", reasoner.isNonTangentialProperPartOf(feature001, feature010));
        assertFalse("f1-f11", reasoner.isNonTangentialProperPartOf(feature001, feature011));
        assertFalse("f1-f12", reasoner.isNonTangentialProperPartOf(feature001, feature012));
        assertFalse("f1-f13", reasoner.isNonTangentialProperPartOf(feature001, feature013));
        assertFalse("f1-f14", reasoner.isNonTangentialProperPartOf(feature001, feature014));
        assertFalse("f1-f15", reasoner.isNonTangentialProperPartOf(feature001, feature015));

        assertFalse("f2-f1", reasoner.isNonTangentialProperPartOf(feature002, feature001));
        assertFalse("f2-f2", reasoner.isNonTangentialProperPartOf(feature002, feature002));
        assertFalse("f2-f3", reasoner.isNonTangentialProperPartOf(feature002, feature003));
        assertFalse("f2-f4", reasoner.isNonTangentialProperPartOf(feature002, feature004));
        assertFalse("f2-f5", reasoner.isNonTangentialProperPartOf(feature002, feature005));
        assertFalse("f2-f6", reasoner.isNonTangentialProperPartOf(feature002, feature006));
        assertFalse("f2-f7", reasoner.isNonTangentialProperPartOf(feature002, feature007));
        assertFalse("f2-f8", reasoner.isNonTangentialProperPartOf(feature002, feature008));
        assertFalse("f2-f9", reasoner.isNonTangentialProperPartOf(feature002, feature009));
        assertFalse("f2-f10", reasoner.isNonTangentialProperPartOf(feature002, feature010));
        assertFalse("f2-f11", reasoner.isNonTangentialProperPartOf(feature002, feature011));
        assertFalse("f2-f12", reasoner.isNonTangentialProperPartOf(feature002, feature012));
        assertFalse("f2-f13", reasoner.isNonTangentialProperPartOf(feature002, feature013));
        assertFalse("f2-f14", reasoner.isNonTangentialProperPartOf(feature002, feature014));
        assertFalse("f2-f15", reasoner.isNonTangentialProperPartOf(feature002, feature015));

        assertFalse("f3-f1", reasoner.isNonTangentialProperPartOf(feature003, feature001));
        assertFalse("f3-f2", reasoner.isNonTangentialProperPartOf(feature003, feature002));
        assertFalse("f3-f3", reasoner.isNonTangentialProperPartOf(feature003, feature003));
        assertFalse("f3-f4", reasoner.isNonTangentialProperPartOf(feature003, feature004));
        assertFalse("f3-f5", reasoner.isNonTangentialProperPartOf(feature003, feature005));
        assertTrue("f3-f6", reasoner.isNonTangentialProperPartOf(feature003, feature006));
        assertTrue("f3-f7", reasoner.isNonTangentialProperPartOf(feature003, feature007));
        assertFalse("f3-f8", reasoner.isNonTangentialProperPartOf(feature003, feature008));
        assertFalse("f3-f9", reasoner.isNonTangentialProperPartOf(feature003, feature009));
        assertFalse("f3-f10", reasoner.isNonTangentialProperPartOf(feature003, feature010));
        assertFalse("f3-f11", reasoner.isNonTangentialProperPartOf(feature003, feature011));
        assertFalse("f3-f12", reasoner.isNonTangentialProperPartOf(feature003, feature012));
        assertFalse("f3-f13", reasoner.isNonTangentialProperPartOf(feature003, feature013));
        assertFalse("f3-f14", reasoner.isNonTangentialProperPartOf(feature003, feature014));
        assertFalse("f3-f15", reasoner.isNonTangentialProperPartOf(feature003, feature015));

        assertFalse("f4-f1", reasoner.isNonTangentialProperPartOf(feature004, feature001));
        assertFalse("f4-f2", reasoner.isNonTangentialProperPartOf(feature004, feature002));
        assertFalse("f4-f3", reasoner.isNonTangentialProperPartOf(feature004, feature003));
        assertFalse("f4-f4", reasoner.isNonTangentialProperPartOf(feature004, feature004));
        assertFalse("f4-f5", reasoner.isNonTangentialProperPartOf(feature004, feature005));
        assertFalse("f4-f6", reasoner.isNonTangentialProperPartOf(feature004, feature006));
        assertFalse("f4-f7", reasoner.isNonTangentialProperPartOf(feature004, feature007));
        assertFalse("f4-f8", reasoner.isNonTangentialProperPartOf(feature004, feature008));
        assertFalse("f4-f9", reasoner.isNonTangentialProperPartOf(feature004, feature009));
        assertFalse("f4-f10", reasoner.isNonTangentialProperPartOf(feature004, feature010));
        assertFalse("f4-f11", reasoner.isNonTangentialProperPartOf(feature004, feature011));
        assertFalse("f4-f12", reasoner.isNonTangentialProperPartOf(feature004, feature012));
        assertFalse("f4-f13", reasoner.isNonTangentialProperPartOf(feature004, feature013));
        assertFalse("f4-f14", reasoner.isNonTangentialProperPartOf(feature004, feature014));
        assertFalse("f4-f15", reasoner.isNonTangentialProperPartOf(feature004, feature015));

        assertFalse("f5-f1", reasoner.isNonTangentialProperPartOf(feature005, feature001));
        assertFalse("f5-f2", reasoner.isNonTangentialProperPartOf(feature005, feature002));
        assertFalse("f5-f3", reasoner.isNonTangentialProperPartOf(feature005, feature003));
        assertFalse("f5-f4", reasoner.isNonTangentialProperPartOf(feature005, feature004));
        assertFalse("f5-f5", reasoner.isNonTangentialProperPartOf(feature005, feature005));
        assertFalse("f5-f6", reasoner.isNonTangentialProperPartOf(feature005, feature006));
        assertFalse("f5-f7", reasoner.isNonTangentialProperPartOf(feature005, feature007));
        assertFalse("f5-f8", reasoner.isNonTangentialProperPartOf(feature005, feature008));
        assertFalse("f5-f9", reasoner.isNonTangentialProperPartOf(feature005, feature009));
        assertTrue("f5-f10", reasoner.isNonTangentialProperPartOf(feature005, feature010));
        assertTrue("f5-f11", reasoner.isNonTangentialProperPartOf(feature005, feature011));
        assertFalse("f5-f12", reasoner.isNonTangentialProperPartOf(feature005, feature012));
        assertTrue("f5-f13", reasoner.isNonTangentialProperPartOf(feature005, feature013));
        assertFalse("f5-f14", reasoner.isNonTangentialProperPartOf(feature005, feature014));
        assertFalse("f5-f15", reasoner.isNonTangentialProperPartOf(feature005, feature015));

        assertFalse("f6-f1", reasoner.isNonTangentialProperPartOf(feature006, feature001));
        assertFalse("f6-f2", reasoner.isNonTangentialProperPartOf(feature006, feature002));
        assertFalse("f6-f3", reasoner.isNonTangentialProperPartOf(feature006, feature003));
        assertFalse("f6-f4", reasoner.isNonTangentialProperPartOf(feature006, feature004));
        assertFalse("f6-f5", reasoner.isNonTangentialProperPartOf(feature006, feature005));
        assertFalse("f6-f6", reasoner.isNonTangentialProperPartOf(feature006, feature006));
        assertFalse("f6-f7", reasoner.isNonTangentialProperPartOf(feature006, feature007));
        assertFalse("f6-f8", reasoner.isNonTangentialProperPartOf(feature006, feature008));
        assertFalse("f6-f9", reasoner.isNonTangentialProperPartOf(feature006, feature009));
        assertFalse("f6-f10", reasoner.isNonTangentialProperPartOf(feature006, feature010));
        assertFalse("f6-f11", reasoner.isNonTangentialProperPartOf(feature006, feature011));
        assertFalse("f6-f12", reasoner.isNonTangentialProperPartOf(feature006, feature012));
        assertFalse("f6-f13", reasoner.isNonTangentialProperPartOf(feature006, feature013));
        assertFalse("f6-f14", reasoner.isNonTangentialProperPartOf(feature006, feature014));
        assertFalse("f6-f15", reasoner.isNonTangentialProperPartOf(feature006, feature015));

        assertFalse("f7-f1", reasoner.isNonTangentialProperPartOf(feature007, feature001));
        assertFalse("f7-f2", reasoner.isNonTangentialProperPartOf(feature007, feature002));
        assertFalse("f7-f3", reasoner.isNonTangentialProperPartOf(feature007, feature003));
        assertFalse("f7-f4", reasoner.isNonTangentialProperPartOf(feature007, feature004));
        assertFalse("f7-f5", reasoner.isNonTangentialProperPartOf(feature007, feature005));
        assertFalse("f7-f6", reasoner.isNonTangentialProperPartOf(feature007, feature006));
        assertFalse("f7-f7", reasoner.isNonTangentialProperPartOf(feature007, feature007));
        assertFalse("f7-f8", reasoner.isNonTangentialProperPartOf(feature007, feature008));
        assertFalse("f7-f9", reasoner.isNonTangentialProperPartOf(feature007, feature009));
        assertFalse("f7-f10", reasoner.isNonTangentialProperPartOf(feature007, feature010));
        assertFalse("f7-f11", reasoner.isNonTangentialProperPartOf(feature007, feature011));
        assertFalse("f7-f12", reasoner.isNonTangentialProperPartOf(feature007, feature012));
        assertFalse("f7-f13", reasoner.isNonTangentialProperPartOf(feature007, feature013));
        assertFalse("f7-f14", reasoner.isNonTangentialProperPartOf(feature007, feature014));
        assertFalse("f7-f15", reasoner.isNonTangentialProperPartOf(feature007, feature015));

        assertFalse("f8-f1", reasoner.isNonTangentialProperPartOf(feature008, feature001));
        assertFalse("f8-f2", reasoner.isNonTangentialProperPartOf(feature008, feature002));
        assertFalse("f8-f3", reasoner.isNonTangentialProperPartOf(feature008, feature003));
        assertFalse("f8-f4", reasoner.isNonTangentialProperPartOf(feature008, feature004));
        assertFalse("f8-f5", reasoner.isNonTangentialProperPartOf(feature008, feature005));
        assertFalse("f8-f6", reasoner.isNonTangentialProperPartOf(feature008, feature006));
        assertFalse("f8-f7", reasoner.isNonTangentialProperPartOf(feature008, feature007));
        assertFalse("f8-f8", reasoner.isNonTangentialProperPartOf(feature008, feature008));
        assertFalse("f8-f9", reasoner.isNonTangentialProperPartOf(feature008, feature009));
        assertTrue("f8-f10", reasoner.isNonTangentialProperPartOf(feature008, feature010));
        assertTrue("f8-f11", reasoner.isNonTangentialProperPartOf(feature008, feature011));
        assertFalse("f8-f12", reasoner.isNonTangentialProperPartOf(feature008, feature012));
        assertFalse("f8-f13", reasoner.isNonTangentialProperPartOf(feature008, feature013));
        assertFalse("f8-f14", reasoner.isNonTangentialProperPartOf(feature008, feature014));
        assertFalse("f8-f15", reasoner.isNonTangentialProperPartOf(feature008, feature015));

        assertFalse("f9-f1", reasoner.isNonTangentialProperPartOf(feature009, feature001));
        assertFalse("f9-f2", reasoner.isNonTangentialProperPartOf(feature009, feature002));
        assertFalse("f9-f3", reasoner.isNonTangentialProperPartOf(feature009, feature003));
        assertFalse("f9-f4", reasoner.isNonTangentialProperPartOf(feature009, feature004));
        assertFalse("f9-f5", reasoner.isNonTangentialProperPartOf(feature009, feature005));
        assertFalse("f9-f6", reasoner.isNonTangentialProperPartOf(feature009, feature006));
        assertFalse("f9-f7", reasoner.isNonTangentialProperPartOf(feature009, feature007));
        assertFalse("f9-f8", reasoner.isNonTangentialProperPartOf(feature009, feature008));
        assertFalse("f9-f9", reasoner.isNonTangentialProperPartOf(feature009, feature009));
        assertFalse("f9-f10", reasoner.isNonTangentialProperPartOf(feature009, feature010));
        assertFalse("f9-f11", reasoner.isNonTangentialProperPartOf(feature009, feature011));
        assertFalse("f9-f12", reasoner.isNonTangentialProperPartOf(feature009, feature012));
        assertFalse("f9-f13", reasoner.isNonTangentialProperPartOf(feature009, feature013));
        assertFalse("f9-f14", reasoner.isNonTangentialProperPartOf(feature009, feature014));
        assertFalse("f9-f15", reasoner.isNonTangentialProperPartOf(feature009, feature015));

        assertFalse("f10-f1", reasoner.isNonTangentialProperPartOf(feature010, feature001));
        assertFalse("f10-f2", reasoner.isNonTangentialProperPartOf(feature010, feature002));
        assertFalse("f10-f3", reasoner.isNonTangentialProperPartOf(feature010, feature003));
        assertFalse("f10-f4", reasoner.isNonTangentialProperPartOf(feature010, feature004));
        assertFalse("f10-f5", reasoner.isNonTangentialProperPartOf(feature010, feature005));
        assertFalse("f10-f6", reasoner.isNonTangentialProperPartOf(feature010, feature006));
        assertFalse("f10-f7", reasoner.isNonTangentialProperPartOf(feature010, feature007));
        assertFalse("f10-f8", reasoner.isNonTangentialProperPartOf(feature010, feature008));
        assertFalse("f10-f9", reasoner.isNonTangentialProperPartOf(feature010, feature009));
        assertFalse("f10-f10", reasoner.isNonTangentialProperPartOf(feature010, feature010));
        assertFalse("f10-f11", reasoner.isNonTangentialProperPartOf(feature010, feature011));
        assertFalse("f10-f12", reasoner.isNonTangentialProperPartOf(feature010, feature012));
        assertFalse("f10-f13", reasoner.isNonTangentialProperPartOf(feature010, feature013));
        assertFalse("f10-f14", reasoner.isNonTangentialProperPartOf(feature010, feature014));
        assertFalse("f10-f15", reasoner.isNonTangentialProperPartOf(feature010, feature015));

        assertFalse("f11-f1", reasoner.isNonTangentialProperPartOf(feature011, feature001));
        assertFalse("f11-f2", reasoner.isNonTangentialProperPartOf(feature011, feature002));
        assertFalse("f11-f3", reasoner.isNonTangentialProperPartOf(feature011, feature003));
        assertFalse("f11-f4", reasoner.isNonTangentialProperPartOf(feature011, feature004));
        assertFalse("f11-f5", reasoner.isNonTangentialProperPartOf(feature011, feature005));
        assertFalse("f11-f6", reasoner.isNonTangentialProperPartOf(feature011, feature006));
        assertFalse("f11-f7", reasoner.isNonTangentialProperPartOf(feature011, feature007));
        assertFalse("f11-f8", reasoner.isNonTangentialProperPartOf(feature011, feature008));
        assertFalse("f11-f9", reasoner.isNonTangentialProperPartOf(feature011, feature009));
        assertFalse("f11-f10", reasoner.isNonTangentialProperPartOf(feature011, feature010));
        assertFalse("f11-f11", reasoner.isNonTangentialProperPartOf(feature011, feature011));
        assertFalse("f11-f12", reasoner.isNonTangentialProperPartOf(feature011, feature012));
        assertFalse("f11-f13", reasoner.isNonTangentialProperPartOf(feature011, feature013));
        assertFalse("f11-f14", reasoner.isNonTangentialProperPartOf(feature011, feature014));
        assertFalse("f11-f15", reasoner.isNonTangentialProperPartOf(feature011, feature015));

        assertFalse("f12-f1", reasoner.isNonTangentialProperPartOf(feature012, feature001));
        assertFalse("f12-f2", reasoner.isNonTangentialProperPartOf(feature012, feature002));
        assertFalse("f12-f3", reasoner.isNonTangentialProperPartOf(feature012, feature003));
        assertFalse("f12-f4", reasoner.isNonTangentialProperPartOf(feature012, feature004));
        assertFalse("f12-f5", reasoner.isNonTangentialProperPartOf(feature012, feature005));
        assertFalse("f12-f6", reasoner.isNonTangentialProperPartOf(feature012, feature006));
        assertFalse("f12-f7", reasoner.isNonTangentialProperPartOf(feature012, feature007));
        assertFalse("f12-f8", reasoner.isNonTangentialProperPartOf(feature012, feature008));
        assertFalse("f12-f9", reasoner.isNonTangentialProperPartOf(feature012, feature009));
        assertFalse("f12-f10", reasoner.isNonTangentialProperPartOf(feature012, feature010));
        assertFalse("f12-f11", reasoner.isNonTangentialProperPartOf(feature012, feature011));
        assertFalse("f12-f12", reasoner.isNonTangentialProperPartOf(feature012, feature012));
        assertFalse("f12-f13", reasoner.isNonTangentialProperPartOf(feature012, feature013));
        assertFalse("f12-f14", reasoner.isNonTangentialProperPartOf(feature012, feature014));
        assertFalse("f12-f15", reasoner.isNonTangentialProperPartOf(feature012, feature015));

        assertFalse("f13-f1", reasoner.isNonTangentialProperPartOf(feature013, feature001));
        assertFalse("f13-f2", reasoner.isNonTangentialProperPartOf(feature013, feature002));
        assertFalse("f13-f3", reasoner.isNonTangentialProperPartOf(feature013, feature003));
        assertFalse("f13-f4", reasoner.isNonTangentialProperPartOf(feature013, feature004));
        assertFalse("f13-f5", reasoner.isNonTangentialProperPartOf(feature013, feature005));
        assertFalse("f13-f6", reasoner.isNonTangentialProperPartOf(feature013, feature006));
        assertFalse("f13-f7", reasoner.isNonTangentialProperPartOf(feature013, feature007));
        assertFalse("f13-f8", reasoner.isNonTangentialProperPartOf(feature013, feature008));
        assertFalse("f13-f9", reasoner.isNonTangentialProperPartOf(feature013, feature009));
        assertTrue("f13-f10", reasoner.isNonTangentialProperPartOf(feature013, feature010));
        assertTrue("f13-f11", reasoner.isNonTangentialProperPartOf(feature013, feature011));
        assertFalse("f13-f12", reasoner.isNonTangentialProperPartOf(feature013, feature012));
        assertFalse("f13-f13", reasoner.isNonTangentialProperPartOf(feature013, feature013));
        assertFalse("f13-f14", reasoner.isNonTangentialProperPartOf(feature013, feature014));
        assertFalse("f13-f15", reasoner.isNonTangentialProperPartOf(feature013, feature015));

        assertFalse("f14-f1", reasoner.isNonTangentialProperPartOf(feature014, feature001));
        assertFalse("f14-f2", reasoner.isNonTangentialProperPartOf(feature014, feature002));
        assertFalse("f14-f3", reasoner.isNonTangentialProperPartOf(feature014, feature003));
        assertFalse("f14-f4", reasoner.isNonTangentialProperPartOf(feature014, feature004));
        assertFalse("f14-f5", reasoner.isNonTangentialProperPartOf(feature014, feature005));
        assertFalse("f14-f6", reasoner.isNonTangentialProperPartOf(feature014, feature006));
        assertFalse("f14-f7", reasoner.isNonTangentialProperPartOf(feature014, feature007));
        assertFalse("f14-f8", reasoner.isNonTangentialProperPartOf(feature014, feature008));
        assertFalse("f14-f9", reasoner.isNonTangentialProperPartOf(feature014, feature009));
        assertFalse("f14-f10", reasoner.isNonTangentialProperPartOf(feature014, feature010));
        assertFalse("f14-f11", reasoner.isNonTangentialProperPartOf(feature014, feature011));
        assertFalse("f14-f12", reasoner.isNonTangentialProperPartOf(feature014, feature012));
        assertFalse("f14-f13", reasoner.isNonTangentialProperPartOf(feature014, feature013));
        assertFalse("f14-f14", reasoner.isNonTangentialProperPartOf(feature014, feature014));
        assertFalse("f14-f15", reasoner.isNonTangentialProperPartOf(feature014, feature015));

        assertFalse("f15-f1", reasoner.isNonTangentialProperPartOf(feature015, feature001));
        assertFalse("f15-f2", reasoner.isNonTangentialProperPartOf(feature015, feature002));
        assertFalse("f15-f3", reasoner.isNonTangentialProperPartOf(feature015, feature003));
        assertFalse("f15-f4", reasoner.isNonTangentialProperPartOf(feature015, feature004));
        assertFalse("f15-f5", reasoner.isNonTangentialProperPartOf(feature015, feature005));
        assertFalse("f15-f6", reasoner.isNonTangentialProperPartOf(feature015, feature006));
        assertFalse("f15-f7", reasoner.isNonTangentialProperPartOf(feature015, feature007));
        assertFalse("f15-f8", reasoner.isNonTangentialProperPartOf(feature015, feature008));
        assertTrue("f15-f9", reasoner.isNonTangentialProperPartOf(feature015, feature009));
        assertFalse("f15-f10", reasoner.isNonTangentialProperPartOf(feature015, feature010));
        assertFalse("f15-f11", reasoner.isNonTangentialProperPartOf(feature015, feature011));
        assertFalse("f15-f12", reasoner.isNonTangentialProperPartOf(feature015, feature012));
        assertFalse("f15-f13", reasoner.isNonTangentialProperPartOf(feature015, feature013));
        assertFalse("f15-f14", reasoner.isNonTangentialProperPartOf(feature015, feature014));
        assertFalse("f15-f15", reasoner.isNonTangentialProperPartOf(feature015, feature015));
    }

    @Test
    public void testGetIndividualsNonTangentialProperPartOf() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0591)");

        // -- same as feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0591)");

        // -- TPP of feature010/11, but not of feature006/7
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8016 51.0589)");

        // -- off
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.7983 51.0591)");

        // -- inside feature010/11, but not TPP
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005, "POINT(13.8015 51.0583)");

        // line strings
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- same as feature006
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- inside but not TPP
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8009 51.0588,13.8003 51.0585,13.8009 51.0583)");

        // -- off
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "LINESTRING(13.8025 51.0577,13.8036 51.0579," +
                        "13.8029 51.0574,13.8040 51.0577," +
                        "13.8032 51.0572,13.8043 51.0574)");

        // areas
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- same as feature010
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- TPP
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.8000 51.0585,13.8008 51.0581," +
                        "13.8007 51.0578,13.7996 51.0583,13.8000 51.0585))");

        // -- NTPP, contains feature005
        OWLIndividual feature013 = i("feature13");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.8011 51.0585,13.8016 51.0586," +
                        "13.8018 51.0582,13.8013 51.0581,13.8011 51.0585))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7991 51.0602,13.7991 51.0597," +
                        "13.7999 51.0599,13.7991 51.0602))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature001)
                        .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));
        assertFalse("f1-f10", result.contains(feature010));
        assertFalse("f1-f11", result.contains(feature011));
        assertFalse("f1-f12", result.contains(feature012));
        assertFalse("f1-f13", result.contains(feature013));
        assertFalse("f1-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature002)
                        .collect(Collectors.toSet());
        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));
        assertFalse("f2-f10", result.contains(feature010));
        assertFalse("f2-f11", result.contains(feature011));
        assertFalse("f2-f12", result.contains(feature012));
        assertFalse("f2-f13", result.contains(feature013));
        assertFalse("f2-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertFalse("f3-f11", result.contains(feature011));
        assertFalse("f3-f12", result.contains(feature012));
        assertFalse("f3-f13", result.contains(feature013));
        assertFalse("f3-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature004)
                        .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertFalse("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));
        assertFalse("f4-f12", result.contains(feature012));
        assertFalse("f4-f13", result.contains(feature013));
        assertFalse("f4-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature005)
                        .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertFalse("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertFalse("f5-f11", result.contains(feature011));
        assertFalse("f5-f12", result.contains(feature012));
        assertFalse("f5-f13", result.contains(feature013));
        assertFalse("f5-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature006)
                        .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertTrue("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertFalse("f6-f11", result.contains(feature011));
        assertFalse("f6-f12", result.contains(feature012));
        assertFalse("f6-f13", result.contains(feature013));
        assertFalse("f6-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature007)
                        .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertTrue("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertFalse("f7-f10", result.contains(feature010));
        assertFalse("f7-f11", result.contains(feature011));
        assertFalse("f7-f12", result.contains(feature012));
        assertFalse("f7-f13", result.contains(feature013));
        assertFalse("f7-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature008)
                        .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertFalse("f8-f8", result.contains(feature008));
        assertFalse("f8-f9", result.contains(feature009));
        assertFalse("f8-f10", result.contains(feature010));
        assertFalse("f8-f11", result.contains(feature011));
        assertFalse("f8-f12", result.contains(feature012));
        assertFalse("f8-f13", result.contains(feature013));
        assertFalse("f8-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature009)
                        .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertFalse("f9-f8", result.contains(feature008));
        assertFalse("f9-f9", result.contains(feature009));
        assertFalse("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));
        assertFalse("f9-f12", result.contains(feature012));
        assertFalse("f9-f13", result.contains(feature013));
        assertFalse("f9-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature010)
                        .collect(Collectors.toSet());

        assertFalse("f10-f1", result.contains(feature001));
        assertFalse("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertTrue("f10-f5", result.contains(feature005));
        assertFalse("f10-f6", result.contains(feature006));
        assertFalse("f10-f7", result.contains(feature007));
        assertTrue("f10-f8", result.contains(feature008));
        assertFalse("f10-f9", result.contains(feature009));
        assertFalse("f10-f10", result.contains(feature010));
        assertFalse("f10-f11", result.contains(feature011));
        assertFalse("f10-f12", result.contains(feature012));
        assertTrue("f10-f13", result.contains(feature013));
        assertFalse("f10-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature011)
                        .collect(Collectors.toSet());

        assertFalse("f11-f1", result.contains(feature001));
        assertFalse("f11-f2", result.contains(feature002));
        assertFalse("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertTrue("f11-f5", result.contains(feature005));
        assertFalse("f11-f6", result.contains(feature006));
        assertFalse("f11-f7", result.contains(feature007));
        assertTrue("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertFalse("f11-f10", result.contains(feature010));
        assertFalse("f11-f11", result.contains(feature011));
        assertFalse("f11-f12", result.contains(feature012));
        assertTrue("f11-f13", result.contains(feature013));
        assertFalse("f11-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature012)
                        .collect(Collectors.toSet());

        assertFalse("f12-f1", result.contains(feature001));
        assertFalse("f12-f2", result.contains(feature002));
        assertFalse("f12-f3", result.contains(feature003));
        assertFalse("f12-f4", result.contains(feature004));
        assertFalse("f12-f5", result.contains(feature005));
        assertFalse("f12-f6", result.contains(feature006));
        assertFalse("f12-f7", result.contains(feature007));
        assertFalse("f12-f8", result.contains(feature008));
        assertFalse("f12-f9", result.contains(feature009));
        assertFalse("f12-f10", result.contains(feature010));
        assertFalse("f12-f11", result.contains(feature011));
        assertFalse("f12-f12", result.contains(feature012));
        assertFalse("f12-f13", result.contains(feature013));
        assertFalse("f12-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsNonTangentialProperPartOf(feature013)
                        .collect(Collectors.toSet());

        assertFalse("f13-f1", result.contains(feature001));
        assertFalse("f13-f2", result.contains(feature002));
        assertFalse("f13-f3", result.contains(feature003));
        assertFalse("f13-f4", result.contains(feature004));
        assertTrue("f13-f5", result.contains(feature005));
        assertFalse("f13-f6", result.contains(feature006));
        assertFalse("f13-f7", result.contains(feature007));
        assertFalse("f13-f8", result.contains(feature008));
        assertFalse("f13-f9", result.contains(feature009));
        assertFalse("f13-f10", result.contains(feature010));
        assertFalse("f13-f11", result.contains(feature011));
        assertFalse("f13-f12", result.contains(feature012));
        assertFalse("f13-f13", result.contains(feature013));
        assertFalse("f13-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsTangentialProperPartOf(feature014)
                        .collect(Collectors.toSet());

        assertFalse("f14-f1", result.contains(feature001));
        assertFalse("f14-f2", result.contains(feature002));
        assertFalse("f14-f3", result.contains(feature003));
        assertFalse("f14-f4", result.contains(feature004));
        assertFalse("f14-f5", result.contains(feature005));
        assertFalse("f14-f6", result.contains(feature006));
        assertFalse("f14-f7", result.contains(feature007));
        assertFalse("f14-f8", result.contains(feature008));
        assertFalse("f14-f9", result.contains(feature009));
        assertFalse("f14-f10", result.contains(feature010));
        assertFalse("f14-f11", result.contains(feature011));
        assertFalse("f14-f12", result.contains(feature012));
        assertFalse("f14-f13", result.contains(feature013));
        assertFalse("f14-f14", result.contains(feature014));
    }

    @Test
    public void testIsSpatiallyIdenticalWith() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0591)");

        // -- identical with feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0591)");

        // -- not identical with feature001
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8016 51.0589)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- identical with feature004
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- identical with feature004
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8009 51.0586,13.8016 51.0589,13.8011 51.0591)");

        // -- not identical with feature004
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8009 51.0586,13.8016 51.0589,13.8011 51.0591,13.8012 51.0587)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- same as feature008
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- same as feature008
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8021 51.0582,13.8007 51.0578," +
                        "13.7996 51.0583,13.7999 51.0591,13.8011 51.0591," +
                        "13.8016 51.0589,13.8021 51.0582))");

        // -- not same as feature008
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8011 51.0585,13.8016 51.0586," +
                        "13.8018 51.0582,13.8013 51.0581,13.8011 51.0585))");

        // -- not same as feature008
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.7991 51.0602,13.7991 51.0597," +
                        "13.7999 51.0599,13.7991 51.0602))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertTrue("f1-f1", reasoner.isSpatiallyIdenticalWith(feature001, feature001));
        assertTrue("f1-f2", reasoner.isSpatiallyIdenticalWith(feature001, feature002));
        assertFalse("f1-f3", reasoner.isSpatiallyIdenticalWith(feature001, feature003));
        assertFalse("f1-f4", reasoner.isSpatiallyIdenticalWith(feature001, feature004));
        assertFalse("f1-f5", reasoner.isSpatiallyIdenticalWith(feature001, feature005));
        assertFalse("f1-f6", reasoner.isSpatiallyIdenticalWith(feature001, feature006));
        assertFalse("f1-f7", reasoner.isSpatiallyIdenticalWith(feature001, feature007));
        assertFalse("f1-f8", reasoner.isSpatiallyIdenticalWith(feature001, feature008));
        assertFalse("f1-f9", reasoner.isSpatiallyIdenticalWith(feature001, feature009));
        assertFalse("f1-f10", reasoner.isSpatiallyIdenticalWith(feature001, feature010));
        assertFalse("f1-f11", reasoner.isSpatiallyIdenticalWith(feature001, feature011));
        assertFalse("f1-f12", reasoner.isSpatiallyIdenticalWith(feature001, feature012));

        assertTrue("f2-f1", reasoner.isSpatiallyIdenticalWith(feature002, feature001));
        assertTrue("f2-f2", reasoner.isSpatiallyIdenticalWith(feature002, feature002));
        assertFalse("f2-f3", reasoner.isSpatiallyIdenticalWith(feature002, feature003));
        assertFalse("f2-f4", reasoner.isSpatiallyIdenticalWith(feature002, feature004));
        assertFalse("f2-f5", reasoner.isSpatiallyIdenticalWith(feature002, feature005));
        assertFalse("f2-f6", reasoner.isSpatiallyIdenticalWith(feature002, feature006));
        assertFalse("f2-f7", reasoner.isSpatiallyIdenticalWith(feature002, feature007));
        assertFalse("f2-f8", reasoner.isSpatiallyIdenticalWith(feature002, feature008));
        assertFalse("f2-f9", reasoner.isSpatiallyIdenticalWith(feature002, feature009));
        assertFalse("f2-f10", reasoner.isSpatiallyIdenticalWith(feature002, feature010));
        assertFalse("f2-f11", reasoner.isSpatiallyIdenticalWith(feature002, feature011));
        assertFalse("f2-f12", reasoner.isSpatiallyIdenticalWith(feature002, feature012));

        assertFalse("f3-f1", reasoner.isSpatiallyIdenticalWith(feature003, feature001));
        assertFalse("f3-f2", reasoner.isSpatiallyIdenticalWith(feature003, feature002));
        assertTrue("f3-f3", reasoner.isSpatiallyIdenticalWith(feature003, feature003));
        assertFalse("f3-f4", reasoner.isSpatiallyIdenticalWith(feature003, feature004));
        assertFalse("f3-f5", reasoner.isSpatiallyIdenticalWith(feature003, feature005));
        assertFalse("f3-f6", reasoner.isSpatiallyIdenticalWith(feature003, feature006));
        assertFalse("f3-f7", reasoner.isSpatiallyIdenticalWith(feature003, feature007));
        assertFalse("f3-f8", reasoner.isSpatiallyIdenticalWith(feature003, feature008));
        assertFalse("f3-f9", reasoner.isSpatiallyIdenticalWith(feature003, feature009));
        assertFalse("f3-f10", reasoner.isSpatiallyIdenticalWith(feature003, feature010));
        assertFalse("f3-f11", reasoner.isSpatiallyIdenticalWith(feature003, feature011));
        assertFalse("f3-f12", reasoner.isSpatiallyIdenticalWith(feature003, feature012));

        assertFalse("f4-f1", reasoner.isSpatiallyIdenticalWith(feature004, feature001));
        assertFalse("f4-f2", reasoner.isSpatiallyIdenticalWith(feature004, feature002));
        assertFalse("f4-f3", reasoner.isSpatiallyIdenticalWith(feature004, feature003));
        assertTrue("f4-f4", reasoner.isSpatiallyIdenticalWith(feature004, feature004));
        assertTrue("f4-f5", reasoner.isSpatiallyIdenticalWith(feature004, feature005));
        assertTrue("f4-f6", reasoner.isSpatiallyIdenticalWith(feature004, feature006));
        assertFalse("f4-f7", reasoner.isSpatiallyIdenticalWith(feature004, feature007));
        assertFalse("f4-f8", reasoner.isSpatiallyIdenticalWith(feature004, feature008));
        assertFalse("f4-f9", reasoner.isSpatiallyIdenticalWith(feature004, feature009));
        assertFalse("f4-f10", reasoner.isSpatiallyIdenticalWith(feature004, feature010));
        assertFalse("f4-f11", reasoner.isSpatiallyIdenticalWith(feature004, feature011));
        assertFalse("f4-f12", reasoner.isSpatiallyIdenticalWith(feature004, feature012));

        assertFalse("f5-f1", reasoner.isSpatiallyIdenticalWith(feature005, feature001));
        assertFalse("f5-f2", reasoner.isSpatiallyIdenticalWith(feature005, feature002));
        assertFalse("f5-f3", reasoner.isSpatiallyIdenticalWith(feature005, feature003));
        assertTrue("f5-f4", reasoner.isSpatiallyIdenticalWith(feature005, feature004));
        assertTrue("f5-f5", reasoner.isSpatiallyIdenticalWith(feature005, feature005));
        assertTrue("f5-f6", reasoner.isSpatiallyIdenticalWith(feature005, feature006));
        assertFalse("f5-f7", reasoner.isSpatiallyIdenticalWith(feature005, feature007));
        assertFalse("f5-f8", reasoner.isSpatiallyIdenticalWith(feature005, feature008));
        assertFalse("f5-f9", reasoner.isSpatiallyIdenticalWith(feature005, feature009));
        assertFalse("f5-f10", reasoner.isSpatiallyIdenticalWith(feature005, feature010));
        assertFalse("f5-f11", reasoner.isSpatiallyIdenticalWith(feature005, feature011));
        assertFalse("f5-f12", reasoner.isSpatiallyIdenticalWith(feature005, feature012));

        assertFalse("f6-f1", reasoner.isSpatiallyIdenticalWith(feature006, feature001));
        assertFalse("f6-f2", reasoner.isSpatiallyIdenticalWith(feature006, feature002));
        assertFalse("f6-f3", reasoner.isSpatiallyIdenticalWith(feature006, feature003));
        assertTrue("f6-f4", reasoner.isSpatiallyIdenticalWith(feature006, feature004));
        assertTrue("f6-f5", reasoner.isSpatiallyIdenticalWith(feature006, feature005));
        assertTrue("f6-f6", reasoner.isSpatiallyIdenticalWith(feature006, feature006));
        assertFalse("f6-f7", reasoner.isSpatiallyIdenticalWith(feature006, feature007));
        assertFalse("f6-f8", reasoner.isSpatiallyIdenticalWith(feature006, feature008));
        assertFalse("f6-f9", reasoner.isSpatiallyIdenticalWith(feature006, feature009));
        assertFalse("f6-f10", reasoner.isSpatiallyIdenticalWith(feature006, feature010));
        assertFalse("f6-f11", reasoner.isSpatiallyIdenticalWith(feature006, feature011));
        assertFalse("f6-f12", reasoner.isSpatiallyIdenticalWith(feature006, feature012));

        assertFalse("f7-f1", reasoner.isSpatiallyIdenticalWith(feature007, feature001));
        assertFalse("f7-f2", reasoner.isSpatiallyIdenticalWith(feature007, feature002));
        assertFalse("f7-f3", reasoner.isSpatiallyIdenticalWith(feature007, feature003));
        assertFalse("f7-f4", reasoner.isSpatiallyIdenticalWith(feature007, feature004));
        assertFalse("f7-f5", reasoner.isSpatiallyIdenticalWith(feature007, feature005));
        assertFalse("f7-f6", reasoner.isSpatiallyIdenticalWith(feature007, feature006));
        assertTrue("f7-f7", reasoner.isSpatiallyIdenticalWith(feature007, feature007));
        assertFalse("f7-f8", reasoner.isSpatiallyIdenticalWith(feature007, feature008));
        assertFalse("f7-f9", reasoner.isSpatiallyIdenticalWith(feature007, feature009));
        assertFalse("f7-f10", reasoner.isSpatiallyIdenticalWith(feature007, feature010));
        assertFalse("f7-f11", reasoner.isSpatiallyIdenticalWith(feature007, feature011));
        assertFalse("f7-f12", reasoner.isSpatiallyIdenticalWith(feature007, feature012));

        assertFalse("f8-f1", reasoner.isSpatiallyIdenticalWith(feature008, feature001));
        assertFalse("f8-f2", reasoner.isSpatiallyIdenticalWith(feature008, feature002));
        assertFalse("f8-f3", reasoner.isSpatiallyIdenticalWith(feature008, feature003));
        assertFalse("f8-f4", reasoner.isSpatiallyIdenticalWith(feature008, feature004));
        assertFalse("f8-f5", reasoner.isSpatiallyIdenticalWith(feature008, feature005));
        assertFalse("f8-f6", reasoner.isSpatiallyIdenticalWith(feature008, feature006));
        assertFalse("f8-f7", reasoner.isSpatiallyIdenticalWith(feature008, feature007));
        assertTrue("f8-f8", reasoner.isSpatiallyIdenticalWith(feature008, feature008));
        assertTrue("f8-f9", reasoner.isSpatiallyIdenticalWith(feature008, feature009));
        assertTrue("f8-f10", reasoner.isSpatiallyIdenticalWith(feature008, feature010));
        assertFalse("f8-f11", reasoner.isSpatiallyIdenticalWith(feature008, feature011));
        assertFalse("f8-f12", reasoner.isSpatiallyIdenticalWith(feature008, feature012));

        assertFalse("f9-f1", reasoner.isSpatiallyIdenticalWith(feature009, feature001));
        assertFalse("f9-f2", reasoner.isSpatiallyIdenticalWith(feature009, feature002));
        assertFalse("f9-f3", reasoner.isSpatiallyIdenticalWith(feature009, feature003));
        assertFalse("f9-f4", reasoner.isSpatiallyIdenticalWith(feature009, feature004));
        assertFalse("f9-f5", reasoner.isSpatiallyIdenticalWith(feature009, feature005));
        assertFalse("f9-f6", reasoner.isSpatiallyIdenticalWith(feature009, feature006));
        assertFalse("f9-f7", reasoner.isSpatiallyIdenticalWith(feature009, feature007));
        assertTrue("f9-f8", reasoner.isSpatiallyIdenticalWith(feature009, feature008));
        assertTrue("f9-f9", reasoner.isSpatiallyIdenticalWith(feature009, feature009));
        assertTrue("f9-f10", reasoner.isSpatiallyIdenticalWith(feature009, feature010));
        assertFalse("f9-f11", reasoner.isSpatiallyIdenticalWith(feature009, feature011));
        assertFalse("f9-f12", reasoner.isSpatiallyIdenticalWith(feature009, feature012));

        assertFalse("f10-f1", reasoner.isSpatiallyIdenticalWith(feature010, feature001));
        assertFalse("f10-f2", reasoner.isSpatiallyIdenticalWith(feature010, feature002));
        assertFalse("f10-f3", reasoner.isSpatiallyIdenticalWith(feature010, feature003));
        assertFalse("f10-f4", reasoner.isSpatiallyIdenticalWith(feature010, feature004));
        assertFalse("f10-f5", reasoner.isSpatiallyIdenticalWith(feature010, feature005));
        assertFalse("f10-f6", reasoner.isSpatiallyIdenticalWith(feature010, feature006));
        assertFalse("f10-f7", reasoner.isSpatiallyIdenticalWith(feature010, feature007));
        assertTrue("f10-f8", reasoner.isSpatiallyIdenticalWith(feature010, feature008));
        assertTrue("f10-f9", reasoner.isSpatiallyIdenticalWith(feature010, feature009));
        assertTrue("f10-f10", reasoner.isSpatiallyIdenticalWith(feature010, feature010));
        assertFalse("f10-f11", reasoner.isSpatiallyIdenticalWith(feature010, feature011));
        assertFalse("f10-f12", reasoner.isSpatiallyIdenticalWith(feature010, feature012));

        assertFalse("f11-f1", reasoner.isSpatiallyIdenticalWith(feature011, feature001));
        assertFalse("f11-f2", reasoner.isSpatiallyIdenticalWith(feature011, feature002));
        assertFalse("f11-f3", reasoner.isSpatiallyIdenticalWith(feature011, feature003));
        assertFalse("f11-f4", reasoner.isSpatiallyIdenticalWith(feature011, feature004));
        assertFalse("f11-f5", reasoner.isSpatiallyIdenticalWith(feature011, feature005));
        assertFalse("f11-f6", reasoner.isSpatiallyIdenticalWith(feature011, feature006));
        assertFalse("f11-f7", reasoner.isSpatiallyIdenticalWith(feature011, feature007));
        assertFalse("f11-f8", reasoner.isSpatiallyIdenticalWith(feature011, feature008));
        assertFalse("f11-f9", reasoner.isSpatiallyIdenticalWith(feature011, feature009));
        assertFalse("f11-f10", reasoner.isSpatiallyIdenticalWith(feature011, feature010));
        assertTrue("f11-f11", reasoner.isSpatiallyIdenticalWith(feature011, feature011));
        assertFalse("f11-f12", reasoner.isSpatiallyIdenticalWith(feature011, feature012));

        assertFalse("f12-f1", reasoner.isSpatiallyIdenticalWith(feature012, feature001));
        assertFalse("f12-f2", reasoner.isSpatiallyIdenticalWith(feature012, feature002));
        assertFalse("f12-f3", reasoner.isSpatiallyIdenticalWith(feature012, feature003));
        assertFalse("f12-f4", reasoner.isSpatiallyIdenticalWith(feature012, feature004));
        assertFalse("f12-f5", reasoner.isSpatiallyIdenticalWith(feature012, feature005));
        assertFalse("f12-f6", reasoner.isSpatiallyIdenticalWith(feature012, feature006));
        assertFalse("f12-f7", reasoner.isSpatiallyIdenticalWith(feature012, feature007));
        assertFalse("f12-f8", reasoner.isSpatiallyIdenticalWith(feature012, feature008));
        assertFalse("f12-f9", reasoner.isSpatiallyIdenticalWith(feature012, feature009));
        assertFalse("f12-f10", reasoner.isSpatiallyIdenticalWith(feature012, feature010));
        assertFalse("f12-f11", reasoner.isSpatiallyIdenticalWith(feature012, feature011));
        assertTrue("f12-f12", reasoner.isSpatiallyIdenticalWith(feature012, feature012));
    }

    @Test
    public void testGetIndividualsSpatiallyIdenticalWith() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0591)");

        // -- identical with feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0591)");

        // -- not identical with feature001
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8016 51.0589)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- identical with feature004
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- identical with feature004
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8009 51.0586,13.8016 51.0589,13.8011 51.0591)");

        // -- not identical with feature004
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8009 51.0586,13.8016 51.0589,13.8011 51.0591,13.8012 51.0587)");

        // areas
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- same as feature008
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- same as feature008
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8021 51.0582,13.8007 51.0578," +
                        "13.7996 51.0583,13.7999 51.0591,13.8011 51.0591," +
                        "13.8016 51.0589,13.8021 51.0582))");

        // -- not same as feature008
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8011 51.0585,13.8016 51.0586," +
                        "13.8018 51.0582,13.8013 51.0581,13.8011 51.0585))");

        // -- not same as feature008
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.7991 51.0602,13.7991 51.0597," +
                        "13.7999 51.0599,13.7991 51.0602))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature001)
                        .collect(Collectors.toSet());

        assertTrue("f1-f1", result.contains(feature001));
        assertTrue("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));
        assertFalse("f1-f10", result.contains(feature010));
        assertFalse("f1-f11", result.contains(feature011));
        assertFalse("f1-f12", result.contains(feature012));

        result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature002)
                        .collect(Collectors.toSet());

        assertTrue("f2-f1", result.contains(feature001));
        assertTrue("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));
        assertFalse("f2-f10", result.contains(feature010));
        assertFalse("f2-f11", result.contains(feature011));
        assertFalse("f2-f12", result.contains(feature012));

        result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertTrue("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertFalse("f3-f11", result.contains(feature011));
        assertFalse("f3-f12", result.contains(feature012));

        result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature004)
                        .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertTrue("f4-f4", result.contains(feature004));
        assertTrue("f4-f5", result.contains(feature005));
        assertTrue("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));
        assertFalse("f4-f12", result.contains(feature012));

        result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature005)
                        .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertTrue("f5-f4", result.contains(feature004));
        assertTrue("f5-f5", result.contains(feature005));
        assertTrue("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertFalse("f5-f11", result.contains(feature011));
        assertFalse("f5-f12", result.contains(feature012));

        result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature006)
                        .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertTrue("f6-f4", result.contains(feature004));
        assertTrue("f6-f5", result.contains(feature005));
        assertTrue("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertFalse("f6-f11", result.contains(feature011));
        assertFalse("f6-f12", result.contains(feature012));

        result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature007)
                        .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertTrue("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertFalse("f7-f10", result.contains(feature010));
        assertFalse("f7-f11", result.contains(feature011));
        assertFalse("f7-f12", result.contains(feature012));

        result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature008)
                        .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertTrue("f8-f8", result.contains(feature008));
        assertTrue("f8-f9", result.contains(feature009));
        assertTrue("f8-f10", result.contains(feature010));
        assertFalse("f8-f11", result.contains(feature011));
        assertFalse("f8-f12", result.contains(feature012));

        result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature009)
                        .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertTrue("f9-f8", result.contains(feature008));
        assertTrue("f9-f9", result.contains(feature009));
        assertTrue("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));
        assertFalse("f9-f12", result.contains(feature012));

        result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature010)
                        .collect(Collectors.toSet());

        assertFalse("f10-f1", result.contains(feature001));
        assertFalse("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertFalse("f10-f6", result.contains(feature006));
        assertFalse("f10-f7", result.contains(feature007));
        assertTrue("f10-f8", result.contains(feature008));
        assertTrue("f10-f9", result.contains(feature009));
        assertTrue("f10-f10", result.contains(feature010));
        assertFalse("f10-f11", result.contains(feature011));
        assertFalse("f10-f12", result.contains(feature012));

        result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature011)
                        .collect(Collectors.toSet());

        assertFalse("f11-f1", result.contains(feature001));
        assertFalse("f11-f2", result.contains(feature002));
        assertFalse("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertFalse("f11-f5", result.contains(feature005));
        assertFalse("f11-f6", result.contains(feature006));
        assertFalse("f11-f7", result.contains(feature007));
        assertFalse("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertFalse("f11-f10", result.contains(feature010));
        assertTrue("f11-f11", result.contains(feature011));
        assertFalse("f11-f12", result.contains(feature012));

        result =
                reasoner.getIndividualsSpatiallyIdenticalWith(feature012)
                        .collect(Collectors.toSet());

        assertFalse("f12-f1", result.contains(feature001));
        assertFalse("f12-f2", result.contains(feature002));
        assertFalse("f12-f3", result.contains(feature003));
        assertFalse("f12-f4", result.contains(feature004));
        assertFalse("f12-f5", result.contains(feature005));
        assertFalse("f12-f6", result.contains(feature006));
        assertFalse("f12-f7", result.contains(feature007));
        assertFalse("f12-f8", result.contains(feature008));
        assertFalse("f12-f9", result.contains(feature009));
        assertFalse("f12-f10", result.contains(feature010));
        assertFalse("f12-f11", result.contains(feature011));
        assertTrue("f12-f12", result.contains(feature012));
    }

    @Test
    public void testGetIndividualsHavingTangentialProperPart() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0591)");

        // -- same as feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0591)");

        // -- TPP of feature010/11, but not of feature006/7
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8016 51.0589)");

        // -- off
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.7983 51.0591)");

        // -- inside feature010/11, but not TPP
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005, "POINT(13.8015 51.0583)");

        // line strings
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- same as feature006
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- inside but not TPP
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8009 51.0588,13.8003 51.0585,13.8009 51.0583)");

        // -- off
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "LINESTRING(13.8025 51.0577,13.8036 51.0579," +
                        "13.8029 51.0574,13.8040 51.0577," +
                        "13.8032 51.0572,13.8043 51.0574)");

        // areas
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- same as feature010
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- TPP
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.8000 51.0585,13.8008 51.0581," +
                        "13.8007 51.0578,13.7996 51.0583,13.8000 51.0585))");

        // -- NTPP, contains feature005
        OWLIndividual feature013 = i("feature13");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.8011 51.0585,13.8016 51.0586," +
                        "13.8018 51.0582,13.8013 51.0581,13.8011 51.0585))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7991 51.0602,13.7991 51.0597," +
                        "13.7999 51.0599,13.7991 51.0602))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsHavingTangentialProperPart(feature001)
                        .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertTrue("f1-f6", result.contains(feature006));
        assertTrue("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));
        assertTrue("f1-f10", result.contains(feature010));
        assertTrue("f1-f11", result.contains(feature011));
        assertFalse("f1-f12", result.contains(feature012));
        assertFalse("f1-f13", result.contains(feature013));
        assertFalse("f1-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature002)
                        .collect(Collectors.toSet());

        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertTrue("f2-f6", result.contains(feature006));
        assertTrue("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));
        assertTrue("f2-f10", result.contains(feature010));
        assertTrue("f2-f11", result.contains(feature011));
        assertFalse("f2-f12", result.contains(feature012));
        assertFalse("f2-f13", result.contains(feature013));
        assertFalse("f2-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));
        assertTrue("f3-f10", result.contains(feature010));
        assertTrue("f3-f11", result.contains(feature011));
        assertFalse("f3-f12", result.contains(feature012));
        assertFalse("f3-f13", result.contains(feature013));
        assertFalse("f3-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature004)
                        .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertFalse("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));
        assertFalse("f4-f12", result.contains(feature012));
        assertFalse("f4-f13", result.contains(feature013));
        assertFalse("f4-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature005)
                        .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertFalse("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertFalse("f5-f11", result.contains(feature011));
        assertFalse("f5-f12", result.contains(feature012));
        assertFalse("f5-f13", result.contains(feature013));
        assertFalse("f5-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature006)
                        .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));
        assertTrue("f6-f10", result.contains(feature010));
        assertTrue("f6-f11", result.contains(feature011));
        assertFalse("f6-f12", result.contains(feature012));
        assertFalse("f6-f13", result.contains(feature013));
        assertFalse("f6-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature007)
                        .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertTrue("f7-f10", result.contains(feature010));
        assertTrue("f7-f11", result.contains(feature011));
        assertFalse("f7-f12", result.contains(feature012));
        assertFalse("f7-f13", result.contains(feature013));
        assertFalse("f7-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature008)
                        .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertFalse("f8-f8", result.contains(feature008));
        assertFalse("f8-f9", result.contains(feature009));
        assertFalse("f8-f10", result.contains(feature010));
        assertFalse("f8-f11", result.contains(feature011));
        assertFalse("f8-f12", result.contains(feature012));
        assertFalse("f8-f13", result.contains(feature013));
        assertFalse("f8-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature009)
                        .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertFalse("f9-f8", result.contains(feature008));
        assertFalse("f9-f9", result.contains(feature009));
        assertFalse("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));
        assertFalse("f9-f12", result.contains(feature012));
        assertFalse("f9-f13", result.contains(feature013));
        assertFalse("f9-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature010)
                        .collect(Collectors.toSet());

        assertFalse("f10-f1", result.contains(feature001));
        assertFalse("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertFalse("f10-f6", result.contains(feature006));
        assertFalse("f10-f7", result.contains(feature007));
        assertFalse("f10-f8", result.contains(feature008));
        assertFalse("f10-f9", result.contains(feature009));
        assertFalse("f10-f10", result.contains(feature010));
        assertFalse("f10-f11", result.contains(feature011));
        assertFalse("f10-f12", result.contains(feature012));
        assertFalse("f10-f13", result.contains(feature013));
        assertFalse("f10-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature011)
                        .collect(Collectors.toSet());

        assertFalse("f11-f1", result.contains(feature001));
        assertFalse("f11-f2", result.contains(feature002));
        assertFalse("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertFalse("f11-f5", result.contains(feature005));
        assertFalse("f11-f6", result.contains(feature006));
        assertFalse("f11-f7", result.contains(feature007));
        assertFalse("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertFalse("f11-f10", result.contains(feature010));
        assertFalse("f11-f11", result.contains(feature011));
        assertFalse("f11-f12", result.contains(feature012));
        assertFalse("f11-f13", result.contains(feature013));
        assertFalse("f11-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature012)
                        .collect(Collectors.toSet());

        assertFalse("f12-f1", result.contains(feature001));
        assertFalse("f12-f2", result.contains(feature002));
        assertFalse("f12-f3", result.contains(feature003));
        assertFalse("f12-f4", result.contains(feature004));
        assertFalse("f12-f5", result.contains(feature005));
        assertFalse("f12-f6", result.contains(feature006));
        assertFalse("f12-f7", result.contains(feature007));
        assertFalse("f12-f8", result.contains(feature008));
        assertFalse("f12-f9", result.contains(feature009));
        assertTrue("f12-f10", result.contains(feature010));
        assertTrue("f12-f11", result.contains(feature011));
        assertFalse("f12-f12", result.contains(feature012));
        assertFalse("f12-f13", result.contains(feature013));
        assertFalse("f12-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature013)
                        .collect(Collectors.toSet());

        assertFalse("f13-f1", result.contains(feature001));
        assertFalse("f13-f2", result.contains(feature002));
        assertFalse("f13-f3", result.contains(feature003));
        assertFalse("f13-f4", result.contains(feature004));
        assertFalse("f13-f5", result.contains(feature005));
        assertFalse("f13-f6", result.contains(feature006));
        assertFalse("f13-f7", result.contains(feature007));
        assertFalse("f13-f8", result.contains(feature008));
        assertFalse("f13-f9", result.contains(feature009));
        assertFalse("f13-f10", result.contains(feature010));
        assertFalse("f13-f11", result.contains(feature011));
        assertFalse("f13-f12", result.contains(feature012));
        assertFalse("f13-f13", result.contains(feature013));
        assertFalse("f13-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingTangentialProperPart(feature014)
                        .collect(Collectors.toSet());

        assertFalse("f14-f1", result.contains(feature001));
        assertFalse("f14-f2", result.contains(feature002));
        assertFalse("f14-f3", result.contains(feature003));
        assertFalse("f14-f4", result.contains(feature004));
        assertFalse("f14-f5", result.contains(feature005));
        assertFalse("f14-f6", result.contains(feature006));
        assertFalse("f14-f7", result.contains(feature007));
        assertFalse("f14-f8", result.contains(feature008));
        assertFalse("f14-f9", result.contains(feature009));
        assertFalse("f14-f10", result.contains(feature010));
        assertFalse("f14-f11", result.contains(feature011));
        assertFalse("f14-f12", result.contains(feature012));
        assertFalse("f14-f13", result.contains(feature013));
        assertFalse("f14-f14", result.contains(feature014));
    }

    @Test
    public void testGetIndividualsHavingNonTangentialProperPart() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8011 51.0591)");

        // -- same as feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8011 51.0591)");

        // -- TPP of feature010/11, but not of feature006/7
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8016 51.0589)");

        // -- off
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.7983 51.0591)");

        // -- inside feature010/11, but not TPP
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005, "POINT(13.8015 51.0583)");

        // line strings
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- same as feature006
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8011 51.0591,13.8016 51.0589,13.8009 51.0586)");

        // -- inside but not TPP
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8009 51.0588,13.8003 51.0585,13.8009 51.0583)");

        // -- off
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "LINESTRING(13.8025 51.0577,13.8036 51.0579," +
                        "13.8029 51.0574,13.8040 51.0577," +
                        "13.8032 51.0572,13.8043 51.0574)");

        // areas
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- same as feature010
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.8011 51.0591,13.8016 51.0589," +
                        "13.8021 51.0582,13.8007 51.0578,13.7996 51.0583," +
                        "13.7999 51.0591,13.8011 51.0591))");

        // -- TPP
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.8000 51.0585,13.8008 51.0581," +
                        "13.8007 51.0578,13.7996 51.0583,13.8000 51.0585))");

        // -- NTPP, contains feature005
        OWLIndividual feature013 = i("feature13");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.8011 51.0585,13.8016 51.0586," +
                        "13.8018 51.0582,13.8013 51.0581,13.8011 51.0585))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7991 51.0602,13.7991 51.0597," +
                        "13.7999 51.0599,13.7991 51.0602))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature001)
                        .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));
        assertFalse("f1-f10", result.contains(feature010));
        assertFalse("f1-f11", result.contains(feature011));
        assertFalse("f1-f12", result.contains(feature012));
        assertFalse("f1-f13", result.contains(feature013));
        assertFalse("f1-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature002)
                        .collect(Collectors.toSet());

        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));
        assertFalse("f2-f10", result.contains(feature010));
        assertFalse("f2-f11", result.contains(feature011));
        assertFalse("f2-f12", result.contains(feature012));
        assertFalse("f2-f13", result.contains(feature013));
        assertFalse("f2-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertTrue("f3-f6", result.contains(feature006));
        assertTrue("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertFalse("f3-f11", result.contains(feature011));
        assertFalse("f3-f12", result.contains(feature012));
        assertFalse("f3-f13", result.contains(feature013));
        assertFalse("f3-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature004)
                        .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertFalse("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));
        assertFalse("f4-f12", result.contains(feature012));
        assertFalse("f4-f13", result.contains(feature013));
        assertFalse("f4-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature005)
                        .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertFalse("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertTrue("f5-f10", result.contains(feature010));
        assertTrue("f5-f11", result.contains(feature011));
        assertFalse("f5-f12", result.contains(feature012));
        assertTrue("f5-f13", result.contains(feature013));
        assertFalse("f5-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature006)
                        .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertFalse("f6-f11", result.contains(feature011));
        assertFalse("f6-f12", result.contains(feature012));
        assertFalse("f6-f13", result.contains(feature013));
        assertFalse("f6-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature007)
                        .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertFalse("f7-f10", result.contains(feature010));
        assertFalse("f7-f11", result.contains(feature011));
        assertFalse("f7-f12", result.contains(feature012));
        assertFalse("f7-f13", result.contains(feature013));
        assertFalse("f7-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature008)
                        .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertFalse("f8-f8", result.contains(feature008));
        assertFalse("f8-f9", result.contains(feature009));
        assertTrue("f8-f10", result.contains(feature010));
        assertTrue("f8-f11", result.contains(feature011));
        assertFalse("f8-f12", result.contains(feature012));
        assertFalse("f8-f13", result.contains(feature013));
        assertFalse("f8-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature009)
                        .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertFalse("f9-f8", result.contains(feature008));
        assertFalse("f9-f9", result.contains(feature009));
        assertFalse("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));
        assertFalse("f9-f12", result.contains(feature012));
        assertFalse("f9-f13", result.contains(feature013));
        assertFalse("f9-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature010)
                        .collect(Collectors.toSet());

        assertFalse("f10-f1", result.contains(feature001));
        assertFalse("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertFalse("f10-f6", result.contains(feature006));
        assertFalse("f10-f7", result.contains(feature007));
        assertFalse("f10-f8", result.contains(feature008));
        assertFalse("f10-f9", result.contains(feature009));
        assertFalse("f10-f10", result.contains(feature010));
        assertFalse("f10-f11", result.contains(feature011));
        assertFalse("f10-f12", result.contains(feature012));
        assertFalse("f10-f13", result.contains(feature013));
        assertFalse("f10-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature011)
                        .collect(Collectors.toSet());

        assertFalse("f11-f1", result.contains(feature001));
        assertFalse("f11-f2", result.contains(feature002));
        assertFalse("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertFalse("f11-f5", result.contains(feature005));
        assertFalse("f11-f6", result.contains(feature006));
        assertFalse("f11-f7", result.contains(feature007));
        assertFalse("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertFalse("f11-f10", result.contains(feature010));
        assertFalse("f11-f11", result.contains(feature011));
        assertFalse("f11-f12", result.contains(feature012));
        assertFalse("f11-f13", result.contains(feature013));
        assertFalse("f11-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature012)
                        .collect(Collectors.toSet());

        assertFalse("f12-f1", result.contains(feature001));
        assertFalse("f12-f2", result.contains(feature002));
        assertFalse("f12-f3", result.contains(feature003));
        assertFalse("f12-f4", result.contains(feature004));
        assertFalse("f12-f5", result.contains(feature005));
        assertFalse("f12-f6", result.contains(feature006));
        assertFalse("f12-f7", result.contains(feature007));
        assertFalse("f12-f8", result.contains(feature008));
        assertFalse("f12-f9", result.contains(feature009));
        assertFalse("f12-f10", result.contains(feature010));
        assertFalse("f12-f11", result.contains(feature011));
        assertFalse("f12-f12", result.contains(feature012));
        assertFalse("f12-f13", result.contains(feature013));
        assertFalse("f12-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature013)
                        .collect(Collectors.toSet());

        assertFalse("f13-f1", result.contains(feature001));
        assertFalse("f13-f2", result.contains(feature002));
        assertFalse("f13-f3", result.contains(feature003));
        assertFalse("f13-f4", result.contains(feature004));
        assertFalse("f13-f5", result.contains(feature005));
        assertFalse("f13-f6", result.contains(feature006));
        assertFalse("f13-f7", result.contains(feature007));
        assertFalse("f13-f8", result.contains(feature008));
        assertFalse("f13-f9", result.contains(feature009));
        assertTrue("f13-f10", result.contains(feature010));
        assertTrue("f13-f11", result.contains(feature011));
        assertFalse("f13-f12", result.contains(feature012));
        assertFalse("f13-f13", result.contains(feature013));
        assertFalse("f13-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsHavingNonTangentialProperPart(feature014)
                        .collect(Collectors.toSet());

        assertFalse("f14-f1", result.contains(feature001));
        assertFalse("f14-f2", result.contains(feature002));
        assertFalse("f14-f3", result.contains(feature003));
        assertFalse("f14-f4", result.contains(feature004));
        assertFalse("f14-f5", result.contains(feature005));
        assertFalse("f14-f6", result.contains(feature006));
        assertFalse("f14-f7", result.contains(feature007));
        assertFalse("f14-f8", result.contains(feature008));
        assertFalse("f14-f9", result.contains(feature009));
        assertFalse("f14-f10", result.contains(feature010));
        assertFalse("f14-f11", result.contains(feature011));
        assertFalse("f14-f12", result.contains(feature012));
        assertFalse("f14-f13", result.contains(feature013));
        assertFalse("f14-f14", result.contains(feature014));
    }

    @Test
    public void testIsExternallyConnectedWith() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8003 51.0596)");

        // -- same as feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8003 51.0596)");

        // -- inside first area feature, EC'ed with feature005/6
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8002 51.0592)");

        // -- off
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.7976 51.0596)");

        // line strings
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8002 51.0592,13.8002 51.0588,13.8013 51.0592)");

        // -- same as feature005
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8002 51.0592,13.8002 51.0588,13.8013 51.0592)");

        // -- EC'ed with feature005/6
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8013 51.0592,13.8019 51.0585,13.8022 51.0592,13.8024 51.0585)");

        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8015 51.0574,13.8019 51.0581,13.8036 51.0571,13.8018 51.0573)");

        // areas
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8003 51.0596,13.8013 51.0592," +
                        "13.8007 51.0584,13.7989 51.0591,13.8003 51.0596))");

        // -- same as feature009
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8003 51.0596,13.8013 51.0592," +
                        "13.8007 51.0584,13.7989 51.0591,13.8003 51.0596))");

        // -- EC'ed with feature012
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7989 51.0591,13.7998 51.0591," +
                        "13.8006 51.0588,13.8007 51.0584,13.7989 51.0591))");

        // -- EC'ed with feature009/10
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.7989 51.0582,13.8007 51.0584," +
                        "13.7989 51.0591,13.7989 51.0582))");

        // -- EC'ed with feature012
        OWLIndividual feature013 = i("feature013");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.7989 51.0582,13.7991 51.0573," +
                        "13.7971 51.0582,13.7989 51.0582))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7949 51.0594,13.7962 51.0589," +
                        "13.7964 51.0595,13.7949 51.0594))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertTrue("f1-f1", reasoner.isExternallyConnectedWith(feature001, feature001));
        assertTrue("f1-f2", reasoner.isExternallyConnectedWith(feature001, feature002));
        assertFalse("f1-f3", reasoner.isExternallyConnectedWith(feature001, feature003));
        assertFalse("f1-f4", reasoner.isExternallyConnectedWith(feature001, feature004));
        assertFalse("f1-f5", reasoner.isExternallyConnectedWith(feature001, feature005));
        assertFalse("f1-f6", reasoner.isExternallyConnectedWith(feature001, feature006));
        assertFalse("f1-f7", reasoner.isExternallyConnectedWith(feature001, feature007));
        assertFalse("f1-f8", reasoner.isExternallyConnectedWith(feature001, feature008));
        assertTrue("f1-f9", reasoner.isExternallyConnectedWith(feature001, feature009));
        assertTrue("f1-f10", reasoner.isExternallyConnectedWith(feature001, feature010));
        assertFalse("f1-f11", reasoner.isExternallyConnectedWith(feature001, feature011));
        assertFalse("f1-f12", reasoner.isExternallyConnectedWith(feature001, feature012));
        assertFalse("f1-f13", reasoner.isExternallyConnectedWith(feature001, feature013));
        assertFalse("f1-f14", reasoner.isExternallyConnectedWith(feature001, feature014));

        assertTrue("f2-f1", reasoner.isExternallyConnectedWith(feature002, feature001));
        assertTrue("f2-f2", reasoner.isExternallyConnectedWith(feature002, feature002));
        assertFalse("f2-f3", reasoner.isExternallyConnectedWith(feature002, feature003));
        assertFalse("f2-f4", reasoner.isExternallyConnectedWith(feature002, feature004));
        assertFalse("f2-f5", reasoner.isExternallyConnectedWith(feature002, feature005));
        assertFalse("f2-f6", reasoner.isExternallyConnectedWith(feature002, feature006));
        assertFalse("f2-f7", reasoner.isExternallyConnectedWith(feature002, feature007));
        assertFalse("f2-f8", reasoner.isExternallyConnectedWith(feature002, feature008));
        assertTrue("f2-f9", reasoner.isExternallyConnectedWith(feature002, feature009));
        assertTrue("f2-f10", reasoner.isExternallyConnectedWith(feature002, feature010));
        assertFalse("f2-f11", reasoner.isExternallyConnectedWith(feature002, feature011));
        assertFalse("f2-f12", reasoner.isExternallyConnectedWith(feature002, feature012));
        assertFalse("f2-f13", reasoner.isExternallyConnectedWith(feature002, feature013));
        assertFalse("f2-f14", reasoner.isExternallyConnectedWith(feature002, feature014));

        assertFalse("f3-f1", reasoner.isExternallyConnectedWith(feature003, feature001));
        assertFalse("f3-f2", reasoner.isExternallyConnectedWith(feature003, feature002));
        assertTrue("f3-f3", reasoner.isExternallyConnectedWith(feature003, feature003));
        assertFalse("f3-f4", reasoner.isExternallyConnectedWith(feature003, feature004));
        assertTrue("f3-f5", reasoner.isExternallyConnectedWith(feature003, feature005));
        assertTrue("f3-f6", reasoner.isExternallyConnectedWith(feature003, feature006));
        assertFalse("f3-f7", reasoner.isExternallyConnectedWith(feature003, feature007));
        assertFalse("f3-f8", reasoner.isExternallyConnectedWith(feature003, feature008));
        assertFalse("f3-f9", reasoner.isExternallyConnectedWith(feature003, feature009));
        assertFalse("f3-f10", reasoner.isExternallyConnectedWith(feature003, feature010));
        assertFalse("f3-f11", reasoner.isExternallyConnectedWith(feature003, feature011));
        assertFalse("f3-f12", reasoner.isExternallyConnectedWith(feature003, feature012));
        assertFalse("f3-f13", reasoner.isExternallyConnectedWith(feature003, feature013));
        assertFalse("f3-f14", reasoner.isExternallyConnectedWith(feature003, feature014));

        assertFalse("f4-f1", reasoner.isExternallyConnectedWith(feature004, feature001));
        assertFalse("f4-f2", reasoner.isExternallyConnectedWith(feature004, feature002));
        assertFalse("f4-f3", reasoner.isExternallyConnectedWith(feature004, feature003));
        assertTrue("f4-f4", reasoner.isExternallyConnectedWith(feature004, feature004));
        assertFalse("f4-f5", reasoner.isExternallyConnectedWith(feature004, feature005));
        assertFalse("f4-f6", reasoner.isExternallyConnectedWith(feature004, feature006));
        assertFalse("f4-f7", reasoner.isExternallyConnectedWith(feature004, feature007));
        assertFalse("f4-f8", reasoner.isExternallyConnectedWith(feature004, feature008));
        assertFalse("f4-f9", reasoner.isExternallyConnectedWith(feature004, feature009));
        assertFalse("f4-f10", reasoner.isExternallyConnectedWith(feature004, feature010));
        assertFalse("f4-f11", reasoner.isExternallyConnectedWith(feature004, feature011));
        assertFalse("f4-f12", reasoner.isExternallyConnectedWith(feature004, feature012));
        assertFalse("f4-f13", reasoner.isExternallyConnectedWith(feature004, feature013));
        assertFalse("f4-f14", reasoner.isExternallyConnectedWith(feature004, feature014));

        assertFalse("f5-f1", reasoner.isExternallyConnectedWith(feature005, feature001));
        assertFalse("f5-f2", reasoner.isExternallyConnectedWith(feature005, feature002));
        assertTrue("f5-f3", reasoner.isExternallyConnectedWith(feature005, feature003));
        assertFalse("f5-f4", reasoner.isExternallyConnectedWith(feature005, feature004));
        assertFalse("f5-f5", reasoner.isExternallyConnectedWith(feature005, feature005));
        assertFalse("f5-f6", reasoner.isExternallyConnectedWith(feature005, feature006));
        assertTrue("f5-f7", reasoner.isExternallyConnectedWith(feature005, feature007));
        assertFalse("f5-f8", reasoner.isExternallyConnectedWith(feature005, feature008));
        assertFalse("f5-f9", reasoner.isExternallyConnectedWith(feature005, feature009));
        assertFalse("f5-f10", reasoner.isExternallyConnectedWith(feature005, feature010));
        assertFalse("f5-f11", reasoner.isExternallyConnectedWith(feature005, feature011));
        assertFalse("f5-f12", reasoner.isExternallyConnectedWith(feature005, feature012));
        assertFalse("f5-f13", reasoner.isExternallyConnectedWith(feature005, feature013));
        assertFalse("f5-f14", reasoner.isExternallyConnectedWith(feature005, feature014));

        assertFalse("f6-f1", reasoner.isExternallyConnectedWith(feature006, feature001));
        assertFalse("f6-f2", reasoner.isExternallyConnectedWith(feature006, feature002));
        assertTrue("f6-f3", reasoner.isExternallyConnectedWith(feature006, feature003));
        assertFalse("f6-f4", reasoner.isExternallyConnectedWith(feature006, feature004));
        assertFalse("f6-f5", reasoner.isExternallyConnectedWith(feature006, feature005));
        assertFalse("f6-f6", reasoner.isExternallyConnectedWith(feature006, feature006));
        assertTrue("f6-f7", reasoner.isExternallyConnectedWith(feature006, feature007));
        assertFalse("f6-f8", reasoner.isExternallyConnectedWith(feature006, feature008));
        assertFalse("f6-f9", reasoner.isExternallyConnectedWith(feature006, feature009));
        assertFalse("f6-f10", reasoner.isExternallyConnectedWith(feature006, feature010));
        assertFalse("f6-f11", reasoner.isExternallyConnectedWith(feature006, feature011));
        assertFalse("f6-f12", reasoner.isExternallyConnectedWith(feature006, feature012));
        assertFalse("f6-f13", reasoner.isExternallyConnectedWith(feature006, feature013));
        assertFalse("f6-f14", reasoner.isExternallyConnectedWith(feature006, feature014));

        assertFalse("f7-f1", reasoner.isExternallyConnectedWith(feature007, feature001));
        assertFalse("f7-f2", reasoner.isExternallyConnectedWith(feature007, feature002));
        assertFalse("f7-f3", reasoner.isExternallyConnectedWith(feature007, feature003));
        assertFalse("f7-f4", reasoner.isExternallyConnectedWith(feature007, feature004));
        assertTrue("f7-f5", reasoner.isExternallyConnectedWith(feature007, feature005));
        assertTrue("f7-f6", reasoner.isExternallyConnectedWith(feature007, feature006));
        assertFalse("f7-f7", reasoner.isExternallyConnectedWith(feature007, feature007));
        assertFalse("f7-f8", reasoner.isExternallyConnectedWith(feature007, feature008));
        assertTrue("f7-f9", reasoner.isExternallyConnectedWith(feature007, feature009));
        assertTrue("f7-f10", reasoner.isExternallyConnectedWith(feature007, feature010));
        assertFalse("f7-f11", reasoner.isExternallyConnectedWith(feature007, feature011));
        assertFalse("f7-f12", reasoner.isExternallyConnectedWith(feature007, feature012));
        assertFalse("f7-f13", reasoner.isExternallyConnectedWith(feature007, feature013));
        assertFalse("f7-f14", reasoner.isExternallyConnectedWith(feature007, feature014));

        assertFalse("f8-f1", reasoner.isExternallyConnectedWith(feature008, feature001));
        assertFalse("f8-f2", reasoner.isExternallyConnectedWith(feature008, feature002));
        assertFalse("f8-f3", reasoner.isExternallyConnectedWith(feature008, feature003));
        assertFalse("f8-f4", reasoner.isExternallyConnectedWith(feature008, feature004));
        assertFalse("f8-f5", reasoner.isExternallyConnectedWith(feature008, feature005));
        assertFalse("f8-f6", reasoner.isExternallyConnectedWith(feature008, feature006));
        assertFalse("f8-f7", reasoner.isExternallyConnectedWith(feature008, feature007));
        assertFalse("f8-f8", reasoner.isExternallyConnectedWith(feature008, feature008));
        assertFalse("f8-f9", reasoner.isExternallyConnectedWith(feature008, feature009));
        assertFalse("f8-f10", reasoner.isExternallyConnectedWith(feature008, feature010));
        assertFalse("f8-f11", reasoner.isExternallyConnectedWith(feature008, feature011));
        assertFalse("f8-f12", reasoner.isExternallyConnectedWith(feature008, feature012));
        assertFalse("f8-f13", reasoner.isExternallyConnectedWith(feature008, feature013));
        assertFalse("f8-f14", reasoner.isExternallyConnectedWith(feature008, feature014));

        assertTrue("f9-f1", reasoner.isExternallyConnectedWith(feature009, feature001));
        assertTrue("f9-f2", reasoner.isExternallyConnectedWith(feature009, feature002));
        assertFalse("f9-f3", reasoner.isExternallyConnectedWith(feature009, feature003));
        assertFalse("f9-f4", reasoner.isExternallyConnectedWith(feature009, feature004));
        assertFalse("f9-f5", reasoner.isExternallyConnectedWith(feature009, feature005));
        assertFalse("f9-f6", reasoner.isExternallyConnectedWith(feature009, feature006));
        assertTrue("f9-f7", reasoner.isExternallyConnectedWith(feature009, feature007));
        assertFalse("f9-f8", reasoner.isExternallyConnectedWith(feature009, feature008));
        assertFalse("f9-f9", reasoner.isExternallyConnectedWith(feature009, feature009));
        assertFalse("f9-f10", reasoner.isExternallyConnectedWith(feature009, feature010));
        assertFalse("f9-f11", reasoner.isExternallyConnectedWith(feature009, feature011));
        assertTrue("f9-f12", reasoner.isExternallyConnectedWith(feature009, feature012));
        assertFalse("f9-f13", reasoner.isExternallyConnectedWith(feature009, feature013));
        assertFalse("f9-f14", reasoner.isExternallyConnectedWith(feature009, feature014));

        assertTrue("f10-f1", reasoner.isExternallyConnectedWith(feature010, feature001));
        assertTrue("f10-f2", reasoner.isExternallyConnectedWith(feature010, feature002));
        assertFalse("f10-f3", reasoner.isExternallyConnectedWith(feature010, feature003));
        assertFalse("f10-f4", reasoner.isExternallyConnectedWith(feature010, feature004));
        assertFalse("f10-f5", reasoner.isExternallyConnectedWith(feature010, feature005));
        assertFalse("f10-f6", reasoner.isExternallyConnectedWith(feature010, feature006));
        assertTrue("f10-f7", reasoner.isExternallyConnectedWith(feature010, feature007));
        assertFalse("f10-f8", reasoner.isExternallyConnectedWith(feature010, feature008));
        assertFalse("f10-f9", reasoner.isExternallyConnectedWith(feature010, feature009));
        assertFalse("f10-f10", reasoner.isExternallyConnectedWith(feature010, feature010));
        assertFalse("f10-f11", reasoner.isExternallyConnectedWith(feature010, feature011));
        assertTrue("f10-f12", reasoner.isExternallyConnectedWith(feature010, feature012));
        assertFalse("f10-f13", reasoner.isExternallyConnectedWith(feature010, feature013));
        assertFalse("f10-f14", reasoner.isExternallyConnectedWith(feature010, feature014));

        assertFalse("f11-f1", reasoner.isExternallyConnectedWith(feature011, feature001));
        assertFalse("f11-f2", reasoner.isExternallyConnectedWith(feature011, feature002));
        assertFalse("f11-f3", reasoner.isExternallyConnectedWith(feature011, feature003));
        assertFalse("f11-f4", reasoner.isExternallyConnectedWith(feature011, feature004));
        assertFalse("f11-f5", reasoner.isExternallyConnectedWith(feature011, feature005));
        assertFalse("f11-f6", reasoner.isExternallyConnectedWith(feature011, feature006));
        assertFalse("f11-f7", reasoner.isExternallyConnectedWith(feature011, feature007));
        assertFalse("f11-f8", reasoner.isExternallyConnectedWith(feature011, feature008));
        assertFalse("f11-f9", reasoner.isExternallyConnectedWith(feature011, feature009));
        assertFalse("f11-f10", reasoner.isExternallyConnectedWith(feature011, feature010));
        assertFalse("f11-f11", reasoner.isExternallyConnectedWith(feature011, feature011));
        assertTrue("f11-f12", reasoner.isExternallyConnectedWith(feature011, feature012));
        assertFalse("f11-f13", reasoner.isExternallyConnectedWith(feature011, feature013));
        assertFalse("f11-f14", reasoner.isExternallyConnectedWith(feature011, feature014));

        assertFalse("f12-f1", reasoner.isExternallyConnectedWith(feature012, feature001));
        assertFalse("f12-f2", reasoner.isExternallyConnectedWith(feature012, feature002));
        assertFalse("f12-f3", reasoner.isExternallyConnectedWith(feature012, feature003));
        assertFalse("f12-f4", reasoner.isExternallyConnectedWith(feature012, feature004));
        assertFalse("f12-f5", reasoner.isExternallyConnectedWith(feature012, feature005));
        assertFalse("f12-f6", reasoner.isExternallyConnectedWith(feature012, feature006));
        assertFalse("f12-f7", reasoner.isExternallyConnectedWith(feature012, feature007));
        assertFalse("f12-f8", reasoner.isExternallyConnectedWith(feature012, feature008));
        assertTrue("f12-f9", reasoner.isExternallyConnectedWith(feature012, feature009));
        assertTrue("f12-f10", reasoner.isExternallyConnectedWith(feature012, feature010));
        assertTrue("f12-f11", reasoner.isExternallyConnectedWith(feature012, feature011));
        assertFalse("f12-f12", reasoner.isExternallyConnectedWith(feature012, feature012));
        assertTrue("f12-f13", reasoner.isExternallyConnectedWith(feature012, feature013));
        assertFalse("f12-f14", reasoner.isExternallyConnectedWith(feature012, feature014));

        assertFalse("f13-f1", reasoner.isExternallyConnectedWith(feature013, feature001));
        assertFalse("f13-f2", reasoner.isExternallyConnectedWith(feature013, feature002));
        assertFalse("f13-f3", reasoner.isExternallyConnectedWith(feature013, feature003));
        assertFalse("f13-f4", reasoner.isExternallyConnectedWith(feature013, feature004));
        assertFalse("f13-f5", reasoner.isExternallyConnectedWith(feature013, feature005));
        assertFalse("f13-f6", reasoner.isExternallyConnectedWith(feature013, feature006));
        assertFalse("f13-f7", reasoner.isExternallyConnectedWith(feature013, feature007));
        assertFalse("f13-f8", reasoner.isExternallyConnectedWith(feature013, feature008));
        assertFalse("f13-f9", reasoner.isExternallyConnectedWith(feature013, feature009));
        assertFalse("f13-f10", reasoner.isExternallyConnectedWith(feature013, feature010));
        assertFalse("f13-f11", reasoner.isExternallyConnectedWith(feature013, feature011));
        assertTrue("f13-f12", reasoner.isExternallyConnectedWith(feature013, feature012));
        assertFalse("f13-f13", reasoner.isExternallyConnectedWith(feature013, feature013));
        assertFalse("f13-f14", reasoner.isExternallyConnectedWith(feature013, feature014));

        assertFalse("f14-f1", reasoner.isExternallyConnectedWith(feature014, feature001));
        assertFalse("f14-f2", reasoner.isExternallyConnectedWith(feature014, feature002));
        assertFalse("f14-f3", reasoner.isExternallyConnectedWith(feature014, feature003));
        assertFalse("f14-f4", reasoner.isExternallyConnectedWith(feature014, feature004));
        assertFalse("f14-f5", reasoner.isExternallyConnectedWith(feature014, feature005));
        assertFalse("f14-f6", reasoner.isExternallyConnectedWith(feature014, feature006));
        assertFalse("f14-f7", reasoner.isExternallyConnectedWith(feature014, feature007));
        assertFalse("f14-f8", reasoner.isExternallyConnectedWith(feature014, feature008));
        assertFalse("f14-f9", reasoner.isExternallyConnectedWith(feature014, feature009));
        assertFalse("f14-f10", reasoner.isExternallyConnectedWith(feature014, feature010));
        assertFalse("f14-f11", reasoner.isExternallyConnectedWith(feature014, feature011));
        assertFalse("f14-f12", reasoner.isExternallyConnectedWith(feature014, feature012));
        assertFalse("f14-f13", reasoner.isExternallyConnectedWith(feature014, feature013));
        assertFalse("f14-f14", reasoner.isExternallyConnectedWith(feature014, feature014));
    }

    @Test
    public void testGetExternallyConnectedIndividuals() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8003 51.0596)");

        // -- same as feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8003 51.0596)");

        // -- inside first area feature, EC'ed with feature005/6
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8002 51.0592)");

        // -- off
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.7976 51.0596)");

        // line strings
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8002 51.0592,13.8002 51.0588,13.8013 51.0592)");

        // -- same as feature005
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8002 51.0592,13.8002 51.0588,13.8013 51.0592)");

        // -- EC'ed with feature005/6
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8013 51.0592,13.8019 51.0585,13.8022 51.0592,13.8024 51.0585)");

        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8015 51.0574,13.8019 51.0581,13.8036 51.0571,13.8018 51.0573)");

        // areas
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8003 51.0596,13.8013 51.0592," +
                        "13.8007 51.0584,13.7989 51.0591,13.8003 51.0596))");

        // -- same as feature009
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8003 51.0596,13.8013 51.0592," +
                        "13.8007 51.0584,13.7989 51.0591,13.8003 51.0596))");

        // -- EC'ed with feature012
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7989 51.0591,13.7998 51.0591," +
                        "13.8006 51.0588,13.8007 51.0584,13.7989 51.0591))");

        // -- EC'ed with feature009/10
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.7989 51.0582,13.8007 51.0584," +
                        "13.7989 51.0591,13.7989 51.0582))");

        // -- EC'ed with feature012
        OWLIndividual feature013 = i("feature013");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.7989 51.0582,13.7991 51.0573," +
                        "13.7971 51.0582,13.7989 51.0582))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7949 51.0594,13.7962 51.0589," +
                        "13.7964 51.0595,13.7949 51.0594))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getExternallyConnectedIndividuals(feature001)
                        .collect(Collectors.toSet());

        assertTrue("f1-f1", result.contains(feature001));
        assertTrue("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertTrue("f1-f9", result.contains(feature009));
        assertTrue("f1-f10", result.contains(feature010));
        assertFalse("f1-f11", result.contains(feature011));
        assertFalse("f1-f12", result.contains(feature012));
        assertFalse("f1-f13", result.contains(feature013));
        assertFalse("f1-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature002)
                        .collect(Collectors.toSet());

        assertTrue("f2-f1", result.contains(feature001));
        assertTrue("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertTrue("f2-f9", result.contains(feature009));
        assertTrue("f2-f10", result.contains(feature010));
        assertFalse("f2-f11", result.contains(feature011));
        assertFalse("f2-f12", result.contains(feature012));
        assertFalse("f2-f13", result.contains(feature013));
        assertFalse("f2-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertTrue("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertTrue("f3-f5", result.contains(feature005));
        assertTrue("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertFalse("f3-f11", result.contains(feature011));
        assertFalse("f3-f12", result.contains(feature012));
        assertFalse("f3-f13", result.contains(feature013));
        assertFalse("f3-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature004)
                        .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertTrue("f4-f4", result.contains(feature004));
        assertFalse("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));
        assertFalse("f4-f12", result.contains(feature012));
        assertFalse("f4-f13", result.contains(feature013));
        assertFalse("f4-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature005)
                        .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertTrue("f5-f3", result.contains(feature003));
        assertFalse("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertTrue("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertFalse("f5-f11", result.contains(feature011));
        assertFalse("f5-f12", result.contains(feature012));
        assertFalse("f5-f13", result.contains(feature013));
        assertFalse("f5-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature006)
                        .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertTrue("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertTrue("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertFalse("f6-f11", result.contains(feature011));
        assertFalse("f6-f12", result.contains(feature012));
        assertFalse("f6-f13", result.contains(feature013));
        assertFalse("f6-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature007)
                        .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertTrue("f7-f5", result.contains(feature005));
        assertTrue("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertTrue("f7-f9", result.contains(feature009));
        assertTrue("f7-f10", result.contains(feature010));
        assertFalse("f7-f11", result.contains(feature011));
        assertFalse("f7-f12", result.contains(feature012));
        assertFalse("f7-f13", result.contains(feature013));
        assertFalse("f7-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature008)
                        .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertFalse("f8-f8", result.contains(feature008));
        assertFalse("f8-f9", result.contains(feature009));
        assertFalse("f8-f10", result.contains(feature010));
        assertFalse("f8-f11", result.contains(feature011));
        assertFalse("f8-f12", result.contains(feature012));
        assertFalse("f8-f13", result.contains(feature013));
        assertFalse("f8-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature009)
                        .collect(Collectors.toSet());

        assertTrue("f9-f1", result.contains(feature001));
        assertTrue("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertTrue("f9-f7", result.contains(feature007));
        assertFalse("f9-f8", result.contains(feature008));
        assertFalse("f9-f9", result.contains(feature009));
        assertFalse("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));
        assertTrue("f9-f12", result.contains(feature012));
        assertFalse("f9-f13", result.contains(feature013));
        assertFalse("f9-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature010)
                        .collect(Collectors.toSet());

        assertTrue("f10-f1", result.contains(feature001));
        assertTrue("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertFalse("f10-f6", result.contains(feature006));
        assertTrue("f10-f7", result.contains(feature007));
        assertFalse("f10-f8", result.contains(feature008));
        assertFalse("f10-f9", result.contains(feature009));
        assertFalse("f10-f10", result.contains(feature010));
        assertFalse("f10-f11", result.contains(feature011));
        assertTrue("f10-f12", result.contains(feature012));
        assertFalse("f10-f13", result.contains(feature013));
        assertFalse("f10-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature011)
                        .collect(Collectors.toSet());

        assertFalse("f11-f1", result.contains(feature001));
        assertFalse("f11-f2", result.contains(feature002));
        assertFalse("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertFalse("f11-f5", result.contains(feature005));
        assertFalse("f11-f6", result.contains(feature006));
        assertFalse("f11-f7", result.contains(feature007));
        assertFalse("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertFalse("f11-f10", result.contains(feature010));
        assertFalse("f11-f11", result.contains(feature011));
        assertTrue("f11-f12", result.contains(feature012));
        assertFalse("f11-f13", result.contains(feature013));
        assertFalse("f11-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature012)
                        .collect(Collectors.toSet());

        assertFalse("f12-f1", result.contains(feature001));
        assertFalse("f12-f2", result.contains(feature002));
        assertFalse("f12-f3", result.contains(feature003));
        assertFalse("f12-f4", result.contains(feature004));
        assertFalse("f12-f5", result.contains(feature005));
        assertFalse("f12-f6", result.contains(feature006));
        assertFalse("f12-f7", result.contains(feature007));
        assertFalse("f12-f8", result.contains(feature008));
        assertTrue("f12-f9", result.contains(feature009));
        assertTrue("f12-f10", result.contains(feature010));
        assertTrue("f12-f11", result.contains(feature011));
        assertFalse("f12-f12", result.contains(feature012));
        assertTrue("f12-f13", result.contains(feature013));
        assertFalse("f12-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature013)
                        .collect(Collectors.toSet());

        assertFalse("f13-f1", result.contains(feature001));
        assertFalse("f13-f2", result.contains(feature002));
        assertFalse("f13-f3", result.contains(feature003));
        assertFalse("f13-f4", result.contains(feature004));
        assertFalse("f13-f5", result.contains(feature005));
        assertFalse("f13-f6", result.contains(feature006));
        assertFalse("f13-f7", result.contains(feature007));
        assertFalse("f13-f8", result.contains(feature008));
        assertFalse("f13-f9", result.contains(feature009));
        assertFalse("f13-f10", result.contains(feature010));
        assertFalse("f13-f11", result.contains(feature011));
        assertTrue("f13-f12", result.contains(feature012));
        assertFalse("f13-f13", result.contains(feature013));
        assertFalse("f13-f14", result.contains(feature014));

        result =
                reasoner.getExternallyConnectedIndividuals(feature014)
                        .collect(Collectors.toSet());

        assertFalse("f14-f1", result.contains(feature001));
        assertFalse("f14-f2", result.contains(feature002));
        assertFalse("f14-f3", result.contains(feature003));
        assertFalse("f14-f4", result.contains(feature004));
        assertFalse("f14-f5", result.contains(feature005));
        assertFalse("f14-f6", result.contains(feature006));
        assertFalse("f14-f7", result.contains(feature007));
        assertFalse("f14-f8", result.contains(feature008));
        assertFalse("f14-f9", result.contains(feature009));
        assertFalse("f14-f10", result.contains(feature010));
        assertFalse("f14-f11", result.contains(feature011));
        assertFalse("f14-f12", result.contains(feature012));
        assertFalse("f14-f13", result.contains(feature013));
        assertFalse("f14-f14", result.contains(feature014));
    }

    @Test
    public void testIsDisconnectedFrom() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8003 51.0596)");

        // -- same as feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8003 51.0596)");

        // -- inside first area feature, EC'ed with feature005/6
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8002 51.0592)");

        // -- off
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.7976 51.0596)");

        // line strings
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8002 51.0592,13.8002 51.0588,13.8013 51.0592)");

        // -- same as feature005
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8002 51.0592,13.8002 51.0588,13.8013 51.0592)");

        // -- EC'ed with feature005/6
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8013 51.0592,13.8019 51.0585,13.8022 51.0592,13.8024 51.0585)");

        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8015 51.0574,13.8019 51.0581,13.8036 51.0571,13.8018 51.0573)");

        // areas
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8003 51.0596,13.8013 51.0592," +
                        "13.8007 51.0584,13.7989 51.0591,13.8003 51.0596))");

        // -- same as feature009
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8003 51.0596,13.8013 51.0592," +
                        "13.8007 51.0584,13.7989 51.0591,13.8003 51.0596))");

        // -- EC'ed with feature012
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7989 51.0591,13.7998 51.0591," +
                        "13.8006 51.0588,13.8007 51.0584,13.7989 51.0591))");

        // -- EC'ed with feature009/10
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.7989 51.0582,13.8007 51.0584," +
                        "13.7989 51.0591,13.7989 51.0582))");

        // -- EC'ed with feature012
        OWLIndividual feature013 = i("feature013");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.7989 51.0582,13.7991 51.0573," +
                        "13.7971 51.0582,13.7989 51.0582))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7949 51.0594,13.7962 51.0589," +
                        "13.7964 51.0595,13.7949 51.0594))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertFalse("f1-f1", reasoner.isDisconnectedFrom(feature001, feature001));
        assertFalse("f1-f2", reasoner.isDisconnectedFrom(feature001, feature002));
        assertTrue("f1-f3", reasoner.isDisconnectedFrom(feature001, feature003));
        assertTrue("f1-f4", reasoner.isDisconnectedFrom(feature001, feature004));
        assertTrue("f1-f5", reasoner.isDisconnectedFrom(feature001, feature005));
        assertTrue("f1-f6", reasoner.isDisconnectedFrom(feature001, feature006));
        assertTrue("f1-f7", reasoner.isDisconnectedFrom(feature001, feature007));
        assertTrue("f1-f8", reasoner.isDisconnectedFrom(feature001, feature008));
        assertFalse("f1-f9", reasoner.isDisconnectedFrom(feature001, feature009));
        assertFalse("f1-f10", reasoner.isDisconnectedFrom(feature001, feature010));
        assertTrue("f1-f11", reasoner.isDisconnectedFrom(feature001, feature011));
        assertTrue("f1-f12", reasoner.isDisconnectedFrom(feature001, feature012));
        assertTrue("f1-f13", reasoner.isDisconnectedFrom(feature001, feature013));
        assertTrue("f1-f14", reasoner.isDisconnectedFrom(feature001, feature014));

        assertFalse("f2-f1", reasoner.isDisconnectedFrom(feature002, feature001));
        assertFalse("f2-f2", reasoner.isDisconnectedFrom(feature002, feature002));
        assertTrue("f2-f3", reasoner.isDisconnectedFrom(feature002, feature003));
        assertTrue("f2-f4", reasoner.isDisconnectedFrom(feature002, feature004));
        assertTrue("f2-f5", reasoner.isDisconnectedFrom(feature002, feature005));
        assertTrue("f2-f6", reasoner.isDisconnectedFrom(feature002, feature006));
        assertTrue("f2-f7", reasoner.isDisconnectedFrom(feature002, feature007));
        assertTrue("f2-f8", reasoner.isDisconnectedFrom(feature002, feature008));
        assertFalse("f2-f9", reasoner.isDisconnectedFrom(feature002, feature009));
        assertFalse("f2-f10", reasoner.isDisconnectedFrom(feature002, feature010));
        assertTrue("f2-f11", reasoner.isDisconnectedFrom(feature002, feature011));
        assertTrue("f2-f12", reasoner.isDisconnectedFrom(feature002, feature012));
        assertTrue("f2-f13", reasoner.isDisconnectedFrom(feature002, feature013));
        assertTrue("f2-f14", reasoner.isDisconnectedFrom(feature002, feature014));

        assertTrue("f3-f1", reasoner.isDisconnectedFrom(feature003, feature001));
        assertTrue("f3-f2", reasoner.isDisconnectedFrom(feature003, feature002));
        assertFalse("f3-f3", reasoner.isDisconnectedFrom(feature003, feature003));
        assertTrue("f3-f4", reasoner.isDisconnectedFrom(feature003, feature004));
        assertFalse("f3-f5", reasoner.isDisconnectedFrom(feature003, feature005));
        assertFalse("f3-f6", reasoner.isDisconnectedFrom(feature003, feature006));
        assertTrue("f3-f7", reasoner.isDisconnectedFrom(feature003, feature007));
        assertTrue("f3-f8", reasoner.isDisconnectedFrom(feature003, feature008));
        assertFalse("f3-f9", reasoner.isDisconnectedFrom(feature003, feature009));
        assertFalse("f3-f10", reasoner.isDisconnectedFrom(feature003, feature010));
        assertTrue("f3-f11", reasoner.isDisconnectedFrom(feature003, feature011));
        assertTrue("f3-f12", reasoner.isDisconnectedFrom(feature003, feature012));
        assertTrue("f3-f13", reasoner.isDisconnectedFrom(feature003, feature013));
        assertTrue("f3-f14", reasoner.isDisconnectedFrom(feature003, feature014));

        assertTrue("f4-f1", reasoner.isDisconnectedFrom(feature004, feature001));
        assertTrue("f4-f2", reasoner.isDisconnectedFrom(feature004, feature002));
        assertTrue("f4-f3", reasoner.isDisconnectedFrom(feature004, feature003));
        assertFalse("f4-f4", reasoner.isDisconnectedFrom(feature004, feature004));
        assertTrue("f4-f5", reasoner.isDisconnectedFrom(feature004, feature005));
        assertTrue("f4-f6", reasoner.isDisconnectedFrom(feature004, feature006));
        assertTrue("f4-f7", reasoner.isDisconnectedFrom(feature004, feature007));
        assertTrue("f4-f8", reasoner.isDisconnectedFrom(feature004, feature008));
        assertTrue("f4-f9", reasoner.isDisconnectedFrom(feature004, feature009));
        assertTrue("f4-f10", reasoner.isDisconnectedFrom(feature004, feature010));
        assertTrue("f4-f11", reasoner.isDisconnectedFrom(feature004, feature011));
        assertTrue("f4-f12", reasoner.isDisconnectedFrom(feature004, feature012));
        assertTrue("f4-f13", reasoner.isDisconnectedFrom(feature004, feature013));
        assertTrue("f4-f14", reasoner.isDisconnectedFrom(feature004, feature014));

        assertTrue("f5-f1", reasoner.isDisconnectedFrom(feature005, feature001));
        assertTrue("f5-f2", reasoner.isDisconnectedFrom(feature005, feature002));
        assertFalse("f5-f3", reasoner.isDisconnectedFrom(feature005, feature003));
        assertTrue("f5-f4", reasoner.isDisconnectedFrom(feature005, feature004));
        assertFalse("f5-f5", reasoner.isDisconnectedFrom(feature005, feature005));
        assertFalse("f5-f6", reasoner.isDisconnectedFrom(feature005, feature006));
        assertFalse("f5-f7", reasoner.isDisconnectedFrom(feature005, feature007));
        assertTrue("f5-f8", reasoner.isDisconnectedFrom(feature005, feature008));
        assertFalse("f5-f9", reasoner.isDisconnectedFrom(feature005, feature009));
        assertFalse("f5-f10", reasoner.isDisconnectedFrom(feature005, feature010));
        assertFalse("f5-f11", reasoner.isDisconnectedFrom(feature005, feature011));
        assertTrue("f5-f12", reasoner.isDisconnectedFrom(feature005, feature012));
        assertTrue("f5-f13", reasoner.isDisconnectedFrom(feature005, feature013));
        assertTrue("f5-f14", reasoner.isDisconnectedFrom(feature005, feature014));

        assertTrue("f6-f1", reasoner.isDisconnectedFrom(feature006, feature001));
        assertTrue("f6-f2", reasoner.isDisconnectedFrom(feature006, feature002));
        assertFalse("f6-f3", reasoner.isDisconnectedFrom(feature006, feature003));
        assertTrue("f6-f4", reasoner.isDisconnectedFrom(feature006, feature004));
        assertFalse("f6-f5", reasoner.isDisconnectedFrom(feature006, feature005));
        assertFalse("f6-f6", reasoner.isDisconnectedFrom(feature006, feature006));
        assertFalse("f6-f7", reasoner.isDisconnectedFrom(feature006, feature007));
        assertTrue("f6-f8", reasoner.isDisconnectedFrom(feature006, feature008));
        assertFalse("f6-f9", reasoner.isDisconnectedFrom(feature006, feature009));
        assertFalse("f6-f10", reasoner.isDisconnectedFrom(feature006, feature010));
        assertFalse("f6-f11", reasoner.isDisconnectedFrom(feature006, feature011));
        assertTrue("f6-f12", reasoner.isDisconnectedFrom(feature006, feature012));
        assertTrue("f6-f13", reasoner.isDisconnectedFrom(feature006, feature013));
        assertTrue("f6-f14", reasoner.isDisconnectedFrom(feature006, feature014));

        assertTrue("f7-f1", reasoner.isDisconnectedFrom(feature007, feature001));
        assertTrue("f7-f2", reasoner.isDisconnectedFrom(feature007, feature002));
        assertTrue("f7-f3", reasoner.isDisconnectedFrom(feature007, feature003));
        assertTrue("f7-f4", reasoner.isDisconnectedFrom(feature007, feature004));
        assertFalse("f7-f5", reasoner.isDisconnectedFrom(feature007, feature005));
        assertFalse("f7-f6", reasoner.isDisconnectedFrom(feature007, feature006));
        assertFalse("f7-f7", reasoner.isDisconnectedFrom(feature007, feature007));
        assertTrue("f7-f8", reasoner.isDisconnectedFrom(feature007, feature008));
        assertFalse("f7-f9", reasoner.isDisconnectedFrom(feature007, feature009));
        assertFalse("f7-f10", reasoner.isDisconnectedFrom(feature007, feature010));
        assertTrue("f7-f11", reasoner.isDisconnectedFrom(feature007, feature011));
        assertTrue("f7-f12", reasoner.isDisconnectedFrom(feature007, feature012));
        assertTrue("f7-f13", reasoner.isDisconnectedFrom(feature007, feature013));
        assertTrue("f7-f14", reasoner.isDisconnectedFrom(feature007, feature014));

        assertTrue("f8-f1", reasoner.isDisconnectedFrom(feature008, feature001));
        assertTrue("f8-f2", reasoner.isDisconnectedFrom(feature008, feature002));
        assertTrue("f8-f3", reasoner.isDisconnectedFrom(feature008, feature003));
        assertTrue("f8-f4", reasoner.isDisconnectedFrom(feature008, feature004));
        assertTrue("f8-f5", reasoner.isDisconnectedFrom(feature008, feature005));
        assertTrue("f8-f6", reasoner.isDisconnectedFrom(feature008, feature006));
        assertTrue("f8-f7", reasoner.isDisconnectedFrom(feature008, feature007));
        assertFalse("f8-f8", reasoner.isDisconnectedFrom(feature008, feature008));
        assertTrue("f8-f9", reasoner.isDisconnectedFrom(feature008, feature009));
        assertTrue("f8-f10", reasoner.isDisconnectedFrom(feature008, feature010));
        assertTrue("f8-f11", reasoner.isDisconnectedFrom(feature008, feature011));
        assertTrue("f8-f12", reasoner.isDisconnectedFrom(feature008, feature012));
        assertTrue("f8-f13", reasoner.isDisconnectedFrom(feature008, feature013));
        assertTrue("f8-f14", reasoner.isDisconnectedFrom(feature008, feature014));

        assertFalse("f9-f1", reasoner.isDisconnectedFrom(feature009, feature001));
        assertFalse("f9-f2", reasoner.isDisconnectedFrom(feature009, feature002));
        assertFalse("f9-f3", reasoner.isDisconnectedFrom(feature009, feature003));
        assertTrue("f9-f4", reasoner.isDisconnectedFrom(feature009, feature004));
        assertFalse("f9-f5", reasoner.isDisconnectedFrom(feature009, feature005));
        assertFalse("f9-f6", reasoner.isDisconnectedFrom(feature009, feature006));
        assertFalse("f9-f7", reasoner.isDisconnectedFrom(feature009, feature007));
        assertTrue("f9-f8", reasoner.isDisconnectedFrom(feature009, feature008));
        assertFalse("f9-f9", reasoner.isDisconnectedFrom(feature009, feature009));
        assertFalse("f9-f10", reasoner.isDisconnectedFrom(feature009, feature010));
        assertFalse("f9-f11", reasoner.isDisconnectedFrom(feature009, feature011));
        assertFalse("f9-f12", reasoner.isDisconnectedFrom(feature009, feature012));
        assertTrue("f9-f13", reasoner.isDisconnectedFrom(feature009, feature013));
        assertTrue("f9-f14", reasoner.isDisconnectedFrom(feature009, feature014));

        assertFalse("f10-f1", reasoner.isDisconnectedFrom(feature010, feature001));
        assertFalse("f10-f2", reasoner.isDisconnectedFrom(feature010, feature002));
        assertFalse("f10-f3", reasoner.isDisconnectedFrom(feature010, feature003));
        assertTrue("f10-f4", reasoner.isDisconnectedFrom(feature010, feature004));
        assertFalse("f10-f5", reasoner.isDisconnectedFrom(feature010, feature005));
        assertFalse("f10-f6", reasoner.isDisconnectedFrom(feature010, feature006));
        assertFalse("f10-f7", reasoner.isDisconnectedFrom(feature010, feature007));
        assertTrue("f10-f8", reasoner.isDisconnectedFrom(feature010, feature008));
        assertFalse("f10-f9", reasoner.isDisconnectedFrom(feature010, feature009));
        assertFalse("f10-f10", reasoner.isDisconnectedFrom(feature010, feature010));
        assertFalse("f10-f11", reasoner.isDisconnectedFrom(feature010, feature011));
        assertFalse("f10-f12", reasoner.isDisconnectedFrom(feature010, feature012));
        assertTrue("f10-f13", reasoner.isDisconnectedFrom(feature010, feature013));
        assertTrue("f10-f14", reasoner.isDisconnectedFrom(feature010, feature014));

        assertTrue("f11-f1", reasoner.isDisconnectedFrom(feature011, feature001));
        assertTrue("f11-f2", reasoner.isDisconnectedFrom(feature011, feature002));
        assertTrue("f11-f3", reasoner.isDisconnectedFrom(feature011, feature003));
        assertTrue("f11-f4", reasoner.isDisconnectedFrom(feature011, feature004));
        assertFalse("f11-f5", reasoner.isDisconnectedFrom(feature011, feature005));
        assertFalse("f11-f6", reasoner.isDisconnectedFrom(feature011, feature006));
        assertTrue("f11-f7", reasoner.isDisconnectedFrom(feature011, feature007));
        assertTrue("f11-f8", reasoner.isDisconnectedFrom(feature011, feature008));
        assertFalse("f11-f9", reasoner.isDisconnectedFrom(feature011, feature009));
        assertFalse("f11-f10", reasoner.isDisconnectedFrom(feature011, feature010));
        assertFalse("f11-f11", reasoner.isDisconnectedFrom(feature011, feature011));
        assertFalse("f11-f12", reasoner.isDisconnectedFrom(feature011, feature012));
        assertTrue("f11-f13", reasoner.isDisconnectedFrom(feature011, feature013));
        assertTrue("f11-f14", reasoner.isDisconnectedFrom(feature011, feature014));

        assertTrue("f12-f1", reasoner.isDisconnectedFrom(feature012, feature001));
        assertTrue("f12-f2", reasoner.isDisconnectedFrom(feature012, feature002));
        assertTrue("f12-f3", reasoner.isDisconnectedFrom(feature012, feature003));
        assertTrue("f12-f4", reasoner.isDisconnectedFrom(feature012, feature004));
        assertTrue("f12-f5", reasoner.isDisconnectedFrom(feature012, feature005));
        assertTrue("f12-f6", reasoner.isDisconnectedFrom(feature012, feature006));
        assertTrue("f12-f7", reasoner.isDisconnectedFrom(feature012, feature007));
        assertTrue("f12-f8", reasoner.isDisconnectedFrom(feature012, feature008));
        assertFalse("f12-f9", reasoner.isDisconnectedFrom(feature012, feature009));
        assertFalse("f12-f10", reasoner.isDisconnectedFrom(feature012, feature010));
        assertFalse("f12-f11", reasoner.isDisconnectedFrom(feature012, feature011));
        assertFalse("f12-f12", reasoner.isDisconnectedFrom(feature012, feature012));
        assertFalse("f12-f13", reasoner.isDisconnectedFrom(feature012, feature013));
        assertTrue("f12-f14", reasoner.isDisconnectedFrom(feature012, feature014));

        assertTrue("f13-f1", reasoner.isDisconnectedFrom(feature013, feature001));
        assertTrue("f13-f2", reasoner.isDisconnectedFrom(feature013, feature002));
        assertTrue("f13-f3", reasoner.isDisconnectedFrom(feature013, feature003));
        assertTrue("f13-f4", reasoner.isDisconnectedFrom(feature013, feature004));
        assertTrue("f13-f5", reasoner.isDisconnectedFrom(feature013, feature005));
        assertTrue("f13-f6", reasoner.isDisconnectedFrom(feature013, feature006));
        assertTrue("f13-f7", reasoner.isDisconnectedFrom(feature013, feature007));
        assertTrue("f13-f8", reasoner.isDisconnectedFrom(feature013, feature008));
        assertTrue("f13-f9", reasoner.isDisconnectedFrom(feature013, feature009));
        assertTrue("f13-f10", reasoner.isDisconnectedFrom(feature013, feature010));
        assertTrue("f13-f11", reasoner.isDisconnectedFrom(feature013, feature011));
        assertFalse("f13-f12", reasoner.isDisconnectedFrom(feature013, feature012));
        assertFalse("f13-f13", reasoner.isDisconnectedFrom(feature013, feature013));
        assertTrue("f13-f14", reasoner.isDisconnectedFrom(feature013, feature014));

        assertTrue("f14 -f1", reasoner.isDisconnectedFrom(feature014, feature001));
        assertTrue("f14-f2", reasoner.isDisconnectedFrom(feature014, feature002));
        assertTrue("f14-f3", reasoner.isDisconnectedFrom(feature014, feature003));
        assertTrue("f14-f4", reasoner.isDisconnectedFrom(feature014, feature004));
        assertTrue("f14-f5", reasoner.isDisconnectedFrom(feature014, feature005));
        assertTrue("f14-f6", reasoner.isDisconnectedFrom(feature014, feature006));
        assertTrue("f14-f7", reasoner.isDisconnectedFrom(feature014, feature007));
        assertTrue("f14-f8", reasoner.isDisconnectedFrom(feature014, feature008));
        assertTrue("f14-f9", reasoner.isDisconnectedFrom(feature014, feature009));
        assertTrue("f14-f10", reasoner.isDisconnectedFrom(feature014, feature010));
        assertTrue("f14-f11", reasoner.isDisconnectedFrom(feature014, feature011));
        assertTrue("f14-f12", reasoner.isDisconnectedFrom(feature014, feature012));
        assertTrue("f14-f13", reasoner.isDisconnectedFrom(feature014, feature013));
        assertFalse("f14-f14", reasoner.isDisconnectedFrom(feature014, feature014));
    }

    @Test
    public void testGetIndividualsDisconnectedFrom() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8003 51.0596)");

        // -- same as feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8003 51.0596)");

        // -- inside first area feature, EC'ed with feature005/6
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.8002 51.0592)");

        // -- off
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004, "POINT(13.7976 51.0596)");

        // line strings
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.8002 51.0592,13.8002 51.0588,13.8013 51.0592)");

        // -- same as feature005
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.8002 51.0592,13.8002 51.0588,13.8013 51.0592)");

        // -- EC'ed with feature005/6
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8013 51.0592,13.8019 51.0585,13.8022 51.0592,13.8024 51.0585)");

        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.8015 51.0574,13.8019 51.0581,13.8036 51.0571,13.8018 51.0573)");

        // areas
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8003 51.0596,13.8013 51.0592," +
                        "13.8007 51.0584,13.7989 51.0591,13.8003 51.0596))");

        // -- same as feature009
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8003 51.0596,13.8013 51.0592," +
                        "13.8007 51.0584,13.7989 51.0591,13.8003 51.0596))");

        // -- EC'ed with feature012
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7989 51.0591,13.7998 51.0591," +
                        "13.8006 51.0588,13.8007 51.0584,13.7989 51.0591))");

        // -- EC'ed with feature009/10
        OWLIndividual feature012 = i("feature012");
        OWLIndividual geom012 = i("geom012");
        kbHelper.addSpatialFeature(feature012, geom012,
                "POLYGON((13.7989 51.0582,13.8007 51.0584," +
                        "13.7989 51.0591,13.7989 51.0582))");

        // -- EC'ed with feature012
        OWLIndividual feature013 = i("feature013");
        OWLIndividual geom013 = i("geom013");
        kbHelper.addSpatialFeature(feature013, geom013,
                "POLYGON((13.7989 51.0582,13.7991 51.0573," +
                        "13.7971 51.0582,13.7989 51.0582))");

        // -- off
        OWLIndividual feature014 = i("feature014");
        OWLIndividual geom014 = i("geom014");
        kbHelper.addSpatialFeature(feature014, geom014,
                "POLYGON((13.7949 51.0594,13.7962 51.0589," +
                        "13.7964 51.0595,13.7949 51.0594))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsDisconnectedFrom(feature001)
                        .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertTrue("f1-f3", result.contains(feature003));
        assertTrue("f1-f4", result.contains(feature004));
        assertTrue("f1-f5", result.contains(feature005));
        assertTrue("f1-f6", result.contains(feature006));
        assertTrue("f1-f7", result.contains(feature007));
        assertTrue("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));
        assertFalse("f1-f10", result.contains(feature010));
        assertTrue("f1-f11", result.contains(feature011));
        assertTrue("f1-f12", result.contains(feature012));
        assertTrue("f1-f13", result.contains(feature013));
        assertTrue("f1-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature002)
                        .collect(Collectors.toSet());

        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertTrue("f2-f3", result.contains(feature003));
        assertTrue("f2-f4", result.contains(feature004));
        assertTrue("f2-f5", result.contains(feature005));
        assertTrue("f2-f6", result.contains(feature006));
        assertTrue("f2-f7", result.contains(feature007));
        assertTrue("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));
        assertFalse("f2-f10", result.contains(feature010));
        assertTrue("f2-f11", result.contains(feature011));
        assertTrue("f2-f12", result.contains(feature012));
        assertTrue("f2-f13", result.contains(feature013));
        assertTrue("f2-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature003)
                        .collect(Collectors.toSet());

        assertTrue("f3-f1", result.contains(feature001));
        assertTrue("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertTrue("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertTrue("f3-f7", result.contains(feature007));
        assertTrue("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertTrue("f3-f11", result.contains(feature011));
        assertTrue("f3-f12", result.contains(feature012));
        assertTrue("f3-f13", result.contains(feature013));
        assertTrue("f3-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature004)
                        .collect(Collectors.toSet());

        assertTrue("f4-f1", result.contains(feature001));
        assertTrue("f4-f2", result.contains(feature002));
        assertTrue("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertTrue("f4-f5", result.contains(feature005));
        assertTrue("f4-f6", result.contains(feature006));
        assertTrue("f4-f7", result.contains(feature007));
        assertTrue("f4-f8", result.contains(feature008));
        assertTrue("f4-f9", result.contains(feature009));
        assertTrue("f4-f10", result.contains(feature010));
        assertTrue("f4-f11", result.contains(feature011));
        assertTrue("f4-f12", result.contains(feature012));
        assertTrue("f4-f13", result.contains(feature013));
        assertTrue("f4-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature005)
                        .collect(Collectors.toSet());

        assertTrue("f5-f1", result.contains(feature001));
        assertTrue("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertTrue("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertTrue("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertFalse("f5-f11", result.contains(feature011));
        assertTrue("f5-f12", result.contains(feature012));
        assertTrue("f5-f13", result.contains(feature013));
        assertTrue("f5-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature006)
                        .collect(Collectors.toSet());

        assertTrue("f6-f1", result.contains(feature001));
        assertTrue("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertTrue("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertTrue("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertFalse("f6-f11", result.contains(feature011));
        assertTrue("f6-f12", result.contains(feature012));
        assertTrue("f6-f13", result.contains(feature013));
        assertTrue("f6-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature007)
                        .collect(Collectors.toSet());

        assertTrue("f7-f1", result.contains(feature001));
        assertTrue("f7-f2", result.contains(feature002));
        assertTrue("f7-f3", result.contains(feature003));
        assertTrue("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
        assertTrue("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertFalse("f7-f10", result.contains(feature010));
        assertTrue("f7-f11", result.contains(feature011));
        assertTrue("f7-f12", result.contains(feature012));
        assertTrue("f7-f13", result.contains(feature013));
        assertTrue("f7-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature008)
                        .collect(Collectors.toSet());

        assertTrue("f8-f1", result.contains(feature001));
        assertTrue("f8-f2", result.contains(feature002));
        assertTrue("f8-f3", result.contains(feature003));
        assertTrue("f8-f4", result.contains(feature004));
        assertTrue("f8-f5", result.contains(feature005));
        assertTrue("f8-f6", result.contains(feature006));
        assertTrue("f8-f7", result.contains(feature007));
        assertFalse("f8-f8", result.contains(feature008));
        assertTrue("f8-f9", result.contains(feature009));
        assertTrue("f8-f10", result.contains(feature010));
        assertTrue("f8-f11", result.contains(feature011));
        assertTrue("f8-f12", result.contains(feature012));
        assertTrue("f8-f13", result.contains(feature013));
        assertTrue("f8-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature009)
                        .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertTrue("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertTrue("f9-f8", result.contains(feature008));
        assertFalse("f9-f9", result.contains(feature009));
        assertFalse("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));
        assertFalse("f9-f12", result.contains(feature012));
        assertTrue("f9-f13", result.contains(feature013));
        assertTrue("f9-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature010)
                        .collect(Collectors.toSet());

        assertFalse("f10-f1", result.contains(feature001));
        assertFalse("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertTrue("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertFalse("f10-f6", result.contains(feature006));
        assertFalse("f10-f7", result.contains(feature007));
        assertTrue("f10-f8", result.contains(feature008));
        assertFalse("f10-f9", result.contains(feature009));
        assertFalse("f10-f10", result.contains(feature010));
        assertFalse("f10-f11", result.contains(feature011));
        assertFalse("f10-f12", result.contains(feature012));
        assertTrue("f10-f13", result.contains(feature013));
        assertTrue("f10-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature011)
                        .collect(Collectors.toSet());

        assertTrue("f11-f1", result.contains(feature001));
        assertTrue("f11-f2", result.contains(feature002));
        assertTrue("f11-f3", result.contains(feature003));
        assertTrue("f11-f4", result.contains(feature004));
        assertFalse("f11-f5", result.contains(feature005));
        assertFalse("f11-f6", result.contains(feature006));
        assertTrue("f11-f7", result.contains(feature007));
        assertTrue("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertFalse("f11-f10", result.contains(feature010));
        assertFalse("f11-f11", result.contains(feature011));
        assertFalse("f11-f12", result.contains(feature012));
        assertTrue("f11-f13", result.contains(feature013));
        assertTrue("f11-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature012)
                        .collect(Collectors.toSet());

        assertTrue("f12-f1", result.contains(feature001));
        assertTrue("f12-f2", result.contains(feature002));
        assertTrue("f12-f3", result.contains(feature003));
        assertTrue("f12-f4", result.contains(feature004));
        assertTrue("f12-f5", result.contains(feature005));
        assertTrue("f12-f6", result.contains(feature006));
        assertTrue("f12-f7", result.contains(feature007));
        assertTrue("f12-f8", result.contains(feature008));
        assertFalse("f12-f9", result.contains(feature009));
        assertFalse("f12-f10", result.contains(feature010));
        assertFalse("f12-f11", result.contains(feature011));
        assertFalse("f12-f12", result.contains(feature012));
        assertFalse("f12-f13", result.contains(feature013));
        assertTrue("f12-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature013)
                        .collect(Collectors.toSet());

        assertTrue("f13-f1", result.contains(feature001));
        assertTrue("f13-f2", result.contains(feature002));
        assertTrue("f13-f3", result.contains(feature003));
        assertTrue("f13-f4", result.contains(feature004));
        assertTrue("f13-f5", result.contains(feature005));
        assertTrue("f13-f6", result.contains(feature006));
        assertTrue("f13-f7", result.contains(feature007));
        assertTrue("f13-f8", result.contains(feature008));
        assertTrue("f13-f9", result.contains(feature009));
        assertTrue("f13-f10", result.contains(feature010));
        assertTrue("f13-f11", result.contains(feature011));
        assertFalse("f13-f12", result.contains(feature012));
        assertFalse("f13-f13", result.contains(feature013));
        assertTrue("f13-f14", result.contains(feature014));

        result =
                reasoner.getIndividualsDisconnectedFrom(feature014)
                        .collect(Collectors.toSet());

        assertTrue("f14 -f1", result.contains(feature001));
        assertTrue("f14-f2", result.contains(feature002));
        assertTrue("f14-f3", result.contains(feature003));
        assertTrue("f14-f4", result.contains(feature004));
        assertTrue("f14-f5", result.contains(feature005));
        assertTrue("f14-f6", result.contains(feature006));
        assertTrue("f14-f7", result.contains(feature007));
        assertTrue("f14-f8", result.contains(feature008));
        assertTrue("f14-f9", result.contains(feature009));
        assertTrue("f14-f10", result.contains(feature010));
        assertTrue("f14-f11", result.contains(feature011));
        assertTrue("f14-f12", result.contains(feature012));
        assertTrue("f14-f13", result.contains(feature013));
        assertFalse("f14-f14", result.contains(feature014));
    }

    @Test
    public void testIsNear() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        // -- near feature002
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8015 51.0592)");

        // -- near feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8018 51.0590)");

        // -- off
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7932 51.0589)");

        // line strings
        // -- near feature001, feature002 and feature005
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.8012 51.0596,13.8021 51.0594,13.8023 51.0586)");

        // -- near feature001, feature002 and feature004
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7995 51.0596,13.7991 51.0587," +
                        "13.7999 51.0585,13.8000 51.0591,13.8011 51.0590)");

        // -- off
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.7943 51.0614,13.7950 51.0613," +
                        "13.7959 51.0614,13.7973 51.0612,13.7981 51.0609)");

        // areas
        // -- near feature001, feature002, feature004, feature005 and feature008
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.8005 51.0587,13.8002 51.0596," +
                        "13.8017 51.0599,13.8032 51.0588,13.8024 51.0582," +
                        "13.8005 51.0587))");

        // -- near feature001, feature002, feature004, feature005 and feature007
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.8004 51.0583,13.8017 51.0578," +
                        "13.8003 51.0576,13.8004 51.0583))");

        // -- off
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8038 51.0558,13.8043 51.0561," +
                        "13.8054 51.0554,13.8050 51.0552,13.8038 51.0558))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);
        reasoner.setNearRadiusInMeters(70);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertTrue("f1-f1", reasoner.isNear(feature001, feature001));
        assertTrue("f1-f2", reasoner.isNear(feature001, feature002));
        assertFalse("f1-f3", reasoner.isNear(feature001, feature003));
        assertTrue("f1-f4", reasoner.isNear(feature001, feature004));
        assertTrue("f1-f5", reasoner.isNear(feature001, feature005));
        assertFalse("f1-f6", reasoner.isNear(feature001, feature006));
        assertTrue("f1-f7", reasoner.isNear(feature001, feature007));
        assertFalse("f1-f8", reasoner.isNear(feature001, feature008));
        assertFalse("f1-f9", reasoner.isNear(feature001, feature009));

        assertTrue("f2-f1", reasoner.isNear(feature002, feature001));
        assertTrue("f2-f2", reasoner.isNear(feature002, feature002));
        assertFalse("f2-f3", reasoner.isNear(feature002, feature003));
        assertTrue("f2-f4", reasoner.isNear(feature002, feature004));
        assertTrue("f2-f5", reasoner.isNear(feature002, feature005));
        assertFalse("f2-f6", reasoner.isNear(feature002, feature006));
        assertTrue("f2-f7", reasoner.isNear(feature002, feature007));
        assertFalse("f2-f8", reasoner.isNear(feature002, feature008));
        assertFalse("f2-f9", reasoner.isNear(feature002, feature009));

        assertFalse("f3-f1", reasoner.isNear(feature003, feature001));
        assertFalse("f3-f2", reasoner.isNear(feature003, feature002));
        assertTrue("f3-f3", reasoner.isNear(feature003, feature003));
        assertFalse("f3-f4", reasoner.isNear(feature003, feature004));
        assertFalse("f3-f5", reasoner.isNear(feature003, feature005));
        assertFalse("f3-f6", reasoner.isNear(feature003, feature006));
        assertFalse("f3-f7", reasoner.isNear(feature003, feature007));
        assertFalse("f3-f8", reasoner.isNear(feature003, feature008));
        assertFalse("f3-f9", reasoner.isNear(feature003, feature009));

        assertTrue("f4-f1", reasoner.isNear(feature004, feature001));
        assertTrue("f4-f2", reasoner.isNear(feature004, feature002));
        assertFalse("f4-f3", reasoner.isNear(feature004, feature003));
        assertTrue("f4-f4", reasoner.isNear(feature004, feature004));
        assertTrue("f4-f5", reasoner.isNear(feature004, feature005));
        assertFalse("f4-f6", reasoner.isNear(feature004, feature006));
        assertTrue("f4-f7", reasoner.isNear(feature004, feature007));
        assertFalse("f4-f8", reasoner.isNear(feature004, feature008));
        assertFalse("f4-f9", reasoner.isNear(feature004, feature009));

        assertTrue("f5-f1", reasoner.isNear(feature005, feature001));
        assertTrue("f5-f2", reasoner.isNear(feature005, feature002));
        assertFalse("f5-f3", reasoner.isNear(feature005, feature003));
        assertTrue("f5-f4", reasoner.isNear(feature005, feature004));
        assertTrue("f5-f5", reasoner.isNear(feature005, feature005));
        assertFalse("f5-f6", reasoner.isNear(feature005, feature006));
        assertTrue("f5-f7", reasoner.isNear(feature005, feature007));
        assertTrue("f5-f8", reasoner.isNear(feature005, feature008));
        assertFalse("f5-f9", reasoner.isNear(feature005, feature009));

        assertFalse("f6-f1", reasoner.isNear(feature006, feature001));
        assertFalse("f6-f2", reasoner.isNear(feature006, feature002));
        assertFalse("f6-f3", reasoner.isNear(feature006, feature003));
        assertFalse("f6-f4", reasoner.isNear(feature006, feature004));
        assertFalse("f6-f5", reasoner.isNear(feature006, feature005));
        assertTrue("f6-f6", reasoner.isNear(feature006, feature006));
        assertFalse("f6-f7", reasoner.isNear(feature006, feature007));
        assertFalse("f6-f8", reasoner.isNear(feature006, feature008));
        assertFalse("f6-f9", reasoner.isNear(feature006, feature009));

        assertTrue("f7-f1", reasoner.isNear(feature007, feature001));
        assertTrue("f7-f2", reasoner.isNear(feature007, feature002));
        assertFalse("f7-f3", reasoner.isNear(feature007, feature003));
        assertTrue("f7-f4", reasoner.isNear(feature007, feature004));
        assertTrue("f7-f5", reasoner.isNear(feature007, feature005));
        assertFalse("f7-f6", reasoner.isNear(feature007, feature006));
        assertTrue("f7-f7", reasoner.isNear(feature007, feature007));
        assertTrue("f7-f8", reasoner.isNear(feature007, feature008));
        assertFalse("f7-f9", reasoner.isNear(feature007, feature009));

        assertFalse("f8-f1", reasoner.isNear(feature008, feature001));
        assertFalse("f8-f2", reasoner.isNear(feature008, feature002));
        assertFalse("f8-f3", reasoner.isNear(feature008, feature003));
        assertFalse("f8-f4", reasoner.isNear(feature008, feature004));
        assertTrue("f8-f5", reasoner.isNear(feature008, feature005));
        assertFalse("f8-f6", reasoner.isNear(feature008, feature006));
        assertTrue("f8-f7", reasoner.isNear(feature008, feature007));
        assertTrue("f8-f8", reasoner.isNear(feature008, feature008));
        assertFalse("f8-f9", reasoner.isNear(feature008, feature009));

        assertFalse("f9-f1", reasoner.isNear(feature009, feature001));
        assertFalse("f9-f2", reasoner.isNear(feature009, feature002));
        assertFalse("f9-f3", reasoner.isNear(feature009, feature003));
        assertFalse("f9-f4", reasoner.isNear(feature009, feature004));
        assertFalse("f9-f5", reasoner.isNear(feature009, feature005));
        assertFalse("f9-f6", reasoner.isNear(feature009, feature006));
        assertFalse("f9-f7", reasoner.isNear(feature009, feature007));
        assertFalse("f9-f8", reasoner.isNear(feature009, feature008));
        assertTrue("f9-f9", reasoner.isNear(feature009, feature009));
    }

    @Test
    public void testGetIndividualsNear() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        // -- near feature002
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.8015 51.0592)");

        // -- near feature001
        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.8018 51.0590)");

        // -- off
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7932 51.0589)");

        // line strings
        // -- near feature001, feature002 and feature005
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.8012 51.0596,13.8021 51.0594,13.8023 51.0586)");

        // -- near feature001, feature002 and feature004
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7995 51.0596,13.7991 51.0587," +
                        "13.7999 51.0585,13.8000 51.0591,13.8011 51.0590)");

        // -- off
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.7943 51.0614,13.7950 51.0613," +
                        "13.7959 51.0614,13.7973 51.0612,13.7981 51.0609)");

        // areas
        // -- near feature001, feature002, feature004, feature005 and feature008
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.8005 51.0587,13.8002 51.0596," +
                        "13.8017 51.0599,13.8032 51.0588,13.8024 51.0582," +
                        "13.8005 51.0587))");

        // -- near feature001, feature002, feature004, feature005 and feature007
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "POLYGON((13.8004 51.0583,13.8017 51.0578," +
                        "13.8003 51.0576,13.8004 51.0583))");

        // -- off
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.8038 51.0558,13.8043 51.0561," +
                        "13.8054 51.0554,13.8050 51.0552,13.8038 51.0558))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);
        reasoner.setNearRadiusInMeters(70);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result = reasoner.getIndividualsNear(feature001)
                .collect(Collectors.toSet());

        assertTrue("f1-f1", result.contains(feature001));
        assertTrue("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertTrue("f1-f4", result.contains(feature004));
        assertTrue("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertTrue("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsNear(feature002)
                        .collect(Collectors.toSet());

        assertTrue("f2-f1", result.contains(feature001));
        assertTrue("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertTrue("f2-f4", result.contains(feature004));
        assertTrue("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertTrue("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsNear(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertTrue("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsNear(feature004)
                        .collect(Collectors.toSet());

        assertTrue("f4-f1", result.contains(feature001));
        assertTrue("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertTrue("f4-f4", result.contains(feature004));
        assertTrue("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertTrue("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsNear(feature005)
                        .collect(Collectors.toSet());

        assertTrue("f5-f1", result.contains(feature001));
        assertTrue("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertTrue("f5-f4", result.contains(feature004));
        assertTrue("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertTrue("f5-f7", result.contains(feature007));
        assertTrue("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsNear(feature006)
                        .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertTrue("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsNear(feature007)
                        .collect(Collectors.toSet());

        assertTrue("f7-f1", result.contains(feature001));
        assertTrue("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertTrue("f7-f4", result.contains(feature004));
        assertTrue("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertTrue("f7-f7", result.contains(feature007));
        assertTrue("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsNear(feature008)
                        .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertTrue("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertTrue("f8-f7", result.contains(feature007));
        assertTrue("f8-f8", result.contains(feature008));
        assertFalse("f8-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsNear(feature009)
                        .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertFalse("f9-f8", result.contains(feature008));
        assertTrue("f9-f9", result.contains(feature009));
    }

    @Test
    public void testStartsNear() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7978 51.0606)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7875 51.0606)");

        // line strings
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003,
                "LINESTRING(13.7952 51.0605,13.7961 51.0611," +
                        "13.7960 51.0603,13.7967 51.0611,13.7967 51.0601," +
                        "13.7974 51.0608)");

        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7982 51.0607,13.7998 51.0606," +
                        "13.7991 51.0597,13.7983 51.0602,13.7983 51.0594," +
                        "13.7999 51.0594,13.8000 51.0603)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7974 51.0598,13.7968 51.0595," +
                        "13.7946 51.0603,13.7958 51.0617,13.7984 51.0611," +
                        "13.7980 51.0608)");

        // areas
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "POLYGON((13.7971 51.0600,13.7977 51.0610," +
                        "13.7986 51.0609,13.7978 51.0598,13.7971 51.0600))");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.7893 51.0622,13.7896 51.0612," +
                        "13.7918 51.0612,13.7916 51.0622,13.7893 51.0622))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);
        reasoner.setNearRadiusInMeters(60);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertFalse("f1-f1", reasoner.startsNear(feature001, feature001));
        assertFalse("f1-f2", reasoner.startsNear(feature001, feature002));
        assertFalse("f1-f3", reasoner.startsNear(feature001, feature003));
        assertFalse("f1-f4", reasoner.startsNear(feature001, feature004));
        assertFalse("f1-f5", reasoner.startsNear(feature001, feature005));
        assertFalse("f1-f6", reasoner.startsNear(feature001, feature006));
        assertFalse("f1-f7", reasoner.startsNear(feature001, feature007));

        assertFalse("f2-f1", reasoner.startsNear(feature002, feature001));
        assertFalse("f2-f2", reasoner.startsNear(feature002, feature002));
        assertFalse("f2-f3", reasoner.startsNear(feature002, feature003));
        assertFalse("f2-f4", reasoner.startsNear(feature002, feature004));
        assertFalse("f2-f5", reasoner.startsNear(feature002, feature005));
        assertFalse("f2-f6", reasoner.startsNear(feature002, feature006));
        assertFalse("f2-f7", reasoner.startsNear(feature002, feature007));

        assertFalse("f3-f1", reasoner.startsNear(feature003, feature001));
        assertFalse("f3-f2", reasoner.startsNear(feature003, feature002));
        assertFalse("f3-f3", reasoner.startsNear(feature003, feature003));
        assertFalse("f3-f4", reasoner.startsNear(feature003, feature004));
        assertTrue("f3-f5", reasoner.startsNear(feature003, feature005));
        assertFalse("f3-f6", reasoner.startsNear(feature003, feature006));
        assertFalse("f3-f7", reasoner.startsNear(feature003, feature007));

        assertTrue("f4-f1", reasoner.startsNear(feature004, feature001));
        assertFalse("f4-f2", reasoner.startsNear(feature004, feature002));
        assertTrue("f4-f3", reasoner.startsNear(feature004, feature003));
        assertFalse("f4-f4", reasoner.startsNear(feature004, feature004));
        assertTrue("f4-f5", reasoner.startsNear(feature004, feature005));
        assertTrue("f4-f6", reasoner.startsNear(feature004, feature006));
        assertFalse("f4-f7", reasoner.startsNear(feature004, feature007));

        assertFalse("f5-f1", reasoner.startsNear(feature005, feature001));
        assertFalse("f5-f2", reasoner.startsNear(feature005, feature002));
        assertTrue("f5-f3", reasoner.startsNear(feature005, feature003));
        assertFalse("f5-f4", reasoner.startsNear(feature005, feature004));
        assertFalse("f5-f5", reasoner.startsNear(feature005, feature005));
        assertTrue("f5-f6", reasoner.startsNear(feature005, feature006));
        assertFalse("f5-f7", reasoner.startsNear(feature005, feature007));

        assertFalse("f6-f1", reasoner.startsNear(feature006, feature001));
        assertFalse("f6-f2", reasoner.startsNear(feature006, feature002));
        assertFalse("f6-f3", reasoner.startsNear(feature006, feature003));
        assertFalse("f6-f4", reasoner.startsNear(feature006, feature004));
        assertFalse("f6-f5", reasoner.startsNear(feature006, feature005));
        assertFalse("f6-f6", reasoner.startsNear(feature006, feature006));
        assertFalse("f6-f7", reasoner.startsNear(feature006, feature007));

        assertFalse("f7-f1", reasoner.startsNear(feature007, feature001));
        assertFalse("f7-f2", reasoner.startsNear(feature007, feature002));
        assertFalse("f7-f3", reasoner.startsNear(feature007, feature003));
        assertFalse("f7-f4", reasoner.startsNear(feature007, feature004));
        assertFalse("f7-f5", reasoner.startsNear(feature007, feature005));
        assertFalse("f7-f6", reasoner.startsNear(feature007, feature006));
        assertFalse("f7-f7", reasoner.startsNear(feature007, feature007));
    }

    @Test
    public void testGetIndividualsStartingNear() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7978 51.0606)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7875 51.0606)");

        // line strings
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003,
                "LINESTRING(13.7952 51.0605,13.7961 51.0611," +
                        "13.7960 51.0603,13.7967 51.0611,13.7967 51.0601," +
                        "13.7974 51.0608)");

        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7982 51.0607,13.7998 51.0606," +
                        "13.7991 51.0597,13.7983 51.0602,13.7983 51.0594," +
                        "13.7999 51.0594,13.8000 51.0603)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7974 51.0598,13.7968 51.0595," +
                        "13.7946 51.0603,13.7958 51.0617,13.7984 51.0611," +
                        "13.7980 51.0608)");

        // areas
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "POLYGON((13.7971 51.0600,13.7977 51.0610," +
                        "13.7986 51.0609,13.7978 51.0598,13.7971 51.0600))");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.7893 51.0622,13.7896 51.0612," +
                        "13.7918 51.0612,13.7916 51.0622,13.7893 51.0622))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);
        reasoner.setNearRadiusInMeters(60);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result = reasoner.getIndividualsStartingNear(feature001)
                .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertTrue("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));

        result = reasoner.getIndividualsStartingNear(feature002)
                .collect(Collectors.toSet());

        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));

        result = reasoner.getIndividualsStartingNear(feature003)
                .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertTrue("f3-f4", result.contains(feature004));
        assertTrue("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));

        result = reasoner.getIndividualsStartingNear(feature004)
                .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertFalse("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));

        result = reasoner.getIndividualsStartingNear(feature005)
                .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertTrue("f5-f3", result.contains(feature003));
        assertTrue("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));

        result = reasoner.getIndividualsStartingNear(feature006)
                .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertTrue("f6-f4", result.contains(feature004));
        assertTrue("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));

        result = reasoner.getIndividualsStartingNear(feature007)
                .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
    }

    @Test
    public void testEndsNear() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7978 51.0606)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7875 51.0606)");

        // line strings
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003,
                "LINESTRING(13.7952 51.0605,13.7961 51.0611," +
                        "13.7960 51.0603,13.7967 51.0611,13.7967 51.0601," +
                        "13.7974 51.0608)");

        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7982 51.0607,13.7998 51.0606," +
                        "13.7991 51.0597,13.7983 51.0602,13.7983 51.0594," +
                        "13.7999 51.0594,13.8000 51.0603)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7974 51.0598,13.7968 51.0595," +
                        "13.7946 51.0603,13.7958 51.0617,13.7984 51.0611," +
                        "13.7980 51.0608)");

        // areas
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "POLYGON((13.7971 51.0600,13.7977 51.0610," +
                        "13.7986 51.0609,13.7978 51.0598,13.7971 51.0600))");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.7893 51.0622,13.7896 51.0612," +
                        "13.7918 51.0612,13.7916 51.0622,13.7893 51.0622))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);
        reasoner.setNearRadiusInMeters(60);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertFalse("f1-f1", reasoner.endsNear(feature001, feature001));
        assertFalse("f1-f2", reasoner.endsNear(feature001, feature002));
        assertFalse("f1-f3", reasoner.endsNear(feature001, feature003));
        assertFalse("f1-f4", reasoner.endsNear(feature001, feature004));
        assertFalse("f1-f5", reasoner.endsNear(feature001, feature005));
        assertFalse("f1-f6", reasoner.endsNear(feature001, feature006));
        assertFalse("f1-f7", reasoner.endsNear(feature001, feature007));

        assertFalse("f2-f1", reasoner.endsNear(feature002, feature001));
        assertFalse("f2-f2", reasoner.endsNear(feature002, feature002));
        assertFalse("f2-f3", reasoner.endsNear(feature002, feature003));
        assertFalse("f2-f4", reasoner.endsNear(feature002, feature004));
        assertFalse("f2-f5", reasoner.endsNear(feature002, feature005));
        assertFalse("f2-f6", reasoner.endsNear(feature002, feature006));
        assertFalse("f2-f7", reasoner.endsNear(feature002, feature007));

        assertTrue("f3-f1", reasoner.endsNear(feature003, feature001));
        assertFalse("f3-f2", reasoner.endsNear(feature003, feature002));
        assertFalse("f3-f3", reasoner.endsNear(feature003, feature003));
        assertTrue("f3-f4", reasoner.endsNear(feature003, feature004));
        assertTrue("f3-f5", reasoner.endsNear(feature003, feature005));
        assertTrue("f3-f6", reasoner.endsNear(feature003, feature006));
        assertFalse("f3-f7", reasoner.endsNear(feature003, feature007));

        assertFalse("f4-f1", reasoner.endsNear(feature004, feature001));
        assertFalse("f4-f2", reasoner.endsNear(feature004, feature002));
        assertFalse("f4-f3", reasoner.endsNear(feature004, feature003));
        assertFalse("f4-f4", reasoner.endsNear(feature004, feature004));
        assertFalse("f4-f5", reasoner.endsNear(feature004, feature005));
        assertFalse("f4-f6", reasoner.endsNear(feature004, feature006));
        assertFalse("f4-f7", reasoner.endsNear(feature004, feature007));

        assertTrue("f5-f1", reasoner.endsNear(feature005, feature001));
        assertFalse("f5-f2", reasoner.endsNear(feature005, feature002));
        assertTrue("f5-f3", reasoner.endsNear(feature005, feature003));
        assertTrue("f5-f4", reasoner.endsNear(feature005, feature004));
        assertFalse("f5-f5", reasoner.endsNear(feature005, feature005));
        assertTrue("f5-f6", reasoner.endsNear(feature005, feature006));
        assertFalse("f5-f7", reasoner.endsNear(feature005, feature007));

        assertFalse("f6-f1", reasoner.endsNear(feature006, feature001));
        assertFalse("f6-f2", reasoner.endsNear(feature006, feature002));
        assertFalse("f6-f3", reasoner.endsNear(feature006, feature003));
        assertFalse("f6-f4", reasoner.endsNear(feature006, feature004));
        assertFalse("f6-f5", reasoner.endsNear(feature006, feature005));
        assertFalse("f6-f6", reasoner.endsNear(feature006, feature006));
        assertFalse("f6-f7", reasoner.endsNear(feature006, feature007));

        assertFalse("f7-f1", reasoner.endsNear(feature007, feature001));
        assertFalse("f7-f2", reasoner.endsNear(feature007, feature002));
        assertFalse("f7-f3", reasoner.endsNear(feature007, feature003));
        assertFalse("f7-f4", reasoner.endsNear(feature007, feature004));
        assertFalse("f7-f5", reasoner.endsNear(feature007, feature005));
        assertFalse("f7-f6", reasoner.endsNear(feature007, feature006));
        assertFalse("f7-f7", reasoner.endsNear(feature007, feature007));
    }

    @Test
    public void testGetIndividualsEndingNear() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7978 51.0606)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7875 51.0606)");

        // line strings
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003,
                "LINESTRING(13.7952 51.0605,13.7961 51.0611," +
                        "13.7960 51.0603,13.7967 51.0611,13.7967 51.0601," +
                        "13.7974 51.0608)");

        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7982 51.0607,13.7998 51.0606," +
                        "13.7991 51.0597,13.7983 51.0602,13.7983 51.0594," +
                        "13.7999 51.0594,13.8000 51.0603)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7974 51.0598,13.7968 51.0595," +
                        "13.7946 51.0603,13.7958 51.0617,13.7984 51.0611," +
                        "13.7980 51.0608)");

        // areas
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "POLYGON((13.7971 51.0600,13.7977 51.0610," +
                        "13.7986 51.0609,13.7978 51.0598,13.7971 51.0600))");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.7893 51.0622,13.7896 51.0612," +
                        "13.7918 51.0612,13.7916 51.0622,13.7893 51.0622))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);
        reasoner.setNearRadiusInMeters(60);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result = reasoner.getIndividualsEndingNear(feature001)
                .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertTrue("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertTrue("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));

        result = reasoner.getIndividualsEndingNear(feature002)
                .collect(Collectors.toSet());

        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));

        result = reasoner.getIndividualsEndingNear(feature003)
                .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertTrue("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));

        result = reasoner.getIndividualsEndingNear(feature004)
                .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertTrue("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertTrue("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));

        result = reasoner.getIndividualsEndingNear(feature005)
                .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertTrue("f5-f3", result.contains(feature003));
        assertFalse("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));

        result = reasoner.getIndividualsEndingNear(feature006)
                .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertTrue("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertTrue("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));

        result = reasoner.getIndividualsEndingNear(feature007)
                .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
    }

    @Test
    public void testCrosses() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7979 51.0603)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7967 51.0596)");

        // -- off
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7842 51.0611)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7952 51.0603,13.7965 51.0606," +
                        "13.7979 51.0603,13.7993 51.0593,13.7987 51.0586," +
                        "13.7968 51.0589,13.7967 51.0596)");

        // -- same as feature004
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7952 51.0603,13.7965 51.0606," +
                        "13.7979 51.0603,13.7993 51.0593,13.7987 51.0586," +
                        "13.7968 51.0589,13.7967 51.0596)");

        // -- crosses feature004/5
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.7973 51.0598,13.7994 51.0602," +
                        "13.8014 51.0598,13.8022 51.0590,13.8014 51.0582," +
                        "13.8022 51.0580)");

        // -- touches feature004/5, but does not cross
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8022 51.0590,13.8033 51.0585," +
                        "13.8040 51.0588,13.8048 51.0587,13.8051 51.0581," +
                        "13.8045 51.0579)");

        // -- off
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.7839 51.0621,13.7852 51.0620," +
                        "13.7862 51.0618,13.7870 51.0618)");

        // areas
        // -- entered by feature004/5 but not crossed
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.7953 51.0595,13.7967 51.0601," +
                        "13.7983 51.0596,13.7982 51.0590,13.7972 51.0583," +
                        "13.7955 51.0589,13.7953 51.0595))");

        // -- crossed by feature006
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8000 51.0606,13.8019 51.0603," +
                        "13.8010 51.0590,13.7996 51.0595,13.8000 51.0606))");

        // -- off
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7894 51.0614,13.7886 51.0609," +
                        "13.7902 51.0607,13.7894 51.0614))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertFalse("f1-f1", reasoner.crosses(feature001, feature001));
        assertFalse("f1-f2", reasoner.crosses(feature001, feature002));
        assertFalse("f1-f3", reasoner.crosses(feature001, feature003));
        assertFalse("f1-f4", reasoner.crosses(feature001, feature004));
        assertFalse("f1-f5", reasoner.crosses(feature001, feature005));
        assertFalse("f1-f6", reasoner.crosses(feature001, feature006));
        assertFalse("f1-f7", reasoner.crosses(feature001, feature007));
        assertFalse("f1-f8", reasoner.crosses(feature001, feature008));
        assertFalse("f1-f9", reasoner.crosses(feature001, feature009));
        assertFalse("f1-f10", reasoner.crosses(feature001, feature010));
        assertFalse("f1-f11", reasoner.crosses(feature001, feature011));

        assertFalse("f2-f1", reasoner.crosses(feature002, feature001));
        assertFalse("f2-f2", reasoner.crosses(feature002, feature002));
        assertFalse("f2-f3", reasoner.crosses(feature002, feature003));
        assertFalse("f2-f4", reasoner.crosses(feature002, feature004));
        assertFalse("f2-f5", reasoner.crosses(feature002, feature005));
        assertFalse("f2-f6", reasoner.crosses(feature002, feature006));
        assertFalse("f2-f7", reasoner.crosses(feature002, feature007));
        assertFalse("f2-f8", reasoner.crosses(feature002, feature008));
        assertFalse("f2-f9", reasoner.crosses(feature002, feature009));
        assertFalse("f2-f10", reasoner.crosses(feature002, feature010));
        assertFalse("f2-f11", reasoner.crosses(feature002, feature011));

        assertFalse("f3-f1", reasoner.crosses(feature003, feature001));
        assertFalse("f3-f2", reasoner.crosses(feature003, feature002));
        assertFalse("f3-f3", reasoner.crosses(feature003, feature003));
        assertFalse("f3-f4", reasoner.crosses(feature003, feature004));
        assertFalse("f3-f5", reasoner.crosses(feature003, feature005));
        assertFalse("f3-f6", reasoner.crosses(feature003, feature006));
        assertFalse("f3-f7", reasoner.crosses(feature003, feature007));
        assertFalse("f3-f8", reasoner.crosses(feature003, feature008));
        assertFalse("f3-f9", reasoner.crosses(feature003, feature009));
        assertFalse("f3-f10", reasoner.crosses(feature003, feature010));
        assertFalse("f3-f11", reasoner.crosses(feature003, feature011));

        assertTrue("f4-f1", reasoner.crosses(feature004, feature001));
        assertFalse("f4-f2", reasoner.crosses(feature004, feature002));
        assertFalse("f4-f3", reasoner.crosses(feature004, feature003));
        assertFalse("f4-f4", reasoner.crosses(feature004, feature004));
        assertFalse("f4-f5", reasoner.crosses(feature004, feature005));
        assertTrue("f4-f6", reasoner.crosses(feature004, feature006));
        assertFalse("f4-f7", reasoner.crosses(feature004, feature007));
        assertFalse("f4-f8", reasoner.crosses(feature004, feature008));
        assertFalse("f4-f9", reasoner.crosses(feature004, feature009));
        assertFalse("f4-f10", reasoner.crosses(feature004, feature010));
        assertFalse("f4-f11", reasoner.crosses(feature004, feature011));

        assertTrue("f5-f1", reasoner.crosses(feature005, feature001));
        assertFalse("f5-f2", reasoner.crosses(feature005, feature002));
        assertFalse("f5-f3", reasoner.crosses(feature005, feature003));
        assertFalse("f5-f4", reasoner.crosses(feature005, feature004));
        assertFalse("f5-f5", reasoner.crosses(feature005, feature005));
        assertTrue("f5-f6", reasoner.crosses(feature005, feature006));
        assertFalse("f5-f7", reasoner.crosses(feature005, feature007));
        assertFalse("f5-f8", reasoner.crosses(feature005, feature008));
        assertFalse("f5-f9", reasoner.crosses(feature005, feature009));
        assertFalse("f5-f10", reasoner.crosses(feature005, feature010));
        assertFalse("f5-f11", reasoner.crosses(feature005, feature011));

        assertFalse("f6-f1", reasoner.crosses(feature006, feature001));
        assertFalse("f6-f2", reasoner.crosses(feature006, feature002));
        assertFalse("f6-f3", reasoner.crosses(feature006, feature003));
        assertTrue("f6-f4", reasoner.crosses(feature006, feature004));
        assertTrue("f6-f5", reasoner.crosses(feature006, feature005));
        assertFalse("f6-f6", reasoner.crosses(feature006, feature006));
        assertFalse("f6-f7", reasoner.crosses(feature006, feature007));
        assertFalse("f6-f8", reasoner.crosses(feature006, feature008));
        assertFalse("f6-f9", reasoner.crosses(feature006, feature009));
        assertTrue("f6-f10", reasoner.crosses(feature006, feature010));
        assertFalse("f6-f11", reasoner.crosses(feature006, feature011));

        assertFalse("f7-f1", reasoner.crosses(feature007, feature001));
        assertFalse("f7-f2", reasoner.crosses(feature007, feature002));
        assertFalse("f7-f3", reasoner.crosses(feature007, feature003));
        assertFalse("f7-f4", reasoner.crosses(feature007, feature004));
        assertFalse("f7-f5", reasoner.crosses(feature007, feature005));
        assertFalse("f7-f6", reasoner.crosses(feature007, feature006));
        assertFalse("f7-f7", reasoner.crosses(feature007, feature007));
        assertFalse("f7-f8", reasoner.crosses(feature007, feature008));
        assertFalse("f7-f9", reasoner.crosses(feature007, feature009));
        assertFalse("f7-f10", reasoner.crosses(feature007, feature010));
        assertFalse("f7-f11", reasoner.crosses(feature007, feature011));

        assertFalse("f8-f1", reasoner.crosses(feature008, feature001));
        assertFalse("f8-f2", reasoner.crosses(feature008, feature002));
        assertFalse("f8-f3", reasoner.crosses(feature008, feature003));
        assertFalse("f8-f4", reasoner.crosses(feature008, feature004));
        assertFalse("f8-f5", reasoner.crosses(feature008, feature005));
        assertFalse("f8-f6", reasoner.crosses(feature008, feature006));
        assertFalse("f8-f7", reasoner.crosses(feature008, feature007));
        assertFalse("f8-f8", reasoner.crosses(feature008, feature008));
        assertFalse("f8-f9", reasoner.crosses(feature008, feature009));
        assertFalse("f8-f10", reasoner.crosses(feature008, feature010));
        assertFalse("f8-f11", reasoner.crosses(feature008, feature011));

        assertFalse("f9-f1", reasoner.crosses(feature009, feature001));
        assertFalse("f9-f2", reasoner.crosses(feature009, feature002));
        assertFalse("f9-f3", reasoner.crosses(feature009, feature003));
        assertFalse("f9-f4", reasoner.crosses(feature009, feature004));
        assertFalse("f9-f5", reasoner.crosses(feature009, feature005));
        assertFalse("f9-f6", reasoner.crosses(feature009, feature006));
        assertFalse("f9-f7", reasoner.crosses(feature009, feature007));
        assertFalse("f9-f8", reasoner.crosses(feature009, feature008));
        assertFalse("f9-f9", reasoner.crosses(feature009, feature009));
        assertFalse("f9-f10", reasoner.crosses(feature009, feature010));
        assertFalse("f9-f11", reasoner.crosses(feature009, feature011));

        assertFalse("f10-f1", reasoner.crosses(feature010, feature001));
        assertFalse("f10-f2", reasoner.crosses(feature010, feature002));
        assertFalse("f10-f3", reasoner.crosses(feature010, feature003));
        assertFalse("f10-f4", reasoner.crosses(feature010, feature004));
        assertFalse("f10-f5", reasoner.crosses(feature010, feature005));
        assertFalse("f10-f6", reasoner.crosses(feature010, feature006));
        assertFalse("f10-f7", reasoner.crosses(feature010, feature007));
        assertFalse("f10-f8", reasoner.crosses(feature010, feature008));
        assertFalse("f10-f9", reasoner.crosses(feature010, feature009));
        assertFalse("f10-f10", reasoner.crosses(feature010, feature010));
        assertFalse("f10-f11", reasoner.crosses(feature010, feature011));

        assertFalse("f11-f1", reasoner.crosses(feature011, feature001));
        assertFalse("f11-f2", reasoner.crosses(feature011, feature002));
        assertFalse("f11-f3", reasoner.crosses(feature011, feature003));
        assertFalse("f11-f4", reasoner.crosses(feature011, feature004));
        assertFalse("f11-f5", reasoner.crosses(feature011, feature005));
        assertFalse("f11-f6", reasoner.crosses(feature011, feature006));
        assertFalse("f11-f7", reasoner.crosses(feature011, feature007));
        assertFalse("f11-f8", reasoner.crosses(feature011, feature008));
        assertFalse("f11-f9", reasoner.crosses(feature011, feature009));
        assertFalse("f11-f10", reasoner.crosses(feature011, feature010));
        assertFalse("f11-f11", reasoner.crosses(feature011, feature011));
    }

    @Test
    public void testGetIndividualsCrossing() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7979 51.0603)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7967 51.0596)");

        // -- off
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003, "POINT(13.7842 51.0611)");

        // line strings
        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7952 51.0603,13.7965 51.0606," +
                        "13.7979 51.0603,13.7993 51.0593,13.7987 51.0586," +
                        "13.7968 51.0589,13.7967 51.0596)");

        // -- same as feature004
        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7952 51.0603,13.7965 51.0606," +
                        "13.7979 51.0603,13.7993 51.0593,13.7987 51.0586," +
                        "13.7968 51.0589,13.7967 51.0596)");

        // -- crosses feature004/5
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "LINESTRING(13.7973 51.0598,13.7994 51.0602," +
                        "13.8014 51.0598,13.8022 51.0590,13.8014 51.0582," +
                        "13.8022 51.0580)");

        // -- touches feature004/5, but does not cross
        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "LINESTRING(13.8022 51.0590,13.8033 51.0585," +
                        "13.8040 51.0588,13.8048 51.0587,13.8051 51.0581," +
                        "13.8045 51.0579)");

        // -- off
        OWLIndividual feature008 = i("feature008");
        OWLIndividual geom008 = i("geom008");
        kbHelper.addSpatialFeature(feature008, geom008,
                "LINESTRING(13.7839 51.0621,13.7852 51.0620," +
                        "13.7862 51.0618,13.7870 51.0618)");

        // areas
        // -- entered by feature004/5 but not crossed
        OWLIndividual feature009 = i("feature009");
        OWLIndividual geom009 = i("geom009");
        kbHelper.addSpatialFeature(feature009, geom009,
                "POLYGON((13.7953 51.0595,13.7967 51.0601," +
                        "13.7983 51.0596,13.7982 51.0590,13.7972 51.0583," +
                        "13.7955 51.0589,13.7953 51.0595))");

        // -- crossed by feature006
        OWLIndividual feature010 = i("feature010");
        OWLIndividual geom010 = i("geom010");
        kbHelper.addSpatialFeature(feature010, geom010,
                "POLYGON((13.8000 51.0606,13.8019 51.0603," +
                        "13.8010 51.0590,13.7996 51.0595,13.8000 51.0606))");

        // -- off
        OWLIndividual feature011 = i("feature011");
        OWLIndividual geom011 = i("geom011");
        kbHelper.addSpatialFeature(feature011, geom011,
                "POLYGON((13.7894 51.0614,13.7886 51.0609," +
                        "13.7902 51.0607,13.7894 51.0614))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsCrossing(feature001)
                        .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertTrue("f1-f4", result.contains(feature004));
        assertTrue("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));
        assertFalse("f1-f8", result.contains(feature008));
        assertFalse("f1-f9", result.contains(feature009));
        assertFalse("f1-f10", result.contains(feature010));
        assertFalse("f1-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsCrossing(feature002)
                        .collect(Collectors.toSet());

        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));
        assertFalse("f2-f10", result.contains(feature010));
        assertFalse("f2-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsCrossing(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));
        assertFalse("f3-f10", result.contains(feature010));
        assertFalse("f3-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsCrossing(feature004)
                        .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertFalse("f4-f5", result.contains(feature005));
        assertTrue("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));
        assertFalse("f4-f10", result.contains(feature010));
        assertFalse("f4-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsCrossing(feature005)
                        .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertFalse("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertTrue("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));
        assertFalse("f5-f10", result.contains(feature010));
        assertFalse("f5-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsCrossing(feature006)
                        .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertTrue("f6-f4", result.contains(feature004));
        assertTrue("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));
        assertFalse("f6-f10", result.contains(feature010));
        assertFalse("f6-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsCrossing(feature007)
                        .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
        assertFalse("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));
        assertFalse("f7-f10", result.contains(feature010));
        assertFalse("f7-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsCrossing(feature008)
                        .collect(Collectors.toSet());

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertFalse("f8-f7", result.contains(feature007));
        assertFalse("f8-f8", result.contains(feature008));
        assertFalse("f8-f9", result.contains(feature009));
        assertFalse("f8-f10", result.contains(feature010));
        assertFalse("f8-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsCrossing(feature009)
                        .collect(Collectors.toSet());

        assertFalse("f9-f1", result.contains(feature001));
        assertFalse("f9-f2", result.contains(feature002));
        assertFalse("f9-f3", result.contains(feature003));
        assertFalse("f9-f4", result.contains(feature004));
        assertFalse("f9-f5", result.contains(feature005));
        assertFalse("f9-f6", result.contains(feature006));
        assertFalse("f9-f7", result.contains(feature007));
        assertFalse("f9-f8", result.contains(feature008));
        assertFalse("f9-f9", result.contains(feature009));
        assertFalse("f9-f10", result.contains(feature010));
        assertFalse("f9-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsCrossing(feature010)
                        .collect(Collectors.toSet());

        assertFalse("f10-f1", result.contains(feature001));
        assertFalse("f10-f2", result.contains(feature002));
        assertFalse("f10-f3", result.contains(feature003));
        assertFalse("f10-f4", result.contains(feature004));
        assertFalse("f10-f5", result.contains(feature005));
        assertTrue("f10-f6", result.contains(feature006));
        assertFalse("f10-f7", result.contains(feature007));
        assertFalse("f10-f8", result.contains(feature008));
        assertFalse("f10-f9", result.contains(feature009));
        assertFalse("f10-f10", result.contains(feature010));
        assertFalse("f10-f11", result.contains(feature011));

        result =
                reasoner.getIndividualsCrossing(feature011)
                        .collect(Collectors.toSet());

        assertFalse("f11-f1", result.contains(feature001));
        assertFalse("f11-f2", result.contains(feature002));
        assertFalse("f11-f3", result.contains(feature003));
        assertFalse("f11-f4", result.contains(feature004));
        assertFalse("f11-f5", result.contains(feature005));
        assertFalse("f11-f6", result.contains(feature006));
        assertFalse("f11-f7", result.contains(feature007));
        assertFalse("f11-f8", result.contains(feature008));
        assertFalse("f11-f9", result.contains(feature009));
        assertFalse("f11-f10", result.contains(feature010));
        assertFalse("f11-f11", result.contains(feature011));
    }

    @Test
    public void testRunsAlong() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7985 51.0601)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7896 51.0605)");

        // line strings
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003,
                "LINESTRING(13.7964 51.0606,13.7975 51.0605," +
                        "13.7983 51.0598,13.7991 51.0598)");

        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7925 51.0614,13.7949 51.0611," +
                        "13.7967 51.0607,13.7977 51.0602,13.7982 51.0600," +
                        "13.7988 51.0597,13.7993 51.0596,13.8013 51.0591," +
                        "13.8018 51.0582,13.8000 51.0578,13.7993 51.0580)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7837 51.0619,13.7833 51.0616," +
                        "13.7843 51.0615,13.7854 51.0613)");

        // areas
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "POLYGON((13.7998 51.0597,13.8011 51.0593," +
                        "13.8008 51.0589,13.7993 51.0594,13.7998 51.0597))");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.7870 51.0624,13.7869 51.0618," +
                        "13.7887 51.0617,13.7888 51.0622,13.7870 51.0624))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);
        reasoner.setRunsAlongToleranceInMeters(10);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertFalse("f1-f1", reasoner.runsAlong(feature001, feature001));
        assertFalse("f1-f2", reasoner.runsAlong(feature001, feature002));
        assertFalse("f1-f3", reasoner.runsAlong(feature001, feature003));
        assertFalse("f1-f4", reasoner.runsAlong(feature001, feature004));
        assertFalse("f1-f5", reasoner.runsAlong(feature001, feature005));
        assertFalse("f1-f6", reasoner.runsAlong(feature001, feature006));
        assertFalse("f1-f7", reasoner.runsAlong(feature001, feature007));

        assertFalse("f2-f1", reasoner.runsAlong(feature002, feature001));
        assertFalse("f2-f2", reasoner.runsAlong(feature002, feature002));
        assertFalse("f2-f3", reasoner.runsAlong(feature002, feature003));
        assertFalse("f2-f4", reasoner.runsAlong(feature002, feature004));
        assertFalse("f2-f5", reasoner.runsAlong(feature002, feature005));
        assertFalse("f2-f6", reasoner.runsAlong(feature002, feature006));
        assertFalse("f2-f7", reasoner.runsAlong(feature002, feature007));

        assertFalse("f3-f1", reasoner.runsAlong(feature003, feature001));
        assertFalse("f3-f2", reasoner.runsAlong(feature003, feature002));
        assertFalse("f3-f3", reasoner.runsAlong(feature003, feature003));
        assertTrue("f3-f4", reasoner.runsAlong(feature003, feature004));
        assertFalse("f3-f5", reasoner.runsAlong(feature003, feature005));
        assertFalse("f3-f6", reasoner.runsAlong(feature003, feature006));
        assertFalse("f3-f7", reasoner.runsAlong(feature003, feature007));

        assertFalse("f4-f1", reasoner.runsAlong(feature004, feature001));
        assertFalse("f4-f2", reasoner.runsAlong(feature004, feature002));
        assertTrue("f4-f3", reasoner.runsAlong(feature004, feature003));
        assertFalse("f4-f4", reasoner.runsAlong(feature004, feature004));
        assertFalse("f4-f5", reasoner.runsAlong(feature004, feature005));
        assertFalse("f4-f6", reasoner.runsAlong(feature004, feature006));
        assertFalse("f4-f7", reasoner.runsAlong(feature004, feature007));

        assertFalse("f5-f1", reasoner.runsAlong(feature005, feature001));
        assertFalse("f5-f2", reasoner.runsAlong(feature005, feature002));
        assertFalse("f5-f3", reasoner.runsAlong(feature005, feature003));
        assertFalse("f5-f4", reasoner.runsAlong(feature005, feature004));
        assertFalse("f5-f5", reasoner.runsAlong(feature005, feature005));
        assertFalse("f5-f6", reasoner.runsAlong(feature005, feature006));
        assertFalse("f5-f7", reasoner.runsAlong(feature005, feature007));

        assertFalse("f6-f1", reasoner.runsAlong(feature006, feature001));
        assertFalse("f6-f2", reasoner.runsAlong(feature006, feature002));
        assertFalse("f6-f3", reasoner.runsAlong(feature006, feature003));
        assertFalse("f6-f4", reasoner.runsAlong(feature006, feature004));
        assertFalse("f6-f5", reasoner.runsAlong(feature006, feature005));
        assertFalse("f6-f6", reasoner.runsAlong(feature006, feature006));
        assertFalse("f6-f7", reasoner.runsAlong(feature006, feature007));

        assertFalse("f7-f1", reasoner.runsAlong(feature007, feature001));
        assertFalse("f7-f2", reasoner.runsAlong(feature007, feature002));
        assertFalse("f7-f3", reasoner.runsAlong(feature007, feature003));
        assertFalse("f7-f4", reasoner.runsAlong(feature007, feature004));
        assertFalse("f7-f5", reasoner.runsAlong(feature007, feature005));
        assertFalse("f7-f6", reasoner.runsAlong(feature007, feature006));
        assertFalse("f7-f7", reasoner.runsAlong(feature007, feature007));
    }

    @Test
    public void testgetIndividualsRunningAlong() throws ComponentInitException {
        SpatialKBPostGISHelper kbHelper = getKBHelper();

        // points
        OWLIndividual feature001 = i("feature001");
        OWLIndividual geom001 = i("geom001");
        kbHelper.addSpatialFeature(feature001, geom001, "POINT(13.7985 51.0601)");

        OWLIndividual feature002 = i("feature002");
        OWLIndividual geom002 = i("geom002");
        kbHelper.addSpatialFeature(feature002, geom002, "POINT(13.7896 51.0605)");

        // line strings
        OWLIndividual feature003 = i("feature003");
        OWLIndividual geom003 = i("geom003");
        kbHelper.addSpatialFeature(feature003, geom003,
                "LINESTRING(13.7964 51.0606,13.7975 51.0605," +
                        "13.7983 51.0598,13.7991 51.0598)");

        OWLIndividual feature004 = i("feature004");
        OWLIndividual geom004 = i("geom004");
        kbHelper.addSpatialFeature(feature004, geom004,
                "LINESTRING(13.7925 51.0614,13.7949 51.0611," +
                        "13.7967 51.0607,13.7977 51.0602,13.7982 51.0600," +
                        "13.7988 51.0597,13.7993 51.0596,13.8013 51.0591," +
                        "13.8018 51.0582,13.8000 51.0578,13.7993 51.0580)");

        OWLIndividual feature005 = i("feature005");
        OWLIndividual geom005 = i("geom005");
        kbHelper.addSpatialFeature(feature005, geom005,
                "LINESTRING(13.7837 51.0619,13.7833 51.0616," +
                        "13.7843 51.0615,13.7854 51.0613)");

        // areas
        OWLIndividual feature006 = i("feature006");
        OWLIndividual geom006 = i("geom006");
        kbHelper.addSpatialFeature(feature006, geom006,
                "POLYGON((13.7998 51.0597,13.8011 51.0593," +
                        "13.8008 51.0589,13.7993 51.0594,13.7998 51.0597))");

        OWLIndividual feature007 = i("feature007");
        OWLIndividual geom007 = i("geom007");
        kbHelper.addSpatialFeature(feature007, geom007,
                "POLYGON((13.7870 51.0624,13.7869 51.0618," +
                        "13.7887 51.0617,13.7888 51.0622,13.7870 51.0624))");

        KnowledgeSource ks = new OWLAPIOntology(kbHelper.getOntology());
        ks.init();
        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dbName);
        reasoner.setDBUser(dbUser);
        reasoner.setDBUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);
        reasoner.setRunsAlongToleranceInMeters(10);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        Set<OWLIndividual> result =
                reasoner.getIndividualsRunningAlong(feature001)
                        .collect(Collectors.toSet());

        assertFalse("f1-f1", result.contains(feature001));
        assertFalse("f1-f2", result.contains(feature002));
        assertFalse("f1-f3", result.contains(feature003));
        assertFalse("f1-f4", result.contains(feature004));
        assertFalse("f1-f5", result.contains(feature005));
        assertFalse("f1-f6", result.contains(feature006));
        assertFalse("f1-f7", result.contains(feature007));

        result =
                reasoner.getIndividualsRunningAlong(feature002)
                        .collect(Collectors.toSet());

        assertFalse("f2-f1", result.contains(feature001));
        assertFalse("f2-f2", result.contains(feature002));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertFalse("f2-f7", result.contains(feature007));

        result =
                reasoner.getIndividualsRunningAlong(feature003)
                        .collect(Collectors.toSet());

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f3", result.contains(feature003));
        assertTrue("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertFalse("f3-f7", result.contains(feature007));

        result =
                reasoner.getIndividualsRunningAlong(feature004)
                        .collect(Collectors.toSet());

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertTrue("f4-f3", result.contains(feature003));
        assertFalse("f4-f4", result.contains(feature004));
        assertFalse("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertFalse("f4-f7", result.contains(feature007));

        result =
                reasoner.getIndividualsRunningAlong(feature005)
                        .collect(Collectors.toSet());

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertFalse("f5-f4", result.contains(feature004));
        assertFalse("f5-f5", result.contains(feature005));
        assertFalse("f5-f6", result.contains(feature006));
        assertFalse("f5-f7", result.contains(feature007));

        result =
                reasoner.getIndividualsRunningAlong(feature006)
                        .collect(Collectors.toSet());

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertFalse("f6-f6", result.contains(feature006));
        assertFalse("f6-f7", result.contains(feature007));

        result =
                reasoner.getIndividualsRunningAlong(feature007)
                        .collect(Collectors.toSet());

        assertFalse("f7-f1", result.contains(feature001));
        assertFalse("f7-f2", result.contains(feature002));
        assertFalse("f7-f3", result.contains(feature003));
        assertFalse("f7-f4", result.contains(feature004));
        assertFalse("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertFalse("f7-f7", result.contains(feature007));
    }

    @Test
    public void testUpdateWithSubPropertyMembers() throws ComponentInitException, OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.createOntology();
        Set<OWLAnnotation> anns = Sets.newHashSet();

        OWLNamedIndividual indiv1 = new OWLNamedIndividualImpl(IRI.create("http://ex.com/i1"));
        OWLNamedIndividual indiv2 = new OWLNamedIndividualImpl(IRI.create("http://ex.com/i2"));
        OWLNamedIndividual indiv3 = new OWLNamedIndividualImpl(IRI.create("http://ex.com/i3"));
        OWLNamedIndividual indiv4 = new OWLNamedIndividualImpl(IRI.create("http://ex.com/i4"));

        OWLObjectProperty prop1 = new OWLObjectPropertyImpl(IRI.create("http://ex.com/p1"));
        OWLObjectProperty prop2 = new OWLObjectPropertyImpl(IRI.create("http://ex.com/p2"));
        man.addAxiom(ont, new OWLSubObjectPropertyOfAxiomImpl(prop2, prop1, anns));
        man.addAxiom(ont, new OWLObjectPropertyAssertionAxiomImpl(indiv1, prop2, indiv2, anns));
        man.addAxiom(ont, new OWLObjectPropertyAssertionAxiomImpl(indiv3, prop1, indiv4, anns));


        KnowledgeSource ks = new OWLAPIOntology(ont);
        ks.init();
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
        cwr.init();

        SpatialReasonerPostGIS spatialReasoner = new SpatialReasonerPostGIS();
        spatialReasoner.setBaseReasoner(cwr);
        // No need to call init here as the method
        // updateWithSuperPropertyMembers only makes use of the base reasoner
        // (cwr in this case)

        Map<OWLIndividual, SortedSet<OWLIndividual>> propIndividuals = cwr.getPropertyMembers(prop2);

        assertTrue(propIndividuals.containsKey(indiv1));
        assertTrue(propIndividuals.containsKey(indiv2));
        assertTrue(propIndividuals.containsKey(indiv3));
        assertTrue(propIndividuals.containsKey(indiv4));

        assertTrue(propIndividuals.get(indiv1).contains(indiv2));
        assertTrue(propIndividuals.get(indiv2).isEmpty());
        assertTrue(propIndividuals.get(indiv3).isEmpty());
        assertTrue(propIndividuals.get(indiv4).isEmpty());

        spatialReasoner.updateWithSuperPropertyMembers(propIndividuals, prop2);

        assertTrue(propIndividuals.containsKey(indiv1));
        assertTrue(propIndividuals.containsKey(indiv2));
        assertTrue(propIndividuals.containsKey(indiv3));
        assertTrue(propIndividuals.containsKey(indiv4));

        assertTrue(propIndividuals.get(indiv1).contains(indiv2));
        assertTrue(propIndividuals.get(indiv2).isEmpty());
        assertTrue(propIndividuals.get(indiv3).contains(indiv4));
        assertTrue(propIndividuals.get(indiv4).isEmpty());
    }
}
