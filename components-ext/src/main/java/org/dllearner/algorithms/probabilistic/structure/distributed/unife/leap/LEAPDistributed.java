/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.algorithms.probabilistic.structure.distributed.unife.leap;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.Sets;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import mpi.Intracomm;
import mpi.MPI;
import mpi.MPIException;
import org.apache.log4j.Logger;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.core.probabilistic.unife.AbstractPSLA;
import org.dllearner.core.probabilistic.unife.ParameterLearningException;
import org.dllearner.core.probabilistic.unife.StructureLearningException;
import org.dllearner.core.probabilistic.distributed.unife.AbstractEDGEDistributed;
import org.dllearner.core.probabilistic.distributed.unife.CommunicatorGroupNotAssignedException;
import org.dllearner.core.probabilistic.distributed.unife.DistributedComponent;
import org.dllearner.utils.unife.OWLUtils;
import org.dllearner.utils.unife.ReflectionHelper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.springframework.beans.factory.annotation.Autowired;
import unife.bundle.exception.InconsistencyException;
import unife.bundle.utilities.BundleUtilities;
import static unife.edge.mpi.EDGEMPIConstants.*;
import unife.edge.mpi.MPIUtilities;
import unife.edge.utilities.EDGEUtilities;
import unife.math.utilities.MathUtilities;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>, Riccardo Zese
 * <riccardo.zese@unife.it>
 */
@ComponentAnn(name = "LEAPDistributed", shortName = "leapdistr", version = 1.0)
public class LEAPDistributed extends AbstractPSLA implements DistributedComponent {

    @ConfigOption(description = "stop difference between log-likelihood of two consecutive iterations", defaultValue = "0.00001")
    private BigDecimal differenceLL = MathUtilities.getBigDecimal(0.00001, 5);

    private BigDecimal currentDifferenceLL;

    @ConfigOption(description = "stop ratio between log-likelihood of two consecutive iterations", defaultValue = "0.00001")
    private BigDecimal ratioLL = MathUtilities.getBigDecimal(0.00001, 5);

    private BigDecimal currentRatioLL;

    @ConfigOption(description = "maximum number of iterations", defaultValue = "2147000000")
    private long maxIterations = 2147000000L;

    private long currentIteration = 0;

    @ConfigOption(description = "accuracy used during the computation of the probabilistic values (number of digital places)", defaultValue = "5")
    private int accuracy = 5;

    private static final Logger logger = Logger.getLogger(LEAPDistributed.class.getName());

    @ConfigOption(description = "probabilistic target axioms which can be deleted from the ontology")
    private String targetAxiomsFilename;

    @ConfigOption(defaultValue = "owl:learnedClass", description = "You can specify a start class for the algorithm. To do this, you have to use Manchester OWL syntax without using prefixes.")
    private OWLClass dummyClass;

    @ConfigOption(description = "number of mpi processes of probabilistic structure learning algorithm", defaultValue = "1")
    private int procPSLA = 1;

    @ConfigOption(description = "number of mpi processes of parameter learning algorithm for each probabilistic structure learner process", defaultValue = "1")
    private int procPLA = 1;

    private int revisionBeamDim = 10;

    private AbstractEDGEDistributed edge;

    private TreeSet<Revision> beamRevisions = new TreeSet<>();

    private Revision bestRevision;
    private Revision previousBestRevision = new Revision();
    private List<Revision> revisions;
    private List<Set<Revision>> revisionsDistribution;

    private final Object countRevisionsLock = new Object();
    private int countRevisions = 0;

    private LinkedHashSet<OWLAxiom> targetAxioms = new LinkedHashSet<>();

    private int myRank;
    private int structureLearnerRank;
    private int parameterLearnerRank;
    private Intracomm structureLearnerComm;
    private Intracomm parameterLearnerComm;
    private OWLOntology originalOntology;
    private final int UPDATE = 10;
    private final int REMOVE = 11;

