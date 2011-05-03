package org.dllearner.algorithm.tbsl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatchingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String s = "was/VBD developed/VBN by/IN";
		
		Pattern nprepPattern = Pattern.compile("(((is)|(are)|(was)|(were))/VB[A-Z]?.(\\w+)/VBN.by/IN)");
		Matcher m = nprepPattern.matcher(s);
		while (m.find()) {
			System.out.println("Found!");
			s = s.replaceFirst(m.group(1),m.group(7)+"/PASSIVE");
		}
		
		System.out.println(s);
	}

}
