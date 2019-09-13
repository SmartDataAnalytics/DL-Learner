package org.dllearner.reasoning.spatial;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.OWLObjectUnionOfImplExt;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.vocabulary.spatial.SpatialVocabulary;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class SpatialReasonerPostGISTest {
    private SpatialReasonerPostGIS spatialReasoner = null;

    private OWLDataFactory df = OWLManager.getOWLDataFactory();
    private String defaultPrefix = "http://dl-learner.org/ont/spatial#";

    private void initGeomPropertyPath(SpatialReasonerPostGIS reasoner) {
        reasoner.clearGeometryPropertyPaths();

        List<OWLProperty> geomPropertyPath = Lists.newArrayList(
                new OWLObjectPropertyImpl(IRI.create(
                        "http://www.opengis.net/ont/geosparql#hasGeometry")),
                new OWLDataPropertyImpl(IRI.create(
                        "http://www.opengis.net/ont/geosparql#asWKT"))
        );
        reasoner.addGeometryPropertyPath(geomPropertyPath);
    }

    private SpatialReasonerPostGIS getReasoner() throws ComponentInitException {
        if (spatialReasoner == null) {
            String exampleFilePath =
                    SpatialReasonerPostGIS.class.getClassLoader()
                            .getResource("example_data.owl").getFile();
            KnowledgeSource ks = new OWLFile(exampleFilePath);
            ks.init();
            ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
            spatialReasoner = new SpatialReasonerPostGIS(
                    cwr, new DBConnectionSetting(
                    "localhost",5432, "dllearner",
                    "postgres", "postgres"));
            spatialReasoner.init();
            initGeomPropertyPath(spatialReasoner);
        }

        return spatialReasoner;
    }

    @Ignore
    @Test
    public void testAddGeometryPropertyPath() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        OWLObjectProperty op1 = new OWLObjectPropertyImpl(
                IRI.create(defaultPrefix + "dummy_obj_prop1"));
        OWLObjectProperty op2 = new OWLObjectPropertyImpl(
                IRI.create(defaultPrefix + "dummy_obj_prop2"));
        OWLObjectProperty op3 = new OWLObjectPropertyImpl(
                IRI.create(defaultPrefix + "dummy_obj_prop3"));
        OWLDataProperty dp1 = new OWLDataPropertyImpl(
                IRI.create(defaultPrefix + "dummy_data_prop1"));
        OWLDataProperty dp2 = new OWLDataPropertyImpl(
                IRI.create(defaultPrefix + "dummy_data_prop2"));

        List<OWLProperty> emptyPropertyPath = new ArrayList<>();
        try {
            reasoner.addGeometryPropertyPath(emptyPropertyPath);
            fail();
        } catch (AssertionError e) {
            // Nothing to do here
        }

        List<OWLProperty> justObjectProperty = Lists.newArrayList(op1);
        try {
            reasoner.addGeometryPropertyPath(justObjectProperty);
            fail();
        } catch (AssertionError e) {
            // Nothing to do here
        }

        List<OWLProperty> justObjectProperties = Lists.newArrayList(op1, op2, op3);
        try {
            reasoner.addGeometryPropertyPath(justObjectProperties);
            fail();
        } catch (AssertionError e) {
            // Nothing to do here
        }

        List<OWLProperty> multipleDataProperties = Lists.newArrayList(op1, dp1, op2, dp2);
        try {
            reasoner.addGeometryPropertyPath(multipleDataProperties);
            fail();
        } catch (AssertionError e) {
            // Nothing to do here
        }

        List<OWLProperty> propPath = Lists.newArrayList(op1, op2, op3, dp1);
        try {
            reasoner.addGeometryPropertyPath(propPath);
        } catch (AssertionError e) {
            fail();
        }

        propPath = Lists.newArrayList(op1, op2, dp1);
        try {
            reasoner.addGeometryPropertyPath(propPath);
        } catch (AssertionError e) {
            fail();
        }

        propPath = Lists.newArrayList(op1, dp1);
        try {
            reasoner.addGeometryPropertyPath(propPath);
        } catch (AssertionError e) {
            fail();
        }

        propPath = Lists.newArrayList(dp1);
        try {
            reasoner.addGeometryPropertyPath(propPath);
        } catch (AssertionError e) {
            fail();
        }

        initGeomPropertyPath(reasoner);
    }

    @Ignore
    @Test
    public void testGetClasses() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        assertTrue(reasoner.getClasses().containsAll(SpatialVocabulary.spatialClasses));
    }

    @Ignore
    @Test
    public void testGetIndividuals() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        OWLClass notSpatial = df.getOWLClass(
                IRI.create(defaultPrefix + "SomethingNonSpatial"));
        OWLClass notSpatial2 = df.getOWLClass(
                IRI.create(defaultPrefix + "SomethingMoreSpecialButStillNonSpatial"));

        // For non-spatial class expressions the getIndividualsImpl call should
        // be delegated to the base reasoner
        assertEquals(
                reasoner.reasoner.getIndividuals(notSpatial),
                reasoner.getIndividualsImpl(notSpatial));

        assertEquals(
                reasoner.reasoner.getIndividuals(notSpatial2),
                reasoner.getIndividualsImpl(notSpatial2));
    }

    @Ignore
    @Test
    public void testGetIndividualsOWLObjectIntersectionOf() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();

        OWLObjectIntersectionOf intersectionWithoutSpatialPart = df.getOWLObjectIntersectionOf(
                df.getOWLClass(IRI.create(defaultPrefix + "SomethingNonSpatial")),
                df.getOWLClass(IRI.create(defaultPrefix + "SomethingMoreSpecialButStillNonSpatial")));
        OWLIndividual expectedIndividual = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "nonspatial_individual_01"));

        assertEquals(
                Sets.newHashSet(expectedIndividual),
                reasoner.getIndividualsOWLObjectIntersectionOf(intersectionWithoutSpatialPart));
    }

    @Ignore
    @Test
    public void testGetIndividualsOWLObjectUnionOfImplExt() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();

        OWLObjectUnionOfImplExt union = new OWLObjectUnionOfImplExt(Sets.newHashSet(
                df.getOWLClass(IRI.create(defaultPrefix + "Footway")),
                df.getOWLClass(IRI.create(defaultPrefix + "AreaFeature"))));
        OWLIndividual expectedIndividual1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "kipsdorfer_str_11"));
        OWLIndividual expectedIndividual2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "area_inside_bhf_neustadt"));
        OWLIndividual expectedIndividual3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "bahnhof_dresden_neustadt_building"));
        OWLIndividual expectedIndividual4 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "toeplerpark_soccer_field"));
        OWLIndividual expectedIndividual5 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix +
                        "toeplerpark_soccer_field_eighteen_yard_area"));
        OWLIndividual expectedIndividual6 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix +
                        "toeplerpark_soccer_field_center_circle"));

        assertEquals(
                Sets.newHashSet(
                        expectedIndividual1, expectedIndividual2,
                        expectedIndividual3, expectedIndividual4,
                        expectedIndividual5, expectedIndividual6),
                reasoner.getIndividualsOWLObjectUnionOfImplExt(union));
    }

    @Ignore
    @Test
    public void testGetIndividualsOWLObjectSomeValuesFrom() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();

        OWLObjectSomeValuesFrom nonSpatialCE = df.getOWLObjectSomeValuesFrom(
                df.getOWLObjectProperty(
                        IRI.create(defaultPrefix + "nonSpatialObjectProperty01")),
                df.getOWLClass(
                        IRI.create(defaultPrefix + "SomethingNonSpatial")));
        OWLIndividual expectedIndividual = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "nonspatial_individual_03"));

        assertEquals(
                Sets.newHashSet(expectedIndividual),
                reasoner.getIndividualsOWLObjectSomeValuesFrom(nonSpatialCE));

        // -----------------------------------

        // :isNear
        reasoner.setNearRadiusInMeters(50);
        OWLObjectSomeValuesFrom spatialCE = df.getOWLObjectSomeValuesFrom(
                SpatialVocabulary.isNear,
                df.getOWLClass(IRI.create(defaultPrefix + "StationBuilding")));
        OWLIndividual expectedIndividual1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt"));
        OWLIndividual expectedIndividual2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt_02"));
        OWLIndividual expectedIndividual3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "bahnhof_dresden_neustadt_building"));
        OWLIndividual expectedIndividual4 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "area_inside_bhf_neustadt"));
        OWLIndividual expectedIndividual5 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_outside_bhf_neustadt_1"));
        OWLIndividual expectedIndividual6 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "way_inside_bhf_neustadt"));

        assertEquals(
                Sets.newHashSet(
                        expectedIndividual1, expectedIndividual2,
                        expectedIndividual3, expectedIndividual4,
                        expectedIndividual5, expectedIndividual6),
                reasoner.getIndividualsOWLObjectSomeValuesFrom(spatialCE));

        // :isInside
        spatialCE = df.getOWLObjectSomeValuesFrom(
                SpatialVocabulary.isInside,
                df.getOWLClass(IRI.create(defaultPrefix + "StationBuilding")));

        assertEquals(
                Sets.newHashSet(
                        expectedIndividual1, expectedIndividual2,
                        expectedIndividual4, expectedIndividual6),
                reasoner.getIndividualsOWLObjectSomeValuesFrom(spatialCE));
    }

    @Ignore
    @Test
    public void testGetIndividualsOWLObjectMaxCardinality() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();

        OWLObjectMaxCardinality nonSpatialCE = df.getOWLObjectMaxCardinality(
                2,
                df.getOWLObjectProperty(
                        IRI.create(defaultPrefix + "nonSpatialObjectProperty01")),
                df.getOWLClass(
                        IRI.create(defaultPrefix + "SomethingNonSpatial")));
        OWLIndividual expectedIndividual1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "nonspatial_individual_03"));

        assertTrue(
                reasoner.getIndividualsOWLObjectMaxCardinality(nonSpatialCE)
                        .contains(expectedIndividual1));

        nonSpatialCE = df.getOWLObjectMaxCardinality(
                1,
                df.getOWLObjectProperty(
                        IRI.create(defaultPrefix + "nonSpatialObjectProperty01")),
                df.getOWLClass(
                        IRI.create(defaultPrefix + "SomethingNonSpatial")));

        assertFalse(
                reasoner.getIndividualsOWLObjectMaxCardinality(nonSpatialCE)
                        .contains(expectedIndividual1));

        // -------------------------------------

        // :isInside
        OWLObjectMaxCardinality spatialCE = df.getOWLObjectMaxCardinality(
                1,
                SpatialVocabulary.isInside,
                df.getOWLClass(IRI.create(defaultPrefix + "AreaFeature")));
        OWLIndividual expectedIndividual2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt_02"));
        assertFalse(
                reasoner.getIndividualsOWLObjectMaxCardinality(spatialCE)
                        .contains(expectedIndividual2));

        spatialCE = df.getOWLObjectMaxCardinality(
                2,
                SpatialVocabulary.isInside,
                df.getOWLClass(IRI.create(defaultPrefix + "AreaFeature")));
        assertTrue(
                reasoner.getIndividualsOWLObjectMaxCardinality(spatialCE)
                        .contains(expectedIndividual2));

        // :isNear
        reasoner.setNearRadiusInMeters(50);
        spatialCE = df.getOWLObjectMaxCardinality(
                1,
                SpatialVocabulary.isNear,
                df.getOWLClass(IRI.create(defaultPrefix + "LineFeature")));
        OWLIndividual expectedIndividual3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_on_turnerweg"));

        assertFalse(
                reasoner.getIndividualsOWLObjectMaxCardinality(spatialCE)
                        .contains(expectedIndividual3));

        spatialCE = df.getOWLObjectMaxCardinality(
                2,
                SpatialVocabulary.isNear,
                df.getOWLClass(IRI.create(defaultPrefix + "LineFeature")));

        assertTrue(
                reasoner.getIndividualsOWLObjectMaxCardinality(spatialCE)
                        .contains(expectedIndividual3));
    }

    @Ignore
    @Test
    public void testGetIndividualsOWLObjectMinCardinality() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();

        OWLObjectMinCardinality nonSpatialCE = df.getOWLObjectMinCardinality(
                2,
                df.getOWLObjectProperty(
                        IRI.create(defaultPrefix + "nonSpatialObjectProperty01")),
                df.getOWLClass(
                        IRI.create(defaultPrefix + "SomethingNonSpatial")));
        OWLIndividual expectedIndividual1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "nonspatial_individual_03"));

        assertEquals(
                Sets.newHashSet(expectedIndividual1),
                reasoner.getIndividualsOWLObjectMinCardinality(nonSpatialCE));

        // -------------------------------------

        // :isInside
        OWLObjectMinCardinality spatialCE = df.getOWLObjectMinCardinality(
                2,
                SpatialVocabulary.isInside,
                df.getOWLClass(IRI.create(defaultPrefix + "AreaFeature")));
        OWLIndividual expectedIndividual2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt_02"));

        assertEquals(
                Sets.newHashSet(expectedIndividual2),
                reasoner.getIndividualsOWLObjectMinCardinality(spatialCE));

        // :isNear
        reasoner.setNearRadiusInMeters(50);
        spatialCE = df.getOWLObjectMinCardinality(
                3,
                SpatialVocabulary.isNear,
                df.getOWLClass(IRI.create(defaultPrefix + "PointFeature")));
        OWLIndividual expectedIndividual3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "bahnhof_dresden_neustadt_building"));
        OWLIndividual expectedIndividual4 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "move_02"));

        assertEquals(Sets.newHashSet(
                expectedIndividual3, expectedIndividual4),
                reasoner.getIndividualsOWLObjectMinCardinality(spatialCE));
    }

    @Ignore
    @Test
    public void testGetIndividualsOWLObjectAllValuesFrom() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        OWLObjectAllValuesFrom nonSpatialCE = df.getOWLObjectAllValuesFrom(
                df.getOWLObjectProperty(
                        IRI.create(defaultPrefix + "nonSpatialObjectProperty01")),
                df.getOWLClass(
                        IRI.create(defaultPrefix + "SomethingNonSpatial")));
        OWLIndividual expectedIndividual1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "nonspatial_individual_03"));

        assertTrue(
                reasoner.getIndividualsOWLObjectAllValuesFrom(nonSpatialCE)
                        .contains(expectedIndividual1)
                /* + also contains all other individuals not having a value
                 *   assigned via :nonSpatialObjectProperty01 */);


        nonSpatialCE = df.getOWLObjectAllValuesFrom(
                df.getOWLObjectProperty(IRI.create(defaultPrefix + "nonSpatialObjectProperty01")),
                df.getOWLClass(
                        IRI.create(defaultPrefix + "SomethingMoreSpecialButStillNonSpatial")));

        assertFalse(reasoner.getIndividualsOWLObjectAllValuesFrom(nonSpatialCE)
                .contains(expectedIndividual1));

        // ----------------------------------

        // :isInside
        OWLObjectAllValuesFrom spatialCE = df.getOWLObjectAllValuesFrom(
                SpatialVocabulary.isInside,
                df.getOWLClass(IRI.create(defaultPrefix + "AreaFeature"))
        );
        expectedIndividual1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "area_inside_bhf_neustadt"));
        OWLIndividual expectedIndividual2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt"));
        OWLIndividual expectedIndividual3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt_02"));
        OWLIndividual expectedIndividual4 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "way_inside_bhf_neustadt"));
        OWLIndividual expectedIndividual5 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix +
                        "toeplerpark_soccer_field_eighteen_yard_area"));
        OWLIndividual expectedIndividual6 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "" +
                        "toeplerpark_soccer_field_center_circle"));

        assertEquals(
                Sets.newHashSet(
                        expectedIndividual1, expectedIndividual2,
                        expectedIndividual3, expectedIndividual4,
                        expectedIndividual5, expectedIndividual6),
                reasoner.getIndividualsOWLObjectAllValuesFrom(spatialCE));

        // :isNear
        reasoner.setNearRadiusInMeters(50);
        reasoner.setIsIsNearRelationReflexive(false);
        spatialCE = df.getOWLObjectAllValuesFrom(
                SpatialVocabulary.isNear,
                df.getOWLClass(IRI.create(defaultPrefix + "AreaFeature")));
        expectedIndividual1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_outside_bhf_neustadt_1"));
        expectedIndividual2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "toeplerpark_soccer_field"));
        expectedIndividual3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix +
                        "toeplerpark_soccer_field_eighteen_yard_area"));
        expectedIndividual4 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix +
                        "toeplerpark_soccer_field_center_circle"));

        assertEquals(
                Sets.newHashSet(
                        expectedIndividual1, expectedIndividual2,
                        expectedIndividual3, expectedIndividual4),
                reasoner.getIndividualsOWLObjectAllValuesFrom(spatialCE));
    }

    @Ignore
    @Test
    public void testGetConnectedIndividuals() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        OWLIndividual point1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "holzhofgasse_no_21"));
        OWLIndividual point2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "ksc_dresden"));
        OWLIndividual point3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_on_turnerweg"));
        OWLIndividual point4 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt"));
        OWLIndividual point5 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "elbwiesen_point_01"));
        OWLIndividual line1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "turnerweg"));
        OWLIndividual line2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "turnerweg_part"));
        OWLIndividual line3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "elbwiesen_line_01"));
        OWLIndividual area1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "bahnhof_dresden_neustadt_building"));
        OWLIndividual area2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "elbwiesen_area_01"));
        OWLIndividual area3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "elbwiesen_area_02"));

        // point - point
        reasoner.setIsConnectednessReflexive(false);
        List<OWLIndividual> connectedIndividuals =
                reasoner.getConnectedIndividuals(point1).collect(Collectors.toList());

        assertEquals(1, connectedIndividuals.size());
        assertTrue(connectedIndividuals.contains(point2));

        reasoner.setIsConnectednessReflexive(true);
        connectedIndividuals =
                reasoner.getConnectedIndividuals(point1).collect(Collectors.toList());

        assertEquals(2, connectedIndividuals.size());
        assertTrue(connectedIndividuals.contains(point1));
        assertTrue(connectedIndividuals.contains(point2));

        // point - line
        reasoner.setIsConnectednessReflexive(false);
        connectedIndividuals =
                reasoner.getConnectedIndividuals(point3).collect(Collectors.toList());

        assertEquals(2, connectedIndividuals.size());
        assertTrue(connectedIndividuals.contains(line1));
        assertTrue(connectedIndividuals.contains(line2));

        connectedIndividuals =
                reasoner.getConnectedIndividuals(line1).collect(Collectors.toList());

        assertEquals(2, connectedIndividuals.size());
        assertTrue(connectedIndividuals.contains(point3));

        // point - area
        reasoner.setIsConnectednessReflexive(false);
        connectedIndividuals =
                reasoner.getConnectedIndividuals(point4).collect(Collectors.toList());

        assertEquals(1, connectedIndividuals.size());
        assertTrue(connectedIndividuals.contains(area1));

        connectedIndividuals =
                reasoner.getConnectedIndividuals(area1).collect(Collectors.toList());
        assertTrue(connectedIndividuals.contains(point4));

        // line - line
        reasoner.setIsConnectednessReflexive(false);
        connectedIndividuals =
                reasoner.getConnectedIndividuals(line1).collect(Collectors.toList());

        assertEquals(2, connectedIndividuals.size());
        assertTrue(connectedIndividuals.contains(line2));

        connectedIndividuals =
                reasoner.getConnectedIndividuals(line2).collect(Collectors.toList());

        assertEquals(2, connectedIndividuals.size());
        assertTrue(connectedIndividuals.contains(line1));

        // line - area
        reasoner.setIsConnectednessReflexive(false);
        connectedIndividuals =
                reasoner.getConnectedIndividuals(line3).collect(Collectors.toList());

        assertEquals(3, connectedIndividuals.size());
        assertTrue(connectedIndividuals.contains(area2));
        assertTrue(connectedIndividuals.contains(area3));

        connectedIndividuals =
                reasoner.getConnectedIndividuals(area2).collect(Collectors.toList());

        assertTrue(connectedIndividuals.contains(line3));

        // area - area
        reasoner.setIsConnectednessReflexive(false);

        connectedIndividuals =
                reasoner.getConnectedIndividuals(area2).collect(Collectors.toList());

        assertEquals(3, connectedIndividuals.size());
        assertTrue(connectedIndividuals.contains(area3));
        assertTrue(connectedIndividuals.contains(line3));
        assertTrue(connectedIndividuals.contains(point5));
    }

    @Ignore
    @Test
    public void testIsNear() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        OWLIndividual point1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt"));
        OWLIndividual point2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt_02"));
        OWLIndividual point3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_on_turnerweg"));

        reasoner.setNearRadiusInMeters(50);
        reasoner.setIsIsNearRelationReflexive(true);
        assertTrue(reasoner.isNear(point1, point1));

        reasoner.setIsIsNearRelationReflexive(false);
        assertFalse(reasoner.isNear(point1, point1));

        assertTrue(reasoner.isNear(point1, point2));

        assertFalse(reasoner.isNear(point1, point3));

        // reset to default value
        reasoner.setIsIsNearRelationReflexive(true);
    }

    @Ignore
    @Test
    public void testGetNearSpatialIndividuals() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();

        OWLIndividual feature1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "turnerweg"));
        OWLIndividual feature2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "turnerweg_part"));
        OWLIndividual feature3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "pos_on_turnerweg"));

        reasoner.setNearRadiusInMeters(50);
        reasoner.setIsIsNearRelationReflexive(true);

        assertEquals(
                Sets.newHashSet(feature1, feature2, feature3),
                reasoner.getNearSpatialIndividuals(feature3));

        reasoner.setIsIsNearRelationReflexive(false);
        assertEquals(
                Sets.newHashSet(feature1, feature2),
                reasoner.getNearSpatialIndividuals(feature3));

        // reset to default value
        reasoner.setIsIsNearRelationReflexive(true);
    }

    @Ignore
    @Test
    public void testIsInside() throws ComponentInitException {
        SpatialReasoner reasoner = getReasoner();
        OWLIndividual pointInsideBuilding = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt"));
        OWLIndividual pointOutsideBuildung1 = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "pos_outside_bhf_neustadt_1"));
        OWLIndividual pointOutsideBuildung2 = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "pos_outside_bhf_neustadt_2"));
        OWLIndividual building = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "bahnhof_dresden_neustadt_building"));

        assertTrue(reasoner.isInside(pointInsideBuilding, building));
        assertFalse(reasoner.isInside(pointOutsideBuildung1, building));
        assertFalse(reasoner.isInside(pointOutsideBuildung2, building));
    }

    @Ignore
    @Test
    public void testRunsAlong() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();

        OWLIndividual street_01 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "kipsdorfer_str_13"));
        OWLIndividual move = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "move_01"));

        reasoner.setRunsAlongToleranceInMeters(20);
        assertTrue(reasoner.runsAlong(move, street_01));

        // -----
        OWLIndividual street_02 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "bergmannstr_05"));
        // wormser_str_07 --> not
        OWLIndividual street_03 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "wormser_str_07"));
        OWLIndividual street_04 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "wormser_str_08"));
        OWLIndividual street_05 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "wormser_str_09"));
        // wormser_str_10 --> not
        OWLIndividual street_06 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "wormser_str_10"));
        OWLIndividual street_07 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "tittmannstr_03"));
        // tittmannstr_04 --> not
        OWLIndividual street_08 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "tittmannstr_04"));

        move = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "move_02"));

        assertTrue(reasoner.runsAlong(move, street_02));
        assertFalse(reasoner.runsAlong(move, street_03));
        assertTrue(reasoner.runsAlong(move, street_04));
        assertTrue(reasoner.runsAlong(move, street_05));
        assertFalse(reasoner.runsAlong(move, street_06));
        assertTrue(reasoner.runsAlong(move, street_07));
        assertFalse(reasoner.runsAlong(move, street_08));
    }

    @Ignore
    @Test
    public void testGetContainedSpatialIndividuals() throws ComponentInitException {
        SpatialReasoner reasoner = getReasoner();

        OWLIndividual somePoint = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "pos_outside_bhf_neustadt_2"));

        // Getting all features contained inside a point feature without
        // considering the point feature itself --> should be empty
        Set<OWLIndividual> containedIndividuals =
                reasoner.getContainedSpatialIndividuals(somePoint);
        assertTrue(containedIndividuals.isEmpty());

        // Getting all features contained inside a point feature, including the
        // point feature itself into the result set --> should just contain the
        // point feature
        ((SpatialReasonerPostGIS) reasoner).setIsContainmentRelationReflexive(true);
        containedIndividuals =
                reasoner.getContainedSpatialIndividuals(somePoint);
        assertEquals(1, containedIndividuals.size());
        assertTrue(containedIndividuals.contains(somePoint));

        // -
        OWLIndividual turnerweg = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "turnerweg"));
        OWLIndividual turnerwegPart = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "turnerweg_part"));
        OWLIndividual pointOnTurnerWeg = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "pos_on_turnerweg"));

        // Getting all features contained inside a line feature without
        // considering the line feature itself --> should be :turnerweg_part and
        // :pos_on_turnerweg
        ((SpatialReasonerPostGIS) reasoner).setIsContainmentRelationReflexive(false);
        containedIndividuals = reasoner.getContainedSpatialIndividuals(turnerweg);
        assertEquals(2, containedIndividuals.size());
        assertTrue(containedIndividuals.contains(turnerwegPart));
        assertTrue(containedIndividuals.contains(pointOnTurnerWeg));

        // Getting all features contained inside a line feature, including the
        // line feature itself into the result set --> should be
        // :turnerweg_part, :pos_on_turnerweg and :turnerweg itself
        ((SpatialReasonerPostGIS) reasoner).setIsContainmentRelationReflexive(true);
        containedIndividuals = reasoner.getContainedSpatialIndividuals(turnerweg);
        assertEquals(3, containedIndividuals.size());
        assertTrue(containedIndividuals.contains(turnerwegPart));
        assertTrue(containedIndividuals.contains(pointOnTurnerWeg));
        assertTrue(containedIndividuals.contains(turnerweg));

        // -
        OWLIndividual pointInsideBuilding = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt"));
        OWLIndividual pointInsideBuilding2 = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "pos_inside_bhf_neustadt_02"));
        OWLIndividual areaInsideBuilding = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "area_inside_bhf_neustadt"));
        OWLIndividual wayInsideBuilding = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "way_inside_bhf_neustadt"));
        OWLIndividual building = new OWLNamedIndividualImpl(
                IRI.create(defaultPrefix + "bahnhof_dresden_neustadt_building"));

        // Getting all features contained in an area feature without considering
        // the area feature itself --> should be :area_inside_bhf_neustadt,
        // :way_inside_bhf_neustadt, :pos_inside_bhf_neustadt and
        // :pos_inside_bhf_neustadt_02
        ((SpatialReasonerPostGIS) reasoner).setIsContainmentRelationReflexive(false);
        containedIndividuals = reasoner.getContainedSpatialIndividuals(building);

        assertEquals(4, containedIndividuals.size());
        assertTrue(containedIndividuals.contains(pointInsideBuilding));
        assertTrue(containedIndividuals.contains(pointInsideBuilding2));
        assertTrue(containedIndividuals.contains(wayInsideBuilding));
        assertTrue(containedIndividuals.contains(areaInsideBuilding));
        assertFalse(containedIndividuals.contains(building));

        // Getting all features contained in an area feature, including the area
        // feature itself --> should be :area_inside_bhf_neustadt,
        // :way_inside_bhf_neustadt, :pos_inside_bhf_neustadt, and
        // :bahnhof_dresden_neustadt_building itself
        ((SpatialReasonerPostGIS) reasoner).setIsContainmentRelationReflexive(true);
        containedIndividuals = reasoner.getContainedSpatialIndividuals(building);

        assertEquals(5, containedIndividuals.size());
        assertTrue(containedIndividuals.contains(pointInsideBuilding));
        assertTrue(containedIndividuals.contains(pointInsideBuilding2));
        assertTrue(containedIndividuals.contains(wayInsideBuilding));
        assertTrue(containedIndividuals.contains(areaInsideBuilding));
        assertTrue(containedIndividuals.contains(building));

        // reset default value
        spatialReasoner.setIsContainmentRelationReflexive(false);
    }

    @Ignore
    @Test
    public void testGetSpatialIndividualsOnWhichRunsAlong() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();

        OWLIndividual move = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "move_01"));

        OWLIndividual expectedIndividual1 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "kipsdorfer_str_13"));

        assertEquals(
                Sets.newHashSet(expectedIndividual1),
                reasoner.getSpatialIndividualsOnWhichRunsAlong(move));

        // ----

        move = df.getOWLNamedIndividual(IRI.create(defaultPrefix + "move_02"));
        OWLIndividual expectedIndividual2 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "bergmannstr_05"));
        OWLIndividual expectedIndividual3 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "wormser_str_08"));
        OWLIndividual expectedIndividual4 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "wormser_str_09"));
        OWLIndividual expectedIndividual5 = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "tittmannstr_03"));

        assertEquals(
                Sets.newHashSet(
                        expectedIndividual2,
                        expectedIndividual3,
                        expectedIndividual4,
                        expectedIndividual5),
                reasoner.getSpatialIndividualsOnWhichRunsAlong(move));
    }

    @Ignore
    @Test
    public void testPasses() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        reasoner.setNearRadiusInMeters(20);
        OWLIndividual move = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "move_02"));
        OWLIndividual pub_fitz = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix  + "pub_fritz"));
        OWLIndividual bhf_neustadt = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "bahnhof_dresden_neustadt_building"));

        assertTrue(reasoner.passes(move, pub_fitz));
        assertFalse(reasoner.passes(move, bhf_neustadt));
    }

    @Ignore 
    @Test
    public void testGetPassedSpatialIndividuals() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        reasoner.setNearRadiusInMeters(20);
        OWLIndividual move = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "move_02"));
        OWLIndividual bergmannstr_05 = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "bergmannstr_05"));
        OWLIndividual wormser_str_08 = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "wormser_str_08"));
        OWLIndividual pub_fitz = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix  + "pub_fritz"));
        OWLIndividual wormser_str_09 = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "wormser_str_09"));
        OWLIndividual wormser_str_10 = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "wormser_str_10"));
        OWLIndividual tittmannstr_03 = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "tittmannstr_03"));
        OWLIndividual recyclingContainer = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "recycling_container_xyz"));

        Set<OWLIndividual> passed = reasoner.getPassedSpatialIndividuals(move);
        assertTrue(passed.contains(bergmannstr_05));
        assertTrue(passed.contains(wormser_str_08));
        assertTrue(passed.contains(pub_fitz));
        assertTrue(passed.contains(wormser_str_09));
        assertTrue(passed.contains(wormser_str_10));
        assertTrue(passed.contains(tittmannstr_03));
        assertTrue(passed.contains(recyclingContainer));
        assertEquals(7, passed.size());
    }

    @Ignore 
    @Test
    public void testGetPassingSpatialIndividuals() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        reasoner.setNearRadiusInMeters(20);

        OWLIndividual move = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "move_02"));
        OWLIndividual bergmannstr_05 = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "bergmannstr_05"));
        OWLIndividual tittmannstr_03 = df.getOWLNamedIndividual(IRI.create(
                defaultPrefix + "tittmannstr_03"));

        Set<OWLIndividual> passing =
                reasoner.getPassingSpatialIndividuals(bergmannstr_05);
        assertTrue(passing.contains(move));

        passing = reasoner.getPassingSpatialIndividuals(tittmannstr_03);
        assertTrue(passing.contains(move));
    }

    @Ignore 
    @Test
    public void testIsNonTangentialProperPartOf() {
        fail();
    }

    @Ignore 
    @Test
    public void testGetIndividualsNearStartPoint() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        reasoner.setNearRadiusInMeters(35);

        OWLIndividual pointNearStartPoint = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "bergmannstr_no_31"));
        OWLIndividual move = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "move_02"));
        Set<OWLIndividual> indivsNearStartPoint =
                reasoner.getIndividualsNearStartPoint(move);

        // 4: pointNearStartPoint + two Bergmannstr segments + Wormster Str. segment
        assertEquals(4, indivsNearStartPoint.size());
        assertTrue(indivsNearStartPoint.contains(pointNearStartPoint));
    }

    @Ignore 
    @Test
    public void testGetIndividualsNearEndPoint() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        reasoner.setNearRadiusInMeters(20);

        OWLIndividual pointNearEndPoint = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "recycling_container_xyz"));
        OWLIndividual move = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "move_02"));
        Set<OWLIndividual> indivsNearEndPoint =
                reasoner.getIndividualsNearEndPoint(move);

        // 2: pointNearEndPoint + Tittmannstr segment
        assertEquals(2, indivsNearEndPoint.size());
        assertTrue(indivsNearEndPoint.contains(pointNearEndPoint));
    }

    @Ignore
    @Test
    public void testStartsNear() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        reasoner.setNearRadiusInMeters(35);

        OWLIndividual pointNearStartPoint = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "bergmannstr_no_31"));
        OWLIndividual pointNotNearStartPoint = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "recycling_container_xyz"));
        OWLIndividual move = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "move_02"));

        assertTrue(reasoner.startsNear(move, pointNearStartPoint));
        assertFalse(reasoner.startsNear(move, pointNotNearStartPoint));
    }

    @Ignore
    @Test
    public void testEndsNear() throws ComponentInitException {
        SpatialReasonerPostGIS reasoner = getReasoner();
        reasoner.setNearRadiusInMeters(20);

        OWLIndividual pointNotNearEndPoint = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "bergmannstr_no_31"));
        OWLIndividual pointNearEndPoint = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "recycling_container_xyz"));
        OWLIndividual move = df.getOWLNamedIndividual(
                IRI.create(defaultPrefix + "move_02"));

        assertTrue(reasoner.endsNear(move, pointNearEndPoint));
        assertFalse(reasoner.endsNear(move, pointNotNearEndPoint));
    }
}
