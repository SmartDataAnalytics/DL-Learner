/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.utilities;

import com.clarkparsia.owlapiv3.XSD;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.Range;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.utilities.owl.SimpleOWLEntityChecker;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.XSDVocabulary;
import org.slf4j.Logger;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A collection of utility methods for the OWL API.
 *
 * @author Lorenz Buehmann
 *
 */
public class OWLAPIUtils {
	
	public static final OWLOntologyManager	manager	= OWLManager.createOWLOntologyManager();
	public static final OWLDataFactory factory = manager.getOWLDataFactory();
	
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
			factory.getOWLDatatype(XSDVocabulary.DECIMAL.getIRI())

	));
    public final static Set<OWLDatatype> fixedDatatypes = new TreeSet<>(Collections.singletonList(
			XSD.BOOLEAN
	));
    
	/**
	 * The OWL 2 datatypes for the representation of time instants with and
	 * without time zone offsets.
	 */
    public final static Set<OWLDatatype> owl2TimeDatatypes = Sets.newTreeSet(Arrays.asList(
			factory.getOWLDatatype(XSDVocabulary.DATE_TIME.getIRI()),
			factory.getOWLDatatype(XSDVocabulary.DATE_TIME_STAMP.getIRI())
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
    		new OWLDatatypeImpl(IRI.create(org.apache.jena.vocabulary.XSD.getURI() + "yearMonthDuration")),
    		new OWLDatatypeImpl(IRI.create(org.apache.jena.vocabulary.XSD.getURI() + "dayTimeDuration"))
        ));
	
	public static final Set<OWLDatatype> numericDatatypes = Sets.union(intDatatypes, floatDatatypes);
	
	private static final Map<OWLDatatype, Class<?>> javaTypeMap;
	static {
		javaTypeMap = new TreeMap<>();
		javaTypeMap.put(XSD.BYTE, Byte.class);
		javaTypeMap.put(XSD.SHORT, Short.class);
		javaTypeMap.put(factory.getOWLDatatype(XSDVocabulary.DECIMAL.getIRI()), Double.class);
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
		dateTimeFormatters.put(factory.getOWLDatatype(XSDVocabulary.DATE_TIME_STAMP.getIRI()), ISODateTimeFormat.dateTimeNoMillis().withOffsetParsed());
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
		dateTimeParsers.put(XSD.DATE_TIME, new DateTimeFormatterBuilder()
		.append(DateTimeFormat.forPattern("yyyy-MM-DD'T'HH:mm:ss"))
		.appendOptional(new DateTimeFormatterBuilder()
		.appendLiteral('.')
		.appendFractionOfSecond(1,4)
		.toParser())
		.appendOptional(DateTimeFormat.forPattern("Z").getParser())
		.toFormatter().withZoneUTC());
//		dateTimeParsers.put(XSD.DATE_TIME, ISODateTimeFormat.dateHourMinuteSecond()); //  .dateTimeNoMillis());
//		dateTimeParsers.put(OWL2DatatypeImpl.getDatatype(OWL2Datatype.XSD_DATE_TIME_STAMP), ISODateTimeFormat.dateTimeNoMillis().withOffsetParsed());
	}
	
	public static final Map<OWLDatatype, PeriodFormatter> periodFormatters = new HashMap<>();
	static {
		periodFormatters.put(XSD.DURATION, ISOPeriodFormat.standard());
		periodFormatters.put(new OWLDatatypeImpl(IRI.create(org.apache.jena.vocabulary.XSD.getURI() + "dayTimeDuration")),
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
		periodFormatters.put(new OWLDatatypeImpl(IRI.create(org.apache.jena.vocabulary.XSD.getURI() + "yearMonthDuration")),
				new PeriodFormatterBuilder()//PnYnM
        .appendLiteral("P")
        .appendYears()
        .appendSuffix("Y")
        .appendMonths()
        .appendSuffix("M")
        .toFormatter());
		
	}

	/**
	 * @param entityType the OWL entity type
	 * @return the name of the OWL entity type
	 */
	public static String getPrintName(EntityType entityType) {
        return entityType.getPrintName().toLowerCase();
	}

	/**
	 * @param lit the OWL literal
	 * @return whether the OWL literal is an integer, i.e. whether the datatype is some integer
	 */
	public static boolean isIntegerDatatype(OWLLiteral lit) {
		return intDatatypes.contains(lit.getDatatype());
	}
	
	public static boolean isIntegerDatatype(OWLDatatype datatype) {
		return intDatatypes.contains(datatype);
	}
	
	public static boolean isNumericDatatype(OWLDatatype datatype){
		return numericDatatypes.contains(datatype);
    }

	/**
	 * Convenience method that converts a set of OWL class expressions to a set of OWL classes.
	 * @param classExpressions a set of OWL class expressions
	 * @return a set of OWL classes
	 */
	public static Set<OWLClass> asOWLClasses(Set<OWLClassExpression> classExpressions) {
		return classExpressions.stream().map(OWLClassExpression::asOWLClass).collect(Collectors.toSet());
	}

	public static final String UNPARSED_OCE = "dllearner+unparsed:";

	
	public static OWLClassExpression classExpressionPropertyExpander (OWLClassExpression startClass, AbstractReasonerComponent reasoner, OWLDataFactory dataFactory, boolean sfp) {
		if(!startClass.isAnonymous() && startClass.asOWLClass().getIRI().toString().startsWith(UNPARSED_OCE)) {
			try {
				String s = startClass.asOWLClass().getIRI().toString().substring(UNPARSED_OCE.length());
				return fromManchester(s, reasoner, dataFactory, sfp);
			} catch (ManchesterOWLSyntaxParserException e) {
				throw new RuntimeException("Parsing of class expression in OWL Manchester Syntax failed. Please check the syntax and "
						+ "remember to use either full IRIs or prefixed IRIs.", e);
			}
		} else {
			return startClass;
		}

	}
	public static OWLClassExpression classExpressionPropertyExpander (OWLClassExpression startClass, AbstractReasonerComponent reasoner, OWLDataFactory dataFactory) {
		return classExpressionPropertyExpander(startClass, reasoner, dataFactory, false);
	}

	@NotNull
	public static OWLClassExpression fromManchester(String expr, AbstractReasonerComponent reasoner, OWLDataFactory dataFactory) {
		ManchesterOWLSyntaxClassExpressionParser parser = new ManchesterOWLSyntaxClassExpressionParser(dataFactory, new SimpleOWLEntityChecker(reasoner));
		return parser.parse(expr);
	}

	@NotNull
	public static OWLClassExpression fromManchester(String expr, AbstractReasonerComponent reasoner, OWLDataFactory dataFactory, boolean shortForm) {
		SimpleOWLEntityChecker oec = new SimpleOWLEntityChecker(reasoner);
		oec.setAllowShortForm(shortForm);
		ManchesterOWLSyntaxClassExpressionParser parser = new ManchesterOWLSyntaxClassExpressionParser(dataFactory, oec);
		return parser.parse(expr);
	}

	/**
	 * Checks whether the given value is in the closed interval [min,max], i.e.
	 * the value is greater than min and lower than max.
	 * @param value the value
	 * @param min the lower interval endpoint
	 * @param max the upper interval endpoint
	 * @return whether the given value is in the closed interval [min,max]
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
		} else if(OWLAPIUtils.floatDatatypes.contains(datatype)) {
			return Range.between(min.parseFloat(), max.parseFloat()).contains(value.parseFloat());
		} else if(OWLAPIUtils.intDatatypes.contains(datatype)) {
			return Range.between(min.parseInteger(), max.parseInteger()).contains(value.parseInteger());
		}
		
		return false;
	}

	public static OWLClassExpression classExpressionPropertyExpanderChecked(OWLClassExpression startClass, AbstractReasonerComponent reasoner, final OWLDataFactory df, Logger logger) {
		return classExpressionPropertyExpanderChecked(startClass, reasoner, df, df::getOWLThing, logger);
	}

	public static OWLClassExpression classExpressionPropertyExpanderChecked(OWLClassExpression startClass, AbstractReasonerComponent reasoner, OWLDataFactory df, Supplier<OWLClassExpression> defaultClass, Logger logger, boolean sfp) {
		if(startClass == null) {
			if (defaultClass != null)
				startClass = defaultClass.get();
		} else {
			try {
				startClass = OWLAPIUtils.classExpressionPropertyExpander(startClass, reasoner, df, sfp);
			} catch (ManchesterOWLSyntaxParserException e) {
				logger.info("Error parsing startClass: " + e.getMessage());
				startClass = defaultClass.get();
				logger.warn("Using "+ startClass +" instead.");
			}
		}
		return startClass;
	}
	public static OWLClassExpression classExpressionPropertyExpanderChecked(OWLClassExpression startClass, AbstractReasonerComponent reasoner, OWLDataFactory df, Supplier<OWLClassExpression> defaultClass, Logger logger) {
		return classExpressionPropertyExpanderChecked(startClass, reasoner, df, defaultClass, logger, false);
	}
	public static OWLClassExpression classExpressionPropertyExpanderChecked(OWLClassExpression startClass, AbstractReasonerComponent reasoner, OWLDataFactory df, boolean sfp, Logger logger) {
		return classExpressionPropertyExpanderChecked(startClass, reasoner, df, null, logger, sfp);
	}
}
