package org.dllearner.algorithm.tbsl.learning;

import static org.junit.Assert.assertTrue;
import org.ini4j.Options;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import opennlp.tools.postag.POSTagger;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithm.tbsl.ltag.parser.Parser;
import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.StanfordPartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.templator.Templator;
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
import org.openjena.atlas.logging.Log;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/** Tests TSBL against the qald2 benchmark test data with the DBpedia endpoint.
 * The qald2 endpoint is not used because it may not always be available.
 * To speed up the process at first the test file is read and an updated copy of it is saved that
 * only contains the questions where the reference query does not return a nonempty list of resources.
 * This could be questions which return literals, ask queries, queries which have no results in the DBpedia endpoint
 * and queries that cause errors. This updated test file contains the reference answers as well and is only created once.
 * Because there are multiple queries that are not all valid at first, further test runs are compared against the first run.
 * The updated test data and the test runs are saved in the cache folder in the same format as the original test data
 * (an xml with the tags question, query and answer).
 * A test fails if it generates questions whose generated queries fail while in the first test run it worked.
 * Because the logging in the dl-learner is so verbose (TODO: rewrite all prints to logging statements), the 
 * logging output is also wrote to the file log/#classname.
 * @author Konrad Höffner
 *  **/
public class SPARQLTemplateBasedLearner3Test
{		
	@Test public void testDBpedia() throws ParserConfigurationException, SAXException, IOException, TransformerException, ComponentInitException, NoTemplateFoundException
	{test(new File(getClass().getClassLoader().getResource("tbsl/evaluation/qald2-dbpedia-train.xml").getFile()),"http://live.dbpedia.org/sparql");}
	//@Test public void testOxford() {test(new File(""),"");}

	public void test(File file, String endpoint) throws ParserConfigurationException, SAXException, IOException, TransformerException, ComponentInitException, NoTemplateFoundException
	{
		String dir = "cache/"+getClass().getSimpleName()+"/";
		new File(dir).mkdirs();
		File updatedFile=new File(dir+"updated_"+file.getName());
		if(!updatedFile.exists()) {generateUpdatedFile(file,updatedFile,endpoint);}

		QueryTestData savedTestData = readQueries(updatedFile);
		QueryTestData newTestData = generateTestData(savedTestData.id2Question);
		Diff QueryTestDataDiff = diffTestData(savedTestData,newTestData);
	}

	/**
	 * @param savedTestData
	 * @param newTestData
	 * @return
	 */
	private Diff diffTestData(QueryTestData d, QueryTestData e)
	{
//		if(d.id2Question.size()!=e.id2Question.size())
		{logger.info("comparing test data D against E. number of questions: "+d.id2Question.size()+" vs "+e.id2Question.size());}
		
		Set<Integer> dMinusE = new HashSet<Integer>(d.id2Question.keySet());
		dMinusE.removeAll(e.id2Question.keySet());		
		if(!dMinusE.isEmpty()) logger.info("questions D/E: "+dMinusE+" ("+dMinusE.size()+" elements)");
		
		Set<Integer> eMinusD = new HashSet<Integer>(e.id2Question.keySet());
		eMinusD.removeAll(d.id2Question.keySet());		
		if(!eMinusD.isEmpty()) logger.info("questions E/D: "+eMinusD+" ("+eMinusD.size()+" elements)");
		
		Set<Integer> intersection = new HashSet<Integer>(d.id2Question.keySet());
		intersection.retainAll(e.id2Question.keySet());
		
		if(!eMinusD.isEmpty()) logger.info("questions E/D: "+eMinusD+" ("+eMinusD.size()+" elements)");
		
		
		// TODO Auto-generated method stub
		return null;
	}

	private class Diff
	{

	}

