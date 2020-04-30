package org.dllearner.utils.spatial;

import com.google.common.collect.Lists;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.reasoning.spatial.SpatialReasonerPostGIS;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExperimentsArtificial {
    // expecting trailing slash!
    private static String dataDir = "/path/to/data/dir/";
    private static List<String> dataFolders = Lists.newArrayList(
            "artificial01", "artificial02", "artificial03",
            "artificial04", "artificial05");
    private static List<Integer> datasetSizes = Lists.newArrayList(
            500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000,
            5500, 6000, 6500, 7000, 7500, 8000, 8500, 9000, 9500, 10000,
            10500, 11000, 11500, 12000, 12500, 13000, 13500, 14000, 14500, 1500);

    private static OWLDataProperty wktLiteralDTypeProperty =
            new OWLDataPropertyImpl(
                    IRI.create("http://www.opengis.net/ont/geosparql#asWKT"));
    private static List<OWLObjectProperty> propertyPathToGeom =
            Lists.newArrayList(new OWLObjectPropertyImpl(
                    IRI.create("http://www.opengis.net/ont/geosparql#hasGeometry")));

    public static void main(String[] args) throws ComponentInitException, IOException {
        for (String dataFolder : dataFolders) {
            for (int dasetSize : datasetSizes) {
                runExperiment(dataFolder, dasetSize);
            }
        }
    }

    private static void runExperiment(String dataFolder, int datasetSize)
            throws ComponentInitException, IOException {

        List<OWLProperty> geometryPropertyPath = new ArrayList<>();
        geometryPropertyPath.addAll(propertyPathToGeom);
        geometryPropertyPath.add(wktLiteralDTypeProperty);

        String path = dataDir + dataFolder + "/kb_" + datasetSize + ".ttl";
        KnowledgeSource ks = new OWLFile(path);
        ks.init();

        OWLAPIReasoner cwrBaseReasoner = new OWLAPIReasoner(ks);
        cwrBaseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        cwrBaseReasoner.init();

        ClosedWorldReasoner cwr = new ClosedWorldReasoner(cwrBaseReasoner);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDBName(dataFolder + "_" + datasetSize);
        reasoner.setDBUser("postgres");
        reasoner.setDBUserPW("postgres");
        reasoner.setHostname("localhost");
        reasoner.setPort(5432);
        reasoner.setBaseReasoner(cwr);
        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        Set<OWLIndividual> allIndividuals =
                reasoner.getIndividuals(
                        new OWLClassImpl(
                                IRI.create("http://dl-learner.org/spatial#SpatialFeature")));

        List<Double> results = new ArrayList<>(10);
        for (int i=0; i<10; i++) {
            double startMillis = System.currentTimeMillis();
            for (OWLIndividual individual : allIndividuals) {
                Set<OWLIndividual> connectedIndividuals =
                        reasoner.getIndividualsConnectedWith(individual)
                                .collect(Collectors.toSet());
            }
            double endMillis = System.currentTimeMillis();

            double duration = endMillis - startMillis;
            results.add(i, duration);
        }
        String resultLine = dataFolder + "_" + datasetSize + "_connected," +
                results.stream()
                        .map(Object::toString)
                        .reduce("", (l, r) -> l + "," + r) +
                System.lineSeparator();

        FileWriter resultsFileWriter = new FileWriter("results.csv", true);

        resultsFileWriter.write(resultLine);
        resultsFileWriter.close();
    }
}
