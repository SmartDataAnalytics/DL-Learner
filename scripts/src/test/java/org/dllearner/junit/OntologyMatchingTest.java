package org.dllearner.junit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.scripts.OntologyMatching;
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
//		Logger.getRootLogger().setLevel(Level.DEBUG);
		
		//DBpedia
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		ExtractionDBCache cache = new ExtractionDBCache("cache");
		String namespace = "http://dbpedia.org/resource/";
		dbpedia = new KnowledgeBase(endpoint, null, namespace);
		
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
		openCyc = new KnowledgeBase(endpoint, null, namespace);
		
		//LinkedGeoData
		endpoint = new SparqlEndpoint(new URL("http://linkedgeodata.org/sparql"));
		cache = new ExtractionDBCache("cache");
		namespace = "http://linkedgeodata.org/triplify/";
		linkedGeoData = new KnowledgeBase(endpoint, null, namespace);
	}

	@Test
	public void testDBpediaWorldFactbook() {
		OntologyMatching matcher = new OntologyMatching(dbpedia, worldFactBook);
		matcher.start();
		save(matcher.getMappingKB1KB2(), "dbpedia_wfb.html");
		save(matcher.getMappingKB2KB1(), "wfb_dbpedia.html");
	}
	
	@Test
	public void testDBpediaOpenCyc() {
		OntologyMatching matcher = new OntologyMatching(dbpedia, openCyc);
		matcher.start();
		save("dbpedia_opencyc.html", matcher.getMappingKB1KB2(), dbpedia, openCyc);
		save("opencyc_dbpedia.html", matcher.getMappingKB2KB1(), openCyc, dbpedia);
	}
	
	@Test
	public void testDBpediaLinkedGeoData() {
		OntologyMatching matcher = new OntologyMatching(dbpedia, linkedGeoData);
		matcher.start();
		save("dbpedia_lgd.html", matcher.getMappingKB1KB2(), dbpedia, linkedGeoData );
		save("lgd_dbpedia.html", matcher.getMappingKB2KB1(), linkedGeoData, dbpedia);
	}
	
	@Test
	public void testSingleClassOpenCycToDBpedia() {
		OntologyMatching matcher = new OntologyMatching(openCyc, dbpedia);
		NamedClass nc = new NamedClass("http://sw.opencyc.org/concept/Mx4r4fYeXvbPQdiKtoNafhmOew");
		List<? extends EvaluatedDescription> mapping = matcher.computeMapping(nc, openCyc, dbpedia);
		Map<Description, List<? extends EvaluatedDescription>> alignment = new HashMap<Description, List<? extends EvaluatedDescription>>();
		alignment.put(nc, mapping);
		System.out.println(OntologyMatching.toHTMLWithLabels(alignment, openCyc, dbpedia));
	}
	
	@Test
	public void testSingleClassLinkedGeoDataToDBpedia() {
		OntologyMatching matcher = new OntologyMatching(linkedGeoData, dbpedia);
		NamedClass nc = new NamedClass("http://linkedgeodata.org/ontology/Aerodrome");
		List<? extends EvaluatedDescription> mapping = matcher.computeMapping(nc, linkedGeoData, dbpedia);
		Map<Description, List<? extends EvaluatedDescription>> alignment = new HashMap<Description, List<? extends EvaluatedDescription>>();
		alignment.put(nc, mapping);
		System.out.println(OntologyMatching.toHTMLWithLabels(alignment, linkedGeoData, dbpedia));
	}
	
	@Test
	public void testSingleClassDBpediaToLinkedGeoData() {
		OntologyMatching matcher = new OntologyMatching(dbpedia, linkedGeoData);
		NamedClass nc = new NamedClass("http://dbpedia.org/ontology/AdministrativeRegion");
		List<? extends EvaluatedDescription> mapping = matcher.computeMapping(nc, dbpedia, linkedGeoData);
		Map<Description, List<? extends EvaluatedDescription>> alignment = new HashMap<Description, List<? extends EvaluatedDescription>>();
		alignment.put(nc, mapping);
		System.out.println(OntologyMatching.toHTMLWithLabels(alignment, dbpedia, linkedGeoData));
	}
	
	private void save(String filename, Map<Description, List<? extends EvaluatedDescription>> mapping, KnowledgeBase source, KnowledgeBase target){
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(filename));
			out.write(OntologyMatching.toHTMLWithLabels(mapping, source, target));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void save(Map<Description, List<? extends EvaluatedDescription>> mapping, String filename){
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(filename));
			out.write(OntologyMatching.toHTML(mapping));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
