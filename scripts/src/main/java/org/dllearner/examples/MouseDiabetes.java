package org.dllearner.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.MaterializableFastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.DLSyntaxObjectRenderer;
import org.junit.runner.Describable;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


public class MouseDiabetes {
    private static final Logger logger = Logger.getLogger(MouseDiabetes.class);
    private static final String dir = "/tmp/smallis/";
    private static final String kbFilePath = dir + "monarch_module_exp1.owl";
    private static final String genoDiseaseFilePath = dir + "mgi_geno_disease.ttl";
    private static final String genoNotDiseaseFilePath = dir + "mgi_geno_notdisease.ttl";
    private static final String posExamplesFilePath = dir + "pos_uris.txt";
    private static final String negExamplesFilePath = dir + "neg_uris.txt";

    public static void main(String[] args) throws OWLOntologyCreationException, IOException, ComponentInitException {
        setUp();
        logger.debug("starting...");
        OWLOntology ontology = readDumpFiles();
        logger.debug("reading positive and negative examples...");
        Set<Individual> posExamples = readExamples(posExamplesFilePath);
        Set<Individual> negExamples = readExamples(negExamplesFilePath);
        logger.debug("finished reading examples");
        
        logger.debug("initializing knowledge source...");
        KnowledgeSource ks = new OWLAPIOntology(ontology);
        ks.init();
        logger.debug("finished initializing knowledge source");
        
        logger.debug("initializing reasoner...");
        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
        baseReasoner.setReasonerTypeString("elk");
        baseReasoner.setUseFallbackReasoner(true);
        baseReasoner.init();
        Logger.getLogger(ElkReasoner.class).setLevel(Level.OFF);
        logger.debug("finished initializing reasoner");
        logger.debug("initializing reasoner component...");
        MaterializableFastInstanceChecker rc = new MaterializableFastInstanceChecker(ks);
        rc.setReasonerComponent(baseReasoner);
        rc.setHandlePunning(true);
        rc.setMaterializeExistentialRestrictions(true);
        rc.init();
        logger.debug("finished initializing reasoner");
        
        logger.debug("initializing learning problem...");
        PosNegLPStandard lp = new PosNegLPStandard(rc);
        lp.setPositiveExamples(posExamples);
        lp.setNegativeExamples(negExamples);
        lp.init();
        logger.debug("finished initializing learning problem");
        
        logger.debug("initializing learning algorithm...");
        AbstractCELA la;
        OEHeuristicRuntime heuristic = new OEHeuristicRuntime();
        heuristic.setExpansionPenaltyFactor(0.1);
        la = new CELOE(lp, rc);
        ((CELOE) la).setHeuristic(heuristic);
        ((CELOE) la).setMaxExecutionTimeInSeconds(60);
        ((CELOE) la).setNoisePercentage(50);
        ((CELOE) la).setMaxNrOfResults(10);
        ((CELOE) la).setWriteSearchTree(true);
        ((CELOE) la).setReplaceSearchTree(true);
        Description startClass = new NamedClass("http://dl-learner.org/smallis/Allelic_info");
//        startClass = new Intersection(
//        		new NamedClass("http://dl-learner.org/smallis/Allelic_info"),
//        		new ObjectSomeRestriction(new ObjectProperty("http://dl-learner.org/smallis/has_phenotype"), Thing.instance));
		((CELOE) la).setStartClass(startClass);
        logger.debug("finished initializing learning algorithm");
        logger.debug("initializing operator...");
        RhoDRDown op = new RhoDRDown();
        op.setUseHasValueConstructor(true);
        op.setInstanceBasedDisjoints(true);
        op.setUseNegation(false);
        op.setStartClass(startClass);
        op.setUseHasValueConstructor(false);
        op.setReasoner(rc);
        op.setSubHierarchy(rc.getClassHierarchy());
        op.setObjectPropertyHierarchy(rc.getObjectPropertyHierarchy());
        op.setDataPropertyHierarchy(rc.getDatatypePropertyHierarchy());
        op.init();
        logger.debug("finished initializing operator");
        ((CELOE) la).setOperator(op);
        
        la.init();
        la.start();
    }
    
    private static Set<Individual> readExamples(String filePath) throws IOException {
        Set<Individual> indivs = new HashSet<Individual>();
        BufferedReader buffRead = new BufferedReader(new FileReader(new File(filePath)));
        String line;
        while ((line = buffRead.readLine()) != null) {
            line = line.trim();
            line = line.substring(1, line.length()-1);  // strip off angle brackets
            indivs.add(new Individual(line));
        }
        buffRead.close();
        return indivs;
    }
    private static void setUp() {
        logger.setLevel(Level.DEBUG);
        Logger.getLogger(AbstractReasonerComponent.class).setLevel(Level.OFF);
        ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
    }

    public static OWLOntology readDumpFiles() throws OWLOntologyCreationException, FileNotFoundException {
        Model model = ModelFactory.createDefaultModel();

        logger.debug("reading main knowledge base (" + kbFilePath + ")...");
        model.read(new FileInputStream(new File(kbFilePath)), null, Lang.RDFXML.getName());

        logger.debug("reading positve examples data (" + genoDiseaseFilePath + ")...");
        model.read(new FileInputStream(new File(genoDiseaseFilePath)), null, Lang.TURTLE.getName());

        logger.debug("reading negative examples data (" + genoNotDiseaseFilePath + ")...");
        model.read(new FileInputStream(new File(genoNotDiseaseFilePath)), null, Lang.TURTLE.getName());

        logger.debug("finished reading files");

        logger.debug("converting to OWLApi ontology...");
        //convert JENA model to OWL API ontology
        OWLOntology ontology = getOWLOntology(model);
        logger.debug("finished conversion");
        
        if(containsErrorNamedClasses(ontology)){
        	System.exit(0);
        }
        return ontology;
    }
    
    private static boolean containsErrorNamedClasses(OWLOntology ontology){
    	for (OWLClass cls : ontology.getClassesInSignature()) {
			if(cls.toStringID().startsWith("http://org.semanticweb.owlapi/error#")){
				return true;
			}
		}
    	return false;
    }

    public static OWLOntology getOWLOntology(final Model model) {
        OWLOntology ontology;
        try (PipedInputStream is = new PipedInputStream();
                PipedOutputStream os = new PipedOutputStream(is);) {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            new Thread(new Runnable() {
                public void run() {
                    model.write(os, "TURTLE", null);
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            ontology = man.loadOntologyFromOntologyDocument(is);
            return ontology;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not convert JENA API model to OWL API ontology.", e);
        }
        }
}