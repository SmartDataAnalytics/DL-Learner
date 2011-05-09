package org.dllearner.algorithm.tbsl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatchingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String s = "New/NNP York/NNP City/NNP is/VBZ a/DT US/NNP state/NN";
		
		Pattern nprepPattern = Pattern.compile("\\s?((\\w+)/NNP[S]?)\\s(\\w+)/NN[S]?(\\W|$)");
		Matcher m = nprepPattern.matcher(s);
		while (m.find()) {
			System.out.println("Found!");
			s = s.replaceFirst(m.group(1),m.group(2) + "/JJ");
		}
		
		System.out.println(s);
	}

}
