package org.dllearner.reasoning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.owltime.OWLTimeOntology;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.joda.time.DateTime;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.clarkparsia.owlapiv3.XSD;
import com.google.common.collect.Sets;

import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImpl;


public class InfluxDBOWLTimeReasoner extends OWLTimeReasoner implements AllenRelationsTemporalOWLReasoner {
	protected InfluxDB influxDB;
	private String dbName = "dllearner";
	private String dbHostString = "localhost";
	private int dbPort = 8086;
	private String dbUser = "root";
	private String dbPassword = "root";
	// FIXME: This is just to get started; needs to be properly refactored
	private ClosedWorldReasoner fileReasoner;
	static final String instantTable = "instant";
	static final String intervalTable = "interval";
	static final String timeColumn = "time";
	static final String individualColumn = "individual";
	static final String classColumn = "class";
	static final String dummyColumn = "dummy";
	private InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
	/**
	 * Used to create additional tag values to work around the issue that there may not be more
	 * than one entry in the InfluxDB with the same time stamp.
	 */
	private Random rnd = new Random();
	private Map<OWLIndividual, OWLAnonymousIndividual> indiv2dateTimeDescription;
	private Map<OWLAnonymousIndividual, OWLIndividual> dateTimeDescription2Individual;

	/**
	 * TODO: clean DB first?
	 */
	@Override
	public void init() throws ComponentInitException {
		super.init();
		influxDB = InfluxDBFactory.connect("http://" + dbHostString + ":" + dbPort, dbUser, dbPassword);
		influxDB.createDatabase(dbName);

		try {
			KnowledgeSource owlTimeKS = new OWLAPIOntology(OWLTimeOntology.getTimeOntology());
			sources.add(owlTimeKS);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileReasoner = new ClosedWorldReasoner(sources);
		fileReasoner.init();

		// TOOD: How to handle the mixed case of time instants *and* time intervals
		if (getTimeIntervallClassExpression() != null)
			// FIXME
			throw new RuntimeException("Not implemented, yet. Intervals will be "
					+ "implemted when reasoning over time instants works");
		if (getTimeInstantClassExpression() != null)
			initTimeInstants();

		indiv2dateTimeDescription = new HashMap<OWLIndividual, OWLAnonymousIndividual>();
		dateTimeDescription2Individual = new HashMap<OWLAnonymousIndividual, OWLIndividual>();
	}

	/**
	 * Issues:
	 * - We cannot assume that there won't be multiple events (even of the same class) occurring at
	 *   the same time. We thus added a workaround using the event individual's most specific class IRI
	 *   and its own IRI as tags
	 * - IRIs are currently stored as strings. Maybe we could apply a certain encoding scheme (e.g.
	 *   http://ieeexplore.ieee.org/abstract/document/7363955/ ) to avoid expensive reasoning.
	 * - Batch not sorted by timestamp
	 */
	private void initTimeInstants() {
		System.out.println("instant class: " + getTimeInstantClassExpression());
		Set<OWLIndividual> timeInstantIndividuals = fileReasoner.getIndividuals(getTimeInstantClassExpression());
		List<OWLProperty> propPath = getDateTimePropertyPath();
		int lastIdx = propPath.size() - 1;
		OWLDataProperty dtimeProp = (OWLDataProperty) propPath.remove(lastIdx);


		BatchPoints batchPoints = BatchPoints.database(dbName).build();
		for (OWLIndividual i : timeInstantIndividuals) {
			Set<OWLIndividual> tmpIndividuals = resolvePropertyChainEnds(i, getPropChainCopy(propPath));
			Set<OWLClass> types = fileReasoner.getTypesImpl(i);

			/* Assumption: Each for loop is taken only once since there
			 * is just one date assigned.
			 * (Though, nothing should break if it is taken multiple times.)
			 */
			for (OWLIndividual tmpIndiv : tmpIndividuals) {
				for (OWLLiteral dateTimeVal : fileReasoner.getRelatedValues(tmpIndiv, dtimeProp)) {
					long timestamp = literalToTimestamp(dateTimeVal);

					System.out.println(timestamp + " " + i  + " (" + i.hashCode() + ")");
					Builder ptBuilder = Point.measurement(instantTable)
							.time(timestamp, TimeUnit.MILLISECONDS)
							.tag(dummyColumn, Integer.toString(rnd.nextInt()))
//							.addField(individualColumn, getIntForIndiv(i));
							.addField(individualColumn, toString(i));

					OWLClassExpression msc = df.getOWLThing();
					for (OWLClass cls : types) {
						OWLSubClassOfAxiom scoAxiom = df.getOWLSubClassOfAxiom(cls, msc);
						if (fileReasoner.getReasonerComponent().isEntailed(scoAxiom))
							msc = cls;
					}

					ptBuilder = ptBuilder.tag(classColumn, msc.toString()); //.addField("dummy", true);
					batchPoints.point(ptBuilder.build());
				}
			}
		}

		influxDB.write(batchPoints);
	}

	protected Set<OWLIndividual> resolvePropertyChainEnds(OWLIndividual startIndividual, List<OWLObjectProperty> remainingPropertyChain) {
		if (remainingPropertyChain.isEmpty())
			return Sets.newHashSet(startIndividual);

		OWLObjectProperty nextProp = remainingPropertyChain.remove(0);

		Set<OWLIndividual> relatedIndividuals = fileReasoner.getRelatedIndividuals(startIndividual, nextProp);
		Set<OWLIndividual> targetIndividuals = new HashSet<>();
		for (OWLIndividual i : relatedIndividuals) {
			targetIndividuals.addAll(resolvePropertyChainEnds(i, remainingPropertyChain));
		}

		return targetIndividuals;
	}

	private List<OWLObjectProperty> getPropChainCopy(List<OWLProperty> chain) {
		List<OWLObjectProperty> objProps = new ArrayList<>();

		for (OWLProperty prop : chain) {
			objProps.add((OWLObjectProperty) prop);
		}

		return objProps;
	}

	// TODO: This is a first shot. To be checked again!
	private long literalToTimestamp(OWLLiteral literal) {
		DateTime dateTimeVal = DateTime.parse(literal.getLiteral());

		return dateTimeVal.getMillis();
	}

	private OWLLiteral instant2DateTimeStampLiteral(TimeInstant instant) {
		return df.getOWLLiteral(
				instant.toString(),  // TODO: not sure whether this is a stable way to get an xsd:dateTimeStamp-compatible string representation
				OWL2Datatype.XSD_DATE_TIME_STAMP);
	}

	private String toString(OWLIndividual i) {
		if (i.isAnonymous()) {
			return ((OWLAnonymousIndividual) i).toStringID();
		} else {
			return ((OWLNamedIndividual) i).getIRI().toString();
		}
	}

	/**
	 * TODO: Maybe add caching here.
	 */
	private List<TimeInstant> queryInstants() {
		String command = "SELECT * FROM " + instantTable;
		Query query = new Query(command, dbName);
		QueryResult queryRes = influxDB.query(query);

		List<TimeInstant> timeInstants = resultMapper.toPOJO(queryRes, TimeInstant.class);

		return timeInstants;
	}

	/**
	 * TODO: Finish the TimeInterval class
	 * TODO: Maybe add caching here.
	 */
	private List<TimeInterval> queryIntervals() {
		String command = "SELECT * FROM " + intervalTable;
		Query query = new Query(command, dbName);
		QueryResult queryRes = influxDB.query(query);

		List<TimeInterval> timeIntervals = resultMapper.toPOJO(queryRes, TimeInterval.class);

		return timeIntervals;
	}

	public void setDBName(String dbName) {
		this.dbName = dbName;
	}

	public String getDBName() {
		return this.dbName;
	}

	@Override
	public void releaseKB() {
		influxDB.deleteDatabase(dbName);
	}

	@Override
	public Set<OWLIndividual> getTimeIndividuals() {
		return fileReasoner.getIndividuals();
	}

	@Override
	public String getBaseURI() {
		OWLOntologyID ontID = fileReasoner.getReasonerComponent().getOntology().getOntologyID();
		return ontID.getOntologyIRI().get().toString();
	}

	@Override
	public Map<String, String> getPrefixes() {
		return fileReasoner.getPrefixes();
	}

	@Override
	public SortedSet<OWLIndividual> getIndividuals() {
		return fileReasoner.getIndividuals();
	}

	@Override
	public Set<OWLClass> getInconsistentClassesImpl() {
		return fileReasoner.getInconsistentClasses();
	}

	@Override
	public Set<OWLClass> getClasses() {
		return fileReasoner.getClasses();
	}

	/**
	 * The method returns all sub classes of a given class expression and
	 * also considers the equalities
	 * - this.timeIntervallClassExpression == owl-time:Instant
	 * - this.timeInstantClassExpression == owl-time:ProperInterval
	 *
	 * So if the given class is one of those, the equivalent class has to
	 * be added to the set of sub classes.
	 *
	 * TODO: How to handle non-proper intervals, i.e. owl-time:Interval?
	 */
	@Override
	public SortedSet<OWLClassExpression> getSubClassesImpl(OWLClassExpression concept) throws ReasoningMethodUnsupportedException {
		SortedSet<OWLClassExpression> subClss = fileReasoner.getSubClasses(concept);

		// this.timeIntervallClassExpression == owl-time:Instant
		if (timeIntervallClassExpression != null) {
			if (concept.equals(timeIntervallClassExpression))
				subClss.add(OWLTimeOntology.properInterval);
			else if (concept.equals(OWLTimeOntology.properInterval))
				subClss.add(timeIntervallClassExpression);
		}

		// this.timeInstantClassExpression == owl-time:ProperInterval
		if (timeInstantClassExpression != null) {
			if (concept.equals(timeInstantClassExpression))
				subClss.add(OWLTimeOntology.instant);
			else if (concept.equals(OWLTimeOntology.instant))
				subClss.add(timeInstantClassExpression);
		}

		return subClss;
	}

	/**
	 * The method returns all super classes of a given class expression and
	 * also considers the equalities
	 * - this.timeIntervallClassExpression == owl-time:Instant
	 * - this.timeInstantClassExpression == owl-time:ProperInterval
	 *
	 * So if the given class is one of those, the equivalent class has to
	 * be added to the set of super classes
	 *
	 * TODO: How to handle non-proper intervals, i.e. owl-time:Interval?
	 */
	@Override
	protected SortedSet<OWLClassExpression> getSuperClassesImpl(OWLClassExpression concept) throws ReasoningMethodUnsupportedException {
		SortedSet<OWLClassExpression> superClss = fileReasoner.getSuperClasses(concept);

		// this.timeIntervallClassExpression == owl-time:Instant
		if (timeIntervallClassExpression != null) {
			if (concept.equals(timeIntervallClassExpression))
				superClss.add(OWLTimeOntology.properInterval);
			else if (concept.equals(OWLTimeOntology.properInterval))
				superClss.add(timeIntervallClassExpression);
		}

		// this.timeInstantClassExpression == owl-time:ProperInterval
		if (timeInstantClassExpression != null) {
			if (concept.equals(timeInstantClassExpression))
				superClss.add(OWLTimeOntology.instant);
			else if (concept.equals(OWLTimeOntology.instant))
				superClss.add(timeInstantClassExpression);
		}

		return superClss;
	}

	@Override
	protected Set<OWLObjectProperty> getObjectPropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		return fileReasoner.getObjectPropertiesImpl();
	}

