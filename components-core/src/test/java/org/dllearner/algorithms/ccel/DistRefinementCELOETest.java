package org.dllearner.algorithms.ccel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import junit.framework.Assert;
import mpi.Group;
import mpi.Intracomm;
import mpi.MPI;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.ccel.utils.RefinementData;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.utilities.mpi.MPIConfig;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.common.collect.Sets;

public class DistRefinementCELOETest {
    private static Logger logger = Logger.getLogger(DistRefinementCELOETest.class);
    private static int myRank;
    private static int numProcesses;
    private static OWLDataFactory dataFactory;
    private static DistRefinementCELOE celoe;
    private static int[] workerRanks;

    // <--------------------------- JUnit Tests --------------------------->
    @Test
    public void testShuffleWorkers() {
        int[] workers = { 3, 4, 5, 6, 7, 8 };
        HashSet<Integer> workersSet = Sets.newHashSet(3, 4, 5, 6, 7, 8);
        DistRefinementCELOE.shuffleWorkers(workers);

        Assert.assertEquals(6, workers.length);

        for (int worker : workers) {
            Assert.assertTrue(workersSet.remove(worker));
        }
        Assert.assertTrue(workersSet.isEmpty());
    }

    // </-------------------------- JUnit Tests --------------------------->
    // <--------------------- Simple exception tests ---------------------->
    private static void initDistRefinementCELOE(DistRefinementCELOE celoe) throws ComponentInitException, OWLException, IOException {
        // FIXME: I'm assuming smpdev as third argument here, which might not hold
        MPIConfig mpiConfig = new MPIConfig( myRank, numProcesses,
                MPIConfig.DeviceName.SMPDEV);
        celoe.setMpiConfig(mpiConfig);

        workerRanks = new int[numProcesses-1];
        for (int i=1; i<numProcesses; i++) {
            workerRanks[i-1] = i;
        }

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        Resource owlFile = new ClassPathResource("/org/dllearner/kb/owl-api-ontology-data.owl");
        OWLOntology ontology = man.loadOntologyFromOntologyDocument(owlFile.getInputStream());
        KnowledgeSource source = new OWLAPIOntology(ontology);
        OWLAPIReasoner reasoner = new OWLAPIReasoner(source);
        reasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
        reasoner.init();
        celoe.setReasoner(reasoner);
        celoe.init();
    }

    private static void testTerminateWorkers() throws Exception {
        logger.info("--------------------------------------------------------");
        logger.info("Testing terminateWorkers() method (proc. " + myRank + ")...");
        celoe.initMPI();
        if (myRank != 0) {
            // this blocks until worker is terminated
            celoe.listenForRefinementRequests();
        }

        if (myRank == 0){
            celoe.terminateWorkers();
        }
        celoe.finalizeMPI();
        logger.info("Finished testing terminateWorkers() method (proc. " + myRank + ").");
    }

    private static void testSendNode() throws Exception {
        logger.info("--------------------------------------------------------");
        logger.info("Testing sendNode(...) method (proc. " + myRank + ")...");
        List<OENode> nodes = new ArrayList<OENode>();
        celoe.initMPI();

        for (int i=1; i<numProcesses; i++) {
            OENode node = new OENode(null, dataFactory.getOWLThing(), i/10f);
            nodes.add(node);
        }

        if (myRank != 0) {
            celoe.listenForRefinementRequests();
        }

        if (myRank == 0) {
            for (int i=1; i<numProcesses; i++) {
                celoe.sendNode(i, nodes.get(i-1));
            }
            for (int i=1; i<numProcesses; i++) {
                celoe.blockingReceiveRefinementData();
            }
        }
        if (myRank == 0) {
            for (int i=1; i<numProcesses; i++) {
                celoe.sendNode(i, nodes.get(i-1));
            }
            for (int i=1; i<numProcesses; i++) {
                celoe.blockingReceiveRefinementData();
            }
        }
        if (myRank == 0) {
            celoe.terminateWorkers();
        }

        celoe.finalizeMPI();
        logger.info("Finished testing sendNode(...) method (proc. " + myRank + ")...");
    }

    private static void testBlockingSendNode() {
        logger.info("--------------------------------------------------------");
        logger.info("Testing blockingSendNode(...) method (proc. " + myRank + ")...");
        celoe.initMPI();
        List<OENode> nodes = new ArrayList<OENode>();

        for (int i=1; i<numProcesses; i++) {
            OENode node = new OENode(null, dataFactory.getOWLThing(), ((i/10f) + 0.01f));
            nodes.add(node);
        }

        if (myRank != 0) {
            celoe.listenForRefinementRequests();
        }

        if (myRank == 0) {
            for (int i=1; i<numProcesses; i++) {
                celoe.blockingSendNode(i, nodes.get(i-1));
            }
            for (int i=1; i<numProcesses; i++) {
                celoe.blockingReceiveRefinementData();
            }
        }
        if (myRank == 0) {
            for (int i=1; i<numProcesses; i++) {
                celoe.blockingSendNode(i, nodes.get(i-1));
            }
            for (int i=1; i<numProcesses; i++) {
                celoe.blockingReceiveRefinementData();
            }
        }
        if (myRank == 0) {
            celoe.terminateWorkers();
        }

        celoe.finalizeMPI();
        logger.info("Finished testing blockingSendNode(...) method (proc. " + myRank + ")...");
    }

