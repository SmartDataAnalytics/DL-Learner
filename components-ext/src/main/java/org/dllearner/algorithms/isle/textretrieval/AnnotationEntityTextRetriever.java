/**
 * 
 */
package org.dllearner.algorithms.isle.textretrieval;

import com.google.common.base.Joiner;
import org.dllearner.algorithms.isle.TextDocumentGenerator;
import org.dllearner.algorithms.isle.index.LinguisticUtil;
import org.dllearner.algorithms.isle.index.Token;
import org.dllearner.kb.OWLAPIOntology;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;

/**
 * @author Lorenz Buehmann
 *
 */
public class AnnotationEntityTextRetriever implements EntityTextRetriever{
	
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	
	private String language = "en";
	private double weight = 1d;
	
	private boolean useShortFormFallback = true;
	private IRIShortFormProvider sfp = new SimpleIRIShortFormProvider();
	protected boolean determineHeadNoun = false;
	
	private OWLAnnotationProperty[] properties;
	
	private static final OWLClass OWL_THING = new OWLDataFactoryImpl().getOWLThing();

	public AnnotationEntityTextRetriever(OWLOntology ontology, OWLAnnotationProperty... properties) {
		this.ontology = ontology;
		this.properties = properties;
	}
	
	public AnnotationEntityTextRetriever(OWLAPIOntology ontology, OWLAnnotationProperty... properties) {
		this.ontology = ontology.createOWLOntology(manager);
	}
	
	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	
	/**
	 * Whether to use the short form of the IRI as fallback, if no label is given.
	 * @param useShortFormFallback the useShortFormFallback to set
	 */
	public void setUseShortFormFallback(boolean useShortFormFallback) {
		this.useShortFormFallback = useShortFormFallback;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.isle.EntityTextRetriever#getRelevantText(org.dllearner.core.owl.Entity)
	 */
	@Override
	public Map<List<Token>, Double> getRelevantText(OWLEntity entity) {
		Map<List<Token>, Double> textWithWeight = new HashMap<>();
		
		for (OWLAnnotationProperty property : properties) {
			Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(entity, ontology, property);
			for (OWLAnnotation annotation : annotations) {
				if (annotation.getValue() instanceof OWLLiteral) {
		            OWLLiteral val = (OWLLiteral) annotation.getValue();
		            if (val.hasLang(language)) {
		            	//trim
		            	String label = val.getLiteral().trim();
		            	if(entity.isOWLClass()){
		            		label = label.toLowerCase();
		            	}
		            	//remove content in brackets like (...)
		            	label = label.replaceAll("\\s?\\((.*?)\\)", "");
		            	try {
							textWithWeight.put(TextDocumentGenerator.getInstance().generateDocument(label, determineHeadNoun), weight);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
		            }
		        }
			}
		}
		
		if(textWithWeight.isEmpty() && useShortFormFallback){
			String shortForm = sfp.getShortForm(entity.getIRI());
			shortForm = Joiner.on(" ").join(LinguisticUtil.getInstance().getWordsFromCamelCase(shortForm));
			shortForm = Joiner.on(" ").join(LinguisticUtil.getInstance().getWordsFromUnderscored(shortForm)).trim();
			textWithWeight.put(TextDocumentGenerator.getInstance().generateDocument(shortForm, determineHeadNoun), weight);
		}
		
		return textWithWeight;
	}
	
	@Override
	public Map<String, Double> getRelevantTextSimple(OWLEntity entity) {
		Map<String, Double> textWithWeight = new HashMap<>();
		
		for (OWLAnnotationProperty property : properties) {
			Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(entity, ontology, property);
			for (OWLAnnotation annotation : annotations) {
				if (annotation.getValue() instanceof OWLLiteral) {
		            OWLLiteral val = (OWLLiteral) annotation.getValue();
		            if (val.hasLang(language)) {
		            	//trim
		            	String label = val.getLiteral().trim();
		            	if(entity.isOWLClass()){
		            		label = label.toLowerCase();
		            	}
		            	//remove content in brackets like (...)
		            	label = label.replaceAll("\\s?\\((.*?)\\)", "");
		            	try {
							textWithWeight.put(label, weight);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
		            }
		        }
			}
		}
		
		if(textWithWeight.isEmpty() && useShortFormFallback){
			String shortForm = sfp.getShortForm(IRI.create(entity.toStringID()));
			shortForm = Joiner.on(" ").join(LinguisticUtil.getInstance().getWordsFromCamelCase(shortForm));
			shortForm = Joiner.on(" ").join(LinguisticUtil.getInstance().getWordsFromUnderscored(shortForm)).trim();
			textWithWeight.put(shortForm, weight);
		}
		
		return textWithWeight;
	}
	
	/**
	 * Returns for each entity in the ontology all relevant text, i.e. either the annotations or the short form of the IRI as fallback.
	 * @return
	 */
	@Override
	public Map<OWLEntity, Set<List<Token>>> getRelevantText(OWLOntology ontology) {
		Map<OWLEntity, Set<List<Token>>> entity2RelevantText = new HashMap<>();
		
		Set<OWLEntity> schemaEntities = new HashSet<>();
		schemaEntities.addAll(ontology.getClassesInSignature());
		schemaEntities.addAll(ontology.getObjectPropertiesInSignature());
		schemaEntities.addAll(ontology.getDataPropertiesInSignature());
		schemaEntities.remove(OWL_THING);
		
		Map<List<Token>, Double> relevantText;
		for (OWLEntity entity : schemaEntities) {
			relevantText = getRelevantText(entity);
			entity2RelevantText.put(entity, relevantText.keySet());
		}
		
		return entity2RelevantText;
	}
}
