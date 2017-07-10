package org.dllearner.reasoning;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.semanticweb.owlapi.model.*;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * The Reasoner component that implements a virtual OWL-Time
 */
@ComponentAnn(description = "OWL-Time reasoner", name = "OWL-Time Reasoner", shortName = "owltimere", version = 0.1)
public class OWLTimeReasoner extends AbstractReasonerComponent {
	@Override
	public void init() throws ComponentInitException {

	}

	@Override
	public Set<OWLClass> getClasses() {
		return null;
	}

	@Override
	public SortedSet<OWLIndividual> getIndividuals() {
		return null;
	}

	@Override
	public String getBaseURI() {
		return null;
	}

	@Override
	public Map<String, String> getPrefixes() {
		return null;
	}

	@Override
	public ReasonerType getReasonerType() {
		return null;
	}

	@Override
	public void releaseKB() {

	}

	@Override
	protected Set<OWLClass> getTypesImpl(OWLIndividual individual) throws ReasoningMethodUnsupportedException {
		return super.getTypesImpl(individual);
	}

	@Override
	protected SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression concept) throws ReasoningMethodUnsupportedException {
		return super.getIndividualsImpl(concept);
	}

	@Override
	protected boolean hasTypeImpl(OWLClassExpression concept, OWLIndividual individual) throws ReasoningMethodUnsupportedException {
		return super.hasTypeImpl(concept, individual);
	}

	@Override
	protected SortedSet<OWLIndividual> hasTypeImpl(OWLClassExpression concept, Set<OWLIndividual> individuals) throws ReasoningMethodUnsupportedException {
		return super.hasTypeImpl(concept, individuals);
	}

	@Override
	protected Set<OWLIndividual> getRelatedIndividualsImpl(OWLIndividual individual, OWLObjectProperty objectProperty) throws ReasoningMethodUnsupportedException {
		return super.getRelatedIndividualsImpl(individual, objectProperty);
	}

	@Override
	protected Set<OWLLiteral> getRelatedValuesImpl(OWLIndividual individual, OWLDataProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		return super.getRelatedValuesImpl(individual, datatypeProperty);
	}

	@Override
	protected Map<OWLIndividual, SortedSet<OWLLiteral>> getDatatypeMembersImpl(OWLDataProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		return super.getDatatypeMembersImpl(datatypeProperty);
	}

	@Override
	public OWLDatatype getDatatype(OWLDataProperty dp) {
		return null;
	}

	@Override
	public void setSynchronized() {

	}
}
