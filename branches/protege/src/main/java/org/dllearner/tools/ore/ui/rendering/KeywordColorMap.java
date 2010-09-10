package org.dllearner.tools.ore.ui.rendering;


import java.awt.Color;
import java.util.HashMap;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Sep 23, 2008<br><br>
 */
public class KeywordColorMap extends HashMap<String, Color> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8840286687096195602L;

	public KeywordColorMap() {
        Color restrictionColor = new Color(178, 0, 178);
        Color logicalOpColor = new Color(0, 178, 178);
        Color axiomColor = new Color(10, 94, 168);
        Color typeColor = new Color(178, 178, 178);

        for (ManchesterOWLSyntax keyword : ManchesterOWLSyntax.values()){
            if (keyword.isAxiomKeyword()){
                put(keyword.toString(), axiomColor);
                put(keyword.toString() + ":", axiomColor);
            }
            else if (keyword.isClassExpressionConnectiveKeyword()){
                put(keyword.toString(), logicalOpColor);
            }
            else if (keyword.isClassExpressionQuantiferKeyword()){
                put(keyword.toString(), restrictionColor);
            }
            else if (keyword.isSectionKeyword()){
                put(keyword.toString(), typeColor);
                put(keyword.toString() + ":", typeColor);
            }
        }

        put("o", axiomColor);
//        put("\u279E", axiomColor);
//        put("\u2192", axiomColor);
//        put("\u2227", axiomColor);
    }
}
