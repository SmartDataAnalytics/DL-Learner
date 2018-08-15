/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.utilities.semkernel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.semkernel.SemKernel;
import org.dllearner.algorithms.semkernel.SemKernel.SvmType;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
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

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import com.google.common.collect.Sets;

/**
 * Since the current setup for running a SemKernel example comprises several
 * steps, like preparing the training data, do the training, preparing the
 * prediction data and so on, this component is intended to encapsulate this
 * whole process and make it callable and configurable via the standard
 * DL-Learner CLI.
 * As already said, there are different steps, depending on the tasks to solve
 *
 * - training:
 *   T1) read URIs to train
 *   T2) read the underlying MP knowledge base
 *   T3) write the prepared training data (in SVM light format) to the training
 *       data directory
 *   T4) read the GO knowledge base
 *   T5) do the training run on the GO knowledge base and write out the
 *       training model to the model directory
 *
 * - prediction
 *   P1) read URIs to predict
 *   P2) read the underlying knowledge base (if not done already)
 *   P3) write out the prepared prediction data to the prediction data directory
 *   P4) read the GO knowledge base
 *   P5) do the prediction based on the GO knowledge base and write out the
 *       prediction results to the result directory
 *
 * Additionally this workflow also uses special files that contain association
 * mappings between MGI marker accession IDs and gene functions (i.e. GO
 * classes) called MGI2GO and between MGI marker accession IDs and phenotypes
 * (i.e. MP classes) called MGI2MP.
 * So after reading the phenotype class URIs to train/predict the corresponding
 * gene functions are determined via these mapping files as follows:
 *
 * classifierFor set = { input MP class and all its subclasses defined in the
 *                       underlying knowledge base from T2/P2 }
 *
 * for MGI ID in MGI2MP {
 *      MP classes = MGI2MP.get(MGI ID)
 *
 *      if the classifiersFor set and the MP classes contain common classes {
 *          // this MGI ID is a positive sample
 *          GO classes = MGI2GO.get(MGI ID)
 *          add GO classes to positive samples
 *
 *      } else {
 *          // this MGI ID is a negative sample
 *          GO classes = MGI2GO.get(MGI ID)
 *          add GO classes to negative samples
 *      }
 * }
 *
 * So a rough illustration (neglecting the determination of positive/negative
 * samples) would look like this:
 *
 * MP class --(MGI2MP)--> MGI ID --(MGI2GO)--> GO classes
 *
 * So, to wrap up, the following files and directories need to be specified:
 *
 * T1) file containing MP URIs to train (one URI per line) (trainURIsFilePath)
 * T2) ontology file containing the MP ontology to derive all subclasses of the
 *     MP class to train from (mpKBFilePath)
 * T3) directory where the prepared training input data in SVM light format
 *     should be written to (one file per MP class to train)
 *     (trainingInputDirectoryPath)
 * T4) ontology file containing the GO ontology used to derive a semantic
 *     similarity between GO classes which is used by the SemKernel
 *     (goKBFilePath)
 * T5) directory where the model data should be written to (one file per MP
 *     class to train) (trainingOutputDirectoryPath)
 *
 * P1) file containing MP URIs to calculate a prediction for (one URI per line)
 *     (predictionURIsFilePath)
 * P2) see T2)
 * P3) directory where the prepared prediction input data in SVM light format
 *     should be written to (one file per MP class to calculate a prediction
 *     for) (predictionInputDirectoryPath)
 * P4) see T4)
 * P5) directory where the prediction output should be written to (one file
 *     per MP class the prediction was made for) (predictionOutputDirectoryPath)
 *
 * - file containing the MGI ID to MP class mappings (mgi2mpMappingsFilePath)
 * - file containing the MGI ID to GO class association mappings
 *   (mgi2goMappingsFilePath)
 *
 * @author Patrick Westphal
 */
@ComponentAnn(name="Mammalian Phenotype SemKernel Workflow", shortName="mpskw", version=0.1)
public class MPSemKernelWorkflow extends SemKernelWorkflow {