	/**	
	 * @return the test data containing those of the given questions for which queries were found and the results of the queries  	 
	 */
	private QueryTestData generateTestData(SortedMap<Integer, String> id2Question) throws MalformedURLException, ComponentInitException
	{
		QueryTestData testData = new QueryTestData();
		// -- only create the learner parameters once to save time -- 
		PartOfSpeechTagger posTagger = new StanfordPartOfSpeechTagger();
		WordNet wordnet = new WordNet();
		Options options = new Options();
		// ----------------------------------------------------------
		int successes = 0;
		for(int i:id2Question.keySet())
		{				
			String question = id2Question.get(i);
			logger.debug("generating query for question \""+question+"\", id "+i);			
			long start = System.currentTimeMillis();	
			SPARQLTemplateBasedLearner2 dbpediaLiveLearner = new SPARQLTemplateBasedLearner2(dbpediaLiveKnowledgebase,posTagger,wordnet,options);

			dbpediaLiveLearner.init();
			dbpediaLiveLearner.setQuestion(question);
			
			try{dbpediaLiveLearner.learnSPARQLQueries();}
			catch(NoTemplateFoundException e) {continue;}
			catch(Exception e) {logger.error("Error processing question "+question,e);continue;}
			successes++;			
			testData.id2Question.put(i, question);
			String learnedQuery = dbpediaLiveLearner.getBestSPARQLQuery();
			testData.id2Query.put(i, learnedQuery);
						
			long end = System.currentTimeMillis();
			logger.debug(String.format("Generated query \"%s\" after %d ms", learnedQuery,end-start));

		}
		logger.info(String.format("Successfully learned queries for %d of %d questions.",successes,id2Question.size()));
		// TODO Auto-generated method stub
		return testData;
	}

	/**
	 * @param file
	 * @param updatedFile
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws TransformerException 
	 */
	private void generateUpdatedFile(File originalFile, File updatedFile,String endpoint) throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		logger.info(String.format("Updating question file \"%s\" by removing questions without nonempty resource list answer and adding answers.\n" +
				" Saving the result to file \"%s\"",originalFile.getPath(),updatedFile.getPath()));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(originalFile);

		doc.getDocumentElement().normalize();
		NodeList questionNodes = doc.getElementsByTagName("question");			
		List<Element> questionElementsToDelete = new LinkedList<Element>(); 
		int id;
		String question;
		String query;
		//			Set<String> answers;

		for(int i = 0; i < questionNodes.getLength(); i++)
		{				
			Element questionNode = (Element) questionNodes.item(i);
			//keep the id to aid comparison between original and updated files 
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
				Set<String> uris = getUris(endpoint, query);
				if(!uris.isEmpty())
				{
					// remove reference answers of the benchmark because they are obtained from an other endpoint
					Element existingAnswersElement = (Element)questionNode.getElementsByTagName("answers").item(0); // there is at most one "answers"-element
					if(existingAnswersElement!=null) {questionNode.removeChild(existingAnswersElement);} 

					Element answersElement =  doc.createElement("answers");
					questionNode.appendChild(answersElement);
					for(String uri:uris)
					{							
						Element answerElement =  doc.createElement("answer");
						answerElement.setTextContent(uri);
						answersElement.appendChild(answerElement);
					}		
					System.out.print('.');
					continue;
				}
			}
			// no answers gotten, mark for deletion
			questionElementsToDelete.add(questionNode);
			System.out.print('x');
		}
		for(Element element: questionElementsToDelete) {doc.getDocumentElement().removeChild(element);}

		TransformerFactory tFactory =
				TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream(updatedFile));
		transformer.transform(source, result);			  

		//		catch (DOMException e) {
		//			e.printStackTrace();
		//		} catch (ParserConfigurationException e) {
		//			e.printStackTrace();
		//		} catch (SAXException e) {
		//			e.printStackTrace();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
	}


	int correctMatches = 0;
	int numberOfNoTemplateFoundExceptions = 0;
	int numberOfOtherExceptions = 0;
	//	int successfullTestThreadRuns = 0;

	/** */
	private static final String	DBPEDIA_LIVE_ENDPOINT_URL_STRING	= "http://live.dbpedia.org/sparql";

	private static Logger logger = Logger.getLogger(SPARQLTemplateBasedLearner3Test.class);

	//	private SPARQLTemplateBasedLearner2 oxfordLearner;
	//	private SPARQLTemplateBasedLearner2 dbpediaLiveLearner;

	private ExtractionDBCache oxfordCache = new ExtractionDBCache("cache");
	private ExtractionDBCache dbpediaLiveCache = new ExtractionDBCache("cache");

	private Knowledgebase dbpediaLiveKnowledgebase = createDBpediaLiveKnowledgebase(dbpediaLiveCache);
	
	static SparqlEndpoint dbpediaLiveEndpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
	//static SparqlEndpoint oxfordEndpoint;

	private ResultSet executeDBpediaLiveSelect(String query){return SparqlQuery.convertJSONtoResultSet(dbpediaLiveCache.executeSelectQuery(dbpediaLiveEndpoint, query));}
