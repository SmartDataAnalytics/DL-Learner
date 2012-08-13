package org.dllearner.algorithm.tbsl.learning;

import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithm.tbsl.ltag.parser.Parser;
import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.SynchronizedStanfordPartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.templator.Templator;
import org.dllearner.algorithm.tbsl.util.Knowledgebase;
import org.dllearner.common.index.HierarchicalIndex;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.common.index.SOLRIndex;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.ini4j.Options;
import org.junit.*;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import cern.colt.Arrays;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

/** Tests TSBL against the qald2 benchmark test data with the DBpedia endpoint.
 * The qald2 endpoint is not used because it may not always be available.
 * To speed up the process at first the test file is read and an updated copy of it is saved that
 * only contains the questions where the reference query does not return a nonempty list of resources.
 * This could be questions which return literals, ask queries, queries which have no results in the DBpedia endpoint
 * and queries that cause errors. This updated test file contains the reference answers as well and is only created once.
 * The answers in the updated query could be out of date as well, so if the answers don't match they are newly queried from the reference query.
 * Because there are multiple queries that are not all valid at first, further test runs are compared against the first run.
 * The updated test data and the test runs are saved in the cache folder in the same format as the original test data
 * (an xml with the tags question, query and answer).
 * A test fails if it generates questions whose generated queries fail while in the first test run it worked.
 * Because the logging in the dl-learner is so verbose (TODO: rewrite all prints to logging statements), the 
 * logging output is also wrote to the file log/#classname.
 * @author Konrad Höffner
 *  **/

// problem mit "In/IN which/WDT films/NNS did/VBD Julia/NNP Roberts/NNP as/RB well/RB as/IN Richard/NNP Gere/NNP play/NN"
public class SPARQLTemplateBasedLearner3Test
{		
	private static final boolean PRETAGGED = true;

	private static final File evaluationFolder = new File("cache/evaluation");

	/*@Test*/ public void testDBpedia() throws Exception
	{test("QALD 2 Benchmark ideally tagged", new File(getClass().getClassLoader().getResource("tbsl/evaluation/qald2-dbpedia-train-tagged(ideal).xml").getFile()),
			SparqlEndpoint.getEndpointDBpedia(),dbpediaLiveCache);}
	//@Test public void testOxford() {test(new File(""),"");}

	@Test public void justTestTheLastWorkingOnesDBpedia() throws Exception
	{		
		SortedMap<Long,Evaluation> evaluations;
		
		if((evaluations=Evaluation.read()).isEmpty())
		{
			testDBpedia();
			evaluations=Evaluation.read();		
		}
		
		Evaluation latestEvaluation = evaluations.get(evaluations.lastKey());
		for(String question: latestEvaluation.correctlyAnsweredQuestions)
		{
			LearnStatus status = new LearnQueryCallable(question, 0,new QueryTestData() , dbpediaLiveKnowledgebase).call();
			if(status.type!=LearnStatus.Type.OK) {fail("Failed with question \""+question+"\", query status: "+status);}
		}
	}

	public void test(String title, final File referenceXML,final  SparqlEndpoint endpoint,ExtractionDBCache cache) throws ParserConfigurationException, SAXException, IOException, TransformerException, ComponentInitException, NoTemplateFoundException
	{		
		generateTestDataIfNecessaryAndEvaluateAndWrite(title,referenceXML,endpoint,cache);
		generateHTML(title); 

		//				if(evaluation.numberOfCorrectAnswers<3) {fail("only " + evaluation.numberOfCorrectAnswers+" correct answers.");}
		/*		{
							logger.info("Comparing updated reference test data with learned test data:");	
							Diff queryTestDataDiff = diffTestData(referenceTestData,learnedTestData);
							logger.info(queryTestDataDiff);
				}
				logger.info("Comparing learned test data with old learned test data");

				try{
					QueryTestData oldLearnedTestData = QueryTestData.read();
					Diff queryTestDataDiff2 = diffTestData(oldLearnedTestData,learnedTestData);
					logger.info(queryTestDataDiff2);
					//			assertFalse("the following queries did not return an answer in the current learned test data: "+queryTestDataDiff2.aMinusB,
					//					queryTestDataDiff2.aMinusB.isEmpty());
					assertTrue("the following queries had different answers: "+queryTestDataDiff2.differentAnswers,
							queryTestDataDiff2.differentAnswers.isEmpty());

				}
				catch(IOException e)
				{
					logger.info("Old test data not loadable, creating it and exiting.");					
				}
				learnedTestData.write();*/
	}

