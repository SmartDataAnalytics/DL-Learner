package org.dllearner.algorithm.tbsl.learning;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithm.tbsl.util.Knowledgebase;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.common.index.SOLRIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.VirtuosoClassesIndex;
import org.dllearner.common.index.VirtuosoPropertiesIndex;
import org.dllearner.common.index.VirtuosoResourcesIndex;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/** @author konrad * */
public class SPARQLTemplateBasedLearner3Test
{
	/** */
	private static final String	DBPEDIA_LIVE_ENDPOINT_URL_STRING	= "http://live.dbpedia.org/sparql";

	private static Logger logger = Logger.getLogger(SPARQLTemplateBasedLearner3Test.class);

	private SPARQLTemplateBasedLearner3 oxfordLearner;
	private SPARQLTemplateBasedLearner3 dbpediaLiveLearner;

	private ExtractionDBCache oxfordCache = new ExtractionDBCache("cache");
	private ExtractionDBCache dbpediaLiveCache = new ExtractionDBCache("cache");

	SparqlEndpoint dbpediaLiveEndpoint;
	SparqlEndpoint oxfordEndpoint;

	private ResultSet executeDBpediaLiveSelect(String query){return SparqlQuery.convertJSONtoResultSet(dbpediaLiveCache.executeSelectQuery(dbpediaLiveEndpoint, query));}
	private ResultSet executeOxfordSelect(String query){return SparqlQuery.convertJSONtoResultSet(oxfordCache.executeSelectQuery(oxfordEndpoint, query));}

	private Knowledgebase createOxfordKnowledgebase(ExtractionDBCache cache) throws MalformedURLException
	{
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());

		SPARQLIndex resourcesIndex = new VirtuosoResourcesIndex(endpoint, cache);
		SPARQLIndex classesIndex = new VirtuosoClassesIndex(endpoint, cache);
		SPARQLIndex propertiesIndex = new VirtuosoPropertiesIndex(endpoint, cache);
		MappingBasedIndex mappingIndex= new MappingBasedIndex(
				this.getClass().getClassLoader().getResource("tbsl/oxford_class_mappings.txt").getPath(), 
				this.getClass().getClassLoader().getResource("tbsl/oxford_resource_mappings.txt").getPath(),
				this.getClass().getClassLoader().getResource("tbsl/oxford_dataproperty_mappings.txt").getPath(),
				this.getClass().getClassLoader().getResource("tbsl/oxford_objectproperty_mappings.txt").getPath()
				);

