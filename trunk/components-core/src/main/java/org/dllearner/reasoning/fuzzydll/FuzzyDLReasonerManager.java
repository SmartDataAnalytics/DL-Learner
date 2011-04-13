package org.dllearner.reasoning.fuzzydll;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import fuzzydl.*;
import fuzzydl.exception.FuzzyOntologyException;
import fuzzydl.milp.Solution;
import fuzzydl.parser.*;
import fuzzydll.fuzzyowl2fuzzydlparser.FuzzyOwl2toFuzzyDL;

public class FuzzyDLReasonerManager {

	private static final String CHANGING_JUST_HIERARCHI_PROBLEM = "/Users/josue/Documents/PhD/AKSW/fuzzySemanticTools/FuzzyDLMacOSX/FuzzyDL/examples/output/fuzzyOWL2fuzzyDLparserOutput_manual.fuzzyDL.txt";
	private static final String FUZZYOWL2FUZZYDLPARSEROUTPUT = "/Users/josue/Documents/PhD/AKSW/fuzzySemanticTools/FuzzyDLMacOSX/FuzzyDL/examples/output/fuzzyOWL2fuzzyDLparserOutput.fuzzyDL.txt";
	private static String CONFIG_FILENAME = "/Users/josue/Documents/PhD/AKSW/fuzzySemanticTools/FuzzyDLMacOSX/FuzzyDL/CONFIG";
	
	private Solution queryResult;
	private KnowledgeBase fuzzyKB;
	private Parser parser;
	private SimpleShortFormProvider shortFormParser;
	
	// private OWLAPI2fuzzyDLConvertVisitor converter;
	
	private FuzzyOwl2toFuzzyDL fuzzyParser;
	
	public FuzzyDLReasonerManager(String ontologyFile) throws Exception {
		queryResult = null;
		parser = null;
		
		shortFormParser = new SimpleShortFormProvider();
		// converter = new OWLAPI2fuzzyDLConvertVisitor();
		
		ConfigReader.loadParameters(CONFIG_FILENAME, new String[0]);
		
		fuzzyKB = parseOWLontologyToFuzzyDLsyntax(ontologyFile);

		solveKB();

		// testAux();

	}
	
	private void solveKB() {
		try {
			fuzzyKB.solveKB();
		} catch (FuzzyOntologyException e) {
			e.printStackTrace();
		}	
	}

	private void testAux() {
		// query stored in the KB
		ArrayList <Query> queries = parser.getQueries();
		Query q1 = queries.get(0);
		System.out.println(q1);
		
		try {
			queryResult = q1.solve(fuzzyKB);
		} catch (FuzzyOntologyException e) {
			e.printStackTrace();
		}
		
		if (queryResult.isConsistentKB())
			System.out.println(q1.toString() + queryResult.getSolution());
		else
			System.out.println("KB is inconsistent");
	}

	private KnowledgeBase parseOWLontologyToFuzzyDLsyntax(String ontologyFile) throws Exception {
		// TODO added by Josue: we may use an in-memory file in the future and not a HD one
		// As the parser doesn't work at 100% a manually-edited fuzzyDL-file is used
		 
		// String fuzzyOnt = "/Users/josue/Documents/PhD/AKSW/ontologies/foodItems_v1.1.owl";

		
		fuzzyParser = new FuzzyOwl2toFuzzyDL(ontologyFile, FUZZYOWL2FUZZYDLPARSEROUTPUT);
		fuzzyParser.translateOwl2Ontology();
		 // return Parser.getKB(FUZZYOWL2FUZZYDLPARSEROUTPUT);
		
		// ontologyFile = CHANGING_JUST_HIERARCHI_PROBLEM;
		
		parser = new Parser(new FileInputStream(FUZZYOWL2FUZZYDLPARSEROUTPUT));
		parser.Start();
		return parser.getKB();
	}

	// added by Josue
	// TODO use beliefDegree
	public double getFuzzyMembership(OWLClassExpression oce, OWLIndividual i, double beliefDegree) {
		
		Individual fIndividual = fuzzyKB.getIndividual(shortFormParser.getShortForm((OWLEntity) i));
		Concept fConcept = fuzzyParser.getFuzzyDLExpresion(oce);
		
		// Concept auxfConcept = Concept.l_or(new Concept("Item"), new Concept("MediumSizeItem"));
		
		Query q = new MinInstanceQuery(fConcept, fIndividual);
		
		try {
			KnowledgeBase clonedFuzzyKB = fuzzyKB.clone();
			queryResult = q.solve(clonedFuzzyKB);
			if (!queryResult.isConsistentKB()){
				System.err.println("Fuzzy KB is inconsistent.");
				System.err.println("This may be a fuzzyDL reasoner bug. Press enter to continue.");
				System.err.println("concept: " + fConcept + " individual: " + fIndividual);
				Scanner sc = new Scanner(System.in);
				sc.nextLine();
				// System.exit(0);
			}
		} catch (FuzzyOntologyException e) {
			e.printStackTrace();
		}
		
		return (1 - Math.abs(beliefDegree - queryResult.getSolution()));
	}
	
	public KnowledgeBase getFuzzyKB() {
		return fuzzyKB;
	}
}