    @Override
    public void init() throws ComponentInitException {
        logger.debug("Start init() LEAPDistributed");
        currentIteration = 0;
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // create dummy class
        if (dummyClass == null) {
            dummyClass = manager.getOWLDataFactory().getOWLClass(IRI.create("owl:learnedClass"));
        }

        // read the ontology conatining the target axioms
        logger.debug("read the ontology containing the target axioms");
        if (targetAxiomsFilename != null) {
            try {
                OWLOntology targetAxiomsOntology = manager.loadOntologyFromOntologyDocument(new File(targetAxiomsFilename));
                for (OWLAxiom targetAxiom : EDGEUtilities.get_ax_filtered(targetAxiomsOntology)) {
                    targetAxioms.add(targetAxiom);
                }
            } catch (OWLOntologyCreationException ex) {
                logger.error("Cannot get the target probabilistic axioms.");
                throw new ComponentInitException(ex);
            }
        }
        // get rank 
        try {
            myRank = MPI.COMM_WORLD.getRank();
        } catch (MPIException mpiEX) {
            logger.error("Cannot get the rank of the process");
            throw new ComponentInitException(mpiEX);
        }
        // create groups and communicators
        int mpiProcesses;
        logger.debug(myRank + " - create groups and communicators");
        try {
            mpiProcesses = MPI.COMM_WORLD.getSize();
            if (mpiProcesses != procPSLA * procPLA) {
                String msg = myRank + " - The number of process must be (procPSLA * procPLA): "
                        + (procPSLA * procPLA) + " instead there are "
                        + mpiProcesses + " processes.";
                logger.error(msg);
                throw new ComponentInitException(msg);
            }
        } catch (MPIException mpiEx) {
            logger.error(myRank + " - Cannot get the number of processes");
            throw new ComponentInitException(mpiEx);
        }
        /* Determine row and column position */
        int row = myRank / procPLA;
        int col = myRank % procPLA;
        logger.debug(myRank + " - Parameter Learner Group "
                + " Group id: " + row
                + " Process Rank: " + col);
        try {
            // The processes in the same row belong to the same parameter learner
            // group communicator
            //Intracomm comm = MPI.COMM_WORLD.dup();
            parameterLearnerComm = MPI.COMM_WORLD.split(row, col);
//            parameterLearnerComm = MPI.COMM_WORLD.split(row, col);
            parameterLearnerRank = parameterLearnerComm.getRank();
            logger.debug(myRank + " - Parameter Learner Group created."
                    + " Group id: " + row
                    + " Process Rank: " + parameterLearnerRank);
            // The processes in the first column belong to the structure learner
            // group communicator  (There can only be one structure learner 
            // group communicator )
            logger.debug("test a");
            structureLearnerComm = MPI.COMM_WORLD.split(col, row);
            logger.debug("test b");
            structureLearnerRank = structureLearnerComm.getRank();
            logger.debug("test c");
            if (col != 0) {
                structureLearnerComm = null;
            } else {
                logger.debug(myRank + " - Structure Learner Group"
                        + " Group id: " + col
                        + " Process Rank: " + row);
                logger.debug(myRank + " - Structure Learner Group created. Rank: "
                        + " Group id: " + col
                        + " Process Rank: " + structureLearnerRank);
            }

        } catch (MPIException mpiEx) {
            logger.error(myRank + " - Cannot create the group communicators");
            throw new ComponentInitException(mpiEx);
        }

        // these  few lines are used to force the use of SHOIN DL,
        // i.e. without qualified cardinality restriction
//        if (cela instanceof CELOE) {
//            CELOE celoe = (CELOE) cela;
//            LengthLimitedRefinementOperator operator = celoe.getOperator();
//            if (operator instanceof RhoDRDown) {
//                LengthLimitedRefinementOperator newOperator = new RhoDRDown((RhoDRDown) operator);
//                // force no qualified cardinality restriction, we use SHOIN
//                ((RhoDRDown) newOperator).setUseCardinalityRestrictions(false);
//
//                newOperator.init();
//                celoe.setOperator(newOperator);
//            }
//        }
        logger.debug(myRank + " - getting the individuals");
        Set<OWLIndividual> positiveIndividuals;
        Set<OWLIndividual> negativeIndividuals;
        AbstractClassExpressionLearningProblem lp = cela.getLearningProblem();
        if (lp instanceof PosNegLP) {
            positiveIndividuals = ((PosNegLP) lp).getPositiveExamples();
            negativeIndividuals = ((PosNegLP) lp).getNegativeExamples();
        } else if (lp instanceof PosOnlyLP) {
            positiveIndividuals = ((PosOnlyLP) lp).getPositiveExamples();
            // use pseudo-negative individuals
            negativeIndividuals = Sets.difference(lp.getReasoner().getIndividuals(), positiveIndividuals);
        } else if (lp instanceof ClassLearningProblem) {
            // Java Reflection has been used to get values from private fields. 
            //It's neither a conventional way nor the universally suggested idea,
            // but in this case is the only way to extract positive and negative individuals
            // without modifing the DLLearner code (the creation of a plugin is the objective)
            try {
                List<OWLIndividual> positiveIndividualsList = ReflectionHelper.getPrivateField(lp, "classInstances");
                positiveIndividuals = new TreeSet<>(positiveIndividualsList);
                negativeIndividuals = new TreeSet<>((List<OWLIndividual>) ReflectionHelper.getPrivateField(lp, "superClassInstances"));
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                String msg = myRank + " - Cannot extract the individuals from"
                        + " learning problem: " + e.getMessage();
                logger.error(msg);
                throw new ComponentInitException(msg);
            }

        } else {
            try {
                throw new LearningProblemUnsupportedException(lp.getClass(), this.getClass());
            } catch (LearningProblemUnsupportedException e) {
                throw new ComponentInitException(e.getMessage());
            }
        }
        // convert the individuals into assertional axioms
        logger.debug(myRank + " - convert the individuals into assertional axioms");
        OWLDataFactory owlFactory = manager.getOWLDataFactory();
        Set<OWLAxiom> positiveExamples = new HashSet<>();
        for (OWLIndividual ind : positiveIndividuals) {
            OWLAxiom axiom = owlFactory.getOWLClassAssertionAxiom(dummyClass, ind);
            positiveExamples.add(axiom);
        }

        Set<OWLAxiom> negativeExamples = new HashSet<>();
        for (OWLIndividual ind : negativeIndividuals) {
            OWLAxiom axiom = owlFactory.getOWLClassAssertionAxiom(dummyClass, ind);
            negativeExamples.add(axiom);
        }

        //AbstractEDGEDistributed edge = (AbstractEDGEDistributed) pla;
        edge.setPositiveExampleAxioms(positiveExamples);
        edge.setNegativeExampleAxioms(negativeExamples);

    }

