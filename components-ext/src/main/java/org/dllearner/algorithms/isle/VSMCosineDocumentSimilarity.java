/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;

/**
 * Imagine an N-dimensional space where N is the number of unique words in a pair of texts. Each of the two texts 
 * can be treated like a vector in this N-dimensional space. The distance between the two vectors is an indication 
 * of the similarity of the two texts. The cosine of the angle between the two vectors is the most common distance measure.
 * @author Lorenz Buehmann
 *
 */
public class VSMCosineDocumentSimilarity {
	
	enum TermWeighting {
		TF, TF_IDF
	}
	
	public static final String CONTENT = "Content";
    public static final FieldType TYPE_STORED = new FieldType();
    
    private final Set<String> terms = new HashSet<>();
    private final RealVector v1;
    private final RealVector v2;
    
    static {
        TYPE_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        TYPE_STORED.setTokenized(true);
        TYPE_STORED.setStored(true);
        TYPE_STORED.setStoreTermVectors(true);
        TYPE_STORED.setStoreTermVectorPositions(true);
        TYPE_STORED.freeze();
    }
    
    public VSMCosineDocumentSimilarity(String s1, String s2, TermWeighting termWeighting) throws IOException {
    	//create the index
        Directory directory = createIndex(s1, s2);
        IndexReader reader = DirectoryReader.open(directory);
        //generate the document vectors
        if(termWeighting == TermWeighting.TF){//based on term frequency only
        	//compute the term frequencies for document 1
            Map<String, Integer> f1 = getTermFrequencies(reader, 0);
            //compute the term frequencies for document 2
            Map<String, Integer> f2 = getTermFrequencies(reader, 1);
            reader.close();
            //map both documents to vector objects
            v1 = getTermVectorInteger(f1);
            v2 = getTermVectorInteger(f2);
        } else if(termWeighting == TermWeighting.TF_IDF){//based on tf*idf weighting
        	//compute the term frequencies for document 1
            Map<String, Double> f1 = getTermWeights(reader, 0);
            //compute the term frequencies for document 2
            Map<String, Double> f2 = getTermWeights(reader, 1);
            reader.close();
            //map both documents to vector objects
            v1 = getTermVectorDouble(f1);
            v2 = getTermVectorDouble(f2);
        } else {
        	v1 = null;
        	v2 = null;
        }
    }
    
    public VSMCosineDocumentSimilarity(String s1, String s2) throws IOException {
    	this(s1, s2, TermWeighting.TF_IDF);
    }
    
    /**
     * Returns the cosine document similarity between document {@code doc1} and {@code doc2} using TF-IDF as weighting for each term.
     * The resulting similarity ranges from -1 meaning exactly opposite, to 1 meaning exactly the same, 
     * with 0 usually indicating independence, and in-between values indicating intermediate similarity or dissimilarity.
     * @param doc1
     * @param doc2
     * @return
     * @throws IOException
     */
    public static double getCosineSimilarity(String doc1, String doc2)
            throws IOException {
        return new VSMCosineDocumentSimilarity(doc1, doc2).getCosineSimilarity();
    }
    
    /**
     * Returns the cosine document similarity between document {@code doc1} and {@code doc2} based on {@code termWeighting} to compute the weight
     * for each term in the documents.
     * The resulting similarity ranges from -1 meaning exactly opposite, to 1 meaning exactly the same, 
     * with 0 usually indicating independence, and in-between values indicating intermediate similarity or dissimilarity.
     * @param doc1
     * @param doc2
     * @return
     * @throws IOException
     */
    public static double getCosineSimilarity(String doc1, String doc2, TermWeighting termWeighting)
            throws IOException {
        return new VSMCosineDocumentSimilarity(doc1, doc2, termWeighting).getCosineSimilarity();
    }
    
    /**
     * Create a in-memory Lucene index for both documents.
     * @param s1
     * @param s2
     * @return
     * @throws IOException
     */
    private Directory createIndex(String s1, String s2) throws IOException {
        Directory directory = new MMapDirectory(Files.createTempDirectory("Lucene"));
        Analyzer analyzer = new SimpleAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, iwc);
        addDocument(writer, s1);
        addDocument(writer, s2);
        writer.close();
        return directory;
    }
    
    /**
     * Add the document to the Lucene index.
     * @param writer
     * @param content
     * @throws IOException
     */
    private void addDocument(IndexWriter writer, String content) throws IOException {
        Document doc = new Document();
        Field field = new Field(CONTENT, content, TYPE_STORED);
        doc.add(field);
        writer.addDocument(doc);
    }
    
    /**
     * Get the frequency of each term contained in the document.
     * @param reader
     * @param docId
     * @return
     * @throws IOException
     */
    private Map<String, Integer> getTermFrequencies(IndexReader reader, int docId)
            throws IOException {
        Terms vector = reader.getTermVector(docId, CONTENT);
        TermsEnum termsEnum = vector.iterator();
        Map<String, Integer> frequencies = new HashMap<>();
        BytesRef text = null;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            int freq = (int) termsEnum.totalTermFreq();
            frequencies.put(term, freq);
            terms.add(term);
        }
        return frequencies;
    }
    
    /**
     * Get the weight(tf*idf) of each term contained in the document.
     * @param reader
     * @param docId
     * @return
     * @throws IOException
     */
    private Map<String, Double> getTermWeights(IndexReader reader, int docId)
            throws IOException {
        Terms vector = reader.getTermVector(docId, CONTENT);
        //TODO: not sure if this is reasonable but it prevents NPEs
        if (vector == null) {
            return new HashMap<>();
        }
        TermsEnum termsEnum = vector.iterator();
        Map<String, Double> weights = new HashMap<>();
        BytesRef text = null;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            //get the term frequency
            int tf = (int) termsEnum.totalTermFreq();
            //get the document frequency
            int df = reader.docFreq(new Term(CONTENT, text));
            //compute the inverse document frequency
            double idf = getIDF(reader.numDocs(), df);
            //compute tf*idf
            double weight = tf * idf;
            
            weights.put(term, weight);
            terms.add(term);
        }
        return weights;
    }
    
    private double getIDF(int totalNumberOfDocuments, int documentFrequency){
    	return 1 + Math.log(totalNumberOfDocuments/documentFrequency);
    }
    
    private double getCosineSimilarity() {
        return (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());
    }
    
    private RealVector getTermVectorInteger(Map<String, Integer> map) {
        RealVector vector = new ArrayRealVector(terms.size());
        int i = 0;
        for (String term : terms) {
            int value = map.containsKey(term) ? map.get(term) : 0;
            vector.setEntry(i++, value);
        }
        return vector.mapDivide(vector.getL1Norm());
    }
    
    private RealVector getTermVectorDouble(Map<String, Double> map) {
        RealVector vector = new ArrayRealVector(terms.size());
        int i = 0;
        for (String term : terms) {
            double value = map.containsKey(term) ? map.get(term) : 0d;
            vector.setEntry(i++, value);
        }
        return vector.mapDivide(vector.getL1Norm());
    }
    
    public static void main(String[] args) throws Exception {
		double cosineSimilarity = VSMCosineDocumentSimilarity.getCosineSimilarity("The king is here", "The salad is cold");
		System.out.println(cosineSimilarity);
	}

}
