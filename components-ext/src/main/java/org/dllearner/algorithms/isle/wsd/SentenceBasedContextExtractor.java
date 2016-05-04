/**
 * 
 */
package org.dllearner.algorithms.isle.wsd;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.dllearner.algorithms.isle.TextDocumentGenerator;
import org.dllearner.algorithms.isle.index.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Lorenz Buehmann
 *
 */
public class SentenceBasedContextExtractor implements ContextExtractor{
	
	private StanfordCoreNLP pipeline;

	public SentenceBasedContextExtractor() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		pipeline = new StanfordCoreNLP(props);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.wsd.ContextExtractor#extractContext(java.lang.String, java.lang.String)
	 */
	@Override
	public List<String> extractContext(org.dllearner.algorithms.isle.index.Annotation annotation) {
		//split text into sentences
		List<CoreMap> sentences = getSentences(annotation.getReferencedDocument().getRawContent());

		//find the sentence containing the token of the annotation
		Token firstToken = annotation.getTokens().get(0);
		for (CoreMap sentence : sentences) {
			boolean found = false;
			for (CoreLabel label : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = label.get(TextAnnotation.class);
				if(word.equals(firstToken.getRawForm())){
					found = true;
					break;
				}
			}
			if(found){
				List<String> context = new ArrayList<>();
				for (CoreLabel label : sentence.get(TokensAnnotation.class)) {
					// this is the text of the token
					String word = label.get(TextAnnotation.class);
					context.add(word);
				}
				return context;
			}
		}
		throw new RuntimeException("Token " + annotation.getString() + " not found in text " + annotation.getReferencedDocument().getRawContent());
	}
	
	private List<CoreMap> getSentences(String document) {
		// create an empty Annotation just with the given text
		Annotation annotation = new Annotation(document);

		// run all Annotators on this text
		pipeline.annotate(annotation);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

		return sentences;
	}
	
	public static void main(String[] args) throws Exception {
		String s = "International Business Machines Corporation, or IBM, is an American multinational services technology and consulting corporation, with headquarters in Armonk, New York, United States. IBM manufactures and markets computer hardware and software,"
				+ " and offers infrastructure, hosting and consulting services in areas ranging from mainframe computers to nanotechnology.";
	
		SentenceBasedContextExtractor extractor = new SentenceBasedContextExtractor();
		List<String> context = extractor.extractContext(new org.dllearner.algorithms.isle.index.Annotation(TextDocumentGenerator.getInstance().generateDocument(s), Arrays.asList(new Token("American"))));
		System.out.println(context);
	}

}
