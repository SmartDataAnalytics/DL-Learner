package org.dllearner.algorithm.tbsl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatchingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String s = "how/WRB many/JJ software/NN companies/NN are/VBP located/VBN in/IN New/NNP York/NNP";
		
		Pattern nprepPattern = Pattern.compile("\\s((\\w+)/NN[S]?\\s(\\w+))/NN[S]?");
		Matcher m = nprepPattern.matcher(s);
		while (m.find()) {
			System.out.println("Found!");
			s = s.replaceFirst(m.group(1),m.group(2) + "_" + m.group(3));
		}
		
		System.out.println(s);
	}

}
