package org.dllearner.algorithm.tbsl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
		
		File file = new File("src/main/resources/tbsl/evaluation/dbpedia-test-questions-tagged(ideal).xml");
		List<String> questions = readQuestions(file);
		
		StringBuilder successful = new StringBuilder();
		StringBuilder failed = new StringBuilder();
		
		Templator templateGenerator = new Templator();
		templateGenerator.setUNTAGGED_INPUT(false);
		
		int cnt = 0;
		for(String question : questions){
			System.out.println("Question: " + question);
			try {
				Set<Template> templates = templateGenerator.buildTemplates(question);
				if(!templates.isEmpty()){
					cnt++;
					successful.append("*****************************************************************\n");
					successful.append(question).append("\n");
				} else {
					failed.append(question).append("\n");
				}
				for(Template t : templates){
					successful.append(t);
					System.out.println(t);
				}
			} catch (Exception e) {
				failed.append(question).append("\n");
				e.printStackTrace();
			}
		}
		
		System.out.println("Could generate templates for " + cnt + "/" + questions.size() + " questions.");
		
		System.out.println(successful);
		System.out.println(failed);
		
		try {
			Writer out = new OutputStreamWriter(new FileOutputStream(
					"successful.txt"));
			Writer out2 = new OutputStreamWriter(new FileOutputStream(
					"failed.txt"));
			try {
				out.write(successful.toString());
				out2.write(failed.toString());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					out.close();
					out2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		

	}

}
