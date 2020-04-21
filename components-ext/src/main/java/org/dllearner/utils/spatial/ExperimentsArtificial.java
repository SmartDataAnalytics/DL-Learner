package org.dllearner.utils.spatial;

import com.google.common.collect.Lists;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.spatial.SpatialReasonerPostGIS;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.io.File;
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
            "artificial04", "artificial05", "artificial06",
            "artificial07", "artificial08", "artificial09",
            "artificial10");
    private static List<Integer> datasetSizes = Lists.newArrayList(
            10, 50, 100, 500, 1000, 5000, 10000, 50000, 100000,
            500000, 1000000);

    private static OWLDataProperty wktLiteralDTypeProperty =
            new OWLDataPropertyImpl(
                    IRI.create("http://www.opengis.net/ont/geosparql#asWKT"));
    private static List<OWLObjectProperty> propertyPathToGeom =
            Lists.newArrayList(new OWLObjectPropertyImpl(
                    IRI.create("http://www.opengis.net/ont/geosparql#hasGeometry")));

    public static void main(String[] args) throws ComponentInitException, IOException {
        FileWriter outFileWriter = new FileWriter(new File("results.csv"));

        for (String dataFolder : dataFolders) {
            for (int dasetSize : datasetSizes) {
                runExperiment(dataFolder, dasetSize, outFileWriter);
            }
        }
    }

    private static void runExperiment(
            String dataFolder, int datasetSize, FileWriter resultsFileWriter) throws ComponentInitException, IOException {

        List<OWLProperty> geometryPropertyPath = new ArrayList<>();
        geometryPropertyPath.addAll(propertyPathToGeom);
        geometryPropertyPath.add(wktLiteralDTypeProperty);

        String path = dataDir + dataFolder + "/kb_" + datasetSize + ".ttl";
        KnowledgeSource ks = new OWLFile(path);
        ks.init();

        ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
        cwr.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();

        reasoner.setDbName(dataFolder);
        reasoner.setDbUser("postgres");
        reasoner.setDbUserPW("postgres");
        reasoner.setHostname("localhost");
        reasoner.setPort(5432);
        reasoner.setBaseReasoner(cwr);
        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        Set<OWLIndividual> allIndividuals = reasoner.getIndividuals();

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
        resultsFileWriter.write(resultLine);
    }
}
