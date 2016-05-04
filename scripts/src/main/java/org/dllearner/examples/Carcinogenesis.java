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

import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.parser.PrologParser;
import org.dllearner.prolog.Atom;
import org.dllearner.prolog.Clause;
import org.dllearner.prolog.Program;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

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

	private static IRI ontologyIRI = IRI.create("http://dl-learner.org/carcinogenesis");

	// directory of Prolog files
	private static final String prologDirectory = "../examples/carcinogenesis/prolog/";	
	
	// mapping of symbols to names of chemical elements
	private static Map<String, String> chemElements;

	// structures in newgroups.pl
	private static Set<String> newGroups = new TreeSet<>();
	
	// types of atoms, bonds, and structures
	private static Set<String> atomTypes = new TreeSet<>();
	private static Set<String> bondTypes = new TreeSet<>();
	private static Set<String> structureTypes = new TreeSet<>();

	// we need a counter for bonds, because they are instances in OWL
	// but not in Prolog
	private static int bondNr = 0;
	private static int structureNr = 0;
	
	// list of all individuals in the knowlege base
//	private static Set<String> individuals = new TreeSet<String>();	
	// list of all compounds
	private static Set<String> compounds = new TreeSet<>();
	// compounds with positive ames test
	private static Set<String> compoundsAmes = new TreeSet<>();
	// list of all bonds
	private static Set<String> bonds = new TreeSet<>();
	
	// list of all "hasProperty" test
	private static Set<String> tests = new TreeSet<>();
	
	// we ignore the ames test since its distribution in PTE-2 is so
	// different from the training substances that a different testing
	// strategy was probably in use
	private static boolean ignoreAmes = false;
	private static boolean ignoreSalmonella = false;
	private static boolean ignoreCytogenCa = false;
	private static boolean includeMutagenesis = true;
	// if true we learn carcinogenic, if false we learn non-carcinogenic
	private static boolean learnCarcinogenic = true;
	private static boolean useNewGroups = true;
	
	private static boolean createPTE1Conf = false;
	private static boolean createPTE2Conf = false;
	
	static OWLOntologyManager man = OWLManager.createOWLOntologyManager();
	static OWLDataFactory df = new OWLDataFactoryImpl();
	
	/**
	 * @param args
	 *            No arguments supported.
	 */
	public static void main(String[] args) throws Exception {
		
		String[] files = new String[] { "newgroups.pl", "ames.pl", "atoms.pl", "bonds.pl", "gentoxprops.pl",
				"ind_nos.pl", "ind_pos.pl"};
		// "pte2/canc_nos.pl", "pte2/pte2ames.pl", "pte2/pte2atoms.pl",
		//		"pte2/pte2bonds.pl", "pte2/pte2gentox.pl", "pte2/pte2ind_nos.pl", "pte2/pte2newgroups.pl"
		// "train.b" => not a pure Prolog file but Progol/Aleph specific
		// };
		File owlFile = new File("/tmp/carcinogenesis.owl");

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
		OWLOntology kb = man.createOntology();
		createChemElementsMapping();
		createNewGroups();
		// create subclasses of atom
		OWLClass atomClass = getAtomicConcept("Atom");
		for (String element : chemElements.values()) {
			OWLClass elClass = getAtomicConcept(element);
			OWLSubClassOfAxiom sc = df.getOWLSubClassOfAxiom(elClass, atomClass);
			man.addAxiom(kb, sc);
		}
		// define properties including domain and range
		String kbString = "DPDOMAIN(" + getURI2("charge") + ") = " + getURI2("Atom") + ".\n";
		kbString += "DPRANGE(" + getURI2("charge") + ") = DOUBLE.\n";
		if(!ignoreAmes) {
			kbString += "DPDOMAIN(" + getURI2("amesTestPositive") + ") = " + getURI2("Compound") + ".\n";
			kbString += "DPRANGE(" + getURI2("amesTestPositive") + ") = BOOLEAN.\n";
		}
		if(includeMutagenesis) {
			kbString += "DPDOMAIN(" + getURI2("isMutagenic") + ") = " + getURI2("Compound") + ".\n";
			kbString += "DPRANGE(" + getURI2("isMutagenic") + ") = BOOLEAN.\n";
		}
		kbString += "OPDOMAIN(" + getURI2("hasAtom") + ") = " + getURI2("Compound") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasAtom") + ") = " + getURI2("Atom") + ".\n";
		kbString += "OPDOMAIN(" + getURI2("hasBond") + ") = " + getURI2("Compound") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasBond") + ") = " + getURI2("Bond") + ".\n";
		kbString += "OPDOMAIN(" + getURI2("inBond") + ") = " + getURI2("Bond") + ".\n";
		kbString += "OPRANGE(" + getURI2("inBond") + ") = " + getURI2("Atom") + ".\n";
		kbString += "OPDOMAIN(" + getURI2("hasStructure") + ") = " + getURI2("Compound") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasStructure") + ") = " + getURI2("Structure") + ".\n";
		kbString += getURI2("Di") + " SUB " + getURI2("Structure") + ".\n";
		kbString += getURI2("Halide") + " SUB " + getURI2("Structure") + ".\n";
		kbString += getURI2("Ring") + " SUB " + getURI2("Structure") + ".\n";
		OWLOntology kb2 = KBParser.parseKBFile(kbString);
		man.addAxioms(kb, kb2.getAxioms());

		// mapping clauses to axioms
		System.out.print("Mapping clauses to axioms ... ");
		startTime = System.nanoTime();
		ArrayList<Clause> clauses = program.getClauses();
		for (Clause clause : clauses) {
			List<OWLAxiom> axioms = mapClause(clause);
			for (OWLAxiom axiom : axioms)
				man.addAxiom(kb, axiom);
		}
		
		if(includeMutagenesis)
			addMutagenesis(kb);
		
		// special handling for ames test (we assume the ames test
		// was performed on all compounds but only the positive ones
		// are in ames.pl [the rest is negative in Prolog by CWA], so
		// we add negative test results here)
		for(String compound : compounds) {
			if(!ignoreAmes && !compoundsAmes.contains(compound)) {
				OWLAxiom ames = getBooleanDatatypePropertyAssertion(compound, "amesTestPositive", false);
				man.addAxiom(kb, ames);
			}
		}
		
		// disjoint classes axioms
		// OWL API is also buggy here, it adds a strange unused prefix
		// and cannot parser its own generated file
