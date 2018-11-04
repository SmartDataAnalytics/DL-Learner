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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
                            .getResource("test/example_data.owl").getFile();
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
        OWLIndividual building = new OWLNamedIndividualImpl(
                IRI.create("http://dl-learner.org/ont/spatial-test#bahnhof_dresden_neustadt_building"));
        assertTrue(reasoner.isInside(pointInsideBuilding, building));
    }
}
