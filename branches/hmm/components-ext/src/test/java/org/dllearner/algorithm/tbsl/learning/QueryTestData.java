/** Helper Class for SPARQLTemplateBasedLearner3Test that encapsulates questions, their learned SPARQL queries and the answers of those SPARQL queries.
 * Also provides methods for serialization and import/export in the QALD benchmark XML format.**/
package org.dllearner.algorithm.tbsl.learning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryTestData implements Serializable
{
	public enum EvaluationStatus {CORRECT,PARTIALLY_CORRECT,INCORRECT};
	
	private static final long	serialVersionUID	= 2L;
	public boolean hmm = false;
	public SortedMap<Integer, String> id2Question = new ConcurrentSkipListMap<Integer, String>();
	public SortedMap<Integer, String> id2Query = new ConcurrentSkipListMap<Integer, String>();
	public SortedMap<Integer, Set<String>> id2Answers = new ConcurrentSkipListMap<Integer, Set<String>>();
	public SortedMap<Integer, LearnStatus> id2QueryStatus = new ConcurrentSkipListMap<Integer, LearnStatus>();
	public SortedMap<Integer, EvaluationStatus> id2EvaluationStatus = new ConcurrentSkipListMap<Integer, EvaluationStatus>();
	/** TODO: include in the xml*/
	public SortedMap<Integer, LearnStatus> id2AnswerStatus = new ConcurrentSkipListMap<Integer, LearnStatus>();

	private static final String persistancePath = "cache/"+SPARQLTemplateBasedLearner3Test.class.getSimpleName()+'/'+QueryTestData.class.getSimpleName();

	/** Saves the test data to a binary file to a default location overwriting the last save. Uses serialization. **/
	public synchronized void save()
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(persistancePath)));
			oos.writeObject(this);
			oos.close();
		} catch(IOException e) {throw new RuntimeException(e);}
	}

	/** Loads the test data written by save(). **/
	public static QueryTestData load() throws FileNotFoundException, IOException
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

	public QueryTestData generateAnswers(SparqlEndpoint endpoint, ExtractionDBCache cache,Model model, int maxAnswers)
	{		
		if(!id2Answers.isEmpty()) {throw new AssertionError("Answers already existing.");}
		for(int i:id2Query.keySet())
		{
			long start = System.currentTimeMillis();
			try
			{
				Set<String> uris = SPARQLTemplateBasedLearner3Test.getUris(endpoint, id2Query.get(i),cache,model);
				if(uris.size()>maxAnswers)
				{
					uris = new HashSet<String>(new ArrayList<String>(uris).subList(0, maxAnswers-1));
				}
				// empty answer set better transfers intended meaning and doesn't cause NPEs in html generation :-)
				id2Answers.put(i, uris);
				if(!uris.isEmpty())	{/*id2Answers.put(i, uris);*/}
				else				{id2QueryStatus.put(i, LearnStatus.QUERY_RESULT_EMPTY);}

			}
			catch(Exception e)
			{
				id2Answers.put(i, Collections.<String>emptySet());
				id2AnswerStatus.put(i, LearnStatus.exceptionStatus(e,System.currentTimeMillis()-start));
			}
		}
		return this;
	}
	/** @see readQaldXml(File file, int MAX_NUMBER_OF_QUESTIONS, boolean whitelistOnly, Set<Integer> whitelist)**/
	public static QueryTestData readQaldXml(final File file)
	{
		return readQaldXml(file, Integer.MAX_VALUE, false,null);
	}

	/** reads test data from a QALD2 benchmark XML file, including questions, queries and answers.
	 * each question needs to have a query but not necessarily an answer.
	 * @param file a QALD benchmark XML file 
	 * @param MAX_NUMBER_OF_QUESTIONS the maximum number of questions read from the file. 
	 * @return the test data read from the XML file */	
	public static QueryTestData readQaldXml(final File file, final int MAX_NUMBER_OF_QUESTIONS, boolean whitelistOnly,Set<Integer> whitelist)
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
				if(i>MAX_NUMBER_OF_QUESTIONS) break;
				String question;
				String query;
				Set<String> answers = new HashSet<String>();
				Element questionNode = (Element) questionNodes.item(i);
				//read question ID
				id = Integer.valueOf(questionNode.getAttribute("id"));
				if(whitelistOnly&&!whitelist.contains(id)) {continue;}
				
				//Read question
				question = ((Element)questionNode.getElementsByTagName("string").item(0)).getChildNodes().item(0).getNodeValue().trim();
				// TODO: read evaluation status
				//Read SPARQL query

				NodeList queryElements = questionNode.getElementsByTagName("query");
				if(queryElements.getLength()>0)
				{
					query = queryElements.item(0).getChildNodes().item(0).getNodeValue().trim();
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
						// some of our qald files were mistakenly created so that they have the "answer" elements directly under the question node 
						// with no answers element
						if(answersElement==null) answersElement = (Element)questionNode;
						//				if(answersElement!=null)
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
		return testData;
	}

	/** write the test data to a QALD2 benchmark XML file, including questions, queries and answers.
	 * each question needs to have a query but not necessarily an answer.
	 * @param file a QALD benchmark XML file **/ 
	public void writeQaldXml(final File file)
	{		
		// see http://www.genedavis.com/library/xml/java_dom_xml_creation.jsp
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			Element root = doc.createElement("dataset");
			doc.appendChild(root);

			for(Integer i:id2Question.keySet())
			{
				Element questionElement = doc.createElement("question");
				root.appendChild(questionElement);
				questionElement.setAttribute("id", i.toString());
				questionElement.setAttribute("answertype", "resource");				
				Element stringElement = doc.createElement("string");
				stringElement.setTextContent(id2Question.get(i));
				questionElement.appendChild(stringElement);
				String query = id2Query.get(i);
				LearnStatus queryStatus = id2QueryStatus.get(i);

				questionElement.appendChild(queryStatus.element(doc,"queryStatus"));

				if(queryStatus==LearnStatus.OK)
				{					
					Element queryElement = doc.createElement("query");
					//					queryElement.setTextContent(query);
					queryElement.appendChild(doc.createCDATASection(query));
					questionElement.appendChild(queryElement);
				} 

				Collection<String> answers = id2Answers.get(i);
				if(answers!=null)
				{
					for(String answer: answers)
					{
						Element answerElement = doc.createElement("answer");
						answerElement.setTextContent(answer);
						questionElement.appendChild(answerElement);
					}
				}
			}		
			//set up a transformer
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			//create string from xml tree
			PrintWriter sw = new PrintWriter(file);
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);            
		}
		catch (Exception e) {throw new RuntimeException(e);}				
	}

}