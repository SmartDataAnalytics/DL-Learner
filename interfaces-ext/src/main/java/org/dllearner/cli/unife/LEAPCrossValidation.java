/**
 * This file is part of LEAP.
 *
 * LEAP was implemented as a plugin of DL-Learner http://dl-learner.org, but
 * some components can be used as stand-alone.
 *
 * LEAP is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * LEAP is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.cli.unife;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import mpi.MPI;
import org.apache.log4j.Logger;
//import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.learningproblems.ClassLearningProblem;
//import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.apache.commons.io.FilenameUtils;
import org.dllearner.cli.CrossValidation;
import org.dllearner.core.probabilistic.unife.AbstractPSLA;
import org.dllearner.algorithms.probabilistic.parameter.unife.edge.AbstractEDGE;
import org.dllearner.algorithms.probabilistic.structure.unife.leap.AbstractLEAP;
import org.dllearner.utils.unife.OWLUtils;
import org.dllearner.utils.unife.ReflectionHelper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import unife.bundle.utilities.BundleUtilities;
import unife.edge.mpi.MPIUtilities;

/**
 * Performs a pseudo cross validation for the given problem using the LEAP
 * system. It is not a real k-fold cross validation, because this class executes
 * only a k-fold training. It produces k output file which must be submitted to
 * testing
 *
 * @author Jens Lehmann
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class LEAPCrossValidation extends CrossValidation {

    private static final Logger logger = Logger.getLogger(LEAPCrossValidation.class);

    public LEAPCrossValidation(AbstractPSLA psla, int folds, boolean leaveOneOut, boolean parallel) throws OWLOntologyStorageException, OWLOntologyCreationException {

        boolean master = true;
        
        if (parallel) {
            master = MPIUtilities.isMaster(MPI.COMM_WORLD);
        }
        
        AbstractLearningProblem lp = psla.getLearningProblem();

        DecimalFormat df = new DecimalFormat();

        // the training sets used later on
        List<Set<OWLIndividual>> trainingSetsPos = new LinkedList<>();
        List<Set<OWLIndividual>> trainingSetsNeg = new LinkedList<>();
        List<Set<OWLIndividual>> testSetsPos = new LinkedList<>();
        List<Set<OWLIndividual>> testSetsNeg = new LinkedList<>();

        // get individuals and shuffle them too
        Set<OWLIndividual> posExamples = new HashSet();
        Set<OWLIndividual> negExamples = new HashSet();
        logger.debug("Setting cross validation");
        if (lp instanceof PosNegLP) {
            posExamples = ((PosNegLP) lp).getPositiveExamples();
            negExamples = ((PosNegLP) lp).getNegativeExamples();
        } else if (lp instanceof PosOnlyLP) {
            posExamples = ((PosNegLP) lp).getPositiveExamples();
            //negExamples = Helper.difference(lp.getReasoner().getIndividuals(), posExamples);
            negExamples = new HashSet<>();
        } else if (lp instanceof ClassLearningProblem) {
            try {
                posExamples = new HashSet((List<OWLIndividual>) ReflectionHelper.getPrivateField(lp, "classInstances"));
                negExamples = new HashSet((List<OWLIndividual>) ReflectionHelper.getPrivateField(lp, "superClassInstances"));
                // if the number of negative examples is lower than the number of folds 
                // get as negative examples all the individuals that are not instances of ClassToDescribe
                if (negExamples.size() < folds) {
                    logger.info("The number of folds is higher than the number of "
                            + "negative examples. Selecting the instances of Thing which "
                            + "are non instances of ClasstoDescribe as negative Examples");
                    AbstractReasonerComponent reasoner = lp.getReasoner();
                    // get as negative examples all the individuals which belong to the class Thing
                    // but not to the ClassToDescribe
                    negExamples = reasoner.getIndividuals(OWLManager.getOWLDataFactory().getOWLThing());
                    negExamples.removeAll(posExamples);
                }
            } catch (Exception e) {
                logger.error("Cannot get positive and negative individuals for the cross validation");
                logger.error(e);
                System.exit(-2);
            }
        } else {
            throw new IllegalArgumentException("Only ClassLearningProblem, PosNeg and PosOnly learning problems are supported");
        }
        List<OWLIndividual> posExamplesList = new LinkedList<>(posExamples);
        List<OWLIndividual> negExamplesList = new LinkedList<>(negExamples);
        Collections.shuffle(posExamplesList, new Random(1));
        Collections.shuffle(negExamplesList, new Random(2));

        // sanity check whether nr. of folds makes sense for this benchmark
        if (!leaveOneOut && (posExamples.size() < folds || negExamples.size() < folds)) {
            logger.error("The number of folds is higher than the number of "
                    + "positive/negative examples. This can result in empty test sets. Exiting.");
            System.exit(0);
        }

        if (leaveOneOut) {
            // note that leave-one-out is not identical to k-fold with
            // k = nr. of examples in the current implementation, because
            // with n folds and n examples there is no guarantee that a fold
            // is never empty (this is an implementation issue)
            int nrOfExamples = posExamples.size() + negExamples.size();
            for (int i = 0; i < nrOfExamples; i++) {
                // ...
            }
            logger.error("Leave-one-out not supported yet.");
            System.exit(1);
        } else {
            // calculating where to split the sets, ; note that we split
            // positive and negative examples separately such that the 
            // distribution of positive and negative examples remains similar
            // (note that there are better but more complex ways to implement this,
            // which guarantee that the sum of the elements of a fold for pos
            // and neg differs by at most 1 - it can differ by 2 in our implementation,
            // e.g. with 3 folds, 4 pos. examples, 4 neg. examples)
            int[] splitsPos = calculateSplits(posExamples.size(), folds);
            int[] splitsNeg = calculateSplits(negExamples.size(), folds);

//				System.out.println(splitsPos[0]);
//				System.out.println(splitsNeg[0]);
            // calculating training and test sets
            for (int i = 0; i < folds; i++) {
                Set<OWLIndividual> testPos = getTestingSet(posExamplesList, splitsPos, i);
                Set<OWLIndividual> testNeg = getTestingSet(negExamplesList, splitsNeg, i);
                testSetsPos.add(i, testPos);
                testSetsNeg.add(i, testNeg);
                trainingSetsPos.add(i, getTrainingSet(posExamples, testPos));
                trainingSetsNeg.add(i, getTrainingSet(negExamples, testNeg));
            }

        }

        String completeLearnedOntology = psla.getOutputFile();
        String cloBase = FilenameUtils.removeExtension(completeLearnedOntology);
        String cloExt = FilenameUtils.getExtension(completeLearnedOntology);

        String positiveFile = "posExamples.owl";
        String pfBase = FilenameUtils.removeExtension(positiveFile);
        String pfExt = FilenameUtils.getExtension(positiveFile);
        String negativeFile = "negExamples.owl";
        String nfBase = FilenameUtils.removeExtension(negativeFile);
        String nfExt = FilenameUtils.getExtension(negativeFile);

        logger.debug("Performing Cross Validation");
        // run the algorithm
        for (int currFold = 0; currFold < folds; currFold++) {
            logger.debug("Current Fold: " + (currFold + 1));
            // setting positive and negative individuals
            final Set<OWLIndividual> trainPos = trainingSetsPos.get(currFold);
            final Set<OWLIndividual> trainNeg = trainingSetsNeg.get(currFold);
            final Set<OWLIndividual> testPos = testSetsPos.get(currFold);
            final Set<OWLIndividual> testNeg = testSetsNeg.get(currFold);
            if (lp instanceof PosNegLP) {
                ((PosNegLP) lp).setPositiveExamples(trainPos);
                ((PosNegLP) lp).setNegativeExamples(trainNeg);
                try {
                    lp.init();
                } catch (ComponentInitException e) {
                    logger.error(e);
                    logger.error(e.getLocalizedMessage());
                    System.exit(-2);
                }
            } else if (lp instanceof PosOnlyLP) {
                // il cross training viene fatto solo per gli esempi/individui positivi
                ((PosOnlyLP) lp).setPositiveExamples(new TreeSet<OWLIndividual>(trainPos));
                try {
                    lp.init();
                } catch (ComponentInitException e) {
                    logger.error(e);
                    logger.error(e.getLocalizedMessage());
                    System.exit(-2);
                }
                // set negative f
            } else if (lp instanceof ClassLearningProblem) {
                try {
                    // Initialize the ClassLearningProblem object first and then 
                    // modify his  private fields
                    //lp.init();
                    ReflectionHelper.setPrivateField(lp, "classInstances", trainPos);
                    ReflectionHelper.setPrivateField(lp, "superClassInstances", trainNeg);
                    ReflectionHelper.setPrivateField(lp, "negatedClassInstances", trainNeg);
                } catch (Exception e) {
                    logger.error("Cannot set positive and negative individuals for the cross validation");
                    logger.error(e);
                    System.exit(-2);
                }
            }

            AbstractEDGE edge = (AbstractEDGE) psla.getLearningParameterAlgorithm();
            OWLOntology startOntology = null;
            try {
                startOntology = BundleUtilities.copyOntology(edge.getSourcesOntology());

            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }

            psla.setOutputFile(cloBase + (currFold + 1) + "." + cloExt);
            try {
                //rs.init();
                edge.init();
                psla.init();
                //edge.setPositiveFile(pfBase + (currFold + 1) + "." + pfExt);
                //edge.setNegativeFile(nfBase + (currFold + 1) + "." + nfExt);
                //edge.init();
            } catch (ComponentInitException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            psla.start();

            if (master) {
                Set<OWLAxiom> posExamplesAxioms = edge.getPositiveExampleAxioms();
                Set<OWLAxiom> negExamplesAxioms = edge.getNegativeExampleAxioms();
                OWLDataFactory odf = OWLManager.getOWLDataFactory();
                // in the case replace superClass
                if (lp instanceof ClassLearningProblem) {
                    ClassLearningProblem clp = (ClassLearningProblem) lp;
                    Set<OWLAxiom> tempPos = new HashSet<>();
                    Set<OWLAxiom> tempNeg = new HashSet<>();

                    for (OWLAxiom ax : posExamplesAxioms) {
                        if (ax.isOfType(AxiomType.CLASS_ASSERTION)) {
                            OWLClassAssertionAxiom ax1 = (OWLClassAssertionAxiom) ax;
                            tempPos.add(odf.getOWLClassAssertionAxiom(clp.getClassToDescribe(), ax1.getIndividual()));
                        }
                    }
                    for (OWLAxiom ax : negExamplesAxioms) {
                        if (ax.isOfType(AxiomType.CLASS_ASSERTION)) {
                            OWLClassAssertionAxiom ax1 = (OWLClassAssertionAxiom) ax;
                            tempNeg.add(odf.getOWLClassAssertionAxiom(clp.getClassToDescribe(), ax1.getIndividual()));
                        }
                    }
                    posExamplesAxioms = tempPos;
                    negExamplesAxioms = tempNeg;
                }
                // convert test set into axioms
                Set<OWLAxiom> testAxiomsPos = new HashSet<>();
                Set<OWLAxiom> testAxiomsNeg = new HashSet<>();
                OWLClass clazz = ((AbstractLEAP) psla).getDummyClass();
                if (lp instanceof ClassLearningProblem) {
                    clazz = ((ClassLearningProblem) lp).getClassToDescribe();
                }
                for (OWLIndividual ind : testPos) {
                    testAxiomsPos.add(odf.getOWLClassAssertionAxiom(clazz, ind));
                }
                for (OWLIndividual ind : testNeg) {
                    testAxiomsNeg.add(odf.getOWLClassAssertionAxiom(clazz, ind));
                }

                OWLUtils.saveAxioms(testAxiomsPos, "posTestExamples" + (currFold + 1) + "." + pfExt, "OWLXML");
                OWLUtils.saveAxioms(testAxiomsNeg, "negTestExamples" + (currFold + 1) + "." + nfExt, "OWLXML");
                OWLUtils.saveAxioms(posExamplesAxioms, pfBase + (currFold + 1) + "." + pfExt, "OWLXML");
                OWLUtils.saveAxioms(negExamplesAxioms, nfBase + (currFold + 1) + "." + nfExt, "OWLXML");
            }
        }
    }

    protected int getCorrectPosClassified(AbstractReasonerComponent rs, OWLClass concept, Set<OWLIndividual> testSetPos) {
        return rs.hasType(concept, testSetPos).size();
    }

    protected int getCorrectNegClassified(AbstractReasonerComponent rs, OWLClass concept, Set<OWLIndividual> testSetNeg) {
        return testSetNeg.size() - rs.hasType(concept, testSetNeg).size();
    }

    public static Set<OWLIndividual> getTestingSet(List<OWLIndividual> examples, int[] splits, int fold) {
        int fromIndex;
        // we either start from 0 or after the last fold ended
        if (fold == 0) {
            fromIndex = 0;
        } else {
            fromIndex = splits[fold - 1];
        }
        // the split corresponds to the ends of the folds
        int toIndex = splits[fold];

//		System.out.println("from " + fromIndex + " to " + toIndex);
        Set<OWLIndividual> testingSet = new HashSet<>();
        // +1 because 2nd element is exclusive in subList method
        testingSet.addAll(examples.subList(fromIndex, toIndex));
        return testingSet;
    }

}
