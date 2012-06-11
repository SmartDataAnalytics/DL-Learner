package org.dllearner.algorithm.tbsl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Collections;

import junit.framework.TestCase;

import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner2;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.SOLRIndex;
import org.dllearner.common.index.SPARQLClassesIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.SPARQLPropertiesIndex;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TBSLTest extends TestCase{
	
	private Model model;
	private SparqlEndpoint endpoint;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		endpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());
		model = ModelFactory.createOntologyModel();
		try {
			model.read(new FileInputStream(new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/examples/ontology.ttl")), null, "TURTLE");
			model.read(new FileInputStream(new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/examples/data/wwagency-letting-triple.ttl")), "http://diadem.cs.ox.ac.uk/ontologies/real-estate#", "TURTLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(model.size());
		String queryStr = "PREFIX owl:<http://www.w3.org/2002/07/owl#> SELECT DISTINCT ?uri WHERE {" +
//				"?s ?uri ?o." +
				"{?uri a owl:DatatypeProperty.} UNION {?uri a owl:ObjectProperty.}" + 
				"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label." +
				"FILTER(REGEX(STR(?label), 'bathroom', 'i'))" +
				"}" +
				"LIMIT 20 OFFSET 0";
		System.out.println(
				ResultSetFormatter.asText(
						QueryExecutionFactory.create(
								QueryFactory.create(queryStr, Syntax.syntaxARQ), model).execSelect()));
	}
	
	@Test
	public void testDBpedia() throws Exception{
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://greententacle.techfak.uni-bielefeld.de:5171/sparql"), 
				Collections.<String>singletonList(""), Collections.<String>emptyList());
		Index resourcesIndex = new SOLRIndex("http://139.18.2.173:8080/solr/dbpedia_resources");
		Index classesIndex = new SOLRIndex("http://139.18.2.173:8080/solr/dbpedia_classes");
		Index propertiesIndex = new SOLRIndex("http://139.18.2.173:8080/solr/dbpedia_properties");
		
		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(endpoint, resourcesIndex, classesIndex, propertiesIndex);
		learner.init();
		
		String question = "Give me all books written by Dan Brown";
		
		learner.setQuestion(question);
		learner.learnSPARQLQueries();
		System.out.println("Learned query:\n" + learner.getBestSPARQLQuery());
		System.out.println("Lexical answer type is: " + learner.getTemplates().iterator().next().getLexicalAnswerType());
		System.out.println(learner.getLearnedPosition());
	}
	
	@Test
	public void testOxfordLocal() throws Exception{
		
		Index resourcesIndex = new SPARQLIndex(model);
		Index classesIndex = new SPARQLClassesIndex(model);
		Index propertiesIndex = new SPARQLPropertiesIndex(model);
		
		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(model, resourcesIndex, classesIndex, propertiesIndex);
		learner.init();
		
		String question = "Give me all houses with more than 2 bedrooms.";
		
		learner.setQuestion(question);
		learner.learnSPARQLQueries();
		System.out.println("Learned query:\n" + learner.getBestSPARQLQuery());
		System.out.println("Lexical answer type is: " + learner.getTemplates().iterator().next().getLexicalAnswerType());
		System.out.println(learner.getLearnedPosition());
	}
	
	@Test
	public void testOxfordRemote() throws Exception{
		
		Index resourcesIndex = new SPARQLIndex(endpoint);
		Index classesIndex = new SPARQLClassesIndex(endpoint);
		Index propertiesIndex = new SPARQLPropertiesIndex(endpoint);
		
		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(endpoint, resourcesIndex, classesIndex, propertiesIndex);
		learner.init();
		
		String question = "Give me all houses with more than 2 bedrooms and more than 3 bathrooms.";
		
		learner.setQuestion(question);
		learner.learnSPARQLQueries();
		System.out.println("Learned query:\n" + learner.getBestSPARQLQuery());
		System.out.println("Lexical answer type is: " + learner.getTemplates().iterator().next().getLexicalAnswerType());
		System.out.println(learner.getLearnedPosition());
	}
	
	@Test
	public void testSPARQLIndex(){
		Index classesIndex = new SPARQLClassesIndex(model);
		System.out.println(classesIndex.getResources("flat"));
		
		
	}
	
	@Test
	public void testSPARQLPropertyPathNegation(){
		String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX gr: <http://purl.org/goodrelations/v1#> " +
				"PREFIX ex: <http://diadem.cs.ox.ac.uk/ontologies/real-estate#>" +
				"SELECT * WHERE {?s a gr:Offering. ?s (!rdfs:label)+ ?o. ?o a ex:RoomSpecification.?o ?p ?o1} LIMIT 50";
		System.out.println(QueryFactory.create(query, Syntax.syntaxARQ));
		ResultSet rs  =QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), model).execSelect();
		while(rs.hasNext()){
			System.out.println(rs.next());
		}
		
	}

}
