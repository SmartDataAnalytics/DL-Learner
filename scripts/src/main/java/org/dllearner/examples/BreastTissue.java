package org.dllearner.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyAssertion;
import org.dllearner.core.owl.DoubleDatatypePropertyAssertion;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.model.IRI;

public class BreastTissue {
	private static IRI ontologyIRI = IRI.create("http://dl-learner.org/breasttissue");
	private static final String fileName = "../test/breasttissue/files/breasttissue.txt";
	private static HashMap<String, Integer> firstClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> secondClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> thirdClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> fourthClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> fifthClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> sixthClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> seventhClass = new HashMap<String, Integer>();

	private static List<Axiom> axioms = new LinkedList<Axiom>();

	public static void main(String agrs[]) throws FileNotFoundException, ParseException {
		Scanner input = new Scanner(new File(fileName), "UTF-8");
		File owlFile = new File("../test/breasttissue/breasttissue.owl");
		long startTime, duration;
		String time;
		KB kb = new KB();
		String kbString = generateDomainAndRangeForObjectProperties();		
		KB kb2 = KBParser.parseKBFile(kbString);
		kb.addKB(kb2);

		System.out.print("Reading in breasttissue files ... ");
		startTime = System.nanoTime();

		int i = 0;
		while (input.hasNextLine()) {
			String nextLine = input.next();

			String patient = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(patient + ";", "");

			String patientClass = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(patientClass + ";", "");

			String impedivity = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(impedivity + ";", "");

			String phaseAngle = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(phaseAngle + ";", "");

			String highFrequency = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(highFrequency + ";", "");

			String impedanceDistance = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(impedanceDistance + ";", "");

			String area = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(area + ";", "");

			String areaNormalized = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(areaNormalized + ";", "");

			String maximum = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(maximum + ";", "");

			String distance = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(distance + ";", "");

			String spectralCurve  = nextLine;

			List<Axiom> axioms = mapClauses(patient, patientClass, impedivity, phaseAngle, highFrequency, impedanceDistance, area, areaNormalized, maximum, distance, spectralCurve);
			for (Axiom axiom : axioms)
				kb.addAxiom(axiom);

			i++;
		}
		int j = 0;
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
		j++;
		// generating second conf file
		System.out.print("Generating first conf file ... ");
		File confTrainFile = new File("../test/breasttissue/train1.conf");
		Files.clearFile(confTrainFile);
		generateConfFile(confTrainFile);
		generateExamples(confTrainFile, firstClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating second conf file ... ");
		File confSecondTrainFile = new File("../test/breasttissue/train2.conf");
		Files.clearFile(confSecondTrainFile);
		generateConfFile(confSecondTrainFile);
		generateExamples(confSecondTrainFile, secondClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating third conf file ... ");
		File confThirdTrainFile = new File("../test/breasttissue/train3.conf");
		Files.clearFile(confThirdTrainFile);
		generateConfFile(confThirdTrainFile);
		generateExamples(confThirdTrainFile, thirdClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating fourth conf file ... ");
		File confFourthTrainFile = new File("../test/breasttissue/train4.conf");
		Files.clearFile(confFourthTrainFile);
		generateConfFile(confFourthTrainFile);
		generateExamples(confFourthTrainFile, fourthClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating fifth conf file ... ");
		File confFifthTrainFile = new File("../test/breasttissue/train5.conf");
		Files.clearFile(confFifthTrainFile);
		generateConfFile(confFifthTrainFile);
		generateExamples(confFifthTrainFile, fifthClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating sixth conf file ... ");
		File confSixthTrainFile = new File("../test/breasttissue/train6.conf");
		Files.clearFile(confSixthTrainFile);
		generateConfFile(confSixthTrainFile);
		generateExamples(confSixthTrainFile, sixthClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating seventh conf file ... ");
		File confSeventhTrainFile = new File("../test/breasttissue/train7.conf");
		Files.clearFile(confSeventhTrainFile);
		generateConfFile(confSeventhTrainFile);
		generateExamples(confSeventhTrainFile, seventhClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");

	}

	private static List<Axiom> mapClauses(String patient, String patientClass, String impedivity, String phaseAngle, String highFrequency, String impedanceDistance, String area, String areaNormalized, String maximum, String distance, String spectralCurve) {
		ClassAssertionAxiom cmpAxiom = getConceptAssertion("Patient", "Patient"
				+ patient);
		axioms.add(cmpAxiom);
		
		double impe = Double.parseDouble(impedivity);
		DatatypePropertyAssertion dpa = getDoubleDatatypePropertyAssertion(
				"Patient" + patient, "hasImpedivityAtZeroFrequency", impe);
		axioms.add(dpa);

		double phase = Double.parseDouble(phaseAngle);
		DatatypePropertyAssertion dpb = getDoubleDatatypePropertyAssertion("Patient" + patient, "hasPhaseAngleAt500KHz"
				, phase);
		axioms.add(dpb);

		double frequency = Double.parseDouble(highFrequency);
		DatatypePropertyAssertion blo = getDoubleDatatypePropertyAssertion(
				"Patient" + patient, "hasHighFrequencySlopeOfPhaseAngle", frequency);
		axioms.add(blo);

		double contractions = Double.parseDouble(impedanceDistance);
		DatatypePropertyAssertion cho = getDoubleDatatypePropertyAssertion(
				"Patient" + patient, "hasImpedanceDistanceBetweenSpectralEnds", contractions);
		axioms.add(cho);

		double impedanceDis = Double.parseDouble(area);
		DatatypePropertyAssertion mhr = getDoubleDatatypePropertyAssertion(
				"Patient" + patient, "hasAreaUnderSpectrum", impedanceDis);
		axioms.add(mhr);

		double areaNorm = Double.parseDouble(areaNormalized);
		DatatypePropertyAssertion op = getDoubleDatatypePropertyAssertion(
				"Patient" + patient, "hasAreaNormalizedByDA", areaNorm);
		axioms.add(op);

		double max = Double.parseDouble(maximum);
		DatatypePropertyAssertion sts = getDoubleDatatypePropertyAssertion(
				"Patient" + patient, "hasMaximumOfTheSpectrum", max);
		axioms.add(sts);

		double dis = Double.parseDouble(distance);
		DatatypePropertyAssertion mv = getDoubleDatatypePropertyAssertion(
				"Patient" + patient, "hasDistanceBetweenI0AndRealPartOfTheMaximumFrequencyPoint", dis);
		axioms.add(mv);
		
		double spectralCu = Double.parseDouble(spectralCurve);
		DatatypePropertyAssertion av = getDoubleDatatypePropertyAssertion(
				"Patient" + patient, "hasLengthOfTheSpectralCurve", spectralCu);
		axioms.add(av);
		
		if (patientClass.equals("car")) {
			firstClass.put("Patient" + patient, 1);
			secondClass.put("Patient" + patient, 0);
			thirdClass.put("Patient" + patient, 0);
			fourthClass.put("Patient" + patient, 0);
			fifthClass.put("Patient" + patient, 0);
			sixthClass.put("Patient" + patient, 0);
			seventhClass.put("Patient" + patient, 0);
		} else if (patientClass.equals("fad")) {
			firstClass.put("Patient" + patient, 0);
			secondClass.put("Patient" + patient, 1);
			thirdClass.put("Patient" + patient, 0);
			fourthClass.put("Patient" + patient, 0);
			fifthClass.put("Patient" + patient, 0);
			sixthClass.put("Patient" + patient, 0);
			seventhClass.put("Patient" + patient, 1);
		} else if (patientClass.equals("mas")) {
			firstClass.put("Patient" + patient, 0);
			secondClass.put("Patient" + patient, 0);
			thirdClass.put("Patient" + patient, 1);
			fourthClass.put("Patient" + patient, 0);
			fifthClass.put("Patient" + patient, 0);
			sixthClass.put("Patient" + patient, 0);
			seventhClass.put("Patient" + patient, 1);
		} else if (patientClass.equals("gla")) {
			firstClass.put("Patient" + patient, 0);
			secondClass.put("Patient" + patient, 0);
			thirdClass.put("Patient" + patient, 0);
			fourthClass.put("Patient" + patient, 1);
			fifthClass.put("Patient" + patient, 0);
			sixthClass.put("Patient" + patient, 0);
			seventhClass.put("Patient" + patient, 1);
		} else if (patientClass.equals("con")) {
			firstClass.put("Patient" + patient, 0);
			secondClass.put("Patient" + patient, 0);
			thirdClass.put("Patient" + patient, 0);
			fourthClass.put("Patient" + patient, 0);
			fifthClass.put("Patient" + patient, 1);
			sixthClass.put("Patient" + patient, 0);
			seventhClass.put("Patient" + patient, 0);
		} else if (patientClass.equals("adi")) {
			firstClass.put("Patient" + patient, 0);
			secondClass.put("Patient" + patient, 0);
			thirdClass.put("Patient" + patient, 0);
			fourthClass.put("Patient" + patient, 0);
			fifthClass.put("Patient" + patient, 0);
			sixthClass.put("Patient" + patient, 1);
			seventhClass.put("Patient" + patient, 0);
		}
		
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

	private static void generateConfFile(File file) {
		String confHeader = "import(\"breasttissue.owl\");\n\n";
		confHeader += "reasoner = fastInstanceChecker;\n";
		confHeader += "algorithm = refexamples;\n";
		confHeader += "refexamples.noisePercentage = 0;\n";
		confHeader += "refexamples.startClass = " + getURI2("Patient") + ";\n";
		confHeader += "refexamples.writeSearchTree = false;\n";
		confHeader += "refexamples.searchTreeFile = \"log/breasttissue/searchTree.log\";\n";
		confHeader += "\n";
		Files.appendToFile(file, confHeader);
	}

	private static void generateExamples(File file, HashMap<String, Integer> patients, int i) {
		StringBuffer content = new StringBuffer();
		Set<String> keys = patients.keySet();
		for (String key : keys) {
			Integer subsValue = patients.get(key);
			if (subsValue == 1) {
				content.append("+\"" + getIndividual(key) + "\"\n");
			} else {
				content.append("-\"" + getIndividual(key) + "\"\n");
			}

		}
		Files.appendToFile(file, content.toString());
	}

	private static String getURI(String name) {
		return ontologyIRI + "#" + name;
	}

	// returns URI including quotationsmark (need for KBparser)
	private static String getURI2(String name) {
		return "\"" + getURI(name) + "\"";
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

	private static String generateDomainAndRangeForObjectProperties() throws ParseException {
		String kbString = "DPDOMAIN(" + getURI2("hasImpedivityAtZeroFrequency") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasImpedivityAtZeroFrequency") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasPhaseAngleAt500KHz") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasPhaseAngleAt500KHz") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasHighFrequencySlopeOfPhaseAngle") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasHighFrequencySlopeOfPhaseAngle") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasImpedanceDistanceBetweenSpectralEnds") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasImpedanceDistanceBetweenSpectralEnds") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasAreaUnderSpectrum") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasAreaUnderSpectrum") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasAreaNormalizedByDA") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasAreaNormalizedByDA") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMaximumOfTheSpectrum") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMaximumOfTheSpectrum") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasDistanceBetweenI0AndRealPartOfTheMaximumFrequencyPoint") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasDistanceBetweenI0AndRealPartOfTheMaximumFrequencyPoint") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasLengthOfTheSpectralCurve") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasLengthOfTheSpectralCurve") + ") = DOUBLE.\n";
		
		return kbString;
		
	}
}
