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
import org.dllearner.algorithms.isle.index.TextDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Lorenz Buehmann
 *
 */
public class WindowBasedContextExtractor implements ContextExtractor{
	
	private StanfordCoreNLP pipeline;
	private int tokensLeft = 10;
	private int tokensRight = 10;

	public WindowBasedContextExtractor(int tokensLeft, int tokensRight) {
		this.tokensLeft = tokensLeft;
		this.tokensRight = tokensRight;
		
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		pipeline = new StanfordCoreNLP(props);
	}
	
	public WindowBasedContextExtractor(int tokensLeftRight) {
		tokensLeft = tokensLeftRight;
		tokensRight = tokensLeftRight;
		
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		pipeline = new StanfordCoreNLP(props);
	}
	
	public WindowBasedContextExtractor() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		pipeline = new StanfordCoreNLP(props);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.wsd.ContextExtractor#extractContext(java.lang.String, java.lang.String)
	 */
	@Override
	public List<String> extractContext(org.dllearner.algorithms.isle.index.Annotation annotation) {
		// split text into sentences
		List<CoreMap> sentences = getSentences(annotation.getReferencedDocument().getContent());

		// find the sentence containing the token of the annotation
		int tokenStart = annotation.getOffset();
		int index = 0;
		for (CoreMap sentence : sentences) {
			String s = sentence.toString();
			if (index <= tokenStart && s.length() > tokenStart) {
				List<String> context = new ArrayList<String>();
				for (CoreLabel label : sentence.get(TokensAnnotation.class)) {
					// this is the text of the token
					String word = label.get(TextAnnotation.class);

					context.add(word);
				}
				return context;
			}
			index += s.length();
		}
		throw new RuntimeException("Token " + annotation + " not found in text "
				+ annotation.getReferencedDocument().getContent());

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
	
		String token = "services";
		WindowBasedContextExtractor extractor = new WindowBasedContextExtractor();
		List<String> context = extractor.extractContext(new org.dllearner.algorithms.isle.index.Annotation(new TextDocument(s), s.indexOf(token), token.length()));
		System.out.println(context);
	}

}
