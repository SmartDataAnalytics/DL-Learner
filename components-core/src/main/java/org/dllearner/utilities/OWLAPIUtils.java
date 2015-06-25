/**
 * 
 */
package org.dllearner.utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.utilities.owl.SimpleOWLEntityChecker;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import uk.ac.manchester.cs.owl.owlapi.OWL2DatatypeImpl;

import com.clarkparsia.owlapiv3.XSD;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class OWLAPIUtils {
	
	private static final OWLCLassExpressionToOWLClassTransformer OWL_CLASS_TRANSFORM_FUNCTION = new OWLCLassExpressionToOWLClassTransformer();
	
    public final static Set<OWLDatatype> intDatatypes = new TreeSet<OWLDatatype>(Arrays.asList(
		XSD.INT,
		XSD.INTEGER,
		XSD.POSITIVE_INTEGER,
		XSD.NEGATIVE_INTEGER,
		XSD.NON_POSITIVE_INTEGER,
		XSD.NON_NEGATIVE_INTEGER,
		XSD.SHORT,
		XSD.BYTE
    ));
    public final static Set<OWLDatatype> floatDatatypes = new TreeSet<OWLDatatype>(Arrays.asList(
    	XSD.FLOAT,
    	XSD.DOUBLE,
    	OWL2DatatypeImpl.getDatatype(OWL2Datatype.XSD_DECIMAL)
    ));
    public final static Set<OWLDatatype> fixedDatatypes = new TreeSet<OWLDatatype>(Arrays.asList(
    	XSD.BOOLEAN
    ));
    
	/**
	 * The OWL 2 datatypes for the representation of time instants with and
	 * without time zone offsets.
	 */
    public final static Set<OWLDatatype> owl2TimeDatatypes = Sets.newTreeSet(Arrays.asList(
    		OWL2DatatypeImpl.getDatatype(OWL2Datatype.XSD_DATE_TIME),
        	OWL2DatatypeImpl.getDatatype(OWL2Datatype.XSD_DATE_TIME_STAMP)
        ));
    
    public final static Set<OWLDatatype> dtDatatypes = Sets.newTreeSet(Arrays.asList(
    	XSD.DATE,
    	XSD.DATE_TIME,
    	XSD.G_DAY
    ));
	
	public static final Set<OWLDatatype> numericDatatypes = Sets.union(intDatatypes, floatDatatypes);
	
	private static final Map<OWLDatatype, Class<?>> javaTypeMap;
	static {
		javaTypeMap = new TreeMap<OWLDatatype, Class<?>>();
		javaTypeMap.put(XSD.BYTE, Byte.class);
		javaTypeMap.put(XSD.SHORT, Short.class);
		javaTypeMap.put(OWL2DatatypeImpl.getDatatype(OWL2Datatype.XSD_DECIMAL), Double.class);
		javaTypeMap.put(XSD.INT, Integer.class);
		javaTypeMap.put(XSD.INTEGER, Integer.class);
		javaTypeMap.put(XSD.POSITIVE_INTEGER, Integer.class);
		javaTypeMap.put(XSD.NEGATIVE_INTEGER, Integer.class);
		javaTypeMap.put(XSD.NON_NEGATIVE_INTEGER, Integer.class);
		javaTypeMap.put(XSD.NON_POSITIVE_INTEGER, Integer.class);
		javaTypeMap.put(XSD.LONG, Long.class);
		javaTypeMap.put(XSD.DOUBLE, Double.class);
		javaTypeMap.put(XSD.FLOAT, Float.class);
		javaTypeMap.put(XSD.BOOLEAN	, Boolean.class);
		//javaTypeMap.put(OWL2Datatype.XSD_STRING, String.class);
		//javaTypeMap.put(OWL2Datatype.XSD_, .class);
	}
	
	public static final Map<OWLDatatype, DateTimeFormatter> dateTimeFormatters = new HashMap<>();
	static {
		dateTimeFormatters.put(XSD.DATE, ISODateTimeFormat.date());
		dateTimeFormatters.put(XSD.DATE_TIME, ISODateTimeFormat.dateTimeNoMillis());
	}
	
	
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
		return intDatatypes.contains(lit.getDatatype());
	}
	
	public static boolean isIntegerDatatype(OWLDatatype datatype) {
		return intDatatypes.contains(datatype);
	}
	
	public static boolean isNumericDatatype(OWLDatatype datatype){
		return numericDatatypes.contains(datatype);
    }
	
	public static Set<OWLClass> asOWLClasses(Set<OWLClassExpression> classExpressions) {
		return Sets.newHashSet(Iterables.transform(classExpressions, OWL_CLASS_TRANSFORM_FUNCTION));
	}

	public static final String UNPARSED_OCE = "dllearner+unparsed:";
	
	public static OWLClassExpression classExpressionPropertyExpander (OWLClassExpression startClass, AbstractReasonerComponent reasoner, OWLDataFactory dataFactory) {
		if(!startClass.isAnonymous() && startClass.asOWLClass().getIRI().toString().startsWith(UNPARSED_OCE)) {
			try {
				String s = startClass.asOWLClass().getIRI().toString().substring(UNPARSED_OCE.length());
				ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(dataFactory, s);
				parser.setOWLEntityChecker(new SimpleOWLEntityChecker(reasoner));
				return parser.parseClassExpression();
			} catch (ParserException e) {
				throw new RuntimeException("Parsing of class expression in OWL Manchester Syntax failed. Please check the syntax and "
						+ "remember to use either full IRIs or prefixed IRIs.", e);
			}
		} else {
			return startClass;
		}

	}
}
