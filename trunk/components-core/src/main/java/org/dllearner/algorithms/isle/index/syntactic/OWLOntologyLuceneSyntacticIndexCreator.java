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
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Creates a Lucene Index for the labels if classes and properties.
 * @author Lorenz Buehmann
 *
 */
public class OWLOntologyLuceneSyntacticIndexCreator {

	private Directory directory = new RAMDirectory();
	private OWLOntology ontology;
	private Set<OWLEntity> schemaEntities;
	
	private OWLDataFactory df = new OWLDataFactoryImpl();
	private OWLAnnotationProperty annotationProperty = df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
	private String language = "en";
	private String searchField;
	
	public OWLOntologyLuceneSyntacticIndexCreator(OWLOntology ontology, OWLAnnotationProperty annotationProperty, String searchField) throws IOException {
		this.ontology = ontology;
		this.annotationProperty = annotationProperty;
		this.searchField = searchField;
		
		schemaEntities = new HashSet<OWLEntity>();
		schemaEntities.addAll(ontology.getClassesInSignature());
		schemaEntities.addAll(ontology.getObjectPropertiesInSignature());
		schemaEntities.addAll(ontology.getDataPropertiesInSignature());
	}
	
	public SyntacticIndex buildIndex() throws Exception{
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_43, analyzer);
		IndexWriter writer = new IndexWriter(directory, indexWriterConfig);
		System.out.println( "Creating index ..." );

        Set<org.apache.lucene.document.Document> luceneDocuments = new HashSet<org.apache.lucene.document.Document>();
        FieldType stringType = new FieldType(StringField.TYPE_STORED);
        stringType.setStoreTermVectors(false);
        FieldType textType = new FieldType(TextField.TYPE_STORED);
        textType.setStoreTermVectors(false);
		
		for (OWLEntity entity : schemaEntities) {
			String label = null;
			Set<OWLAnnotation> annotations = entity.getAnnotations(ontology, annotationProperty);
			for (OWLAnnotation annotation : annotations) {
				if (annotation.getValue() instanceof OWLLiteral) {
		            OWLLiteral val = (OWLLiteral) annotation.getValue();
		            if (val.hasLang(language)) {
		            	label = val.getLiteral();
		            }
		        }
			}
			
			if(label != null){
                org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
	            luceneDocument.add(new Field("uri", entity.toStringID(), stringType));
	            luceneDocument.add(new Field(searchField, label, textType));
	            luceneDocuments.add(luceneDocument);
			}
			
		}
		writer.addDocuments(luceneDocuments);
		
		System.out.println("Done.");
		writer.close();
		
		return new LuceneSyntacticIndex(directory, searchField);
	}
	
	

}
