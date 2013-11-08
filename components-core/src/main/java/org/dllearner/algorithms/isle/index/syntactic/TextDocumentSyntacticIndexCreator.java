/**
 * 
 */
package org.dllearner.algorithms.isle.index.syntactic;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.dllearner.algorithms.isle.index.TextDocument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Creates a syntactic index from text files stored on disk
 *
 */
public class TextDocumentSyntacticIndexCreator {

	private Directory indexDirectory;
    private final File inputDirectory;
    private final static String searchField = "text";

    public TextDocumentSyntacticIndexCreator(File inputDirectory, File indexDirectory)
            throws IOException {
        this.indexDirectory = new SimpleFSDirectory(indexDirectory);
        this.inputDirectory = inputDirectory;
    }

    public SyntacticIndex buildIndex() throws Exception{
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_43, analyzer);
		IndexWriter writer = new IndexWriter(indexDirectory, indexWriterConfig);
		System.out.println( "Creating index ..." );

        Set<org.apache.lucene.document.Document> luceneDocuments = new HashSet<org.apache.lucene.document.Document>();
        FieldType stringType = new FieldType(StringField.TYPE_STORED);
        stringType.setStoreTermVectors(false);
        FieldType textType = new FieldType(TextField.TYPE_STORED);
        textType.setStoreTermVectors(false);
		
		for (File f : inputDirectory.listFiles()) {
            if (!f.getName().endsWith(".txt")) {
                continue;
            }
            org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
            luceneDocument.add(new Field("uri", f.toURI().toString(), stringType));

            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(f));

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
            reader.close();

            luceneDocument.add(new Field(searchField, content.toString(), textType));
            luceneDocuments.add(luceneDocument);
        }
        writer.addDocuments(luceneDocuments);
		
		System.out.println("Done.");
		writer.close();
		
		return new LuceneSyntacticIndex(indexDirectory, searchField);
	}
    
    public SyntacticIndex buildIndex(Set<TextDocument> documents) throws Exception{
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_43, analyzer);
		IndexWriter writer = new IndexWriter(indexDirectory, indexWriterConfig);
		System.out.println( "Creating index ..." );

        Set<org.apache.lucene.document.Document> luceneDocuments = new HashSet<org.apache.lucene.document.Document>();
        FieldType stringType = new FieldType(StringField.TYPE_STORED);
        stringType.setStoreTermVectors(false);
        FieldType textType = new FieldType(TextField.TYPE_STORED);
        textType.setStoreTermVectors(false);
		
        int id = 1;
		for (TextDocument document : documents) {
            org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
            luceneDocument.add(new Field("uri", Integer.toString(id++), stringType));
            luceneDocument.add(new Field(searchField, document.getContent(), textType));
            luceneDocuments.add(luceneDocument);
        }
        writer.addDocuments(luceneDocuments);
		
		System.out.println("Done.");
		writer.close();
		
		return new LuceneSyntacticIndex(indexDirectory, searchField);
	}

    public static SyntacticIndex loadIndex(File indexDirectory) throws Exception {
        return new LuceneSyntacticIndex(new SimpleFSDirectory(indexDirectory), searchField);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: <input directory> <index directory>");
            System.exit(1);
            return;
        }
        new TextDocumentSyntacticIndexCreator(new File(args[0]), new File(args[1])).buildIndex();
    }
}
