/**
 * 
 */
package org.dllearner.algorithms.isle.textretrieval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dllearner.algorithms.isle.index.LinguisticAnnotator;
import org.dllearner.algorithms.isle.index.LinguisticUtil;
import org.dllearner.core.owl.Entity;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import com.google.common.base.Joiner;


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
	
	private OWLAnnotationProperty[] properties;

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
	public Map<String, Double> getRelevantText(Entity entity) {
		Map<String, Double> textWithWeight = new HashMap<String, Double>();
		
		OWLEntity e = OWLAPIConverter.getOWLAPIEntity(entity);
		
		for (OWLAnnotationProperty property : properties) {
			Set<OWLAnnotation> annotations = e.getAnnotations(ontology, property);
			for (OWLAnnotation annotation : annotations) {
				if (annotation.getValue() instanceof OWLLiteral) {
		            OWLLiteral val = (OWLLiteral) annotation.getValue();
		            if (val.hasLang(language)) {
		            	String label = val.getLiteral().trim();
		            	textWithWeight.put(label, weight);
		            }
		        }
			}
		}
		
		if(textWithWeight.isEmpty() && useShortFormFallback){
			String shortForm = sfp.getShortForm(IRI.create(entity.getURI()));
			shortForm = Joiner.on(" ").join(LinguisticUtil.getInstance().getWordsFromCamelCase(shortForm));
			shortForm = Joiner.on(" ").join(LinguisticUtil.getInstance().getWordsFromUnderscored(shortForm)).trim();
			textWithWeight.put(shortForm, weight);
		}
		
		return textWithWeight;
	}
	
	/**
	 * Returns for each entity in the ontology all relevant text, i.e. eitherthe annotations or the short form of the IRI as fallback.
	 * @return
	 */
	@Override
	public Map<Entity, Set<String>> getRelevantText(OWLOntology ontology) {
		Map<Entity, Set<String>> entity2RelevantText = new HashMap<Entity, Set<String>>();
		
		Set<OWLEntity> schemaEntities = new HashSet<OWLEntity>();
		schemaEntities.addAll(ontology.getClassesInSignature());
		schemaEntities.addAll(ontology.getObjectPropertiesInSignature());
		schemaEntities.addAll(ontology.getDataPropertiesInSignature());
		
		Map<String, Double> relevantText;
		for (OWLEntity owlEntity : schemaEntities) {
			Entity entity = OWLAPIConverter.getEntity(owlEntity);
			relevantText = getRelevantText(entity);
			entity2RelevantText.put(entity, relevantText.keySet());
		}
		
		return entity2RelevantText;
	}
}
