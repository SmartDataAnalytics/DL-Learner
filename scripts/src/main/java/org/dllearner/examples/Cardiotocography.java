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

public class Cardiotocography {

	private static IRI ontologyIRI = IRI.create("http://dl-learner.org/cardiotocography");
	private static final String fileName = "../test/cardiotocography/files/cardiotocography.txt";
	private static HashMap<String, Integer> firstClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> secondClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> thirdClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> fourthClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> fifthClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> sixthClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> seventhClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> eightClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> ninthClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> tenthClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> normalClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> suspectClass = new HashMap<String, Integer>();
	private static HashMap<String, Integer> pathologicClass = new HashMap<String, Integer>();
	private static List<Axiom> axioms = new LinkedList<Axiom>();

	public static void main(String agrs[]) throws FileNotFoundException, ParseException {
		Scanner input = new Scanner(new File(fileName), "UTF-8");
		File owlFile = new File("../test/cardiotocography/cardiotocography.owl");
		long startTime, duration;
		String time;
		KB kb = new KB();
		String kbString = generateDomainAndRangeForObjectProperties();		
		KB kb2 = KBParser.parseKBFile(kbString);
		kb.addKB(kb2);

		System.out.print("Reading in cardiotocography files ... ");
		startTime = System.nanoTime();

		int i = 0;
		while (input.hasNextLine()) {
			String nextLine = input.next();

			String lb = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(lb + ";", "");

			String ac = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(ac + ";", "");

			String fm = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(fm + ";", "");

			String uc = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(uc + ";", "");

			String dl = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(dl + ";", "");

			String ds = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(ds + ";", "");

			String dp = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(dp + ";", "");

			String astv = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(astv + ";", "");

			String mstv = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(mstv + ";", "");

			String altv = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(altv + ";", "");

			String mltv = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(mltv + ";", "");

			String width = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(width + ";", "");

			String min = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(min + ";", "");
			
			String max = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(max + ";", "");
			
			String nMax = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(nMax + ";", "");
			
			String nZeros = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(nZeros + ";", "");
			
			String mode = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(mode + ";", "");
			
			String mean = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(mean + ";", "");
			
			String median = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(median + ";", "");
			
			String variance = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(variance + ";", "");
			
			String tendency = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(tendency + ";", "");
			
			String fhrClass = nextLine.substring(0, nextLine.indexOf(";"));
			nextLine = nextLine.replaceFirst(fhrClass + ";", "");

			String nsp = nextLine;
			
			List<Axiom> axioms = mapClauses(lb, ac, fm, uc, dl, ds, dp, astv, mstv, altv, mltv, width, min, max, nMax, nZeros, mode, mean, median, variance, tendency, fhrClass, nsp, i);
			for (Axiom axiom : axioms)
				kb.addAxiom(axiom);

			i++;
		}
		int j =0;
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
		File confTrainFile = new File("../test/cardiotocography/train1.conf");
		Files.clearFile(confTrainFile);
		generateConfFile(confTrainFile);
		generateExamples(confTrainFile, firstClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating second conf file ... ");
		File confSecondTrainFile = new File("../test/cardiotocography/train2.conf");
		Files.clearFile(confSecondTrainFile);
		generateConfFile(confSecondTrainFile);
		generateExamples(confSecondTrainFile, secondClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating third conf file ... ");
		File confThirdTrainFile = new File("../test/cardiotocography/train3.conf");
		Files.clearFile(confThirdTrainFile);
		generateConfFile(confThirdTrainFile);
		generateExamples(confThirdTrainFile, thirdClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating fourth conf file ... ");
		File confFourthTrainFile = new File("../test/cardiotocography/train4.conf");
		Files.clearFile(confFourthTrainFile);
		generateConfFile(confFourthTrainFile);
		generateExamples(confFourthTrainFile, fourthClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating fifth conf file ... ");
		File confFifthTrainFile = new File("../test/cardiotocography/train5.conf");
		Files.clearFile(confFifthTrainFile);
		generateConfFile(confFifthTrainFile);
		generateExamples(confFifthTrainFile, fifthClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating sixth conf file ... ");
		File confSixthTrainFile = new File("../test/cardiotocography/train6.conf");
		Files.clearFile(confSixthTrainFile);
		generateConfFile(confSixthTrainFile);
		generateExamples(confSixthTrainFile, sixthClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating seventh conf file ... ");
		File confSeventhTrainFile = new File("../test/cardiotocography/train7.conf");
		Files.clearFile(confSeventhTrainFile);
		generateConfFile(confSeventhTrainFile);
		generateExamples(confSeventhTrainFile, seventhClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating eight conf file ... ");
		File confEightTrainFile = new File("../test/cardiotocography/train8.conf");
		Files.clearFile(confEightTrainFile);
		generateConfFile(confEightTrainFile);
		generateExamples(confEightTrainFile, eightClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating ninth conf file ... ");
		File confNinthTrainFile = new File("../test/cardiotocography/train9.conf");
		Files.clearFile(confNinthTrainFile);
		generateConfFile(confNinthTrainFile);
		generateExamples(confNinthTrainFile, ninthClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating tenth conf file ... ");
		File confTenthTrainFile = new File("../test/cardiotocography/train10.conf");
		Files.clearFile(confTenthTrainFile);
		generateConfFile(confTenthTrainFile);
		generateExamples(confTenthTrainFile, tenthClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating eleventh conf file ... ");
		File confEleventhTrainFile = new File("../test/cardiotocography/train11.conf");
		Files.clearFile(confEleventhTrainFile);
		generateConfFile(confEleventhTrainFile);
		generateExamples(confEleventhTrainFile, normalClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating twelfth conf file ... ");
		File confTwelfthTrainFile = new File("../test/cardiotocography/train12.conf");
		Files.clearFile(confTwelfthTrainFile);
		generateConfFile(confTwelfthTrainFile);
		generateExamples(confTwelfthTrainFile, suspectClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		j++;
		System.out.print("Generating thirteenth conf file ... ");
		File confThirteenthTrainFile = new File("../test/cardiotocography/train13.conf");
		Files.clearFile(confThirteenthTrainFile);
		generateConfFile(confThirteenthTrainFile);
		generateExamples(confThirteenthTrainFile, pathologicClass, j);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		System.out.println("Finished");

	}

	private static List<Axiom> mapClauses(String lb, String ac, String fm, String uc, String dl, String ds, String dp, String astv, String mstv, String altv, String mltv, String width, String min, String max, String nMax, String nZeros, String mode, String mean, String median, String variance, String tendency, String fhrClass, String nsp, int i) {
		ClassAssertionAxiom cmpAxiom = getConceptAssertion("Patient", "Patient"
				+ i);
		axioms.add(cmpAxiom);
		double beats = Double.parseDouble(lb);
		DatatypePropertyAssertion dpa = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasBeatsPerMinute", beats);
		axioms.add(dpa);

		double accelerations = Double.parseDouble(ac);
		DatatypePropertyAssertion dpb = getDoubleDatatypePropertyAssertion("Patient" + i, "hasAccelerationsPerSecond"
				, accelerations);
		axioms.add(dpb);

		double movements = Double.parseDouble(fm);
		DatatypePropertyAssertion blo = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasFetalMovementsPerSecond", movements);
		axioms.add(blo);

		double contractions = Double.parseDouble(uc);
		DatatypePropertyAssertion cho = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasUterineContractionsPerSecond", contractions);
		axioms.add(cho);

		double decelerations = Double.parseDouble(dl);
		DatatypePropertyAssertion mhr = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasLightDecelerationsPerSecond", decelerations);
		axioms.add(mhr);

		double severeDecelerations = Double.parseDouble(ds);
		DatatypePropertyAssertion op = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasSevereDecelerationsPerSecond", severeDecelerations);
		axioms.add(op);

		double prolonguedDecelerations = Double.parseDouble(dp);
		DatatypePropertyAssertion sts = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasProlonguedDecelerationsPerSecond", prolonguedDecelerations);
		axioms.add(sts);

		double shortTermVariability = Double.parseDouble(astv);
		DatatypePropertyAssertion mv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasPercentageOfTimeWithAbnormalShortTermVariability", shortTermVariability);
		axioms.add(mv);
		
		double meanShortTermVariability = Double.parseDouble(mstv);
		DatatypePropertyAssertion av = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasMeanValueOfShortTermVariability", meanShortTermVariability);
		axioms.add(av);
		
		double abnormalLongTermVariability  = Double.parseDouble(altv);
		DatatypePropertyAssertion bv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasPercentageOfTimeWithAbnormalLongTermVariability", abnormalLongTermVariability);
		axioms.add(bv);
		
		double longTermVariability  = Double.parseDouble(mltv);
		DatatypePropertyAssertion cv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasMeanValueOfLongTermVariability", longTermVariability);
		axioms.add(cv);
		
		double FHRHistogram = Double.parseDouble(width);
		DatatypePropertyAssertion dv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasWidthOfFHRHistogram", FHRHistogram);
		axioms.add(dv);
		
		double minHist = Double.parseDouble(min);
		DatatypePropertyAssertion ev = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasMinimumOfFHRHistogram", minHist);
		axioms.add(ev);
		
		double maxHist = Double.parseDouble(max);
		DatatypePropertyAssertion fv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasMaximumOfFHRHistogram", maxHist);
		axioms.add(fv);
		
		double histogramPeaks  = Double.parseDouble(nMax);
		DatatypePropertyAssertion gv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasHistogramPeaks", histogramPeaks);
		axioms.add(gv);
		
		double histogramZeros  = Double.parseDouble(nZeros);
		DatatypePropertyAssertion hv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasHistogramZeros", histogramZeros );
		axioms.add(hv);
		
		double histogramMode  = Double.parseDouble(mode);
		DatatypePropertyAssertion iv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasHistogramMode", histogramMode);
		axioms.add(iv);
		
		double histogramMean  = Double.parseDouble(mean);
		DatatypePropertyAssertion jv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasHistogramMean", histogramMean);
		axioms.add(jv);
		
		double histogramMedian  = Double.parseDouble(median);
		DatatypePropertyAssertion kv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasHistogramMedian", histogramMedian);
		axioms.add(kv);
		
		double histogramVariance = Double.parseDouble(variance);
		DatatypePropertyAssertion lv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasHistogramVariance", histogramVariance);
		axioms.add(lv);
		
		double histogramTendency  = Double.parseDouble(tendency);
		DatatypePropertyAssertion nv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasHistogramTendency", histogramTendency);
		axioms.add(nv);

		int fhrClasses = Integer.parseInt(fhrClass);
		switch(fhrClasses) {
			case 1:
				firstClass.put("Patient" + i, 1);
				secondClass.put("Patient" + i, 0);
				thirdClass.put("Patient" + i, 0);
				fourthClass.put("Patient" + i, 0);
				fifthClass.put("Patient" + i, 0);
				sixthClass.put("Patient" + i, 0);
				seventhClass.put("Patient" + i, 0);
				eightClass.put("Patient" + i, 0);
				ninthClass.put("Patient" + i, 0);
				tenthClass.put("Patient" + i, 0);
				break;
			case 2:
				firstClass.put("Patient" + i, 0);
				secondClass.put("Patient" + i, 1);
				thirdClass.put("Patient" + i, 0);
				fourthClass.put("Patient" + i, 0);
				fifthClass.put("Patient" + i, 0);
				sixthClass.put("Patient" + i, 0);
				seventhClass.put("Patient" + i, 0);
				eightClass.put("Patient" + i, 0);
				ninthClass.put("Patient" + i, 0);
				tenthClass.put("Patient" + i, 0);
				break;
			case 3:
				firstClass.put("Patient" + i, 0);
				secondClass.put("Patient" + i, 0);
				thirdClass.put("Patient" + i, 1);
				fourthClass.put("Patient" + i, 0);
				fifthClass.put("Patient" + i, 0);
				sixthClass.put("Patient" + i, 0);
				seventhClass.put("Patient" + i, 0);
				eightClass.put("Patient" + i, 0);
				ninthClass.put("Patient" + i, 0);
				tenthClass.put("Patient" + i, 0);
				break;
			case 4:
				firstClass.put("Patient" + i, 0);
				secondClass.put("Patient" + i, 0);
				thirdClass.put("Patient" + i, 0);
				fourthClass.put("Patient" + i, 1);
				fifthClass.put("Patient" + i, 0);
				sixthClass.put("Patient" + i, 0);
				seventhClass.put("Patient" + i, 0);
				eightClass.put("Patient" + i, 0);
				ninthClass.put("Patient" + i, 0);
				tenthClass.put("Patient" + i, 0);
				break;
			case 5:
				firstClass.put("Patient" + i, 0);
				secondClass.put("Patient" + i, 0);
				thirdClass.put("Patient" + i, 0);
				fourthClass.put("Patient" + i, 0);
				fifthClass.put("Patient" + i, 1);
				sixthClass.put("Patient" + i, 0);
				seventhClass.put("Patient" + i, 0);
				eightClass.put("Patient" + i, 0);
				ninthClass.put("Patient" + i, 0);
				tenthClass.put("Patient" + i, 0);
				break;
			case 6:
				firstClass.put("Patient" + i, 0);
				secondClass.put("Patient" + i, 0);
				thirdClass.put("Patient" + i, 0);
				fourthClass.put("Patient" + i, 0);
				fifthClass.put("Patient" + i, 0);
				sixthClass.put("Patient" + i, 1);
				seventhClass.put("Patient" + i, 0);
				eightClass.put("Patient" + i, 0);
				ninthClass.put("Patient" + i, 0);
				tenthClass.put("Patient" + i, 0);
				break;
			case 7:
				firstClass.put("Patient" + i, 0);
				secondClass.put("Patient" + i, 0);
				thirdClass.put("Patient" + i, 0);
				fourthClass.put("Patient" + i, 0);
				fifthClass.put("Patient" + i, 0);
				sixthClass.put("Patient" + i, 0);
				seventhClass.put("Patient" + i, 1);
				eightClass.put("Patient" + i, 0);
				ninthClass.put("Patient" + i, 0);
				tenthClass.put("Patient" + i, 0);
				break;
			case 8:
				firstClass.put("Patient" + i, 0);
				secondClass.put("Patient" + i, 0);
				thirdClass.put("Patient" + i, 0);
				fourthClass.put("Patient" + i, 0);
				fifthClass.put("Patient" + i, 0);
				sixthClass.put("Patient" + i, 0);
				seventhClass.put("Patient" + i, 0);
				eightClass.put("Patient" + i, 1);
				ninthClass.put("Patient" + i, 0);
				tenthClass.put("Patient" + i, 0);
				break;
			case 9:
				firstClass.put("Patient" + i, 0);
				secondClass.put("Patient" + i, 0);
				thirdClass.put("Patient" + i, 0);
				fourthClass.put("Patient" + i, 0);
				fifthClass.put("Patient" + i, 0);
				sixthClass.put("Patient" + i, 0);
				seventhClass.put("Patient" + i, 0);
				eightClass.put("Patient" + i, 0);
				ninthClass.put("Patient" + i, 1);
				tenthClass.put("Patient" + i, 0);
				break;
			case 10:
				firstClass.put("Patient" + i, 0);
				secondClass.put("Patient" + i, 0);
				thirdClass.put("Patient" + i, 0);
				fourthClass.put("Patient" + i, 0);
				fifthClass.put("Patient" + i, 0);
				sixthClass.put("Patient" + i, 0);
				seventhClass.put("Patient" + i, 0);
				eightClass.put("Patient" + i, 0);
				ninthClass.put("Patient" + i, 0);
				tenthClass.put("Patient" + i, 1);
				break;
		}
		
		int nspClasses = Integer.parseInt(nsp);
		
		switch(nspClasses) {
		case 1:
			normalClass.put("Patient" + i, 1);
			suspectClass.put("Patient" + i, 0);
			pathologicClass.put("Patient" + i, 0);
			break;
		case 2:
			normalClass.put("Patient" + i, 0);
			suspectClass.put("Patient" + i, 1);
			pathologicClass.put("Patient" + i, 0);
			break;
		case 3:
			normalClass.put("Patient" + i, 0);
			suspectClass.put("Patient" + i, 0);
			pathologicClass.put("Patient" + i, 1);
			break;
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
		String confHeader = "import(\"cardiotocography.owl\");\n\n";
		confHeader += "reasoner = fastInstanceChecker;\n";
		confHeader += "algorithm = refexamples;\n";
		confHeader += "refexamples.noisePercentage = 15;\n";
		confHeader += "refexamples.startClass = " + getURI2("Patient") + ";\n";
		confHeader += "refexamples.writeSearchTree = false;\n";
		confHeader += "refexamples.searchTreeFile = \"log/cardiotocography/searchTree.log\";\n";
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
		String kbString = "DPDOMAIN(" + getURI2("hasBeatsPerMinute") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasBeatsPerMinute") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasAccelerationsPerSecond") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasAccelerationsPerSecond") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasFetalMovementsPerSecond") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasFetalMovementsPerSecond") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasUterineContractionsPerSecond") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasUterineContractionsPerSecond") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasLightDecelerationsPerSecond") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasLightDecelerationsPerSecond") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasSevereDecelerationsPerSecond") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasSevereDecelerationsPerSecond") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasProlonguedDecelerationsPerSecond") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasProlonguedDecelerationsPerSecond") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasPercentageOfTimeWithAbnormalShortTermVariability") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasPercentageOfTimeWithAbnormalShortTermVariability") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMeanValueOfShortTermVariability") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMeanValueOfShortTermVariability") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasPercentageOfTimeWithAbnormalLongTermVariability") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasPercentageOfTimeWithAbnormalLongTermVariability") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMeanValueOfLongTermVariability") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMeanValueOfLongTermVariability") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasWidthOfFHRHistogram") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasWidthOfFHRHistogram") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMinimumOfFHRHistogram") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMinimumOfFHRHistogram") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMaximumOfFHRHistogram") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMaximumOfFHRHistogram") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasHistogramPeaks") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasHistogramPeaks") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasHistogramZeros") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasHistogramZeros") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasHistogramMode") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasHistogramMode") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasHistogramMean") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasHistogramMean") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasHistogramMedian") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasHistogramMedian") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasHistogramVariance") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasHistogramVariance") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasHistogramTendency") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasHistogramTendency") + ") = DOUBLE.\n";
		
		return kbString;
		
	}

}
