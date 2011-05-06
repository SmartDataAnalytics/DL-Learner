package org.dllearner.algorithm.tbsl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.algorithm.tbsl.templator.Templator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TemplateGenerationTest {
	
	private static List<String> readQuestions(File file){
		List<String> questions = new ArrayList<String>();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList questionNodes = doc.getElementsByTagName("question");
			
			String question;
			for(int i = 0; i < questionNodes.getLength(); i++){
				Element questionNode = (Element) questionNodes.item(i);
				//Read question
				question = ((Element)questionNode.getElementsByTagName("string").item(0)).getChildNodes().item(0).getNodeValue().trim();
				
				questions.add(question);
				
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
		return questions;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File("src/main/resources/tbsl/evaluation/dbpedia-test-questions.xml");
		List<String> questions = readQuestions(file);
		
		Templator templateGenerator = new Templator();
		int cnt = 0;
		for(String question : questions){
			System.out.println("Question: " + question);
			try {
				Set<Template> templates = templateGenerator.buildTemplates(question);
				if(!templates.isEmpty()){
					cnt++;
				}
				for(Template t : templates){
					System.out.println(t);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Could generate templates for " + cnt + "/" + questions.size() + " questions.");

	}

}
