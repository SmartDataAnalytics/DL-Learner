package org.dllearner.examples;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.semkernel.SemKernel;
import org.dllearner.algorithms.semkernel.SemKernel.SvmType;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import com.google.common.collect.Sets;

public class SemKernelExample {
    private static final Logger logger = Logger.getLogger(SemKernelExample.class);

    private static HashMap<String, Set<String>> mgi2go;
    private static HashMap<String, Set<String>> mgi2mp;
    private static Set<String> allClsUriStrs;
    private static OWLDataFactory factory;

    private static SemKernel kernel;
    /* kernel settings, according to
     * svm_train -s 0 -t 5 -b 1 -v 10 -c $cost -f go.obo \
     *      $TRAININGDIR/$NAME-$par > $CVDIR/$NAME-$par-$cost
     */
    private static final SvmType svmType = SvmType.C_SVC;
    private static final boolean doProbabilityEstimates = true;
    private static final int crossValidationFolds = 10;
    private static final float cost = 5f;
    private static final boolean predictProbability = true;

    /* semkernel settings */
    /** URIs of phenotype classes to build a classifier for */
    private static final Set<String> trainClsUriStrs = Sets.newHashSet(
//            "http://purl.obolibrary.org/obo/MP_0004031",
//            "http://purl.obolibrary.org/obo/MP_0000202",
            "http://purl.obolibrary.org/obo/MP_0001186"
//            "http://purl.obolibrary.org/obo/MP_0000001"
            );
    private static final Set<String> predictClsUriStrs = Sets.newHashSet(
//          "http://purl.obolibrary.org/obo/MP_0004031",
//          "http://purl.obolibrary.org/obo/MP_0000202",
          "http://purl.obolibrary.org/obo/MP_0001186"
//          "http://purl.obolibrary.org/obo/MP_0000001"
          );
    private static final double posNegExampleRatio = 1;

    /* input/output */
    private static final String workingDirPath = "/tmp/semkernel/";
    // get it at http://purl.obolibrary.org/obo/go.obo
//    private static final String kbFilePath = workingDirPath + "go.obo";
    private static final String kbFilePath = workingDirPath + "go.owl";
    private static final String trainDirPath = workingDirPath + "train/";
    private static final String modelDirPath = workingDirPath + "models/";
    private static final String predictionDirPath = workingDirPath + "predictions/";
    private static final String resultsDirPath = workingDirPath + "results/";
    // get it at http://purl.obolibrary.org/obo/mp.obo
    private static final String samplesKbFilePath = workingDirPath + "mp.obo";
    private static OWLOntology samplesKb;
    private static OWLReasoner samplesReasoner;
    // get it at ftp://ftp.informatics.jax.org/pub/reports/gene_association.mgi
    private static String mgi2goClsMappingsFilePath = workingDirPath + "gene_association.mgi";
    // get it at http://aber-owl.net/aber-owl/diseasephenotypes/data/mousephenotypes.txt
    private static String mgi2mpClsMappingsFilePath = workingDirPath + "mousephenotypes_new.txt";

    /* URI constants */
    private static final String oboPrefix = "http://purl.obolibrary.org/obo/";

    public static void main(String[] args) throws Exception {
        initExample();
        prepareTrainingData();

        initSemkernel();
        train();

        preparePredictionData();
        predict();
    }

