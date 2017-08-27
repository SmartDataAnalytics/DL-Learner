package org.dllearner.reasoning;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.influxdb.InfluxDB;
import org.influxdb.impl.InfluxDBImpl;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.HermiT.datatypes.datetime.DateTime;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class InfluxDBOWLTimeReasonerTest {
	String prefix = "http://dl-learner.org/temporal#";
	InfluxDB influxDB;
	OWLDataFactory df = OWLManager.getOWLDataFactory();
	OWLDataProperty dp1 = df.getOWLDataProperty(IRI.create(prefix + "dp1"));
	OWLObjectProperty op1 = df.getOWLObjectProperty(IRI.create(prefix + "op1"));

	OWLIndividual indiv1 = df.getOWLNamedIndividual(IRI.create(prefix + "indiv1"));
	OWLLiteral dtimeStampLit1 = df.getOWLLiteral(
			"2342-01-23T23:45:01.23+00:00", OWL2Datatype.XSD_DATE_TIME_STAMP);
	DateTime dtime1 = new DateTime(2342, 1, 23, 23, 45, 1, 23, 0);

	OWLIndividual indiv2 = df.getOWLNamedIndividual(IRI.create(prefix + "indiv2"));
	OWLLiteral dtimeStampLit2 = df.getOWLLiteral(
			"2342-01-23T23:45:12.24+00:00", OWL2Datatype.XSD_DATE_TIME_STAMP);
	DateTime dtime2 = new DateTime(2342, 1, 23, 23, 45, 12, 24, 0);

	OWLIndividual indiv3 = df.getOWLNamedIndividual(IRI.create(prefix + "indiv3"));
	OWLLiteral dtimeStampLit3 = df.getOWLLiteral(
			"2342-01-23T23:46:34.25+00:00", OWL2Datatype.XSD_DATE_TIME_STAMP);
	DateTime dtime3 = new DateTime(2342, 1, 23, 23, 46, 34, 25, 0);

	@Before
	public void setUp() {
		influxDB = mock(InfluxDBImpl.class);
	}

	public KnowledgeSource kb1() throws OWLOntologyCreationException {
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

	@Test
	public void testResolvePropertyChainEnds1() throws OWLOntologyCreationException {
		KnowledgeSource kb = kb1();

		InfluxDBOWLTimeReasoner reasoner = new InfluxDBOWLTimeReasoner();
//		reasoner.dateTimePropertyPath = Lists.newArrayList(dp1);
		reasoner.setSources(kb);

		List<OWLObjectProperty> propChain = Lists.newArrayList();

		assertEquals(Sets.newHashSet(indiv1), reasoner.resolvePropertyChainEnds(indiv1, propChain));
	}

}
