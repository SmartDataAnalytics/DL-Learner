package org.dllearner.algorithm.tbsl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatchingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String nep = "World";
		String s = "Who/WP developed/VBD the/DT video/NN game/NN World/NN of/IN Warcraft/NNP";
		
		Pattern p = Pattern.compile("(\\s)?(" + nep + "/([A-Z]+))(\\s)?");
		Matcher m = p.matcher(s);
		while (m.find()) {
			System.out.println("Found! " + m.group(2));
			s = s.replaceFirst(m.group(2),nep+"/NNP");
		}
		
		System.out.println(s);
	}

}
