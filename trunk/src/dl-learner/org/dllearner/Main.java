/**
 * Copyright (C) 2007, Jens Lehmann
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

package org.dllearner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.Config.Algorithm;
import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.algorithms.LearningAlgorithm;
import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.Reasoner;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.dl.AssertionalAxiom;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.ConceptAssertion;
import org.dllearner.core.dl.FlatABox;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.KB;
import org.dllearner.core.dl.Negation;
import org.dllearner.core.dl.RoleAssertion;
import org.dllearner.kb.OntologyFileFormat;
import org.dllearner.learningproblems.DefinitionLP;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.modules.ModuleInvocator;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.parser.TokenMgrError;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastRetrievalReasoner;
import org.dllearner.reasoning.KAON2Reasoner;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.ConceptTransformation;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.RoleComparator;
import org.dllearner.utilities.Stat;

/** 
 * Stellt die Verbindung zwischen Parser und den anderen Programmteilen her.
 * Startet den Algorithmus.
 * 
 * TODO: Die Hauptmethode sollte noch in kleinere Einheiten zerlegt werden zur
 * besseren Lesbarkeit.
 * 
 * TODO: einige Errors können durch RuntimeException ersetzt werden (kein throws
 * und Exception-Handling notwendig)
 * 
 * @author Jens Lehmann
 * 
 */
public class Main {

	private static FlatABox abox;

	private static ConfigurationManager confMgr;
	
	private Reasoner reasoner;
	private ReasoningService rs;
	private LearningProblem learningProblem;
	// es werden jeweils die Dateien mit dem zugehörigen Format abgelegt
	Map<URL, OntologyFileFormat> importedFiles;
	Map<File, OntologyFileFormat> exportedFiles;
	KB kb;
	SortedSet<Individual> posExamples;
	SortedSet<Individual> negExamples;
	List<File> loadedJars;
	List<String> preprocessingModules;

	private static long algorithmStartTime = 0;
	
	private String baseDir;

