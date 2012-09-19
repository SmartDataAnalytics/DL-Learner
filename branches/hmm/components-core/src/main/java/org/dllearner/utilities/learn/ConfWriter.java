/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.utilities.learn;

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
			String uri, SortedSet<String> ignoredConcepts, String standardSettings, String prefixAlgortihm) {

	
		String prefixSparql = "sparql";

		


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
	
	
	public static String listExamples (boolean posOrNeg, SortedSet<Individual> s ){
		StringBuffer sbuf = new StringBuffer();
		String sign = (posOrNeg)?"+":"-";
		for (Individual individual : s) {
			sbuf.append(sign+"\""+individual+"\"\n");
		}
		
		return sbuf.toString();
	}
	

	public void addToStats(String add) {
		stats.append(add);
	}

}
