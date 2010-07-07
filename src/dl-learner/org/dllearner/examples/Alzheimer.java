package org.dllearner.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
	private static final String prologDirectory = "examples/alzheimer/prolog/";

	public static void main(String[] args) throws FileNotFoundException,
			IOException, ParseException {
		String[] files = new String[] { "d_alz.b", "nd_alz.b" };

		File owlFile = new File("examples/alzheimer/alzheimer.owl");

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
	}

	private static List<Axiom> mapClause(Clause clause) throws IOException,
			ParseException {
		List<Axiom> axioms = new LinkedList<Axiom>();
		Atom head = clause.getHead();
		String headName = head.getName();

		if (headName.equals("polar")) {
			String compoundName = head.getArgument(0).toPLString();
			compoundName = compoundName.replace(" ", "");
			String polatisation = head.getArgument(1).toPLString();
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Substituent",
					compoundName);
			axioms.add(cmpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("hasPolatisation",
					compoundName, polatisation);
			axioms.add(ra);
		} else if (headName.equals("size")) {
			String compoundName = head.getArgument(0).toPLString();
			compoundName = compoundName.replace(" ", "");
			String size = head.getArgument(1).toPLString();
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Substituent",
					compoundName);
			axioms.add(cmpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("hasSize",
					compoundName, size);
			axioms.add(ra);
		} else if (headName.equals("flex")) {
			String compoundName = head.getArgument(0).toPLString();
			compoundName = compoundName.replace(" ", "");
			String flex = head.getArgument(1).toPLString();
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Substituent",
					compoundName);
			axioms.add(cmpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("hasFlex",
					compoundName, flex);
			axioms.add(ra);
		} else if (headName.equals("h_doner")) {
				String compoundName = head.getArgument(0).toPLString();
				compoundName = compoundName.replace(" ", "");
				String hDoner = head.getArgument(1).toPLString();
				
				ObjectPropertyAssertion ra = getRoleAssertion("isHDoner",
						compoundName, hDoner);
				axioms.add(ra);
		} else if (headName.equals("h_acceptor")) {
				String compoundName = head.getArgument(0).toPLString();
				compoundName = compoundName.replace(" ", "");
				String hAcceptor = head.getArgument(1).toPLString();
				
				ObjectPropertyAssertion ra = getRoleAssertion("isHAcceptor",
						compoundName, hAcceptor);
				axioms.add(ra);
		} else if (headName.equals("pi_doner")) {
				String compoundName = head.getArgument(0).toPLString();
				compoundName = compoundName.replace(" ", "");
				String piDoner = head.getArgument(1).toPLString();
				
				ObjectPropertyAssertion ra = getRoleAssertion("isPiDoner",
						compoundName, piDoner);
				axioms.add(ra);
		} else if (headName.equals("pi_acceptor")) {
				String compoundName = head.getArgument(0).toPLString();
				compoundName = compoundName.replace(" ", "");
				String piAcceptor = head.getArgument(1).toPLString();
				
				ObjectPropertyAssertion ra = getRoleAssertion("isPiAcceptor",
						compoundName, piAcceptor);
				axioms.add(ra);
		} else if (headName.equals("polarisable")) {
				String compoundName = head.getArgument(0).toPLString();
				compoundName = compoundName.replace(" ", "");
				String polarisable = head.getArgument(1).toPLString();
				
				ObjectPropertyAssertion ra = getRoleAssertion("isPolarisable",
						compoundName, polarisable);
				axioms.add(ra);
		} else if (headName.equals("sigma")) {
				String compoundName = head.getArgument(0).toPLString();
				compoundName = compoundName.replace(" ", "");
				String sigma = head.getArgument(1).toPLString();
				
				ObjectPropertyAssertion ra = getRoleAssertion("hasSigma",
						compoundName, sigma);
				axioms.add(ra);
		} else if (headName.equals("gt")) {
			String firstNr = head.getArgument(0).toPLString();
			String secondNr = head.getArgument(1).toPLString();
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreater",
					firstNr, secondNr);
			axioms.add(ra);
		} else if (headName.equals("great_polar")) {
			String firstPolar = head.getArgument(0).toPLString();
			String secondPolar = head.getArgument(1).toPLString();
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterPolar",
					firstPolar, secondPolar);
			axioms.add(ra);
		} else if (headName.equals("great_size")) {
			String firstSize = head.getArgument(0).toPLString();
			String secondSize = head.getArgument(1).toPLString();
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterSize",
					firstSize, secondSize);
			axioms.add(ra);
		} else if (headName.equals("great_flex")) {
			String firstFlex = head.getArgument(0).toPLString();
			String secondFlex = head.getArgument(1).toPLString();
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterFlex",
					firstFlex, secondFlex);
			axioms.add(ra);
		} else if (headName.equals("great_h_don")) {
			String firstHDonor = head.getArgument(0).toPLString();
			String secondHDonor = head.getArgument(1).toPLString();
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterHDonor",
					firstHDonor, secondHDonor);
			axioms.add(ra);
		} else if (headName.equals("great_h_acc")) {
			String firstHAcc = head.getArgument(0).toPLString();
			String secondHAcc = head.getArgument(1).toPLString();
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterHAcceptor",
					firstHAcc, secondHAcc);
			axioms.add(ra);
		} else if (headName.equals("great_pi_don")) {
			String firstPiDonor = head.getArgument(0).toPLString();
			String secondPiDonor = head.getArgument(1).toPLString();
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterPiDonor",
					firstPiDonor, secondPiDonor);
			axioms.add(ra);
		} else if (headName.equals("great_pi_acc")) {
			String firstPiAcc = head.getArgument(0).toPLString();
			String secondPiAcc = head.getArgument(1).toPLString();
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterPiAcceptor",
					firstPiAcc, secondPiAcc);
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
			
			ObjectPropertyAssertion ra = getRoleAssertion("isGreaterSigma",
					firstSigma, secondSigma);
			axioms.add(ra);
		} else if (headName.equals("x_subst")) {
			String drugName = head.getArgument(0).toPLString();
			String positionrOfSubs = head.getArgument(1).toPLString();
			String substituent = head.getArgument(2).toPLString();
			
			ObjectPropertyAssertion ra = getRoleAssertion("hasPositionOfSubstituent",
					drugName, positionrOfSubs);
			axioms.add(ra);
			
			ObjectPropertyAssertion sub = getRoleAssertion("getsSubstituent",
					drugName, substituent);
			axioms.add(sub);
		} else if (headName.equals("alk_groups")) {
			String drugName = head.getArgument(0).toPLString();
			String nrOfSubs = head.getArgument(1).toPLString();
			
			ObjectPropertyAssertion ra = getRoleAssertion("hasNrOfAlkylSubstitutions",
					drugName, nrOfSubs);
			axioms.add(ra);
			
		} else if (headName.equals("r_subst_1")) {
			//complex r substitution
			//z.B. r_subst_l (n 1, single_alk(2)). R substitution beginnt mit 2 methyl gruppen
		} else if (headName.equals("n_val")) {

		} else if (headName.equals("r_subst_2")) {
			//complex r substitution
			//z.B. r_subst_2(n 1, double_alk(1)). eine ethygruppe
		} else if (headName.equals("r_subst_3")) {
			//complex r substitution
			//z.B. r_subst_3(nl, 3, aro(2)). the final alkyl group in drug nl has two substitutions
			//drug n l has two aromatic rings;
		} else if (headName.equals("ring_substitutions")) {
			String drugName = head.getArgument(0).toPLString();
			String nrOfSubs = head.getArgument(1).toPLString();
			
			double nrOfSubstituitons = Double
			.parseDouble(nrOfSubs);
			DatatypePropertyAssertion dpa = getDoubleDatatypePropertyAssertion(
					drugName, "nrOfSubstitutionsInRing", nrOfSubstituitons);
			axioms.add(dpa);
		} else if (headName.equals("ring_subst_4")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Drug",
					drugName);
			axioms.add(cmpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("getsRingNrSubstitute",
					drugName, "4");
			axioms.add(ra);
			
			ObjectPropertyAssertion is = getRoleAssertion("getsSubstitute",
					drugName, substituent);
			axioms.add(is);
		} else if (headName.equals("ring_subst_3")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Drug",
					drugName);
			axioms.add(cmpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("getsRingNrSubstitute",
					drugName, "3");
			axioms.add(ra);
			
			ObjectPropertyAssertion is = getRoleAssertion("getsSubstitute",
					drugName, substituent);
			axioms.add(is);
		} else if (headName.equals("ring_subst_2")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Drug",
					drugName);
			axioms.add(cmpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("getsRingNrSubstitute",
					drugName, "2");
			axioms.add(ra);
			
			ObjectPropertyAssertion is = getRoleAssertion("getsSubstitute",
					drugName, substituent);
			axioms.add(is);
		} else if (headName.equals("ring_subst_5")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Drug",
					drugName);
			axioms.add(cmpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("getsRingNrSubstitute",
					drugName, "5");
			axioms.add(ra);
			
			ObjectPropertyAssertion is = getRoleAssertion("getsSubstitute",
					drugName, substituent);
			axioms.add(is);
		} else if (headName.equals("ring_subst_6")) {
			String drugName = head.getArgument(0).toPLString();
			String substituent = head.getArgument(1).toPLString();
			ClassAssertionAxiom cmpAxiom = getConceptAssertion("Drug",
					drugName);
			axioms.add(cmpAxiom);
			
			ObjectPropertyAssertion ra = getRoleAssertion("getsRingNrSubstitute",
					drugName, "6");
			axioms.add(ra);
			
			ObjectPropertyAssertion is = getRoleAssertion("getsSubstitute",
					drugName, substituent);
			axioms.add(is);
		} else if (headName.equals("r_subst")) {

		} else if (headName.equals("ring_struc")) {

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