//	private ResultSet executeOxfordSelect(String query){return SparqlQuery.convertJSONtoResultSet(oxfordCache.executeSelectQuery(oxfordEndpoint, query));}

//	@Test public void benchmarkCreateOxfordKnowledgeBase()
//	{
//		long start = System.currentTimeMillis();
//		for(int i=0;i<1000;i++)
//		{
//			createOxfordKnowledgebase(oxfordCache);
//		}
//		long end = System.currentTimeMillis();
//		long diff = end-start;
//		System.out.println(diff+" millis as a whole, "+diff/1000.0+" millis per run");
//	}

//	private Knowledgebase createOxfordKnowledgebase(ExtractionDBCache cache)
//	{
//		URL url;
//		try{url = new URL("http://lgd.aksw.org:8900/sparql");} catch(Exception e) {throw new RuntimeException(e);}
//		SparqlEndpoint endpoint = new SparqlEndpoint(url, Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());
//
//		SPARQLIndex resourcesIndex = new VirtuosoResourcesIndex(endpoint, cache);
//		SPARQLIndex classesIndex = new VirtuosoClassesIndex(endpoint, cache);
//		SPARQLIndex propertiesIndex = new VirtuosoPropertiesIndex(endpoint, cache);
//		MappingBasedIndex mappingIndex= new MappingBasedIndex(
//				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_class_mappings.txt").getPath(), 
//				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_resource_mappings.txt").getPath(),
//				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_dataproperty_mappings.txt").getPath(),
//				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_objectproperty_mappings.txt").getPath()
//				);
//
//		Knowledgebase kb = new Knowledgebase(oxfordEndpoint, "Oxford - Real estate", "TODO", resourcesIndex, propertiesIndex, classesIndex, mappingIndex);
//		return kb;
//	}

	private Knowledgebase createDBpediaLiveKnowledgebase(ExtractionDBCache cache)
	{		
		SOLRIndex resourcesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_resources");
		resourcesIndex.setPrimarySearchField("label");
		//			resourcesIndex.setSortField("pagerank");
		Index classesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_classes");
		Index propertiesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_properties");

		MappingBasedIndex mappingIndex= new MappingBasedIndex(
				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("test/dbpedia_class_mappings.txt").getPath(), 
				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("test/dbpedia_resource_mappings.txt").getPath(),
				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("test/dbpedia_dataproperty_mappings.txt").getPath(),
				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("test/dbpedia_objectproperty_mappings.txt").getPath()
				);

		Knowledgebase kb = new Knowledgebase(dbpediaLiveEndpoint, "DBpedia Live", "TODO", resourcesIndex, propertiesIndex, classesIndex, mappingIndex);
		return kb;
	}

	@Before
	public void setup() throws IOException
	{			
		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger(Templator.class).setLevel(Level.WARN);
		Logger.getLogger(Parser.class).setLevel(Level.WARN);
		Logger.getLogger(SPARQLTemplateBasedLearner2.class).setLevel(Level.INFO);
		logger.setLevel(Level.ALL); // TODO: remove when finishing implementation of this class
		logger.addAppender(new FileAppender(new SimpleLayout(), "log/"+this.getClass().getSimpleName()+".log", false));
		//		oxfordEndpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());		
		//		oxfordLearner = new SPARQLTemplateBasedLearner2(createOxfordKnowledgebase(oxfordCache));
	}

	private class QueryTestData
	{
		public SortedMap<Integer, String> id2Question = new TreeMap<Integer, String>();
		public SortedMap<Integer, String> id2Query = new TreeMap<Integer, String>();
		public SortedMap<Integer, Set<String>> id2Answers = new TreeMap<Integer, Set<String>>();
	}

	private QueryTestData readQueries(final File file)
	{
		QueryTestData testData = new QueryTestData();
		logger.info("Reading file containing queries and answers...");
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList questionNodes = doc.getElementsByTagName("question");
			int id;

			for(int i = 0; i < questionNodes.getLength(); i++)
			{
				String question;
				String query;
				Set<String> answers = new HashSet<String>();
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
					testData.id2Question.put(id, question);
					testData.id2Query.put(id, query);
					Element answersElement = (Element) questionNode.getElementsByTagName("answers").item(0);
					if(answersElement!=null)
					{
						NodeList answerElements = answersElement.getElementsByTagName("answer");						
						for(int j=0; j<answerElements.getLength();j++)
						{
							String answer = ((Element)answerElements.item(j)).getTextContent();
							answers.add(answer);
						}
						testData.id2Answers.put(id, answers);
					}
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
		return testData;
	}

	private Set<String> getUris(String endpoint, String query)
	{
		if(!query.contains("SELECT")&&!query.contains("select")) {return Collections.<String>emptySet();} // abort when not a select query
		Set<String> uris = new HashSet<String>();
		QueryEngineHTTP qe = new QueryEngineHTTP(DBPEDIA_LIVE_ENDPOINT_URL_STRING, query);
		ResultSet rs = qe.execSelect();		
		String variable = "?uri";
		resultsetloop:
			while(rs.hasNext())
			{
				QuerySolution qs = rs.nextSolution();						
				RDFNode node = qs.get(variable);			
				if(node!=null&&node.isResource())
				{
					String uri=node.asResource().getURI();
					uris.add(uri);			
				}
				else // there is no variable "uri" 
				{
					// try to guess the correct variable by using the first one which is assigned to a resource
					for(Iterator<String> it = qs.varNames();it.hasNext();)
					{
						String varName = it.next();
						RDFNode node2 = qs.get(varName); 
						if(node2.isResource())					 					
						{
							variable = "?"+varName;
							String uri=node2.asResource().getURI();
							uris.add(uri);
							continue resultsetloop;
						}				
					}
					return Collections.<String>emptySet(); // we didn't a resource for the first query solution - give up and don't look in the others
				}
			}
		return uris;
	}

	private class TestQueryThread implements Runnable
	{
		private String question;
		private String referenceQuery;

		public TestQueryThread(String question, String referenceQuery)
		{
			this.question=question;
			this.referenceQuery=referenceQuery;
		}
		//		String referenceQuery 	= id2Query.get(i);
		//		String question = id2Question.get(i);
		@Override public void run()
		{

			logger.trace("question: "+question);

			// TODO: check for query isomorphism and leave out result comparison if possible
			// TODO: only load the reference answers once and permanently cache them somehow (file, ehcache, serialization, ...)
			// get the answers for the gold standard query
			logger.trace("reference query: "+referenceQuery);

			try
			{			
				Set<String> referenceURIs = getUris(DBPEDIA_LIVE_ENDPOINT_URL_STRING,referenceQuery);			

				// learn query
				SPARQLTemplateBasedLearner2 dbpediaLiveLearner = new SPARQLTemplateBasedLearner2(createDBpediaLiveKnowledgebase(dbpediaLiveCache));
				dbpediaLiveLearner.init();
				dbpediaLiveLearner.setQuestion(question);
				dbpediaLiveLearner.learnSPARQLQueries();
				String learnedQuery = dbpediaLiveLearner.getBestSPARQLQuery();					

				logger.trace(learnedQuery);

				Set<String> learnedURIs = getUris(DBPEDIA_LIVE_ENDPOINT_URL_STRING,learnedQuery);

				logger.trace("referenced uris: "+referenceURIs);
				logger.trace("learned uris: "+learnedURIs);

				boolean correctMatch = referenceURIs.equals(learnedURIs);
				logger.trace(correctMatch?"matches":"doesn't match");
				if(correctMatch) {synchronized(this) {correctMatches++;}}
			}
			catch(NoTemplateFoundException e)
			{
				synchronized(this) {numberOfNoTemplateFoundExceptions++;}
				logger.warn(String.format("no template found for question \"%s\"",question));
			}
			catch(Exception e)
			{
				synchronized(this) {numberOfOtherExceptions++;}
				logger.error(String.format("Exception for question \"%s\": %s",question,e.getLocalizedMessage()));
				e.printStackTrace();
				// maybe the exception has corrupted the learner? better create a new one
				//		
			}
			// get the answers for the learned query
			// compare gold standard query and learned query answers						
		}

	}

	private void updateFile(File originalFile, File updatedFile, String endpoint)
	{


	}

	//	private void test(File file) throws MalformedURLException, InterruptedException
	//	{
	//		SortedMap<Integer, String> id2Question = new TreeMap<Integer, String>();
	//		SortedMap<Integer, String> id2Query = new TreeMap<Integer, String>();
	//		SortedMap<Integer, Set<String>> id2Answers = new TreeMap<Integer, Set<String>>();
	//
	//		{			
	//			//			URL url = getClass().getClassLoader().getResource(s);
	//			//			assertFalse("resource not found: "+s,url==null);			
	//			//			readQueries(new File(url.getPath()),id2Question,id2Query);
	//			readQueries(file);
	//		}
	//		assertTrue("no questions loaded",id2Question.size()>0);
	//		logger.info(id2Question.size()+" questions loaded.");
	//		assertTrue(String.format("number of questions (%d) != number of queries (%d).",id2Question.size(),id2Query.size()),
	//				id2Question.size()==id2Query.size());
	//
	//		// get question and answer from file 
	//		dbpediaLiveEndpoint = new SparqlEndpoint(new URL(DBPEDIA_LIVE_ENDPOINT_URL_STRING), Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
	//
	//		//		dbpediaLiveLearner = new SPARQLTemplateBasedLearner2(createDBpediaLiveKnowledgebase(dbpediaLiveCache));
	//		//		dbpediaLiveLearner.init();
	//
	//		// TODO: use thread pools
	//		ExecutorService service = Executors.newFixedThreadPool(10);
	//		for(int i: id2Query.keySet())
	//		{
	//			Runnable r = new TestQueryThread(id2Question.get(i),id2Query.get(i));
	//			service.execute(r);
	//		}
	//		boolean timeout =!service.awaitTermination(600, TimeUnit.SECONDS);
	//
	//		logger.info(timeout?"timeout":"finished all threads");
	//		if(numberOfNoTemplateFoundExceptions>0)	{logger.warn(numberOfNoTemplateFoundExceptions+" NoTemplateFoundExceptions");}
	//		if(numberOfOtherExceptions>0)			{logger.error(numberOfOtherExceptions+" other exceptions");}
	//		assertTrue(String.format("only %d/%d correct answers",correctMatches,id2Query.size()),correctMatches==id2Query.size());
	//
	//		//		dbpediaLiveLearner.setQuestion("houses with more than 2 bedrooms");
	//		//		dbpediaLiveLearner.learnSPARQLQueries();
	//		//		String learnedQuery = dbpediaLiveLearner.getBestSPARQLQuery();
	//		//		logger.trace(learnedQuery);
	//		//fail("Not yet implemented");		
	//	}

	//	@Test public void testDBpediaX() throws NoTemplateFoundException, ComponentInitException, MalformedURLException, InterruptedException
	//	{
	//		// original file - qald benchmark xml
	//		// updated file - delete questions giving no nonempty list of resources (literals, ask query, no result or error)   
	//		final String originalDirName = "tbsl/evaluation";
	//		final String updatedDirName = "cache";		
	//		final File processedDir = new File(updatedDirName);
	//
	//		if(!processedDir.exists()) {processedDir.mkdirs();} 
	//
	//		final String originalFilename = "qald2-dbpedia-train.xml";
	//		final String updatedFilename = "processed_"+originalFilename;
	//		final File originalFile = new File(originalDirName+'/'+originalFilename);		
	//		final File updatedFile = new File(updatedDirName+'/'+updatedFilename);
	//
	//		if(!updatedFile.exists()) {updateFile(originalFile,updatedFile,DBPEDIA_LIVE_ENDPOINT_URL_STRING);}
	//
	//		test(updatedFile);
	//
	//	}
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