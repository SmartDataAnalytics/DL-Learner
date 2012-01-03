package org.dllearner.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import org.dllearner.parser.PrologParser;
import org.dllearner.prolog.Atom;
import org.dllearner.prolog.Clause;
import org.dllearner.prolog.Program;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.model.IRI;

public class Alzheimer {

	private static IRI ontologyIRI = IRI
			.create("http://dl-learner.org/alzheimer");
	// directory of Prolog files
	private static final String prologDirectory = "test/alzheimer/prolog/";
	private static HashMap<String, Integer> substancesToxic = new HashMap<String, Integer>();
	private static HashMap<String, Integer> substancesScopolamine = new HashMap<String, Integer>();
	private static HashMap<String, Integer> substancesCholine = new HashMap<String, Integer>();
	private static HashMap<String, Integer> substancesAmine = new HashMap<String, Integer>();
	private static Set<String> measures = new TreeSet<String>();

	public static void main(String[] args) throws FileNotFoundException,
			IOException, ParseException {
		String[] files = new String[] { "d_alz.b", "nd_alz.b",
				"amine_uptake/ne.f", "choline/inh.f", "scopolamine/rsd.f",
				"toxic/toxic.f" };

		File owlFile = new File("test/alzheimer/alzheimer.owl");
		setMeasures();
		Program program = null;
		long startTime, duration;
		String time;

		// reading files
		System.out.print("Reading in alzheimer files ... ");
		startTime = System.nanoTime();
		String content = "";
		for (String file : files) {
			content += Files.readFile(new File(prologDirectory + file));
		}
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");

		// parsing files
		System.out.print("Parsing Prolog files ... ");
		startTime = System.nanoTime();
		PrologParser pp = new PrologParser();
		program = pp.parseProgram(content);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");

		// prepare mapping
		KB kb = new KB();
		
		NamedClass atomClass = getAtomicConcept("Measure");
		for (String measure : measures) {
			NamedClass elClass = getAtomicConcept(measure);
			SubClassAxiom sc = new SubClassAxiom(elClass, atomClass);
			kb.addAxiom(sc);
		}
		
		String kbString = generateDomainAndRangeForObjectProperties();		
		KB kb2 = KBParser.parseKBFile(kbString);
		kb.addKB(kb2);
		
		// mapping clauses to axioms
		System.out.print("Mapping clauses to axioms ... ");
		startTime = System.nanoTime();
		ArrayList<Clause> clauses = program.getClauses();
		for (Clause clause : clauses) {
			List<Axiom> axioms = mapClause(clause);
			for (Axiom axiom : axioms) {
				kb.addAxiom(axiom);
			}
		}
		System.out.println("OK (" + time + ").");

		// writing generated knowledge base
		System.out.print("Writing OWL file ... ");
		startTime = System.nanoTime();
		OWLAPIReasoner.exportKBToOWL(owlFile, kb, ontologyIRI);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");

		System.out.print("Generatin first conf file ... ");
		startTime = System.nanoTime();
		File confTrainFile = new File("test/alzheimer/train1.conf");
		Files.clearFile(confTrainFile);
		generateConfFile(confTrainFile);
		generateExamples(substancesToxic, confTrainFile);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");

		// generating second conf file
		System.out.print("Generatin second conf file ... ");
		File confSecondTrainFile = new File("test/alzheimer/train2.conf");
		Files.clearFile(confSecondTrainFile);
		generateConfFile(confSecondTrainFile);
		generateExamples(substancesScopolamine, confSecondTrainFile);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");

		System.out.print("Generatin third conf file ... ");
		startTime = System.nanoTime();
		File confThirdTrainFile = new File("test/alzheimer/train3.conf");
		Files.clearFile(confThirdTrainFile);
		generateConfFile(confThirdTrainFile);
		generateExamples(substancesCholine, confThirdTrainFile);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");

		// generating second conf file
		System.out.print("Generatin fourth conf file ... ");
		File confFourthTrainFile = new File("test/alzheimer/train4.conf");
		Files.clearFile(confFourthTrainFile);
		generateConfFile(confFourthTrainFile);
		generateExamples(substancesAmine, confFourthTrainFile);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		System.out.println("Finished");
	}

