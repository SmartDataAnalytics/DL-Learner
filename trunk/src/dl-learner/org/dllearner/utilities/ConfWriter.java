package org.dllearner.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.Individual;

public class ConfWriter {

	StringBuffer stats = new StringBuffer("/**STATS:\n");

	public String workingDir = "examples/stest/";

	public void writeROLLearnerOWLFileInd(String filename, SortedSet<Individual> pos,
			SortedSet<Individual> neg, String owlFile, SortedSet<String> ignoredConcepts) {

		writeROLLearnerOWLFile(filename, toString(pos), toString(neg), owlFile, ignoredConcepts);
	}

	public void writeROLLearnerOWLFile(String filename, SortedSet<String> pos,
			SortedSet<String> neg, String owlFile, SortedSet<String> ignoredConcepts) {

		String prefix = "refexamples";
		StringBuffer buf = new StringBuffer();
		buf.append("import(\"" + owlFile + "\");\n\n");

		buf.append(getIgnoredConcepts(ignoredConcepts, prefix));

		buf.append(getStandard(prefix));

		buf.append(getPosAndNeg(pos, neg));

		stats.append(ignoredConcepts + "\n");
		stats.append("No of positive Examples: " + pos.size() + "\n");
		stats.append("No of negative Examples: " + neg.size() + "\n");
		stats.append("\n**/\n\n");

		writeToFile(filename, stats.toString() + buf.toString());

	}

	public void writeSPARQL(String filename, SortedSet<String> pos, SortedSet<String> neg,
			String uri, SortedSet<String> ignoredConcepts) {

		String prefixAlgortihm = "refexamples";
		String prefixSparql = "sparql";

		String standardSettings = 
			"sparql.recursionDepth = 1;\n" +
			"sparql.predefinedFilter = 1;\n" + 
			"sparql.predefinedEndpoint = 1;\n" +
			"refexamples.minExecutionTimeInSeconds = 10;\n" +
			"refexamples.maxExecutionTimeInSeconds = 10;\n" +
			"refexamples.logLevel=\"TRACE\";\n" ;


		// "sparql.format = \"KB\";\n\n";

		StringBuffer buf = new StringBuffer();
		buf.append("import(\"" + uri + "\",\"SPARQL\");\n\n");

		buf.append(standardSettings);
		buf.append(getIgnoredConcepts(ignoredConcepts, prefixAlgortihm));
		buf.append(getStandard(prefixAlgortihm));
		buf.append(sparqlInstances(pos, neg, prefixSparql));
		buf.append(getPosAndNeg(pos, neg));

		stats.append(ignoredConcepts + "\n");
		stats.append("No of positive Examples: " + pos.size() + "\n");
		stats.append("No of negative Examples: " + neg.size() + "\n");
		stats.append("\n**/\n\n");

		writeToFile(filename, stats.toString() + buf.toString());

	}

	public String sparqlInstances(SortedSet<String> pos, SortedSet<String> neg, String prefix) {
		SortedSet<String> ret = new TreeSet<String>();
		ret.addAll(pos);
		ret.addAll(neg);

		return getStringSet(ret, prefix, "instances");

	}

	public String getPosAndNeg(SortedSet<String> pos, SortedSet<String> neg) {
		StringBuffer buf = new StringBuffer();
		buf.append("\n\n");
		for (String individuals : pos) {
			buf.append("+\"" + individuals + "\"\n");

		}
		buf.append("\n\n");
		for (String individuals : neg) {

			buf.append("-\"" + individuals + "\"\n");
		}

		return buf.toString();
	}

	public String getIgnoredConcepts(SortedSet<String> ignoredConcepts, String prefix) {

		return getStringSet(ignoredConcepts, prefix, "ignoredConcepts");
	}

	public String getStringSet(SortedSet<String> set, String prefix, String type) {
		if (set.size() == 0)
			return "\n";
		String ret = prefix + "." + type + "={\n";
		int x = 0;
		for (String string : set) {
			if (x > 0)
				ret += ",";
			ret += "\"" + string + "\"\n";
			x++;
		}
		ret += "};\n";
		return ret;
	}

	public String getStandard(String prefix) {
		String ret = "algorithm = " + prefix + ";\n" + "reasoner=fastInstanceChecker;\n\n" +

		prefix + ".useAllConstructor = false;\n" + prefix + ".useExistsConstructor = true;\n"
				+ prefix + ".useCardinalityRestrictions = false;\n" + prefix
				+ ".useNegation = false;\n";

		return ret;

	}

	protected void writeToFile(String filename, String content) {
		// create the file we want to use
		File file = new File(workingDir + filename);

		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(workingDir + filename, false);
			// ObjectOutputStream o = new ObjectOutputStream(fos);
			fos.write(content.getBytes());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected SortedSet<String> toString(SortedSet<Individual> set) {
		SortedSet<String> ret = new TreeSet<String>();
		for (Individual ind : set) {
			ret.add(ind.toString());
		}
		return ret;
	}

	public void addToStats(String add) {
		stats.append(add);
	}

}
