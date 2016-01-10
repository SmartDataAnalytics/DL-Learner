package org.dllearner.core.owl;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;

/**
 * Represents a hierarchy of datatype properties.
 * 
 * TODO: Currently, the role hierarchy pruning algorithm (analogous to the
 * subsumption hierarchy) is not implemented.  
 * 
 * @author Jens Lehmann
 *
 */
public class DatatypePropertyHierarchy extends AbstractHierarchy<OWLDataProperty>{

	private static final OWLDataProperty OWL_TOP_DATA_PROPERTY = new OWLDataPropertyImpl(
			OWLRDFVocabulary.OWL_TOP_DATA_PROPERTY.getIRI());
	private static final OWLDataProperty OWL_BOTTOM_DATA_PROPERTY = new OWLDataPropertyImpl(
			OWLRDFVocabulary.OWL_BOTTOM_DATA_PROPERTY.getIRI());

	public DatatypePropertyHierarchy(
			SortedMap<OWLDataProperty, SortedSet<OWLDataProperty>> roleHierarchyUp,
			SortedMap<OWLDataProperty, SortedSet<OWLDataProperty>> roleHierarchyDown) {
		super(roleHierarchyUp, roleHierarchyDown);
	}
	
	public SortedSet<OWLDataProperty> getMoreGeneralRoles(OWLDataProperty role) {
		return new TreeSet<>(getParents(role));
	}
	
	public SortedSet<OWLDataProperty> getMoreSpecialRoles(OWLDataProperty role) {
		return new TreeSet<>(getChildren(role));
	}	
	
	public boolean isSubpropertyOf(OWLDataProperty subProperty, OWLDataProperty superProperty) {
		return isChildOf(subProperty, superProperty);
	}	

	/**
	 * @return The most general roles.
	 */
	public SortedSet<OWLDataProperty> getMostGeneralRoles() {
		return getMostGeneralEntities();
	}

	/**
	 * @return The most special roles.
	 */
	public SortedSet<OWLDataProperty> getMostSpecialRoles() {
		return getMostSpecialEntities();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AbstractHierarchy#getTopConcept()
	 */
	@Override
	public OWLDataProperty getTopConcept() {
		return OWL_TOP_DATA_PROPERTY;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AbstractHierarchy#getBottomConcept()
	 */
	@Override
	public OWLDataProperty getBottomConcept() {
		return OWL_BOTTOM_DATA_PROPERTY;
	}
	
	@Override
	public DatatypePropertyHierarchy clone() {
		return new DatatypePropertyHierarchy(getHierarchyUp(), getHierarchyDown());		
	}
}
