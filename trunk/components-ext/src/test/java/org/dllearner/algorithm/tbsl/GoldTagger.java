package org.dllearner.algorithm.tbsl;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.StanfordPartOfSpeechTagger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GoldTagger {

	static String GOLD = "/home/christina/Downloads/dbpedia-test-new.xml";
	static String OUT  = "/home/christina/Downloads/dbpedia-test-new-tagged.xml";
	
	public static void main(String[] args) {
						
		PartOfSpeechTagger tagger = new StanfordPartOfSpeechTagger();
		
		System.out.print("\nStart tagging " + GOLD + "...");
		try {			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new File(GOLD));
			doc.getDocumentElement().normalize();
			NodeList questionNodes = doc.getElementsByTagName("question");
			String question;
			String tagged;
			for (int i = 0; i < questionNodes.getLength(); i++) {
				Element questionNode = (Element) questionNodes.item(i);
				Node qnode = questionNode.getElementsByTagName("string").item(0).getChildNodes().item(0);
				question = qnode.getNodeValue().trim().replaceAll("[?.!,]","");
				tagged = tagger.tag(question);
				qnode.setNodeValue(tagged);
			}
			
			Source source = new DOMSource(doc);
		    Result result = new StreamResult(new File(OUT));
		    Transformer xformer = TransformerFactory.newInstance().newTransformer();
		    xformer.transform(source,result);
			
			System.out.print("Done.");
			
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}	


}
