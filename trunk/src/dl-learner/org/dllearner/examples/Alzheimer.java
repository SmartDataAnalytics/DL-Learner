package org.dllearner.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dllearner.core.owl.Axiom;
import org.dllearner.parser.ParseException;
import org.dllearner.parser.PrologParser;
import org.dllearner.prolog.Atom;
import org.dllearner.prolog.Clause;
import org.dllearner.prolog.Program;
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

		// mapping clauses to axioms
		System.out.print("Mapping clauses to axioms ... ");
		startTime = System.nanoTime();
		ArrayList<Clause> clauses = program.getClauses();
		for (Clause clause : clauses) {
			List<Axiom> axioms = mapClause(clause);
			for (Axiom axiom : axioms) {
				// kb.addAxiom(axiom);
			}
		}
		System.out.println("OK (" + time + ").");

	}

	private static List<Axiom> mapClause(Clause clause) throws IOException,
			ParseException {
		List<Axiom> axioms = new LinkedList<Axiom>();
		Atom head = clause.getHead();
		String headName = head.getName();

		if (headName.equals("polar")) {

		} else if (headName.equals("size")) {

		} else if (headName.equals("flex")) {

		} else if (headName.equals("h_doner")) {

		} else if (headName.equals("h_acceptor")) {

		} else if (headName.equals("pi_doner")) {

		} else if (headName.equals("pi_acceptor")) {

		} else if (headName.equals("polarisable")) {

		} else if (headName.equals("sigma")) {

		} else if (headName.equals("gt")) {

		} else if (headName.equals("great_polar")) {

		} else if (headName.equals("great_size")) {

		} else if (headName.equals("great_flex")) {

		} else if (headName.equals("great_h_don")) {

		} else if (headName.equals("great_h_acc")) {

		} else if (headName.equals("great_pi_don")) {

		} else if (headName.equals("great_pi_acc")) {

		} else if (headName.equals("great_polari")) {

		} else if (headName.equals("great_sigma")) {

		} else if (headName.equals("x_subst")) {

		} else if (headName.equals("alk_groups")) {

		} else if (headName.equals("r_subst_1")) {

		} else if (headName.equals("n_val")) {

		} else if (headName.equals("r_subst_2")) {

		} else if (headName.equals("r_subst_3")) {

		} else if (headName.equals("ring_substitutions")) {

		} else if (headName.equals("ring_subst_4")) {

		} else if (headName.equals("ring_subst_3")) {

		} else if (headName.equals("ring_subst_2")) {

		} else if (headName.equals("ring_subst_5")) {

		} else if (headName.equals("ring_subst_6")) {

		} else if (headName.equals("r_subst")) {

		} else if (headName.equals("ring_struc")) {

		} else {
			System.out.println("clause not supportet: " + headName);
		}
		return axioms;
	}
}
