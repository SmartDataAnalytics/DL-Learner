package org.dllearner.reasoning.fuzzydll;

import java.io.FileInputStream;
import java.util.ArrayList;

import fuzzydl.*;
import fuzzydl.milp.Solution;
import fuzzydl.parser.*;

public class FuzzyDLReasonerManager {

	private static final String auxFuzzyKBfileName = "/Users/josue/Documents/PhD/AKSW/DL_Learner/workspace/dllearner-parent/components-core/src/main/resources/FuzzyDL/examples/output/auxFuzzyKBfileName.fuzzyDL.txt";
	private static final String auxFuzzyKBfileName_manuallyModified = "/Users/josue/Documents/PhD/AKSW/DL_Learner/workspace/dllearner-parent/components-core/src/main/resources/FuzzyDL/examples/output/auxFuzzyKBfileName_manuallyModified.fuzzyDL.txt";
	private static String aux = "/Users/josue/Documents/PhD/AKSW/DL_Learner/workspace/dllearner-parent/components-core/src/main/resources/FuzzyDL/examples/output/kk.fuzzyDL.txt";
	private static String configFilename = "/Users/josue/Documents/PhD/AKSW/DL_Learner/workspace/dllearner-parent/components-core/src/main/resources/FuzzyDL/CONFIG";
	
	public FuzzyDLReasonerManager(String ontologyFile) throws Exception {
		ConfigReader.loadParameters(configFilename, new String[0]);
		KnowledgeBase fuzzyKB = parseOWLontologyToFuzzyDLsyntax(ontologyFile);
	}

	private KnowledgeBase parseOWLontologyToFuzzyDLsyntax(String ontologyFile) throws Exception {
		// TODO added by Josue: we may use an in-memory file in the future and not a HD one
		// As the parser doesn't work at 100% a manually-edited fuzzyDL-file is used
		// FuzzyOwl2toFuzzyDL f = new FuzzyOwl2toFuzzyDL(ontologyFile, auxFuzzyKBfileName);
		// f.translateOwl2Ontology();
		// return Parser.getKB(auxFuzzyKBfileName);
		
		Parser parser = new Parser(new FileInputStream(auxFuzzyKBfileName_manuallyModified));
		parser.Start();
		KnowledgeBase kb = parser.getKB();
		
		kb.solveKB();
		
		// query stored in the KB
		ArrayList <Query> queries = parser.getQueries();
		Query q1 = queries.get(0);
		System.out.println(q1);
		
		Solution result = q1.solve(kb);
		if (result.isConsistentKB())
			System.out.println(q1.toString() + result.getSolution());
		else
			System.out.println("KB is inconsistent");
		
		return null;
	}

}