    @Override
    public void start() {
        stop = false;
        isRunning = true;
        currentIteration = 0;
        long totalTimeMills = System.currentTimeMillis();
        long celaTimeMills = 0;
        try {
            originalOntology = BundleUtilities.copyOntology(edge.getSourcesOntology());
        } catch (OWLOntologyCreationException e) {
            logger.error(myRank + "Error: " + e.getMessage());
            throw new StructureLearningException(e);
        }
        //AbstractEDGEDistributed edge = (AbstractEDGEDistributed) pla;
        // First step: run Distributed EDGE
//        try {
//            edge.changeSourcesOntology(BundleUtilities.copyOntology(originalOntology));
//        } catch (OWLOntologyCreationException e) {
//            logger.error("morte", e);
//            System.exit(-1);
//        }
        edge.start();

        logger.debug(myRank + " - First EDGE cycle terminated.");
        logger.debug(myRank + " - Initial Log-likelihood: " + edge.getLL());
        //OWLOntology originalOntology = edge.getLearnedOntology();

        if (structureLearnerComm != null) {
            logger.debug(myRank + " - Structure Learner");
            if (MPIUtilities.isMaster(structureLearnerComm)) {
                logger.debug(myRank + " - Structure Learner Master");
                List<Boolean> boolVars = new ArrayList<>();
                for (int i = 0; i < targetAxioms.size(); i++) {
                    boolVars.add(true);
                }
                Revision startRevision = new Revision(targetAxioms, boolVars, new LinkedHashSet<OWLSubClassOfAxiom>(), edge.getLL());
                beamRevisions.add(startRevision);
                bestRevision = startRevision;
                currentDifferenceLL = bestRevision.getLL().subtract(previousBestRevision.getLL());
                currentRatioLL = currentDifferenceLL.divide(previousBestRevision.getLL(), accuracy, BigDecimal.ROUND_HALF_UP);
                do {
                    //BigDecimal CLL0 = MathUtilities.getBigDecimal(-2.2 * Math.pow(10, 10), edge.getAccuracy());
                    //BigDecimal CLL1 = edge.getLL();

                    // generate all the refinements
                    //refinements = generateRefinements();
                    // divido i raffinamenti tra i vari slaves
                    // TO DO
                    // receive refinement
                    // TO DO
                    // ***start temporary code lines***
                    Revision revision = startRevision;
                    // *** end ***

                    OWLOntology ontology = generateOntologyFromRevision(originalOntology, revision);
                    // send the refinement to the EDGE slaves
                    try {
                        MPIUtilities.sendBCastSignal(START, parameterLearnerComm);
                        logger.debug(myRank + " - Sent START signal to EDGE slaves");
                        int sentBytes = MPIUtilities.sendBCastObject(revision, parameterLearnerComm);
                        logger.debug(myRank + " - Sent revision to "
                                + MPIUtilities.getSlaves(parameterLearnerComm) + " slaves "
                                + "(" + sentBytes + " bytes)");
                    } catch (MPIException mpiEx) {
                        logger.error(myRank + " - Cannot send to EDGE slaves the refinement: " + mpiEx.getMessage());
                        throw new StructureLearningException(mpiEx);
                    }

//                    Set<KnowledgeSource> newSources = Collections.singleton((KnowledgeSource) new OWLAPIOntology(ontology));
//                    AbstractReasonerComponent reasoner = cela.getReasoner();
//                    reasoner.changeSources(newSources);
//                    try {
//                        reasoner.init();
//                        cela.init();
//                    } catch (ComponentInitException cie) {
//                        logger.error(myRank + " - Error: " + cie.getMessage());
//                        throw new StructureLearningException(cie);
//                    }
                    // start class expression learning algorithm
                    celaTimeMills = System.currentTimeMillis();
                    cela.start();
                    celaTimeMills = System.currentTimeMillis() - celaTimeMills;
                    // get the best class expressions
                    NavigableSet<? extends EvaluatedDescription> evaluatedDescriptions = cela.getCurrentlyBestEvaluatedDescriptions();
                    // convert the class expressions into axioms
                    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                    LinkedHashSet<OWLSubClassOfAxiom> candidateAxioms = convertIntoAxioms(manager, evaluatedDescriptions);

                    logger.debug(myRank + " - current best revision LL: " + bestRevision.getLL());
                    // perform a greedy search 
                    logger.debug(myRank + " - Start greedy search");
                    // temporaneo i raffinamenti dopo dovranno essere assegnati ad ogni processo
                    Revision revisionResult = greedySearch(ontology, startRevision, candidateAxioms);
                    logger.debug(myRank + " - Greedy search finished");
                    // aggiungerlo eventualmente alla lista beam
                    // TO DO
                    // aggiornare il best
                    // TO DO
                    // ***righe temporanee***
                    logger.debug(myRank + " - current LL: " + revisionResult.getLL());
                    logger.debug(myRank + " - current best revision LL: " + bestRevision.getLL());
                    if (revisionResult.getLL().compareTo(bestRevision.getLL()) > 0) {
                        logger.info(myRank + " - Found a better revision with LL: " + revisionResult.getLL());
                        previousBestRevision = bestRevision;
                        bestRevision = revisionResult;
                    }
                    // ***fine righe temporanee***
                    // update criteria
                    updateTerminationCriteria();
                } while (!terminationCriteriaSatisfied());
                try {
                    MPIUtilities.sendBCastSignal(TERMINATE, parameterLearnerComm);
                } catch (MPIException e) {
                    logger.error("Cannot send TERMINATE signal to EDGE slaves");
                    throw new StructureLearningException(e);
                }
                OWLOntology finalOntology = edge.getSourcesOntology();
                // In case replace super class
                if (cela.getLearningProblem() instanceof ClassLearningProblem) {
                    finalOntology = replaceSuperClass(finalOntology, bestRevision.getLearnedAxioms());
                } else {
                    for (OWLAxiom axiom : bestRevision.getLearnedAxioms()) {
                        logger.info(myRank + " - Learned Axiom: " + axiom);
                    }
                }
                // final step save the ontology
                try {
                    OWLUtils.saveOntology(finalOntology, outputFile, outFormat);
                } catch (OWLOntologyStorageException e) {
                    String msg = myRank + " - Cannot save the learned ontology: " + e.getMessage();
                    throw new StructureLearningException(msg);
                }
                totalTimeMills = System.currentTimeMillis() - totalTimeMills;
                printTimings(totalTimeMills, celaTimeMills, edge.getTimeMap());

            } else { // structure learner slaves
                // Leggo i raffinamenti
                // TO DO 
                // trovo le class expression
                // invio in broadcast al parameter learner communicator group
                // apprendo i parametri
                throw new UnsupportedOperationException("Not supported yet!");
            }
        } else if (parameterLearnerComm != null) { // EDGE slaves 
            // devo solo eseguire degli EDGE distribuiti
            // prendo la lista di boolean e costruisco una nuova ontologia
            logger.debug(myRank + " - Parameter Learner Slave");
            boolean terminate = false;
            while (!terminate) {
                int signal;
                try {
                    signal = MPIUtilities.recvBCastSignal(MASTER, parameterLearnerComm);
                } catch (MPIException mpiEx) {
                    String msg = myRank + " - Cannot receive synchronization signal " + mpiEx.getMessage();
                    logger.error(msg);
                    throw new StructureLearningException(mpiEx);
                }
                if (signal == START) {
                    // read the refinement and modify the ontology
                    logger.debug(myRank + " - Received START signal.");
                    logger.debug(myRank + " - Waiting to receive ontology revision.");
                    OWLOntology ontology;
                    try {
                        Revision revision = (Revision) MPIUtilities.recvBCastObject(MASTER, parameterLearnerComm);
                        ontology = generateOntologyFromRevision(originalOntology, revision);
                    } catch (MPIException mpiEx) {
                        String msg = myRank + " - Cannot receive Refinement from EDGE Master: " + mpiEx.getMessage();
                        logger.error(msg);
                        throw new StructureLearningException(mpiEx);
                    }
                    boolean stopEDGE = false;
                    // receive signal
                    try {
                        signal = MPIUtilities.recvBCastSignal(MASTER, parameterLearnerComm);
                    } catch (MPIException e) {
                        String msg = myRank + " - Cannot receive signal: " + e.getMessage();
                        logger.error(msg);
                        throw new StructureLearningException(msg);
                    }
                    while (signal == START) {
                        edge.reset();
                        // add axiom
                        logger.debug(myRank + " - Waiting for axiom to add");
                        OWLAxiom addedAxiom = recvAddAxiom(ontology);
                        edge.changeSourcesOntology(ontology);
                        if (edge.getSourcesOntology().containsAxiom(addedAxiom)) {
                            logger.debug(myRank + " - Axiom added into the ontology");
                        } else {
                            String msg = myRank + " - Impossible to add the received axiom";
                            logger.debug(msg);
                            throw new StructureLearningException(msg);
                        }
                        // compute edge
                        edge.start();
                        // waiting for decision: keep the axiom or remove it?

                        try {
                            signal = MPIUtilities.recvBCastSignal(MASTER, parameterLearnerComm);
                        } catch (MPIException e) {
                            String msg = myRank + " - Cannot receive signal: " + e.getMessage();
                            logger.error(msg);
                            throw new StructureLearningException(msg);
                        }
                        if (signal == UPDATE) { // keep the added axiom
                            logger.debug(myRank + " - Received UPDATE ontology signal");
                            updateOntology();
                        } else if (signal == REMOVE) { // remove the added axiom
                            logger.debug(myRank + " - Received REMOVE axiom from ontology signal");
                            removeAxiom(ontology, addedAxiom);
                        } else { // wrong signal
                            String msg = myRank + " - Wrong signal received: " + signal;
                            logger.error(msg);
                            throw new StructureLearningException(msg);
                        }
                        // receive signal
                        try {
                            signal = MPIUtilities.recvBCastSignal(MASTER, parameterLearnerComm);
                            logger.debug(myRank + " - Received " + signal + " from MASTER");
                        } catch (MPIException e) {
                            String msg = myRank + " - Cannot receive signal: " + e.getMessage();
                            logger.error(msg);
                            throw new StructureLearningException(msg);
                        }

                    }
                    logger.debug(myRank + " - Received STOP signal.");
                } else if (signal == TERMINATE) {
                    logger.debug(myRank + " - Received TERMINATE signal.");
                    terminate = true;
                }
            }

        } else {
            // ERROR
            String msg = myRank + " - MPI process not assigned to any communicator group";
            logger.error(msg);
            throw new CommunicatorGroupNotAssignedException(msg);
        }
        isRunning = false;
    }