		Knowledgebase kb = new Knowledgebase(oxfordEndpoint, "Oxford - Real estate", "TODO", resourcesIndex, propertiesIndex, classesIndex, mappingIndex);
		return kb;
	}

	private Knowledgebase createDBpediaLiveKnowledgebase(ExtractionDBCache cache) throws MalformedURLException
	{		
		SOLRIndex resourcesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_resources");
		resourcesIndex.setPrimarySearchField("label");
		//			resourcesIndex.setSortField("pagerank");
		Index classesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_classes");
		Index propertiesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_properties");

		MappingBasedIndex mappingIndex= new MappingBasedIndex(
				this.getClass().getClassLoader().getResource("test/dbpedia_class_mappings.txt").getPath(), 
				this.getClass().getClassLoader().getResource("test/dbpedia_resource_mappings.txt").getPath(),
				this.getClass().getClassLoader().getResource("test/dbpedia_dataproperty_mappings.txt").getPath(),
				this.getClass().getClassLoader().getResource("test/dbpedia_objectproperty_mappings.txt").getPath()
				);

		Knowledgebase kb = new Knowledgebase(dbpediaLiveEndpoint, "DBpedia Live", "TODO", resourcesIndex, propertiesIndex, classesIndex, mappingIndex);
		return kb;
	}

		@Before
		public void setup() throws MalformedURLException
		{
			logger.setLevel(Level.ALL);
	//		oxfordEndpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());		
	//		oxfordLearner = new SPARQLTemplateBasedLearner3(createOxfordKnowledgebase(oxfordCache));
		}

	private void readQueries(final File file,final SortedMap<Integer, String> id2Question,final SortedMap<Integer, String> id2Query)
	{
		id2Question.clear();
		id2Query.clear();
		logger.info("Reading file containing queries and answers...");
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList questionNodes = doc.getElementsByTagName("question");
			int id;
			String question;
			String query;
			Set<String> answers;

			for(int i = 0; i < questionNodes.getLength(); i++){
				Element questionNode = (Element) questionNodes.item(i);
				//read question ID
				id = Integer.valueOf(questionNode.getAttribute("id"));
				//Read question
				question = ((Element)questionNode.getElementsByTagName("string").item(0)).getChildNodes().item(0).getNodeValue().trim();
				//Read SPARQL query
				query = ((Element)questionNode.getElementsByTagName("query").item(0)).getChildNodes().item(0).getNodeValue().trim();
				//				//Read answers
				//				answers = new HashSet<String>();
				//				NodeList aswersNodes = questionNode.getElementsByTagName("answer");
				//				for(int j = 0; j < aswersNodes.getLength(); j++){
				//					Element answerNode = (Element) aswersNodes.item(j);
				//					answers.add(((Element)answerNode.getElementsByTagName("uri").item(0)).getChildNodes().item(0).getNodeValue().trim());
				//				}

				if(!query.equals("OUT OF SCOPE")) // marker in qald benchmark file, will create holes interval of ids (e.g. 1,2,5,7)   
				{
					id2Question.put(id, question);
					id2Query.put(id, query);									
				}				
				//				question2Answers.put(question, answers);

			}
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//		StringBuilder sb = new StringBuilder();
		//		for(Entry<Integer, String> e : id2Question.entrySet()){
		//			sb.append(e.getKey()+ ": " + extractSentence(e.getValue()) + "\n");
		//		}
		//		try {
		//			BufferedWriter out = new BufferedWriter(new FileWriter("questions.txt"));
		//			out.write(sb.toString());
		//			out.close();
		//			} 
		//			catch (IOException e) 
		//			{ 
		//			System.out.println("Exception ");
		//
		//			}
		logger.info("Done.");
	}

	private Set<String> getURIs(String endpoint, String query)
	{
		Set<String> uris = new HashSet<String>();
		QueryEngineHTTP qe = new QueryEngineHTTP(DBPEDIA_LIVE_ENDPOINT_URL_STRING, query);
		ResultSet rs = qe.execSelect();		
		while(rs.hasNext())
		{
			QuerySolution qs = rs.nextSolution();
			String uri = qs.getResource("?uri").getURI();
			uris.add(uri);
		}
		return uris;
	}

	@Test public void testDBpedia() throws NoTemplateFoundException, ComponentInitException, MalformedURLException
	{
		SortedMap<Integer, String> id2Question = new TreeMap<Integer, String>();
		SortedMap<Integer, String> id2Query = new TreeMap<Integer, String>();
		SortedMap<Integer, Object> id2Answer = new TreeMap<Integer, Object>();

		{
			String s = "tbsl/evaluation/qald2-dbpedia-train.xml";			
			URL url = getClass().getClassLoader().getResource(s);
			assertFalse("resource not found: "+s,url==null);			
			readQueries(new File(url.getPath()),id2Question,id2Query);
		}
		assertTrue("no questions loaded",id2Question.size()>0);
		logger.info(id2Question.size()+" questions loaded.");
		assertTrue(String.format("no number of questions (%n) != number of queries (%n).",id2Question.size(),id2Query.size()),
				id2Question.size()==id2Query.size());

		// get question and answer from file 
		dbpediaLiveEndpoint = new SparqlEndpoint(new URL(DBPEDIA_LIVE_ENDPOINT_URL_STRING), Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
		
		dbpediaLiveLearner = new SPARQLTemplateBasedLearner3(createDBpediaLiveKnowledgebase(dbpediaLiveCache));
		dbpediaLiveLearner.init();
		
		// TODO: use thread pools
		for(int i: id2Query.keySet())
		{
			if(i>3) break; // TODO: remove
			String question = id2Question.get(i);
			logger.trace("question: "+question);
			String referenceQuery 	= id2Query.get(i);			
			// TODO: check for query isomorphism and leave out result comparison if possible
			// TODO: only load the reference answers once and permanently cache them somehow (file, ehcache, serialization, ...)
			// get the answers for the gold standard query
			logger.trace("reference query: "+referenceQuery);
			
			Set<String> referenceURIs = getURIs(DBPEDIA_LIVE_ENDPOINT_URL_STRING,referenceQuery);			
		
			// learn query
			dbpediaLiveLearner.setQuestion(question);
			dbpediaLiveLearner.learnSPARQLQueries();
			String learnedQuery = dbpediaLiveLearner.getBestSPARQLQuery();					
			
			logger.trace(learnedQuery);
			
			Set<String> learnedURIs = getURIs(DBPEDIA_LIVE_ENDPOINT_URL_STRING,learnedQuery);
			
			logger.trace(referenceURIs);
			logger.trace(learnedURIs);
			assertTrue(referenceURIs.equals(learnedURIs));
			// get the answers for the learned query
			// compare gold standard query and learned query answers						
		}
		

//		dbpediaLiveLearner.setQuestion("houses with more than 2 bedrooms");
//		dbpediaLiveLearner.learnSPARQLQueries();
//		String learnedQuery = dbpediaLiveLearner.getBestSPARQLQuery();
//		logger.trace(learnedQuery);
		//fail("Not yet implemented");
	}

	//	@Test public void test() throws NoTemplateFoundException, ComponentInitException
	//	{
	//		// get question and answer from file 
	//		
	//		oxfordLearner.init();
	//		oxfordLearner.setQuestion("houses with more than 2 bedrooms");
	//		oxfordLearner.learnSPARQLQueries();
	//		String learnedQuery = oxfordLearner.getBestSPARQLQuery();
	//		
	//		//fail("Not yet implemented");
	//	}

}