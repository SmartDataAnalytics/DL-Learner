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
package org.dllearner.utilities.examples;

import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * a container for examples
 * used for operations like randomization
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 *
 */
public class Examples {
	private static final Logger logger = Logger.getLogger(Examples.class);

	private final SortedSet<String> positiveExamples = new TreeSet<String>();
	private final SortedSet<String> negativeExamples = new TreeSet<String>();
	private final SortedSet<String> posTrain = new TreeSet<String>();
	private final SortedSet<String> negTrain = new TreeSet<String>();
	private final SortedSet<String> posTest = new TreeSet<String>();
	private final SortedSet<String> negTest = new TreeSet<String>();

	public Examples() { }
	
	public Examples(SortedSet<String> posTrain , SortedSet<String> negTrain, SortedSet<String> posTest, SortedSet<String> negTest){
		this.addPosTrain(posTrain);
		this.addPosTest(posTest);
		this.addNegTrain(negTrain);
		this.addNegTest(negTest);
	}
	
	public void remove(Set<String> remove) {
		for (String string : remove) {
			positiveExamples.remove(string);
			negativeExamples.remove(string);
		}
	}

	public void addPosTrain(Collection<String> pos) {
		positiveExamples.addAll(pos);
		posTrain.addAll(pos);
	}

	public void addPosTrain(String pos) {
		positiveExamples.add(pos);
		posTrain.add(pos);
	}

	public void addPosTest(Collection<String> pos) {
		positiveExamples.addAll(pos);
		posTest.addAll(pos);
	}

	public void addPosTest(String pos) {
		positiveExamples.add(pos);
		posTest.add(pos);
	}

	public void addNegTrain(Collection<String> neg) {
		negativeExamples.addAll(neg);
		negTrain.addAll(neg);
	}

	public void addNegTrain(String neg) {
		negativeExamples.add(neg);
		negTrain.add(neg);
	}

	public void addNegTest(Collection<String> neg) {
		negativeExamples.addAll(neg);
		negTest.addAll(neg);
	}

	public void addNegTest(String neg) {
		negativeExamples.add(neg);
		negTest.add(neg);
	}

	public boolean checkConsistency() {
		for (String one : positiveExamples) {
			if (negativeExamples.contains(one)) {
				logger.error("positve and negative example overlap " + one);
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {

		int total = (positiveExamples.size() + negativeExamples.size());

		String ret = "Training:\n";
		for (String one : posTrain) {
			ret += "+" + one + "\n";
		}
		for (String one : negTrain) {
			ret += "-" + one + "\n";
		}
		ret += "Testing:\n";
		for (String one : posTest) {
			ret += "+" + one + "\n";
		}
		for (String one : negTest) {
			ret += "-" + one + "\n";
		}

		ret += "\nTotal: " + total;
		double posPercent = posTrain.size() / (double) positiveExamples.size();
		double negPercent = negTrain.size() / (double) negativeExamples.size();
		ret += "\nPositive: " + posTrain.size() + " | " + posTest.size() + " ("
				+ DecimalFormat.getPercentInstance().format(posPercent) + ")";
		ret += "\nNegative: " + negTrain.size() + " | " + negTest.size() + " ("
				+ DecimalFormat.getPercentInstance().format(negPercent) + ")";

		return ret;
	}

	public void writeExamples(String filename) {
		try {
			FileWriter a = new FileWriter(filename, false);
			for (String s : posTrain) {
				a.write("+\"" + s + "\"\n");
			}
			for (String s : negTrain) {
				a.write("-\"" + s + "\"\n");
			}
			a.flush();
			a.close();
			logger.info("wrote examples to " + filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SortedSet<String> getPositiveExamples() {
		return positiveExamples;
	}

	public SortedSet<String> getNegativeExamples() {
		return negativeExamples;
	}

	public SortedSet<String> getPosTrain() {
		return posTrain;
	}

	public SortedSet<String> getNegTrain() {
		return negTrain;
	}

	public SortedSet<String> getPosTest() {
		return posTest;
	}

	public SortedSet<String> getNegTest() {
		return negTest;
	}

}
