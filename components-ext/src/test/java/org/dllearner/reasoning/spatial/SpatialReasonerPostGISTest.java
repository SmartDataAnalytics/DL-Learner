package org.dllearner.reasoning.spatial;

import com.google.common.collect.Lists;
import org.apache.jena.base.Sys;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.utils.spatial.SpatialKBPostGISHelper;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplNoCompression;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        reasoner.setDbName(dbName);
        reasoner.setDbUser(dbUser);
        reasoner.setDbUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);

        Set<OWLIndividual> result =
                reasoner.getIndividualsConnectedWith(feature001).collect(Collectors.toSet());
//        System.out.println("Res: " + result);

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
//        System.out.println("Res: " + result);

        assertTrue("f2-f1", result.contains(feature001));
        assertFalse("f2-f3", result.contains(feature003));
        assertFalse("f2-f4", result.contains(feature004));
        assertFalse("f2-f5", result.contains(feature005));
        assertFalse("f2-f6", result.contains(feature006));
        assertTrue("f2-f7", result.contains(feature007));
        assertFalse("f2-f8", result.contains(feature008));
        assertFalse("f2-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature003).collect(Collectors.toSet());
//        System.out.println("Res: " + result);

        assertFalse("f3-f1", result.contains(feature001));
        assertFalse("f3-f2", result.contains(feature002));
        assertFalse("f3-f4", result.contains(feature004));
        assertFalse("f3-f5", result.contains(feature005));
        assertFalse("f3-f6", result.contains(feature006));
        assertTrue("f3-f7", result.contains(feature007));
        assertFalse("f3-f8", result.contains(feature008));
        assertFalse("f3-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature004).collect(Collectors.toSet());
//        System.out.println("Res: " + result);

        assertFalse("f4-f1", result.contains(feature001));
        assertFalse("f4-f2", result.contains(feature002));
        assertFalse("f4-f3", result.contains(feature003));
        assertTrue("f4-f5", result.contains(feature005));
        assertFalse("f4-f6", result.contains(feature006));
        assertTrue("f4-f7", result.contains(feature007));
        assertFalse("f4-f8", result.contains(feature008));
        assertFalse("f4-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature005).collect(Collectors.toSet());
//        System.out.println("Res: " + result);

        assertFalse("f5-f1", result.contains(feature001));
        assertFalse("f5-f2", result.contains(feature002));
        assertFalse("f5-f3", result.contains(feature003));
        assertTrue("f5-f4", result.contains(feature004));
        assertFalse("f5-f6", result.contains(feature006));
        assertTrue("f5-f7", result.contains(feature007));
        assertFalse("f5-f8", result.contains(feature008));
        assertFalse("f5-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature006).collect(Collectors.toSet());
//        System.out.println("Res: " + result);

        assertFalse("f6-f1", result.contains(feature001));
        assertFalse("f6-f2", result.contains(feature002));
        assertFalse("f6-f3", result.contains(feature003));
        assertFalse("f6-f4", result.contains(feature004));
        assertFalse("f6-f5", result.contains(feature005));
        assertFalse("f6-f7", result.contains(feature007));
        assertFalse("f6-f8", result.contains(feature008));
        assertFalse("f6-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature007).collect(Collectors.toSet());
//        System.out.println("Res: " + result);

        assertTrue("f7-f1", result.contains(feature001));
        assertTrue("f7-f2", result.contains(feature002));
        assertTrue("f7-f3", result.contains(feature003));
        assertTrue("f7-f4", result.contains(feature004));
        assertTrue("f7-f5", result.contains(feature005));
        assertFalse("f7-f6", result.contains(feature006));
        assertTrue("f7-f8", result.contains(feature008));
        assertFalse("f7-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature008).collect(Collectors.toSet());
//        System.out.println("Res: " + result);

        assertFalse("f8-f1", result.contains(feature001));
        assertFalse("f8-f2", result.contains(feature002));
        assertFalse("f8-f3", result.contains(feature003));
        assertFalse("f8-f4", result.contains(feature004));
        assertFalse("f8-f5", result.contains(feature005));
        assertFalse("f8-f6", result.contains(feature006));
        assertTrue("f8-f7", result.contains(feature007));
        assertFalse("f8-f9", result.contains(feature009));

        result =
                reasoner.getIndividualsConnectedWith(feature009).collect(Collectors.toSet());
//        System.out.println("Res: " + result);

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

        reasoner.setDbName(dbName);
        reasoner.setDbUser(dbUser);
        reasoner.setDbUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);

        assertTrue("f1-f2", reasoner.isConnectedWith(feature001, feature002));
        assertFalse("f1-f3", reasoner.isConnectedWith(feature001, feature003));
        assertFalse("f1-f4", reasoner.isConnectedWith(feature001, feature004));
        assertFalse("f1-f5", reasoner.isConnectedWith(feature001, feature005));
        assertFalse("f1-f6", reasoner.isConnectedWith(feature001, feature006));
        assertTrue("f1-f7", reasoner.isConnectedWith(feature001, feature007));
        assertFalse("f1-f8", reasoner.isConnectedWith(feature001, feature008));
        assertFalse("f1-f9", reasoner.isConnectedWith(feature001, feature009));

        assertTrue("f2-f1", reasoner.isConnectedWith(feature002, feature001));
        assertFalse("f2-f3", reasoner.isConnectedWith(feature002, feature003));
        assertFalse("f2-f4", reasoner.isConnectedWith(feature002, feature004));
        assertFalse("f2-f5", reasoner.isConnectedWith(feature002, feature005));
        assertFalse("f2-f6", reasoner.isConnectedWith(feature002, feature006));
        assertTrue("f2-f7", reasoner.isConnectedWith(feature002, feature007));
        assertFalse("f2-f8", reasoner.isConnectedWith(feature002, feature008));
        assertFalse("f2-f9", reasoner.isConnectedWith(feature002, feature009));

        assertFalse("f3-f1", reasoner.isConnectedWith(feature003, feature001));
        assertFalse("f3-f2", reasoner.isConnectedWith(feature003, feature002));
        assertFalse("f3-f4", reasoner.isConnectedWith(feature003, feature004));
        assertFalse("f3-f5", reasoner.isConnectedWith(feature003, feature005));
        assertFalse("f3-f6", reasoner.isConnectedWith(feature003, feature006));
        assertTrue("f3-f7", reasoner.isConnectedWith(feature003, feature007));
        assertFalse("f3-f8", reasoner.isConnectedWith(feature003, feature008));
        assertFalse("f3-f9", reasoner.isConnectedWith(feature003, feature009));

        assertFalse("f4-f1", reasoner.isConnectedWith(feature004, feature001));
        assertFalse("f4-f2", reasoner.isConnectedWith(feature004, feature002));
        assertFalse("f4-f3", reasoner.isConnectedWith(feature004, feature003));
        assertTrue("f4-f5", reasoner.isConnectedWith(feature004, feature005));
        assertFalse("f4-f6", reasoner.isConnectedWith(feature004, feature006));
        assertTrue("f4-f7", reasoner.isConnectedWith(feature004, feature007));
        assertFalse("f4-f8", reasoner.isConnectedWith(feature004, feature008));
        assertFalse("f4-f9", reasoner.isConnectedWith(feature004, feature009));

        assertFalse("f5-f1", reasoner.isConnectedWith(feature005, feature001));
        assertFalse("f5-f2", reasoner.isConnectedWith(feature005, feature002));
        assertFalse("f5-f3", reasoner.isConnectedWith(feature005, feature003));
        assertTrue("f5-f4", reasoner.isConnectedWith(feature005, feature004));
        assertFalse("f5-f6", reasoner.isConnectedWith(feature005, feature006));
        assertTrue("f5-f7", reasoner.isConnectedWith(feature005, feature007));
        assertFalse("f5-f8", reasoner.isConnectedWith(feature005, feature008));
        assertFalse("f5-f9", reasoner.isConnectedWith(feature005, feature009));

        assertFalse("f6-f1", reasoner.isConnectedWith(feature006, feature001));
        assertFalse("f6-f2", reasoner.isConnectedWith(feature006, feature002));
        assertFalse("f6-f3", reasoner.isConnectedWith(feature006, feature003));
        assertFalse("f6-f4", reasoner.isConnectedWith(feature006, feature004));
        assertFalse("f6-f5", reasoner.isConnectedWith(feature006, feature005));
        assertFalse("f6-f7", reasoner.isConnectedWith(feature006, feature007));
        assertFalse("f6-f8", reasoner.isConnectedWith(feature006, feature008));
        assertFalse("f6-f9", reasoner.isConnectedWith(feature006, feature009));

        assertTrue("f7-f1", reasoner.isConnectedWith(feature007, feature001));
        assertTrue("f7-f2", reasoner.isConnectedWith(feature007, feature002));
        assertTrue("f7-f3", reasoner.isConnectedWith(feature007, feature003));
        assertTrue("f7-f4", reasoner.isConnectedWith(feature007, feature004));
        assertTrue("f7-f5", reasoner.isConnectedWith(feature007, feature005));
        assertFalse("f7-f6", reasoner.isConnectedWith(feature007, feature006));
        assertTrue("f7-f8", reasoner.isConnectedWith(feature007, feature008));
        assertFalse("f7-f9", reasoner.isConnectedWith(feature007, feature009));

        assertFalse("f8-f1", reasoner.isConnectedWith(feature008, feature001));
        assertFalse("f8-f2", reasoner.isConnectedWith(feature008, feature002));
        assertFalse("f8-f3", reasoner.isConnectedWith(feature008, feature003));
        assertFalse("f8-f4", reasoner.isConnectedWith(feature008, feature004));
        assertFalse("f8-f5", reasoner.isConnectedWith(feature008, feature005));
        assertFalse("f8-f6", reasoner.isConnectedWith(feature008, feature006));
        assertTrue("f8-f7", reasoner.isConnectedWith(feature008, feature007));
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

        reasoner.setDbName(dbName);
        reasoner.setDbUser(dbUser);
        reasoner.setDbUserPW(dbUserPW);
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

        reasoner.setDbName(dbName);
        reasoner.setDbUser(dbUser);
        reasoner.setDbUserPW(dbUserPW);
        reasoner.setHostname(db.getContainerIpAddress());
        reasoner.setPort(db.getFirstMappedPort());
        reasoner.setBaseReasoner(cwr);

        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        kbHelper.createTables(reasoner.conn);
        kbHelper.writeSpatialInfoToPostGIS(reasoner.conn);
//        System.out.println(kbHelper.getGeometryCollection());

        assertTrue("f1-f2", reasoner.overlapsWith(feature001, feature002));
        assertFalse("f1-f3", reasoner.overlapsWith(feature001, feature003));
        assertTrue("f1-f4", reasoner.overlapsWith(feature001, feature004));
        assertFalse("f1-f5", reasoner.overlapsWith(feature001, feature005));
        assertFalse("f1-f6", reasoner.overlapsWith(feature001, feature006));
        assertFalse("f1-f7", reasoner.overlapsWith(feature001, feature007));
        assertFalse("f1-f8", reasoner.overlapsWith(feature001, feature008));
        assertFalse("f1-f9", reasoner.overlapsWith(feature001, feature009));

        assertTrue("f2-f1", reasoner.overlapsWith(feature002, feature001));
        assertFalse("f2-f3", reasoner.overlapsWith(feature002, feature003));
        assertTrue("f2-f4", reasoner.overlapsWith(feature002, feature004));
        assertFalse("f2-f5", reasoner.overlapsWith(feature002, feature005));
        assertFalse("f2-f6", reasoner.overlapsWith(feature002, feature006));
        assertFalse("f2-f7", reasoner.overlapsWith(feature002, feature007));
        assertFalse("f2-f8", reasoner.overlapsWith(feature002, feature008));
        assertFalse("f2-f9", reasoner.overlapsWith(feature002, feature009));

        assertFalse("f3-f1", reasoner.overlapsWith(feature003, feature001));
        assertFalse("f3-f2", reasoner.overlapsWith(feature003, feature002));
        assertTrue("f3-f4", reasoner.overlapsWith(feature003, feature004));
        assertFalse("f3-f5", reasoner.overlapsWith(feature003, feature005));
        assertFalse("f3-f6", reasoner.overlapsWith(feature003, feature006));
        assertFalse("f3-f7", reasoner.overlapsWith(feature003, feature007));
        assertTrue("f3-f8", reasoner.overlapsWith(feature003, feature008));
        assertFalse("f3-f9", reasoner.overlapsWith(feature003, feature009));

        assertTrue("f4-f1", reasoner.overlapsWith(feature004, feature001));
        assertTrue("f4-f2", reasoner.overlapsWith(feature004, feature002));
        assertTrue("f4-f3", reasoner.overlapsWith(feature004, feature003));
        assertTrue("f4-f5", reasoner.overlapsWith(feature004, feature005));
        assertTrue("f4-f6", reasoner.overlapsWith(feature004, feature006));
        assertFalse("f4-f7", reasoner.overlapsWith(feature004, feature007));
        assertFalse("f4-f8", reasoner.overlapsWith(feature004, feature008));  // this is maybe debatable
        assertFalse("f4-f9", reasoner.overlapsWith(feature004, feature009));

        assertFalse("f5-f1", reasoner.overlapsWith(feature005, feature001));
        assertFalse("f5-f2", reasoner.overlapsWith(feature005, feature002));
        assertFalse("f5-f3", reasoner.overlapsWith(feature005, feature003));
        assertTrue("f5-f4", reasoner.overlapsWith(feature005, feature004));
        assertTrue("f5-f6", reasoner.overlapsWith(feature005, feature006));
        assertFalse("f5-f7", reasoner.overlapsWith(feature005, feature007));
        assertFalse("f5-f8", reasoner.overlapsWith(feature005, feature008));
        assertFalse("f5-f9", reasoner.overlapsWith(feature005, feature009));

        assertFalse("f6-f1", reasoner.overlapsWith(feature006, feature001));
        assertFalse("f6-f2", reasoner.overlapsWith(feature006, feature002));
        assertFalse("f6-f3", reasoner.overlapsWith(feature006, feature003));
        assertTrue("f6-f4",  reasoner.overlapsWith(feature006, feature004));
        assertTrue("f6-f5",  reasoner.overlapsWith(feature006, feature005));
        assertTrue("f6-f7",  reasoner.overlapsWith(feature006, feature007));
        assertFalse("f6-f8", reasoner.overlapsWith(feature006, feature008));
        assertFalse("f6-f9", reasoner.overlapsWith(feature006, feature009));

        assertFalse("f7-f1", reasoner.overlapsWith(feature007, feature001));
        assertFalse("f7-f2", reasoner.overlapsWith(feature007, feature002));
        assertFalse("f7-f3", reasoner.overlapsWith(feature007, feature003));
        assertFalse("f7-f4", reasoner.overlapsWith(feature007, feature004));
        assertFalse("f7-f5", reasoner.overlapsWith(feature007, feature005));
        assertTrue("f7-f6", reasoner.overlapsWith(feature007, feature006));
        assertFalse("f7-f8", reasoner.overlapsWith(feature007, feature008));
        assertFalse("f7-f9", reasoner.overlapsWith(feature007, feature009));

        assertFalse("f8-f1", reasoner.overlapsWith(feature008, feature001));
        assertFalse("f8-f2", reasoner.overlapsWith(feature008, feature002));
        assertTrue("f8-f3", reasoner.overlapsWith(feature008, feature003));
        assertFalse("f8-f4", reasoner.overlapsWith(feature008, feature004));
        assertFalse("f8-f5", reasoner.overlapsWith(feature008, feature005));
        assertFalse("f8-f6", reasoner.overlapsWith(feature008, feature006));
        assertFalse("f8-f7", reasoner.overlapsWith(feature008, feature007));
        assertTrue("f8-f9", reasoner.overlapsWith(feature008, feature009));

        assertFalse("f9-f1", reasoner.overlapsWith(feature009, feature001));
        assertFalse("f9-f2", reasoner.overlapsWith(feature009, feature002));
        assertFalse("f9-f3", reasoner.overlapsWith(feature009, feature003));
        assertFalse("f9-f4", reasoner.overlapsWith(feature009, feature004));
        assertFalse("f9-f5", reasoner.overlapsWith(feature009, feature005));
        assertFalse("f9-f6", reasoner.overlapsWith(feature009, feature006));
        assertFalse("f9-f7", reasoner.overlapsWith(feature009, feature007));
        assertTrue("f9-f8", reasoner.overlapsWith(feature009, feature008));
    }
}
