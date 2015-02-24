/**
 * 
 */
package org.dllearner.utilities;

import org.semanticweb.owlapi.model.EntityType;

/**
 * @author Lorenz Buehmann
 *
 */
public class OWLAPIUtils {
	
	public static String getPrintName(EntityType entityType) {
		String str = entityType.getName();
		
        char[] c = str.toCharArray();
        
        String printName = "";
        
        int tokenStart = 0;
        int currentType = Character.getType(c[tokenStart]);
        for (int pos = tokenStart + 1; pos < c.length; pos++) {
            int type = Character.getType(c[pos]);
            if (type == currentType) {
                continue;
            }
            if (type == Character.LOWERCASE_LETTER && currentType == Character.UPPERCASE_LETTER) {
                int newTokenStart = pos - 1;
                if (newTokenStart != tokenStart) {
                    printName += new String(c, tokenStart, newTokenStart - tokenStart);
                    tokenStart = newTokenStart;
                }
            } else {
            	printName += new String(c, tokenStart, pos - tokenStart);
                tokenStart = pos;
            }
            currentType = type;
        }
        printName += new String(c, tokenStart, c.length - tokenStart);
        return printName.toLowerCase();
	}

}
