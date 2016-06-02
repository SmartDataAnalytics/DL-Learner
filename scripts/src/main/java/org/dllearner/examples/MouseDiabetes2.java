package org.dllearner.examples;

import com.google.common.collect.Sets;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.algorithms.el.ELLearningAlgorithm;
import org.dllearner.core.*;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.scripts.MouseDiabetesCBD;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class MouseDiabetes2 {

    private static final Logger logger = Logger.getLogger(MouseDiabetes2.class);
    private static final String dir = "/tmp/smallis/../experiment2/";
//    private static final String kbFilePath = dir + "monarch_module_exp2.owl";
//    private static final String kbFilePath = dir + "kb_cbd_05.owl";
    private static final String kbFilePath = dir + "mp_kb_cbd_05.owl";
    private static final String genoDiseaseFilePath = dir + "mgi_gene_pheno_pos.ttl";
    private static final String genoNotDiseaseFilePath = dir + "mgi_gene_pheno_neg.ttl";
    public static final String posExamplesFilePath = dir + "pos_uris.txt";
    public static final String negExamplesFilePath = dir + "neg_uris.txt";

    public static final Set<String> posExplUriStrs = new HashSet<>(Sets.newHashSet(
		    // positive
		    "http://dl-learner.org/smallis/allelic_info00001",
		    "http://dl-learner.org/smallis/allelic_info00002",
		    "http://dl-learner.org/smallis/allelic_info00003",
		    "http://dl-learner.org/smallis/allelic_info00004",
		    "http://dl-learner.org/smallis/allelic_info00005"
    ));
    public static final Set<String> negExplUriStrs = new HashSet<>(Sets.newHashSet(
		    // negative
		    "http://dl-learner.org/smallis/allelic_info00181",  // MP_0005379
		    "http://dl-learner.org/smallis/allelic_info00435",  // MP_0005387
		    "http://dl-learner.org/smallis/allelic_info01924",  // MP_0005387
		    "http://dl-learner.org/smallis/allelic_info01923",  // MP_0005387
		    "http://dl-learner.org/smallis/allelic_info00182"  // MP_0005379
    ));

    static boolean useCBD = false;

    public static void main(String[] args) throws OWLOntologyCreationException, IOException, ComponentInitException {
        setUp();
        logger.debug("starting...");
        OWLOntology ontology;
        if(useCBD){
        	ontology = readCBD();
        } else {
        	ontology = readDumpFiles();
        }

        logger.debug("reading positive and negative examples...");
//        Set<OWLIndividual> posExamples = readExamples(posExamplesFilePath);
        Set<OWLIndividual> posExamples = makeExamples(posExplUriStrs);
//        Set<OWLIndividual> negExamples = readExamples(negExamplesFilePath);
        Set<OWLIndividual> negExamples = makeExamples(negExplUriStrs);
        if(useCBD){
        	posExamples = new HashSet<>(new ArrayList<>(posExamples).subList(0, MouseDiabetesCBD.nrOfPosExamples));
        	negExamples = new HashSet<>(new ArrayList<>(negExamples).subList(0, MouseDiabetesCBD.nrOfNegExamples));
        }
        logger.debug("finished reading examples");

        logger.debug("initializing knowledge source...");
        KnowledgeSource ks = new OWLAPIOntology(ontology);
        ks.init();
        logger.debug("finished initializing knowledge source");

        logger.debug("initializing reasoner...");
        OWLAPIReasoner baseReasoner = new OWLAPIReasoner(ks);
        baseReasoner.setUseFallbackReasoner(true);
        baseReasoner.init();
        Logger.getLogger(ElkReasoner.class).setLevel(Level.OFF);
        logger.debug("finished initializing reasoner");
        logger.debug("initializing reasoner component...");
        ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
        rc.setReasonerComponent(baseReasoner);
        rc.setHandlePunning(false);
        rc.setUseMaterializationCaching(false);
        rc.setMaterializeExistentialRestrictions(true);
        rc.init();
        logger.debug("finished initializing reasoner");

//        Reasoner myObject = (Reasoner) MonProxyFactory.monitor(rc);

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
        CELOE celoe = new CELOE(lp, rc);
        celoe.setHeuristic(heuristic);
        celoe.setMaxExecutionTimeInSeconds(1800);
        celoe.setNoisePercentage(80);
        celoe.setMaxNrOfResults(50);
        celoe.setWriteSearchTree(true);
        celoe.setReplaceSearchTree(true);
        OWLClassExpression startClass = new OWLClassImpl(IRI.create("http://dl-learner.org/smallis/Allelic_info"));
//        startClass = new Intersection(
//        		new NamedClass("http://dl-learner.org/smallis/Allelic_info"),
//        		new ObjectSomeRestriction(new ObjectProperty("http://dl-learner.org/smallis/has_phenotype"), Thing.instance));
        celoe.setStartClass(startClass);

        ELLearningAlgorithm elLa = new ELLearningAlgorithm(lp, rc);
        elLa.setNoisePercentage(2.0);
        elLa.setStartClass(startClass);

        la = celoe;
//        la = elLa;

        logger.debug("finished initializing learning algorithm");
        logger.debug("initializing operator...");
        RhoDRDown op = new RhoDRDown();
        op.setInstanceBasedDisjoints(true);
        op.setUseNegation(false);
        op.setUseHasValueConstructor(true);
        op.setUseAllConstructor(false);
        op.setStartClass(startClass);
        op.setReasoner(rc);
        op.setSubHierarchy(rc.getClassHierarchy());
        op.setObjectPropertyHierarchy(rc.getObjectPropertyHierarchy());
        op.setDataPropertyHierarchy(rc.getDatatypePropertyHierarchy());
        op.init();
        logger.debug("finished initializing operator");
        if(la instanceof CELOE)
            ((CELOE) la).setOperator(op);

//        System.exit(0);
//        SortedSet<Description> subClasses = rc.getSubClasses(Thing.instance);
//        for (Description sub : subClasses) {
//			System.out.println(sub + ":" + rc.getOWLIndividuals(sub).size() + " instances");
//		}

//
//		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
//		OWLDataFactory dataFactory = man.getOWLDataFactory();
//		OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
//		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
//		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
//        NodeSet<OWLClass> subClasses2 = reasoner.getSuperClasses(
//        		dataFactory.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/MP_0001265")), false);
//		for (OWLClass cls : subClasses2.getFlattened()) {
//			System.out.println(cls);
//		}

        la.init();
        la.start();
    }

    public static Set<OWLIndividual> loadPosExamples() throws IOException {
	    return readExamples(posExamplesFilePath);
    }

    public static Set<OWLIndividual> loadNegExamples() throws IOException {
	    return readExamples(negExamplesFilePath);
    }

    public static Set<OWLIndividual> makeExamples(Set<String> uris) {
        Set<OWLIndividual> indivs = new TreeSet<>();

        for (String uri : uris) {
            indivs.add(new OWLNamedIndividualImpl(IRI.create(uri)));
        }

        return indivs;
    }
    public static Set<OWLIndividual> readExamples(String filePath) throws IOException {
        Set<OWLIndividual> indivs = new TreeSet<>();
        try(BufferedReader buffRead = new BufferedReader(new FileReader(new File(filePath)))){
	        String line;
	        while ((line = buffRead.readLine()) != null) {
	            line = line.trim();
	            line = line.substring(1, line.length()-1);  // strip off angle brackets
	            indivs.add(new OWLNamedIndividualImpl(IRI.create(line)));
	        }
        }
        return indivs;
    }
    private static void setUp() {
        logger.setLevel(Level.DEBUG);
        Logger.getLogger(AbstractReasonerComponent.class).setLevel(Level.OFF);
        StringRenderer.setRenderer(Rendering.DL_SYNTAX);
    }

    public static OWLOntology readCBD() {
        logger.debug("reading CBD-based knowledge base (" + kbFilePath + ")...");
        try(FileInputStream is = new FileInputStream(new File(MouseDiabetesCBD.cbdFilePath))){
	        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(is);
	        logger.debug("finished reading files");

	        if(containsErrorNamedClasses(ontology)){
	        	System.exit(0);
	        }

	        return ontology;
        } catch (IOException | OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	    return null;
    }

    public static OWLOntology readDumpFiles() throws IOException {
        Model model = ModelFactory.createDefaultModel();

		logger.debug("reading main knowledge base (" + kbFilePath + ")...");
		try (InputStream is = new FileInputStream(new File(kbFilePath))) {
			model.read(is, null, Lang.RDFXML.getName());
		}

		logger.debug("reading positive examples data (" + genoDiseaseFilePath + ")...");
		try (InputStream is = new FileInputStream(new File(genoDiseaseFilePath))) {
			model.read(is, null, Lang.TURTLE.getName());
		}

		logger.debug("reading negative examples data (" + genoNotDiseaseFilePath + ")...");
		try (InputStream is = new FileInputStream(new File(genoNotDiseaseFilePath))) {
			model.read(is, null, Lang.TURTLE.getName());
		}

        logger.debug("finished reading files");

        //convert JENA model to OWL API ontology
        logger.debug("converting to OWLApi ontology...");
        OWLOntology ontology = OwlApiJenaUtils.getOWLOntology(model);
        logger.debug("finished conversion");

        // sanity check
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
}