	private void generateTestDataIfNecessaryAndEvaluateAndWrite(String title, final File referenceXML,final  SparqlEndpoint endpoint,ExtractionDBCache cache) throws ParserConfigurationException, SAXException, IOException, TransformerException, ComponentInitException
	{
		String dir = "cache/"+getClass().getSimpleName()+"/";

		new File(dir).mkdirs();
		File updatedReferenceXML=new File(dir+"updated_"+referenceXML.getName());
		if(!updatedReferenceXML.exists())
		{
			logger.info("Generating updated reference for "+title);
			generateUpdatedXML(referenceXML,updatedReferenceXML,endpoint,cache);
		}

		QueryTestData referenceTestData = readQueries(updatedReferenceXML);
		logger.info(title+" subset loaded with "+referenceTestData.id2Question.size()+" questions.");

		long startLearning = System.currentTimeMillis();
		QueryTestData learnedTestData = generateTestDataMultiThreaded(referenceTestData.id2Question, dbpediaLiveKnowledgebase);
		long endLearning = System.currentTimeMillis();
		logger.info("finished learning after "+(endLearning-startLearning)/1000.0+"s");
		learnedTestData.generateAnswers(endpoint,cache);
		long endGeneratingAnswers = System.currentTimeMillis();
		logger.info("finished generating answers in "+(endGeneratingAnswers-endLearning)/1000.0+"s");
		Evaluation evaluation = evaluate(referenceTestData, learnedTestData); 
		logger.info(evaluation);
		evaluation.write();
	}

	/** evaluates a data set against a reference.
	 * @param reference the test data assumed to be correct. needs to contain the answers for all queries.
	 * @param suspect the test data to compare with the reference.
	 * if a query for a question does not match and the answers are not provided or don't match as well then the question is marked as incorrectly answered.*/
	private static Evaluation evaluate(QueryTestData reference, QueryTestData suspect)
	{
		//		Diff d = diffTestData(reference,testData);	
		Evaluation evaluation = new Evaluation(suspect,reference);
		evaluation.numberOfQuestions = reference.id2Question.keySet().size();

		for(int i: reference.id2Question.keySet())
		{
			String question = reference.id2Question.get(i);
			if(!suspect.id2Query.containsKey(i))
			{
				evaluation.unansweredQuestions.add(question);
				continue;
			}
			evaluation.numberOfAnsweredQuestions++;

			String referenceQuery = reference.id2Query.get(i);
			String suspectQuery = suspect.id2Query.get(i);
			// reference is required to contain answers for every key so we shouldn't get NPEs here (even though it could be the empty set but that shouldn't happen because only questions with nonempty answers are included in the updated reference)
			if(referenceQuery.equals(suspectQuery)||reference.id2Answers.get(i).equals(suspect.id2Answers.get(i)))
			{
				evaluation.correctlyAnsweredQuestions.add(question);
				evaluation.numberOfCorrectAnswers++;
			}
			else
			{
				evaluation.incorrectlyAnsweredQuestions.add(question);
				logger.debug("learned queries differing. reference query:\n"+referenceQuery+"\nsuspect query:\n"+suspectQuery);
				logger.debug("learned answers differing: reference answers:\n"+reference.id2Answers.get(i)+"\nsuspect answers:\n"+suspect.id2Answers.get(i));
			}
		}
		return evaluation;
	}

	static class Evaluation implements Serializable
	{		
		private static final long	serialVersionUID	= 5L;
		final QueryTestData testData;
		final QueryTestData referenceData;
		int numberOfQuestions = 0;
		int numberOfAnsweredQuestions = 0;
		int numberOfCorrectAnswers = 0;
		double precision = 0;
		double recall = 0;	
		final Set<String> unansweredQuestions = new HashSet<String>();
		final Set<String> incorrectlyAnsweredQuestions = new HashSet<String>();
		final Set<String> correctlyAnsweredQuestions = new HashSet<String>();		

		public Evaluation(QueryTestData testData,QueryTestData referenceData) {this.testData = testData;this.referenceData = referenceData;}

		void computePrecisionAndRecall() // we have at maximum one answer set per question
		{
			precision = numberOfCorrectAnswers / numberOfAnsweredQuestions;
			recall = numberOfCorrectAnswers / numberOfQuestions;
		}

		@Override public String toString()
		{
			StringBuffer sb = new StringBuffer();
			sb.append(numberOfAnsweredQuestions+" of "+numberOfQuestions+" questions answered, ");
			sb.append(numberOfCorrectAnswers+" correct answers.");
			sb.append("precision: "+precision+", recall: "+recall+"\n");
			sb.append("Detailed List: ");
			sb.append(toHTML());
			return sb.toString();
		}

