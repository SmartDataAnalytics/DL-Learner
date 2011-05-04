package org.dllearner.reasoning.fuzzydll;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import fuzzydl.*;
import fuzzydl.exception.FuzzyOntologyException;
import fuzzydl.milp.Solution;
import fuzzydl.parser.*;
import fuzzydll.fuzzyowl2fuzzydlparser.*;

public class FuzzyDLReasonerManager {

	// private static final String CHANGING_JUST_HIERARCHI_PROBLEM = "/Users/josue/Documents/PhD/AKSW/fuzzySemanticTools/FuzzyDLMacOSX/FuzzyDL/examples/output/fuzzyOWL2fuzzyDLparserOutput_manual.fuzzyDL.txt";
	private static final String FUZZYOWL2FUZZYDLPARSEROUTPUT = "fuzzyOWL2fuzzyDLparserOutput.fuzzyDL.txt";
	private static String CONFIG_FILENAME = "/Users/josue/Documents/PhD/AKSW/fuzzySemanticTools/FuzzyDLMacOSX/FuzzyDL/CONFIG";

	private Solution queryResult;
	private KnowledgeBase fuzzyKB;
	private Parser parser;
	private SimpleShortFormProvider shortFormParser;

	private FuzzyOwl2toFuzzyDL fuzzyFileParser;
	private int auxCounter = 0;
	private FileOutputStream errorFile;

	public FuzzyDLReasonerManager(String ontologyFile) throws Exception {
		queryResult = null;
		parser = null;

		shortFormParser = new SimpleShortFormProvider();

		ConfigReader.loadParameters(CONFIG_FILENAME, new String[0]);

		fuzzyKB = parseOWLontologyToFuzzyDLsyntax(ontologyFile);
//		fuzzyFileParser.setBaseKB(fuzzyKB);
		OWLAPI_fuzzyDLObjectParser.setParsingFuzzyKB(fuzzyFileParser, fuzzyKB);
		
		solveKB();
		
		errorFile = new FileOutputStream("errorFile.txt");
	}

	private void solveKB() {
		try {
			fuzzyKB.solveKB();
		} catch (FuzzyOntologyException e) {
			e.printStackTrace();
		}	
	}

	private KnowledgeBase parseOWLontologyToFuzzyDLsyntax(String ontologyFile) throws Exception {
		// TODO added by Josue: we may use an in-memory file in the future and not a HD one
		// As the parser doesn't work at 100% a manually-edited fuzzyDL-file is used

		fuzzyFileParser = new FuzzyOwl2toFuzzyDL(ontologyFile, FUZZYOWL2FUZZYDLPARSEROUTPUT);
		fuzzyFileParser.translateOwl2Ontology();

//		System.err.println("WARNING: you're using a particular fuzzy ontology");
//		parser = new Parser(new FileInputStream(CHANGING_JUST_HIERARCHI_PROBLEM));
		parser = new Parser(new FileInputStream(FUZZYOWL2FUZZYDLPARSEROUTPUT));

		parser.Start();
		return parser.getKB();
	}

	// added by Josue
	public double getFuzzyMembership(OWLClassExpression oce, OWLIndividual i, double truthDegree) {

			Individual fIndividual = fuzzyKB.getIndividual(shortFormParser.getShortForm((OWLEntity) i));
			Concept fConcept = OWLAPI_fuzzyDLObjectParser.getFuzzyDLExpresion(oce);
			
			try {
				errorFile.write(fConcept.toString().getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			try {
//				fuzzyKB.saveToFile("file.txt");
//				Parser parser = new Parser(new FileInputStream("file.txt"));
//				parser.Start();
//				fuzzyKB = parser.getKB();
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}
			
			if (fConcept.toString().equalsIgnoreCase("SOME_hasFirstCar_SOME_inFrontOf_LongCar"))
					System.err.println(fConcept);
			
			System.err.println(fConcept);
			
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
			} catch (Exception e) {
				e.printStackTrace();
				try {
					errorFile.write(" 1".getBytes());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			try {
				errorFile.write("\n".getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return (1 - Math.abs(truthDegree - queryResult.getSolution()));
	}

	public KnowledgeBase getFuzzyKB() {
		return fuzzyKB;
	}
}
