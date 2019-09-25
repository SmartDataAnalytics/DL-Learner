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
package org.dllearner.algorithms.semkernel;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.dllearner.core.AbstractComponent;

import semlibsvm.svm_predict;
import semlibsvm.svm_train;
import semlibsvm.libsvm.svm;
import semlibsvm.libsvm.svm_model;
import semlibsvm.libsvm.svm_parameter;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassModel.AllVsAllMode;
import edu.berkeley.compbio.jlibsvm.multi.MultiClassModel.OneVsAllMode;

public class SemKernel extends AbstractComponent {
    public enum SvmType {
        C_SVC,
        NU_SVC,
        ONE_CLASS,
        EPSILON_SVR,
        NU_SVR
    }

    public enum ScalingMode { NONE, LINEAR, ZSCORE }

    private boolean useCrossValidation;
    private static final Float UNSPECIFIED_GAMMA = -1F;
    private boolean predictProbability;

    // SVM params
    private svm_parameter svmParams;
    private float nu = 0.5f;
    private int cacheSize = 100;
    private float epsilon = 1e-3f;
    private float p = 0.1f;
    private boolean doShrinking = true;
    private boolean doProbabilityEstimates = false;
    /**
     * For unbalanced data, redistribute the misclassification cost C according
     * to the numbers of examples in each class, so that each class has the
     * same total misclassification weight assigned to it and the average is
     * param.C
     * (from edu.berkeley.compbio.jlibsvm.ImmutableSvmParameter.java)
     * */
    private boolean redistributeUnbalanbcedCosts = true;
    private SvmType svmType = SvmType.C_SVC;
    /** degree in kernel function */
    private int degree = 3;
    /** gammas in kernel function */
    private double gamma = UNSPECIFIED_GAMMA;
    /** coef0 in kernel function */
    private int coef0 = 0;
    /** the parameter C of C-SVC, epsilon-SVR, and nu-SVR */
    private double cost;
    private int crossValidationFolds;
    // TODO: weights
    /** allVsAllMode: None, AllVsAll, FilteredVsAll, FilteredVsFiltered */
    private AllVsAllMode allVsAllMode;
    /** oneVsAllMode: None, Best, Veto, BreakTies, VetoAndBreakTies */
    private OneVsAllMode oneVsAllMode;
    private double oneVsAllThreshold = -1;
    /** the chosen class must have at least this proportion of the total votes */
    private double minVoteProportion = -1;
    /** scalingmode : none (default), linear, zscore */
    private ScalingMode scalingMode = ScalingMode.NONE;
    /** scalinglimit : maximum examples to use for scaling (default 1000) */
    private int scalingLimit = 1000;
    /** project to unit sphere (normalize L2 distance) */
    private boolean normalizeL2 = false;

    // input/output
    // TODO: use fallback for these if not set (create tmp dir)
    private String ontologyFilePath;
    private String trainingDirPath;
    private String modelDirPath;
    private String predictionDataDirPath;
    private String resultsDirPath;

    @Override
    public void init() {
        svmParams = new svm_parameter();
        svmParams.C = cost;
        svmParams.cache_size = cacheSize;
        // params.class2id  // set by svm_train.read_problem
        svmParams.coef0 = coef0;
        svmParams.degree = degree;
        svmParams.eps = epsilon;
        svmParams.gamma = gamma;
        svmParams.kernel_type = svm_parameter.SEMANTIC;
        svmParams.nr_weight = 0;  // TODO: make configurable
        svmParams.nu = nu;
        svmParams.ontology_file = ontologyFilePath;
        svmParams.p = p;
        svmParams.probability = doProbabilityEstimates ? 1 : 0;
        svmParams.shrinking = doShrinking ? 1 : 0;

        switch (svmType) {
            case C_SVC:
                svmParams.svm_type = svm_parameter.C_SVC;
                break;
            case NU_SVC:
                svmParams.svm_type = svm_parameter.NU_SVC;
                break;
            case ONE_CLASS:
                svmParams.svm_type = svm_parameter.ONE_CLASS;
                break;
            case EPSILON_SVR:
                svmParams.svm_type = svm_parameter.EPSILON_SVR;
                break;
            case NU_SVR:
                svmParams.svm_type = svm_parameter.NU_SVR;
                break;
        }

        svmParams.weight = new double[0];  // TODO: make configurable
        svmParams.weight_label = new int[0];  // TODO make configurable
        
        initialized = true;
    }

