/**
 * 
 */
package org.dllearner.utilities;

import java.util.Collection;
import java.util.Set;

import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class OWLAPIUtils {
	
	private static final OWLCLassExpressionToOWLClassTransformer OWL_CLASS_TRANSFORM_FUNCTION = new OWLCLassExpressionToOWLClassTransformer();
	
	private static final Set<IRI> intDatatypeIRIs = Sets.newHashSet(
			OWL2Datatype.XSD_INTEGER.getIRI(),
			OWL2Datatype.XSD_INT.getIRI(),
			OWL2Datatype.XSD_POSITIVE_INTEGER.getIRI(),
			OWL2Datatype.XSD_NON_POSITIVE_INTEGER.getIRI(),
			OWL2Datatype.XSD_NEGATIVE_INTEGER.getIRI(),
			OWL2Datatype.XSD_NON_NEGATIVE_INTEGER.getIRI()
			);

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
	
	public static boolean isIntegerDatatype(OWLLiteral lit) {
		return intDatatypeIRIs.contains(lit.getDatatype().getIRI());
	}
	
	public static Set<OWLClass> asOWLClasses(Set<OWLClassExpression> classExpressions) {
		return Sets.newHashSet(Iterables.transform(classExpressions, OWL_CLASS_TRANSFORM_FUNCTION));
	}

}
