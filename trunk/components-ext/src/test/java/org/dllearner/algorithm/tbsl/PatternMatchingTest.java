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
		String s = "is/VBZ there/RB a/DT video/NN game/NN called/VBN Battle/NNP Chess/NNP";
		
		Pattern p = Pattern.compile("(((is)|(are)|(was)|(were))/VB[A-Z]?.((.+)\\s\\w+)/VB(N|D))(?<!is/VBZ there/RB.+/VB(N|D))");
		Matcher m = p.matcher(s);
		while (m.find()) {
			System.out.println("Found! " + m.group(1) + "  m.group(7): " + m.group(7));
			s = s.replaceFirst(m.group(1),m.group(7)+"/NNP");
		}
		
		System.out.println(s);
	}

}
