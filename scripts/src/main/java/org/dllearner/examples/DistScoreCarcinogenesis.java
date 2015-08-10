package org.dllearner.examples;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.distributed.amqp.DistScoreCELOEAMQP;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

public class DistScoreCarcinogenesis {
    private static final Logger logger = Logger.getLogger(DistScoreCarcinogenesis.class);

    private static final List<String> posExampleIRIStrs = Arrays.asList(
            "http://dl-learner.org/carcinogenesis#d1",
            "http://dl-learner.org/carcinogenesis#d10",
            "http://dl-learner.org/carcinogenesis#d101",
            "http://dl-learner.org/carcinogenesis#d102",
            "http://dl-learner.org/carcinogenesis#d103",
            "http://dl-learner.org/carcinogenesis#d106",
            "http://dl-learner.org/carcinogenesis#d107",
            "http://dl-learner.org/carcinogenesis#d108",
            "http://dl-learner.org/carcinogenesis#d11",
            "http://dl-learner.org/carcinogenesis#d12",
            "http://dl-learner.org/carcinogenesis#d13",
            "http://dl-learner.org/carcinogenesis#d134",
            "http://dl-learner.org/carcinogenesis#d135",
            "http://dl-learner.org/carcinogenesis#d136",
            "http://dl-learner.org/carcinogenesis#d138",
            "http://dl-learner.org/carcinogenesis#d140",
            "http://dl-learner.org/carcinogenesis#d141",
            "http://dl-learner.org/carcinogenesis#d144",
            "http://dl-learner.org/carcinogenesis#d145",
            "http://dl-learner.org/carcinogenesis#d146",
            "http://dl-learner.org/carcinogenesis#d147",
            "http://dl-learner.org/carcinogenesis#d15",
            "http://dl-learner.org/carcinogenesis#d17",
            "http://dl-learner.org/carcinogenesis#d19",
            "http://dl-learner.org/carcinogenesis#d192",
            "http://dl-learner.org/carcinogenesis#d193",
            "http://dl-learner.org/carcinogenesis#d195",
            "http://dl-learner.org/carcinogenesis#d196",
            "http://dl-learner.org/carcinogenesis#d197",
            "http://dl-learner.org/carcinogenesis#d198",
            "http://dl-learner.org/carcinogenesis#d199",
            "http://dl-learner.org/carcinogenesis#d2",
            "http://dl-learner.org/carcinogenesis#d20",
            "http://dl-learner.org/carcinogenesis#d200",
            "http://dl-learner.org/carcinogenesis#d201",
            "http://dl-learner.org/carcinogenesis#d202",
            "http://dl-learner.org/carcinogenesis#d203",
            "http://dl-learner.org/carcinogenesis#d204",
            "http://dl-learner.org/carcinogenesis#d205",
            "http://dl-learner.org/carcinogenesis#d21",
            "http://dl-learner.org/carcinogenesis#d22",
            "http://dl-learner.org/carcinogenesis#d226",
            "http://dl-learner.org/carcinogenesis#d227",
            "http://dl-learner.org/carcinogenesis#d228",
            "http://dl-learner.org/carcinogenesis#d229",
            "http://dl-learner.org/carcinogenesis#d231",
            "http://dl-learner.org/carcinogenesis#d232",
            "http://dl-learner.org/carcinogenesis#d234",
            "http://dl-learner.org/carcinogenesis#d236",
            "http://dl-learner.org/carcinogenesis#d239",
            "http://dl-learner.org/carcinogenesis#d23_2",
            "http://dl-learner.org/carcinogenesis#d242",
            "http://dl-learner.org/carcinogenesis#d245",
            "http://dl-learner.org/carcinogenesis#d247",
            "http://dl-learner.org/carcinogenesis#d249"
            );
    private static final List<String> negExampleIRIStrs = Arrays.asList(
            "http://dl-learner.org/carcinogenesis#d110",
            "http://dl-learner.org/carcinogenesis#d111",
            "http://dl-learner.org/carcinogenesis#d114",
            "http://dl-learner.org/carcinogenesis#d116",
            "http://dl-learner.org/carcinogenesis#d117",
            "http://dl-learner.org/carcinogenesis#d119",
            "http://dl-learner.org/carcinogenesis#d121",
            "http://dl-learner.org/carcinogenesis#d123",
            "http://dl-learner.org/carcinogenesis#d124",
            "http://dl-learner.org/carcinogenesis#d125",
            "http://dl-learner.org/carcinogenesis#d127",
            "http://dl-learner.org/carcinogenesis#d128",
            "http://dl-learner.org/carcinogenesis#d130",
            "http://dl-learner.org/carcinogenesis#d133",
            "http://dl-learner.org/carcinogenesis#d150",
            "http://dl-learner.org/carcinogenesis#d151",
            "http://dl-learner.org/carcinogenesis#d154",
            "http://dl-learner.org/carcinogenesis#d155",
            "http://dl-learner.org/carcinogenesis#d156",
            "http://dl-learner.org/carcinogenesis#d159",
            "http://dl-learner.org/carcinogenesis#d160",
            "http://dl-learner.org/carcinogenesis#d161",
            "http://dl-learner.org/carcinogenesis#d162",
            "http://dl-learner.org/carcinogenesis#d163",
            "http://dl-learner.org/carcinogenesis#d164",
            "http://dl-learner.org/carcinogenesis#d165",
            "http://dl-learner.org/carcinogenesis#d166",
            "http://dl-learner.org/carcinogenesis#d169",
            "http://dl-learner.org/carcinogenesis#d170",
            "http://dl-learner.org/carcinogenesis#d171",
            "http://dl-learner.org/carcinogenesis#d172",
            "http://dl-learner.org/carcinogenesis#d173",
            "http://dl-learner.org/carcinogenesis#d174",
            "http://dl-learner.org/carcinogenesis#d178",
            "http://dl-learner.org/carcinogenesis#d179",
            "http://dl-learner.org/carcinogenesis#d180",
            "http://dl-learner.org/carcinogenesis#d181",
            "http://dl-learner.org/carcinogenesis#d183",
            "http://dl-learner.org/carcinogenesis#d184",
            "http://dl-learner.org/carcinogenesis#d185",
            "http://dl-learner.org/carcinogenesis#d186",
            "http://dl-learner.org/carcinogenesis#d188",
            "http://dl-learner.org/carcinogenesis#d190",
            "http://dl-learner.org/carcinogenesis#d194",
            "http://dl-learner.org/carcinogenesis#d207",
            "http://dl-learner.org/carcinogenesis#d208_1"

            );
    public static void main(String[] args) throws Exception {
        String kbFilePath = args[0];
        logger.info("loading knowledge source from " + kbFilePath + " ...");
        KnowledgeSource ks = new OWLAPIOntology(readOntology(kbFilePath));
        logger.info("-Done-");

        logger.info("instantiating positive and negative examples...");
        Set<OWLIndividual> posExamples = makeExamples(posExampleIRIStrs);
        Set<OWLIndividual> negExamples = makeExamples(negExampleIRIStrs);
        logger.info("-Done-");

        logger.info("initializing base reasoner...");
        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
        baseReasoner.setReasonerImplementation(ReasonerImplementation.PELLET);
        baseReasoner.init();
        logger.info("-Done-");

//        logger.info("initializing closed world reasoner...");
//        ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
//        rc.setReasonerComponent(baseReasoner);
//        rc.setHandlePunning(false);
//        rc.init();
//        logger.info("-Done-");

        logger.info("initializing learning problem...");
//        PosNegLPStandard lp = new PosNegLPStandard(rc);
        PosNegLPStandard lp = new PosNegLPStandard(baseReasoner);
        lp.setPositiveExamples(posExamples);
        lp.setNegativeExamples(negExamples);
        lp.init();
        logger.info("-Done-");

        logger.info("initializing learning algorithm...");
//        DistScoreCELOEAMQP celoe = new DistScoreCELOEAMQP(lp, rc);
        DistScoreCELOEAMQP celoe = new DistScoreCELOEAMQP(lp, baseReasoner);
        celoe.setMaxExecutionTimeInSeconds(300);
        celoe.updateAMQPSettings("amqp.properties");
        int id = Integer.parseInt(args[1]);
        boolean isMaster = (Integer.parseInt(args[2]) > 0);
        if (isMaster) celoe.setMaster();
        celoe.setAgentID(id);
        celoe.init();
        logger.info("-Done-");

        logger.info("initializing operator...");
        logger.info("-Done-");

        logger.info("running learning algorithm...");
        ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
        celoe.start();
        logger.info("learning algorithm finished successfully");
    }

    private static Set<OWLIndividual> makeExamples(List<String> uris) {
        Set<OWLIndividual> indivs = new TreeSet<OWLIndividual>();

        for (String uri : uris) {
            indivs.add(new OWLNamedIndividualImpl(IRI.create(uri)));
        }

        return indivs;
    }

    private static OWLOntology readOntology(String filePath) throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.loadOntologyFromOntologyDocument(new File(filePath));

        return ont;
    }
}
