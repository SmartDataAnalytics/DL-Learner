/**
 * 
 */
package org.dllearner.algorithms.isle.index.syntactic;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dllearner.algorithms.isle.TextDocumentGenerator;
import org.dllearner.algorithms.isle.index.AnnotatedDocument;
import org.dllearner.algorithms.isle.index.AnnotatedTextDocument;
import org.dllearner.algorithms.isle.index.Index;
import org.dllearner.algorithms.isle.index.TextDocument;
import org.dllearner.algorithms.isle.index.Token;
import org.dllearner.algorithms.isle.textretrieval.AnnotationEntityTextRetriever;
import org.dllearner.algorithms.isle.textretrieval.RDFSLabelEntityTextRetriever;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Lorenz Buehmann
 *
 */
public class LuceneSyntacticIndex implements Index {
	
	private IndexSearcher searcher;
	private QueryParser parser;
	private IndexReader indexReader;
	private String searchField;
	
	AnnotationEntityTextRetriever textRetriever;

	public LuceneSyntacticIndex(OWLOntology ontology, IndexReader indexReader, String searchField) {
		this.indexReader = indexReader;
		this.searchField = searchField;
		searcher = new IndexSearcher(indexReader);
		StandardAnalyzer analyzer = new StandardAnalyzer();
		parser = new QueryParser(searchField, analyzer);
		
		textRetriever = new RDFSLabelEntityTextRetriever(ontology);
	}
	
	public LuceneSyntacticIndex(OWLOntology ontology, Directory directory, String searchField) throws Exception {
		this(ontology, DirectoryReader.open(directory), searchField);
	}
	
	public LuceneSyntacticIndex(OWLOntology ontology, String indexDirectory, String searchField) throws Exception {
		this(ontology, DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectory))), searchField);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.SyntacticIndex#getDocuments(java.lang.String)
	 */
	@Override
	public Set<AnnotatedDocument> getDocuments(OWLEntity entity) {
		Set<AnnotatedDocument> documents = new HashSet<>();
		
		Map<List<Token>, Double> relevantText = textRetriever.getRelevantText(entity);
		
		for (Entry<List<Token>, Double> entry : relevantText.entrySet()) {
			List<Token> tokens = entry.getKey();
			for (Token token : tokens) {
				try {
					Query query = parser.parse(token.getRawForm());
					ScoreDoc[] result = searcher.search(query, indexReader.numDocs()).scoreDocs;
					for (ScoreDoc aResult : result) {
						Document doc = searcher.doc(aResult.doc);
						documents.add(new AnnotatedTextDocument(
								TextDocumentGenerator.getInstance().generateDocument(doc.get(searchField)),
								Collections.EMPTY_SET));
					}
				} catch (ParseException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return documents;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Index#getTotalNumberOfDocuments()
	 */
	@Override
	public long getTotalNumberOfDocuments() {
		return indexReader.numDocs();
	}
	
	public Set<TextDocument> getAllDocuments(){
		Set<TextDocument> documents = new HashSet<>(indexReader.numDocs());
		for (int i = 0; i < indexReader.numDocs(); i++) {
			try {
				Document doc = indexReader.document(i);
				String content = doc.get(searchField);
				documents.add(TextDocumentGenerator.getInstance().generateDocument(content));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return documents;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Index#getNumberOfDocumentsFor(org.dllearner.core.owl.Entity)
	 */
	@Override
	public long getNumberOfDocumentsFor(OWLEntity entity) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.index.Index#getNumberOfDocumentsFor(org.dllearner.core.owl.Entity[])
	 */
	@Override
	public long getNumberOfDocumentsFor(OWLEntity... entities) {
		return 0;
	}

}
