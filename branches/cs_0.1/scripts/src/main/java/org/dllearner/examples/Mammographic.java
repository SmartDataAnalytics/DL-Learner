package org.dllearner.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyAssertion;
import org.dllearner.core.owl.DoubleDatatypePropertyAssertion;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.model.IRI;

public class Mammographic {

	private static IRI ontologyIRI = IRI
	.create("http://dl-learner.org/mammographic");
	private static final String fileName = "test/mammographic/files/mammographic_masses.data";
	private static HashMap<String, String> shapes;
	private static HashMap<String, String> margines;
	private static HashMap<String, String> densities;
	private static HashMap<String, Integer> patients = new HashMap<String, Integer>();
	private static List<Axiom> axioms = new LinkedList<Axiom>();
	private static Set<String> measures = new TreeSet<String>();
	private static Set<String> shape = new TreeSet<String>();
	private static Set<String> margin = new TreeSet<String>();
	private static Set<String> density = new TreeSet<String>();
	
	public static void main(String agrs[]) throws FileNotFoundException, ParseException {
		Scanner input = new Scanner(new File(fileName), "UTF-8");
		File owlFile = new File("test/mammographic/mammographic.owl");
		long startTime, duration;
		String time;
		createShapeMapping();
		createMarginMapping();
		createDensityMapping();
		setMeasures();
		KB kb = new KB();
		
		NamedClass atomClass = getAtomicConcept("Measure");
		for (String measure : measures) {
			NamedClass elClass = getAtomicConcept(measure);
			SubClassAxiom sc = new SubClassAxiom(elClass, atomClass);
			kb.addAxiom(sc);
		}
		
		NamedClass subClass = getAtomicConcept("Shape");
		for (String shapes : shape) {
			NamedClass elClass = getAtomicConcept(shapes);
			SubClassAxiom sc = new SubClassAxiom(elClass, subClass);
			kb.addAxiom(sc);
		}
		
		NamedClass subsClass = getAtomicConcept("Margin");
		for (String margines : margin) {
			NamedClass elClass = getAtomicConcept(margines);
			SubClassAxiom sc = new SubClassAxiom(elClass, subsClass);
			kb.addAxiom(sc);
		}
		
		NamedClass subClasses = getAtomicConcept("Density");
		for (String densities : density) {
			NamedClass elClass = getAtomicConcept(densities);
			SubClassAxiom sc = new SubClassAxiom(elClass, subClasses);
			kb.addAxiom(sc);
		}
		
		String kbString = generateDomainAndRangeForObjectProperties();		
		KB kb2 = KBParser.parseKBFile(kbString);
		kb.addKB(kb2);
		
		System.out.print("Reading in mammographic files ... ");
		startTime = System.nanoTime();
		// create subclasses of atom
		int i = 0;
		while ( input.hasNextLine() ) {
			String nextLine = input.next();
			String biRads = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(biRads + ",", "");
			String age = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(age + ",", "");
			String shape = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(shape + ",", "");
			shape = shapes.get(shape);
			String margin = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(margin + ",", "");
			margin = margines.get(margin);
			String density = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(density + ",", "");
			density = densities.get(density);
			String malignant = nextLine;
			List<Axiom> axioms = mapClauses(biRads, age, shape, margin, density, malignant, i);
			for (Axiom axiom : axioms)
				kb.addAxiom(axiom);
			
			i++;
		}
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		
		// writing generated knowledge base
		System.out.print("Writing OWL file ... ");
		startTime = System.nanoTime();
		OWLAPIReasoner.exportKBToOWL(owlFile, kb, ontologyIRI);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		
		// generating second conf file
		System.out.print("Generating  conf file ... ");
		File confTrainFile = new File("test/mammographic/train.conf");
		Files.clearFile(confTrainFile);
		generateConfFile(confTrainFile);
		generateExamples(confTrainFile);	
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		System.out.println("Finished");
	}
	
	// create chemical element list
	private static void createShapeMapping() {
		shapes = new HashMap<String, String>();
		shapes.put("0", "unknown");
		shapes.put("1", "round");
		shapes.put("2", "oval");
		shapes.put("3", "lobular");
		shapes.put("4", "irregular");

	}
	
	// create chemical element list
	private static void createMarginMapping() {
		margines = new HashMap<String, String>();
		margines.put("0", "unknown");
		margines.put("1", "circumscribed");
		margines.put("2", "microlobulated");
		margines.put("3", "obscured");
		margines.put("4", "ill-defined");
		margines.put("5", "spiculated");

	}
	
	// create chemical element list
	private static void createDensityMapping() {
		densities = new HashMap<String, String>();
		densities.put("0", "unknown");
		densities.put("1", "high");
		densities.put("2", "iso");
		densities.put("3", "low");
		densities.put("4", "fat-containing");

	}
	
