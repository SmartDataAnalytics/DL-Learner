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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.utilities.URLencodeUTF8;

/**
 * a container for examples
 * used for operations like randomization
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 *
 */
public class Examples {
	private static final Logger logger = Logger.getLogger(Examples.class);

//	private final SortedSet<String> positiveExamples = new TreeSet<String>();
//	private final SortedSet<String> negativeExamples = new TreeSet<String>();
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
	
//	public void remove(Set<String> remove) {
//		for (String string : remove) {
//			positiveExamples.remove(string);
//			negativeExamples.remove(string);
//		}
//	}

	private void _remove(String toBeRemoved ){
		posTrain.remove(toBeRemoved);
		negTrain.remove(toBeRemoved);
		posTest.remove(toBeRemoved);
		negTest.remove(toBeRemoved);
	}
	private void _removeAll(Collection<String> toBeRemoved){
		posTrain.removeAll(toBeRemoved);
		negTrain.removeAll(toBeRemoved);
		posTest.removeAll(toBeRemoved);
		negTest.removeAll(toBeRemoved);
	}
	
	public void addPosTrain(Collection<String> pos) {
		_removeAll(pos);
		posTrain.addAll(pos);
	}
	public void addPosTest(Collection<String> pos) {
		_removeAll(pos);
		posTest.addAll(pos);
	}
	public void addNegTrain(Collection<String> neg) {
		_removeAll(neg);
		negTrain.addAll(neg);
	}
	public void addNegTest(Collection<String> neg) {
		_removeAll(neg);
		negTest.addAll(neg);
	}
	
	
	public void addPosTrain(String pos) {
		_remove(pos);
		posTrain.add(pos);
	}
	public void addPosTest(String pos) {
		_remove(pos);
		posTest.add(pos);
	}
	public void addNegTrain(String neg) {
		_remove(neg);
		negTrain.add(neg);
	}
	public void addNegTest(String neg) {
		_remove(neg);
		negTest.add(neg);
	}

	public boolean checkConsistency() {
		for (String one : posTrain) {
			if (negTrain.contains(one)) {
				logger.error("positve and negative example overlap " + one);
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		String ret =  "Total: " + size();
		double posPercent = posTrain.size() / (double) sizeTotalOfPositives();
		double negPercent = negTrain.size() / (double) sizeTotalOfNegatives();
		ret += "\nPositive: " + posTrain.size() + " | " + posTest.size() + " ("
				+ DecimalFormat.getPercentInstance().format(posPercent) + ")";
		ret += "\nNegative: " + negTrain.size() + " | " + negTest.size() + " ("
				+ DecimalFormat.getPercentInstance().format(negPercent) + ")";

		return ret;
	}
	
	public String toFullString() {

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

		return ret+this.toString();
		
	}

	public void writeExamples(String filename) {
		try {
			FileWriter a = new FileWriter(filename, false);
			
			StringBuffer buffer = new StringBuffer();
			buffer.append("\n\n\n\n\n");
			for (String s : posTrain) {
				a.write("import(\""+URLencodeUTF8.encode(s)+"\");\n");
				buffer.append("+\"" + s + "\"\n");
			}
			for (String s : negTrain) {
				a.write("import(\""+URLencodeUTF8.encode(s)+"\");\n");
				buffer.append("-\"" + s + "\"\n");
			}
		
			a.write(buffer.toString());
			a.flush();
			a.close();
			logger.info("wrote examples to " + filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int size(){
		return posTrain.size()+negTrain.size()+posTest.size()+negTest.size();
	}
	public int sizeTotalOfPositives(){
		return posTrain.size()+posTest.size();
	}
	public int sizeTotalOfNegatives(){
		return negTrain.size()+negTest.size();
	}
	public int sizeOfTrainingSets(){
		return posTrain.size()+negTrain.size();
	}
	
	
	public SortedSet<String> getPositiveExamples() {
		SortedSet<String> total = new TreeSet<String>();
		total.addAll(posTrain);
		total.addAll(posTest);
		return total;
	}

	public SortedSet<String> getNegativeExamples() {
		SortedSet<String> total = new TreeSet<String>();
		total.addAll(negTrain);
		total.addAll(negTest);
		return total;
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
