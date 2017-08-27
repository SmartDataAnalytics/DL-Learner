package org.dllearner.reasoning;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Allen's interval relations. See e.g.
 * https://en.wikipedia.org/wiki/Allen%27s_interval_algebra
 */
public interface AllenRelationsTemporalOWLReasoner extends TemporalOWLReasoner {
	
	// before/after
	public Set<OWLIndividual> happensBefore(OWLIndividual i);
	
	public boolean happensBefore(OWLIndividual before, OWLIndividual after);
	
	public Set<OWLIndividual> happensAfter(OWLIndividual i);
	
	public boolean happensAfter(OWLIndividual after, OWLIndividual before);
	
	// meets/met-by
	public Set<OWLIndividual> meets(OWLIndividual i);
	
	public boolean meets(OWLIndividual meeting, OWLIndividual met);
	
	public Set<OWLIndividual> metBy(OWLIndividual i);
	
	public boolean metBy(OWLIndividual met, OWLIndividual meeting);
	
	// overlaps-with/overlapped-by
	public Set<OWLIndividual> overlapsWith(OWLIndividual i);
	
	public boolean overlapsWith(OWLIndividual earlier, OWLIndividual later);
	
	public Set<OWLIndividual> overlappedBy(OWLIndividual i);
	
	public boolean overlappedBy(OWLIndividual later, OWLIndividual earlier);
	
	// starts/started-by
	public Set<OWLIndividual> starts(OWLIndividual i);
	
	public boolean starts(OWLIndividual starting, OWLIndividual started);
	
	public Set<OWLIndividual> startedBy(OWLIndividual i);
	
	public boolean startedBy(OWLIndividual started, OWLIndividual starting);
	
	// during/contains
	public Set<OWLIndividual> happensDuring(OWLIndividual i);
	
	public boolean happensDuring(OWLIndividual inner, OWLIndividual outer);
	
	public Set<OWLIndividual> contains(OWLIndividual i);
	
	public boolean contains(OWLIndividual outer, OWLIndividual inner);
	
	// finishes/finished by
	public Set<OWLIndividual> finishes(OWLIndividual i);
	
	public boolean finishes(OWLIndividual finishing, OWLIndividual finished);
	
	public Set<OWLIndividual> finishedBy(OWLIndividual i);
	
	public boolean finishedBy(OWLIndividual finished, OWLIndividual finishing);
	
	// equal
	public Set<OWLIndividual> equals (OWLIndividual i);
	
	public boolean equals(OWLIndividual i, OWLIndividual j);
}
