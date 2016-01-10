package org.dllearner.core.owl;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a class subsumption hierarchy (ignoring equivalent concepts).
 * 
 * @author Jens Lehmann
 * 
 */
public class LazyClassHierarchy extends ClassHierarchy {

	public static Logger logger = LoggerFactory.getLogger(LazyClassHierarchy.class);
	
	private AbstractReasonerComponent rc;
	
	public LazyClassHierarchy(AbstractReasonerComponent rc) {
		super(new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>(), new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>());
		this.rc = rc;
	}

	@Override
	public SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression concept, boolean direct) {
		return rc.getSuperClasses(concept);
	}

	@Override
	public SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression concept, boolean direct) {
		return rc.getSubClasses(concept);
	}

	@Override
	public LazyClassHierarchy clone() {
		return new LazyClassHierarchy(rc);		
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AbstractHierarchy#cloneAndRestrict(java.util.Set)
	 */
	@Override
	public AbstractHierarchy<OWLClassExpression> cloneAndRestrict(Set<? extends OWLClassExpression> allowedEntities) {
		return new LazyClassHierarchy(rc);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AbstractHierarchy#thinOutSubsumptionHierarchy()
	 */
	@Override
	public void thinOutSubsumptionHierarchy() {
		// do nothing here because we don't have anything precomputed
	}
}
