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

public class Parkinsons {

	private static IRI ontologyIRI = IRI
			.create("http://dl-learner.org/parkinsons");
	private static final String fileName = "../test/parkinsons/files/parkinsons.data";
	private static HashMap<String, Integer> patients = new HashMap<String, Integer>();
	private static List<Axiom> axioms = new LinkedList<Axiom>();

	public static void main(String agrs[]) throws FileNotFoundException,
			ParseException {
		Scanner input = new Scanner(new File(fileName), "UTF-8");
		File owlFile = new File("../test/parkinsons/parkinsons.owl");
		long startTime, duration;
		String time;
		KB kb = new KB();

		String kbString = generateDomainAndRangeForObjectProperties();
		KB kb2 = KBParser.parseKBFile(kbString);
		kb.addKB(kb2);

		System.out.print("Reading in parkinsons files ... ");
		startTime = System.nanoTime();

		while (input.hasNextLine()) {
			String nextLine = input.next();

			String name = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(name + ",", "");

			String MDVPFo = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(MDVPFo + ",", "");

			String MDVPFhi = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(MDVPFhi + ",", "");

			String MDVPFlo = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(MDVPFlo + ",", "");

			String MDVPJitter = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(MDVPJitter + ",", "");

			String MDVPJitterAbs = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(MDVPJitterAbs + ",", "");

			String MDVPRAP = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(MDVPRAP + ",", "");

			String MDVPPPQ = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(MDVPPPQ + ",", "");

			String JitterDDP = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(JitterDDP + ",", "");

			String MDVPShimmer = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(MDVPShimmer + ",", "");

			String MDVPShimmerdB = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(MDVPShimmerdB + ",", "");

			String ShimmerAPQ3 = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(ShimmerAPQ3 + ",", "");

			String ShimmerAPQ5 = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(ShimmerAPQ5 + ",", "");

			String MDVPAPQ = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(MDVPAPQ + ",", "");

			String ShimmerDDA = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(ShimmerDDA + ",", "");

			String NHR = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(NHR + ",", "");

			String HNR = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(HNR + ",", "");

			String status = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(status + ",", "");

			String RPDE = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(RPDE + ",", "");

			String DFA = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(DFA + ",", "");

			String spread1 = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(spread1 + ",", "");

			String spread2 = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(spread2 + ",", "");

			String D2 = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(D2 + ",", "");

			String PPE = nextLine;

			List<Axiom> axioms = mapClauses(name, MDVPFo, MDVPFhi, MDVPFlo,
					MDVPJitter, MDVPJitterAbs, MDVPRAP, MDVPPPQ, JitterDDP,
					MDVPShimmer, MDVPShimmerdB, ShimmerAPQ3, ShimmerAPQ5,
					MDVPAPQ, ShimmerDDA, NHR, HNR, status, RPDE, DFA, spread1,
					spread2, D2, PPE);
			for (Axiom axiom : axioms)
				kb.addAxiom(axiom);

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
		File confTrainFile = new File("../test/parkinsons/train.conf");
		Files.clearFile(confTrainFile);
		generateConfFile(confTrainFile);
		generateExamples(confTrainFile);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		System.out.println("Finished");

	}

	private static List<Axiom> mapClauses(String name, String MDVPFo,
			String MDVPFhi, String MDVPFlo, String MDVPJitter,
			String MDVPJitterAbs, String MDVPRAP, String MDVPPPQ,
			String JitterDDP, String MDVPShimmer, String MDVPShimmerdB,
			String ShimmerAPQ3, String ShimmerAPQ5, String MDVPAPQ,
			String ShimmerDDA, String NHR, String HNR, String status,
			String RPDE, String DFA, String spread1, String spread2, String D2,
			String PPE) {
		ClassAssertionAxiom cmpAxiom = getConceptAssertion("Patient",
				name);
		axioms.add(cmpAxiom);

		DatatypePropertyAssertion dpa = getDoubleDatatypePropertyAssertion(
				name, "hasMDVPFo", Double.parseDouble(MDVPFo));
		axioms.add(dpa);
		
		DatatypePropertyAssertion dpb = getDoubleDatatypePropertyAssertion(
				name, "hasMDVPFhi", Double.parseDouble(MDVPFhi));
		axioms.add(dpb);
		
		DatatypePropertyAssertion dpc = getDoubleDatatypePropertyAssertion(
				name, "hasMDVPFlo", Double.parseDouble(MDVPFlo));
		axioms.add(dpc);
		
		DatatypePropertyAssertion dpd = getDoubleDatatypePropertyAssertion(
				name, "hasMDVPJitter", Double.parseDouble(MDVPJitter));
		axioms.add(dpd);
		
		DatatypePropertyAssertion dpe = getDoubleDatatypePropertyAssertion(
				name, "hasMDVPJitterAbs", Double.parseDouble(MDVPJitterAbs));
		axioms.add(dpe);
		
		DatatypePropertyAssertion dpf = getDoubleDatatypePropertyAssertion(
				name, "hasMDVPRAP", Double.parseDouble(MDVPRAP));
		axioms.add(dpf);
		
		DatatypePropertyAssertion dpg = getDoubleDatatypePropertyAssertion(
				name, "hasMDVPPPQ", Double.parseDouble(MDVPPPQ));
		axioms.add(dpg);
		
		DatatypePropertyAssertion dph = getDoubleDatatypePropertyAssertion(
				name, "hasJitterDDP", Double.parseDouble(JitterDDP));
		axioms.add(dph);
		
		DatatypePropertyAssertion dpi = getDoubleDatatypePropertyAssertion(
				name, "hasMDVPShimmer", Double.parseDouble(MDVPShimmer));
		axioms.add(dpi);
		
		DatatypePropertyAssertion dpj = getDoubleDatatypePropertyAssertion(
				name, "hasMDVPShimmerdB", Double.parseDouble(MDVPShimmerdB));
		axioms.add(dpj);
		
		DatatypePropertyAssertion dpk = getDoubleDatatypePropertyAssertion(
				name, "hasShimmerAPQ3", Double.parseDouble(ShimmerAPQ3));
		axioms.add(dpk);
		
		DatatypePropertyAssertion dpl = getDoubleDatatypePropertyAssertion(
				name, "hasShimmerAPQ5", Double.parseDouble(ShimmerAPQ5));
		axioms.add(dpl);
		
		DatatypePropertyAssertion dpm = getDoubleDatatypePropertyAssertion(
				name, "hasMDVPAPQ", Double.parseDouble(MDVPAPQ));
		axioms.add(dpm);
		
		DatatypePropertyAssertion dpn = getDoubleDatatypePropertyAssertion(
				name, "hasShimmerDDA", Double.parseDouble(ShimmerDDA));
		axioms.add(dpn);
		
		DatatypePropertyAssertion dpo = getDoubleDatatypePropertyAssertion(
				name, "hasNHR", Double.parseDouble(NHR));
		axioms.add(dpo);
		
		DatatypePropertyAssertion dpp = getDoubleDatatypePropertyAssertion(
				name, "hasHNR", Double.parseDouble(HNR));
		axioms.add(dpp);
		
		DatatypePropertyAssertion dpq = getDoubleDatatypePropertyAssertion(
				name, "hasRPDE", Double.parseDouble(RPDE));
		axioms.add(dpq);
		

		DatatypePropertyAssertion dpr = getDoubleDatatypePropertyAssertion(
				name, "hasDFA", Double.parseDouble(DFA));
		axioms.add(dpr);
		
		DatatypePropertyAssertion dps = getDoubleDatatypePropertyAssertion(
				name, "hasSpread1", Double.parseDouble(spread1));
		axioms.add(dps);
		
		DatatypePropertyAssertion dpt = getDoubleDatatypePropertyAssertion(
				name, "hasSpread2", Double.parseDouble(spread2));
		axioms.add(dpt);
		
		DatatypePropertyAssertion dpu = getDoubleDatatypePropertyAssertion(
				name, "hasD2", Double.parseDouble(D2));
		axioms.add(dpu);
		
		DatatypePropertyAssertion dpv = getDoubleDatatypePropertyAssertion(
				name, "hasPPE", Double.parseDouble(PPE));
		axioms.add(dpv);

		patients.put(name, new Integer(status));
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
		String confHeader = "import(\"parkinsons.owl\");\n\n";
		confHeader += "reasoner = fastInstanceChecker;\n";
		confHeader += "algorithm = refexamples;\n";
		confHeader += "refexamples.noisePercentage = 0;\n";
		confHeader += "refexamples.startClass = " + getURI2("Patient") + ";\n";
		confHeader += "refexamples.writeSearchTree = false;\n";
		confHeader += "refexamples.searchTreeFile = \"log/parkinsons/searchTree.log\";\n";
		confHeader += "\n";
		Files.appendToFile(file, confHeader);
	}

	private static void generateExamples(File file) {
		StringBuffer content = new StringBuffer();
		Set<String> keys = patients.keySet();
		for (String key : keys) {
			Integer subsValue = patients.get(key);
			if (subsValue == 0) {
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

	private static String generateDomainAndRangeForObjectProperties()
			throws ParseException {
		String kbString = "DPDOMAIN(" + getURI2("hasMDVPFo") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMDVPFo") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMDVPFhi") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMDVPFhi") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMDVPFlo") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMDVPFlo") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMDVPJitter") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMDVPJitter") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMDVPJitterAbs") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMDVPJitterAbs") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMDVPRAP") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMDVPRAP") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMDVPPPQ") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMDVPPPQ") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasJitterDDP") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasJitterDDP") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMDVPShimmer") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMDVPShimmer") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMDVPShimmerdB") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMDVPShimmerdB") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasShimmerAPQ3") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasShimmerAPQ3") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasShimmerAPQ5") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasShimmerAPQ5") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMDVPAPQ") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMDVPAPQ") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasShimmerDDA") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasShimmerDDA") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasNHR") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasNHR") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasHNR") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasHNR") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasRPDE") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasRPDE") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasDFA") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasDFA") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasSpread1") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasSpread1") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasSpread2") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasSpread2") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasD2") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasD2") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasPPE") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasPPE") + ") = DOUBLE.\n";
		
		return kbString;

	}
}
