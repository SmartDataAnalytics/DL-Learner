package org.dllearner.algorithm.tbsl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dllearner.algorithm.tbsl.nlp.ApachePartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.LingPipePartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.StanfordPartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.TreeTagger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class POSTaggerQALDEvaluation {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		List<PartOfSpeechTagger> taggers = Arrays
				.asList(new PartOfSpeechTagger[] {
						new ApachePartOfSpeechTagger(),
						new StanfordPartOfSpeechTagger(),
//						new LingPipePartOfSpeechTagger(),
						new TreeTagger()
						});

		SortedMap<Integer, String> id2UntaggedQuestion = new TreeMap<Integer, String>();
		SortedMap<Integer, String> id2TaggedQuestion = new TreeMap<Integer, String>();

		// read untagged questions
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db
				.parse(POSTaggerQALDEvaluation.class.getClassLoader()
						.getResourceAsStream(
								"tbsl/evaluation/qald2-dbpedia-train.xml"));
		doc.getDocumentElement().normalize();
		NodeList questionNodes = doc.getElementsByTagName("question");
		int id;
		String question;

		for (int i = 0; i < questionNodes.getLength(); i++) {
			Element questionNode = (Element) questionNodes.item(i);
			id = Integer.valueOf(questionNode.getAttribute("id"));
			question = ((Element) questionNode.getElementsByTagName("string")
					.item(0)).getChildNodes().item(0).getNodeValue().trim();
			id2UntaggedQuestion.put(id, question);
		}

		// read ideal tagged questions
		doc = db.parse(POSTaggerQALDEvaluation.class
				.getClassLoader()
				.getResourceAsStream(
						"tbsl/evaluation/qald2-dbpedia-train-tagged(ideal).xml"));
		doc.getDocumentElement().normalize();
		questionNodes = doc.getElementsByTagName("question");
		for (int i = 0; i < questionNodes.getLength(); i++) {
			Element questionNode = (Element) questionNodes.item(i);
			id = Integer.valueOf(questionNode.getAttribute("id"));
			question = ((Element) questionNode.getElementsByTagName("string")
					.item(0)).getChildNodes().item(0).getNodeValue().trim();
			id2TaggedQuestion.put(id, question);
		}
		
//		int correctTaggedQuestions = 0;
//		for(Entry<Integer, String> entry : id2UntaggedQuestion.entrySet()){
//			String untaggedQuestion = entry.getValue();
//			String idealTaggedQuestion = id2TaggedQuestion.get(entry.getKey());
//			for(PartOfSpeechTagger tagger : taggers){
//				List<String> taggedQuestions = tagger.tagTopK(untaggedQuestion);
//				for(String taggedQuestion : taggedQuestions){
//					taggedQuestion = taggedQuestion.replace("?/.", "").replace("./.", "").trim();
//					if(taggedQuestion.equals(idealTaggedQuestion)){
//						correctTaggedQuestions++;
//					}
//				}
//			}
//		}
//		System.out.println("Correct: " + correctTaggedQuestions + "/" + id2TaggedQuestion.size());
		
		for(PartOfSpeechTagger tagger : taggers){
			Map<Integer, Integer> position2Correct = new TreeMap<Integer, Integer>();
			for(Entry<Integer, String> entry : id2UntaggedQuestion.entrySet()){
				String untaggedQuestion = entry.getValue();
				String idealTaggedQuestion = id2TaggedQuestion.get(entry.getKey()).toLowerCase();
				
				List<String> taggedQuestions = tagger.tagTopK(untaggedQuestion);
				for(int i = 0; i < taggedQuestions.size(); i++){
					String taggedQuestion = taggedQuestions.get(i).
							replace("?/.", "").
							replace("./.", "").
							replace("./NN", "/NN").
							replace("?/NN", "/NN").
							replace("./NP", "/NP").
							replace("?/NP", "/NP").
							trim().toLowerCase();
					if(taggedQuestion.equals(idealTaggedQuestion)){
						for(int j = i; j < taggedQuestions.size(); j++){
							Integer correct = position2Correct.get(j);
							if(correct == null){
								correct = Integer.valueOf(1);
							} else {
								correct = correct + 1;
							}
							position2Correct.put(j, correct);
							
						}
					}
				}
			}
			System.out.println(tagger.getName());
			for(Entry<Integer, Integer> pos2CorrectCnt : position2Correct.entrySet()){
				System.out.println("First " + (pos2CorrectCnt.getKey()+1) + " tags: " + pos2CorrectCnt.getValue() + "/" + id2UntaggedQuestion.size());
			}
			
			
		}
		
		

	}

}
