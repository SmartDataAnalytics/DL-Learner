package org.dllearner.reasoning;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;

/**
 * The Reasoner component that implements a virtual OWL-Time.
 * 
 * TODO: Which kinds of date properties to support? The date-time stamp? has-month, has-day, ... style properies?
 */
@ComponentAnn(description = "OWL-Time reasoner", name = "OWL-Time Reasoner", shortName = "owltimere", version = 0.1)
abstract class OWLTimeReasoner extends AbstractReasonerComponent implements TemporalOWLReasoner {
	protected List<OWLProperty> dateTimePropertyPath;
	protected OWLClassExpression timeIntervallClassExpression;
	protected OWLClassExpression timeInstantClassExpression;
	
	@Override
	public void init() throws ComponentInitException {
		if (dateTimePropertyPath == null)
			throw new RuntimeException("A property path to the actual "
					+ "date time information must be set!");
		else if ((timeIntervallClassExpression == null) && (timeInstantClassExpression == null))
			throw new RuntimeException("A class containing all time intervall or "
					+ "all time instance individuals must be set!");
	}

	@Override
	public Set<OWLClass> getClasses() {
		// FIXME
		throw new RuntimeException("Not implemented,yet");
	}

	@Override
	public SortedSet<OWLIndividual> getIndividuals() {
		// FIXME
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public ReasonerType getReasonerType() {
		// FIXME
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public void releaseKB() {
		// FIXME
		throw new RuntimeException("Not implemented, yet");
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
		// FIXME
		throw new RuntimeException("Not implemented, yet");
	}

	@Override
	public void setSynchronized() {

	}

	public List<OWLProperty> getDateTimePropertyPath() {
		return dateTimePropertyPath;
	}

	public void setDateTimePropertyPath(List<OWLProperty> dateTimePropertyPath) {
		this.dateTimePropertyPath = dateTimePropertyPath;
	}
	
	public void setDateTimePropertyPath(OWLDataProperty dateTimeProperty) {
		List<OWLProperty> dateTimePropertyPath = Lists.newArrayList(dateTimeProperty);
		this.dateTimePropertyPath = dateTimePropertyPath;
	}

	public OWLClassExpression getTimeIntervallClassExpression() {
		return timeIntervallClassExpression;
	}

	public void setTimeIntervallClassExpression(OWLClassExpression timeIntervallClassExpression) {
		this.timeIntervallClassExpression = timeIntervallClassExpression;
	}

	public OWLClassExpression getTimeInstantClassExpression() {
		return timeInstantClassExpression;
	}

	public void setTimeInstantClassExpression(OWLClassExpression timeInstantClassExpression) {
		this.timeInstantClassExpression = timeInstantClassExpression;
	}
	
	
}