    /**
     * @return the targetAxiomsFilename
     */
    public String getTargetAxiomsFilename() {
        return targetAxiomsFilename;
    }

    /**
     * @param targetAxiomsFilename the targetAxiomsFilename to set
     */
    public void setTargetAxiomsFilename(String targetAxiomsFilename) {
        this.targetAxiomsFilename = targetAxiomsFilename;
    }

    /**
     * @return the PSLAproc
     */
    public int getProcPSLA() {
        return procPSLA;
    }

    /**
     * @param procPSLA the PSLAproc to set
     */
    public void setProcPSLA(int procPSLA) {
        this.procPSLA = procPSLA;
    }

    /**
     * @return the PLAproc
     */
    public int getProcPLA() {
        return procPLA;
    }

    /**
     * @param procPLA the procPLA to set
     */
    public void setProcPLA(int procPLA) {
        this.procPLA = procPLA;
    }

    /**
     * @return the differenceLL
     */
    public BigDecimal getDifferenceLL() {
        return differenceLL;
    }

    public void setDifferenceLL(double differenceLL) {
        this.differenceLL = MathUtilities.getBigDecimal(differenceLL, accuracy);
    }

    /**
     * @return the ratioLL
     */
    public BigDecimal getRatioLL() {
        return ratioLL;
    }