    public void train() {
        svm_train svmTrain = new svm_train();
        File trainDir = new File(trainingDirPath);

        for (String trainFileName : trainDir.list()) {
            String modelFilePath;
            if (!modelDirPath.endsWith(File.separator)) {
                modelFilePath = modelDirPath + File.separator + trainFileName;
            } else {
                modelFilePath = modelDirPath + trainFileName;
            }

            String trainFilePath;
            if (!trainingDirPath.endsWith(File.separator)) {
                trainFilePath = trainingDirPath + File.separator + trainFileName;
            } else {
                trainFilePath = trainingDirPath + trainFileName;
            }
            try {
                svmTrain.run(svmParams, trainFilePath, modelFilePath);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void predict() {
        File predDataDir = new File(predictionDataDirPath);

        for (String predFileName : predDataDir.list()) {
            String predFilePath;
            if (!predictionDataDirPath.endsWith(File.separator)) {
                predFilePath = predictionDataDirPath + File.separator +
                        predFileName;
            } else {
                predFilePath = predictionDataDirPath + predFileName;
            }

            String modelFilePath;
            if (!modelDirPath.endsWith(File.separator)) {
                modelFilePath = modelDirPath + File.separator + predFileName;
            } else {
                modelFilePath = modelDirPath + predFileName;
            }

            String resultFilePath;
            if (!resultsDirPath.endsWith(File.separator)) {
                resultFilePath = resultsDirPath + File.separator + predFileName;
            } else {
                resultFilePath = resultsDirPath + predFileName;
            }

            try {
                svm_model model = svm.svm_load_model(modelFilePath);

                if (model == null) {
                    final String msg = String.format(
                            "can't open model file %s", modelFilePath);
                    throw new Exception(msg);
                }

                model.param.ontology_file = ontologyFilePath;
                svm.initSimilarityEngine(ontologyFilePath);

                if(predictProbability) {
                    if(svm.svm_check_probability_model(model)==0) {
                        final String msg =
                                "Model does not support probabiliy estimates";
                        throw new Exception(msg);
                    }
                } else {
                    if(svm.svm_check_probability_model(model)!=0) {
                        svm_predict.info("Model supports probability " +
                                "estimates, but disabled in prediction.\n");
                    }
                }

                BufferedReader predFileReader = new BufferedReader(
                        new FileReader(predFilePath));
                DataOutputStream resStream = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(resultFilePath)));

                int predProbInt = predictProbability ? 1 : 0;

                svm_predict.predict(predFileReader, resStream, model, predProbInt);

                predFileReader.close();
                resStream.close();

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    // ------------------- only getters and setters below ---------------------
    public boolean isUseCrossValidation() {
        return useCrossValidation;
    }

    public void setUseCrossValidation(boolean useCrossValidation) {
        this.useCrossValidation = useCrossValidation;
    }

    public float getNu() {
        return nu;
    }

    public void setNu(float nu) {
        this.nu = nu;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public float getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(float epsilon) {
        this.epsilon = epsilon;
    }

    public float getP() {
        return p;
    }

    public void setP(float p) {
        this.p = p;
    }

    public boolean isDoShrinking() {
        return doShrinking;
    }

    public void setDoShrinking(boolean doShrinking) {
        this.doShrinking = doShrinking;
    }

    public boolean isDoProbabilityEstimates() {
        return doProbabilityEstimates;
    }

    public void setDoProbabilityEstimates(boolean doProbabilityEstimates) {
        this.doProbabilityEstimates = doProbabilityEstimates;
    }

    public boolean isRedistributeUnbalanbcedCosts() {
        return redistributeUnbalanbcedCosts;
    }

    public void setRedistributeUnbalanbcedCosts(boolean redistributeUnbalanbcedCosts) {
        this.redistributeUnbalanbcedCosts = redistributeUnbalanbcedCosts;
    }

    public SvmType getSvmType() {
        return svmType;
    }

    public void setSvmType(SvmType svmType) {
        this.svmType = svmType;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public int getCoef0() {
        return coef0;
    }

    public void setCoef0(int coef0) {
        this.coef0 = coef0;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gammaSet) {
        this.gamma = gammaSet;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double costs) {
        this.cost = costs;
    }

    public int getCrossValidationFolds() {
        return crossValidationFolds;
    }

    public void setCrossValidationFolds(int crossValidationFolds) {
        this.useCrossValidation = true;
        this.crossValidationFolds = crossValidationFolds;
    }

    public AllVsAllMode getAllVsAllMode() {
        return allVsAllMode;
    }

    public void setAllVsAllMode(AllVsAllMode allVsAllMode) {
        this.allVsAllMode = allVsAllMode;
    }

    public OneVsAllMode getOneVsAllMode() {
        return oneVsAllMode;
    }

    public void setOneVsAllMode(OneVsAllMode oneVsAllMode) {
        this.oneVsAllMode = oneVsAllMode;
    }

    public double getOneVsAllThreshold() {
        return oneVsAllThreshold;
    }

    public void setOneVsAllThreshold(double oneVsAllThreshold) {
        this.oneVsAllThreshold = oneVsAllThreshold;
    }

    public double getMinVoteProportion() {
        return minVoteProportion;
    }

    public void setMinVoteProportion(double minVoteProportion) {
        this.minVoteProportion = minVoteProportion;
    }

    public ScalingMode getScalingMode() {
        return scalingMode;
    }

    public void setScalingMode(ScalingMode scalingMode) {
        this.scalingMode = scalingMode;
    }

    public int getScalingLimit() {
        return scalingLimit;
    }

    public void setScalingLimit(int scalingLimit) {
        this.scalingLimit = scalingLimit;
    }

    public boolean isNormalizeL2() {
        return normalizeL2;
    }

    public void setNormalizeL2(boolean normalizeL2) {
        this.normalizeL2 = normalizeL2;
    }

    public String getTrainingOutputDirPath() {
        return trainingDirPath;
    }

    public void setTrainingDirPath(String trainingOutputDirPath) {
        this.trainingDirPath = trainingOutputDirPath;
    }

    public String getOntologyFilePath() {
        return ontologyFilePath;
    }

    public void setOntologyFilePath(String ontologyFilePath) {
        this.ontologyFilePath = ontologyFilePath;
    }

    public String getModelDirPath() {
        return modelDirPath;
    }

    public void setModelDirPath(String modelDirPath) {
        this.modelDirPath = modelDirPath;
    }

    public String getPredictionDataDirPath() {
        return predictionDataDirPath;
    }

    public void setPredictionDataDirPath(String predictionDataDirPath) {
        this.predictionDataDirPath = predictionDataDirPath;
    }

    public String getResultsDirPath() {
        return resultsDirPath;
    }

    public void setResultsDirPath(String resultsDirPath) {
        this.resultsDirPath = resultsDirPath;
    }

    public boolean isPredictProbability() {
        return predictProbability;
    }

    public void setPredictProbability(boolean predictProbability) {
        this.predictProbability = predictProbability;
    }
}
