package org.dllearner.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.dllearner.Config;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.Reasoner;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.dl.Individual;
import org.dllearner.kb.OntologyFileFormat;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.Helper;

/**
 * Offene Fragen:
 * 
 * Welche Rückgabetypen sind erlaubt?
 * Wie behandelt man Exceptions (z.B. aus angegebener URI kann keine Ontologie
 * gelesen werden)?
 * 
 * @author Jens Lehmann
 *
 */
@WebService(name = "DLLearnerWebService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class DLLearnerWSoriginal {

	// private String ontologyURL;
	// private String ontologyFormat;
	private Reasoner reasoner;
	private ReasoningService rs;
	private SortedSet<Individual> positiveExamples = new TreeSet<Individual>();
	private SortedSet<Individual> negativeExamples = new TreeSet<Individual>();
	
	/**
	 * Specifies the URI of the ontology containing the background 
	 * knowledge. Reads the ontology and sends it to the reasoner.
	 * 
	 * @param ontologyURI The URI of the ontology to use.
	 */
	// gleiche Methoden mit verschiedenen Parametern sind offenbar problematisch
	/*
	@WebMethod
	public void readOntology(String ontologyURI) {
		readOntology(ontologyURI, "RDF/XML");
	}
	*/
	
	/**
	 * Specifies the URI of the ontology containing the background 
	 * knowledge and its format. Reads the ontology and sends it to
	 * the reasoner.
	 * 
	 * @param ontologyURI The URI of the ontology to use.
	 * @param format "RDF/XML" or "N-TRIPLES".
	 */
	@WebMethod
	public void readOntology(String ontologyURL, String format) {
		// this.ontologyURL = ontologyURL;
		// this.ontologyFormat = format;
		
		// TODO: potentielles Sicherheitsrisiko, da man damit derzeit auch lokale Dateien
		// laden könnte (Fix: nur http:// zulassen, kein file://)
		URL ontology = null;
		try {
			ontology = new URL(ontologyURL);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		OntologyFileFormat ofFormat;
		if (format.equals("RDF/XML"))
			ofFormat = OntologyFileFormat.RDF_XML;
		else
			ofFormat = OntologyFileFormat.N_TRIPLES;
		
		Map<URL, OntologyFileFormat> m = new HashMap<URL, OntologyFileFormat>();
		m.put(ontology, ofFormat);
		
		// Default-URI für DIG-Reasoner setzen
//		try {
//			Config.digReasonerURL = new URL("http://localhost:8081");
//		} catch (MalformedURLException e) {
//			// Exception tritt nie auf, da URL korrekt
//			e.printStackTrace();
//		}		
		 
		 // reasoner = Main.createReasoner(new KB(), m);
		 
		 rs = new ReasoningService(reasoner);
		 
	}
	
	
	@WebMethod
	public String[] testString(String c) {
		
		return new String[]{"a","b"};
	}
	
	// String[] funktioniert leider noch nicht
	@WebMethod
	public void addPositiveExamples(String[] posExamples) {
		for(String example : posExamples)
			positiveExamples.add(new Individual(example));
	}
	
	@WebMethod
	public void addNegativeExamples(String[] negExamples) {
		for(String example : negExamples)
			negativeExamples.add(new Individual(example));
	}
	

	@WebMethod
	public void addPositiveExample(String posExample) {
		positiveExamples.add(new Individual(posExample));
	}
	
	@WebMethod
	public void addNegativeExample(String negExample) {
		negativeExamples.add(new Individual(negExample));
	}	
	
	@WebMethod
	public String learnConcept() {
		// notwendige Vorverarbeitungsschritte für den Lernalgorithmus
		// - es müssen ein paar Konzepte, die ev. von Jena generiert wurden ignoriert
		//   werden
		// - die Subsumptionhierarchie muss erstellt werden
		// - die Subsumptionhierarchie wird verbessert um das Lernen effizienter zu machen
		Helper.autoDetectConceptsAndRoles(rs);
		reasoner.prepareSubsumptionHierarchy();
		if (Config.Refinement.improveSubsumptionHierarchy) {
			try {
				reasoner.getSubsumptionHierarchy().improveSubsumptionHierarchy();
			} catch (ReasoningMethodUnsupportedException e) {
				// solange DIG-Reasoner eingestellt ist, schlägt diese Operation nie fehl
				e.printStackTrace();
			}
		}
		
		// LearningProblem learningProblem = new LearningProblem(rs, positiveExamples, negativeExamples);
		PosNegLP learningProblem = null;
		// erstmal wird nur der Refinement-Learner als Web-Service angeboten
		ROLearner learner = new ROLearner(learningProblem, null);
		return learner.getBestSolution().toString();
	}

	// Testmethode
	@WebMethod
	public String hello(String name) {
		return "Hello " + name + "!";
	}		
	
}