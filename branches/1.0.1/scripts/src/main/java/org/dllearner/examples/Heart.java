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
import org.dllearner.core.owl.BooleanDatatypePropertyAssertion;
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

public class Heart {

	private static IRI ontologyIRI = IRI.create("http://dl-learner.org/heart");
	private static final String fileName = "test/heart/files/heart.dat";
	private static HashMap<String, Integer> patients = new HashMap<String, Integer>();
	private static List<Axiom> axioms = new LinkedList<Axiom>();
	private static Set<String> thals = new TreeSet<String>();

	public static void main(String agrs[]) throws FileNotFoundException, ParseException {
		Scanner input = new Scanner(new File(fileName), "UTF-8");
		File owlFile = new File("test/heart/heart.owl");
		long startTime, duration;
		String time;
		mapThalValues();
		KB kb = new KB();
		kb.addKB(generateDomainAndRangeForObjectProperties());
		NamedClass atomClass = getAtomicConcept("Thals");
		for (String thal : thals) {
			NamedClass elClass = getAtomicConcept(thal);
			SubClassAxiom sc = new SubClassAxiom(elClass, atomClass);
			kb.addAxiom(sc);
		}

		System.out.print("Reading in heart files ... ");
		startTime = System.nanoTime();

		int i = 0;
		while (input.hasNextLine()) {
			String nextLine = input.next();

			String age = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(age + ",", "");

			String sex = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(sex + ",", "");

			String chestPain = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(chestPain + ",", "");

			String bloodPressure = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(bloodPressure + ",", "");

			String cholestoral = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(cholestoral + ",", "");

			String bloodSugar = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(bloodSugar + ",", "");

			String electrocardiographicResults = nextLine.substring(0,
					nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(electrocardiographicResults + ",",
					"");

			String maximumHeartRate = nextLine.substring(0,
					nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(maximumHeartRate + ",", "");

			String angina = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(angina + ",", "");

			String oldpeak = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(oldpeak + ",", "");

			String stSegment = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(stSegment + ",", "");

			String majorVessels = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(majorVessels + ",", "");

			String thal = nextLine.substring(0, nextLine.indexOf(","));
			nextLine = nextLine.replaceFirst(thal + ",", "");

			String heartDisease = nextLine;
			// System.out.println("age: "+ age + " sex: " + sex + " 1: " +
			// chestPain + " 2: "+ bloodPressure+" 3: "+cholestoral+" 4: "+
			// bloodSugar+" 5: "+ electrocardiographicResults+" 6: "+
			// maximumHeartRate+" 7: "+ angina+" 8: " + oldpeak+ " 9: "+
			// stSegment+" 10: "+majorVessels+" 11: "+ thal+" 12: "+
			// heartDisease+ " 13: "+i);
			List<Axiom> axioms = mapClauses(age, sex, chestPain, bloodPressure,
					cholestoral, bloodSugar, electrocardiographicResults,
					maximumHeartRate, angina, oldpeak, stSegment, majorVessels,
					thal, heartDisease, i);
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
		File confTrainFile = new File("test/heart/train.conf");
		Files.clearFile(confTrainFile);
		generateConfFile(confTrainFile);
		generateExamples(confTrainFile);
		duration = System.nanoTime() - startTime;
		time = Helper.prettyPrintNanoSeconds(duration, false, false);
		System.out.println("OK (" + time + ").");
		System.out.println("Finished");

	}

	private static List<Axiom> mapClauses(String age, String sex,
			String chestPain, String bloodPressure, String cholestoral,
			String bloodSugar, String electrocardiographicResults,
			String maximumHeartRate, String angina, String oldPeak,
			String stSegment, String majorVessels, String thal,
			String heartDisease, int i) {

		ClassAssertionAxiom cmpAxiom = getConceptAssertion("Patient", "Patient"
				+ i);
		axioms.add(cmpAxiom);

		double ages = Double.parseDouble(age);

		DatatypePropertyAssertion dpa = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasAge", ages);
		axioms.add(dpa);

		double gender = Double.parseDouble(sex);

		if (gender == 0.0) {

			ClassAssertionAxiom maAxiom = getConceptAssertion("Gender", "male");
			axioms.add(maAxiom);
			ObjectPropertyAssertion sa = getRoleAssertion("hasGender",
					"Patient" + i, "male");
			axioms.add(sa);
		} else {
			ClassAssertionAxiom maAxiom = getConceptAssertion("Gender",
					"female");
			axioms.add(maAxiom);
			ObjectPropertyAssertion sa = getRoleAssertion("hasGender",
					"Patient" + i, "female");
			axioms.add(sa);
		}

		ObjectPropertyAssertion dpb = getRoleAssertion("hasChestPain",
				"Patient" + i, chestPain);
		axioms.add(dpb);

		double blood = Double.parseDouble(bloodPressure);

		DatatypePropertyAssertion blo = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasBloodPressure", blood);
		axioms.add(blo);

		double cholester = Double.parseDouble(cholestoral);

		DatatypePropertyAssertion cho = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasCholestoral", cholester);
		axioms.add(cho);

		double cholest = Double.parseDouble(cholestoral);
		if (cholest == 0.0) {
			BooleanDatatypePropertyAssertion ames = getBooleanDatatypePropertyAssertion(
					"Patient" + i, "hasBloodSugarOver120mg/dl", false);
			axioms.add(ames);
		} else {
			BooleanDatatypePropertyAssertion ames = getBooleanDatatypePropertyAssertion(
					"Patient" + i, "hasBloodSugarOver120mg/dl", true);
			axioms.add(ames);
		}

		ObjectPropertyAssertion ddb = getRoleAssertion(
				"hasElectrocardiographicResults", "Patient" + i,
				electrocardiographicResults);
		axioms.add(ddb);

		double maxHeart = Double.parseDouble(maximumHeartRate);

		DatatypePropertyAssertion mhr = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasMaximumHeartRate", maxHeart);
		axioms.add(mhr);

		double ang = Double.parseDouble(angina);
		if (ang == 0.0) {
			BooleanDatatypePropertyAssertion ames = getBooleanDatatypePropertyAssertion(
					"Patient" + i, "hasAngina", false);
			axioms.add(ames);
		} else {
			BooleanDatatypePropertyAssertion ames = getBooleanDatatypePropertyAssertion(
					"Patient" + i, "hasAngina", true);
			axioms.add(ames);
		}

		double oldP = Double.parseDouble(oldPeak);

		DatatypePropertyAssertion op = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasOldPeak", oldP);
		axioms.add(op);

		double stS = Double.parseDouble(stSegment);

		DatatypePropertyAssertion sts = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasStSegment", stS);
		axioms.add(sts);

		double majorV = Double.parseDouble(majorVessels);

		DatatypePropertyAssertion mv = getDoubleDatatypePropertyAssertion(
				"Patient" + i, "hasMajorVessels", majorV);
		axioms.add(mv);
		double th = Double.parseDouble(thal);
		if (th == 3.0) {
			ObjectPropertyAssertion tha = getRoleAssertion("hasThalValue",
					"Patient" + i, "normal");
			axioms.add(tha);
			ClassAssertionAxiom maAxiom = getConceptAssertion("normal",
					"normal");
			axioms.add(maAxiom);

		} else if (th == 6.0) {
			ObjectPropertyAssertion thb = getRoleAssertion("hasThalValue",
					"Patient" + i, "fixed-defect");
			axioms.add(thb);
			ClassAssertionAxiom maAxiom = getConceptAssertion("fixed-defect",
					"fixed-defect");
			axioms.add(maAxiom);
		} else if (th == 7.0) {
			ObjectPropertyAssertion thc = getRoleAssertion("hasThalValue",
					"Patient" + i, "reversable-defect");
			axioms.add(thc);
			ClassAssertionAxiom maAxiom = getConceptAssertion(
					"reversable-defect", "reversable-defect");
			axioms.add(maAxiom);
		}
		patients.put("Patient" + i, new Integer(heartDisease));

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
		String confHeader = "import(\"heart.owl\");\n\n";
		confHeader += "reasoner = fastInstanceChecker;\n";
		confHeader += "algorithm = refexamples;\n";
		confHeader += "refexamples.noisePercentage = 15;\n";
		confHeader += "refexamples.startClass = " + getURI2("Patient") + ";\n";
		confHeader += "refexamples.writeSearchTree = false;\n";
		confHeader += "refexamples.searchTreeFile = \"log/heart/searchTree.log\";\n";
		confHeader += "\n";
		Files.appendFile(file, confHeader);
	}

	private static void generateExamples(File file) {
		StringBuffer content = new StringBuffer();
		Set<String> keys = patients.keySet();
		for (String key : keys) {
			Integer subsValue = patients.get(key);
			if (subsValue == 2) {
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

	private static DoubleDatatypePropertyAssertion getDoubleDatatypePropertyAssertion(
			String individual, String datatypeProperty, double value) {
		Individual ind = getIndividual(individual);
		DatatypeProperty dp = getDatatypeProperty(datatypeProperty);
		return new DoubleDatatypePropertyAssertion(dp, ind, value);
	}

	private static DatatypeProperty getDatatypeProperty(String name) {
		return new DatatypeProperty(ontologyIRI + "#" + name);
	}

	private static BooleanDatatypePropertyAssertion getBooleanDatatypePropertyAssertion(
			String individual, String datatypeProperty, boolean value) {
		Individual ind = getIndividual(individual);
		DatatypeProperty dp = getDatatypeProperty(datatypeProperty);
		return new BooleanDatatypePropertyAssertion(dp, ind, value);
	}

	private static void mapThalValues() {
		thals.add("normal");
		thals.add("fixed-defect");
		thals.add("reversable-defect");
	}

	private static KB generateDomainAndRangeForObjectProperties() throws ParseException {
		String kbString = "OPDOMAIN(" + getURI2("hasThalValue") + ") = "
				+ getURI2("Patient") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasThalValue") + ") = " + getURI2("Thals")
				+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("hasGender") + ") = "
				+ getURI2("Patient") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasGender") + ") = " + getURI2("Gender")
				+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("hasChestPain") + ") = " + getURI2("Patient")
				+ ".\n";
		kbString += "OPRANGE(" + getURI2("hasChestPain") + ") = " + getURI2("Thing")
				+ ".\n";
		
		kbString += "OPDOMAIN(" + getURI2("hasElectrocardiographicResults") + ") = "
				+ getURI2("Patient") + ".\n";
		kbString += "OPRANGE(" + getURI2("hasElectrocardiographicResults") + ") = "
		
				+ getURI2("Thing") + ".\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMajorVessels") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMajorVessels") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasBloodPressure") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasBloodPressure") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasBloodSugarOver120mg/dl") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasBloodSugarOver120mg/dl") + ") = BOOLEAN.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasAngina") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasAngina") + ") = BOOLEAN.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasAge") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasAge") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasMaximumHeartRate") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasMaximumHeartRate") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasCholestoral") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasCholestoral") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasOldPeak") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasOldPeak") + ") = DOUBLE.\n";
		
		kbString += "DPDOMAIN(" + getURI2("hasStSegment") + ") = "
		+ getURI2("Patient") + ".\n";
		kbString += "DPRANGE(" + getURI2("hasStSegment") + ") = DOUBLE.\n";
		
		KB kb2 = KBParser.parseKBFile(kbString);
		return kb2;
		
	}

}