//		DisjointClassesAxiom disjointAtomTypes = getDisjointClassesAxiom(atomTypes);
//		kb.addAxiom(disjointAtomTypes);
		String[] mainClasses = new String[] {"Compound", "Atom", "Bond", "Structure"};
		Set<String> mainClassesSet = new HashSet<>(Arrays.asList(mainClasses));
		OWLAxiom disjointAtomTypes = getDisjointClassesAxiom(mainClassesSet);
		man.addAxiom(kb, disjointAtomTypes);		
		
		// all different axiom (UNA)
		// exporting differentIndividuals axioms is broken in OWL API
//		individuals.addAll(compounds);
//		individuals.addAll(bonds);
//		DifferentIndividualsAxiom una = getDifferentIndividualsAxiom(individuals);
//		kb.addAxiom(una);
		
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");

		// writing generated knowledge base
		System.out.print("Writing OWL file ... ");
		startTime = System.nanoTime();
		man.saveOntology(kb, new RDFXMLDocumentFormat(), new FileOutputStream(owlFile));
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");

		// generating conf files
		File confTrainFile = new File("examples/carcinogenesis/train.conf");
		Files.clearFile(confTrainFile);
		String confHeader = "import(\"carcinogenesis.owl\");\n\n";
		confHeader += "reasoner = fastInstanceChecker;\n";
		confHeader += "algorithm = refexamples;\n";
		confHeader += "refexamples.noisePercentage = 31;\n";
		confHeader += "refexamples.startClass = " + getURI2("Compound") + ";\n";
		confHeader += "refexamples.writeSearchTree = false;\n";
		confHeader += "refexamples.searchTreeFile = \"log/carcinogenesis/searchTree.log\";\n";
		confHeader += "\n";
		Files.appendToFile(confTrainFile, confHeader);
		
		// generating training examples
		File trainingFilePositives = new File(prologDirectory + "train.f");
		File trainingFileNegatives = new File(prologDirectory + "train.n");

		List<OWLIndividual> posTrainExamples = getExamples(trainingFilePositives);
		List<OWLIndividual> negTrainExamples = getExamples(trainingFileNegatives);
		appendPosExamples(confTrainFile, posTrainExamples);
		appendNegExamples(confTrainFile, negTrainExamples);
		
		// generating test examples for PTE-1
		// => put all in one file, because they were used as training for PTE-2
		File confPTE1File = new File("examples/carcinogenesis/testpte1.conf");
		File testPTE1Positives = new File(prologDirectory + "pte1.f");
		File testPTE1Negatives = new File(prologDirectory + "pte1.n");
		
		List<OWLIndividual> posPTE1Examples = getExamples(testPTE1Positives);
		List<OWLIndividual> negPTE1Examples = getExamples(testPTE1Negatives);
		appendPosExamples(confTrainFile, posPTE1Examples);
		appendNegExamples(confTrainFile, negPTE1Examples);
		if(createPTE1Conf) {
			Files.clearFile(confPTE1File);
			Files.appendToFile(confPTE1File, "import(\"pte.owl\");\nreasoner=fastInstanceChecker;\n\n");
			appendPosExamples(confPTE1File, posPTE1Examples);
			appendNegExamples(confPTE1File, negPTE1Examples);
		}
		
		// create a PTE-2 test file
		if(createPTE2Conf) {
			File confPTE2File = new File("examples/carcinogenesis/testpte2.conf");
			Files.clearFile(confPTE2File);
			Files.appendToFile(confPTE2File, "import(\"pte.owl\");\nreasoner=fastInstanceChecker;\n\n");
			Files.appendToFile(confPTE2File, getPTE2Examples());
		}

	}

	private static List<OWLAxiom> mapClause(Clause clause) throws ParseException, OWLOntologyCreationException {
		List<OWLAxiom> axioms = new LinkedList<>();
		Atom head = clause.getHead();
		String headName = head.getName();
		// Body body = clause.getBody();
		// ArrayList<Literal> literals = body.getLiterals();
		// handle: atm(compound,atom,element,atomtype,charge)
		
		// Ames-Test: http://en.wikipedia.org/wiki/Ames_test
		// problem: the file apparently mentions only positive
		// tests (why is it different from the other tests e.g. in
		// gentoxprops.pl?) => we need to add negative axioms for the
		// remaining stuff or use closed world assumption in the 
		// TBox dematerialisation later on
		if(headName.equals("ames")) {
			if(!ignoreAmes) {
			String compoundName = head.getArgument(0).toPLString();
			OWLAxiom ames = getBooleanDatatypePropertyAssertion(compoundName, "amesTestPositive", true);
			axioms.add(ames);
			compoundsAmes.add(compoundName);
			}
		} else if (headName.equals("atm")) {
			String compoundName = head.getArgument(0).toPLString();
			String atomName = head.getArgument(1).toPLString();
			String elementName = head.getArgument(2).toPLString();
			String type = head.getArgument(3).toPLString();
			double charge = Double.parseDouble(head.getArgument(4).toPLString());
			// make the compound an instance of the Compound class
			OWLAxiom cmpAxiom = getConceptAssertion("Compound", compoundName);
			axioms.add(cmpAxiom);
			compounds.add(compoundName);
			// relate compound and atom
			OWLAxiom ra = getRoleAssertion("hasAtom", compoundName, atomName);
			axioms.add(ra);
			// atom is made instance of the correct class
			String atomClass = getAtomClass(elementName, type);
			OWLAxiom ca = getConceptAssertion(atomClass, atomName);
			axioms.add(ca);
			// write subclass axiom if doesn't exist already
			if (!atomTypes.contains(atomClass)) {
				OWLClass subClass = getAtomicConcept(atomClass);
				OWLClass superClass = getAtomicConcept(getFullElementName(elementName));
				OWLAxiom sc = df.getOWLSubClassOfAxiom(subClass, superClass);
				axioms.add(sc);
				atomTypes.add(atomClass);
			}
			// charge of atom
			OWLAxiom dpa = getDoubleDatatypePropertyAssertion(atomName, "charge",
					charge);
			axioms.add(dpa);
		} else if (headName.equals("bond")) {
			String compoundName = head.getArgument(0).toPLString();
			String atom1Name = head.getArgument(1).toPLString();
			String atom2Name = head.getArgument(2).toPLString();
			String bondType = head.getArgument(3).toPLString();
			String bondClass = "Bond-" + bondType;
			String bondInstance = "bond" + bondNr;
			bonds.add(bondInstance);
			OWLAxiom op = getRoleAssertion("hasBond", compoundName, "bond" + bondNr);
			axioms.add(op);
			// make Bond-X subclass of Bond if that hasn't been done already
			if (!bondTypes.contains(bondClass)) {
				OWLClass subClass = getAtomicConcept(bondClass);
				OWLAxiom sc = df.getOWLSubClassOfAxiom(subClass, getAtomicConcept("Bond"));
				axioms.add(sc);
				bondTypes.add(bondClass);
			}
			// make e.g. bond382 instance of Bond-3
			OWLAxiom ca = getConceptAssertion(bondClass, bondInstance);
			axioms.add(ca);
			bondNr++;
			// connect atoms with bond
			OWLAxiom op1 = getRoleAssertion("inBond", bondInstance, atom1Name);
			OWLAxiom op2 = getRoleAssertion("inBond", bondInstance, atom2Name);
			axioms.add(op1);
			axioms.add(op2);
		} else if (headName.equals("has_property")) {
			String compoundName = head.getArgument(0).toPLString();
			String testName = head.getArgument(1).toPLString();
			if(!(ignoreSalmonella && testName.equals("salmonella"))
				&& !(ignoreCytogenCa && testName.equals("cytogen_ca"))) {
				String resultStr = head.getArgument(2).toPLString();
				boolean testResult = (resultStr.equals("p"));
					
				// create a new datatype property if it does not exist already
				if(!tests.contains(testName)) {
					String axiom1 = "DPDOMAIN(" + getURI2(testName) + ") = " + getURI2("Compound") + ".\n";
					String axiom2 = "DPRANGE(" + getURI2(testName) + ") = BOOLEAN.\n";
					OWLOntology kb = KBParser.parseKBFile(axiom1 + axiom2);
					axioms.addAll(kb.getAxioms());
				}
				// create an axiom with the test result
				OWLAxiom dpa = getBooleanDatatypePropertyAssertion(compoundName, testName,
						testResult);
				axioms.add(dpa);
			}
		// either parse this or ashby_alert - not both - ashby_alert contains
		// all information in ind already
		} else if (headName.equals("ind") || headName.equals("ring_no")) {
			// parse this only if the new groups are not parsed
//			if(!useNewGroups) {
			String compoundName = head.getArgument(0).toPLString();
			String structureName = head.getArgument(1).toPLString();
			int count = Integer.parseInt(head.getArgument(2).toPLString());
			// upper case first letter
			String structureClass = structureName.substring(0,1).toUpperCase() + structureName.substring(1);
			String structureInstance = structureName + "-" + structureNr;
			
			addStructureSubclass(axioms, structureClass);	
			
			for(int i=0; i<count; i++) {
				OWLAxiom op = getRoleAssertion("hasStructure", compoundName, structureInstance);
				axioms.add(op);
				// make e.g. halide10-382 instance of Bond-3
				OWLAxiom ca = getConceptAssertion(structureClass, structureInstance);
				axioms.add(ca);
				structureNr++;
			}
//			}
		} else if (headName.equals("ashby_alert")) {
			// ... currently ignored ...
		} else if (newGroups.contains(headName)) {
			if(useNewGroups) {
			String compoundName = head.getArgument(0).toPLString();
			String structureName = headName;
			// upper case first letter
			String structureClass = structureName.substring(0,1).toUpperCase() + structureName.substring(1);
				String structureInstance = structureName + "-" + structureNr;
			
			addStructureSubclass(axioms, structureClass);
			
				OWLAxiom op = getRoleAssertion("hasStructure", compoundName, structureInstance);
				axioms.add(op);
				OWLAxiom ca = getConceptAssertion(structureClass, structureInstance);
				axioms.add(ca);
				structureNr++;
			}
		} else {
			// print clauses which are not supported yet
			System.out.println("unsupported clause");
			System.out.println(clause.toPLString());
			System.out.println(clause);
			System.exit(0);
		}
		return axioms;
	}

	private static void addStructureSubclass(List<OWLAxiom> axioms, String structureClass) {
		// build in more fine-grained subclasses e.g. Di+number is subclass of Di
		if (!structureTypes.contains(structureClass)) {
			OWLClass nc = getAtomicConcept("Structure");
			if(structureClass.contains("Di"))
				nc = getAtomicConcept("Di");
			else if(structureClass.contains("ring") || structureClass.contains("Ring"))
				nc = getAtomicConcept("Ring");
			else if(structureClass.contains("halide") || structureClass.contains("Halide"))
				nc = getAtomicConcept("Halide");
			OWLClass subClass = getAtomicConcept(structureClass);
			OWLAxiom sc = df.getOWLSubClassOfAxiom(subClass, nc);
			axioms.add(sc);
			structureTypes.add(structureClass);
		}			
	}
	
	// takes a *.f or *.n file as input and returns the 
	// contained examples
	private static List<OWLIndividual> getExamples(File file) throws IOException, ParseException {
		String content = Files.readFile(file);
		PrologParser pp = new PrologParser();
		Program programPos = pp.parseProgram(content);
		List<OWLIndividual> ret = new LinkedList<>();
		for(Clause c : programPos.getClauses()) {
			String example = c.getHead().getArgument(0).toPLString();
			ret.add(getIndividual(example));
		}
		return ret;
	}
	
	public static void appendPosExamples(File file, List<OWLIndividual> examples) {
		StringBuffer content = new StringBuffer();
		for(OWLIndividual example : examples) {
			if(learnCarcinogenic)
				content.append("+\"").append(example.toString()).append("\"\n");
			else
				content.append("-\"").append(example.toString()).append("\"\n");
		}
		Files.appendToFile(file, content.toString());
	}
	
	public static void appendNegExamples(File file, List<OWLIndividual> examples) {
		StringBuffer content = new StringBuffer();
		for(OWLIndividual example : examples) {
			if(learnCarcinogenic)
				content.append("-\"").append(example.toString()).append("\"\n");
			else
				content.append("+\"").append(example.toString()).append("\"\n");
		}
		Files.appendToFile(file, content.toString());
	}	
	
	private static String getAtomClass(String element, String atomType) {
		return getFullElementName(element) + "-" + atomType;
	}

	private static OWLAxiom getConceptAssertion(String concept, String i) {
		OWLIndividual ind = getIndividual(i);
		OWLClass c = getAtomicConcept(concept);
		return df.getOWLClassAssertionAxiom(c, ind);
	}

	private static OWLAxiom getRoleAssertion(String role, String i1, String i2) {
		OWLIndividual ind1 = getIndividual(i1);
		OWLIndividual ind2 = getIndividual(i2);
		OWLObjectProperty ar = getRole(role);
		return df.getOWLObjectPropertyAssertionAxiom(ar, ind1, ind2);
	}

	private static OWLAxiom getBooleanDatatypePropertyAssertion(
			String individual, String datatypeProperty, boolean value) {
		OWLIndividual ind = getIndividual(individual);
		OWLDataProperty dp = getDatatypeProperty(datatypeProperty);
		return  df.getOWLDataPropertyAssertionAxiom(dp, ind, value);
	}	
	
	private static OWLAxiom getDoubleDatatypePropertyAssertion(
			String individual, String datatypeProperty, double value) {
		OWLIndividual ind = getIndividual(individual);
		OWLDataProperty dp = getDatatypeProperty(datatypeProperty);
		return df.getOWLDataPropertyAssertionAxiom(dp, ind, value);
	}

	private static OWLAxiom getDisjointClassesAxiom(Set<String> classes) {
		Set<OWLClassExpression> descriptions = new HashSet<>();
		for(String namedClass : classes)
			descriptions.add(df.getOWLClass(IRI.create(getURI(namedClass))));
		return df.getOWLDisjointClassesAxiom(descriptions);
	}
	
	@SuppressWarnings({"unused"})
	private static OWLAxiom getDifferentIndividualsAxiom(Set<String> individuals) {
		Set<OWLIndividual> inds = new HashSet<>();
		for(String i : individuals)
			inds.add(getIndividual(i));
		return df.getOWLDifferentIndividualsAxiom(inds);
	}	
	
	private static OWLIndividual getIndividual(String name) {
		return df.getOWLNamedIndividual(IRI.create(ontologyIRI + "#" + name));
	}

	private static OWLObjectProperty getRole(String name) {
		return df.getOWLObjectProperty(IRI.create(ontologyIRI + "#" + name));
	}

	private static OWLDataProperty getDatatypeProperty(String name) {
		return df.getOWLDataProperty(IRI.create(ontologyIRI + "#" + name));
	}

	private static OWLClass getAtomicConcept(String name) {
		return df.getOWLClass(IRI.create(ontologyIRI + "#" + name));
	}

	private static String getURI(String name) {
		return ontologyIRI + "#" + name;
	}
	
	// returns URI including quotationsmark (need for KBparser)
	private static String getURI2(String name) {
		return "\"" + getURI(name) + "\"";
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
		chemElements = new HashMap<>();
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
	
	private static void createNewGroups() {		
		String[] groups = new String[] {"six_ring", "non_ar_6c_ring",
				"ketone", "amine", "alcohol", "ether", "ar_halide",
				"five_ring", "non_ar_5c_ring", "alkyl_halide",
				"methyl", "non_ar_hetero_5_ring", "nitro", "sulfo",
				"methoxy", "amine", "aldehyde", "sulfide",
				"non_ar_hetero_6_ring", "phenol", "carboxylic_acid",
				"ester", "imine", 
		};
		
		List<String> list = Arrays.asList(groups);
		newGroups.addAll(list);
	}

	/**
	 * <p>To find out whether a substance is carinogenetic go to 
	 * "http://ntp-server.niehs.nih.gov/" and click
	 * on "Testing Status of Agents at NTP".</p>
	 * 
	 * Levels:
	 * <ul>
	 * 	<li>CE = clear evidence</li>
	 *  <li>SE = some evidence</li>
	 *  <li>E = equivocal evidence</li>
	 *  <li>NE = no evidence</li>
	 * </ul>
	 * Levels CE and SE are positive examples. E and NE negative examples.
	 * Experiments are performed on rats and mice of both genders, so we
	 * have four evidence values. An example is positive if at least one
	 * value is SE or CE.
	 * 
	 * <p>Some values are taken from the IJCAI-97 paper of Muggleton.</p>
	 * 
	 * <p>Positives (19): <br />
	 * <ul>
	 * <li>t3 (SE+3NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BCACAFD4-123F-7908-7B521E4F665EFBD9</li>
	 * <li>t4 (3CE+NE) - contradicts IJCAI-97 paper and should probably be case 75-52-5 instead of 75-52-8: http://ntp.niehs.nih.gov/index.cfm?objectid=BCE49084-123F-7908-7BE127F7AF1FFBB5</li>
	 * <li>t5: paper</li>
	 * <li>t7: paper</li>
	 * <li>t8: paper</li>
	 * <li>t9 (3CE+SE): http://ntp.niehs.nih.gov/index.cfm?objectid=BD7C6869-123F-7908-7BDEA4CFAA55CEA8</li>
	 * <li>t10: paper</li>
	 * <li>t12 (2SE+E+NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BCB0ADE0-123F-7908-7BEC101C7309C4DE</li>
	 * <li>t14 (2CE+2NE) probably 111-42-2 instead of 11-42-2: http://ntp.niehs.nih.gov/index.cfm?objectid=BCC60FF1-123F-7908-7B2D579AA48DE90C</li>
	 * <li>t15: paper</li>
	 * <li>t16 (2CE+SE+E): http://ntp.niehs.nih.gov/index.cfm?objectid=BCC5D9CE-123F-7908-7B959CCE5262468A</li>
	 * <li>t18 (2SE+E+NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BCA087AA-123F-7908-7B79FDFDE3CDCF87</li>
	 * <li>t19 (2CE+E+NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BCAE5690-123F-7908-7B02E35E2BB57694</li>
	 * <li>t20 (2SE+E+NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BCF95607-123F-7908-7B0761D3C515CC12</li>
	 * <li>t21 (CE+3NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BCFCB63C-123F-7908-7BF910C2783AE9FE</li>
	 * <li>t22 (SE+3NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BD8345C2-123F-7908-7BC52FEF80F110E1</li>
	 * <li>t23 (4CE): http://ntp.niehs.nih.gov/index.cfm?objectid=BCADD2D9-123F-7908-7B5C8180FE80B22F</li>
	 * <li>t24 (CE+E): http://ntp.niehs.nih.gov/index.cfm?objectid=BCFB19FF-123F-7908-7B845E176F13E6E1</li>
	 * <li>t25 (3CE+SE): http://ntp.niehs.nih.gov/index.cfm?objectid=BD2D2A62-123F-7908-7B0DA824E782754C</li>
	 * <li>t30 (2CE+SE+E) : http://ntp.niehs.nih.gov/index.cfm?objectid=BCB13734-123F-7908-7BEBA533E35A48B7</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>Negatives (10):
	 * <ul>
	 * <li>t1 (4NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BD9FF53C-123F-7908-7B123DAE0A25B122 </li>
	 * <li>t2 (4NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BCF8651E-123F-7908-7B21DD5ED83CD0FF </li>
	 * <li><strike>t4: paper</strike></li>
	 * <li>t6: paper</li>
	 * <li>t11: paper</li>
	 * <li>t13 (4NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BD136ED6-123F-7908-7B619EE79F2FD062</li>
	 * <li>t17: paper</li>
	 * <li>t26 (2E+2NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BD1E6209-123F-7908-7B95EB8BAE662CE7</li>
	 * <li>t27 (E+3NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BCAC5D00-123F-7908-7BC46ECB72A6C91B</li>
	 * <li>t28 (E+3NE): http://ntp.niehs.nih.gov/index.cfm?objectid=BD34E02A-123F-7908-7BC6791917B591DF</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>Unclear (1):
	 * <ul>
	 * <li>t29: probably a negative (see http://ntp.niehs.nih.gov/index.cfm?objectid=BD855EA1-123F-7908-7B573FC3C08188DC) but
	 * no tests directly for this substance</li>
	 * </ul>
	 * 
	 * <p>The following examples are probably not part of the IJCAI PTE-2 challenge
	 * (reports younger than 1998):
	 * <ul>
	 * <li>pos: t21 (5/99), t25 (9/04), t30(10/01)</li>
	 * <li>neg: t26 (5/99), t27 (05/01), t28 (05/00), t29 (09/02)</li>
	 * </ul>
	 * </p>
	 * </p>
	 * @return A string for all examples as used in the conf file.
	 */
	public static String getPTE2Examples() {
		String[] pos = new String[] {"t3","t4","t5","t7","t8",
				"t9",
				"t10","t12",
				"t14","t15","t16","t18","t19","t20",
				"t21",
				"t22",
				"t23",
				"t24",
				"t25",
				"t30"};
		String[] neg = new String[] {"t1", "t2",
				"t6", "t11", "t13",
				"t17","t26","t27",
				"t28","t29"
				};

		String ret = "";
		for(String posEx : pos) {
			if(learnCarcinogenic)
				ret += "+" + getURI2(posEx) + "\n";
			else
				ret += "-" + getURI2(posEx) + "\n";
		}
		for(String negEx : neg) {
			if(learnCarcinogenic)
				ret += "-" + getURI2(negEx) + "\n";
			else
				ret += "+" + getURI2(negEx) + "\n";
		}
		
		return ret;
	}
	
	private static void addMutagenesis(OWLOntology kb) {
		String[] mutagenicCompounds = new String[] {
			"d101", "d104", "d106", "d107", "d112", "d113", "d117", 
			"d121", "d123", "d126", "d128", "d13", "d135", "d137", 
			"d139", "d140", "d143", "d144", "d145", "d146", "d147",
			"d152", "d153", "d154", "d155", "d156", "d159", "d160",
			"d161", "d163", "d164", "d166", "d168", "d171", "d173",
			"d174", "d177", "d179", "d18", "d180", "d182", "d183",
			"d185", "d186", "d187", "d188", "d189", "d19", "d191",
			"d192", "d193", "d195", "d197", "d2", "d201", "d202", 
			"d205", "d206", "d207", "d211", "d214", "d215", "d216",
			"d224", "d225", "d227", "d228", "d229", "d231", "d235",
			"d237", "d239", "d242", "d245", "d246", "d249", "d251",
			"d254", "d257", "d258", "d261", "d264", "d266", "d269",
			"d27", "d270", "d271", "d28", "d288", "d292", "d297",
			"d300", "d308", "d309", "d311", "d313", "d314", "d322",
			"d323", "d324", "d329", "d330", "d332", "d334", "d35",
			"d36", "d37", "d38", "d41", "d42", "d48", "d50", "d51",
			"d54", "d58", "d61", "d62", "d63", "d66", "d69", "d72",
			"d76", "d77", "d78", "d84", "d86", "d89", "d92", "d96"};
		TreeSet<String> mutagenic = new TreeSet<>(Arrays.asList(mutagenicCompounds));
	
		for(String compound : compounds) {
			if(mutagenic.contains(compound)) {
				OWLAxiom muta = getBooleanDatatypePropertyAssertion(compound, "isMutagenic", true);
				man.addAxiom(kb, muta);
			} else {
				OWLAxiom muta = getBooleanDatatypePropertyAssertion(compound, "isMutagenic", false);
				man.addAxiom(kb, muta);
			}
		}
	}
}