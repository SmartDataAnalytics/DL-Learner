/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyAssertion;
import org.dllearner.core.owl.DoubleDatatypePropertyAssertion;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.parser.ParseException;
import org.dllearner.parser.PrologParser;
import org.dllearner.prolog.Atom;
import org.dllearner.prolog.Clause;
import org.dllearner.prolog.Program;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;

/**
 * This class maps the carcinogenesis Prolog files to an OWL file. In a first
 * step, a Prolog parser is used to read all files. The main step involves
 * applying mapping Prolog clauses to OWL axioms through domain specific mapping
 * rules.
 * 
 * The carcinogenesis Prolog files are available here:
 * http://web.comlab.ox.ac.uk/oucl/research/areas/machlearn/cancer.html
 * 
 * .f files contain positive and .n files contain negative examples. pte1.n and
 * pte.f contain the PTE-1 challenge examples. train.n and train.f contain other
 * examples which can be used to train for PTE-1.
 * 
 * The PTE-2 directory contains PTE-2 files, i.e. all substances referred to in
 * those files are only those of the PTE-2 challenge.
 * 
 * @author Jens Lehmann
 * 
 */
public class Carcinogenesis {

	private static URI ontologyURI = URI.create("http://dl-learner.org/carcinogenesis");

	// directory of Prolog files
	private static String prologDirectory = "examples/carcinogenesis/prolog/";	
	
	// mapping of symbols to names of chemical elements
	private static Map<String, String> chemElements;

	// types of atoms and bonds
	private static Set<String> atomTypes = new TreeSet<String>();
	private static Set<String> bondTypes = new TreeSet<String>();

	// we need a counter for bonds, because they are instances in OWL
	// but not in Prolog
	private static int bondNr = 0;

