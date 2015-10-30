/**
 * 
 */
package org.dllearner.utilities;

import java.util.*;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.utilities.owl.SimpleOWLEntityChecker;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
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
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;

import com.clarkparsia.owlapiv3.XSD;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class OWLAPIUtils {
	
	private static final OWLCLassExpressionToOWLClassTransformer OWL_CLASS_TRANSFORM_FUNCTION = new OWLCLassExpressionToOWLClassTransformer();
	
    public final static Set<OWLDatatype> intDatatypes = new TreeSet<>(Arrays.asList(
			XSD.INT,
			XSD.INTEGER,
			XSD.POSITIVE_INTEGER,
			XSD.NEGATIVE_INTEGER,
			XSD.NON_POSITIVE_INTEGER,
			XSD.NON_NEGATIVE_INTEGER,
			XSD.SHORT,
			XSD.BYTE,
			XSD.UNSIGNED_INT,
			XSD.UNSIGNED_LONG
	));
    public final static Set<OWLDatatype> floatDatatypes = new TreeSet<>(Arrays.asList(
			XSD.FLOAT,
			XSD.DOUBLE,
			OWL2DatatypeImpl.getDatatype(OWL2Datatype.XSD_DECIMAL)
	));
    public final static Set<OWLDatatype> fixedDatatypes = new TreeSet<>(Collections.singletonList(
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
    	XSD.G_DAY,
    	XSD.G_MONTH,
    	XSD.G_YEAR
    ));
    
    public final static Set<OWLDatatype> periodDatatypes = Sets.newTreeSet(Arrays.asList(
    		XSD.DURATION,
    		new OWLDatatypeImpl(IRI.create(com.hp.hpl.jena.vocabulary.XSD.getURI() + "yearMonthDuration")),
    		new OWLDatatypeImpl(IRI.create(com.hp.hpl.jena.vocabulary.XSD.getURI() + "dayTimeDuration"))
        ));
	
	public static final Set<OWLDatatype> numericDatatypes = Sets.union(intDatatypes, floatDatatypes);
	
	private static final Map<OWLDatatype, Class<?>> javaTypeMap;
	static {
		javaTypeMap = new TreeMap<>();
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
		dateTimeFormatters.put(XSD.G_YEAR, ISODateTimeFormat.year());
		dateTimeFormatters.put(XSD.G_YEAR_MONTH, ISODateTimeFormat.yearMonth());
		dateTimeFormatters.put(XSD.G_MONTH, DateTimeFormat.forPattern("--MM").withZoneUTC());
		dateTimeFormatters.put(XSD.G_MONTH_DAY, DateTimeFormat.forPattern("--MM-DD").withZoneUTC());
		dateTimeFormatters.put(XSD.G_DAY, DateTimeFormat.forPattern("---DD").withZoneUTC());
		dateTimeFormatters.put(XSD.TIME, DateTimeFormat.forPattern("hh:mm:ss.sss").withOffsetParsed());
		dateTimeFormatters.put(XSD.DATE, ISODateTimeFormat.date());
		dateTimeFormatters.put(XSD.DATE_TIME, ISODateTimeFormat.dateHourMinuteSecond()); //  .dateTimeNoMillis());
		dateTimeFormatters.put(OWL2DatatypeImpl.getDatatype(OWL2Datatype.XSD_DATE_TIME_STAMP), ISODateTimeFormat.dateTimeNoMillis().withOffsetParsed());
	}

	public static final Map<OWLDatatype, DateTimeFormatter> dateTimeParsers = new HashMap<>(dateTimeFormatters);
	static {
//		dateTimeParsers.put(XSD.G_YEAR, ISODateTimeFormat.year());
//		dateTimeParsers.put(XSD.G_YEAR_MONTH, ISODateTimeFormat.yearMonth());
		dateTimeParsers.put(XSD.G_MONTH, new DateTimeFormatterBuilder()
		.append(DateTimeFormat.forPattern("--MM"))
		.appendOptional(DateTimeFormat.forPattern("Z").getParser())
		.toFormatter().withZoneUTC());
		dateTimeParsers.put(XSD.G_MONTH_DAY, new DateTimeFormatterBuilder()
		.append(DateTimeFormat.forPattern("--MM-DD"))
		.appendOptional(DateTimeFormat.forPattern("Z").getParser())
		.toFormatter().withZoneUTC());
		dateTimeParsers.put(XSD.G_DAY, new DateTimeFormatterBuilder()
		.append(DateTimeFormat.forPattern("---DD"))
		.appendOptional(DateTimeFormat.forPattern("Z").getParser())
		.toFormatter().withZoneUTC());
//		dateTimeParsers.put(XSD.TIME, DateTimeFormat.forPattern("hh:mm:ss.sss").withOffsetParsed());
//		dateTimeParsers.put(XSD.DATE, ISODateTimeFormat.date());
//		dateTimeParsers.put(XSD.DATE_TIME, ISODateTimeFormat.dateHourMinuteSecond()); //  .dateTimeNoMillis());
//		dateTimeParsers.put(OWL2DatatypeImpl.getDatatype(OWL2Datatype.XSD_DATE_TIME_STAMP), ISODateTimeFormat.dateTimeNoMillis().withOffsetParsed());
	}
	
	public static final Map<OWLDatatype, PeriodFormatter> periodFormatters = new HashMap<>();
	static {
		periodFormatters.put(XSD.DURATION, ISOPeriodFormat.standard());
		periodFormatters.put(new OWLDatatypeImpl(IRI.create(com.hp.hpl.jena.vocabulary.XSD.getURI() + "dayTimeDuration")),
				new PeriodFormatterBuilder()//PnDTnHnMnS
        .appendLiteral("P")
        .appendDays()
        .appendSuffix("D")
        .appendSeparatorIfFieldsAfter("T")
        .appendHours()
        .appendSuffix("H")
        .appendMinutes()
        .appendSuffix("M")
        .appendSecondsWithOptionalMillis()
        .appendSuffix("S")
        .toFormatter());
		periodFormatters.put(new OWLDatatypeImpl(IRI.create(com.hp.hpl.jena.vocabulary.XSD.getURI() + "yearMonthDuration")),
				new PeriodFormatterBuilder()//PnYnM
        .appendLiteral("P")
        .appendYears()
        .appendSuffix("Y")
        .appendMonths()
        .appendSuffix("M")
        .toFormatter());
		
	}
	
	public static String getPrintName(EntityType entityType) {
        return entityType.getPrintName().toLowerCase();
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
	
	/**
	 * Checks whether the given value is in the closed interval [min,max], i.e.
	 * the value is greater than min and lower than max.
	 * @param value the value
	 * @param min the lower interval endpoint
	 * @param max the upper interval endpoint
	 * @return
	 */
	public static boolean inRange(OWLLiteral value, OWLLiteral min, OWLLiteral max) {
		OWLDatatype datatype = value.getDatatype();
		
		if(OWLAPIUtils.dtDatatypes.contains(datatype)) {
			DateTimeFormatter parser = OWLAPIUtils.dateTimeParsers.get(datatype);
			
			// parse min
			DateTime minDateTime = null;
			if(min != null) {
				minDateTime = parser.parseDateTime(min.getLiteral());
			}
			
			// parse max
			DateTime maxDateTime = null;
			if(max != null) {
				maxDateTime = parser.parseDateTime(max.getLiteral());
			}
			
			// parse value
			DateTime dateTime = parser.parseDateTime(value.getLiteral());
			
			if(
					(minDateTime == null || (dateTime.isEqual(minDateTime) ||  dateTime.isAfter(minDateTime)))
					&&
					(maxDateTime == null || (dateTime.isEqual(maxDateTime) ||  dateTime.isBefore(maxDateTime)))
					)
					{
				return true;
			}
		}
		
		return false;
	}
}
