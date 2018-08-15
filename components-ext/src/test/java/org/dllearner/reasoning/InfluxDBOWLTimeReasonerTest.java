package org.dllearner.reasoning;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.owltime.OWLTimeOntology;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.influxdb.impl.InfluxDBImpl;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InfluxDBOWLTimeReasonerTest {
	String prefix = "http://dl-learner.org/temporal#";
	InfluxDB influxDB;
	OWLDataFactory df = OWLManager.getOWLDataFactory();
	OWLDataProperty dp1 = df.getOWLDataProperty(IRI.create(prefix + "dp1"));
	OWLObjectProperty op1 = df.getOWLObjectProperty(IRI.create(prefix + "op1"));
	OWLObjectProperty hop1ObjProp = df.getOWLObjectProperty(IRI.create(prefix + "hop1ObjProp"));
	OWLObjectProperty hop2ObjProp = df.getOWLObjectProperty(IRI.create(prefix + "hop2ObjProp"));
	OWLObjectProperty hop3ObjProp = df.getOWLObjectProperty(IRI.create(prefix + "hop3ObjProp"));

	OWLIndividual indiv1 = df.getOWLNamedIndividual(IRI.create(prefix + "indiv1"));
	OWLIndividual hop1Indiv1 = df.getOWLNamedIndividual(IRI.create(prefix + "hop1Indiv1"));
	OWLIndividual hop2Indiv1 = df.getOWLNamedIndividual(IRI.create(prefix + "hop2Indiv1"));
	OWLIndividual hop3Indiv1 = df.getOWLNamedIndividual(IRI.create(prefix + "hop3Indiv1"));
	String dateTimeString1 = "2342-01-23T23:45:01.230Z";
	OWLLiteral dtimeStampLit1 = df.getOWLLiteral(
			dateTimeString1, OWL2Datatype.XSD_DATE_TIME_STAMP);
	ZonedDateTime dtime1 = ZonedDateTime.parse(dateTimeString1);
	long dtimeStamp1 = 11741154301230L;

	OWLIndividual indiv2 = df.getOWLNamedIndividual(IRI.create(prefix + "indiv2"));
	OWLIndividual hop1Indiv2 = df.getOWLNamedIndividual(IRI.create(prefix + "hop1Indiv2"));
	OWLIndividual hop2Indiv2 = df.getOWLNamedIndividual(IRI.create(prefix + "hop2Indiv2"));
	OWLIndividual hop3Indiv2 = df.getOWLNamedIndividual(IRI.create(prefix + "hop3Indiv2"));
	String dateTimeString2 = "2342-01-23T23:45:12.240Z";
	OWLLiteral dtimeStampLit2 = df.getOWLLiteral(
			dateTimeString2, OWL2Datatype.XSD_DATE_TIME_STAMP);
	ZonedDateTime dtime2 = ZonedDateTime.parse(dateTimeString2);
	long dtimeStamp2 = 11741154312240L;

	OWLIndividual indiv3 = df.getOWLNamedIndividual(IRI.create(prefix + "indiv3"));
	OWLIndividual hop1Indiv3 = df.getOWLNamedIndividual(IRI.create(prefix + "hop1Indiv3"));
	OWLIndividual hop2Indiv3 = df.getOWLNamedIndividual(IRI.create(prefix + "hop2Indiv3"));
	OWLIndividual hop3Indiv3 = df.getOWLNamedIndividual(IRI.create(prefix + "hop3Indiv3"));
	String dateTimeString3 = "2342-01-23T23:46:34.250Z";
	OWLLiteral dtimeStampLit3 = df.getOWLLiteral(
			dateTimeString3, OWL2Datatype.XSD_DATE_TIME_STAMP);
	ZonedDateTime dtime3 = ZonedDateTime.parse(dateTimeString3);
	long dtimeStamp3 = 11741154394250L;

	@Before
	public void setUp() {
		influxDB = mock(InfluxDBImpl.class);
	}

	private KnowledgeSource ks1() throws OWLOntologyCreationException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ont = man.createOntology();

		Set<OWLAxiom> axioms = Sets.newHashSet(
			df.getOWLDataPropertyAssertionAxiom(dp1, indiv1, dtimeStampLit1),
			df.getOWLDataPropertyAssertionAxiom(dp1, indiv2, dtimeStampLit2),
			df.getOWLDataPropertyAssertionAxiom(dp1, indiv3, dtimeStampLit3)
		);

		man.addAxioms(ont, axioms);

		return new OWLAPIOntology(ont);
	}

	/** Test of a zero-hop property chain. */
	@Test
	public void testResolvePropertyChainEnds1() throws OWLOntologyCreationException, ComponentInitException {
		KnowledgeSource ks = ks1();
		ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
		cwr.init();

		InfluxDBOWLTimeReasoner reasoner = new InfluxDBOWLTimeReasoner();
		reasoner.setSources(ks);
		reasoner.fileReasoner = cwr;

		List<OWLObjectProperty> propChain = Lists.newArrayList();
		assertEquals(Sets.newHashSet(indiv1), reasoner.resolvePropertyChainEnds(indiv1, propChain));

		propChain = Lists.newArrayList();
		assertEquals(Sets.newHashSet(indiv2), reasoner.resolvePropertyChainEnds(indiv2, propChain));

		propChain = Lists.newArrayList();
		assertEquals(Sets.newHashSet(indiv3), reasoner.resolvePropertyChainEnds(indiv3, propChain));
	}

	private KnowledgeSource ks2() throws OWLOntologyCreationException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ont = man.createOntology();

		Set<OWLAxiom> axioms = Sets.newHashSet(
				df.getOWLObjectPropertyAssertionAxiom(hop1ObjProp, indiv1, hop1Indiv1),
				df.getOWLDataPropertyAssertionAxiom(dp1, hop1Indiv1, dtimeStampLit1),
				df.getOWLObjectPropertyAssertionAxiom(hop1ObjProp, indiv2, hop1Indiv2),
				df.getOWLDataPropertyAssertionAxiom(dp1, hop1Indiv2, dtimeStampLit2),
				df.getOWLObjectPropertyAssertionAxiom(hop1ObjProp, indiv3, hop1Indiv3),
				df.getOWLDataPropertyAssertionAxiom(dp1, hop1Indiv3, dtimeStampLit3)
		);

		man.addAxioms(ont, axioms);

		return new OWLAPIOntology(ont);
	}

	/** Test simple one-hop property chain */
	@Test
	public void testResolvePropertyChainEnds2() throws OWLOntologyCreationException, ComponentInitException {
		KnowledgeSource ks = ks2();
		ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
		cwr.init();

		InfluxDBOWLTimeReasoner reasoner = new InfluxDBOWLTimeReasoner();
		reasoner.setSources(ks);
		reasoner.fileReasoner = cwr;

		List<OWLObjectProperty> propChain = Lists.newArrayList(hop1ObjProp);
		assertEquals(Sets.newHashSet(hop1Indiv1), reasoner.resolvePropertyChainEnds(indiv1, propChain));

		propChain = Lists.newArrayList(hop1ObjProp);
		assertEquals(Sets.newHashSet(hop1Indiv2), reasoner.resolvePropertyChainEnds(indiv2, propChain));

		propChain = Lists.newArrayList(hop1ObjProp);
		assertEquals(Sets.newHashSet(hop1Indiv3), reasoner.resolvePropertyChainEnds(indiv3, propChain));
	}

	private KnowledgeSource ks3() throws OWLOntologyCreationException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ont = man.createOntology();

		Set<OWLAxiom> axioms = Sets.newHashSet(
				df.getOWLObjectPropertyAssertionAxiom(hop1ObjProp, indiv1, hop1Indiv1),
				df.getOWLObjectPropertyAssertionAxiom(hop2ObjProp, hop1Indiv1, hop2Indiv1),
				df.getOWLObjectPropertyAssertionAxiom(hop3ObjProp, hop2Indiv1, hop3Indiv1),
				df.getOWLDataPropertyAssertionAxiom(dp1, hop3Indiv1, dtimeStampLit1),
				df.getOWLObjectPropertyAssertionAxiom(hop1ObjProp, indiv2, hop1Indiv2),
				df.getOWLObjectPropertyAssertionAxiom(hop2ObjProp, hop1Indiv2, hop2Indiv2),
				df.getOWLObjectPropertyAssertionAxiom(hop3ObjProp, hop2Indiv2, hop3Indiv2),
				df.getOWLDataPropertyAssertionAxiom(dp1, hop3Indiv2, dtimeStampLit2),
				df.getOWLObjectPropertyAssertionAxiom(hop1ObjProp, indiv3, hop1Indiv3),
				df.getOWLObjectPropertyAssertionAxiom(hop2ObjProp, hop1Indiv3, hop2Indiv3),
				df.getOWLObjectPropertyAssertionAxiom(hop3ObjProp, hop2Indiv3, hop3Indiv3),
				df.getOWLDataPropertyAssertionAxiom(dp1, hop3Indiv3, dtimeStampLit3)
		);

		man.addAxioms(ont, axioms);

		return new OWLAPIOntology(ont);
	}

	/** Test multi-hop property chain */
	@Test
	public void testResolvePropertyChainEnds3() throws OWLOntologyCreationException, ComponentInitException {
		KnowledgeSource ks = ks3();
		ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
		cwr.init();

		InfluxDBOWLTimeReasoner reasoner = new InfluxDBOWLTimeReasoner();
		reasoner.setSources(ks);
		reasoner.fileReasoner = cwr;

		List<OWLObjectProperty> propChain = Lists.newArrayList(hop1ObjProp, hop2ObjProp, hop3ObjProp);
		assertEquals(Sets.newHashSet(hop3Indiv1), reasoner.resolvePropertyChainEnds(indiv1, propChain));

		propChain = Lists.newArrayList(hop1ObjProp, hop2ObjProp, hop3ObjProp);
		assertEquals(Sets.newHashSet(hop3Indiv2), reasoner.resolvePropertyChainEnds(indiv2, propChain));

		propChain = Lists.newArrayList(hop1ObjProp, hop2ObjProp, hop3ObjProp);
		assertEquals(Sets.newHashSet(hop3Indiv3), reasoner.resolvePropertyChainEnds(indiv3, propChain));
	}

	@Test
	public void testLiteralToTimestamp() {
		assertEquals(dtimeStamp1, InfluxDBOWLTimeReasoner.literalToTimestamp(dtimeStampLit1));
		assertEquals(dtimeStamp2, InfluxDBOWLTimeReasoner.literalToTimestamp(dtimeStampLit2));
		assertEquals(dtimeStamp3, InfluxDBOWLTimeReasoner.literalToTimestamp(dtimeStampLit3));
	}

	@Test
	public void testInstant2DateTimeStampLiteral() {
		TimeInstant ti1 = new TimeInstant();
		ti1.time = dtime1.toInstant();
		assertEquals(dtimeStampLit1, InfluxDBOWLTimeReasoner.instant2DateTimeStampLiteral(ti1));

		TimeInstant ti2 = new TimeInstant();
		ti2.time = dtime2.toInstant();
		assertEquals(dtimeStampLit2, InfluxDBOWLTimeReasoner.instant2DateTimeStampLiteral(ti2));

		TimeInstant ti3 = new TimeInstant();
		ti3.time = dtime3.toInstant();
		assertEquals(dtimeStampLit3, InfluxDBOWLTimeReasoner.instant2DateTimeStampLiteral(ti3));
	}

	private OWLClass cls(int id) {
		return df.getOWLClass(IRI.create(prefix + "Cls" + id));
	}

	private OWLNamedIndividual indiv(int id) {
		return df.getOWLNamedIndividual(IRI.create(prefix + "indiv" + id));
	}
	/**
	 * OWLAPIOntology knowledge source containing the simple hierarchy
	 *                      Cls1
	 *               .-----´    `------.
	 *              /                   \
	 *          Cls2 (= time:Instant)   Cls3
	 *         /   \                   /     \
	 *        /     \                 /       \
	 *    Cls4       Cls5         Cls6         Cls7
	 *    /  \       /   \        /   \        /   \
	 * Cls8 Cls9  Cls10 Cls11  Cls12 Cls13  Cls14 Cls15
	 *
	 * with one individual assigned explicitly to each class.
	 */
	private KnowledgeSource ks4() throws OWLOntologyCreationException {
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ont = man.createOntology();

		Set<OWLAxiom> axioms = Sets.newHashSet(
				// Cls1
				df.getOWLClassAssertionAxiom(cls(1), indiv(1)),
				// Cls2
				df.getOWLSubClassOfAxiom(cls(2), cls(1)),
				df.getOWLClassAssertionAxiom(cls(2), indiv(2)),
				// Cls3
				df.getOWLSubClassOfAxiom(cls(3), cls(1)),
				df.getOWLClassAssertionAxiom(cls(3), indiv(3)),
				// Cls4
				df.getOWLSubClassOfAxiom(cls(4), cls(2)),
				df.getOWLClassAssertionAxiom(cls(4), indiv(4)),
				// Cls5
				df.getOWLSubClassOfAxiom(cls(5), cls(2)),
				df.getOWLClassAssertionAxiom(cls(5), indiv(5)),
				// Cls6
				df.getOWLSubClassOfAxiom(cls(6), cls(3)),
				df.getOWLClassAssertionAxiom(cls(6), indiv(6)),
				// Cls7
				df.getOWLSubClassOfAxiom(cls(7), cls(3)),
				df.getOWLClassAssertionAxiom(cls(7), indiv(7)),
				// Cls8
				df.getOWLSubClassOfAxiom(cls(8), cls(4)),
				df.getOWLClassAssertionAxiom(cls(8), indiv(8)),
				// Cls9
				df.getOWLSubClassOfAxiom(cls(9), cls(4)),
				df.getOWLClassAssertionAxiom(cls(9), indiv(9)),
				// Cls10
				df.getOWLSubClassOfAxiom(cls(10), cls(5)),
				df.getOWLClassAssertionAxiom(cls(10), indiv(10)),
				// Cls11
				df.getOWLSubClassOfAxiom(cls(11), cls(5)),
				df.getOWLClassAssertionAxiom(cls(11), indiv(11)),
				// Cls12
				df.getOWLSubClassOfAxiom(cls(12), cls(6)),
				df.getOWLClassAssertionAxiom(cls(12), indiv(12)),
				// Cls13
				df.getOWLSubClassOfAxiom(cls(13), cls(6)),
				df.getOWLClassAssertionAxiom(cls(13), indiv(13)),
				// Cls14
				df.getOWLSubClassOfAxiom(cls(14), cls(7)),
				df.getOWLClassAssertionAxiom(cls(14), indiv(14)),
				// Cls15
				df.getOWLSubClassOfAxiom(cls(15), cls(7)),
				df.getOWLClassAssertionAxiom(cls(15), indiv(15))
		);

		man.addAxioms(ont, axioms);

		return new OWLAPIOntology(ont);
	}

	/**
	 * Tests the standard case where the class to get all super classes of
	 * is not the time interval class set set on the InfluxDBOWLTimeReasoner
	 * instance.
	 */
	@Test
	public void testGetSuperClassesImpl1() throws OWLOntologyCreationException, ComponentInitException, ReasoningMethodUnsupportedException {
		KnowledgeSource ks = ks4();
		ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
		cwr.init();

		InfluxDBOWLTimeReasoner reasoner = new InfluxDBOWLTimeReasoner();
		reasoner.setInfluxDB(influxDB);
		reasoner.setSources(ks);
		reasoner.fileReasoner = cwr;
		reasoner.setTimeInstantClassExpression(cls(2));
		reasoner.setDateTimePropertyPath(
				df.getOWLDataProperty(IRI.create(prefix + "dummy")));
		reasoner.init();

		assertEquals(
				Sets.newHashSet(df.getOWLThing()),
				reasoner.getSuperClassesImpl(cls(1)));

		assertEquals(
				Sets.newHashSet(cls(7)),
				reasoner.getSuperClasses(cls(15)));

		assertEquals(
				Sets.newHashSet(),
				reasoner.getSuperClasses(cls(99)));
	}

	/**
	 * Tests the case where the class to get all super classes of is
	 * the time interval class set set on the InfluxDBOWLTimeReasoner
	 * instance.
	 */
	@Test
	public void testGetSuperClassesImpl2() throws OWLOntologyCreationException, ComponentInitException, ReasoningMethodUnsupportedException {
		KnowledgeSource ks = ks4();
		ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
		cwr.init();

		InfluxDBOWLTimeReasoner reasoner = new InfluxDBOWLTimeReasoner();
		reasoner.setInfluxDB(influxDB);
		reasoner.setSources(ks);
		reasoner.fileReasoner = cwr;
		reasoner.setTimeInstantClassExpression(cls(2));
		reasoner.setDateTimePropertyPath(
				df.getOWLDataProperty(IRI.create(prefix + "dummy")));
		reasoner.init();

		assertEquals(
				Sets.newHashSet(OWLTimeOntology.temporalEntity, cls(1)),
				reasoner.getSuperClassesImpl(cls(2)));

		assertEquals(
				Sets.newHashSet(OWLTimeOntology.temporalEntity, cls(1)),
				reasoner.getSuperClassesImpl(OWLTimeOntology.instant));
	}

	public QueryResult queryResult1() {
		QueryResult res = new QueryResult();

		List<Series> seriesList = new ArrayList<>();
		Series series = new Series();
		series.setColumns(Lists.newArrayList(
				InfluxDBOWLTimeReasoner.timeColumn,
				InfluxDBOWLTimeReasoner.classColumn,
				InfluxDBOWLTimeReasoner.dummyColumn,
				InfluxDBOWLTimeReasoner.individualColumn));
		series.setName(InfluxDBOWLTimeReasoner.intervalTable);
		Map<String, String> tags = new HashMap<>();
//		tags.put("class", "class");
//		tags.put("dummy", "dummy");
//		series.setTags(tags);

		Result r = new Result();
		r.setSeries(seriesList);

		List<Result> results = Lists.newArrayList();
		results.add(r);
		res.setResults(results);

		return res;
	}

	@Test
	public void getDatatypeMembers1() throws OWLOntologyCreationException, ComponentInitException {
		KnowledgeSource ks = ks1();

		ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
		cwr.init();

		InfluxDBOWLTimeReasoner reasoner = new InfluxDBOWLTimeReasoner();

		String queryStr = "SELECT * FROM " + InfluxDBOWLTimeReasoner.instantTable;
		Query query = new Query(queryStr, reasoner.getDBName());
		when(influxDB.query(query)).thenReturn(queryResult1());
		reasoner.setInfluxDB(influxDB);
		reasoner.setSources(ks);
		reasoner.fileReasoner = cwr;
		reasoner.setTimeInstantClassExpression(cls(2));
		reasoner.setDateTimePropertyPath(dp1);
		reasoner.init();

		Map<OWLIndividual, SortedSet<OWLLiteral>> expected = new HashMap<>();
		SortedSet<OWLLiteral> values1 = new TreeSet<>();
		values1.add(dtimeStampLit1);
		expected.put(indiv1, values1);

		SortedSet<OWLLiteral> values2 = new TreeSet<>();
		values2.add(dtimeStampLit2);
		expected.put(indiv2, values2);

		SortedSet<OWLLiteral> values3 = new TreeSet<>();
		values3.add(dtimeStampLit3);
		expected.put(indiv3, values3);

		assertEquals(
				expected,
				reasoner.getDatatypeMembers(dp1));
	}

	@Test
	public void getDatatypeMembers2() throws OWLOntologyCreationException, ComponentInitException, ReasoningMethodUnsupportedException {
		KnowledgeSource ks = ks1();

		ClosedWorldReasoner cwr = new ClosedWorldReasoner(ks);
		cwr.init();

		InfluxDBOWLTimeReasoner reasoner = new InfluxDBOWLTimeReasoner();

		String queryStr = "SELECT * FROM " + InfluxDBOWLTimeReasoner.instantTable;
		Query query = new Query(queryStr, reasoner.getDBName());
		when(influxDB.query(query)).thenReturn(queryResult1());
		reasoner.setInfluxDB(influxDB);
		reasoner.setSources(ks);
		reasoner.fileReasoner = cwr;
		reasoner.setTimeInstantClassExpression(cls(2));
		reasoner.setDateTimePropertyPath(dp1);
		reasoner.init();

		Map<OWLIndividual, SortedSet<OWLLiteral>> expected = new HashMap<>();
		SortedSet<OWLLiteral> values1 = new TreeSet<>();
		OWLLiteral lit1 = df.getOWLLiteral(
				Integer.toString(dtime1.getDayOfMonth()),
				df.getOWLDatatype(XSDVocabulary.G_DAY.getIRI())
				);
		values1.add(lit1);
		expected.put(indiv1, values1);

		SortedSet<OWLLiteral> values2 = new TreeSet<>();
		values2.add(dtimeStampLit2);
		expected.put(indiv2, values2);

		SortedSet<OWLLiteral> values3 = new TreeSet<>();
		values3.add(dtimeStampLit3);
		expected.put(indiv3, values3);

		assertEquals(
				expected,
				reasoner.getDatatypeMembersImpl(OWLTimeOntology.day));
	}
}
