package org.dllearner.core.owl;

import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

/**
 * Represents a hierarchy of object properties (roles in Description Logics).
 * 
 * @author Jens Lehmann
 *
 */
public class ObjectPropertyHierarchy extends AbstractHierarchy<OWLObjectProperty>{
	
	private static final OWLObjectProperty OWL_TOP_OBJECT_PROPERTY = new OWLObjectPropertyImpl(
			OWLRDFVocabulary.OWL_TOP_OBJECT_PROPERTY.getIRI());
	private static final OWLObjectProperty OWL_BOTTOM_OBJECT_PROPERTY = new OWLObjectPropertyImpl(
			OWLRDFVocabulary.OWL_BOTTOM_OBJECT_PROPERTY.getIRI());

	public ObjectPropertyHierarchy(
			SortedMap<OWLObjectProperty, SortedSet<OWLObjectProperty>> roleHierarchyUp,
			SortedMap<OWLObjectProperty, SortedSet<OWLObjectProperty>> roleHierarchyDown) {
		super(roleHierarchyUp, roleHierarchyDown);
	}
	
	public SortedSet<OWLObjectProperty> getMoreGeneralRoles(OWLObjectProperty role) {
		return new TreeSet<>(getParents(role));
	}
	
	public SortedSet<OWLObjectProperty> getMoreSpecialRoles(OWLObjectProperty role) {
		return new TreeSet<>(getChildren(role));
	}
	
	public boolean isSubpropertyOf(OWLObjectProperty subProperty, OWLObjectProperty superProperty) {
		return isChildOf(subProperty, superProperty);
	}	

	/**
	 * @return The most general roles.
	 */
	public SortedSet<OWLObjectProperty> getMostGeneralRoles() {
		return getMostGeneralEntities();
	}

	/**
	 * @return The most special roles.
	 */
	public SortedSet<OWLObjectProperty> getMostSpecialRoles() {
		return getMostSpecialEntities();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AbstractHierarchy#getTopConcept()
	 */
	@Override
	public OWLObjectProperty getTopConcept() {
		return OWL_TOP_OBJECT_PROPERTY;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AbstractHierarchy#getBottomConcept()
	 */
	@Override
	public OWLObjectProperty getBottomConcept() {
		return OWL_BOTTOM_OBJECT_PROPERTY;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AbstractHierarchy#toString(java.util.SortedMap, org.semanticweb.owlapi.model.OWLObject, int)
	 */
	@Override
	protected String toString(SortedMap<OWLObjectProperty, SortedSet<OWLObjectProperty>> hierarchy,
			OWLObjectProperty prop, int depth) {
		String str = "";
		for (int i = 0; i < depth; i++)
			str += "  ";
		str += prop.toString() + "\n";
		Set<OWLObjectProperty> tmp;
		if(prop.isTopEntity()) {
			tmp = getMostGeneralRoles();
		} else {
			tmp  = hierarchy.get(prop);
		}
		
		if (tmp != null) {
			for (OWLObjectProperty c : tmp)
				str += toString(hierarchy, c, depth + 1);
		}
		return str;
	}
	
	@Override
	public ObjectPropertyHierarchy clone() {
		return new ObjectPropertyHierarchy(getHierarchyUp(), getHierarchyDown());		
	}
}