    // ------------------- files and directories to specify -------------------
    /** file containing MP URIs to train (one URI per line) */
    private String trainURIsFilePath;
    /**
     * ontology file containing the MP ontology to derive all subclasses of the
     * MP class to train from */
    private String mpKBFilePath;
    /**
     * directory where the prepared training input data in SVM light format
     * should be written to (one file per MP class to train) */
    private String trainingInputDirectoryPath;
    /**
     * ontology file containing the GO ontology used to derive a semantic
     * similarity between GO classes which is used by the SemKernel */
    private String goKBFilePath;
    /**
     * directory where the model data should be written to (one file per MP
     * class to train) */
    private String trainingOutputDirectoryPath;
    /**
     * file containing MP URIs to calculate a prediction for (one URI per line)
     */
    private String predictionURIsFilePath;
    /**
     * directory where the prepared prediction input data in SVM light format
     * should be written to (one file per MP class to calculate a prediction
     * for */
    private String predictionInputDirectoryPath;
    /**
     * directory where the prediction output should be written to (one file
     * per MP class the prediction was made for) */
    private String predictionOutputDirectoryPath;
    /** file containing the MGI ID to MP class mappings */
    private String mgi2mpMappingsFilePath;
    /** file containing the MGI ID to GO class association mappings */
    private String mgi2goMappingsFilePath;

    // -------------------------- SemKernel settings --------------------------
    private SemKernel kernel;
    private SvmType svmType = SvmType.C_SVC;
    private boolean doProbabilityEstimates = true;
    private int crossValidationFolds = 10;
    private float cost = 5f;
    private boolean predictProbability = true;
    private double posNegExampleRatio = 1;
    private boolean doTraining = true;
    private boolean doPrediction = true;

    // -------------------------------- misc ---------------------------------
    private final Logger logger = Logger.getLogger(MPSemKernelWorkflow.class);
    private OWLDataFactory dataFactory;
    private OWLOntology mpKB;
    private OWLReasoner mpKBReasoner;
    private Map<String, Set<String>> mgi2mp;
    private Map<String, Set<String>> mgi2go;
    private final String oboPrefix = "http://purl.obolibrary.org/obo/";