	// neuer Hauptkonstruktor
	public Main(KB kb, Map<AtomicConcept, SortedSet<Individual>> positiveExamples,
			Map<AtomicConcept, SortedSet<Individual>> negativeExamples,
			List<ConfigurationOption> confOptions, List<List<String>> functionCalls,
			String baseDir, boolean useQueryMode) {

		this.kb = kb;
		this.baseDir = baseDir;

		// Konfigurationsmanager erstellen
		confMgr = new ConfigurationManager(confOptions);
		if (!confMgr.checkConsistency()) {
			throw new Error(
					"inconsistent/unsupported combination of configuration options");
		}
		
		// Funktionsaufrufe in Klassenvariablen laden
		parseFunctionCalls(functionCalls);
		


		// verwandte Individuen entfernen
		/*
		 * removeIndividualSubtree(kb, new
		 * Individual("http://localhost/foo#nonnie"));
		 * removeIndividualSubtree(kb, new
		 * Individual("http://localhost/foo#beatrice"));
		 * removeIndividualSubtree(kb, new
		 * Individual("http://localhost/foo#ann"));
		 */

		// momentan keine Unterst�tzung f�r das gleichzeitige Lernen mehrerer
		// Konzepte
		// TODO: irgendwann lernen mehrerer Konzepte eventuell unterst�tzen
		// (momentan keine hohe Priorit�t)
		if (positiveExamples.size() > 1 || negativeExamples.size() > 1) {
			throw new Error("Currently only one target concept is supported.");
		}

		// Jar-Dateien laden
		List<URL> urls = new LinkedList<URL>();
		// for (List<String> call : functionCalls) {
		//	if (call.get(0).equals("loadJarFile")) {
		for(File f : loadedJars) {
				// base dir wird hier nicht verwendet, da die Module eher
				// relativ zum DL-Learner als zu den Beispielen liegen
				// File f = new File(baseDir,call.get(1));
				// File f = new File(call.get(1));
				System.out.println("Loading jar file \"" + f.getAbsolutePath() + "\".");
				try {
					urls.add(new URL("file", "localhost", f.getAbsolutePath()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
		//	}
		//}
		}
		
		URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[] {}));

		// confMgr.applyOptions();
		
		// Preprocessing-Modul aufrufen, falls notwendig
		for (String module : preprocessingModules) {
		//if (!Config.preprocessingModule.equals("")) {
			System.out.println("Invoking preprocessing module main class " + module + ".");
			ModuleInvocator mi = new ModuleInvocator(loader, module);
			mi.invokePreprocessingModule(kb, positiveExamples, negativeExamples, confOptions, functionCalls, baseDir, useQueryMode);
		//}
		}
		
		// Funktionsaufrufe nochmal in Klassenvariablen laden, falls sie sich
		// geändert haben
		parseFunctionCalls(functionCalls);
		
		// Konfigurationsoptionen anwenden (könnten auch von einem Modul
		// gesetzt werden)
		confMgr.applyOptions();

		// Beispiele werden in eine Menge gebracht
		posExamples = new TreeSet<Individual>();
		negExamples = new TreeSet<Individual>();

		for (AtomicConcept target : positiveExamples.keySet())
			posExamples = positiveExamples.get(target);

		for (AtomicConcept target : negativeExamples.keySet())
			negExamples = negativeExamples.get(target);

		// File-Objekte an function calls binden
		/*
		 * boolean importKB = functionCalls.containsKey("import"); importedFiles =
		 * new LinkedList<File>(); if (importKB) { Set<String>
		 * importedFilesStr = functionCalls.get("import"); for (String
		 * importFileStr : importedFilesStr) { importedFiles.add(new
		 * File(baseDir, importFileStr)); } }
		 */
		/*
		importedFiles = new HashMap<File, OntologyFileFormat>();
		for (List<String> call : functionCalls) {
			if (call.get(0).equals("import")) {
				File f = new File(baseDir, call.get(1));
				if (call.size() == 2)
					// falls nichts angegeben, dann wird RDF/XML gewählt
					importedFiles.put(f, OntologyFileFormat.RDF_XML);
				else {
					String formatString = call.get(2);
					OntologyFileFormat format;
					if (formatString.equals("RDF/XML"))
						format = OntologyFileFormat.RDF_XML;
					else
						format = OntologyFileFormat.N_TRIPLES;
					importedFiles.put(f, format);
				}
			}
		}
		*/

		// DIG-Reasoner testen
		// new DIGReasonerTest(kb);
		// System.exit(0);

		reasoner = createReasoner(kb, importedFiles);
		ReasoningService rs = new ReasoningService(reasoner);
		learningProblem = new LearningProblem(rs, posExamples, negExamples);

		// export function
		/*
		 * if (functionCalls.containsKey("export")) { for (String export :
		 * functionCalls.get("export")) { rs.saveOntology(export, baseDir); } }
		 */
		/*
		for (List<String> call : functionCalls) {
			if (call.get(0).equals("export")) {
				File f = new File(baseDir, call.get(1));
				if (call.size() == 2)
					// falls nichts angegeben, dann wird RDF/XML gewählt
					rs.saveOntology(f, OntologyFileFormat.RDF_XML);
				else {
					String formatString = call.get(2);
					OntologyFileFormat format;
					if (formatString.equals("RDF/XML"))
						format = OntologyFileFormat.RDF_XML;
					else
						format = OntologyFileFormat.N_TRIPLES;
					rs.saveOntology(f, format);
				}
			}
		}
		*/
		for(File f : exportedFiles.keySet()) {
			rs.saveOntology(f, exportedFiles.get(f));
		}

		// Satisfiability Check
		if (Config.reasonerType != ReasonerType.FAST_RETRIEVAL) {
			System.out.print("Satisfiability Check ... ");
			long satStartTime = System.nanoTime();
			boolean satisfiable = rs.isSatisfiable();
			long satDuration = System.nanoTime() - satStartTime;

			String result = satisfiable ? "OK" : "not satisfiable!";
			System.out.println(result + " ("
					+ Helper.prettyPrintNanoSeconds(satDuration, true, false) + ")");
			if (!satisfiable)
				System.exit(0);
		}

		autoDetectConceptsAndRoles(rs);
		
		// Subsumptionhierarchie vorbereiten
		System.out.print("Preparing Subsumption Hierarchy ... ");
		long subHierTimeStart = System.nanoTime();
		reasoner.prepareSubsumptionHierarchy();
		long subHierTime = System.nanoTime() - subHierTimeStart;
		System.out.println("OK ("
				+ Helper.prettyPrintNanoSeconds(subHierTime, true, false) + ")");

		// prepare role hierarchy
		System.out.print("Preparing Role Hierarchy ... ");
		long roleHierTimeStart = System.nanoTime();
		try {
			reasoner.prepareRoleHierarchy();
			// System.out.println();
			// System.out.println(reasoner.getRoleHierarchy());	
		} catch (ReasoningMethodUnsupportedException e1) {
			System.out.println("Tried to construct the role hierarchy, but the reasoner "
					+ "does not support it. Currently only DIG reasoners support this feature.");
		}
		long roleHierTime = System.nanoTime() - roleHierTimeStart;
		System.out.println("OK ("
				+ Helper.prettyPrintNanoSeconds(roleHierTime, true, false) + ")");		
		
		// Beispiele anzeigen
		boolean oneLineExampleInfo = true;
		int maxExampleStringLength = posExamples.toString().length();
		maxExampleStringLength = Math.max(maxExampleStringLength, negExamples.toString()
				.length());
		if (maxExampleStringLength > 100)
			oneLineExampleInfo = false;

		if (oneLineExampleInfo) {
			System.out.println("positive examples[" + posExamples.size() + "]: "
					+ posExamples);
			System.out.println("negative examples[" + negExamples.size() + "]: "
					+ negExamples);
		} else {
			System.out.println("positive examples[" + posExamples.size() + "]: ");
			for (Individual ex : posExamples)
				System.out.println("  " + ex);
			System.out.println("negative examples[" + negExamples.size() + "]: ");
			for (Individual ex : negExamples)
				System.out.println("  " + ex);
		}

		// Individuals
		if (Config.showIndividuals) {
			int stringLength = reasoner.getIndividuals().toString().length();
			if (stringLength > Config.maxLineLength) {
				System.out.println("individuals[" + reasoner.getIndividuals().size()
						+ "]: ");
				for (Individual ind : reasoner.getIndividuals())
					System.out.println("  " + ind);
			} else
				System.out.println("individuals[" + reasoner.getIndividuals().size()
						+ "]: " + reasoner.getIndividuals());
		}

		// Konzepte
		if (Config.showConcepts) {
			int stringLength = reasoner.getAtomicConcepts().toString().length();
			if (stringLength > Config.maxLineLength) {
				System.out.println("concepts[" + reasoner.getAtomicConcepts().size()
						+ "]: ");
				for (AtomicConcept ac : reasoner.getAtomicConcepts())
					System.out.println("  " + ac);
			} else
				System.out.println("concepts[" + reasoner.getAtomicConcepts().size()
						+ "]: " + reasoner.getAtomicConcepts());
		}

		// Rollen
		if (Config.showRoles) {
			int stringLength = reasoner.getAtomicRoles().toString().length();
			if (stringLength > Config.maxLineLength) {
				System.out.println("roles[" + reasoner.getAtomicRoles().size() + "]: ");
				for (AtomicRole r : reasoner.getAtomicRoles())
					System.out.println("  " + r);
			} else
				System.out.println("roles[" + reasoner.getAtomicRoles().size() + "]: "
						+ reasoner.getAtomicRoles());
		}

		// Anzeige der Subsumptionhierarchie
		if (Config.showSubsumptionHierarchy) {
			System.out.println("Subsumption Hierarchy:");
			try {
				System.out.println(reasoner.getSubsumptionHierarchy());
			} catch (ReasoningMethodUnsupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Anzeige der Wissensbasis
		if (Config.showInternalKB) {
			System.out.println(kb);
		}
		
				
		// Vergleichsreasoner erstellen zum Testen
		// Reasoner reasoner2 = null;
		// try {
		// URL dig2URL = new URL("http://localhost:8081");
		// reasoner2 = new DIGReasoner(kb, dig2URL);
		// reasoner2 = new KAON2Reasoner(kb, importedFiles);
		// } catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// ReasoningService rs2 = new ReasoningService(reasoner2);
		// LearningProblem learningProblem2 = new LearningProblem(rs2,
		// posExamples, negExamples);

		// test(learningProblem);
		// test2(learningProblem);

		// createStatisticsMLDMPaper(learningProblem, baseDir);
		// System.exit(0);

		if (useQueryMode) {
			processQueryMode(reasoner);
		} else {
			if (Config.statisticMode)
				; // createStatistics(learningProblem, baseDir);
			else {

				rs.resetStatistics();
				algorithmStartTime = System.nanoTime();

				if (Config.algorithm == Algorithm.BRUTE_FORCE) {
					LearningAlgorithm la = new BruteForceLearner(learningProblem);
					la.start();
				} else if (Config.algorithm == Algorithm.RANDOM_GUESSER) {
					// new RandomGuesser(learningProblem, 10000, 10);
				} else if (Config.algorithm == Algorithm.GP 
						|| Config.algorithm == Algorithm.HYBRID_GP) {
					//LearningAlgorithm la = new GP(learningProblem);
					//la.start();
				} else {
					if (Config.Refinement.improveSubsumptionHierarchy) {
						// if(Config.reasonerType == ReasonerType.DIG) {
						System.out
								.println("Subsumption Hierarchy is improved for Refinement Operator Based Algorithm");

						
						try {
							reasoner.getSubsumptionHierarchy()
									.improveSubsumptionHierarchy();
							// System.out.println(reasoner.getSubsumptionHierarchy());
						} catch (ReasoningMethodUnsupportedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// ((DIGReasoner)reasoner).improveSubsumptionHierarchy();
						// ((DIGReasoner)reasoner).printSubsumptionHierarchies();
						// } else {
						// System.out.println("Subsumption Hierarchy not
						// improved (support for selected reasoner not
						// implemented)");
						// }
					}
					LearningAlgorithm la = new ROLearner(learningProblem);
					la.start();
					// new ROLearner(learningProblem, learningProblem2);
				}

				long algorithmDuration = System.nanoTime() - algorithmStartTime;

				if (rs.getNrOfRetrievals() > 0) {
					System.out.println("number of retrievals: " + rs.getNrOfRetrievals());
					System.out.println("retrieval reasoning time: "
							+ Helper.prettyPrintNanoSeconds(rs
									.getRetrievalReasoningTimeNs()) + " ( "
							+ Helper.prettyPrintNanoSeconds(rs.getTimePerRetrievalNs())
							+ " per retrieval)");
				}
				if (rs.getNrOfInstanceChecks() > 0) {
					System.out.println("number of instance checks: "
							+ rs.getNrOfInstanceChecks() + " ("
							+ rs.getNrOfMultiInstanceChecks() + " multiple)");
					System.out.println("instance check reasoning time: "
							+ Helper.prettyPrintNanoSeconds(rs
									.getInstanceCheckReasoningTimeNs())
							+ " ( "
							+ Helper.prettyPrintNanoSeconds(rs
									.getTimePerInstanceCheckNs())
							+ " per instance check)");
				}
				if (rs.getNrOfSubsumptionHierarchyQueries() > 0) {
					System.out.println("subsumption hierarchy queries: "
							+ rs.getNrOfSubsumptionHierarchyQueries());
					/*
					 * System.out.println("subsumption hierarchy reasoning time: " +
					 * Helper.prettyPrintNanoSeconds(rs
					 * .getSubsumptionHierarchyTimeNs()) + " ( " +
					 * Helper.prettyPrintNanoSeconds(rs
					 * .getTimePerSubsumptionHierarchyQueryNs()) + " per
					 * subsumption hierachy query)");
					 */
				}
				if (rs.getNrOfSubsumptionChecks() > 0) {
					System.out.println("(complex) subsumption checks: "
							+ rs.getNrOfSubsumptionChecks() + " ("
							+ rs.getNrOfMultiSubsumptionChecks() + " multiple)");
					System.out.println("subsumption reasoning time: "
							+ Helper.prettyPrintNanoSeconds(rs
									.getSubsumptionReasoningTimeNs())
							+ " ( "
							+ Helper.prettyPrintNanoSeconds(rs
									.getTimePerSubsumptionCheckNs())
							+ " per subsumption check)");
				}
				DecimalFormat df = new DecimalFormat();
				double reasoningPercentage = 100 * rs.getOverallReasoningTimeNs()
						/ (double) algorithmDuration;
				System.out
						.println("overall reasoning time: "
								+ Helper.prettyPrintNanoSeconds(rs
										.getOverallReasoningTimeNs()) + " ("
								+ df.format(reasoningPercentage)
								+ "% of overall runtime)");
				System.out.println("overall algorithm runtime: "
						+ Helper.prettyPrintNanoSeconds(algorithmDuration));
			}
		}

		// Aufräumarbeiten (TODO: das fehlt für KAON2 noch)
		if (reasoner instanceof DIGReasoner)
			((DIGReasoner) reasoner).releaseKB();
	}

	// parst Funktionsaufrufe in Klassenvariablen
	private void parseFunctionCalls(List<List<String>> functionCalls) {
		// Funktionsaufrufe parsen
		importedFiles = new HashMap<URL, OntologyFileFormat>();
		exportedFiles = new HashMap<File, OntologyFileFormat>();
		loadedJars = new LinkedList<File>();
		preprocessingModules = new LinkedList<String>();
		for (List<String> call : functionCalls) {
			if(call.get(0).equals("import")) {
				// alte Methode mit file statt URI
				// File f = new File(baseDir, call.get(1));
				
				URL url = null;
				try {				
					String fileString = call.get(1);
					if(fileString.startsWith("http:")) {
						url = new URL(fileString);
					} else {
						File f = new File(baseDir, call.get(1));
						url = f.toURI().toURL();
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (call.size() == 2)
					// falls nichts angegeben, dann wird RDF/XML gewählt
					importedFiles.put(url, OntologyFileFormat.RDF_XML);
				else {
					String formatString = call.get(2);
					OntologyFileFormat format;
					if (formatString.equals("RDF/XML"))
						format = OntologyFileFormat.RDF_XML;
					else
						format = OntologyFileFormat.N_TRIPLES;
					importedFiles.put(url, format);
				}
			} else if(call.get(0).equals("export")) {
				File f = new File(baseDir, call.get(1));
				if (call.size() == 2)
					// falls nichts angegeben, dann wird RDF/XML gewählt
					exportedFiles.put(f, OntologyFileFormat.RDF_XML);
				else {
					String formatString = call.get(2);
					OntologyFileFormat format;
					if (formatString.equals("RDF/XML"))
						format = OntologyFileFormat.RDF_XML;
					else
						format = OntologyFileFormat.N_TRIPLES;
					exportedFiles.put(f, format);
				}
			} else if(call.get(0).equals("loadJarFile")) {
				loadedJars.add(new File(call.get(1)));
			} else if(call.get(0).equals("runPreprocessingModule")) {
				preprocessingModules.add(call.get(1));
			} else {
				System.out.println("Unknown function \"" + call.get(0) + "\". Exiting.");
				System.exit(0);
			}
		}
	}
	
	// Reasoning-Service erstellen
	// TODO: überlegen, ob man die Methode nicht ev. auslagert, da sich Klassenvariablen
	// mit Parametern überlappen
	public static Reasoner createReasoner(KB kb, Map<URL, OntologyFileFormat> importedFiles) {
		Reasoner reasoner = null;
		if (Config.reasonerType == ReasonerType.KAON2) {
			reasoner = new KAON2Reasoner(kb, importedFiles);
			System.out.println("Reasoner: KAON2 (over Java API)");
		} else if (Config.reasonerType == ReasonerType.DIG) {
			reasoner = new DIGReasoner(kb, Config.digReasonerURL, importedFiles);
			System.out.println("Reasoner: " + ((DIGReasoner) reasoner).getIdentifier()
					+ " - connected via DIG 1.1 at " + Config.digReasonerURL);
		} else if (Config.reasonerType == ReasonerType.FAST_RETRIEVAL) {
			// erst KAON2-Reasoner erstellen um FlatABox zu erzeugen
			if (Config.startUpReasoningType == ReasonerType.KAON2) {
				Reasoner startUpReasoner = new KAON2Reasoner(kb, importedFiles);
				// FlatABox abox;
				try {
					abox = createFlatABox(startUpReasoner);
				} catch (ReasoningMethodUnsupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new Error(
							"StartUpReasoner does not support all necessary reasoning methods.");
				}
				reasoner = new FastRetrievalReasoner(abox);
			} else
				throw new Error("Unsupported startup reasoner.");
		} else {
			throw new Error("Unsupported Reasoner");
		}

		// Reasoner.initializeReasoner(ReasonerType.KAON2,kb,importedFiles);
		// ReasoningService reasoner = new ReasoningService(Config.reasonerType,
		// Config.startUpReasoningType, kb, importedFiles);
		// ReasoningService
		// return new ReasoningService(reasoner);
		return reasoner;
	}
	
	/**
	 * Computes the set of allowed concepts based on configuration settings (also
	 * ignores anonymous and standard RDF, RDFS, OWL concept produces by Jena).
	 *
	 */
	public static void autoDetectConceptsAndRoles(ReasoningService rs) {
	// einige Sachen, die momentan nur vom Refinement-Algorithmus
	// unterstützt werden (später ev. auch von anderen Algorithmen)
	//if (Config.algorithm == Algorithm.REFINEMENT) {
		
		// berechnen der verwendbaren Konzepte
		if (Config.Refinement.allowedConceptsAutoDetect) {
			// TODO: Code aus DIG-Reasoner-Klasse einfügen

			Set<AtomicConcept> allowedConceptsTmp = new TreeSet<AtomicConcept>(
					new ConceptComparator());
			allowedConceptsTmp.addAll(rs.getAtomicConcepts());
			Iterator<AtomicConcept> it = allowedConceptsTmp.iterator();
			while (it.hasNext()) {
				String conceptName = it.next().getName();
				// System.out.println(conceptName);
				// seltsame anon-Konzepte, die von Jena erzeugt werden
				// löschen
				if (conceptName.startsWith("anon")) {
					System.out
							.println("  Ignoring concept "
									+ conceptName
									+ " (probably an anonymous concept produced by Jena when reading in OWL file).");
					it.remove();
				} else if (conceptName
						.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#")) {
					System.out
							.println("  Ignoring concept "
									+ conceptName
									+ " (RDF construct produced by Jena when reading in OWL file).");
					it.remove();
				} else if (conceptName
						.startsWith("http://www.w3.org/2000/01/rdf-schema#")) {
					System.out
							.println("  Ignoring concept "
									+ conceptName
									+ " (RDF Schema construct produced by Jena when reading in OWL file).");
					it.remove();
				} else if (conceptName.startsWith("http://www.w3.org/2002/07/owl#")) {
					System.out
							.println("  Ignoring concept "
									+ conceptName
									+ " (OWL construct produced by Jena when reading in OWL file).");
					it.remove();
				}
			}
			
			// hier werden jetzt noch die zu ignorierenden Konzepte entfernt
			if(Config.Refinement.ignoredConcepts != null) {
				
				
				for(AtomicConcept ac : Config.Refinement.ignoredConcepts) {
					boolean success = allowedConceptsTmp.remove(ac);
					if(!success) {
						System.out.println("Ignored concept " + ac + " does not exist in knowledge base.");
						System.exit(0);
					}
						
				}
			}
				
			
			Config.Refinement.allowedConcepts = allowedConceptsTmp;
		} else {
			// prüfen, ob nur verfügbare Konzepte vom Nutzer gewählt worden
			Set<AtomicConcept> allowed = new HashSet<AtomicConcept>();
			allowed.addAll(Config.Refinement.allowedConcepts);
			allowed.removeAll(rs.getAtomicConcepts());
			if (allowed.size() > 0) {
				System.out
						.println("Some of the concepts you told the learner to use in the definition, "
								+ "do not exist in the background knowledge: "
								+ allowed);
				System.out.println("Please correct this problem and restart.");
				System.exit(0);
			}
		}

		if (Config.Refinement.allowedRolesAutoDetect) {
			Set<AtomicRole> allowedRolesTmp = rs.getAtomicRoles();
			
			// hier werden jetzt noch die zu ignorierenden Rollen entfernt
			if(Config.Refinement.ignoredRoles != null) {
				
				
				for(AtomicRole ar : Config.Refinement.ignoredRoles) {
					boolean success = allowedRolesTmp.remove(ar);
					if(!success) {
						System.out.println("Ignored role " + ar + " does not exist in knowledge base.");
						System.exit(0);
					}
						
				}
			}
			
			Config.Refinement.allowedRoles = allowedRolesTmp;
			
		} else {
			Set<AtomicRole> allowedR = new HashSet<AtomicRole>();
			allowedR.addAll(Config.Refinement.allowedRoles);

			Set<AtomicRole> existingR = new TreeSet<AtomicRole>(new RoleComparator());
			existingR.addAll(rs.getAtomicRoles());

			// allowedR.removeAll(rs.getAtomicRoles());
			allowedR.removeAll(existingR);

			if (allowedR.size() > 0) {
				System.out
						.println("Some of the roles you told the learner to use in the definition, "
								+ "do not exist in the background knowledge: "
								+ allowedR);
				System.out.println("Please correct this problem and restart.");
				System.out.println(rs.getAtomicRoles());
				System.out.println(Config.Refinement.allowedRoles);
				System.exit(0);
			}

		}
	}	
	
	// Statistikerzeugung wird ausgelagert, damit im "normalen" Programm
	// nichts ge�ndert werden muss
//	private void createStatistics(LearningProblemNew learningProblem, String baseDir) {
//
//		int runs = 20;
//		LearningAlgorithm alg;
//
//		Stat stat1 = new Stat();
//		Stat stat2 = new Stat();
//		Stat stat3 = new Stat();
//		// Stat stat4 = new Stat();
//		File exportFile1 = new File(baseDir, "../../stats/guesser.data");
//		File exportFile2 = new File(baseDir, "../../stats/gp1.data");
//		File exportFile3 = new File(baseDir, "../../stats/gp2.data");
//		// File exportFile4 = new File(baseDir, "../../stats/gp3.data");
//		StringBuilder exportString1 = new StringBuilder();
//		StringBuilder exportString2 = new StringBuilder();
//		StringBuilder exportString3 = new StringBuilder();
//		// StringBuilder exportString4 = new StringBuilder();
//
//		for (int i = 1000; i <= 30000; i += 2000) {
//			stat1 = new Stat();
//			stat2 = new Stat();
//			stat3 = new Stat();
//			// stat4 = new Stat();
//			for (int run = 0; run < runs; run++) {
//				System.out.println("=============");
//				System.out.println("i " + i + " run " + run);
//				System.out.println("=============");
//				alg = new RandomGuesser(learningProblem, i, 6);
//				stat1.addNumber(alg.getSolutionScore().getScore() - 0.1
//						* alg.getBestSolution().getLength());
//
//				Config.GP.numberOfIndividuals = i / 10;
//				Config.GP.mutationProbability = 0.03f;
//				Config.GP.crossoverProbability = 0.9f;
//				Config.GP.hillClimbingProbability = 0.0f;
//				alg = new GP(learningProblem);
//				stat2.addNumber(alg.getSolutionScore().getScore() - 0.1
//						* alg.getBestSolution().getLength());
//
//				// wie GP 1, aber mit Hill Climbing
//				Config.GP.crossoverProbability = 0.8f;
//				Config.GP.hillClimbingProbability = 0.15f;
//				alg = new GP(learningProblem);
//				stat3.addNumber(alg.getSolutionScore().getScore() - 0.1
//						* alg.getBestSolution().getLength());
//				// stat.addNumber(((GP)alg).fittestIndividualGeneration);
//
//				// wie GP 1, aber mit festem return type
//				/*
//				 * Config.GP.crossoverProbability = 0.85f;
//				 * Config.GP.hillClimbingProbability = 0.0f; Config.returnType =
//				 * "male"; alg = new GP();
//				 * stat4.addNumber(alg.getSolutionScore().getScore()-0.1*alg.getBestSolution().getConceptLength());
//				 * Config.returnType = "";
//				 */
//
//			}
//			exportString1.append(i + " " + stat1.getMean() + " "
//					+ stat1.getStandardDeviation() + "\n");
//			exportString2.append(i + " " + stat2.getMean() + " "
//					+ stat2.getStandardDeviation() + "\n");
//			exportString3.append(i + " " + stat3.getMean() + " "
//					+ stat3.getStandardDeviation() + "\n");
//			// exportString4.append(i + " " + stat4.getMean() + " " +
//			// stat4.getStandardDeviation() + "\n");
//		}
//
//		createFile(exportFile1, exportString1.toString());
//		createFile(exportFile2, exportString2.toString());
//		createFile(exportFile3, exportString3.toString());
//		// createFile(exportFile4, exportString4.toString());
//	}

	// erzeugt Statistiken für MLDM-Paper zur Verarbeitung mit GnuPlot
	// Vorsicht: Laufzeit von mehreren Stunden
	@SuppressWarnings("unused")
	private void createStatisticsMLDMPaper(PosNegDefinitionLP learningProblem, String baseDir) {
		// Algorithmus 1: hybrid GP (100% refinement)
		// Algorithmus 2: 50% refinement, 40% crossover, 1% mutation
		// Algorithmus 3: 80% crossover, 2% mutation

		// Diagramm 1: Prozentzahl richtig klassifiziert
		// Diagramm 2: Konzeptlänge
		// Diagramm 3: Laufzeit

		int runs = 9;
		GP gp;
		int nrOfExamples = learningProblem.getPositiveExamples().size()
				+ learningProblem.getNegativeExamples().size();

		Stat[][] statAr = new Stat[4][3];
		File[][] fileAr = new File[4][3];
		StringBuilder[][] exportString = new StringBuilder[4][3];
		// initialise export strings
		for (int j = 0; j < 4; j++) {
			for (int k = 0; k < 3; k++) {
				exportString[j][k] = new StringBuilder();
			}
		}

		fileAr[0][0] = new File(baseDir, "gnuplot/hybrid100classification.data");
		fileAr[0][1] = new File(baseDir, "gnuplot/hybrid100length.data");
		fileAr[0][2] = new File(baseDir, "gnuplot/hybrid100runtime.data");
		fileAr[1][0] = new File(baseDir, "gnuplot/hybrid50classification.data");
		fileAr[1][1] = new File(baseDir, "gnuplot/hybrid50length.data");
		fileAr[1][2] = new File(baseDir, "gnuplot/hybrid50runtime.data");
		fileAr[2][0] = new File(baseDir, "gnuplot/gpclassification.data");
		fileAr[2][1] = new File(baseDir, "gnuplot/gplength.data");
		fileAr[2][2] = new File(baseDir, "gnuplot/gpruntime.data");

		// Extra-Test
		fileAr[3][0] = new File(baseDir, "gnuplot/extraclassification.data");
		fileAr[3][1] = new File(baseDir, "gnuplot/extralength.data");
		fileAr[3][2] = new File(baseDir, "gnuplot/extraruntime.data");

		ComponentManager cm = ComponentManager.getInstance();
		
		long overallTimeStart = System.nanoTime();

		// allgemeine Einstellungen
		// Config.GP.elitism = true;

		for (int i = 700; i <= 700; i += 100) {
			// initialise statistics array
			for (int j = 0; j < 4; j++) {
				for (int k = 0; k < 3; k++) {
					statAr[j][k] = new Stat();
				}
			}

			for (int run = 0; run < runs; run++) {
				System.out.println("=============");
				System.out.println("i " + i + " run " + run);
				System.out.println("=============");

				// nur ein Test durchlaufen
				for (int j = 0; j < 3; j++) {

					// Reasoner neu erstellen um Speicherprobleme zu vermeiden
					reasoner = new DIGReasoner(kb, Config.digReasonerURL, importedFiles);
					reasoner.prepareSubsumptionHierarchy();
					rs = new ReasoningService(reasoner);
					// learningProblem = new LearningProblem(rs, posExamples, negExamples);
					learningProblem = cm.learningProblem(PosNegDefinitionLP.class, rs);
					cm.applyConfigEntry(learningProblem, "positiveExamples", posExamples);
					cm.applyConfigEntry(learningProblem, "negativeExamples", negExamples);

					if (j == 0) {
						Config.algorithm = Algorithm.HYBRID_GP;
						Config.GP.numberOfIndividuals = i + 1;
						Config.GP.refinementProbability = 0.85;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.05;
						Config.GP.hillClimbingProbability = 0;
					} else if (j == 1) {
						Config.algorithm = Algorithm.HYBRID_GP;
						Config.GP.numberOfIndividuals = i + 1;
						Config.GP.refinementProbability = 0.4;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.4;
						Config.GP.hillClimbingProbability = 0;
					} else if (j == 2) {
						Config.algorithm = Algorithm.GP;
						Config.GP.numberOfIndividuals = i + 1;
						Config.GP.refinementProbability = 0;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.8;
						Config.GP.hillClimbingProbability = 0;
					} else if (j == 3) {
						Config.algorithm = Algorithm.HYBRID_GP;
						Config.GP.numberOfIndividuals = i + 1;
						Config.GP.refinementProbability = 0.7;
						Config.GP.mutationProbability = 0.02;
						Config.GP.crossoverProbability = 0.1;
						Config.GP.hillClimbingProbability = 0;
					}

					algorithmStartTime = System.nanoTime();
					gp = new GP(learningProblem);
					long algorithmTime = System.nanoTime() - algorithmStartTime;
					long algorithmTimeSeconds = algorithmTime / 1000000000;

					// Release, damit Pellet (hoffentlich) Speicher wieder
					// freigibt
					((DIGReasoner) reasoner).releaseKB();

					int conceptLength = gp.getBestSolution().getLength();
					Score bestScore = gp.getSolutionScore();
					int misClassifications = bestScore.getCoveredNegatives().size()
							+ bestScore.getNotCoveredPositives().size();
					double classificationRatePercent = 100 * ((nrOfExamples - misClassifications) / (double) nrOfExamples);

					statAr[j][0].addNumber(classificationRatePercent);
					statAr[j][1].addNumber(conceptLength);
					statAr[j][2].addNumber(algorithmTimeSeconds);

				}
			}

			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 3; k++) {
					exportString[j][k].append(i + " " + statAr[j][k].getMean() + " "
							+ statAr[j][k].getStandardDeviation() + "\n");
				}
			}

			// Daten werden nach jeder Populationserhöhung geschrieben, nicht
			// nur
			// am Ende => man kann den Test also auch zwischendurch abbrechen
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 3; k++) {
					Files.createFile(fileAr[j][k], exportString[j][k].toString());
				}
			}
		}

		long overallTime = System.nanoTime() - overallTimeStart;
		System.out.println("\noverall time: "
				+ Helper.prettyPrintNanoSeconds(overallTime));
	}

	// TODO: query mode umschreiben
	private void processQueryMode(Reasoner reasoner) {

		System.out
				.println("Entering query mode. Enter a concept for performing retrieval or q to quit.");
		String queryStr = "";
		do {

			System.out.print("enter query: "); // String einlesen
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

			// Eingabestring einlesen
			try {
				queryStr = input.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (!queryStr.equals("q")) {

				// Konzept parsen
				Concept concept = null;
				boolean parsedCorrectly = true;

				try {
					concept = KBParser.parseConcept(queryStr);
				} catch (ParseException e1) {
					e1.printStackTrace();
					System.err
							.println("The concept you entered could not be parsed. Please try again.");
					parsedCorrectly = false;
				} catch (TokenMgrError e) {
					e.printStackTrace();
					System.err
							.println("An error occured during parsing. Please enter a syntactically valid concept.");
					parsedCorrectly = false;
				}

				if (parsedCorrectly) {
					// berechne im Konzept vorkommende atomare Rollen und
					// Konzepte
					SortedSet<AtomicConcept> occurringConcepts = new TreeSet<AtomicConcept>(
							new ConceptComparator());
					occurringConcepts.addAll(Helper.getAtomicConcepts(concept));
					SortedSet<AtomicRole> occurringRoles = new TreeSet<AtomicRole>(
							new RoleComparator());
					occurringRoles.addAll(Helper.getAtomicRoles(concept));

					// ziehe davon die existierenden ab => die resultierenden
					// Mengen
					// sollten leer sein, ansonsten Fehler (der DIG-Reasoner
					// fängt das
					// leider nicht selbst ab)
					// => momentan etwas umständlich gelöst, da es in Java bei
					// removeAll darauf
					// ankommt, dass die Argumentmenge den Comparator
					// implementiert hat, was hier
					// (noch) nicht der Fall ist
					for (AtomicConcept ac : rs.getAtomicConcepts())
						occurringConcepts.remove(ac);
					for (AtomicRole ar : rs.getAtomicRoles())
						occurringRoles.remove(ar);

					boolean nonExistingConstructs = false;
					if (occurringConcepts.size() != 0 || occurringRoles.size() != 0) {
						System.out
								.println("You used non-existing atomic concepts or roles. Please correct your query.");
						if (occurringConcepts.size() > 0)
							System.out.println("non-existing concepts: "
									+ occurringConcepts);
						if (occurringRoles.size() > 0)
							System.out.println("non-existing roles: " + occurringRoles);
						nonExistingConstructs = true;
					}

					if (!nonExistingConstructs) {
						// Retrieval stellen
						Set<Individual> result = null;
						try {
							result = reasoner.retrieval(concept);
						} catch (ReasoningMethodUnsupportedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						System.out.println(result);

						Score score = learningProblem.computeScore(concept);
						System.out.println(score);

						// feststellen, was zur Lösung noch fehlt
						// Set<String> notCoveredPositives
					}
				}
			}

		} while (!queryStr.equals("q"));

		/*
		 * String queryStr = "";
		 * 
		 * do {
		 * 
		 * System.out.print("enter query: "); // String einlesen BufferedReader
		 * input = new BufferedReader(new InputStreamReader(System.in));
		 * 
		 * try { queryStr = input.readLine(); // queryStr.trim(); } catch
		 * (IOException e) { e.printStackTrace(); }
		 * 
		 * if (!queryStr.equals("q")) {
		 * 
		 * Predicate queryPredicate = null; boolean parseError = false; boolean
		 * showRole = false; ASTConcept conceptNode = null; // testen, ob es
		 * einem Rollennamen entspricht if (roles.containsKey(queryStr)) {
		 * queryPredicate = roles.get(queryStr); showRole = true; } else { // zu
		 * einer Konzeptbeschreibung vervollst�ndigen queryStr = "query = " +
		 * queryStr + "."; // String parsen SimpleNode queryNode = null; try {
		 * queryNode = DLLearner.parseString(queryStr); conceptNode =
		 * (ASTConcept) queryNode.jjtGetChild(0).jjtGetChild(1); queryPredicate =
		 * parseConcept(conceptNode); } catch (ParseException e) {
		 * System.out.println(e); // e.printStackTrace(); System.out
		 * .println("Exception encountered. Please enter a valid concept
		 * description or a role name."); parseError = true; } catch
		 * (TokenMgrError e2) { System.out.println(e2); System.out
		 * .println("Error encountered. Please enter a valid concept description
		 * or a role name."); parseError = true; } }
		 * 
		 * if (!parseError) { // Kind ermitteln (Kind 0 ist "query", Kind 1 //
		 * "=", Kind // 2 Konzept) // Node conceptNode = //
		 * queryNode.jjtGetChild(0).jjtGetChild(1); // if (conceptNode
		 * instanceof ASTConcept) { // Description d = parseConcept((ASTConcept) // //
		 * conceptNode); // Query stellen List<String> results = new //
		 * LinkedList<String>(); Map<String, List<String>> resultMap = new
		 * HashMap<String, List<String>>(); try { query =
		 * reasoner.createQuery(queryPredicate); Object[] tupleBuffer = null;
		 * 
		 * query.open(); while (!query.afterLast()) { tupleBuffer =
		 * query.tupleBuffer(); // .toString(); if (tupleBuffer.length == 1)
		 * results.add(tupleBuffer[0].toString()); else { String ind1 =
		 * tupleBuffer[0].toString(); String ind2 = tupleBuffer[1].toString();
		 * List<String> l; if (!resultMap.containsKey(ind1)) l = new LinkedList<String>();
		 * else l = resultMap.get(ind1);
		 * 
		 * l.add(ind2); resultMap.put(ind1, l); } query.next(); } // Query an
		 * eigenen Algorithmus stellen if(!showRole) { org.dllearner.dl.Concept
		 * conceptOwn = parseConceptOwn(conceptNode); // Score score =
		 * conceptOwn.computeScore(); Score score = new Score(conceptOwn);
		 * System.out.println(score); System.out.println("positive (own
		 * algorithm): " + score.getDefPosSet()); System.out.println("negative
		 * (own algorithm): " + score.getDefNegSet()); }
		 * 
		 * if (tupleBuffer == null || tupleBuffer.length <= 1) {
		 * System.out.println("KAON2: " + results); } else
		 * System.out.println(resultMap); query.close(); query.dispose(); }
		 * catch (KAON2Exception e) { e.printStackTrace(); } catch
		 * (InterruptedException e) { e.printStackTrace(); } // } else { //
		 * System.out.println("Please enter a concept."); // } } // String
		 * einlesen, String parsen => SimpleNode, falls // einzigstes Kind //
		 * kein Konzept ist, dann Abbruch; falls doch dann parsen // und // //
		 * zur�ckerhaltene // Description an Reasoner geben } } while
		 * (!queryStr.equals("q"));
		 */

		/*
		 * String queryStr = ""; Query query;
		 * 
		 * do {
		 * 
		 * System.out.print("enter query: "); // String einlesen BufferedReader
		 * input = new BufferedReader(new InputStreamReader(System.in));
		 * 
		 * try { queryStr = input.readLine(); // queryStr.trim(); } catch
		 * (IOException e) { e.printStackTrace(); }
		 * 
		 * if (!queryStr.equals("q")) {
		 * 
		 * Predicate queryPredicate = null; boolean parseError = false; boolean
		 * showRole = false; ASTConcept conceptNode = null; // testen, ob es
		 * einem Rollennamen entspricht if (roles.containsKey(queryStr)) {
		 * queryPredicate = roles.get(queryStr); showRole = true; } else { // zu
		 * einer Konzeptbeschreibung vervollst�ndigen queryStr = "query = " +
		 * queryStr + "."; // String parsen SimpleNode queryNode = null; try {
		 * queryNode = DLLearner.parseString(queryStr); conceptNode =
		 * (ASTConcept) queryNode.jjtGetChild(0).jjtGetChild(1); queryPredicate =
		 * parseConcept(conceptNode); } catch (ParseException e) {
		 * System.out.println(e); // e.printStackTrace(); System.out
		 * .println("Exception encountered. Please enter a valid concept
		 * description or a role name."); parseError = true; } catch
		 * (TokenMgrError e2) { System.out.println(e2); System.out
		 * .println("Error encountered. Please enter a valid concept description
		 * or a role name."); parseError = true; } }
		 * 
		 * if (!parseError) { // Kind ermitteln (Kind 0 ist "query", Kind 1 "=",
		 * Kind // 2 Konzept) // Node conceptNode = //
		 * queryNode.jjtGetChild(0).jjtGetChild(1); // if (conceptNode
		 * instanceof ASTConcept) { // Description d = parseConcept((ASTConcept) //
		 * conceptNode); // Query stellen List<String> results = new LinkedList<String>();
		 * Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
		 * try { query = reasoner.createQuery(queryPredicate); Object[]
		 * tupleBuffer = null;
		 * 
		 * query.open(); while (!query.afterLast()) { tupleBuffer =
		 * query.tupleBuffer(); // .toString(); if (tupleBuffer.length == 1)
		 * results.add(tupleBuffer[0].toString()); else { String ind1 =
		 * tupleBuffer[0].toString(); String ind2 = tupleBuffer[1].toString();
		 * List<String> l; if (!resultMap.containsKey(ind1)) l = new LinkedList<String>();
		 * else l = resultMap.get(ind1);
		 * 
		 * l.add(ind2); resultMap.put(ind1, l); } query.next(); } // Query an
		 * eigenen Algorithmus stellen if(!showRole) { org.dllearner.dl.Concept
		 * conceptOwn = parseConceptOwn(conceptNode); // Score score =
		 * conceptOwn.computeScore(); Score score = new Score(conceptOwn);
		 * System.out.println(score); System.out.println("positive (own
		 * algorithm): " + score.getDefPosSet()); System.out.println("negative
		 * (own algorithm): " + score.getDefNegSet()); }
		 * 
		 * if (tupleBuffer == null || tupleBuffer.length <= 1) {
		 * System.out.println("KAON2: " + results); } else
		 * System.out.println(resultMap); query.close(); query.dispose(); }
		 * catch (KAON2Exception e) { e.printStackTrace(); } catch
		 * (InterruptedException e) { e.printStackTrace(); } // } else { //
		 * System.out.println("Please enter a concept."); // } } // String
		 * einlesen, String parsen => SimpleNode, falls // einzigstes Kind //
		 * kein Konzept ist, dann Abbruch; falls doch dann parsen // und //
		 * zur�ckerhaltene // Description an Reasoner geben } } while
		 * (!queryStr.equals("q"));
		 */
	}

	// Funktion wird nicht mehr ben�tigt
	/*
	 * private File searchImports(Node rootNode, String baseDir) { // es wird
	 * zuerst nach einer import-Anweisung gesucht // boolean importKB = false;
	 * File importedFile = null; for (int i = 0; i <
	 * rootNode.jjtGetNumChildren(); i++) { if (rootNode.jjtGetChild(i)
	 * instanceof ASTFunctionCall) { Node functionCall = (ASTFunctionCall)
	 * rootNode.jjtGetChild(i); String function = ((ASTId)
	 * functionCall.jjtGetChild(0)).getId(); if (function.equals("import")) {
	 * String importFile = ""; importFile = ((ASTString)
	 * functionCall.jjtGetChild(1)).getId(); // Anf�hrungszeichen im String
	 * wegschneiden importFile = importFile.substring(1, importFile.length() -
	 * 1); importedFile = new File(baseDir, importFile); // importKB = true; } } }
	 * return importedFile; }
	 */

	// VERALTET
	/*
	 * private void handleReturnType(Ontology ontology) { // es wird eine Regel:
	 * "zielkonzept SUBCLASS r�ckgabetyp" zur Ontologie // hinzugef�gt // TODO:
	 * es muss noch gepr�ft werden was bei Inkosistenz mit pos. bzw. //
	 * negativen Beispiel passiert // Problem: diese Implementierungsvariante
	 * bringt leider nichts, da der // einfache DL-Algorithmus daraus keinen
	 * Nutzen zieht (au�erdem kann ein // Nutzer falls er m�chte einfach so eine
	 * Regel in die Konf.datei // schreiben) FlatABox abox =
	 * FlatABox.getInstance(); String subClass = abox.getTargetConcept() + "
	 * SUBCLASS " + Config.returnType; Description d2 = null; try { d2 =
	 * parseConcept((ASTConcept)
	 * DLLearner.parseString(subClass).jjtGetChild(1)); } catch (ParseException
	 * e) { System.err.println("Cannot parse return type.");
	 * e.printStackTrace(); System.exit(0); } Description d1 =
	 * getConcept(abox.getTargetConcept());
	 * 
	 * List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
	 * changes.add(new OntologyChangeEvent(KAON2Manager.factory().subClassOf(d1,
	 * d2), OntologyChangeEvent.ChangeType.ADD));
	 * 
	 * try { ontology.applyChanges(changes); } catch (KAON2Exception e) {
	 * System.err.println("Error in handling return type " + Config.returnType +
	 * "."); e.printStackTrace(); System.exit(0); } }
	 */

	/*
	 * private static OWLClass getConcept(String name) { if
	 * (!concepts.containsKey(name)) { concepts.put(name,
	 * KAON2Manager.factory().owlClass(name)); } return concepts.get(name); }
	 * 
	 * private static ObjectProperty getRole(String name) { if
	 * (!roles.containsKey(name)) { roles.put(name,
	 * KAON2Manager.factory().objectProperty(name)); } return roles.get(name); }
	 * 
	 * public static Individual getIndividual(String name) { if
	 * (!individuals.containsKey(name)) { individuals.put(name,
	 * KAON2Manager.factory().individual(name)); } return individuals.get(name); }
	 */

	// die Methode muss private bleiben
	private static FlatABox createFlatABox(Reasoner reasoner)
			throws ReasoningMethodUnsupportedException {
		long dematStartTime = System.currentTimeMillis();

		FlatABox aBox = new FlatABox(); // FlatABox.getInstance();
		for (AtomicConcept atomicConcept : reasoner.getAtomicConcepts()) {
			aBox.atomicConceptsPos.put(atomicConcept.getName(), Helper
					.getStringSet(reasoner.retrieval(atomicConcept)));
			Negation negatedAtomicConcept = new Negation(atomicConcept);
			aBox.atomicConceptsNeg.put(atomicConcept.getName(), Helper
					.getStringSet(reasoner.retrieval(negatedAtomicConcept)));
			aBox.concepts.add(atomicConcept.getName());
		}

		for (AtomicRole atomicRole : reasoner.getAtomicRoles()) {
			aBox.rolesPos.put(atomicRole.getName(), Helper.getStringMap(reasoner
					.getRoleMembers(atomicRole)));
			aBox.roles.add(atomicRole.getName());
		}

		aBox.domain = Helper.getStringSet(reasoner.getIndividuals());
		aBox.top = aBox.domain;
		// ab hier keine �nderungen mehr an FlatABox
		aBox.prepare();

		// System.out.println(aBox);

		long dematDuration = System.currentTimeMillis() - dematStartTime;
		System.out.println("OK (" + dematDuration + " ms)");
		return aBox;
	}

	// gibt M�glickeit flat Abox zu bekommen, falls eine erzeugt wurde
	// (wird momentan nur f�r hill climbing ben�tigt und es ist sauberer diese
	// hier
	// zu erzeugen und bekommen als die ReasoningService-Klasse zu hacken)
	public static FlatABox getFlatAbox() {
		return abox;
	}

	// irgendwelche Sachen testen
	@SuppressWarnings("unused")
	private void test(LearningProblem learningProblem) {
		System.out.println("=== starting test method ===");
		// Concept concept = DLLearner.parseConcept("NOT EXISTS hasChild.NOT
		// male");
		// System.out.println("testing: " + concept);
		/*
		 * PsiDown pd = new PsiDown(learningProblem); concept =
		 * ConceptTransformation.transformToMulti(concept);
		 * 
		 * Set<Concept> r = pd.refine(concept); System.out.println(r);
		 */
		/*
		 * Concept result =
		 * ConceptTransformation.transformToNegationNormalForm(concept);
		 * System.out.println(result);
		 * 
		 */

		/*
		 * Concept male = new
		 * AtomicConcept("http://www.csc.liv.ac.uk/~luigi/ontologies/basicFamily#Male");
		 * AtomicRole hasChild = new
		 * AtomicRole("http://www.csc.liv.ac.uk/~luigi/ontologies/basicFamily#hasChild");
		 * Concept exists = new Exists(hasChild, new Top()); MultiConjunction mc =
		 * new MultiConjunction(); mc.addChild(male); mc.addChild(exists);
		 * 
		 * SortedSet<String> result = rs.retrieval(mc); for(String s : result)
		 * System.out.println(s);
		 * 
		 * SortedSet<String> result2 = Helper.intersection(result,
		 * learningProblem.getPositiveExamples());
		 * 
		 * Score score = learningProblem.computeScore(mc);
		 * System.out.println("===");
		 * System.out.println(score.getCoveredPositives());
		 * System.out.println(score.getCoveredNegatives());
		 * 
		 * Concept top = new Top(); RhoDown rd = new RhoDown(learningProblem);
		 * Set<Concept> result3 = rd.refine(top, 3, null); for(Concept c :
		 * result3) System.out.println(c);
		 * 
		 * System.out.println(rs.getMoreSpecialConcepts(top));
		 * 
		 * Concept container = new
		 * AtomicConcept("http://www.w3.org/2000/01/rdf-schema#Resource");
		 * System.out.println(((DIGReasoner)reasoner).getMoreSpecialConceptsDIG(container));
		 */

		/*
		 * for(int i=0; i<10; i++) { Program p =
		 * GPUtilities.createGrowRandomProgram(learningProblem, 3); //
		 * System.out.println("concept: " + p.getTree()); //
		 * System.out.println("fitness: " + p.getFitness()); //
		 * System.out.println("length of concept: " + p.getTree().getLength()); //
		 * System.out.println(p.getScore());
		 * 
		 * System.out.println("##"); boolean ok = GPUtilities.checkProgram(p); //
		 * if(!ok) System.out.println(p.getTree()); }
		 * 
		 * System.out.println("==="); // Concept male = new
		 * AtomicConcept("male"); Concept male =
		 * rs.getAtomicConceptsList().get(0); // Concept male = null; //
		 * for(Concept c : rs.getAtomicConcepts()) // male = c;
		 * GPUtilities.checkTree(male, true); System.out.println(male);
		 * System.out.println(male.getParent());
		 */

		Concept c = null;
		try {
			c = KBParser.parseConcept("EXISTS r.(TOP AND (TOP OR BOTTOM))");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ConceptTransformation.transformToMulti(c);
		// Concept cMod =
		// ConceptTransformation.transformToNegationNormalForm(c);
		Concept cMod = ConceptTransformation.applyEquivalenceRules(c);
		System.out.println(c);
		System.out.println(cMod);

		System.exit(0);
	}

	// generiert sibling aus Forte-Daten
	@SuppressWarnings("unused")
	private void test2(LearningProblem learningProblem) {
		// Set<AtomicRole> roles = reasoner.getAtomicRoles();
		// for(AtomicRole role : roles) {
		// System.out.println(rs.getRoleMembers(role));
		// }
		/*
		 * AtomicRole sibling = new AtomicRole("sibling"); Map<String,SortedSet<String>>
		 * members = rs.getRoleMembers(sibling); for(String name :
		 * members.keySet()) { SortedSet<String> fillers = members.get(name);
		 * for(String filler : fillers) {
		 * System.out.println("sibling("+name+","+filler+")."); } }
		 */
		Concept c = null;
		try {
			c = KBParser.parseConcept("EXISTS uncle.TOP");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Set<Individual> uncles = rs.retrieval(c);
		Set<Individual> inds = rs.getIndividuals();
		for (Individual ind : uncles)
			System.out.println("+isUncle(" + ind + ").");
		inds.removeAll(uncles);
		for (Individual ind : inds)
			System.out.println("-isUncle(" + ind + ").");

		System.exit(0);
	}

	public static long getAlgorithmStartTime() {
		return algorithmStartTime;
	}

	// die Methode soll alle Konzeptzusicherungen und Rollenzusicherungen von
	// Individuen entfernen, die mit diesem Individuum verbunden sind
	@SuppressWarnings("unused")
	private void removeIndividualSubtree(KB kb, Individual individual) {
		System.out.println();
		// erster Schritt: alle verbundenen Individuen finden
		Set<Individual> connectedIndividuals = kb.findRelatedIndividuals(individual);
		System.out.println("connected individuals: " + connectedIndividuals);
		// Individual selbst auch entfernen
		connectedIndividuals.add(individual);

		// zweiter Schritt: entfernen von Rollen- und Konzeptzusicherungen
		Set<AssertionalAxiom> abox = kb.getAbox();
		Iterator<AssertionalAxiom> it = abox.iterator();
		while (it.hasNext()) {
			AssertionalAxiom a = it.next();
			if (a instanceof RoleAssertion) {
				RoleAssertion ra = (RoleAssertion) a;
				if (connectedIndividuals.contains(ra.getIndividual1())
						|| connectedIndividuals.contains(ra.getIndividual2())) {
					System.out.println("remove " + ra);
					it.remove();
				}
			} else if (a instanceof ConceptAssertion) {
				if (connectedIndividuals.contains(((ConceptAssertion) a).getIndividual())) {
					System.out.println("remove " + a);
					it.remove();
				}
			} else
				throw new RuntimeException();
		}

		Set<Individual> inds = kb.findAllIndividuals();
		System.out.println("remaining individuals: " + inds);
		System.out.println();
	}

	public static ConfigurationManager getConfMgr() {
		return confMgr;
	}
	
}