	private static List<Axiom> mapClauses(String bi, String age, String shape, String margin, String density, String malignant, int i) {
		
		
		ClassAssertionAxiom cmpAxiom = getConceptAssertion("Patient",
				"Patient" + i);
		axioms.add(cmpAxiom);
		
		double biRads = Double
		.parseDouble(bi);
		
		DatatypePropertyAssertion dpa = getDoubleDatatypePropertyAssertion(
				"Patient"+i, "hasBiRads", biRads);
		axioms.add(dpa);
		
		double agePatient = Double
		.parseDouble(age);
		
		DatatypePropertyAssertion dpb = getDoubleDatatypePropertyAssertion(
				"Patient"+i, "hasAge", agePatient);
		axioms.add(dpb);
		
		ObjectPropertyAssertion sa = getRoleAssertion("hasShape",
				"Patient" + i, shape);
		axioms.add(sa);
		
		ClassAssertionAxiom compAxiom = getConceptAssertion(shape,
				shape);
		axioms.add(compAxiom);
		
		ObjectPropertyAssertion ma = getRoleAssertion("hasMargin",
				"Patient" + i, margin);
		axioms.add(ma);
		
		ClassAssertionAxiom maAxiom = getConceptAssertion(margin,
				margin);
		axioms.add(maAxiom);
		
		ObjectPropertyAssertion ca = getRoleAssertion("hasDensity",
				"Patient" + i, density);
		axioms.add(ca);
		
		ClassAssertionAxiom denAxiom = getConceptAssertion(density,
				density);
		axioms.add(denAxiom);
		
		patients.put("Patient" + i, new Integer(malignant));
		
		return axioms;
	}
	
	private static NamedClass getAtomicConcept(String name) {
		return new NamedClass(ontologyIRI + "#" + name);
	}
	
	private static ClassAssertionAxiom getConceptAssertion(String concept,
			String i) {
		Individual ind = getIndividual(i);
		NamedClass c = getAtomicConcept(concept);
		return new ClassAssertionAxiom(c, ind);
	}
	
	private static Individual getIndividual(String name) {
		return new Individual(ontologyIRI + "#" + name);
	}
	
	private static ObjectPropertyAssertion getRoleAssertion(String role,
			String i1, String i2) {
		Individual ind1 = getIndividual(i1);
		Individual ind2 = getIndividual(i2);
		ObjectProperty ar = getRole(role);
		return new ObjectPropertyAssertion(ar, ind1, ind2);
	}
	
	private static ObjectProperty getRole(String name) {
		return new ObjectProperty(ontologyIRI + "#" + name);
	}
	
	private static void generateConfFile(File file) {
		String confHeader = "import(\"mammographic.owl\");\n\n";
		confHeader += "reasoner = fastInstanceChecker;\n";
		confHeader += "algorithm = refexamples;\n";
		confHeader += "refexamples.noisePercentage = 5;\n";
		confHeader += "refexamples.startClass = " + getURI2("Patient") + ";\n";
		confHeader += "refexamples.writeSearchTree = false;\n";
		confHeader += "refexamples.searchTreeFile = \"log/mammographic/searchTree.log\";\n";
		confHeader += "\n";
		Files.appendFile(file, confHeader);
	}
	
	private static void generateExamples(File file) {
		StringBuffer content = new StringBuffer();
		Set<String> keys = patients.keySet();
		for(String key: keys) {
			Integer subsValue = patients.get(key);
			if(subsValue == 1) {
				content.append("+\"" + getIndividual(key) + "\"\n");
			} else {
				content.append("-\"" + getIndividual(key) + "\"\n");
			}
		}
		Files.appendFile(file, content.toString());
	}
	
	private static String getURI(String name) {
		return ontologyIRI + "#" + name;
	}

	// returns URI including quotationsmark (need for KBparser)
	private static String getURI2(String name) {
		return "\"" + getURI(name) + "\"";
	}
	
	private static void setMeasures() {
		measures.add("Density");
		measures.add("Margin");
		measures.add("Shape");
				
		shape.add("unknown");
		shape.add("round");
		shape.add("oval");
		shape.add("lobular");
		shape.add("irregular");
		
		margin.add("unknown");
		margin.add("circumscribed");
		margin.add("microlobulated");
		margin.add("obscured");
		margin.add("ill-defined");
		margin.add("spiculated");
		
		density.add("unknown");
		density.add("high");
		density.add("iso");
		density.add("low");
		density.add("fat-containing");
	}
	
	private static DoubleDatatypePropertyAssertion getDoubleDatatypePropertyAssertion(
			String individual, String datatypeProperty, double value) {
		Individual ind = getIndividual(individual);
		DatatypeProperty dp = getDatatypeProperty(datatypeProperty);
		return new DoubleDatatypePropertyAssertion(dp, ind, value);
	}
	
	private static DatatypeProperty getDatatypeProperty(String name) {
		return new DatatypeProperty(ontologyIRI + "#" + name);
	}
	
	private static String generateDomainAndRangeForObjectProperties() {
		String kbString = "OPDOMAIN(" + getURI2("hasDensity") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasDensity") + ") = " + getURI2("Density")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("hasShape") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasShape") + ") = " + getURI2("Shape")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("hasMargin") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasMargin") + ") = " + getURI2("Margin")
		+ ".\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasAge") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasAge") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasBiRads") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasBiRads") + ") = DOUBLE.\n";

		return kbString;
	}

}