	@Override
	protected SortedSet<OWLObjectProperty> getSubPropertiesImpl(OWLObjectProperty role) throws ReasoningMethodUnsupportedException {
		return fileReasoner.getSubPropertiesImpl(role);
	}

	@Override
	protected SortedSet<OWLObjectProperty> getSuperPropertiesImpl(OWLObjectProperty role) throws ReasoningMethodUnsupportedException {
		return fileReasoner.getSuperPropertiesImpl(role);
	}

	@Override
	protected Set<OWLDataProperty> getDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		return fileReasoner.getDatatypePropertiesImpl();
	}

	/**
	 * Returns the sub data properties of a given data property.
	 * In case role equals one of the OWL-Time properties describing actual time data
	 * (esp. owl-time:inXSDDateTimeStamp) the property chain held in
	 * this.dateTimePropertyPath should also be part of the returned result.
	 * Buuuut: Since the returned set may only contain OWLDataProperty objects
	 * this is omitted here.
	 */
	@Override
	protected SortedSet<OWLDataProperty> getSubPropertiesImpl(OWLDataProperty role) throws ReasoningMethodUnsupportedException {
		return fileReasoner.getSubPropertiesImpl(role);
	}

	/**
	 * Returns the super data properties of a given data property.
	 * In case role equals one of the OWL-Time properties describing actual time data
	 * (esp. owl-time:inXSDDateTimeStamp) the property chain held in
	 * this.dateTimePropertyPath should also be part of the returned result.
	 * Buuuut: Since the returned set may only contain OWLDataProperty objects
	 * this is omitted here.
	 */
	@Override
	protected SortedSet<OWLDataProperty> getSuperPropertiesImpl(OWLDataProperty role) throws ReasoningMethodUnsupportedException {
		return fileReasoner.getSuperPropertiesImpl(role);
	}

	@Override
	protected OWLClassExpression getDomainImpl(OWLObjectProperty objectProperty)
			throws ReasoningMethodUnsupportedException {
		return fileReasoner.getDomainImpl(objectProperty);
	}

	@Override
	protected OWLClassExpression getRangeImpl(OWLObjectProperty objectProperty) {
		return fileReasoner.getRangeImpl(objectProperty);
	}

	@Override
	protected OWLClassExpression getDomainImpl(OWLDataProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		return fileReasoner.getDomainImpl(datatypeProperty);
	}

	@Override
	protected Set<OWLDataProperty> getIntDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		return fileReasoner.getIntDatatypePropertiesImpl();
	}

	@Override
	protected Set<OWLDataProperty> getDoubleDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		return fileReasoner.getDoubleDatatypePropertiesImpl();
	}

	private void updateMembersMap(OWLIndividual indiv, OWLLiteral value, Map<OWLIndividual, SortedSet<OWLLiteral>> members) {
		SortedSet<OWLLiteral> indivValuesSoFar = members.get(indiv);
		if (indivValuesSoFar == null) {
			indivValuesSoFar = new TreeSet<OWLLiteral>();
			members.put(indiv, indivValuesSoFar);
		}

		indivValuesSoFar.add(value);
	}

	private OWLAnonymousIndividual addDateTimeDescriptionToIndividual(OWLIndividual indiv) {
		OWLAnonymousIndividual dtimeDescr = df.getOWLAnonymousIndividual();
		indiv2dateTimeDescription.put(indiv, dtimeDescr);
		dateTimeDescription2Individual.put(dtimeDescr, indiv);

		return dtimeDescr;
	}

	@Override
	protected Map<OWLIndividual, SortedSet<OWLLiteral>> getDatatypeMembersImpl(
			OWLDataProperty datatypeProperty) throws ReasoningMethodUnsupportedException {

		Map<OWLIndividual, SortedSet<OWLLiteral>> members =
				fileReasoner.getDatatypeMembersImpl(datatypeProperty);

		// :day
		if (datatypeProperty.equals(OWLTimeOntology.day)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :dayOfYear
		else if (datatypeProperty.equals(OWLTimeOntology.dayOfYear)) {
			for (TimeInstant timeInstant : queryInstants()) {
				int dayOfYear = DateTime.parse(timeInstant.time.toString()).dayOfYear().get();
				OWLIndividual indiv =
						df.getOWLNamedIndividual(IRI.create(timeInstant.individual));

				OWLAnonymousIndividual dtimeDescr = indiv2dateTimeDescription.get(indiv);
				if (dtimeDescr == null) {
					dtimeDescr = addDateTimeDescriptionToIndividual(indiv);
				}

				updateMembersMap(
						dtimeDescr,
						df.getOWLLiteral(
								Integer.toString(dayOfYear),
								OWL2Datatype.XSD_NON_NEGATIVE_INTEGER),
						members);
			}
		}
		// :days
		else if (datatypeProperty.equals(OWLTimeOntology.days)) {
			for (TimeInterval timeInstant : queryIntervals()) {
				throw new RuntimeException("Not implemented, yet");
			}
		}
		// :hasXSDDuration
		else if (datatypeProperty.equals(OWLTimeOntology.hasXSDDuration)) {
			throw new RuntimeException("Not implemented, yet");

		}
		// :hour
		else if (datatypeProperty.equals(OWLTimeOntology.hour)) {
			for (TimeInstant timeInstant : queryInstants()) {
				int hour = DateTime.parse(timeInstant.time.toString()).getHourOfDay();
				OWLNamedIndividual indiv =
						df.getOWLNamedIndividual(IRI.create(timeInstant.individual));

				OWLAnonymousIndividual dtimeDescr = indiv2dateTimeDescription.get(indiv);
				if (dtimeDescr == null) {
					dtimeDescr = addDateTimeDescriptionToIndividual(indiv);
				}

				updateMembersMap(
						dtimeDescr,
						df.getOWLLiteral(
								Integer.toString(hour),
								OWL2Datatype.XSD_NON_NEGATIVE_INTEGER),
						members);
			}
		}
		// :hours
		else if (datatypeProperty.equals(OWLTimeOntology.hours)) {
			for (TimeInterval timeInterval : queryIntervals()) {
				throw new RuntimeException("Not implemented, yet");
			}
		}
		// :inXSDDate
		else if (datatypeProperty.equals(OWLTimeOntology.inXSDDate)) {
			for (TimeInstant timeInstant : queryInstants()) {
				OWLNamedIndividual indiv =
						df.getOWLNamedIndividual(IRI.create(timeInstant.individual));

				DateTime dtime = DateTime.parse(timeInstant.time.toString());
				// TODO: use the right formatter for this!!
				String dateStr = dtime.toString().split("T")[0];

				OWLLiteral dateLiteral =
						df.getOWLLiteral(
								dateStr,
								new OWLDatatypeImpl(XSD.DATE.getIRI()));

				updateMembersMap(indiv, dateLiteral, members);
			}
		}
		// :inXSDDateTime
		else if (datatypeProperty.equals(OWLTimeOntology.inXSDDateTime)) {
			for (TimeInstant timeInstant : queryInstants()) {
				OWLNamedIndividual indiv =
						df.getOWLNamedIndividual(IRI.create(timeInstant.individual));

				OWLLiteral dateTime = df.getOWLLiteral(
						timeInstant.time.toString(),  // TODO: not sure whether this is a stable way to get an xsd:dateTimeStamp-compatible string representation
						OWL2Datatype.XSD_DATE_TIME);

				updateMembersMap(indiv, dateTime, members);
			}
		}
		// :inXSDDateTimeStamp
		else if (datatypeProperty.equals(OWLTimeOntology.inXSDDateTimeStamp)) {
			for (TimeInstant timeInstant : queryInstants()) {
				OWLNamedIndividual indiv =
						df.getOWLNamedIndividual(IRI.create(timeInstant.individual));

				OWLLiteral dateTimeStamp = instant2DateTimeStampLiteral(timeInstant);

				updateMembersMap(indiv, dateTimeStamp, members);
			}
			throw new RuntimeException("Not implemented, yet");

		// :inXSDgYear
		} else if (datatypeProperty.equals(OWLTimeOntology.inXSDgYear)) {
			for (TimeInstant timeInstant : queryInstants()) {
				OWLNamedIndividual indiv =
						df.getOWLNamedIndividual(IRI.create(timeInstant.individual));

				OWLLiteral gYear = new OWLLiteralImpl(
						Integer.toString(DateTime.parse(timeInstant.time.toString()).getMonthOfYear()),
						null,
						new OWLDatatypeImpl(XSD.G_MONTH.getIRI()));

				updateMembersMap(indiv, gYear, members);
			}
		}
		// :inXSDgYearMonth
		else if (datatypeProperty.equals(OWLTimeOntology.inXSDgYearMonth)) {
			throw new RuntimeException("Not implemented, yet");

		}
		// :minute
		else if (datatypeProperty.equals(OWLTimeOntology.minute)) {
			for (TimeInstant timeInstant : queryInstants()) {
				OWLNamedIndividual indiv =
						df.getOWLNamedIndividual(IRI.create(timeInstant.individual));

				int minute = DateTime.parse(timeInstant.time.toString()).getMinuteOfHour();

				OWLAnonymousIndividual dtimeDescr = indiv2dateTimeDescription.get(indiv);
				if (dtimeDescr == null) {
					dtimeDescr = addDateTimeDescriptionToIndividual(indiv);
				}

				updateMembersMap(
						dtimeDescr,
						df.getOWLLiteral(
								Integer.toString(minute),
								OWL2Datatype.XSD_NON_NEGATIVE_INTEGER),
						members);
			}
		}
		// :minutes
		else if (datatypeProperty.equals(OWLTimeOntology.minutes)) {
			for (TimeInterval timeInterval : queryIntervals()) {
				throw new RuntimeException("Not implemented, yet");
			}
		}
		// :month
		else if (datatypeProperty.equals(OWLTimeOntology.month)) {
			throw new RuntimeException("Not implemented, yet");

		// :months
		} else if (datatypeProperty.equals(OWLTimeOntology.months)) {
			for (TimeInterval timeInterval : queryIntervals()) {
				throw new RuntimeException("Not implemented, yet");
			}
		}
		// NOT SUPPORTED
//		// :nominalPosition
//		else if (datatypeProperty.equals(OWLTimeOntology.nominalPosition)) {
//			throw new RuntimeException("Not implemented, yet");
//		}
		// :numericDuration
		else if (datatypeProperty.equals(OWLTimeOntology.numericDuration)) {
			for (TimeInterval timeInterval : queryIntervals()) {
				throw new RuntimeException("Not implemented, yet");
			}
		}
		// NOT SUPPORTED
//		// :numericPosition
//		else if (datatypeProperty.equals(OWLTimeOntology.numericPosition)) {
//			throw new RuntimeException("Not implemented, yet");
//		}
		// :second
		// FIMXE: since :second has a xsd:decimal range, I should add the milliseconds
		else if (datatypeProperty.equals(OWLTimeOntology.second)) {
			for (TimeInstant timeInstant : queryInstants()) {
				OWLNamedIndividual indiv =
						df.getOWLNamedIndividual(IRI.create(timeInstant.individual));

				int second = DateTime.parse(timeInstant.time.toString()).getSecondOfMinute();

				OWLAnonymousIndividual dtimeDescr = indiv2dateTimeDescription.get(indiv);
				if (dtimeDescr == null) {
					dtimeDescr = addDateTimeDescriptionToIndividual(indiv);
				}

				updateMembersMap(
						indiv,
						df.getOWLLiteral(
								Integer.toString(second),
								OWL2Datatype.XSD_DECIMAL),
						members);
			}
		}
		// :seconds
		else if (datatypeProperty.equals(OWLTimeOntology.seconds)) {
			for (TimeInterval timeInterval : queryIntervals()) {
				throw new RuntimeException("Not implemented, yet");
			}
		}
		// :week
		else if (datatypeProperty.equals(OWLTimeOntology.week)) {
			for (TimeInstant timeInstant : queryInstants()) {
				OWLNamedIndividual indiv =
						df.getOWLNamedIndividual(IRI.create(timeInstant.individual));

				int week = DateTime.parse(timeInstant.time.toString()).getWeekOfWeekyear();

				OWLAnonymousIndividual dtimeDescr = indiv2dateTimeDescription.get(indiv);
				if (dtimeDescr == null) {
					dtimeDescr = addDateTimeDescriptionToIndividual(indiv);
				}

				updateMembersMap(
						dtimeDescr,
						df.getOWLLiteral(
								Integer.toString(week),
								OWL2Datatype.XSD_NON_NEGATIVE_INTEGER),
						members);
			}
		}
		// :weeks
		else if (datatypeProperty.equals(OWLTimeOntology.weeks)) {
			for (TimeInterval timeInterval : queryIntervals()) {
				throw new RuntimeException("Not implemented, yet");
			}
		}
		// :xsdDateTime
		else if (datatypeProperty.equals(OWLTimeOntology.xsdDateTime)) {
			for (TimeInterval timeInterval : queryIntervals()) {
				throw new RuntimeException("Not implemented, yet");
			}
		}
		// :year
		else if (datatypeProperty.equals(OWLTimeOntology.year)) {
			throw new RuntimeException("Not implemented, yet");

		}
		// :years
		else if (datatypeProperty.equals(OWLTimeOntology.years)) {
			for (TimeInterval timeInterval : queryIntervals()) {
				throw new RuntimeException("Not implemented, yet");
			}
		}

		return members;
	}

	@Override
	public OWLDatatype getDatatype(OWLDataProperty dp) {
		return fileReasoner.getDatatype(dp);
	}

	@Override
	protected Map<OWLIndividual, SortedSet<OWLIndividual>> getPropertyMembersImpl(
			OWLObjectProperty atomicRole) throws ReasoningMethodUnsupportedException {
		Map<OWLIndividual, SortedSet<OWLIndividual>> members = fileReasoner.getPropertyMembersImpl(atomicRole);
		// :day
		if (atomicRole.equals(OWLTimeOntology.day)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :dayOfYear
		else if (atomicRole.equals(OWLTimeOntology.dayOfYear)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :days
		else if (atomicRole.equals(OWLTimeOntology.days)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :hasXSDDuration
		else if (atomicRole.equals(OWLTimeOntology.hasXSDDuration)) {
			throw new RuntimeException("Not implemented, yet");

		}
		// :hour
		else if (atomicRole.equals(OWLTimeOntology.hour)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :hours
		else if (atomicRole.equals(OWLTimeOntology.hours)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :inXSDDate
		else if (atomicRole.equals(OWLTimeOntology.inXSDDate)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :inXSDDateTime
		else if (atomicRole.equals(OWLTimeOntology.inXSDDateTime)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :inXSDDateTimeStamp
		else if (atomicRole.equals(OWLTimeOntology.inXSDDateTimeStamp)) {
			throw new RuntimeException("Not implemented, yet");

		// :inXSDgYear
		} else if (atomicRole.equals(OWLTimeOntology.inXSDgYear)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :inXSDgYearMonth
		else if (atomicRole.equals(OWLTimeOntology.inXSDgYearMonth)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :minute
		else if (atomicRole.equals(OWLTimeOntology.minute)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :minutes
		else if (atomicRole.equals(OWLTimeOntology.minutes)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :month
		else if (atomicRole.equals(OWLTimeOntology.month)) {
			throw new RuntimeException("Not implemented, yet");

		// :months
		} else if (atomicRole.equals(OWLTimeOntology.months)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// NOT SUPPORTED
//				// :nominalPosition
//				else if (atomicRole.equals(OWLTimeOntology.nominalPosition)) {
//					throw new RuntimeException("Not implemented, yet");
//				}
		// :numericDuration
		else if (atomicRole.equals(OWLTimeOntology.numericDuration)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// NOT SUPPORTED
//				// :numericPosition
//				else if (atomicRole.equals(OWLTimeOntology.numericPosition)) {
//					throw new RuntimeException("Not implemented, yet");
//				}
		// :second
		else if (atomicRole.equals(OWLTimeOntology.second)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :seconds
		else if (atomicRole.equals(OWLTimeOntology.seconds)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :week
		else if (atomicRole.equals(OWLTimeOntology.week)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :weeks
		else if (atomicRole.equals(OWLTimeOntology.weeks)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :xsdDateTime
		else if (atomicRole.equals(OWLTimeOntology.xsdDateTime)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :year
		else if (atomicRole.equals(OWLTimeOntology.year)) {
			throw new RuntimeException("Not implemented, yet");
		}
		// :years
		else if (atomicRole.equals(OWLTimeOntology.years)) {
			throw new RuntimeException("Not implemented, yet");
		}

		return members;
	}

	@Override
	public OWLAPIReasoner getReasonerComponent() {
		return fileReasoner.getReasonerComponent();
	}

	@Override
	protected SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression concept)
			throws ReasoningMethodUnsupportedException {

		if (concept.equals(OWLTimeOntology.instant) &&
				timeInstantClassExpression != null)
			return fileReasoner.getIndividuals(timeInstantClassExpression);

		// TODO: What about time:Interval?
		else if (concept.equals(OWLTimeOntology.properInterval) &&
				timeIntervallClassExpression != null)
			return fileReasoner.getIndividuals(timeIntervallClassExpression);

		else
			return fileReasoner.getIndividualsImpl(concept);
	}

	@Override
	protected Set<OWLDataProperty> getBooleanDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		return fileReasoner.getBooleanDatatypePropertiesImpl();
	}

	@Override
	protected boolean hasTypeImpl(OWLClassExpression concept, OWLIndividual individual)
			throws ReasoningMethodUnsupportedException {

		if (concept.equals(OWLTimeOntology.instant) && timeInstantClassExpression != null)
			return fileReasoner.hasTypeImpl(timeInstantClassExpression, individual);

		else if (concept.equals(OWLTimeOntology.properInterval) && timeIntervallClassExpression != null)
			return fileReasoner.hasTypeImpl(timeIntervallClassExpression, individual);

		else
			return fileReasoner.hasTypeImpl(concept, individual);
	}
	// <--------------------- Allen's relations --------------------->

	@Override
	public Set<OWLIndividual> happensBefore(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean happensBefore(OWLIndividual before, OWLIndividual after) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> happensAfter(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean happensAfter(OWLIndividual after, OWLIndividual before) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> meets(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean meets(OWLIndividual meeting, OWLIndividual met) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> metBy(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean metBy(OWLIndividual met, OWLIndividual meeting) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> overlapsWith(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean overlapsWith(OWLIndividual earlier, OWLIndividual later) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> overlappedBy(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean overlappedBy(OWLIndividual later, OWLIndividual earlier) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> starts(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean starts(OWLIndividual starting, OWLIndividual started) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> startedBy(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean startedBy(OWLIndividual started, OWLIndividual starting) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> happensDuring(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean happensDuring(OWLIndividual inner, OWLIndividual outer) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> contains(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean contains(OWLIndividual outer, OWLIndividual inner) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> finishes(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean finishes(OWLIndividual finishing, OWLIndividual finished) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> finishedBy(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean finishedBy(OWLIndividual finished, OWLIndividual finishing) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public Set<OWLIndividual> equals(OWLIndividual i) {
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public boolean equals(OWLIndividual i, OWLIndividual j) {
		throw new RuntimeException("Not implemented, yet");
	}


}
