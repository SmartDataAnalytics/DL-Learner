package org.dllearner.algorithm.tbsl.learning;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithm.tbsl.ltag.parser.Parser;
import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.StanfordPartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.SynchronizedStanfordPartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.templator.Templator;
import org.dllearner.algorithm.tbsl.util.Knowledgebase;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.IndexResultItem;
import org.dllearner.common.index.IndexResultSet;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.common.index.SOLRIndex;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.ini4j.Options;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

/** If you just want to test the standard queries, activate justTestTheLastWorkingOnesDBpedia() and testOxford().
 * Tests TSBL against the qald2 benchmark test data with the DBpedia endpoint.
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
	protected static final boolean	USE_HMM	= false;
	protected static final File evaluationFolder = new File("cache/evaluation");
	protected static final boolean	DBPEDIA_PRETAGGED	= true;
	protected static final boolean	OXFORD_PRETAGGED	= false;
	protected static final int MAX_NUMBER_OF_QUESTIONS = Integer.MAX_VALUE;	
	protected static final boolean WHITELIST_ONLY = false;
	protected static final Set<Integer> WHITELIST = Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(new Integer[] {4})));
	protected static final boolean	GENERATE_HTML_ONLY	= false;
	protected static final int	MAX_THREADS	= 1;

	/*@Test*/ public void testDBpedia() throws Exception
	{
		File file = generateTestDataIfNecessary(
				new File(getClass().getClassLoader().getResource("tbsl/evaluation/qald2-dbpedia-train-tagged(ideal).xml").getFile()),
				SparqlEndpoint.getEndpointDBpediaLiveAKSW(),
				dbpediaLiveCache);
		test("QALD 2 Benchmark ideally tagged", file,SparqlEndpoint.getEndpointDBpedia(),dbpediaLiveCache,dbpediaLiveKnowledgebase,null,null,DBPEDIA_PRETAGGED);
	}

	//	protected char[] hmmHtmlRow(String question, String string, String string2, String string3, Set<String> set, Set<String> set2,
	//			Set<String> set3, LearnStatus learnStatus, LearnStatus learnStatus2)
	//	{
	//		return null;
	//	}

	protected static boolean probablySparqlSelectQuery(String s)
	{
		s=s.toLowerCase();
		return s.contains("select")&&s.contains("{")&&s.contains("}");
	}

	/** returns an html table row representation &lt;tr&gt;&lt;td&gt;t(o_1)&lt;/td&gt;...&lt;td&gt;t(o_n)&lt;/td&gt;&lt;/tr&gt; of the string representation of objects,
	 * transformed by escaping HTML characters, setting fixed width on SPARQL queries and shortening and linking of dbpedia resource URIs.  
	 */
	// unescaped form from the top: <tr><td>t(o_1)</td>...<td>t(o_n)</td></tr>
	protected static String htmlTableTr(Object... tds)
	{
		System.out.println();
		StringBuilder sb = new StringBuilder();		
		// shorten and link dbpedia resource uris
		Pattern p = Pattern.compile("http://dbpedia\\.org/resource/([\\w]*)");		

		for(Object td: tds)
		{
			if(td==null) {sb.append("<td></td>");continue;}
			sb.append("<td>");

			// probably a SPARQL query? use fixed font width.
			String s =  StringEscapeUtils.escapeHtml(td.toString());			 
			if(probablySparqlSelectQuery(s)) {s="<pre>"+s+"</pre>";}
			else {s =(p.matcher(s).replaceAll("<a href=\"$0\">dbpedia:$1</a>"));}

			sb.append(s);
			sb.append("</td>");
		}
		return sb.toString();
	}	

	@SuppressWarnings("unchecked") /*@Test*/ public void evaluateHMMAgainstNormalAndBenchmark() throws FileNotFoundException
	{		
		// get the newest evaluations from both with and without hmm
		SortedMap<Long,Evaluation> evaluations = new TreeMap<Long,Evaluation>(Collections.reverseOrder());
		evaluations.putAll(Evaluation.read());
		Evaluation newestWithHmm = null;
		Evaluation newestWithoutHmm = null;

		for(Iterator<Long> it = evaluations.keySet().iterator();it.hasNext()&&(newestWithHmm==null||newestWithoutHmm==null);)
		{
			Evaluation e = evaluations.get(it.next());
			if(e.testData.hmm)
			{if(newestWithHmm==null) {newestWithHmm=e;}}			
			else if(newestWithoutHmm==null) {newestWithoutHmm=e;}
		}
		if(newestWithHmm==null||newestWithoutHmm==null) {logger.warn("No pair of evaluations for Aborting.");return;}

		Set<String> intersectionOfQuestions = new HashSet<String>(newestWithHmm.testData.id2Question.values());
		intersectionOfQuestions.retainAll(newestWithoutHmm.testData.id2Question.values());
		if(intersectionOfQuestions.isEmpty()) {logger.warn("No common questions. Aborting.");return;}

		Set<String> questionsOnlyCorrectWithHMM = new HashSet<String> (intersectionOfQuestions);
		questionsOnlyCorrectWithHMM.retainAll(newestWithHmm.correctlyAnsweredQuestions);
		questionsOnlyCorrectWithHMM.removeAll(newestWithoutHmm.correctlyAnsweredQuestions);		

		Set<String> questionsOnlyCorrectWithoutHMM = new HashSet<String> (intersectionOfQuestions);
		questionsOnlyCorrectWithoutHMM.retainAll(newestWithoutHmm.correctlyAnsweredQuestions);
		questionsOnlyCorrectWithoutHMM.removeAll(newestWithHmm.correctlyAnsweredQuestions);

		PrintWriter out = new PrintWriter("log/evaluatehmm.html");
		String title = "Evaluation of HMM vs the normal disambiguation.";

		out.println("<!DOCTYPE html><html>\n<head><title>"+title+"</title></head>\n<body>\n<table border='1'>");							
		out.println("<tr><th>Question</th><th>Query with HMM</th><th>Query without HMM</th><th>Reference Query</th>" +
				"<th>Answers with HMM</th><th>Answers without HMM</th><th>Reference Answers</th><th>Status with HMM</th><th>Status without HMM</th></tr>");

		// most of the time it should be enough to assume that the keys are equal, but this could introduce subtle bugs  
		BidiMap<String,Integer> question2IdWithHmm = new DualHashBidiMap<Integer,String>(newestWithHmm.testData.id2Question).inverseBidiMap();
		BidiMap<String,Integer> question2IdWithoutHmm = new DualHashBidiMap<Integer,String>(newestWithoutHmm.testData.id2Question).inverseBidiMap();
		//		if(newestWithHmm.correctlyAnsweredQuestions.contains(question)!=newestWithoutHmm.correctlyAnsweredQuestions.contains(question)) {..}

		for(Set<String> c : new Set[] {questionsOnlyCorrectWithHMM, questionsOnlyCorrectWithoutHMM})
		{
			for(String question: c)
			{
				int idWithHmm = question2IdWithHmm.get(question);
				int idWithoutHmm = question2IdWithoutHmm.get(question);						
				out.println(htmlTableTr(
						question,
						newestWithHmm.testData.id2Query.get(idWithHmm),
						newestWithoutHmm.testData.id2Query.get(idWithoutHmm),
						newestWithHmm.referenceData.id2Query.get(idWithHmm),
						newestWithHmm.testData.id2Answers.get(idWithHmm),
						newestWithoutHmm.testData.id2Answers.get(idWithoutHmm),
						newestWithHmm.referenceData.id2Answers.get(idWithHmm),
						newestWithHmm.testData.id2LearnStatus.get(idWithHmm),
						newestWithoutHmm.testData.id2LearnStatus.get(idWithoutHmm)
						));
			}
		}	

		//		Integer id = question2Id.get(question);
		//		if(evaluation.testData.id2Answers.get(id)==null) {System.err.println(question);continue;}
		//		out.println(
		//				"<tr><td>"+question+"</td>"+
		//						"<td><code><pre>"+escapePre(evaluation.testData.id2Query.get(id))+"</pre></code></td>"+
		//						"<td><code><pre>"+escapePre(evaluation.referenceData.id2Query.get(id))+"</pre></code></td>"+
		//						"<td><ul>"+getAnswerHTMLList(evaluation.testData.id2Answers.get(id).toArray(new String[0]))+"</ul></td>"+
		//						"<td><ul>"+getAnswerHTMLList(evaluation.referenceData.id2Answers.get(id).toArray(new String[0]))+"</ul></td>"+
		//						"<td>"+evaluation.testData.id2LearnStatus.get(id)+"</td></tr>");					

		logger.info(questionsOnlyCorrectWithHMM.size()+" questions only correct with hmm, "+
				questionsOnlyCorrectWithoutHMM.size()+" questions only correct without hmm");

		// generate a html description of it

		out.close();
	}

	/*@Test*/ public void testOxford() throws Exception
	{
		File file = new File(getClass().getClassLoader().getResource("tbsl/evaluation/oxford_working_questions.xml").getFile());
		test("Oxford 19 working questions", file,null,null,null,loadOxfordModel(),getOxfordMappingIndex(),OXFORD_PRETAGGED);
	}

	//	/*@Test*/ public void testOxford() throws Exception
	//	{
	//		Model model = loadOxfordModel();
	//		QueryTestData testData = QueryTestData.readQaldXml(new File("log/oxford_working_questions.xml"));
	//		// answers are not included at least in the first query TODO: check, why
	//		testData.generateAnswers(null, null, model);
	//		QueryTestData newTestData = generateTestDataMultiThreaded(testData.id2Question, null, model,getOxfordMappingIndex() , OXFORD_PRETAGGED);
	//		newTestData.generateAnswers(null, null, model);
	//		for(int i : testData.id2Question.keySet())
	//		{
	//			logger.info("Comparing answers for question "+testData.id2Question.get(i));
	//			String referenceQuery = testData.id2Query.get(i);
	//			String newQuery = newTestData.id2Query.get(i);			
	//			if(!referenceQuery.equals(newQuery))
	//			{
	//				logger.warn("not equal, reference query: "+referenceQuery+", new query: "+newQuery);
	//				Collection<String> referenceAnswers = testData.id2Answers.get(i);
	//				Collection<String> newAnswers = newTestData.id2Answers.get(i);			
	//				if(!referenceAnswers.equals(newAnswers)) fail("not equal, reference answers: "+referenceAnswers+", new answers: "+newAnswers);
	//			}
	//		}
	//	}

	/** For debugging one question in particular.
	 */
	/*@Test*/ public void testSingleQueryOxford()
	{
		Logger.getLogger(Templator.class).setLevel(Level.DEBUG);
		Logger.getLogger(Parser.class).setLevel(Level.DEBUG);
		Logger.getLogger(SPARQLTemplateBasedLearner2.class).setLevel(Level.DEBUG);
		//		String question = "houses for less than 900000 pounds";
		String question = "houses/NNS for/IN less/JJR than/IN 900000/CD pounds/NNS";
		//question = new StanfordPartOfSpeechTagger().tag(question);

		Model model = loadOxfordModel();
		QueryTestData testData = new QueryTestData();
		new LearnQueryCallable(question, 0, testData, model, getOxfordMappingIndex(), true).call();
		logger.info("learned query: "+testData.id2Query.get(0));
	}

	/** For debugging one question in particular.
	 */
	/*@Test*/ public void testSingleQueryDBpedia()
	{
		//		Logger.getLogger(Templator.class).setLevel(Level.DEBUG);
		//		Logger.getLogger(Parser.class).setLevel(Level.DEBUG);
		//		Logger.getLogger(SPARQLTemplateBasedLearner2.class).setLevel(Level.DEBUG);
		//		String question = "houses for less than 900000 pounds";
		String question = "Give/VB me/PRP all/DT video/JJ games/NNS published/VBN by/IN Mean/NNP Hamster/NNP Software/NNP";
		//		String question = "give me all video games published by mean hamster software";
		//		String question = "Give me all video games published by Mean Hamster Software";		
		//		question = new StanfordPartOfSpeechTagger().tag(question);
		//		System.out.println(question);

		//		Model model = loadOxfordModel();
		QueryTestData testData = new QueryTestData();
		new LearnQueryCallable(question, 0, testData, dbpediaLiveKnowledgebase, true).call();
		logger.info("learned query: "+testData.id2Query.get(0));
	}

	@Test  @SuppressWarnings("null") public void generateXMLOxford() throws IOException
	{		
		boolean ADD_POS_TAGS = true;
		PartOfSpeechTagger posTagger = null;
		if(ADD_POS_TAGS) {posTagger=new StanfordPartOfSpeechTagger();}
		Model model = loadOxfordModel();
		List<String> questions = new LinkedList<String>();
		BufferedReader in = new BufferedReader((new InputStreamReader(getClass().getClassLoader().getResourceAsStream("tbsl/oxford_eval_queries.txt"))));
		for(String line;(line=in.readLine())!=null;)
		{
			String question = line.replace("question: ", "").trim();
			if(ADD_POS_TAGS&&!OXFORD_PRETAGGED) {question = posTagger.tag(question);}
			if(!line.trim().isEmpty()) {questions.add(question);}
		}
		in.close();
		SortedMap<Integer,String> id2Question = new TreeMap<Integer, String>();
		Iterator<String> it = questions.iterator();
		for(int i=0;i<questions.size();i++) {id2Question.put(i, it.next());}
		MappingBasedIndex mappingIndex= getOxfordMappingIndex();
		logger.info("generating sparql queries for questions");
		QueryTestData testData = generateTestDataMultiThreaded(id2Question, null,model,mappingIndex,ADD_POS_TAGS||OXFORD_PRETAGGED);
		logger.info("generating answers for sparql queries");
		testData.generateAnswers(null, null, model);
		logger.info("writing test data to benchmark file");
		testData.writeQaldXml(new File("log/test.xml"));
		logger.info("finished generating oxford benchmark file, "+testData.id2Question.values().size()+"questions, "+testData.id2Query.size()+" SPARQL queries, "+testData.id2Answers.size()+" answers.");
	}

	public static MappingBasedIndex getOxfordMappingIndex()
	{
		return new MappingBasedIndex(
				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_class_mappings.txt").getPath(), 
				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_resource_mappings.txt").getPath(),
				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_dataproperty_mappings.txt").getPath(),
				SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_objectproperty_mappings.txt").getPath()
				);
	}

	public static Model loadOxfordModel()
	{
		// load it into a model because we can and it's faster and doesn't rely on endpoint availability
		// the files are located in the paper svn under http://diadem.cs.ox.ac.uk/svn/papers/oxpath/question-answering-iswc-2012/data/ and data_v2
		// ls *ttl | xargs -I @ echo \"@\",
		//find -type f | sed -r "s|\./(.*)|\"\1\",|"
		final String[] rdf = {
		"andrewsonline-sales-triple.ttl",
		"johnwood-sales-triple.ttl",
		"wwagency.ttl",
		"findaproperty-sales-triple.ttl",
		"carterjonas-sales.ttl",
		"breckon.ttl",
		"sequencehome.ttl",
		"schema/ontology.ttl",
		"schema/goodRelations.owl",
		"schema/vCard.owl",
		"schema/LGD-Dump-110406-Ontology.nt",
		"hodson.ttl",
		"thomasmerrifield.ttl",
		"churchgribben.ttl",
		"bairstoweves-sales-triple.ttl",
		"anker.ttl",
		"martinco-letting-triples.ttl",
		"scrivenerandreinger.ttl",
		"ballards.ttl",
		"teamprop.ttl",
		"abbeys-sales-triple.ttl",
		"links/ontology.ttl",
		"links/lgd_data.ttl",
		"links/allNear.ttl",
		"links/all_walking_distance.ttl",
		"scottsymonds.ttl",
		"scottfraser.ttl"
		};
		Model m = ModelFactory.createDefaultModel();
		for(final String s:rdf)
		{
			// see http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/rdf/model/Model.html#read%28java.io.InputStream,%20java.lang.String,%20java.lang.String%29
			String ending = s.substring(s.lastIndexOf('.')+1, s.length());			
			String type = (ending.equals("ttl")||ending.equals("nt"))?"TURTLE":ending.equals("owl")?"RDF/XML":String.valueOf(Integer.valueOf("filetype "+ending+" not handled."));
			// switch(type) {case "ttl":type="TURTLE";break;case "owl":type="RDF/XML";break;default:throw new RuntimeException("filetype "+ending+" not handled.");} // no Java 1.7 :-(
			try{
				//				m.read(new FileInputStream(new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/data/"+s)), null, type);}catch (FileNotFoundException e) {}
				m.read(SPARQLTemplateBasedLearner3Test.class.getClassLoader().getResourceAsStream("oxford/"+s),null, type);}
			catch(RuntimeException e) {throw new RuntimeException("Could not read into model: "+s,e);} 
		}
		//		test("Oxford evaluation questions", new File(getClass().getClassLoader().getResource("tbsl/evaluation/qald2-dbpedia-train-tagged(ideal).xml").getFile()),
		//			SparqlEndpoint.getEndpointDBpediaLiveAKSW(),dbpediaLiveCache);
		return m;
	}

	/*@Test*/ public void justTestTheLastWorkingOnesDBpedia() throws Exception
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
			LearnStatus status = new LearnQueryCallable(question, 0,new QueryTestData() , dbpediaLiveKnowledgebase,DBPEDIA_PRETAGGED).call();
			if(status.type!=LearnStatus.Type.OK) {fail("Failed with question \""+question+"\", query status: "+status);}
		}
	}

	public void test(String title, final File referenceXML,final  SparqlEndpoint endpoint,ExtractionDBCache cache,Knowledgebase kb, Model model, MappingBasedIndex index,boolean pretagged)
			throws ParserConfigurationException, SAXException, IOException, TransformerException, ComponentInitException, NoTemplateFoundException
			{		
		if(!GENERATE_HTML_ONLY) {evaluateAndWrite(title,referenceXML,endpoint,cache,kb,model,index,pretagged);}
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

	protected File generateTestDataIfNecessary(final File referenceXML,final  SparqlEndpoint endpoint,ExtractionDBCache cache) throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		String dir = "cache/"+getClass().getSimpleName()+"/";
		new File(dir).mkdirs();
		File updatedReferenceXML=new File(dir+"updated_"+referenceXML.getName());

		if(!updatedReferenceXML.exists())
		{
			logger.info("Generating updated reference.");
			generateUpdatedXML(referenceXML,updatedReferenceXML,endpoint,cache,null);
		}
		return updatedReferenceXML;
	}

	protected void evaluateAndWrite(String title,final File updatedReferenceXML, final  SparqlEndpoint endpoint,ExtractionDBCache cache,
			Knowledgebase kb, Model model, MappingBasedIndex index,boolean pretagged)
	{

		QueryTestData referenceTestData = QueryTestData.readQaldXml(updatedReferenceXML,MAX_NUMBER_OF_QUESTIONS,WHITELIST_ONLY,WHITELIST);
		logger.info(title+" subset loaded with "+referenceTestData.id2Question.size()+" questions.");

		long startLearning = System.currentTimeMillis();
		QueryTestData learnedTestData = generateTestDataMultiThreaded(referenceTestData.id2Question, kb,model,index,pretagged);
		long endLearning = System.currentTimeMillis();
		logger.info("finished learning after "+(endLearning-startLearning)/1000.0+"s");
		learnedTestData.generateAnswers(endpoint,cache,model);
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
	protected static Evaluation evaluate(QueryTestData reference, QueryTestData suspect)
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
			if(reference.id2Answers.get(i)==null)
			{
				logger.warn("no reference answers for question "+i+" ("+question+")");
				continue;
			}
			if(suspect.id2Answers.get(i)==null)
			{
				logger.warn("no suspect answers for question "+i+" ("+question+")");
				continue;
			}			

			if(referenceQuery.equals(suspectQuery)||reference.id2Answers.get(i).equals(suspect.id2Answers.get(i)))
			{
				evaluation.correctlyAnsweredQuestions.add(question);
				evaluation.numberOfCorrectAnswers++;
				evaluation.question2JaccardOfAnswers.put(question,1.0);
			}
			else
			{
				Set<String> intersection = new HashSet<String>(reference.id2Answers.get(i));				
				intersection.retainAll(suspect.id2Answers.get(i));
				if(!intersection.isEmpty())
				{
					evaluation.partlyCorrectlyAnsweredQuestions.add(question);
					evaluation.numberOfPartlyCorrectAnswers++;
					Set<String> union = new HashSet<String>(reference.id2Answers.get(i));
					union.addAll(suspect.id2Answers.get(i));
					evaluation.question2JaccardOfAnswers.put(question,((double)intersection.size())/union.size());
				} else
				{
					evaluation.incorrectlyAnsweredQuestions.add(question);
					evaluation.question2JaccardOfAnswers.put(question,0.0);
					logger.debug("learned queries differing. reference query:\n"+referenceQuery+"\nsuspect query:\n"+suspectQuery);
					logger.debug("learned answers differing: reference answers:\n"+reference.id2Answers.get(i)+"\nsuspect answers:\n"+suspect.id2Answers.get(i));
				}

			}
		}
		return evaluation;
	}

	static class Evaluation implements Serializable
	{		
		protected static final long	serialVersionUID	= 6L;
		final QueryTestData testData;
		final QueryTestData referenceData;
		int numberOfQuestions = 0;
		int numberOfAnsweredQuestions = 0;		
		int numberOfCorrectAnswers = 0;
		int numberOfPartlyCorrectAnswers = 0;
		double precision = 0;
		double recall = 0;	
		final Set<String> unansweredQuestions = new HashSet<String>();
		final Set<String> incorrectlyAnsweredQuestions = new HashSet<String>();
		final Set<String> correctlyAnsweredQuestions = new HashSet<String>();
		final Set<String> partlyCorrectlyAnsweredQuestions = new HashSet<String>();

		final Map<String,Double> question2JaccardOfAnswers = new HashMap<String,Double>();

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
			sb.append(numberOfCorrectAnswers+" exactly correct answers, "+numberOfPartlyCorrectAnswers+" partly correct answers.");
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
			sb.append(htmlDetailsList("Partly correctly Answered Questions",partlyCorrectlyAnsweredQuestions));
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

		/**
		 * @return the evaluations by timestamp, sorted ascending (from oldest to newest)
		 */
		public static SortedMap<Long,Evaluation> read()
		{
			SortedMap<Long,Evaluation> evaluations = new ConcurrentSkipListMap<Long,Evaluation>();			
			evaluationFolder.mkdirs();
			File[] files = evaluationFolder.listFiles();		
			for(int i=0;i<files.length;i++) {evaluations.put(Long.valueOf(files[i].getName()),read(files[i]));}
			return evaluations;
		}

		protected static Evaluation read(File file)
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

			if (partlyCorrectlyAnsweredQuestions == null)
			{
				if (other.partlyCorrectlyAnsweredQuestions != null) return false;
			}
			else if (!partlyCorrectlyAnsweredQuestions.equals(other.partlyCorrectlyAnsweredQuestions)) return false;

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

	public static class LearnStatus implements Serializable
	{
		public enum Type {OK, TIMEOUT, NO_TEMPLATE_FOUND,QUERY_RESULT_EMPTY,NO_QUERY_LEARNED,EXCEPTION}

		public final Type type;

		protected static final long	serialVersionUID	= 1L;
		public static final LearnStatus OK = new LearnStatus(Type.OK,null);
		public static final LearnStatus TIMEOUT = new LearnStatus(Type.TIMEOUT,null);
		public static final LearnStatus NO_TEMPLATE_FOUND = new LearnStatus(Type.NO_TEMPLATE_FOUND,null);
		public static final LearnStatus QUERY_RESULT_EMPTY = new LearnStatus(Type.QUERY_RESULT_EMPTY,null);
		public static final LearnStatus NO_QUERY_LEARNED = new LearnStatus(Type.NO_QUERY_LEARNED,null);

		public final Exception exception;

		protected LearnStatus(Type type, Exception exception) {this.type=type;this.exception = exception;}

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
	 * @param id2Question
	 * @param kb either the kb or both the model and the index can be null. if the kb is null the model and index are used, else the kb is used.
	 * @param model can be null if the kb is not null
	 * @param index can be null if the kb is not null
	 * @return the test data containing those of the given questions for which queries were found and the results of the queries
	 * @throws MalformedURLException
	 * @throws ComponentInitException
	 */
	protected QueryTestData generateTestDataMultiThreaded(SortedMap<Integer, String> id2Question,Knowledgebase kb,Model model, MappingBasedIndex index,boolean pretagged)
	{
		QueryTestData testData = new QueryTestData();
		testData.hmm = USE_HMM;
		// -- only create the learner parameters once to save time -- 
		//		PartOfSpeechTagger posTagger = new StanfordPartOfSpeechTagger();		
		//		WordNet wordnet = new WordNet();
		//		Options options = new Options();
		// ----------------------------------------------------------
		//		int successes = 0;

		//		List<Callable<Object>> todo = new ArrayList<Callable<Object>>(id2Question.size());
		Map<Integer,Future<LearnStatus>> futures = new HashMap<Integer,Future<LearnStatus>>();

		//		List<FutureTask> todo = new ArrayList<FutureTask>(id2Question.size());
		ExecutorService service = Executors.newFixedThreadPool(MAX_THREADS);

		for(int i: id2Question.keySet())
		{//if(i != 78)continue;
			if(kb!=null)	{futures.put(i,service.submit(new LearnQueryCallable(id2Question.get(i),i, testData,kb,pretagged)));}
			else			{futures.put(i,service.submit(new LearnQueryCallable(id2Question.get(i),i, testData,model,index,pretagged)));}
		}
		for(int i: id2Question.keySet())
		{//if(i != 78)continue;
			String question = id2Question.get(i);
			try
			{
				testData.id2LearnStatus.put(i,futures.get(i).get(USE_HMM?120:30, TimeUnit.SECONDS));				
			}
			catch (InterruptedException e)
			{
				//				logger.warn("Timeout while generating test data for question "+id2Question.get(i)+".");
				//				testData.id2LearnStatus.put(i, LearnStatus.TIMEOUT);
				throw new RuntimeException("question= "+question,e);
			}
			catch (ExecutionException e)
			{
				testData.id2LearnStatus.put(i, new LearnStatus(LearnStatus.Type.EXCEPTION, e));
				//throw new RuntimeException("question="+question,e);
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

		//		long end = System.currentTimeMillis();
		//		logger.trace(String.format("Generated query \"%s\" after %d ms", learnedQuery,end-start));


		//		logger.info(String.format("Learned queries for %d of %d questions.",successes,id2Question.size()));
		return testData;
	}

	/** Updates question file by removing questions without nonempty resource list answer and adding answers.
	 * @param file
	 * @param updatedFile
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws TransformerException 
	 */
	protected void generateUpdatedXML(File originalFile, File updatedFile,SparqlEndpoint endpoint, ExtractionDBCache cache,Model model) throws ParserConfigurationException, SAXException, IOException, TransformerException
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
				Set<String> uris = getUris(endpoint, query,cache,model);
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
	//	protected static final String DBPEDIA_LIVE_ENDPOINT_URL_STRING	= "http://live.dbpedia.org/sparql";

	private static final Logger logger = Logger.getLogger(SPARQLTemplateBasedLearner3Test.class);

	//	protected SPARQLTemplateBasedLearner2 oxfordLearner;
	//	protected SPARQLTemplateBasedLearner2 dbpediaLiveLearner;

	//	protected final ExtractionDBCache oxfordCache = new ExtractionDBCache("cache");
	protected final static ExtractionDBCache dbpediaLiveCache = new ExtractionDBCache("cache");

	protected final Knowledgebase dbpediaLiveKnowledgebase = createDBpediaLiveKnowledgebase(dbpediaLiveCache);

	static final SparqlEndpoint dbpediaLiveEndpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
	//static SparqlEndpoint oxfordEndpoint;

	//	protected ResultSet executeDBpediaLiveSelect(String query){return SparqlQuery.convertJSONtoResultSet(dbpediaLiveCache.executeSelectQuery(dbpediaLiveEndpoint, query));}

	protected static boolean httpResponseOK(String url) throws MalformedURLException, IOException
	{		
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("HEAD");
		int responseCode = connection.getResponseCode();
		return responseCode == 200;
	}

	static final String resourcesURL = "http://dbpedia.aksw.org:8080/solr/dbpedia_resources";
	static final String classesURL = "http://dbpedia.aksw.org:8080/solr/dbpedia_classes";
	static final String propertiesURL = "http://dbpedia.aksw.org:8080/solr/dbpedia_properties";
	static final String boaPropertiesURL = "http://139.18.2.173:8080/solr/boa_fact_detail";

	public boolean isOnline(String url)
	{
		try
		{					
			URL u = new URL (url ); 
			HttpURLConnection huc =  ( HttpURLConnection )  u.openConnection (); 
			huc.setRequestMethod ("GET"); 
			huc.connect () ; 
			//		   OutputStream os = huc.getOutputStream (  ) ; 
			int code = huc.getResponseCode (  ) ;
			if(code!=404) logger.warn("Response code "+code+" for url "+url);
			return code==404;
		}
		catch(Throwable t) {logger.warn("Exception when checking availability of url "+url,t);return false;}		
	}

	// gets called by setup() 
	/*@Test*/ public void testSolrOnline()
	{
		String[] solrServerUrls = {resourcesURL,classesURL,propertiesURL,boaPropertiesURL};
		String suffix = "/admin";
		for(String url : solrServerUrls)
		{
			url+=suffix;
			if(!isOnline(url)) fail();
		}		
	}

	protected void testIndex(Index index, String[][] entities)
	{				
		for(int i=0;i<entities.length;i++)
		{			
			IndexResultSet rs = index.getResourcesWithScores(entities[i][0]);			
			Set<IndexResultItem> items = new TreeSet<IndexResultItem>(rs.getItems());
			ArrayList<IndexResultItem> itemList = new ArrayList<IndexResultItem>(items);
			// the right resource should have the top or second from the top score
			String uri = items.iterator().next().getUri();
			IndexResultItem secondItem = itemList.size()<2?null:itemList.get(1);
			String secondUri = secondItem==null?null:secondItem.getUri();
			assertTrue(entities[i][1]+"!="+uri+" "+items,entities[i][1].equals(uri)||entities[i][1].equals(secondUri));
		}		
	}

	/*@Test*/ public void testSolrGoodResults()
	{
		Knowledgebase dbpedia = createDBpediaLiveKnowledgebase(dbpediaLiveCache);

		testIndex(dbpedia.getResourceIndex(),new String[][]
				{{"Brooklyn Bridge","http://dbpedia.org/resource/Brooklyn_Bridge"},{"Estonia","http://dbpedia.org/resource/Estonia"},
			{"Germany","http://dbpedia.org/resource/Germany"}});
		testIndex(dbpedia.getPropertyIndex(),new String[][] {{"born in","http://dbpedia.org/ontology/birthPlace"}});
	}

	protected static Knowledgebase createDBpediaLiveKnowledgebase(ExtractionDBCache cache)
	{		

		//		for(String url : new String[] {resourcesURL,classesURL,propertiesURL,boaPropertiesURL})
		//		{
		//			try{if(!httpResponseOK(url)) throw new RuntimeException("Http response not 200 for url "+url);} catch(Exception e) {throw new RuntimeException(e);}
		//		}

		SOLRIndex resourcesIndex = new SOLRIndex(resourcesURL);
		resourcesIndex.setPrimarySearchField("label");
		//			resourcesIndex.setSortField("pagerank");
		Index classesIndex = new SOLRIndex(classesURL);
		Index propertiesIndex = new SOLRIndex(propertiesURL);
		SOLRIndex boa_propertiesIndex = new SOLRIndex(boaPropertiesURL);
		boa_propertiesIndex.setSortField("boa-score");
		//		propertiesIndex = new HierarchicalIndex(boa_propertiesIndex, propertiesIndex);
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
		Logger.getLogger(SPARQLTemplateBasedLearner2.class).setLevel(Level.DEBUG);
		//		Logger.getLogger(SPARQLTemplateBasedLearner2.class).setLevel(Level.INFO);
		logger.setLevel(Level.INFO); // TODO: remove when finishing implementation of this class
		logger.addAppender(new FileAppender(new SimpleLayout(), "log/"+this.getClass().getSimpleName()+".log", false));
		testSolrOnline();
		//		Logger.getRootLogger().removeAllAppenders();

		//		oxfordEndpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());		
		//		oxfordLearner = new SPARQLTemplateBasedLearner2(createOxfordKnowledgebase(oxfordCache));
	}

	public static Set<String> getUris(final SparqlEndpoint endpoint, final String query, ExtractionDBCache cache, Model model)
	{		
		if(query==null)		{throw new AssertionError("query is null");}
		//		if(endpoint==null)	{throw new AssertionError("endpoint is null");}		
		if(!query.contains("SELECT")&&!query.contains("select")) {return Collections.<String>emptySet();} // abort when not a select query
		Set<String> uris = new HashSet<String>();
		//		QueryEngineHTTP qe = new QueryEngineHTTP(DBPEDIA_LIVE_ENDPOINT_URL_STRING, query);		

		ResultSet rs;
		//		try{rs = qe.execSelect();}
		try
		{
			if(model!=null)	{rs = QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), model).execSelect();}
			else			{rs = executeSelect(endpoint, query, cache);}
		}
		catch(QueryExceptionHTTP e)
		{
			throw new QueryExceptionHTTP("Error getting uris for query "+query+" at endpoint "+endpoint,e);
			//			logger.error("Error getting uris for query "+query+" at endpoint "+endpoint,e);
			//			return Collections.<String>emptySet();
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

	protected static String urlDecode(String url){
		String decodedURL = null;
		try {
			decodedURL = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decodedURL;
	}


	//	protected ResultSet executeOxfordSelect(String query){return SparqlQuery.convertJSONtoResultSet(oxfordCache.executeSelectQuery(oxfordEndpoint, query));}

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

	//		protected Knowledgebase createOxfordKnowledgebase(ExtractionDBCache cache)
	//		{
	//			URL url;
	//			try{url = new URL("http://lgd.aksw.org:8900/sparql");} catch(Exception e) {throw new RuntimeException(e);}
	//			SparqlEndpoint endpoint = new SparqlEndpoint(url, Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());
	//	
	//			SPARQLIndex resourcesIndex = new VirtuosoResourcesIndex(endpoint, cache);
	//			SPARQLIndex classesIndex = new VirtuosoClassesIndex(endpoint, cache);
	//			SPARQLIndex propertiesIndex = new VirtuosoPropertiesIndex(endpoint, cache);
	//			MappingBasedIndex mappingIndex= new MappingBasedIndex(
	//					SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_class_mappings.txt").getPath(), 
	//					SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_resource_mappings.txt").getPath(),
	//					SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_dataproperty_mappings.txt").getPath(),
	//					SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_objectproperty_mappings.txt").getPath()
	//					);
	//	
	//			Knowledgebase kb = new Knowledgebase(oxfordEndpoint, "Oxford - Real estate", "TODO", resourcesIndex, propertiesIndex, classesIndex, mappingIndex);
	//
	//			return kb;
	//		}
	/** @author konrad
	 * Learns a query for a question and puts it into the given testData object. * */
	protected static class LearnQueryCallable implements Callable<LearnStatus>
	{
		protected final String question;
		//		protected final String endpoint;
		protected final int id;
		protected final QueryTestData testData;

		static protected class POSTaggerHolder
		{static public final PartOfSpeechTagger posTagger = new SynchronizedStanfordPartOfSpeechTagger();}

		static protected final WordNet wordnet = new WordNet();
		static protected final Options options = new Options();	
		protected final SPARQLTemplateBasedLearner2 learner;

		public LearnQueryCallable(String question, int id, QueryTestData testData, Knowledgebase knowledgeBase,boolean pretagged)
		{
			this.question=question;
			this.id=id;					
			this.testData=testData;
			learner = new SPARQLTemplateBasedLearner2(knowledgeBase,pretagged?null:POSTaggerHolder.posTagger,wordnet,options);
			try {learner.init();} catch (ComponentInitException e) {throw new RuntimeException(e);}
			learner.setUseIdealTagger(pretagged);
			learner.setGrammarFiles(new String[]{"tbsl/lexicon/english.lex"});
		}								

		public LearnQueryCallable(String question, int id, QueryTestData testData, Model model,MappingBasedIndex index,boolean pretagged)
		{
			this.question=question;
			this.id=id;					
			this.testData=testData;
			MappingBasedIndex mappingIndex= new MappingBasedIndex(
					SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_class_mappings.txt").getPath(), 
					SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_resource_mappings.txt").getPath(),
					SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_dataproperty_mappings.txt").getPath(),
					SPARQLTemplateBasedLearner2.class.getClassLoader().getResource("tbsl/oxford_objectproperty_mappings.txt").getPath()
					);

			learner = new SPARQLTemplateBasedLearner2(model,mappingIndex,pretagged?null:POSTaggerHolder.posTagger);
			try {learner.init();} catch (ComponentInitException e) {throw new RuntimeException(e);}
			learner.setUseIdealTagger(pretagged);
			learner.setGrammarFiles(new String[]{"tbsl/lexicon/english.lex","tbsl/lexicon/english_oxford.lex"});
			learner.setUseDomainRangeRestriction(false);
		}								


		@Override public LearnStatus call()
		{
			logger.trace("learning question: "+question);					
			try
			{			
				// learn query

				learner.setQuestion(question);						
				learner.learnSPARQLQueries(USE_HMM);						
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
			catch(AssertionError e )
			{
				// this is the only exception that we want to halt on
				throw new RuntimeException(e);
			}
			catch(NoTemplateFoundException e)
			{		
				logger.warn(String.format("no template found for question \"%s\"",question));
				return LearnStatus.NO_TEMPLATE_FOUND;
			}
			catch(Exception e)
			{
				logger.error(String.format("Exception for question %d \"%s\": %s",id, question,e.getLocalizedMessage()));
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

	protected static String escapePre(String s) {return s.replace("<", "&lt;").replace(">", "&gt;");}

	protected static String getAnswerHTMLList(String[] answers)
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

	static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
		SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
				new Comparator<Map.Entry<K,V>>() {
					@Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
						int res = e1.getValue().compareTo(e2.getValue());
						return res != 0 ? res : 1; // Special fix to preserve items with equal values
					}
				}
				);
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}

	/** Generates the HTML string content for one of the 4 colored bars which represent the correctly, incorrectly and unanswered question.
	 * Also creates and links to a file which contains the questions.*/
	protected static String createColoredColumn(/*@NonNull*/ File link,/*@NonNull*/ String title,/*@NonNull*/ String color,/*@NonNull*/ Collection<String> questions, int numberOfQuestionsTotal, boolean queriesAvailable,boolean jaccard, Evaluation evaluation)
	{				
		final StringBuilder sb = new StringBuilder();
		sb.append("<a href='"+link.getAbsolutePath()+"' title='"+title+" ("+questions.size()+"/"+(numberOfQuestionsTotal==0?"":numberOfQuestionsTotal)+")'>");
		sb.append("<div style='float:left;width:"+100.0*questions.size()/numberOfQuestionsTotal+"%;height:1em;background-color:"+color+";'></div>");
		sb.append("</a>");
		//		link.getParentFile().mkdirs();
		Collection<String> sortedQuestions;
		if(jaccard) // sort by jaccard descending
		{
			sortedQuestions = new LinkedList<String>();
			SortedMap<String,Double> map = new TreeMap<String,Double>();
			for(String question : questions) {map.put(question, 1-evaluation.question2JaccardOfAnswers.get(question));}

			for(Entry<String,Double> e: entriesSortedByValues(map)) {sortedQuestions.add(e.getKey());}
		} else sortedQuestions = questions;

		try
		{
			PrintWriter out = new PrintWriter(link);
			final Map<String,Integer> question2Id = new HashMap<String,Integer>();
			// only the reference data contains entries for questions without answers
			for(Integer i: evaluation.referenceData.id2Question.keySet()) {question2Id.put(evaluation.referenceData.id2Question.get(i),i);}
			out.println("<!DOCTYPE html><html>\n<head><title>"+title+"</title></head>\n<body>\n<table border='1'>");
			if(queriesAvailable)
			{				
				out.println("<tr><th>Question</th><th>Learned Query</th><th>Reference Query</th><th>Learned Answers</th><th>Reference Answers</th><th>Error Type</th>"+
						(jaccard?"<th>jaccard</th>":"")+"</tr>");
				for(String question: sortedQuestions)
				{
					Integer id = question2Id.get(question);
					if(evaluation.testData.id2Answers.get(id)==null) {System.err.println(question);continue;}
					out.println(
							"<tr><td>"+question+"</td>"+
									"<td><code><pre>"+escapePre(evaluation.testData.id2Query.get(id))+"</pre></code></td>"+
									"<td><code><pre>"+escapePre(evaluation.referenceData.id2Query.get(id))+"</pre></code></td>"+
									"<td><ul>"+getAnswerHTMLList(evaluation.testData.id2Answers.get(id).toArray(new String[0]))+"</ul></td>"+
									"<td><ul>"+getAnswerHTMLList(evaluation.referenceData.id2Answers.get(id).toArray(new String[0]))+"</ul></td>"+
									"<td>"+evaluation.testData.id2LearnStatus.get(id)+"</td>"+
									"<td>"+(jaccard?evaluation.question2JaccardOfAnswers.get(question):"")+"</td></tr>");					
				}
			} else
			{
				out.println("<tr><th>Question</th><th>Error Type</th></tr>");
				for(String question: sortedQuestions)
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
			out.println(diffHTML("Partly correctly Answered Questions", from.partlyCorrectlyAnsweredQuestions, to.partlyCorrectlyAnsweredQuestions));
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
				sb2.append(createColoredColumn(new File(folder,"correctly_answered.html"),	"Correctly Answered Questions",		"green",	e.correctlyAnsweredQuestions,	e.numberOfQuestions,true,false,e));
				sb2.append(createColoredColumn(new File(folder,"partly_correctly_answered.html"),	"Partly Correctly Answered Questions",	"gold",	e.partlyCorrectlyAnsweredQuestions,	e.numberOfQuestions,true,true,e));
				sb2.append(createColoredColumn(new File(folder,"incorrectly_answered.html"),	"Incorrectly Answered Questions",	"darkorange",	e.incorrectlyAnsweredQuestions,	e.numberOfQuestions,true,false,e));
				sb2.append(createColoredColumn(new File(folder,"unanswered.html"),			"Unanswered Questions",				"red",		e.unansweredQuestions,			e.numberOfQuestions,false,false,e));
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
	//	protected void updateFile(File originalFile, File updatedFile, String endpoint)
	//	{
	//
	//
	//	}

	//	protected void test(File file) throws MalformedURLException, InterruptedException
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

	protected static ResultSet executeSelect(SparqlEndpoint endpoint, String query,  ExtractionDBCache cache){return SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));}
}