    public void setRatioLL(double ratioLL) {
        this.ratioLL = MathUtilities.getBigDecimal(ratioLL, accuracy);
    }

    /**
     * @return the maxIterations
     */
    public long getMaxIterations() {
        return maxIterations;
    }

    /**
     * @param maxIterations the maxIterations to set
     */
    public void setMaxIterations(long maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * @return the accuracy
     */
    public int getAccuracy() {
        return accuracy;
    }

    /**
     * @param accuracy the accuracy to set
     */
    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    private List<Revision> generateRevisions() {
        // see http://stackoverflow.com/questions/10923601/java-generator-of-trues-falses-combinations-by-giving-the-number-n
        // for generating refinements
        // takes all the target axioms and the best refinement from the beam
        // and generate all the refinements
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the dummyClass
     */
    public OWLClass getDummyClass() {
        return dummyClass;
    }

    /**
     * @param dummyClass the dummyClass to set
     */
    public void setDummyClass(OWLClass dummyClass) {
        this.dummyClass = dummyClass;
    }

    private LinkedHashSet<OWLSubClassOfAxiom> convertIntoAxioms(OWLOntologyManager manager, NavigableSet<? extends EvaluatedDescription> evaluatedDescriptions) {
        LinkedHashSet<OWLSubClassOfAxiom> axioms = new LinkedHashSet<>(evaluatedDescriptions.size());
        OWLDataFactory factory = manager.getOWLDataFactory();
        for (EvaluatedDescription description : evaluatedDescriptions.descendingSet()) {
            OWLAnnotation annotation = factory.
                    getOWLAnnotation(BundleUtilities.PROBABILISTIC_ANNOTATION_PROPERTY, factory.getOWLLiteral(description.getAccuracy()));
            OWLSubClassOfAxiom axiom = factory.
                    getOWLSubClassOfAxiom((OWLClassExpression) description.getDescription(), dummyClass, Collections.singleton(annotation));
            axioms.add(axiom);
        }
        return axioms;
    }

    /**
     * This class implements a thread that will be in listening and when a slave
     * is ready will send it a refinement.
     */
//    class RefinementsListener implements Runnable {
//
//        private BigDecimal[] probs;
//        private int sentRefinements;
//        int recvRefinementsEval = 0;
//
//        public RefinementsListener(int sentRefinements) {
//            super();
//            this.sentRefinements = sentRefinements;
//        }
//
//        @Override
//        public void run() {
//            try {
//                boolean stopRequest = false;
//                Refinement refinement = null;
//                int slaveId = 0;
//                while (sentRefinements != recvRefinementsEval) {
//
//                    synchronized (countRefinementsLock) {
//                        logger.debug(myRank + " - T_2");
//                        if (refinement != null) {
//                            refinementsDistribution.get(slaveId).add(refinement);
//                        }
//                        refinement = nextRefinement();
//                    }
//                    Status recvStat = structureLearnerComm.probe(MPI.ANY_SOURCE, MPI.ANY_TAG);
//                    byte[] buffer = new byte[recvStat.getCount(MPI.BYTE)];
//                    logger.debug(myRank + " - Waiting for a refinement result...");
//                    // receive
//                    structureLearnerComm.recv(buffer, buffer.length, MPI.BYTE, MPI.ANY_SOURCE, MPI.ANY_TAG);
//                    recvRefinementsEval++;
//                    // get example probability
//                    byte[] exProbBuff = new byte[buffer.length - (Integer.SIZE / 8)];
//                    int pos = MPI.COMM_WORLD.unpack(buffer, 0, exProbBuff, exProbBuff.length, MPI.BYTE);
//                    int indexEx = recvStat.getTag() - 1;
//                    slaveId = recvStat.getSource();
//                    probs[indexEx] = (BigDecimal) MPIUtilities.byteArrayToObject(exProbBuff);
//                    String msg = myRank + " - RECV from " + recvStat.getSource()
//                            + " - " + BundleUtilities.getManchesterSyntaxString(initialExamples.get(indexEx).getKey())
//                            + " - prob: " + probs[indexEx]
//                            + " - tag: " + recvStat.getTag();
//                    if (!test) {
//                        int[] numBoolVars = new int[1];
//                        MPI.COMM_WORLD.unpack(buffer, pos, numBoolVars, 1, MPI.INT);
//                        msg += " - #vars: " + numBoolVars[0];
//                        // indices of the axioms used in the BDD
//                        int[] axiomsIdxs = new int[numBoolVars[0]];
//                        if (numBoolVars[0] > 0) {
//                            // receive the buffer containing the indices of the used axioms
//                            MPI.COMM_WORLD.recv(axiomsIdxs, axiomsIdxs.length, MPI.INT, slaveId, recvStat.getTag());
//                        }
//
//                        for (int j = 0; j < numBoolVars[0]; j++) {
//                            usedAxioms[axiomsIdxs[j]] = true;
//                        }
//                    }
//                    logger.debug(msg);
//                    // I've received the query/example results, 
//                    // now it's time to send another example
//                    if (example == null) {
//                        if (!stopRequest) {
//                            // Send stop request to all the slaves
//                            new Thread() {
//                                @Override
//                                public void run() {
//                                    synchronized (countActuallySentExamplesLock) {
//                                        while (sentRefinements != countActuallySentExamples) {
//                                            try {
//                                                countActuallySentExamplesLock.wait();
//                                            } catch (InterruptedException ex) {
//                                                logger.fatal(myRank + " - Error: " + ex.getMessage());
//                                                throw new RuntimeException(ex);
//                                            }
//                                        }
//                                    }
//                                    logger.debug(myRank + " - Sending stop signal to all slaves");
//                                    try {
//                                        MPIUtilities.sendSignal(ALL, STOP, MPI.COMM_WORLD);
//                                    } catch (MPIException mpiEx) {
//                                        logger.error(myRank + " - Error: " + mpiEx.getMessage());
//                                        throw new RuntimeException(mpiEx);
//                                    }
//                                }
//                            }.start();
//                            stopRequest = true;
//                        }
//                    } else {
//                        // send Example to the slave
//                        if (slaveId == 0) {
//                            throw new RuntimeException("Inconsistent slave receiver!");
//                        }
//                        sentRefinements++;
//                        (new Thread(new EDGEMPIDynamic.ExampleSender(slaveId, example, sentRefinements))).start();
//
//                    }
//                }
//                if (!stopRequest) {
//                    logger.debug(myRank + " - Sending stop signal to all slaves");
//                    MPIUtilities.sendSignal(ALL, STOP, MPI.COMM_WORLD);
//                    stopRequest = true; // unused
//                }
//            } catch (MPIException mpiEx) {
//                logger.error(myRank + " - Error: " + mpiEx.getMessage());
//                throw new RuntimeException(mpiEx);
//            } catch (Exception ex) {
//                logger.error(myRank + " - Error: " + ex.getMessage());
//                throw new RuntimeException(ex);
//            }
//        }
//
//        /**
//         * @param sentRefinements the sentExamples to set
//         */
//        public void setSentRefinements(int sentRefinements) {
//            this.sentRefinements = sentRefinements;
//        }
//
//    }
    private Revision nextRefinement() {
        if (countRevisions < revisions.size()) {
            countRevisions++;
            return revisions.get(countRevisions - 1);
        } else {
            return null;
        }
    }

    private Revision greedySearch(OWLOntology ontology, Revision revision, LinkedHashSet<OWLSubClassOfAxiom> candidateAxioms) {
        BigDecimal LL0 = edge.getLL(); // da rivedere
        logger.debug(myRank + " - Resetting EDGE");
        edge.reset();
        edge.changeSourcesOntology(ontology);
        LinkedHashSet<OWLSubClassOfAxiom> learnedAxioms = new LinkedHashSet<>();
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        for (OWLSubClassOfAxiom axiom : candidateAxioms) {
            try {
//                logger.debug(myRank + " - Sending START signal to parameter learner slaves");
//                logger.debug(myRank + " - Signal sent to the slaves");
                logger.debug(myRank + " - Adding Axiom: " + axiom);
                addAxiom(ontology, axiom);
                logger.info("Axiom added.");
                logger.info("Running parameter Learner");
                edge.start();
                BigDecimal LL1 = edge.getLL();
                logger.info("Current Log-Likelihood: " + LL1);
                if (LL1.compareTo(LL0) > 0) {
                    logger.info("Log-Likelihood enhanced. Updating ontologies...");
                    OWLAnnotation annotation = df.
                    getOWLAnnotation(BundleUtilities.PROBABILISTIC_ANNOTATION_PROPERTY, 
                            df.getOWLLiteral(edge.getParameter(axiom).doubleValue()));
                    OWLSubClassOfAxiom updatedAxiom = df.getOWLSubClassOfAxiom(axiom.getSubClass(), 
                            axiom.getSuperClass(), Collections.singleton(annotation));
                    learnedAxioms.add(updatedAxiom);
                    updateOntology();
                    LL0 = LL1;
                    try {
                        MPIUtilities.sendBCastSignal(UPDATE, parameterLearnerComm);
                        logger.debug(myRank + " - Sent UPDATE signal to the slaves");
                    } catch (MPIException e) {
                        String msg = myRank + " - Cannot send UPDATE signal to slaves: " + e.getMessage();
                        logger.error(msg);
                        throw new StructureLearningException(msg);
                    }
                } else {
                    logger.info("Log-Likelihood worsened. Removing Last Axiom...");
                    removeAxiom(ontology, axiom);
                    try {
                        MPIUtilities.sendBCastSignal(REMOVE, parameterLearnerComm);
                        logger.debug(myRank + " - Sent REMOVE signal to the slaves");
                    } catch (MPIException e) {
                        String msg = myRank + " - Cannot send REMOVE signal to slaves: " + e.getMessage();
                        logger.error(msg);
                        throw new StructureLearningException(msg);
                    }
                }

            } catch (MPIException mpiEx) {
                logger.error(myRank + " - Cannot perform greedy search: " + mpiEx.getMessage());
                throw new ParameterLearningException(mpiEx);
            } catch (InconsistencyException iex) {
                logger.info(iex.getMessage());
                logger.info("Trying with the next class expression");
                continue;
            }
        }
        try {
            MPIUtilities.sendBCastSignal(STOP, parameterLearnerComm);
            logger.debug(myRank + " - Sent STOP signal to the slaves");
        } catch (MPIException mpiEx) {
            logger.error(myRank + " - Cannot send stop signal: " + mpiEx.getMessage());
            throw new ParameterLearningException(mpiEx);
        }
        return new Revision(targetAxioms, revision.getBoolVars(), learnedAxioms, LL0);
    }

    private boolean terminationCriteriaSatisfied() {
        boolean condition = stop
                || currentIteration > maxIterations
                || beamRevisions.isEmpty()
                || currentDifferenceLL.compareTo(differenceLL) <= 0
                || currentRatioLL.compareTo(ratioLL) <= 0;

        if (stop) {
            logger.info("Termination due to: STOP");
        } else if (currentIteration > maxIterations) {
            logger.info("Termination due to: max iterations reached");
        } else if (beamRevisions.isEmpty()) {
            logger.info("Termination due to: beam of revisions is empty");
        } else if (currentDifferenceLL.compareTo(differenceLL) <= 0) {
            logger.info("Termination due to: minimum diffLL threshold reached");
        } else if (currentRatioLL.compareTo(ratioLL) <= 0) {
            logger.info("Termination due to: minimum ratioLL threshold reached");
        }

        return condition;
    }

    private void addAxiom(OWLOntology ontology, OWLAxiom axiom) throws InconsistencyException, MPIException {

        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        manager.addAxiom(ontology, axiom);
        PelletReasoner pelletReasoner = new PelletReasonerFactory().createNonBufferingReasoner(ontology);
        if (!pelletReasoner.isConsistent()) {
            String message = "The axiom will make the KB inconsistent.\n"
                    + "It will NOT be added";
            logger.warn(message);
            manager.removeAxiom(ontology, axiom);
            throw new InconsistencyException(message);
        } else {
            try {
                MPIUtilities.sendBCastSignal(START, parameterLearnerComm);
                logger.debug(myRank + " - Sent START signal to slaves");
                int sentBytes = MPIUtilities.sendBCastObject(axiom, parameterLearnerComm);
                logger.debug(myRank + " - Sent to slaves OWLAxiom object (" + sentBytes + " bytes)");
            } catch (MPIException e) {
                logger.error(myRank + " - Cannot send axiom to EDGE slaves");
                throw new StructureLearningException(e);
            }
        }
    }

    private OWLAxiom recvAddAxiom(OWLOntology ontology) {
        OWLAxiom axiom;
        try {
            Object obj = MPIUtilities.recvBCastObject(MASTER, parameterLearnerComm);
            axiom = (OWLAxiom) obj;
        } catch (MPIException e) {
            String msg = myRank + " - Cannot receive axiom to add: " + e.getMessage();
            logger.error(msg);
            throw new StructureLearningException(msg);
        }
        ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
        return axiom;
    }

    private void removeAxiom(OWLOntology ontology, OWLAxiom axiom) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        manager.removeAxiom(ontology, axiom);
    }

    /**
     * @return the edge
     */
    public AbstractEDGEDistributed getEdge() {
        return edge;
    }

    /**
     * @param edge the edge to set
     */
    @Autowired
    public void setEdge(AbstractEDGEDistributed edge) {
        this.edge = edge;
    }

    private void updateOntology() {
        logger.debug("Updating ontology");
        OWLOntology ontology = edge.getLearnedOntology();
        edge.changeSourcesOntology(ontology);
        logger.debug("Ontology Updated");
    }

    private OWLOntology generateOntologyFromRevision(OWLOntology ontology, Revision revision) {
        try {
            OWLOntology revisionedOntology = BundleUtilities.copyOntology(ontology);
            Set<OWLSubClassOfAxiom> learnedAxioms = revision.getLearnedAxioms();
            OWLOntologyManager manager = revisionedOntology.getOWLOntologyManager();
            manager.addAxioms(revisionedOntology, revision.getLearnedAxioms());
            int i = 0;
            for (OWLAxiom axiom : revision.getTargetAxioms()) {
                if (!revision.getBoolVars().get(i)) {
                    manager.removeAxiom(revisionedOntology, axiom);
                }
            }
            return revisionedOntology;
        } catch (OWLOntologyCreationException e) {
            logger.error("Cannot refine ontology");
            throw new StructureLearningException(e);
        }
    }

    private void printTimings(long totalTimeMills, long celaTimeMills, Map<String, Long> timeMap) {
        logger.info("Main: " + totalTimeMills + " ms");
        logger.info("CELOE: " + celaTimeMills + " ms");
        logger.info("EDGE: " + (timeMap.get("EM") + timeMap.get("Bundle")) + " ms");
        logger.info("\tBundle: " + timeMap.get("Bundle") + " ms");
        logger.info("\tEM: " + timeMap.get("EM") + " ms");
        long timeOther = totalTimeMills - celaTimeMills - (timeMap.get("EM") + timeMap.get("Bundle"));
        logger.info("Other: " + timeOther + " ms");
        logger.info("Program client: execution successfully terminated");
    }

    private void updateTerminationCriteria() {
        currentIteration++;
        currentDifferenceLL = bestRevision.getLL().subtract(previousBestRevision.getLL());
        currentRatioLL = currentDifferenceLL.divide(previousBestRevision.getLL(), accuracy, BigDecimal.ROUND_HALF_UP);
    }

    private OWLOntology replaceSuperClass(OWLOntology finalOntology, Set<OWLSubClassOfAxiom> learnedAxioms) {
        logger.debug(myRank + " - Replacing super class \"dummyClass\" with \"classToDescribe\"");
        ClassLearningProblem clp = (ClassLearningProblem) cela.getLearningProblem();
        //Set<OWLSubClassOfAxiom> learnedAxioms = bestRevision.getLearnedAxioms();
        OWLOntologyManager man = finalOntology.getOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();
        int numInitialAxioms = finalOntology.getAxiomCount();
        // remove the learned Axioms
        //man.removeAxiom(finalOntology, learnedAxioms.iterator().next());
        Set<OWLSubClassOfAxiom> learnedAxiomsCopy = new LinkedHashSet<>(learnedAxioms);
        for (OWLAxiom axiom : finalOntology.getAxioms(AxiomType.SUBCLASS_OF)) {
            for (OWLAxiom axiomToRemove : learnedAxiomsCopy) {
                // conviene usare una copia di probAddedAxioms 
                //in maniera tale da eliminare gli assiomi gi� trovati durante la ricerca e 
                //quindi ridurre il numero di check
                //logger.debug("Learned axiom to remove: " + BundleUtilities.getManchesterSyntaxString(axiomToRemove));
                if (axiomToRemove.equalsIgnoreAnnotations(axiom)) {
                    man.removeAxiom(finalOntology, axiom);
                    learnedAxiomsCopy.remove(axiomToRemove);
                    break;
                }
            }
        }
        int numAxiomsAfterRemove = finalOntology.getAxiomCount();
        // check if correctly removed
        if (numAxiomsAfterRemove != numInitialAxioms - learnedAxioms.size()) {
            String msg = myRank + " - Error during the replacement of super class: "
                    + "Axiom remotion was incorrect. "
                    + "numAxiomsAfterRemove: " + numAxiomsAfterRemove
                    + " numInitialAxioms: " + numInitialAxioms
                    + " numAxioms to remove: " + learnedAxioms.size()
                    + " numAxioms removed: " + (numInitialAxioms - numAxiomsAfterRemove);
            logger.error(msg);
            throw new StructureLearningException(msg);
        }
        LinkedHashSet<OWLSubClassOfAxiom> newAxioms = new LinkedHashSet<>();
        for (OWLSubClassOfAxiom axiom : learnedAxioms) {
            OWLSubClassOfAxiom newAxiom = df.getOWLSubClassOfAxiom(axiom.getSubClass(),
                    clp.getClassToDescribe(), axiom.getAnnotations());
            newAxioms.add(newAxiom);
            logger.info(myRank + " - Learned Axiom: " + newAxiom);
        }
        man.addAxioms(finalOntology, newAxioms);
        // check if correctly added
        if (finalOntology.getAxiomCount() != numAxiomsAfterRemove + learnedAxioms.size()) {
            String msg = myRank + " - Error during the replacement of super class: "
                    + "Axiom addition was incorrect."
                    + " numAxiomsAfterRemove: " + numAxiomsAfterRemove
                    + " numAxioms to add: " + learnedAxioms.size()
                    + " numAxioms added: " + (finalOntology.getAxiomCount() - numAxiomsAfterRemove);;
            logger.error(msg);
            throw new StructureLearningException(msg);
        }
        logger.debug(myRank + " - Replaced all the super classes");
        return finalOntology;
    }
    
    public String getName() {
        return "LEAPDistributed";
    }
}
