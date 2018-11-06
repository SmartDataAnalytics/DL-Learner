package org.dllearner.reasoning.spatial;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dllearner.algorithms.spatial.SpatialLearningAlgorithm;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.refinementoperators.spatial.SpatialRhoDRDown;
import org.semanticweb.owlapi.model.IRI;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

public class SpatialLearningTestScenario {
    public static void main(String[] args) throws ComponentInitException {
        String exampleFilePath =
                SpatialLearningTestScenario.class.getClassLoader()
                        .getResource("example_data.owl").getFile();
        KnowledgeSource ks = new OWLFile(exampleFilePath);
        ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
        SpatialReasonerPostGIS spatialReasoner = new SpatialReasonerPostGIS(
                cwr, new DBConnectionSetting(
                "localhost",5432, "dllearner",
                "postgres", "postgres"));
        spatialReasoner.addGeometryPropertyPath(Lists.newArrayList(
                new OWLObjectPropertyImpl(IRI.create(
                        "http://www.opengis.net/ont/geosparql#hasGeometry")),
                new OWLDataPropertyImpl(IRI.create(
                        "http://www.opengis.net/ont/geosparql#asWKT"))
        ));
        spatialReasoner.init();

        SpatialRhoDRDown refinementOperator = new SpatialRhoDRDown();
        refinementOperator.setReasoner((SpatialReasoner) spatialReasoner);
        refinementOperator.init();

        PosNegLPStandard lp = new PosNegLPStandard();
        lp.setPositiveExamples(Sets.newHashSet(
                new OWLNamedIndividualImpl(IRI.create(
                        "http://dl-learner.org/ont/spatial#pos_inside_bhf_neustadt"))));

        lp.setNegativeExamples(Sets.newHashSet(
                new OWLNamedIndividualImpl(IRI.create(
                        "http://dl-learner.org/ont/spatial#pos_outside_bhf_neustadt_1")),
                new OWLNamedIndividualImpl(IRI.create(
                        "http://dl-learner.org/ont/spatial#pos_on_turnerweg"))));
        lp.setReasoner(spatialReasoner);
        lp.init();

        SpatialLearningAlgorithm alg = new SpatialLearningAlgorithm();
        alg.setLearningProblem(lp);
        alg.setOperator(refinementOperator);
        alg.setMaxExecutionTimeInSeconds(20);
//        alg.se
        alg.init();
        alg.start();
    }
}