    private static void testSend_BlockingReceiveRefinementData() {
        logger.info("--------------------------------------------------------");
        logger.info("Testing sendRefinementData(...), blockingReceiveRefinementData() methods (proc. " + myRank + ")...");
        celoe.initMPI();
        List<RefinementData> refinementData = new ArrayList<RefinementData>();

        for (int i=1; i<=(3*(numProcesses-1)); i++) {
            OENode node = new OENode(null, dataFactory.getOWLThing(), ((i/10f) + 0.01f));

            TreeSet<OWLClassExpression> refinements = new TreeSet<OWLClassExpression>();
            RefinementData refinementRecord = new RefinementData(node, refinements, 23);
            refinementData.add(refinementRecord);
        }
        Group workers = MPI.COMM_WORLD.Group();
        workers.Incl(workerRanks);
        if (myRank != 0) {
            celoe.sendRefinementData(refinementData.get(myRank-1));
            celoe.sendRefinementData(refinementData.get(myRank-1+(numProcesses-1)));
            celoe.sendRefinementData(refinementData.get(myRank-1+(2*(numProcesses-1))));
        }

        // wait until all workers have sent their refinements...
        Intracomm COMM_WORKERS = MPI.COMM_WORLD.Create(workers);
        COMM_WORKERS.Barrier();

        // ...and then collect all refinements
        if (myRank == 0) {
            for (int i=1; i<=(3*(numProcesses-1)); i++) {
                celoe.blockingReceiveRefinementData();
            }
        }

        if (myRank == 0) {
            celoe.terminateWorkers();
        }

        celoe.finalizeMPI();
        logger.info("Finished testing sendRefinementData(), blockingReceiveRefinementData() methods (proc. " + myRank + ")...");
    }

    private static void testSend_ReceiveRefinementData() throws Exception {
        logger.info("--------------------------------------------------------");
        logger.info("Testing sendRefinementData(...), receiveRefinementData() methods (proc. " + myRank + ")...");
        celoe.initMPI();
        List<RefinementData> refinementData = new ArrayList<RefinementData>();
        int numSentRefinements = 3 * (numProcesses - 1);
        for (int i=1; i<=numSentRefinements; i++) {
            OENode node = new OENode(null, dataFactory.getOWLThing(), ((i/10f) + 0.01f));

            TreeSet<OWLClassExpression> refinements = new TreeSet<OWLClassExpression>();
            RefinementData refinementRecord = new RefinementData(node, refinements, 23);
            refinementData.add(refinementRecord);
        }
        Group workers = MPI.COMM_WORLD.Group();
        workers.Incl(workerRanks);
        if (myRank != 0) {
            celoe.sendRefinementData(refinementData.get(myRank-1));
            celoe.sendRefinementData(refinementData.get(myRank-1+(numProcesses-1)));
            celoe.sendRefinementData(refinementData.get(myRank-1+(2*(numProcesses-1))));
        }

        // wait until all workers have sent their refinements...
        Intracomm COMM_WORKERS = MPI.COMM_WORLD.Create(workers);
        COMM_WORKERS.Barrier();

        // ...and then collect all refinements
        if (myRank == 0) {
            int refinementsCntr = 0;
            while (celoe.receiveRefinementData() != null) {
                refinementsCntr++;
            }

            if (refinementsCntr != numSentRefinements) {
                throw new CCELTestException("Did not get as many refinements as were sent");
            }
        }

        if (myRank == 0) {
            celoe.terminateWorkers();
        }

        celoe.finalizeMPI();
        logger.info("Finished testing sendRefinementData(), " +
                "blockingReceiveRefinementData() methods (proc. " + myRank + ")...");
    }

    private static void testGetNextNodesToExpand() throws Exception {
        logger.info("Testing getNextNodesToExpand() method (proc. " + myRank + ")...");
        celoe.initMPI();

        if (myRank == 0) {
            TreeSet<OENode> nodes = new TreeSet<OENode>(new OEHeuristicRuntime());
            for (int i=0; i<=numProcesses; i++) {
                nodes.add(new OENode(null, dataFactory.getOWLThing(), 0.03*i));
            }
            celoe.setNodes(nodes);
            List<OENode> nextNodes = celoe.getNextNodesToExpand();

            if (nextNodes.size() != (numProcesses-1)) {
                throw new CCELTestException("getNextNodes() returned more or less nodes than expected");
            }

            int cntr = numProcesses;
            for (OENode node : nextNodes) {
                if (node.getAccuracy() != cntr*0.03) {
                    throw new CCELTestException("getNextNodes() returned a list that is not ordered as expected");
                }

                cntr--;
            }

            nodes.clear();
            for (int i=0; i<=1; i++) {
                nodes.add(new OENode(null, dataFactory.getOWLThing(), 0.03*i));
            }
            celoe.setNodes(nodes);
            nextNodes = celoe.getNextNodesToExpand();
            if (nextNodes.size() != 2) {
                throw new CCELTestException("getNextNodes() returned more or less nodes than expected");
            }
            cntr = 1;
            for (OENode node : nextNodes) {
                if (node.getAccuracy() != cntr*0.03) {
                    throw new CCELTestException("getNextNodes() returned a list that is not ordered as expected");
                }
                cntr--;
            }
            celoe.terminateWorkers();
        }

        celoe.finalizeMPI();
        logger.info("Finished testing getNextNodesToExpand() method (proc. " + myRank + ").");
    }

    public static void main(String[] args) throws Exception {
        myRank = Integer.parseInt(args[0]);
        numProcesses = Integer.parseInt(args[1]);
        logger.info("MPI process started with rank " + myRank);
        dataFactory = OWLManager.getOWLDataFactory();
        celoe = new DistRefinementCELOE();
        initDistRefinementCELOE(celoe);

        testTerminateWorkers();
        testSendNode();  // non-blocking send
        testBlockingSendNode();
        testSend_BlockingReceiveRefinementData();
        testSend_ReceiveRefinementData();
        testGetNextNodesToExpand();
    }
    // </-------------------- Simple exception tests ---------------------->
}
