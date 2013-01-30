package org.dllearner.scripts;

import java.net.URL;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.scripts.OntologyMatching.KnowledgeBase;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.io.ToStringRenderer;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class OntologyMatchingTest {
	
	private KnowledgeBase dbpedia;
	private KnowledgeBase worldFactBook;
	private KnowledgeBase openCyc;
	private KnowledgeBase linkedGeoData;

	@Before
	public void setUp() throws Exception {
		// render output
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		// set logging properties
		Logger.getLogger(SparqlKnowledgeSource.class).setLevel(Level.WARN);
		Logger.getLogger(ExtractionAlgorithm.class).setLevel(Level.WARN);
		Logger.getLogger(org.dllearner.kb.extraction.Manager.class).setLevel(Level.WARN);
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
		
		//DBpedia
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		ExtractionDBCache cache = new ExtractionDBCache("cache");
		String namespace = "http://dbpedia.org/resource/";
		dbpedia = new KnowledgeBase(endpoint, cache, namespace);
		
		//World Factbook
		//TODO problem with World Factbook is that old FU Berlin server is useless because of bugs and current version
		//is provide by University Of Mannheim now with another namespace http://wifo5-03.informatik.uni-mannheim.de/factbook/resource/
		//but the DBpedia links are still to the old D2R server instance
		//workaround: replace namespace before learning
		endpoint = new SparqlEndpoint(new URL("http://wifo5-03.informatik.uni-mannheim.de/factbook/sparql"));
		cache = new ExtractionDBCache("cache");
		namespace = "http://www4.wiwiss.fu-berlin.de/factbook/resource/";
		worldFactBook = new KnowledgeBase(endpoint, cache, namespace);
		
		//local OpenCyc
		endpoint = new SparqlEndpoint(new URL("http://localhost:8890/sparql"));
		cache = new ExtractionDBCache("cache");
		namespace = "http://sw.cyc.com";
		openCyc = new KnowledgeBase(endpoint, cache, namespace);
		
		//LinkedGeoData
		endpoint = new SparqlEndpoint(new URL("http://linkedgeodata.org/sparql"));
		cache = new ExtractionDBCache("cache");
		namespace = "http://linkedgeodata.org/triplify/";
		linkedGeoData = new KnowledgeBase(endpoint, cache, namespace);
	}

	@Test
	public void testDBpediaWorldFactbook() {
		OntologyMatching matcher = new OntologyMatching(dbpedia, worldFactBook);
		matcher.start();
	}
	
	@Test
	public void testDBpediaOpenCyc() {
		OntologyMatching matcher = new OntologyMatching(dbpedia, openCyc);
		matcher.start();
	}
	
	@Test
	public void testDBpediaLinkedGeoData() {
		OntologyMatching matcher = new OntologyMatching(dbpedia, linkedGeoData);
		matcher.start();
	}

}