    private static void initExample() throws OWLOntologyCreationException, IOException {
        logger.setLevel(Level.DEBUG);

        factory = OWLManager.getOWLDataFactory();
        allClsUriStrs = new TreeSet<>();

        logger.info(String.format("Loading samples ontology file %s ...", samplesKbFilePath));
        samplesKb = readKb(samplesKbFilePath);
        logger.info("-Done-");

        logger.info("Initialising reasoner...");
        ConsoleProgressMonitor mon = new ConsoleProgressMonitor();
        OWLReasonerConfiguration reasonerConf = new SimpleConfiguration(mon);
        OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
        samplesReasoner = reasonerFactory.createReasoner(samplesKb, reasonerConf);
        samplesReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        logger.info("-Done-");

        logger.info(String.format(
                "Reading MGI ID to GO class mappings from %s ...",
                mgi2goClsMappingsFilePath));
        mgi2go = new HashMap<>();
        BufferedReader buffReader = new BufferedReader(
                new FileReader(mgi2goClsMappingsFilePath));

        String line;
        while ((line = buffReader.readLine()) != null) {
            if (line.startsWith("!")) continue;

            String[] fields = line.split("\t");
            String mgiId = fields[1];
            // one or more of "NOT", "contributes_to", "co-localizes_with"
            String qualifier = fields[3];
            String goId = fields[4];
            String evidenceCode = fields[6];

            if (goId.trim().length() == 0) continue;  // skip lines not containing a GO ID

            if (evidenceCode != "ND" && !qualifier.contains("NOT")) {
                String goUriStr = oboPrefix + goId.replace(":", "_");

                if (!mgi2go.containsKey(mgiId)) {
                    mgi2go.put(mgiId, new TreeSet<>());
                }
                mgi2go.get(mgiId).add(goUriStr);
                allClsUriStrs.add(goUriStr);
            } else {
                logger.debug(String.format("-- Skipping line \"%s\"", line));
            }
        }
        buffReader.close();
        logger.info("-Done-");

        logger.info(String.format(
                "Reading MGI ID to MP class mappings from %s ...",
                mgi2mpClsMappingsFilePath));
        mgi2mp = new HashMap<>();
        buffReader = new BufferedReader(new FileReader(mgi2mpClsMappingsFilePath));

        while ((line = buffReader.readLine()) != null) {
            String[] fields = line.split("\t");
            if (fields.length < 2) continue;

            String mgiId = fields[0];

            if (mgi2go.containsKey(mgiId)) {
                String mpId = fields[1];
                if (mpId.trim().length() == 0) continue;  // skip lines not containing an MP ID

                String mpUriStr = oboPrefix + mpId.replace(":", "_");

                if (!mgi2mp.containsKey(mgiId)) {
                    mgi2mp.put(mgiId, new TreeSet<>());
                }
                mgi2mp.get(mgiId).add(mpUriStr);
            }
        }
        buffReader.close();
        logger.info("-Done-");
    }

