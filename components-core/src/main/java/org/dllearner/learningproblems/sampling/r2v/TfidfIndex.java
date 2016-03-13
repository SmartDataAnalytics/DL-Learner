package org.dllearner.learningproblems.sampling.r2v;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dllearner.learningproblems.sampling.r2v.tfidf.TfIdf;
import org.dllearner.learningproblems.sampling.r2v.tfidf.ngram.NgramTfIdf;


/**
 * @author Tommaso Soru <tsoru@informatik.uni-leipzig.de>
 *
 */
class TfidfIndex {

	private static final int NGRAMS = 1;

	/**
	 * Text to be indexed.
	 */
	private List<String> text = new ArrayList<>();
	
	/**
	 * 
	 */
	private Map<String, Map<String, Double>> index; 
	
	private R2VProperty property;

	/**
	 * NGrams to be considered.
	 */
	private static List<Integer> ns = new ArrayList<>();

	static {
		for (int i = 1; i <= NGRAMS; i++)
			ns.add(i);
	}

	public TfidfIndex(R2VProperty property) {
		super();
		this.property = property;
		this.index = new HashMap<>();
	}

	public List<String> getText() {
		return text;
	}

	public R2VProperty getProperty() {
		return property;
	}
	
	public void addNumeric(String doc) {
		text.add(doc);
	}
	
	public void compute() {
		Iterable<Collection<String>> documents = NgramTfIdf.ngramDocumentTerms(
				ns, text);
		Iterable<Map<String, Double>> tfs = TfIdf.tfs(documents);
		Map<String, Double> idf = TfIdf.idfFromTfs(tfs);	
		Iterator<Map<String, Double>> tfsIter = tfs.iterator();
		for (int i=0; tfsIter.hasNext(); i++) {
			String doc = text.get(i);
			Map<String, Double> tf = tfsIter.next();
			Map<String, Double> tfIdf = TfIdf.tfIdf(tf, idf,
					TfIdf.Normalization.COSINE);
			index.put(doc, tfIdf);
		}
	}
	
	public Map<String, Double> tfidf(String doc) {
		return index.get(doc);
	}

}
