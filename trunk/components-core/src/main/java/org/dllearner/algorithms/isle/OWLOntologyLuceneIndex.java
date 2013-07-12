/**
 * 
 */
package org.dllearner.algorithms.isle;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.semanticweb.owlapi.model.IRI;
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
public class OWLOntologyLuceneIndex {

	private Directory directory = new RAMDirectory();
	private OWLOntology ontology;
	private Set<OWLEntity> schemaEntities;
	
	private OWLDataFactory df = new OWLDataFactoryImpl();
	private OWLAnnotationProperty annotationProperty = df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
	private String language = "en";
	private String searchField;
	
	public OWLOntologyLuceneIndex(OWLOntology ontology, String searchField) throws IOException {
		this.ontology = ontology;
		this.searchField = searchField;
		
		schemaEntities = new HashSet<OWLEntity>();
		schemaEntities.addAll(ontology.getClassesInSignature());
		schemaEntities.addAll(ontology.getObjectPropertiesInSignature());
		schemaEntities.addAll(ontology.getDataPropertiesInSignature());
		
		buildIndex();
	}
	
	public OWLOntologyLuceneIndex(OWLOntology ontology, OWLAnnotationProperty annotationProperty) throws IOException {
		this.ontology = ontology;
		this.annotationProperty = annotationProperty;
		
		schemaEntities = new HashSet<OWLEntity>();
		schemaEntities.addAll(ontology.getClassesInSignature());
		schemaEntities.addAll(ontology.getObjectPropertiesInSignature());
		schemaEntities.addAll(ontology.getDataPropertiesInSignature());
		
		buildIndex();
	}
	
	/**
	 * @return the ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}
	
	/**
	 * @return the directory
	 */
	public Directory getDirectory() {
		return directory;
	}
	
	/**
	 * @param annotationProperty the annotationProperty to set
	 */
	public void setAnnotationProperty(OWLAnnotationProperty annotationProperty) {
		this.annotationProperty = annotationProperty;
	}
	
	/**
	 * @param annotationProperty the annotationProperty to set
	 */
	public void setAnnotationProperty(String annotationPropertyIRI) {
		this.annotationProperty = df.getOWLAnnotationProperty(IRI.create(annotationPropertyIRI));
	}
	
	public void buildIndex() throws IOException{
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_43, analyzer);
		IndexWriter writer = new IndexWriter(directory, indexWriterConfig);
		System.out.println( "Creating index ..." );
		
		Set<Document> luceneDocuments = new HashSet<Document>();
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
				Document luceneDocument = new Document();
	            luceneDocument.add(new Field("uri", entity.toStringID(), stringType));
	            luceneDocument.add(new Field(searchField, label, textType));
	            luceneDocuments.add(luceneDocument);
			}
			
		}
		writer.addDocuments(luceneDocuments);
		
		System.out.println("Done.");
		writer.close();
	}
	
	

}