		public String toHTML()
		{
			StringBuffer sb = new StringBuffer();
			sb.append(htmlDetailsList("Unanswered Questions",unansweredQuestions));
			sb.append(htmlDetailsList("Wrongly Answered Questions",incorrectlyAnsweredQuestions));
			sb.append(htmlDetailsList("Correctly Answered Questions",correctlyAnsweredQuestions));
			return sb.toString();
		}

		public static String htmlDetailsList(/*@NonNull*/ String summary,/*@NonNull*/ Collection<String> elements)
		{
			if(elements.isEmpty()) {return "<p>"+summary+": none</p>";}

			StringBuffer sb = new StringBuffer();
			sb.append("<p><details>\n<summary>"+summary+"</summary>\n<ul>");
			for(String element: elements)
				sb.append("<li>"+element+"</li>");
			sb.append("</ul>\n</details></p>");
			return sb.toString();
		}

		public synchronized void write()
		{
			evaluationFolder.mkdirs(); 
			try
			{
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(evaluationFolder,String.valueOf(System.currentTimeMillis()))));
				oos.writeObject(this);
				oos.close();
			} catch(IOException e) {throw new RuntimeException(e);}
		}

		public static SortedMap<Long,Evaluation> read()
		{
			SortedMap<Long,Evaluation> evaluations = new ConcurrentSkipListMap<Long,Evaluation>();
			evaluationFolder.mkdirs();
			File[] files = evaluationFolder.listFiles();		
			for(int i=0;i<files.length;i++) {evaluations.put(Long.valueOf(files[i].getName()),read(files[i]));}
			return evaluations;
		}

		private static Evaluation read(File file)
		{
			try
			{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
				Evaluation evaluation = (Evaluation) ois.readObject();
				ois.close();
				return evaluation;
			}
			catch (Exception e){throw new RuntimeException(e);}		
		}

		@Override public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Evaluation other = (Evaluation) obj;
			if (correctlyAnsweredQuestions == null)
			{
				if (other.correctlyAnsweredQuestions != null) return false;
			}
			else if (!correctlyAnsweredQuestions.equals(other.correctlyAnsweredQuestions)) return false;
			if (incorrectlyAnsweredQuestions == null)
			{
				if (other.incorrectlyAnsweredQuestions != null) return false;
			}
			else if (!incorrectlyAnsweredQuestions.equals(other.incorrectlyAnsweredQuestions)) return false;
			if (unansweredQuestions == null)
			{
				if (other.unansweredQuestions != null) return false;
			}
			else if (!unansweredQuestions.equals(other.unansweredQuestions)) return false;
			return true;
		}
	}

	public static class Diff
	{
		final Set<Integer> aMinusB 			= new HashSet<Integer>();
		final Set<Integer> bMinusA 			= new HashSet<Integer>();
		final Set<Integer> intersection 	= new HashSet<Integer>();
		final Set<Integer> differentAnswers	= new HashSet<Integer>();

		public Diff(QueryTestData reference, QueryTestData newData)
		{
			//		if(d.id2Question.size()!=e.id2Question.size())
			//		logger.info("comparing test data a against b. number of questions: "+reference.id2Question.size()+" vs "+newData.id2Question.size());
			//		if(reference.id2Question.size()!=newData.id2Question.size())
			//		{
			//			logger.info("questions a: "+reference.id2Question.keySet());
			//			logger.info("questions b: "+newData.id2Question.keySet());
			//		}
			aMinusB.addAll(reference.id2Question.keySet());
			aMinusB.removeAll(newData.id2Question.keySet());		

			bMinusA.addAll(newData.id2Question.keySet());
			bMinusA.removeAll(reference.id2Question.keySet());				

			intersection.addAll(reference.id2Question.keySet());
			intersection.retainAll(newData.id2Question.keySet());

			for(int i: intersection)
			{
				// the questions are the same - we don't care about the answer
				if(reference.id2Question.get(i).equals(newData.id2Question.get(i))) 

					if(reference.id2Answers.containsKey(i)&&!reference.id2Answers.get(i).equals(newData.id2Answers.get(i)))
					{
						differentAnswers.add(i);
					} 
			}
		}

		@Override public String toString()
		{			
			StringBuilder sb = new StringBuilder();
			if(!aMinusB.isEmpty())			sb.append("questions a/b: "+aMinusB+" ("+aMinusB.size()+" elements)\n");
			if(!bMinusA.isEmpty())			sb.append("questions b/a: "+bMinusA+" ("+bMinusA.size()+" elements)\n");
			if(!intersection.isEmpty())		sb.append("questions intersection: "+intersection+" ("+intersection.size()+" elements)\n");
			if(!differentAnswers.isEmpty())	{sb.append("questions with different answers: "+differentAnswers+" ("+differentAnswers.size()+" elements)\n");}
			else							{sb.append("all answers are equal\n");}
			return sb.substring(0, sb.length()-1); // remove last \n
		}
	}

	private static class LearnStatus implements Serializable
	{
		public enum Type {OK, TIMEOUT, NO_TEMPLATE_FOUND,QUERY_RESULT_EMPTY,NO_QUERY_LEARNED,EXCEPTION}

		public final Type type;

		private static final long	serialVersionUID	= 1L;
		public static final LearnStatus OK = new LearnStatus(Type.OK,null);
		public static final LearnStatus TIMEOUT = new LearnStatus(Type.TIMEOUT,null);
		public static final LearnStatus NO_TEMPLATE_FOUND = new LearnStatus(Type.NO_TEMPLATE_FOUND,null);
		public static final LearnStatus QUERY_RESULT_EMPTY = new LearnStatus(Type.QUERY_RESULT_EMPTY,null);
		public static final LearnStatus NO_QUERY_LEARNED = new LearnStatus(Type.NO_QUERY_LEARNED,null);

		public final Exception exception;

		private LearnStatus(Type type, Exception exception) {this.type=type;this.exception = exception;}

		public static LearnStatus exceptionStatus(Exception cause)
		{
			if (cause == null) throw new NullPointerException();
			return new LearnStatus(Type.EXCEPTION,cause);
		}

		@Override public String toString()
		{
			switch(type)
			{
				case OK:				return "OK";
				case TIMEOUT:			return "timeout";
				case NO_TEMPLATE_FOUND:	return "no template found";
				case QUERY_RESULT_EMPTY:return "query result empty";
				case NO_QUERY_LEARNED:	return "no query learned";
				case EXCEPTION:			return "<summary>Exception: <details>"+Arrays.toString(exception.getStackTrace())+"</details></summary>";
				default: throw new RuntimeException("switch type not handled");
			}			
		}

	}
	//	enum LearnStatus {OK, TIMEOUT,EXCEPTION,NO_TEMPLATE_FOUND,QUERY_RESULT_EMPTY, NO_QUERY_LEARNED;}

	/**	
	 * @return the test data containing those of the given questions for which queries were found and the results of the queries  	 
	 */
	private QueryTestData generateTestDataMultiThreaded(SortedMap<Integer, String> id2Question,Knowledgebase kb) throws MalformedURLException, ComponentInitException
	{
		QueryTestData testData = new QueryTestData();
		// -- only create the learner parameters once to save time -- 
		//		PartOfSpeechTagger posTagger = new StanfordPartOfSpeechTagger();		
		//		WordNet wordnet = new WordNet();
		//		Options options = new Options();
		// ----------------------------------------------------------
		//		int successes = 0;

		//		List<Callable<Object>> todo = new ArrayList<Callable<Object>>(id2Question.size());
		Map<Integer,Future<LearnStatus>> futures = new HashMap<Integer,Future<LearnStatus>>();

		//		List<FutureTask> todo = new ArrayList<FutureTask>(id2Question.size());
		ExecutorService service = Executors.newFixedThreadPool(1);

		for(int i: id2Question.keySet())
		{//if(i != 78)continue;
			futures.put(i,service.submit(new LearnQueryCallable(id2Question.get(i),i, testData,kb)));
		}
		for(int i: id2Question.keySet())
		{//if(i != 78)continue;
			String question = id2Question.get(i);
			try
			{
				testData.id2LearnStatus.put(i,futures.get(i).get(30, TimeUnit.MINUTES));				
			}
			catch (InterruptedException e)
			{
				//				logger.warn("Timeout while generating test data for question "+id2Question.get(i)+".");
				//				testData.id2LearnStatus.put(i, LearnStatus.TIMEOUT);
				throw new RuntimeException("question= "+question,e);
			}
			catch (ExecutionException e)
			{
				throw new RuntimeException("question="+question,e);
			}
			catch (TimeoutException e)
			{
				logger.warn("Timeout while generating test data for question "+question+".");
				testData.id2LearnStatus.put(i, LearnStatus.TIMEOUT);
			}
		}		
		service.shutdown();
		//		try{service.awaitTermination(10, TimeUnit.MINUTES);} catch (InterruptedException e)	{throw new RuntimeException("Timeout while generating test data.");}

		//		try{service.invokeAll(todo);} catch (InterruptedException e) {throw new RuntimeException(e);}			
		//			logger.debug("generating query for question \""+question+"\", id "+i);			
		//			long start = System.currentTimeMillis();	
		//			SPARQLTemplateBasedLearner2 dbpediaLiveLearner = new SPARQLTemplateBasedLearner2(dbpediaLiveKnowledgebase,posTagger,wordnet,options);
		//			//			dbpediaLiveLearner.setUseIdealTagger(true); // TODO: use this or not?
		//			dbpediaLiveLearner.init();
		//			dbpediaLiveLearner.setQuestion(question);
		//
		//			try{dbpediaLiveLearner.learnSPARQLQueries();}
		//			catch(NoTemplateFoundException e) {continue;}
		//			catch(NullPointerException e) {continue;}
		//catch(Exception e) {logger.error("Error processing question """+question,e);continue;}
		//			successes++;									
		//			String learnedQuery = dbpediaLiveLearner.getBestSPARQLQuery();			
		//			if(learnedQuery==null) {continue;}
		//
		//			testData.id2Question.put(i, question);
		//			testData.id2Query.put(i, learnedQuery);						
		//			try {testData.id2Answers.put(i,getUris(endpoint, learnedQuery));}
		//			catch(Exception e) {logger.warn("Error with learned query "+learnedQuery+" for question "+question+" at endpoint "+endpoint+": "+e.getLocalizedMessage());}

		long end = System.currentTimeMillis();
		//			logger.debug(String.format("Generated query \"%s\" after %d ms", learnedQuery,end-start));


		//		logger.info(String.format("Learned queries for %d of %d questions.",successes,id2Question.size()));
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
	private void generateUpdatedXML(File originalFile, File updatedFile,SparqlEndpoint endpoint, ExtractionDBCache cache) throws ParserConfigurationException, SAXException, IOException, TransformerException
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

			logger.trace("id "+id+", question: "+question);
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
				Set<String> uris = getUris(endpoint, query,cache);
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
	private static final String DBPEDIA_LIVE_ENDPOINT_URL_STRING	= "http://live.dbpedia.org/sparql";

	private static final Logger logger = Logger.getLogger(SPARQLTemplateBasedLearner3Test.class);

	//	private SPARQLTemplateBasedLearner2 oxfordLearner;
	//	private SPARQLTemplateBasedLearner2 dbpediaLiveLearner;

	private final ExtractionDBCache oxfordCache = new ExtractionDBCache("cache");
	private final static ExtractionDBCache dbpediaLiveCache = new ExtractionDBCache("cache");

	private final Knowledgebase dbpediaLiveKnowledgebase = createDBpediaLiveKnowledgebase(dbpediaLiveCache);

	static final SparqlEndpoint dbpediaLiveEndpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
	//static SparqlEndpoint oxfordEndpoint;
	private static final int	MAXIMUM_QUESTIONS	= Integer.MAX_VALUE;

	//	private ResultSet executeDBpediaLiveSelect(String query){return SparqlQuery.convertJSONtoResultSet(dbpediaLiveCache.executeSelectQuery(dbpediaLiveEndpoint, query));}


	private static Knowledgebase createDBpediaLiveKnowledgebase(ExtractionDBCache cache)
	{		
		SOLRIndex resourcesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_resources");
		resourcesIndex.setPrimarySearchField("label");
		//			resourcesIndex.setSortField("pagerank");
		Index classesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_classes");
		Index propertiesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_properties");
		SOLRIndex boa_propertiesIndex = new SOLRIndex("http://139.18.2.173:8080/solr/boa_fact_detail");
		boa_propertiesIndex.setSortField("boa-score");
		propertiesIndex = new HierarchicalIndex(boa_propertiesIndex, propertiesIndex);
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
		Logger.getLogger(SPARQLTemplateBasedLearner2.class).setLevel(Level.WARN);
		//		Logger.getLogger(SPARQLTemplateBasedLearner2.class).setLevel(Level.INFO);
		logger.setLevel(Level.INFO); // TODO: remove when finishing implementation of this class
		logger.addAppender(new FileAppender(new SimpleLayout(), "log/"+this.getClass().getSimpleName()+".log", false));

		//		oxfordEndpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());		
		//		oxfordLearner = new SPARQLTemplateBasedLearner2(createOxfordKnowledgebase(oxfordCache));
	}

	private static class QueryTestData implements Serializable
	{
		public SortedMap<Integer, String> id2Question = new ConcurrentSkipListMap<Integer, String>();
		public SortedMap<Integer, String> id2Query = new ConcurrentSkipListMap<Integer, String>();
		public SortedMap<Integer, Set<String>> id2Answers = new ConcurrentSkipListMap<Integer, Set<String>>();
		public SortedMap<Integer, LearnStatus> id2LearnStatus = new ConcurrentSkipListMap<Integer, LearnStatus>();

		private static final String persistancePath = "cache/"+SPARQLTemplateBasedLearner3Test.class.getSimpleName()+'/'+QueryTestData.class.getSimpleName();

		public synchronized void write()
		{
			try
			{
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(persistancePath)));
				oos.writeObject(this);
				oos.close();
			} catch(IOException e) {throw new RuntimeException(e);}
		}

		public static QueryTestData read() throws FileNotFoundException, IOException
		{
			try
			{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(persistancePath)));
				QueryTestData testData = (QueryTestData) ois.readObject();
				ois.close();
				return testData;
			}
			catch (ClassNotFoundException e){throw new RuntimeException(e);}		
		}

		public QueryTestData generateAnswers(SparqlEndpoint endpoint, ExtractionDBCache cache)
		{
			if(!id2Answers.isEmpty()) {throw new AssertionError("Answers already existing.");}
			for(int i:id2Query.keySet())
			{
				Set<String> uris = getUris(endpoint, id2Query.get(i),cache); 
				id2Answers.put(i, uris); // empty answer set better transfers intended meaning and doesn't cause NPEs in html generation :-) 
				if(!uris.isEmpty())	{/*id2Answers.put(i, uris);*/}
				else				{id2LearnStatus.put(i, LearnStatus.QUERY_RESULT_EMPTY);}
			}
			return this;
		}
	}

	private QueryTestData readQueries(final File file)
	{
		QueryTestData testData = new QueryTestData();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList questionNodes = doc.getElementsByTagName("question");
			int id;

			for(int i = 0; i < questionNodes.getLength(); i++)
			{
				if(i>=MAXIMUM_QUESTIONS) break; // TODO: remove
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
		return testData;
	}

	protected static Set<String> getUris(final SparqlEndpoint endpoint, final String query, ExtractionDBCache cache)
	{		
		if(query==null)		{throw new AssertionError("query is null");}
		if(endpoint==null)	{throw new AssertionError("endpoint is null");}		
		if(!query.contains("SELECT")&&!query.contains("select")) {return Collections.<String>emptySet();} // abort when not a select query
		Set<String> uris = new HashSet<String>();
		//		QueryEngineHTTP qe = new QueryEngineHTTP(DBPEDIA_LIVE_ENDPOINT_URL_STRING, query);		
		ResultSet rs;
		//		try{rs = qe.execSelect();}
		try{rs = executeSelect(endpoint, query, cache);}
		catch(QueryExceptionHTTP e)
		{
			logger.error("Error getting uris for query "+query+" at endpoint "+endpoint,e);
			return Collections.<String>emptySet();
		}
		String variable = "?uri";
		resultsetloop:
			while(rs.hasNext())
			{
				QuerySolution qs = rs.nextSolution();						
				RDFNode node = qs.get(variable);			
				if(node!=null&&node.isResource())
				{
					String uri=node.asResource().getURI();
					uris.add(urlDecode(uri));			
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
							uris.add(urlDecode(uri));
							continue resultsetloop;
						}				
					}
					if(uris.isEmpty()) {return Collections.<String>emptySet();} // we didn't a resource for the first query solution - give up and don't look in the others
				}
			}
		return uris;
	}
	
	private static String urlDecode(String url){
		String decodedURL = null;
		try {
			decodedURL = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decodedURL;
	}


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
	private static class LearnQueryCallable implements Callable<LearnStatus>
	{
		private final String question;
		//		private final String endpoint;
		private final int id;
		private final QueryTestData testData;
		private final Knowledgebase knowledgeBase;

		static private final PartOfSpeechTagger posTagger = PRETAGGED? null: new SynchronizedStanfordPartOfSpeechTagger();		
		static private final WordNet wordnet = new WordNet();
		static private final Options options = new Options();


		public LearnQueryCallable(String question, int id, QueryTestData testData, Knowledgebase knowledgeBase)
		{
			this.question=question;
			this.id=id;					
			this.knowledgeBase=knowledgeBase;
			this.testData=testData;
		}								

		@Override public LearnStatus call()
		{					
			logger.trace("learning question: "+question);					
			try
			{			
				// learn query
				// TODO: change to knowledgebase parameter
				SPARQLTemplateBasedLearner3 learner = new SPARQLTemplateBasedLearner3(createDBpediaLiveKnowledgebase(dbpediaLiveCache),posTagger,wordnet,options);

				//						SPARQLTemplateBasedLearner2 dbpediaLiveLearner = new SPARQLTemplateBasedLearner2(createDBpediaLiveKnowledgebase(dbpediaLiveCache));
				learner.init();
				learner.setUseIdealTagger(true);
				learner.setQuestion(question);						
				learner.learnSPARQLQueries();						
				String learnedQuery = learner.getBestSPARQLQuery();
				testData.id2Question.put(id, question);
				if(learnedQuery!=null&&!learnedQuery.isEmpty())
				{				
					testData.id2Query.put(id, learnedQuery);
				}
				else {return LearnStatus.NO_QUERY_LEARNED;} 
				logger.trace("learned query for question "+question+": "+learnedQuery);

				//						Set<String> learnedURIs = getUris(DBPEDIA_LIVE_ENDPOINT_URL_STRING,learnedQuery);
			}
			catch(NoTemplateFoundException e)
			{		
				logger.warn(String.format("no template found for question \"%s\"",question));
				return LearnStatus.NO_TEMPLATE_FOUND;
			}
			catch(Exception e)
			{
				logger.error(String.format("Exception for question \"%s\": %s",question,e.getLocalizedMessage()));
				e.printStackTrace();
				return LearnStatus.exceptionStatus(e);
			}			
			return LearnStatus.OK;
		}
	}

	public static String diffHTML(String title, Set<String> from, Set<String> to)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<h3>"+title+"</h3>");
		{
			Set<String> addedStrings = new HashSet<String>(to);
			addedStrings.removeAll(from);
			sb.append("<ul class='added'>");
			for(String added: addedStrings) {sb.append("<li>"+added+"</li>\n");}
			sb.append("</ul>\n");
		}
		sb.append('\n');
		{
			Set<String> removedStrings = new HashSet<String>(from);
			removedStrings.removeAll(to);
			sb.append("<ul class='removed'>");
			for(String removed: removedStrings) {sb.append("<li>"+removed+"</li>\n");}
			sb.append("</ul>\n");

		}
		return sb.toString();
	}

	private static String escapePre(String s) {return s.replace("<", "&lt;").replace(">", "&gt;");}

	private static String getAnswerHTMLList(String[] answers)
	{
		StringBuilder sbAnswers = new StringBuilder();					
		final int MAX = 10;
		for(int i=0;i<answers.length;i++)
		{
			if(i>=MAX)
			{
				sbAnswers.append("["+(answers.length-i+1)+" more...]");
				break;
			}
			sbAnswers.append("<li><a href='"+answers[i]+"'>"+answers[i].replace("http://dbpedia.org/resource/","dbpedia:")+"</a></li>\n");
		}
		return sbAnswers.toString();
	}

	/** Generates the HTML string content for one of the 3 colored bars which represent the correctly, incorrectly and unanswered question.
	 * Also creates and links to a file which contains the questions.*/
	private static String createColoredColumn(/*@NonNull*/ File link,/*@NonNull*/ String title,/*@NonNull*/ String color,/*@NonNull*/ Collection<String> questions, int numberOfQuestionsTotal, boolean queriesAvailable, Evaluation evaluation)
	{				
		final StringBuilder sb = new StringBuilder();
		sb.append("<a href='"+link.getAbsolutePath()+"' title='"+title+"'>");
		sb.append("<div style='float:left;width:"+100.0*questions.size()/numberOfQuestionsTotal+"%;height:1em;background-color:"+color+";'></div>");
		sb.append("</a>");

		//		link.getParentFile().mkdirs();		
		try
		{
			PrintWriter out = new PrintWriter(link);
			final Map<String,Integer> question2Id = new HashMap<String,Integer>();
			// only the reference data contains entries for questions without answers
			for(Integer i: evaluation.referenceData.id2Question.keySet()) {question2Id.put(evaluation.referenceData.id2Question.get(i),i);}
			out.println("<!DOCTYPE html><html>\n<head><title>"+title+"</title></head>\n<body>\n<table border='1'>");
			if(queriesAvailable)
			{				
				out.println("<tr><th>Question</th><th>Learned Query</th><th>Reference Query</th><th>Learned Answers</th><th>Reference Answers</th></tr>");
				for(String question: questions)
				{
					Integer id = question2Id.get(question);
					if(evaluation.testData.id2Answers.get(id)==null) {System.err.println(question);continue;}
					out.println(
							"<tr><td>"+question+"</td>"+
									"<td><code><pre>"+escapePre(evaluation.testData.id2Query.get(id))+"</pre></code></td>"+
									"<td><code><pre>"+escapePre(evaluation.referenceData.id2Query.get(id))+"</pre></code></td>"+
									"<td><ul>"+getAnswerHTMLList(evaluation.testData.id2Answers.get(id).toArray(new String[0]))+"</ul></td>"+
									"<td><ul>"+getAnswerHTMLList(evaluation.referenceData.id2Answers.get(id).toArray(new String[0]))+"</ul></td></tr>");					
				}								
			} else
			{				
				out.println("<tr><th>Question</th><th>Error Type</th></tr>");
				for(String question: questions)
				{
					Integer id = question2Id.get(question);
					if(id==null) {System.err.println(question);continue;}
					out.println(
							"<tr><td>"+question+"</td>"+
									"<td>"+evaluation.testData.id2LearnStatus.get(id)+"</td></tr>");
				}					
			}
			out.println("</table>\n</body>\n</html>");
			out.close();	
		}
		catch (Exception e){throw new RuntimeException(e);}		

		return sb.toString();
	}

	static String createChangeHTML(File link, Evaluation from, Evaluation to)
	{				
		try
		{
			PrintWriter out = new PrintWriter(link);
			out.println("<!DOCTYPE html><html>");
			out.println("<head><style type='text/css'>");
			out.println(".added {text-color:green;}");
			out.println(".added li {list-style: none;margin-left: 0;padding-left: -2em;text-indent: -2em;color:darkgreen;}");
			out.println(".added li:before {content: '+ ';}");
			out.println(".removed li {list-style: none;margin-left: 0;padding-left: -2em;text-indent: -2em;color:darkred;}");
			out.println(".removed li:before {content: '- ';}");

			out.println("</style></head>");
			out.println("<body>");
			out.println(diffHTML("Correctly Answered Questions (precision and recall = 1)", from.correctlyAnsweredQuestions, to.correctlyAnsweredQuestions));
			out.println(diffHTML("Incorrectly Answered Questions", from.incorrectlyAnsweredQuestions, to.incorrectlyAnsweredQuestions));
			out.println(diffHTML("Unanswered Questions", from.unansweredQuestions, to.unansweredQuestions));
			out.println("</body>\n</html>");
			out.close();
		}
		catch (Exception e){throw new RuntimeException(e);}

		return "<a href='"+link.getAbsolutePath()+"'>change</a>";
	}

	static void generateHTML(String title)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html><html><head><title>"+title+"</title></head>\n<body>\n<table style='width:100%'>\n");
		SortedMap<Long,Evaluation> evaluations = Evaluation.read();
		//		SortedSet<Long> timestampsDescending = new TreeSet<Long>(Collections.reverseOrder());
		//		timestampsDescending.addAll(evaluations.keySet());
		Evaluation last = null;

		Stack<String> stack = new Stack<String>(); // show reverse chronological order (we can't iterate in reverse order because of the diffs of the evaluations)

		for(long timestamp: evaluations.keySet())
		{
			StringBuilder sb2 = new StringBuilder();
			try
			{		
				File folder = new File("log/"+SPARQLTemplateBasedLearner3Test.class.getSimpleName()+"/"+timestamp);			
				folder.mkdirs();
				Evaluation e = evaluations.get(timestamp);			
				sb2.append("<tr><td style='white-space: nowrap'>");
				Date date = new Date(timestamp);
				sb2.append(DateFormat.getInstance().format(date));
				sb2.append("</td><td style='white-space: nowrap'>");
				if(last!=null)
				{
					if(last.equals(e))	{/*sb2.append("no change");*/}
					else				{sb2.append(createChangeHTML(new File(folder,"change.html"),last,e));}
				}
				sb2.append("</td><td width='100%'>");		
				sb2.append("<div style='width:100%;height:1em;border:solid 1px;'>");			
				sb2.append(createColoredColumn(new File(folder,"correctly_answered.html"),	"Correctly Answered Questions",		"green",	e.correctlyAnsweredQuestions,	e.numberOfQuestions,true,e));
				sb2.append(createColoredColumn(new File(folder,"incorrectly_answered.html"),	"Incorrectly Answered Questions",	"orange",	e.incorrectlyAnsweredQuestions,	e.numberOfQuestions,true,e));
				sb2.append(createColoredColumn(new File(folder,"unanswered.html"),			"Unanswered Questions",				"red",		e.unansweredQuestions,			e.numberOfQuestions,false,e));
				sb2.append("<span style='width:1000px;'></span>");
				sb2.append("</td></tr>\n");				
				last = e;
				stack.push(sb2.toString());
			} catch(Exception e) {logger.warn("error with evaluation from timestamp "+timestamp,e);}
		}
		while(!stack.isEmpty()) {sb.append(stack.pop());}
		sb.append("</table>\n</body>\n</html>");				
		try
		{
			PrintWriter out = new PrintWriter("log/"+SPARQLTemplateBasedLearner3Test.class.getSimpleName()+".html");
			out.println(sb.toString());
			out.close();
		}
		catch (Exception e){throw new RuntimeException(e);}				
	}
	//	private void updateFile(File originalFile, File updatedFile, String endpoint)
	//	{
	//
	//
	//	}

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

	private static ResultSet executeSelect(SparqlEndpoint endpoint, String query,  ExtractionDBCache cache){return SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));}
}