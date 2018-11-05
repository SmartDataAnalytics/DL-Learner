package org.dllearner.reasoning.spatial;

import com.google.common.collect.Lists;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class SpatialReasonerPostGISTest {
    private SpatialReasonerPostGIS spatialReasoner = null;

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
                IRI.create("http://dl-learner.org/ont/spatial-test#dummy_obj_prop1"));
        OWLObjectProperty op2 = new OWLObjectPropertyImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#dummy_obj_prop2"));
        OWLObjectProperty op3 = new OWLObjectPropertyImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#dummy_obj_prop3"));
        OWLDataProperty dp1 = new OWLDataPropertyImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#dummy_data_prop1"));
        OWLDataProperty dp2 = new OWLDataPropertyImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#dummy_data_prop2"));

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
    public void testIsInside() throws ComponentInitException {
        SpatialReasoner reasoner = getReasoner();
        OWLIndividual pointInsideBuilding = new OWLNamedIndividualImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#pos_inside_bhf_neustadt"));
        OWLIndividual pointOutsideBuildung1 = new OWLNamedIndividualImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#pos_outside_bhf_neustadt_1"));
        OWLIndividual pointOutsideBuildung2 = new OWLNamedIndividualImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#pos_outside_bhf_neustadt_2"));
        OWLIndividual building = new OWLNamedIndividualImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#bahnhof_dresden_neustadt_building"));

        assertTrue(reasoner.isInside(pointInsideBuilding, building));
        assertFalse(reasoner.isInside(pointOutsideBuildung1, building));
        assertFalse(reasoner.isInside(pointOutsideBuildung2, building));
    }

    @Ignore
    @Test
    public void testGetContainedSpatialIndividuals() throws ComponentInitException {
        SpatialReasoner reasoner = getReasoner();

        OWLIndividual somePoint = new OWLNamedIndividualImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#pos_outside_bhf_neustadt_2"));

        // Getting all features contained inside a point feature without
        // considering the point feature itself --> should be empty
        Set<OWLIndividual> containedIndividuals =
                reasoner.getContainedSpatialIndividuals(somePoint);
        assertTrue(containedIndividuals.isEmpty());

        // Getting all features contained inside a point feature, including the
        // point feature itself into the result set --> should just contain the
        // point feature
        containedIndividuals =
                reasoner.getContainedSpatialIndividuals(somePoint, true);
        assertEquals(1, containedIndividuals.size());
        assertTrue(containedIndividuals.contains(somePoint));

        // -
        OWLIndividual turnerweg = new OWLNamedIndividualImpl(IRI.create(
                "http://dl-learner.org/ont/spatial-test#turnerweg"));
        OWLIndividual turnerwegPart = new OWLNamedIndividualImpl(IRI.create(
                "http://dl-learner.org/ont/spatial-test#turnerweg_part"));
        OWLIndividual pointOnTurnerWeg = new OWLNamedIndividualImpl(IRI.create(
                "http://dl-learner.org/ont/spatial-test#pos_on_turnerweg"));

        // Getting all features contained inside a line feature without
        // considering the line feature itself --> should be :turnerweg_part and
        // :pos_on_turnerweg
        containedIndividuals = reasoner.getContainedSpatialIndividuals(turnerweg);
        assertEquals(2, containedIndividuals.size());
        assertTrue(containedIndividuals.contains(turnerwegPart));
        assertTrue(containedIndividuals.contains(pointOnTurnerWeg));

        // Getting all features contained inside a line feature, including the
        // line feature itself into the result set --> should be
        // :turnerweg_part, :pos_on_turnerweg and :turnerweg itself
        containedIndividuals = reasoner.getContainedSpatialIndividuals(turnerweg, true);
        assertEquals(3, containedIndividuals.size());
        assertTrue(containedIndividuals.contains(turnerwegPart));
        assertTrue(containedIndividuals.contains(pointOnTurnerWeg));
        assertTrue(containedIndividuals.contains(turnerweg));

        // -
        OWLIndividual pointInsideBuilding = new OWLNamedIndividualImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#pos_inside_bhf_neustadt"));
        OWLIndividual areaInsideBuilding = new OWLNamedIndividualImpl(IRI.create(
                "http://dl-learner.org/ont/spatial-test#area_inside_bhf_neustadt"));
        OWLIndividual wayInsideBuilding = new OWLNamedIndividualImpl(IRI.create(
                "http://dl-learner.org/ont/spatial-test#way_inside_bhf_neustadt"));
        OWLIndividual building = new OWLNamedIndividualImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#bahnhof_dresden_neustadt_building"));

        // Getting all features contained in an area feature without considering
        // the area feature itself --> should be :area_inside_bhf_neustadt,
        // :way_inside_bhf_neustadt, and :pos_inside_bhf_neustadt
        containedIndividuals = reasoner.getContainedSpatialIndividuals(building);
        assertEquals(3, containedIndividuals.size());
        assertTrue(containedIndividuals.contains(pointInsideBuilding));
        assertTrue(containedIndividuals.contains(wayInsideBuilding));
        assertTrue(containedIndividuals.contains(areaInsideBuilding));

        // Getting all features contained in an area feature, including the area
        // feature itself --> should be :area_inside_bhf_neustadt,
        // :way_inside_bhf_neustadt, :pos_inside_bhf_neustadt, and
        // :bahnhof_dresden_neustadt_building itself
        containedIndividuals = reasoner.getContainedSpatialIndividuals(building, true);
        assertEquals(4, containedIndividuals.size());
        assertTrue(containedIndividuals.contains(pointInsideBuilding));
        assertTrue(containedIndividuals.contains(wayInsideBuilding));
        assertTrue(containedIndividuals.contains(areaInsideBuilding));
        assertTrue(containedIndividuals.contains(building));
    }
}
