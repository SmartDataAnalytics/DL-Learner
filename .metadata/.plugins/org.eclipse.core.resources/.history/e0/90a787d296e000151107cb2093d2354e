package org.dllearner.learningproblems;

import java.io.File;

public class EngineeringRunningScript {
	public static void main(String[] args) { 
		
		String ontologyFileName ="";
		String outputFilename ="";
		
		// TODO Auto-generated method stub
		String[] files = {"Economy", "MDM0.73", "GeoSkills", "adhesome","earthrealm", "eukariotic","SC" };
		for (int I=0;I< files.length; I++)
		{
			ontologyFileName = files[I]+".owl";
			outputFilename = files[I]+".txt";
		}
		

		
		// OWL file is the first argument of the script
		//File file = new File(filePath);
		//"../UCI/Mushroom.data"
		File ontFile = new File("../Celoe datasets/"+ontologyFileName);
		//File outFile = new File("../Celoe datasets/"+outputFilename);
	
		// load OWL in reasoner
		try
		{
		System.out.println("Loaded ontology " + ontologyFileName + ".");
		OntologyEngineering ontEng= new OntologyEngineering(ontFile, outputFilename);
		
		}
		catch (Exception e)
		{
			
		}
	}
}