	private static List<Axiom> mapClause(Clause clause) throws IOException,
			ParseException {
		List<Axiom> axioms = new LinkedList<Axiom>();
		Atom head = clause.getHead();
		String headName = head.getName();

		if (headName.equals("polar")) {
			
			String compoundName = head.getArgument(0).toPLString();
			compoundName = changeSubstituionNames(compoundName);
			String polatisation = head.getArgument(1).toPLString();
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Substituent",
					compoundName);
			axioms.add(cmpAxiom);

			ObjectPropertyAssertion ra = getRoleAssertion("hasPolarisation",
					compoundName, polatisation);
			axioms.add(ra);
		} else if (headName.equals("size")) {
			String compoundName = head.getArgument(0).toPLString();
			compoundName = changeSubstituionNames(compoundName);
			String size = head.getArgument(1).toPLString();
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Substituent",
					compoundName);
			axioms.add(cmpAxiom);

			ObjectPropertyAssertion ra = getRoleAssertion("hasSize",
					compoundName, size);
			axioms.add(ra);
		} else if (headName.equals("flex")) {
			String compoundName = head.getArgument(0).toPLString();
			compoundName = changeSubstituionNames(compoundName);
			String flex = head.getArgument(1).toPLString();
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Substituent",
					compoundName);
			axioms.add(cmpAxiom);

			ObjectPropertyAssertion ra = getRoleAssertion("hasFlex",
					compoundName, flex);
			axioms.add(ra);
		} else if (headName.equals("h_doner")) {
			String compoundName = head.getArgument(0).toPLString();
			compoundName = changeSubstituionNames(compoundName);
			String hDoner = head.getArgument(1).toPLString();

			ObjectPropertyAssertion ra = getRoleAssertion("isHDonor",
					compoundName, hDoner);
			axioms.add(ra);
		} else if (headName.equals("h_acceptor")) {
			String compoundName = head.getArgument(0).toPLString();
			compoundName = changeSubstituionNames(compoundName);
			String hAcceptor = head.getArgument(1).toPLString();

			ObjectPropertyAssertion ra = getRoleAssertion("isHAcceptor",
					compoundName, hAcceptor);
			axioms.add(ra);
		} else if (headName.equals("pi_doner")) {
			String compoundName = head.getArgument(0).toPLString();
			compoundName = changeSubstituionNames(compoundName);
			String piDoner = head.getArgument(1).toPLString();

			ObjectPropertyAssertion ra = getRoleAssertion("isPiDonor",
					compoundName, piDoner);
			axioms.add(ra);
		} else if (headName.equals("pi_acceptor")) {
			String compoundName = head.getArgument(0).toPLString();
			compoundName = changeSubstituionNames(compoundName);
			String piAcceptor = head.getArgument(1).toPLString();

			ObjectPropertyAssertion ra = getRoleAssertion("isPiAcceptor",
					compoundName, piAcceptor);
			axioms.add(ra);
		} else if (headName.equals("polarisable")) {
			String compoundName = head.getArgument(0).toPLString();
			compoundName = changeSubstituionNames(compoundName);
			String polarisable = head.getArgument(1).toPLString();

			ObjectPropertyAssertion ra = getRoleAssertion("isPolarisable",
					compoundName, polarisable);
			axioms.add(ra);
		} else if (headName.equals("sigma")) {
			String compoundName = head.getArgument(0).toPLString();
			compoundName = changeSubstituionNames(compoundName);
			String sigma = head.getArgument(1).toPLString();

			ObjectPropertyAssertion ra = getRoleAssertion("hasSigma",
					compoundName, sigma);
			axioms.add(ra);
		} else if (headName.equals("gt")) {
			String firstNr = head.getArgument(0).toPLString();
			String secondNr = head.getArgument(1).toPLString();

			ObjectPropertyAssertion ra = getRoleAssertion("isGreater", firstNr,
					secondNr);
			axioms.add(ra);
		} else if (headName.equals("great_polar")) {
			String firstPolar = head.getArgument(0).toPLString();
			String secondPolar = head.getArgument(1).toPLString();
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Polar",
					firstPolar);
			axioms.add(cmpAxiom);
			
			ClassAssertionAxiom cmpSecondAxiom = getConceptAssertion("Polar",
					secondPolar);
			axioms.add(cmpSecondAxiom);

			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterPolar",
					firstPolar, secondPolar);
			axioms.add(ra);
		} else if (headName.equals("great_size")) {
			String firstSize = head.getArgument(0).toPLString();
			String secondSize = head.getArgument(1).toPLString();
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Size",
					firstSize);
			axioms.add(cmpAxiom);
			
			ClassAssertionAxiom cmpSecondAxiom = getConceptAssertion("Size",
					secondSize);
			axioms.add(cmpSecondAxiom);

			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterSize",
					firstSize, secondSize);
			axioms.add(ra);
		} else if (headName.equals("great_flex")) {
			String firstFlex = head.getArgument(0).toPLString();
			String secondFlex = head.getArgument(1).toPLString();
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Flex",
					firstFlex);
			axioms.add(cmpAxiom);
			
			ClassAssertionAxiom cmpSecondAxiom = getConceptAssertion("Flex",
					secondFlex);
			axioms.add(cmpSecondAxiom);

			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterFlex",
					firstFlex, secondFlex);
			axioms.add(ra);
		} else if (headName.equals("great_h_don")) {
			String firstHDonor = head.getArgument(0).toPLString();
			String secondHDonor = head.getArgument(1).toPLString();
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("HDonor",
					firstHDonor);
			axioms.add(cmpAxiom);
			
			ClassAssertionAxiom cmpSecondAxiom = getConceptAssertion("HDonor",
					secondHDonor);
			axioms.add(cmpSecondAxiom);

			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterHDonor",
					firstHDonor, secondHDonor);
			axioms.add(ra);
		} else if (headName.equals("great_h_acc")) {
			String firstHAcc = head.getArgument(0).toPLString();
			String secondHAcc = head.getArgument(1).toPLString();

			ClassAssertionAxiom cmpAxiom = getConceptAssertion("HAcceptor",
					firstHAcc);
			axioms.add(cmpAxiom);
			
			ClassAssertionAxiom cmpSecondAxiom = getConceptAssertion("HAcceptor",
					secondHAcc);
			axioms.add(cmpSecondAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterHAcceptor",
					firstHAcc, secondHAcc);
			axioms.add(ra);
		} else if (headName.equals("great_pi_don")) {
			String firstPiDonor = head.getArgument(0).toPLString();
			String secondPiDonor = head.getArgument(1).toPLString();

			ClassAssertionAxiom cmpAxiom = getConceptAssertion("PiDonor",
					firstPiDonor);
			axioms.add(cmpAxiom);
			
			ClassAssertionAxiom cmpSecondAxiom = getConceptAssertion("PiDonor",
					secondPiDonor);
			axioms.add(cmpSecondAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterPiDonor",
					firstPiDonor, secondPiDonor);
			axioms.add(ra);
		} else if (headName.equals("great_pi_acc")) {
			String firstPiAcc = head.getArgument(0).toPLString();
			String secondPiAcc = head.getArgument(1).toPLString();

			ClassAssertionAxiom cmpAxiom = getConceptAssertion("PiAcceptor",
					firstPiAcc);
			axioms.add(cmpAxiom);
			
			ClassAssertionAxiom cmpSecondAxiom = getConceptAssertion("PiAcceptor",
					secondPiAcc);
			axioms.add(cmpSecondAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion(
					"isGreaterPiAcceptor", firstPiAcc, secondPiAcc);
			axioms.add(ra);
		} else if (headName.equals("great_polari")) {
			String firstPolar = head.getArgument(0).toPLString();
			String secondPolar = head.getArgument(1).toPLString();

			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterPolar",
					firstPolar, secondPolar);
			axioms.add(ra);
		} else if (headName.equals("great_sigma")) {
			String firstSigma = head.getArgument(0).toPLString();
			String secondSigma = head.getArgument(1).toPLString();

			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Sigma",
					firstSigma);
			axioms.add(cmpAxiom);
			
			ClassAssertionAxiom cmpSecondAxiom = getConceptAssertion("Sigma",
					secondSigma);
			axioms.add(cmpSecondAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterSigma",
					firstSigma, secondSigma);
			axioms.add(ra);
		} else if (headName.equals("x_subst")) {
			String drugName = head.getArgument(0).toPLString();
			String positionrOfSubs = head.getArgument(1).toPLString();
			String substituent = head.getArgument(2).toPLString();
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Position",
					positionrOfSubs);
			axioms.add(cmpAxiom);
			
			drugName = changeSubstituionNames(drugName);
			substituent = changeSubstituionNames(substituent);
			
			ObjectPropertyAssertion ra = getRoleAssertion(
					"getsReplacedAtPosition", drugName, positionrOfSubs);
			axioms.add(ra);

			ObjectPropertyAssertion sub = getRoleAssertion("getsReplacedAtPosition"+positionrOfSubs+"By",
					drugName, substituent);
			axioms.add(sub);
		} else if (headName.equals("alk_groups")) {
			String drugName = head.getArgument(0).toPLString();
			String nrOfSubs = head.getArgument(1).toPLString();
			
			drugName = changeSubstituionNames(drugName);
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Drug",
					drugName);
			axioms.add(cmpAxiom);
			
			double subs = Double
			.parseDouble(nrOfSubs);
			
			DatatypePropertyAssertion dpa = getDoubleDatatypePropertyAssertion(
					drugName, "hasNrOfAlkylSubstitutions", subs);
			axioms.add(dpa);

		} else if (headName.equals("r_subst_1")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			
			drugName = changeSubstituionNames(drugName);
			substituent = changeSubstituionNames(substituent);
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Substituent",
					substituent);
			axioms.add(cmpAxiom);
			
			ClassAssertionAxiom cpAxiom = getConceptAssertion("Position",
			"1");
			axioms.add(cpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("getsReplacedAtPositionOneBy",
					drugName, substituent);
			axioms.add(ra);
			// z.B. r_subst_l (n 1, single_alk(2)). R substitution beginnt mit 2
			// methyl gruppen
		} else if (headName.equals("n_val")) {

		} else if (headName.equals("r_subst_2")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			
			drugName = changeSubstituionNames(drugName);
			substituent = changeSubstituionNames(substituent);
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Substituent",
					substituent);
			axioms.add(cmpAxiom);
			
			ClassAssertionAxiom cpAxiom = getConceptAssertion("Position",
			"2");
			axioms.add(cpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("getsReplacedAtPositionTwoBy",
					drugName, substituent);
			axioms.add(ra);
			// z.B. r_subst_2(n 1, double_alk(1)). eine ethygruppe
		} else if (headName.equals("r_subst_3")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			
			drugName = changeSubstituionNames(drugName);
			substituent = changeSubstituionNames(substituent);
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Substituent",
					substituent);
			axioms.add(cmpAxiom);
			
			ClassAssertionAxiom cpAxiom = getConceptAssertion("Position",
			"3");
			axioms.add(cpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("getsReplacedAtPositionThreeBy",
					drugName, substituent);
			axioms.add(ra);
			// z.B. r_subst_3(nl, 3, aro(2)). the final alkyl group in drug nl
			// has two substitutions
			// drug n l has two aromatic rings;
		} else if (headName.equals("ring_substitutions")) {
			String drugName = head.getArgument(0).toPLString();
			String nrOfSubs = head.getArgument(1).toPLString();
			
			double subs = Double
			.parseDouble(nrOfSubs);
			
			DatatypePropertyAssertion dpa = getDoubleDatatypePropertyAssertion(
					drugName, "nrOfSubstitutionsInRing", subs);
			axioms.add(dpa);
		} else if (headName.equals("ring_subst_4")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			
			drugName = changeSubstituionNames(drugName);
			substituent = changeSubstituionNames(substituent);
			
			ClassAssertionAxiom compAxiom = getConceptAssertion("Substituent",
					substituent);
			axioms.add(compAxiom);
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Drug", drugName);
			axioms.add(cmpAxiom);

			ClassAssertionAxiom cpAxiom = getConceptAssertion("Position",
					"4");
			axioms.add(cpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion(
					"getsRingReplacedAtPosition", drugName, "4");
			axioms.add(ra);

			ObjectPropertyAssertion is = getRoleAssertion("getsRingReplacedAtPositionFourBy",
					drugName, substituent);
			axioms.add(is);
		} else if (headName.equals("ring_subst_3")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			
			drugName = changeSubstituionNames(drugName);
			substituent = changeSubstituionNames(substituent);
			
			ClassAssertionAxiom compAxiom = getConceptAssertion("Substituent",
					substituent);
			axioms.add(compAxiom);
			
			ClassAssertionAxiom cpAxiom = getConceptAssertion("Position",
			"3");
			axioms.add(cpAxiom);
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Drug", drugName);
			axioms.add(cmpAxiom);

			ObjectPropertyAssertion ra = getRoleAssertion(
					"getsRingReplacedAtPosition", drugName, "3");
			axioms.add(ra);

			ObjectPropertyAssertion is = getRoleAssertion("getsRingReplacedAtPositionThreeBy",
					drugName, substituent);
			axioms.add(is);
		} else if (headName.equals("ring_subst_2")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			
			drugName = changeSubstituionNames(drugName);
			substituent = changeSubstituionNames(substituent);
			
			
			ClassAssertionAxiom cpAxiom = getConceptAssertion("Position",
			"2");
			axioms.add(cpAxiom);
			
			ClassAssertionAxiom compAxiom = getConceptAssertion("Substituent",
					substituent);
			axioms.add(compAxiom);
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Drug", drugName);
			axioms.add(cmpAxiom);

			ObjectPropertyAssertion ra = getRoleAssertion(
					"getsRingReplacedAtPosition", drugName, "2");
			axioms.add(ra);

			ObjectPropertyAssertion is = getRoleAssertion("getsRingReplacedAtPositionTwoBy",
					drugName, substituent);
			axioms.add(is);
		} else if (headName.equals("ring_subst_5")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			
			drugName = changeSubstituionNames(drugName);
			substituent = changeSubstituionNames(substituent);
			
			ClassAssertionAxiom compAxiom = getConceptAssertion("Substituent",
					substituent);
			axioms.add(compAxiom);
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Drug", drugName);
			axioms.add(cmpAxiom);

			ClassAssertionAxiom cpAxiom = getConceptAssertion("Position",
			"5");
			axioms.add(cpAxiom);
	
			ObjectPropertyAssertion ra = getRoleAssertion(
					"getsRingReplacedAtPosition", drugName, "5");
			axioms.add(ra);

			ObjectPropertyAssertion is = getRoleAssertion("getsRingReplacedAtPositionFifeBy",
					drugName, substituent);
			axioms.add(is);
		} else if (headName.equals("ring_subst_6")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			
			drugName = changeSubstituionNames(drugName);
			substituent = changeSubstituionNames(substituent);
			
			ClassAssertionAxiom compAxiom = getConceptAssertion("Substituent",
					substituent);
			axioms.add(compAxiom);
			
			ClassAssertionAxiom cpAxiom = getConceptAssertion("Position",
			"6");
			axioms.add(cpAxiom);
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Drug", drugName);
			axioms.add(cmpAxiom);

			ObjectPropertyAssertion ra = getRoleAssertion(
					"getsRingReplacedAtPosition", drugName, "6");
			axioms.add(ra);

			ObjectPropertyAssertion is = getRoleAssertion("getsRingReplacedAtPositionSixBy",
					drugName, substituent);
			axioms.add(is);
		} else if (headName.equals("r_subst")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(2).toPLString();
			
			drugName = changeSubstituionNames(drugName);
			substituent = changeSubstituionNames(substituent);
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Substituent",
					substituent);
			axioms.add(cmpAxiom);
			
			ObjectPropertyAssertion da = getRoleAssertion(
					"getsReplacedBy", drugName, substituent);
			axioms.add(da);
			
			

		} else if (headName.equals("ring_struc")) {
			String drugName = head.getArgument(0).toPLString();
			String ringStruct = head.getArgument(1).toPLString();
			
			drugName = changeSubstituionNames(drugName);
			ringStruct = changeSubstituionNames(ringStruct);
			
			if(ringStruct.startsWith("subs")) {
				String substituent = replaceSubsString(ringStruct);
				
				String subsPosition = (substituent.substring(substituent.indexOf(" ") + 1));
				String replacement = substituent.substring(0, substituent.indexOf(" "));
				
				ObjectPropertyAssertion da = getRoleAssertion(
						"getsRingReplacementAt", drugName, subsPosition);
				axioms.add(da);
				
				ObjectPropertyAssertion ma = getRoleAssertion(
						"getsReplacedWith", drugName, replacement);
				axioms.add(ma);
			} else {
				ObjectPropertyAssertion ra = getRoleAssertion(
						"hasRingStructure", drugName, ringStruct);
				axioms.add(ra);
			}
			
		} else if (headName.equals("great_rsd")) {
			String subs1 = head.getArgument(0).toPLString();
			String subs2 = head.getArgument(1).toPLString();
			if(!substancesScopolamine.containsKey(subs1)) {
				substancesScopolamine.put(subs1, new Integer(1));
			} else {
				Integer subsValue = substancesScopolamine.get(subs1);
				substancesScopolamine.put(subs1, subsValue+1);
			}
			
			if(!substancesScopolamine.containsKey(subs2)) {
				substancesScopolamine.put(subs2, new Integer(-1));
			} else {
				Integer subsValue = substancesScopolamine.get(subs2);
				substancesScopolamine.put(subs2, subsValue-1);
			}

		} else if (headName.equals("less_toxic")) {
			String subs1 = head.getArgument(0).toPLString();
			String subs2 = head.getArgument(1).toPLString();
			
			if(!substancesToxic.containsKey(subs1)) {
				substancesToxic.put(subs1, new Integer(1));
			} else {
				Integer subsValue = substancesToxic.get(subs1);
				substancesToxic.put(subs1, subsValue+1);
			}
			
			if(!substancesToxic.containsKey(subs2)) {
				substancesToxic.put(subs2, new Integer(-1));
			} else {
				Integer subsValue = substancesToxic.get(subs2);
				substancesToxic.put(subs2, subsValue-1);
			}
		} else if (headName.equals("great")) {
			String subs1 = head.getArgument(0).toPLString();
			String subs2 = head.getArgument(1).toPLString();

			if(!substancesCholine.containsKey(subs1)) {
				substancesCholine.put(subs1, new Integer(1));
			} else {
				Integer subsValue = substancesCholine.get(subs1);
				substancesCholine.put(subs1, subsValue+1);
			}
			
			if(!substancesCholine.containsKey(subs2)) {
				substancesCholine.put(subs2, new Integer(-1));
			} else {
				Integer subsValue = substancesCholine.get(subs2);
				substancesCholine.put(subs2, subsValue-1);
			}
		} else if (headName.equals("great_ne")) {
			String subs1 = head.getArgument(0).toPLString();
			String subs2 = head.getArgument(1).toPLString();

			if(!substancesAmine.containsKey(subs1)) {
				substancesAmine.put(subs1, new Integer(1));
			} else {
				Integer subsValue = substancesAmine.get(subs1);
				substancesAmine.put(subs1, subsValue+1);
			}
			
			if(!substancesAmine.containsKey(subs2)) {
				substancesAmine.put(subs2, new Integer(-1));
			} else {
				Integer subsValue = substancesAmine.get(subs2);
				substancesAmine.put(subs2, subsValue-1);
			}
		} else {
			System.out.println("clause not supportet: " + headName);
		}
		return axioms;
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

	private static NamedClass getAtomicConcept(String name) {
		return new NamedClass(ontologyIRI + "#" + name);
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
	
	private static void generateExamples(HashMap<String,Integer> examples, File file) {
		StringBuffer content = new StringBuffer();
		Set<String> keys = examples.keySet();
		for(String key: keys) {
			Integer subsValue = examples.get(key);
			if(subsValue > 0) {
				content.append("+\"" + getIndividual(key) + "\"\n");
			} else {
				content.append("-\"" + getIndividual(key) + "\"\n");
			}
		}
		Files.appendFile(file, content.toString());
	}

	private static void generateConfFile(File file) {
		String confHeader = "import(\"alzheimer.owl\");\n\n";
		confHeader += "reasoner = fastInstanceChecker;\n";
		confHeader += "algorithm = refexamples;\n";
		confHeader += "refexamples.noisePercentage = 16;\n";
		confHeader += "refexamples.startClass = " + getURI2("Drug") + ";\n";
		confHeader += "refexamples.writeSearchTree = false;\n";
		confHeader += "refexamples.searchTreeFile = \"log/alzheimer/searchTree.log\";\n";
		confHeader += "\n";
		Files.appendFile(file, confHeader);
	}

	// returns URI including quotationsmark (need for KBparser)
	private static String getURI2(String name) {
		return "\"" + getURI(name) + "\"";
	}

	private static String getURI(String name) {
		return ontologyIRI + "#" + name;
	}
	
	private static String changeSubstituionNames(String substituent) {
		String subs = substituent;
		
		if(subs.startsWith("single_alk")) {
			if(subs.contains("1")) {
				subs = "CH3";
			} else if (subs.contains("2")) {
				subs = "(CH3)2";
			} else if (subs.contains("3")) {
				subs = "(CH3)3";
			}
		} else if(subs.startsWith("double_alk")) {
			if(subs.contains("1")) {
				subs = "CH2-CH3";
			} else if (subs.contains("2")) {
				subs = "(CH2-CH3)2";
			} else if (subs.contains("3")) {
				subs = "(CH2-CH3)3";
			}
			
		} else if(subs.startsWith("aro")) {
			if(subs.contains("1")) {
				subs = "Aromatic-Ring";
			} else if (subs.contains("2")) {
				subs = "(Aromatic-Ring)2";
			} else if (subs.contains("3")) {
				subs = "(Aromatic-Ring)3";
			}
			
		} else if (subs.startsWith("bond(n,group(ch3,2)") || subs.startsWith("bond(n, group(ch3, 2))")) {
			subs = "N(CH3)2";
		}
		
		return subs;
	}
	
	private static String replaceSubsString(String subs) {
		subs = subs.replace(" ", "");
		String substi = subs.substring(subs.indexOf("(")+1, subs.indexOf(","));
		String ringPosition = subs.substring(subs.indexOf(",") + 1, subs.indexOf(")"));
		subs = substi + " " + ringPosition;
		return subs;
	}
	
	/**
	 * This method contains all subclasses of measure
	 */
	private static void setMeasures() {
		measures.add("Polar");
		measures.add("Size");
		measures.add("Sigma");
		measures.add("PiDonor");
		measures.add("PiAcceptor");
		measures.add("HDonor");
		measures.add("HAcceptor");
		measures.add("Flex");
		measures.add("Position");
	}
	
	/**
	 * In this method the domain and range for all object properties is generated.
	 * @return domain and range for object properties
	 */
	private static String generateDomainAndRangeForObjectProperties() {
		// define properties including domain and range
		String kbString = "OPDOMAIN(" + getURI2("hasFlex") + ") = "
				+ getURI2("Substituent") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasFlex") + ") = " + getURI2("Flex")
				+ ".\n";
				
		kbString += "OPDOMAIN(" + getURI2("hasSize") + ") = "
				+ getURI2("Substituent") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasSize") + ") = " + getURI2("Size")
				+ ".\n";
				
		kbString += "OPDOMAIN(" + getURI2("hasSigma") + ") = "
		+ getURI2("Substituent") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasSigma") + ") = " + getURI2("Sigma")
		+ ".\n";

		kbString += "OPDOMAIN(" + getURI2("hasPolarisation") + ") = "
		+ getURI2("Substituent") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasPolarisation") + ") = " + getURI2("Polar")
		+ ".\n";

		kbString += "OPDOMAIN(" + getURI2("isPiDonor") + ") = "
		+ getURI2("Substituent") + ".\n";
		kbString += "OPRANGE(" + getURI2("isPiDonor") + ") = " + getURI2("PiDonor")
		+ ".\n";

		kbString += "OPDOMAIN(" + getURI2("isPiAcceptor") + ") = "
		+ getURI2("Substituent") + ".\n";
		kbString += "OPRANGE(" + getURI2("isPiAcceptor") + ") = " + getURI2("PiAcceptor")
		+ ".\n";

		kbString += "OPDOMAIN(" + getURI2("isHDonor") + ") = "
		+ getURI2("Substituent") + ".\n";
		kbString += "OPRANGE(" + getURI2("isHDonor") + ") = " + getURI2("HDonor")
		+ ".\n";

		kbString += "OPDOMAIN(" + getURI2("isHAcceptor") + ") = "
		+ getURI2("Substituent") + ".\n";
		kbString += "OPRANGE(" + getURI2("isHAcceptor") + ") = " + getURI2("HAcceptor")
		+ ".\n";

		kbString += "OPDOMAIN(" + getURI2("isPolarisable") + ") = "
		+ getURI2("Substituent") + ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsReplacedBy") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsReplacedBy") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsNrOfReplacementsInMiddleRing") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsNrOfReplacementsInMiddleRing") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsReplacedAtPosition") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsReplacedAtPosition") + ") = " + getURI2("Position")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsReplacedByFirst") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsReplacedByFirst") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsReplacedBySecond") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsReplacedBySecond") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsReplacedByThird") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsReplacedByThird") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsRingReplacedAtPosition") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsRingReplacedAtPosition") + ") = " + getURI2("Position")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsRingReplacementAt") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsRingReplacementAt") + ") = " + getURI2("Position")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsReplacedWith") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsReplacedWith") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("hasRingStructure") + ") = "
		+ getURI2("Drug") + ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsReplacedAtPosition6By") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsReplacedAtPosition6By") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsReplacedAtPosition7By") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsReplacedAtPosition7By") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsReplacedAtPositionOneBy") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsReplacedAtPositionOneBy") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsReplacedAtPositionTwoBy") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsReplacedAtPositionTwoBy") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsReplacedAtPositionThreeBy") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsReplacedAtPositionThreeBy") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsRingReplacedAtPositionTwoBy") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsRingReplacedAtPositionTwoBy") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsRingReplacedAtPositionThreeBy") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsRingReplacedAtPositionThreeBy") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsRingReplacedAtPositionFourBy") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsRingReplacedAtPositionFourBy") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsRingReplacedAtPositionFifeBy") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsRingReplacedAtPositionFifeBy") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("getsRingReplacedAtPositionSixBy") + ") = "
		+ getURI2("Drug") + ".\n";
		kbString += "OPRANGE(" + getURI2("getsRingReplacedAtPositionSixBy") + ") = " + getURI2("Substituent")
		+ ".\n";
		
		return kbString;
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
	
}