    @Override
    public void init() throws ComponentInitException {
        logger.info("Inializing workflow...");
        dataFactory = OWLManager.getOWLDataFactory();

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        try {
            // T2)/P2) ------------
            mpKB = man.loadOntologyFromOntologyDocument(new File(mpKBFilePath));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            System.exit(1);
        }
        ConsoleProgressMonitor mon = new ConsoleProgressMonitor();
        OWLReasonerConfiguration reasonerConf = new SimpleConfiguration(mon);
        OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
        mpKBReasoner = reasonerFactory.createReasoner(mpKB, reasonerConf);
        mpKBReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        try {
            mgi2go = readMGI2GOMapping(mgi2goMappingsFilePath);
            mgi2mp = readMGI2MPMapping(mgi2mpMappingsFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        kernel = new SemKernel();
        kernel.setSvmType(svmType);
        kernel.setDoProbabilityEstimates(doProbabilityEstimates);
        kernel.setCrossValidationFolds(crossValidationFolds);
        kernel.setCost(cost);
        kernel.setOntologyFilePath(goKBFilePath);
        kernel.setTrainingDirPath(trainingInputDirectoryPath);
        kernel.setModelDirPath(trainingOutputDirectoryPath);
        kernel.setPredictionDataDirPath(predictionInputDirectoryPath);
        kernel.setResultsDirPath(predictionOutputDirectoryPath);
        kernel.setGamma(0);
        kernel.setPredictProbability(predictProbability);
        kernel.init();

        initialized = true;
        logger.info("Finished workflow initialization.");
    }

    @Override
    public void start() {
        if (doTraining) {
            logger.info("Preparing training data...");
            try {
                prepareMPSampleTrainingData();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            logger.info("Finished training data preparation.");

            // T4) done during kernel.init() ------------
            // T5) ------------
            logger.info("Training...");
            kernel.train();
            logger.info("Finished trainig.");
        }

        if (doPrediction) {
            logger.info("Preparing prediction data...");
            try {
                prepareMPPredictionData();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            logger.info("Finished prediction data preparation.");

            // P4) done during kernel.init() ------------
            // P5) ------------
            logger.info("Doing predictions...");
            kernel.predict();
            logger.info("Finished prediction.");
        }
    }

    private Map<String, Set<String>> readMGI2MPMapping(String mgi2mpFilePath)
            throws IOException {

        Map<String, Set<String>> mgi2mp = new HashMap<>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(
                new File(mgi2mpFilePath)));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
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
        bufferedReader.close();

        return mgi2mp;
    }

    public void prepareMPSampleTrainingData() throws IOException {
        // append path separator if not set already
        if (!trainingInputDirectoryPath.endsWith(File.separator)) {
            trainingInputDirectoryPath = trainingInputDirectoryPath + File.separator;
        }

        // T1) ------------
        Set<String> trainUriStrs = readTrainURIs(trainURIsFilePath);

        // T3) (for each URI from T1)) ------------
        for (String searchClassUriStr : trainUriStrs) {
            String localPart = getLocalPart(searchClassUriStr);

            String trainOutFilePath = trainingInputDirectoryPath + localPart;

            OWLClass searchCls = new OWLClassImpl(IRI.create(searchClassUriStr));

            Set<String> classifierFor = new TreeSet<>();
            classifierFor.add(searchClassUriStr);

            Set<OWLClass> subClasses =
                    mpKBReasoner.getSubClasses(searchCls, false).getFlattened();

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
    }

    private void prepareMPPredictionData() throws IOException {
        if (!predictionInputDirectoryPath.endsWith(File.separator)) {
            predictionInputDirectoryPath += File.separator;
        }

        // P1) ------------
        Set<String> predictClsUriStrs = readTrainURIs(predictionURIsFilePath);

        // P3) (for each URI from P1)) ------------
        for (String predClsUriStr : predictClsUriStrs) {
            String localPart = getLocalPart(predClsUriStr);
            String predOutFilePath = predictionInputDirectoryPath + localPart;

            BufferedWriter buffWriter = new BufferedWriter(
                    new FileWriter(predOutFilePath));
//            buffWriter.write("map");
//            for (String clsUri : allClsUriStrs) {
//                buffWriter.write("\t" + clsUri);
//            }
//            buffWriter.newLine();

            Set<String> classifierFor = new TreeSet<>();
            classifierFor.add(predClsUriStr);

            OWLClass predCls = new OWLClassImpl(IRI.create(predClsUriStr));
            Set<OWLClass> subClasses =
                    mpKBReasoner.getSubClasses(predCls, false).getFlattened();

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
    }

    private static Set<String> readTrainURIs(String trainURIsFilePath) throws IOException {
        Set<String> uriStrs = new HashSet<>();
        BufferedReader bufferedReader = new BufferedReader(
                new FileReader(new File(trainURIsFilePath)));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            // strip off leading and trailing angled bracket
            if (line.startsWith("<") && line.endsWith(">")) {
                line = line.substring(1, line.length()-1);
            }

            uriStrs.add(line);
        }
        bufferedReader.close();

        return uriStrs;
    }

    private Map<String, Set<String>> readMGI2GOMapping(String mgi2goFilePath)
            throws IOException {
        Map<String, Set<String>> mgi2go = new HashMap<>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(
                new File(mgi2goFilePath)));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("!")) continue;

            String[] fields = line.split("\t");
            String mgiId = fields[1];
            // one or more of "NOT", "contributes_to", "co-localizes_with"
            String qualifier = fields[3];
            String goId = fields[4];
            String evidenceCode = fields[6];

            if (goId.trim().length() == 0) continue;  // skip lines not containing a GO ID

            if (!Objects.equals(evidenceCode, "ND") && !qualifier.contains("NOT")) {
                String goUriStr = oboPrefix + goId.replace(":", "_");

                if (!mgi2go.containsKey(mgiId)) {
                    mgi2go.put(mgiId, new TreeSet<>());
                }
                mgi2go.get(mgiId).add(goUriStr);
            }
        }
        bufferedReader.close();

        return mgi2go;
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

    // -------------------- only getters and setters below --------------------
    public String getTrainURIsFilePath() {
        return trainURIsFilePath;
    }

    public void setTrainURIsFilePath(String trainURIsFilePath) {
        this.trainURIsFilePath = trainURIsFilePath;
    }

    public String getMpKBFilePath() {
        return mpKBFilePath;
    }

    public void setMpKBFilePath(String mpKBFilePath) {
        this.mpKBFilePath = mpKBFilePath;
    }

    public String getTrainingInputDirectoryPath() {
        return trainingInputDirectoryPath;
    }

    public void setTrainingInputDirectoryPath(String trainingInputDirectoryPath) {
        this.trainingInputDirectoryPath = trainingInputDirectoryPath;
    }

    public String getGoKBFilePath() {
        return goKBFilePath;
    }

    public void setGoKBFilePath(String goKBFilePath) {
        this.goKBFilePath = goKBFilePath;
    }

    public String getTrainingOutputDirectoryPath() {
        return trainingOutputDirectoryPath;
    }

    public void setTrainingOutputDirectoryPath(
            String trainingOutputDirectoryPath) {
        this.trainingOutputDirectoryPath = trainingOutputDirectoryPath;
    }

    public String getPredictionURIsFilePath() {
        return predictionURIsFilePath;
    }

    public void setPredictionURIsFilePath(String predictionURIsFilePath) {
        this.predictionURIsFilePath = predictionURIsFilePath;
    }

    public String getPredictionInputDirectoryPath() {
        return predictionInputDirectoryPath;
    }

    public void setPredictionInputDirectoryPath(
            String predictionInputDirectoryPath) {
        this.predictionInputDirectoryPath = predictionInputDirectoryPath;
    }

    public String getPredictionOutputDirectoryPath() {
        return predictionOutputDirectoryPath;
    }

    public void setPredictionOutputDirectoryPath(
            String predictionOutputDirectoryPath) {
        this.predictionOutputDirectoryPath = predictionOutputDirectoryPath;
    }

    public String getMgi2mpMappingsFilePath() {
        return mgi2mpMappingsFilePath;
    }

    public void setMgi2mpMappingsFilePath(String mgi2mpMappingsFilePath) {
        this.mgi2mpMappingsFilePath = mgi2mpMappingsFilePath;
    }

    public String getMgi2goMappingsFilePath() {
        return mgi2goMappingsFilePath;
    }

    public void setMgi2goMappingsFilePath(String mgi2goMappingsFilePath) {
        this.mgi2goMappingsFilePath = mgi2goMappingsFilePath;
    }

    public SvmType getSvmType() {
        return svmType;
    }

    public void setSvmType(SvmType svmType) {
        this.svmType = svmType;
    }

    public boolean isDoProbabilityEstimates() {
        return doProbabilityEstimates;
    }

    public void setDoProbabilityEstimates(boolean doProbabilityEstimates) {
        this.doProbabilityEstimates = doProbabilityEstimates;
    }

    public int getCrossValidationFolds() {
        return crossValidationFolds;
    }

    public void setCrossValidationFolds(int crossValidationFolds) {
        this.crossValidationFolds = crossValidationFolds;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public boolean isPredictProbability() {
        return predictProbability;
    }

    public void setPredictProbability(boolean predictProbability) {
        this.predictProbability = predictProbability;
    }

    public double getPosNegExampleRatio() {
        return posNegExampleRatio;
    }

    public void setPosNegExampleRatio(double posNegExampleRatio) {
        this.posNegExampleRatio = posNegExampleRatio;
    }

    public boolean isDoTraining() {
        return doTraining;
    }

    public void setDoTraining(boolean doTraining) {
        this.doTraining = doTraining;
    }

    public boolean isDoPrediction() {
        return doPrediction;
    }

    public void setDoPrediction(boolean doPrediction) {
        this.doPrediction = doPrediction;
    }

}
