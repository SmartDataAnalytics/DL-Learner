package org.dllearner.learningproblems.sampling;

import java.io.File;

import org.dllearner.learningproblems.OntologyEngineering;

public class CeloeSamplingRunningScript {
public static void main(String[] args) { 
		
		String ontologyFileName ="";
		String outputFilename ="";
		
		// TODO Auto-generated method stub
		String[] files = 
			{"adhesome","earthrealm", "eukariotic","SC" , "GeoSkills"};
//			{"Economy", "MDM0.73", "adhesome","earthrealm", "eukariotic","SC" };
		//	{"GeoSkills"};
		for (int I = 0; I < files.length; I++) {
			ontologyFileName = files[I] + ".owl";
			outputFilename = files[I] + ".sampling.txt";
			// OWL file is the first argument of the script
			// File file = new File(filePath);
			// "../UCI/Mushroom.data"
			File ontFile = new File("../Celoe datasets/" + ontologyFileName);
			String outFile = "../Celoe datasets/" + outputFilename;

			// load OWL in reasoner
			try {
				System.out.println("Loaded ontology " + ontologyFileName + ".");
				
				TemporaryOntologyEngineeringTest ontEng = new TemporaryOntologyEngineeringTest(ontFile, outFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
