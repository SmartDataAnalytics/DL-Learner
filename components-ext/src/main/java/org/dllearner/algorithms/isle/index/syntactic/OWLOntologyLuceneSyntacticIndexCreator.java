/**
 * 
 */
package org.dllearner.algorithms.isle.index.syntactic;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.dllearner.algorithms.isle.index.Index;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Creates a Lucene Index for the labels if classes and properties.
 * @author Lorenz Buehmann
 *
 */
public class OWLOntologyLuceneSyntacticIndexCreator {

	private Directory directory = new MMapDirectory(Files.createTempDirectory("Lucene"));
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

		schemaEntities = new HashSet<>();
		schemaEntities.addAll(ontology.getClassesInSignature());
		schemaEntities.addAll(ontology.getObjectPropertiesInSignature());
		schemaEntities.addAll(ontology.getDataPropertiesInSignature());
	}
	
	public Index buildIndex() throws Exception{
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(directory, indexWriterConfig);
		System.out.println( "Creating index ..." );

        Set<org.apache.lucene.document.Document> luceneDocuments = new HashSet<>();
        FieldType stringType = new FieldType(StringField.TYPE_STORED);
        stringType.setStoreTermVectors(false);
        FieldType textType = new FieldType(TextField.TYPE_STORED);
        textType.setStoreTermVectors(false);
		
		for (OWLEntity entity : schemaEntities) {
			String label = null;
			Collection<OWLAnnotation> annotations = ontology.getAnnotations();
			for (OWLAnnotation annotation : annotations) {
				if (annotation.getProperty().equals(annotationProperty) && 
						annotation.getValue() instanceof OWLLiteral) {
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
		
		return new LuceneSyntacticIndex(ontology, directory, searchField);
	}
	
	

}
