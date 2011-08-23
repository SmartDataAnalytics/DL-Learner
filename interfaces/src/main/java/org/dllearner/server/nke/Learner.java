package org.dllearner.server.nke;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.vocabulary.OWL;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.aksw.commons.jena.Constants;
import org.aksw.commons.jena.ModelUtils;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.el.ELLearningAlgorithm;
import org.dllearner.core.*;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This object encapsulates the Learning process,
 *
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 *         Created: 15.06.11
 */
public class Learner {

    public Class reasoner = OWLAPIReasoner.class;
    //public Class reasoner = FastInstanceChecker.class;

    private static Logger log = Logger.getLogger(Learner.class);

    public List<EvaluatedDescriptionPosNeg> learn(Set<String> pos, Set<String> neg, OntModel model, int maxTime) throws IOException, ComponentInitException, LearningProblemUnsupportedException {
        ComponentManager cm = ComponentManager.getInstance();
         Monitor total = MonitorFactory.getTimeMonitor("Learner:total").start();
         Monitor totalPerEx = MonitorFactory.getTimeMonitor("Learner:totalPerEx"+(pos.size()+neg.size())).start();

        try {
            Monitor owlapi = MonitorFactory.getTimeMonitor("Learner:owlapi").start();
            model.createIndividual("http://nke.aksw.org/", model.createClass(OWL.Ontology.getURI()));
            ModelUtils.write(model, new File("test.owl"));
            PipedOutputStream out = new PipedOutputStream();
            model.write(out, Constants.RDFXML);
            RDFWriter writer = model.getWriter("RDF/XML");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writer.write(model, baos, "");

            ByteArrayInputStream bs = new ByteArrayInputStream(baos.toString().getBytes());

            log.debug("Loading ontology into OWL API");
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology retOnt = null;
            try {
                retOnt = manager.loadOntologyFromOntologyDocument(bs);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }

            AbstractKnowledgeSource ks = new OWLAPIOntology(retOnt);
            ks.init();

            owlapi.stop();
            log.debug("OWL API conversion time: " + Helper.prettyPrintNanoSeconds((long) owlapi.getLastValue()));

            // TODO: should the reasoner be initialised at every request or just once (?)

            Monitor mon = MonitorFactory.getTimeMonitor("Learner:reasoner").start();
            AbstractReasonerComponent rc = cm.reasoner(reasoner, ks);
//            ReasonerComponent rc = cm.reasoner(OWLAPIReasoner.class, ks); // try OWL API / Pellet, because ontology is not complex
            rc.init();

            mon.stop();
            log.debug("reasoner load time: " + Helper.prettyPrintNanoSeconds((long) mon.getLastValue()));

//        System.out.println(rc.getClassHierarchy());

            PosNegLPStandard lp = cm.learningProblem(PosNegLPStandard.class, rc);
            lp.setPositiveExamples(Helper.getIndividualSet(pos));
            lp.setNegativeExamples(Helper.getIndividualSet(neg));
//        lp.getConfigurator().setAccuracyMethod("fmeasure");
//        lp.getConfigurator().setUseApproximations(false);
            lp.init();

            ELLearningAlgorithm la = cm.learningAlgorithm(ELLearningAlgorithm.class, lp, rc);

            la.setInstanceBasedDisjoints(false);
//        CELOE la = cm.learningAlgorithm(CELOE.class, lp, rc);
            la.init();
            log.debug("Running learning algorithm");

            Monitor learn = MonitorFactory.getTimeMonitor("Learner:learning").start();
            la.start();
            List<EvaluatedDescriptionPosNeg> eds = new ArrayList<EvaluatedDescriptionPosNeg>();
            for (EvaluatedDescription ed : ((TreeSet<EvaluatedDescriptionPosNeg>)la.getCurrentlyBestEvaluatedDescriptions()).descendingSet()) {

                eds.add((EvaluatedDescriptionPosNeg) ed);
                if (ed.getAccuracy() < 1.0 ) {
                    break;
                }
            }

            learn.stop();
            log.debug("learning time: " + Helper.prettyPrintNanoSeconds((long) learn.getLastValue()));
            // use this to get all solutions
            // rc.getIndividuals(ed.getDescription());
            log.debug(eds.get(0).toString());
            return eds;
        } finally {
            // remove all components to avoid side effects
            cm.freeAllComponents();
            total.stop();
            totalPerEx.stop();

        }
    }

}