    private static OWLOntology readKb(String filePath) throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        return man.loadOntologyFromOntologyDocument(new File(filePath));
    }

    private static void initSemkernel() {
        logger.info("Initialising the semkernel...");
        kernel = new SemKernel();

        // svm_train -s 0 ...
        kernel.setSvmType(svmType);
        // ... -t 5 ...  --> use semantic kernel (fixed for SemKernel anyway)
        // ... -b 1 ...
        kernel.setDoProbabilityEstimates(doProbabilityEstimates);
        // ... -v 10 ...
        kernel.setCrossValidationFolds(crossValidationFolds);
        // ... -c $cost ...
        kernel.setCost(cost);
        // ... -f go.obo ...
        kernel.setOntologyFilePath(kbFilePath);
        // ... $TRAININGDIR/$NAME-$par > $CVDIR/$NAME-$par-$cost
        kernel.setTrainingDirPath(trainDirPath);
        kernel.setModelDirPath(modelDirPath );
        kernel.setPredictionDataDirPath(predictionDirPath);
        kernel.setResultsDirPath(resultsDirPath);

        kernel.setGamma(0);
        kernel.setPredictProbability(predictProbability);

        kernel.init();
        logger.info("-Done-");
    }

    private static void prepareTrainingData() throws IOException {
        logger.info(String.format("Writing training sample data to file (%s)...",
                trainDirPath));
        for (String searchClassUriStr : trainClsUriStrs) {
            String localPart = getLocalPart(searchClassUriStr);
            String trainOutFilePath = trainDirPath + localPart;
            logger.debug("-- " + trainOutFilePath);

//            OWLClass searchCls = new OWLClassImpl(IRI.create(searchClassUriStr));
            OWLClass searchCls = factory.getOWLClass(IRI.create(searchClassUriStr));

            Set<String> classifierFor = new TreeSet<>();
            classifierFor.add(searchClassUriStr);

            Set<OWLClass> subClasses =
                    samplesReasoner.getSubClasses(searchCls, false).getFlattened();

            for (OWLClass owlClass : subClasses) {
                String uriStr = owlClass.getIRI().toString();
                classifierFor.add(uriStr);
            }

            List<String> negatives = new ArrayList<>();
            List<String> positives = new ArrayList<>();

            // build lines to write to file (SVM light format)
            for (String mgiId : mgi2mp.keySet()) {
                String outputLine = "";

                Set<String> mpUriStrs = mgi2mp.get(mgiId);

                if (Sets.intersection(classifierFor, mpUriStrs).isEmpty()) {
                    outputLine += "0";
                } else {
                    outputLine += "1";
                }

                if (!mgi2go.containsKey(mgiId)) continue;
                for (String goUristr : mgi2go.get(mgiId)) {
                    outputLine += "\t" + goUristr;
                }

                if (Sets.intersection(classifierFor, mpUriStrs).isEmpty()) {
                    negatives.add(outputLine);
                } else {
                    positives.add(outputLine);
                }
            }

            // shorten negative SVM light lines set to the configured
            // positives-negatives ratio
            Collections.shuffle(negatives);
            int cutoff = (int) Math.round(posNegExampleRatio * positives.size());
            if (cutoff < negatives.size()) {
                negatives = negatives.subList(0, cutoff);
            }

            BufferedWriter buffWriter = new BufferedWriter(new FileWriter(trainOutFilePath));

            for (String posLine : positives) {
                buffWriter.write(posLine);
                buffWriter.newLine();
            }
            for (String negLine : negatives) {
                buffWriter.write(negLine);
                buffWriter.newLine();
            }

            buffWriter.close();
        }
        logger.info("-Done-");
    }

    private static void preparePredictionData() throws IOException {
        logger.info("Preparing prediction data...");
        for (String predClsUriStr : predictClsUriStrs) {
            String localPart = getLocalPart(predClsUriStr);
            String predOutFilePath = predictionDirPath + localPart;
            logger.debug("-- " + predOutFilePath);

            BufferedWriter buffWriter = new BufferedWriter(
                    new FileWriter(predOutFilePath));
//            buffWriter.write("map");
//            for (String clsUri : allClsUriStrs) {
//                buffWriter.write("\t" + clsUri);
//            }
//            buffWriter.newLine();

            Set<String> classifierFor = new TreeSet<>();
            classifierFor.add(predClsUriStr);

            OWLClass predCls = factory.getOWLClass(IRI.create(predClsUriStr));
            Set<OWLClass> subClasses =
                    samplesReasoner.getSubClasses(predCls, false).getFlattened();

            for (OWLClass subClass : subClasses) {
                String uriStr = subClass.getIRI().toString();
                classifierFor.add(uriStr);
            }

            List<String> negatives = new ArrayList<>();
            List<String> positives = new ArrayList<>();

            // build lines to write to file (SVM light format)
            for (String mgiId : mgi2mp.keySet()) {
                String outputLine = "";

                Set<String> mpUriStrs = mgi2mp.get(mgiId);

                if (Sets.intersection(classifierFor, mpUriStrs).isEmpty()) {
                    outputLine += "0";
                } else {
                    outputLine += "1";
                }

                if (!mgi2go.containsKey(mgiId)) continue;
                for (String goUristr : mgi2go.get(mgiId)) {
                    outputLine += "\t" + goUristr;
                }

                if (Sets.intersection(classifierFor, mpUriStrs).isEmpty()) {
                    negatives.add(outputLine);
                } else {
                    positives.add(outputLine);
                }
            }

            // shorten negative SVM light lines set to the configured
            // positives-negatives ratio
            Collections.shuffle(negatives);
            int cutoff = (int) Math.round(posNegExampleRatio * positives.size());
            if (cutoff < negatives.size()) {
                negatives = negatives.subList(0, cutoff);
            }

            for (String posLine : positives) {
                buffWriter.write(posLine);
                buffWriter.newLine();
            }
            for (String negLine : negatives) {
                buffWriter.write(negLine);
                buffWriter.newLine();
            }

            buffWriter.close();
        }
        logger.info("-Done-");
    }

    private static String getLocalPart(String uriStr) {
        int lastSlashIdx = uriStr.lastIndexOf('/');
        if (lastSlashIdx > -1) {
            return uriStr.substring(lastSlashIdx);

        } else {  // try looking for a hash sign
            int lastHashIdx = uriStr.lastIndexOf('#');
            if (lastHashIdx > -1) {
                return uriStr.substring(lastHashIdx);
            }
        }

        return uriStr;
    }

    private static void train() {
        logger.info("Training...");
        kernel.train();
        logger.info("-Done-");
    }

    private static void predict() {
        logger.info("Running prediction...");
        kernel.predict();
        logger.info("-Done-");
    }
}