	/**
	 * @param args
	 *            No arguments supported.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException,
			ParseException {

		String[] files = new String[] { "ames.pl", "atoms.pl", "bonds.pl", "gentoxprops.pl",
				"ind_nos.pl", "ind_pos.pl", "newgroups.pl",
		// "train.b" => not a pure Prolog file but Progol/Aleph specific
		};
		File owlFile = new File("examples/carcinogenesis/pte.owl");

		Program program = null;
		long startTime, duration;
		String time;

		// reading files
		System.out.print("Reading in carcinogenesis Prolog files ... ");
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
		createChemElementsMapping();
		// create subclasses of atom
		NamedClass atomClass = getAtomicConcept("Atom");
		for (String element : chemElements.values()) {
			NamedClass elClass = getAtomicConcept(element);
			SubClassAxiom sc = new SubClassAxiom(elClass, atomClass);
			kb.addAxiom(sc);
		}
		// define properties including domain and range
		// ... TODO ...

		// mapping clauses to axioms
		System.out.print("Mapping clauses to axioms ... ");
		startTime = System.nanoTime();
		ArrayList<Clause> clauses = program.getClauses();
		for (Clause clause : clauses) {
			List<Axiom> axioms = mapClause(clause);
			for (Axiom axiom : axioms)
				kb.addAxiom(axiom);
		}
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");

		// writing generated knowledge base
		System.out.print("Writing OWL file ... ");
		startTime = System.nanoTime();
		OWLAPIReasoner.exportKBToOWL(owlFile, kb, ontologyURI);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");

		// generating conf files
		File confTrainFile = new File("examples/carcinogenesis/train.conf");		
		String confHeader = "";
		Files.appendFile(confTrainFile, confHeader);

		// generating training examples
		File trainingFilePositives = new File(prologDirectory + "train.f");
		File trainingFileNegatives = new File(prologDirectory + "train.n");

		List<Individual> posTrainExamples = getExamples(trainingFilePositives);
		List<Individual> negTrainExamples = getExamples(trainingFileNegatives);
		appendPosExamples(confTrainFile, posTrainExamples);
		appendNegExamples(confTrainFile, negTrainExamples);
		
		// generating testExamples
//		File confTestFile = new File("examples/carcinogenesis/test.conf");
//		File testFilePositives = new File(prologDirectory + "train.f");
//		File testFileNegatives = new File(prologDirectory + "train.n");
//		
	}

	private static List<Axiom> mapClause(Clause clause) {
		List<Axiom> axioms = new LinkedList<Axiom>();
		Atom head = clause.getHead();
		String headName = head.getName();
		// Body body = clause.getBody();
		// ArrayList<Literal> literals = body.getLiterals();
		// handle: atm(compound,atom,element,atomtype,charge)
		if (headName.equals("atm")) {

			String compoundName = head.getArgument(0).toPLString();
			String atomName = head.getArgument(1).toPLString();
			String elementName = head.getArgument(2).toPLString();
			String type = head.getArgument(3).toPLString();
			double charge = Double.parseDouble(head.getArgument(4).toPLString());
			// relate compound and atom
			ObjectPropertyAssertion ra = getRoleAssertion("hasAtom", compoundName, atomName);
			axioms.add(ra);
			// atom is made instance of the correct class
			String atomClass = getAtomClass(elementName, type);
			ClassAssertionAxiom ca = getConceptAssertion(atomClass, atomName);
			axioms.add(ca);
			// write subclass axiom if doesn't exist already
			if (!atomTypes.contains(atomClass)) {
				NamedClass subClass = getAtomicConcept(atomClass);
				NamedClass superClass = getAtomicConcept(getFullElementName(elementName));
				SubClassAxiom sc = new SubClassAxiom(subClass, superClass);
				axioms.add(sc);
				atomTypes.add(atomClass);
			}
			// charge of atom
			DatatypePropertyAssertion dpa = getDoubleDatatypePropertyAssertion(atomName, "charge",
					charge);
			axioms.add(dpa);
		} else if (headName.equals("bond")) {
			String compoundName = head.getArgument(0).toPLString();
			String atom1Name = head.getArgument(1).toPLString();
			String atom2Name = head.getArgument(2).toPLString();
			String bondType = head.getArgument(3).toPLString();
			String bondClass = "Bond-" + bondType;
			String bondInstance = "bond" + bondNr;
			ObjectPropertyAssertion op = getRoleAssertion("hasBond", compoundName, "bond" + bondNr);
			axioms.add(op);
			// make Bond-X subclass of Bond if that hasn't been done already
			if (!bondTypes.contains(bondClass)) {
				NamedClass subClass = getAtomicConcept(bondClass);
				SubClassAxiom sc = new SubClassAxiom(subClass, getAtomicConcept("Bond"));
				axioms.add(sc);
				bondTypes.add(bondClass);
			}
			// make e.g. bond382 instance of Bond-3
			ClassAssertionAxiom ca = getConceptAssertion(bondClass, bondInstance);
			axioms.add(ca);
			bondNr++;
			// connect atoms with bond
			ObjectPropertyAssertion op1 = getRoleAssertion("inBond", bondInstance, atom1Name);
			ObjectPropertyAssertion op2 = getRoleAssertion("inBond", bondInstance, atom2Name);
			axioms.add(op1);
			axioms.add(op2);
		} else {
			// print clauses which are not supported yet
			System.out.println("unsupported clause");
			System.out.println(clause.toPLString());
			System.out.println(clause);
		}
		return axioms;
	}

	// takes a *.f or *.n file as input and returns the 
	// contained examples
	private static List<Individual> getExamples(File file) throws FileNotFoundException, IOException, ParseException {
		String content = Files.readFile(new File(prologDirectory + file));
		PrologParser pp = new PrologParser();
		Program programPos = pp.parseProgram(content);
		List<Individual> ret = new LinkedList<Individual>();
		for(Clause c : programPos.getClauses()) {
			String example = c.getHead().getArgument(0).toPLString();
			ret.add(getIndividual(example));
		}
		return ret;
	}
	
	private static void appendPosExamples(File file, List<Individual> examples) {
		StringBuffer content = new StringBuffer();
		for(Individual example : examples) {
			content.append("+\""+example.toString()+"\"");
		}
		Files.appendFile(file, content.toString());
	}
	
	private static void appendNegExamples(File file, List<Individual> examples) {
		StringBuffer content = new StringBuffer();
		for(Individual example : examples) {
			content.append("-\""+example.toString()+"\"");
		}
		Files.appendFile(file, content.toString());
	}	
	
	private static String getAtomClass(String element, String atomType) {
		return getFullElementName(element) + "-" + atomType;
	}

	private static ClassAssertionAxiom getConceptAssertion(String concept, String i) {
		Individual ind = getIndividual(i);
		NamedClass c = getAtomicConcept(concept);
		return new ClassAssertionAxiom(c, ind);
	}

	private static ObjectPropertyAssertion getRoleAssertion(String role, String i1, String i2) {
		Individual ind1 = getIndividual(i1);
		Individual ind2 = getIndividual(i2);
		ObjectProperty ar = getRole(role);
		return new ObjectPropertyAssertion(ar, ind1, ind2);
	}

	private static DoubleDatatypePropertyAssertion getDoubleDatatypePropertyAssertion(
			String individual, String datatypeProperty, double value) {
		Individual ind = getIndividual(individual);
		DatatypeProperty dp = getDatatypeProperty(datatypeProperty);
		return new DoubleDatatypePropertyAssertion(dp, ind, value);
	}

	private static Individual getIndividual(String name) {
		return new Individual(ontologyURI + "#" + name);
	}

	private static ObjectProperty getRole(String name) {
		return new ObjectProperty(ontologyURI + "#" + name);
	}

	private static DatatypeProperty getDatatypeProperty(String name) {
		return new DatatypeProperty(ontologyURI + "#" + name);
	}

	private static NamedClass getAtomicConcept(String name) {
		return new NamedClass(ontologyURI + "#" + name);
	}

	private static String getFullElementName(String abbreviation) {
		// return corresponding element or throw an error if it
		// is not in the list
		String result = chemElements.get(abbreviation);
		if (result == null)
			throw new Error("Unknown element " + abbreviation);
		else
			return result;
	}

	// create chemical element list
	private static void createChemElementsMapping() {
		chemElements = new HashMap<String, String>();
		chemElements.put("as", "Arsenic");
		chemElements.put("ba", "Barium");
		chemElements.put("br", "Bromine");
		chemElements.put("c", "Carbon");
		chemElements.put("ca", "Calcium");
		chemElements.put("cl", "Chlorine");
		chemElements.put("cu", "Copper");
		chemElements.put("f", "Fluorine");
		chemElements.put("ga", "Gallium");
		chemElements.put("h", "Hydrogen");
		chemElements.put("hg", "Mercury");
		chemElements.put("i", "Iodine");
		chemElements.put("k", "Krypton");
		chemElements.put("mn", "Manganese");
		chemElements.put("mo", "Molybdenum");
		chemElements.put("n", "Nitrogen");
		chemElements.put("na", "Sodium");
		chemElements.put("o", "Oxygen");
		chemElements.put("p", "Phosphorus");
		chemElements.put("pb", "Lead");
		chemElements.put("s", "Sulfur");
		chemElements.put("se", "Selenium");
		chemElements.put("sn", "Tin");
		chemElements.put("te", "Tellurium");
		chemElements.put("ti", "Titanium");
		chemElements.put("v", "Vanadium");
		chemElements.put("zn", "Zinc");
	}
}
