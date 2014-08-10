package org.dllearner.algorithms.ParCEL.split;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.dllearner.algorithms.ParCEL.ParCELOntologyUtil;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLFile;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * This class implements a splitting strategy for datatype property In this strategy, the splitted
 * value will be generate in a manner such that there exist no range which contains both positive
 * and negative examples to avoid the learn get stuck in its specialisation
 * 
 * @author An C. Tran
 * 
 */

@ComponentAnn(name = "ParCEL double splitter v1", shortName = "parcelSplitterV1", version = 0.08)
public class ParCELDoubleSplitterV1 implements ParCELDoubleSplitterAbstract {

	private AbstractReasonerComponent reasoner = null;
	private Set<Individual> positiveExamples = null;
	private Set<Individual> negativeExamples = null;

	private KnowledgeSource knowledgeSource = null;
	private OWLOntology ontology = null;

	Set<OWLDataPropertyExpression> doubleDatatypeProperties;

	public ParCELDoubleSplitterV1() {

	}

	/**
	 * ============================================================================================
	 * Create a Splitter given a reasoner, positive and negative examples
	 * 
	 * @param reasoner
	 * @param positiveExamples
	 * @param negativeExamples
	 */
	public ParCELDoubleSplitterV1(AbstractReasonerComponent reasoner,
			Set<Individual> positiveExamples, Set<Individual> negativeExamples) {
		this.reasoner = reasoner;

		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
	}

	/**
	 * ============================================================================================
	 * Initialise the Splitter
	 * 
	 * @throws ComponentInitException
	 * @throws OWLOntologyCreationException
	 */
	public void init() throws ComponentInitException {
		if (this.reasoner == null)
			throw new ComponentInitException("There is no reasoner for initialising the Splitter");

		if (this.positiveExamples == null)
			throw new ComponentInitException(
					"There is no positive examples for initialising the Splitter");

		if (this.negativeExamples == null)
			throw new ComponentInitException(
					"There is no negative examples for initialising the Splitter");

		// get knowledge source (OWL file to built abox dependency graph
		this.knowledgeSource = reasoner.getSources().iterator().next();

		String ontologyPath = ((OWLFile) knowledgeSource).getBaseDir()
				+ ((OWLFile) knowledgeSource).getFileName();

		try {
			ontology = ParCELOntologyUtil.loadOntology(ontologyPath);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

		if (!(knowledgeSource instanceof OWLFile))
			throw new RuntimeException("Only OWLFile is supported");

		// get a list of double data type properties for filtering out other properties
		this.doubleDatatypeProperties = new HashSet<OWLDataPropertyExpression>();
		for (OWLDataProperty dp : reasoner.getDoubleDatatypeProperties())
			this.doubleDatatypeProperties.add(OWLAPIConverter.getOWLAPIDataProperty(dp));

	}

	/**
	 * ============================================================================================
	 * Compute splits for all data properties in the ontology
	 * 
	 * @return a map of datatype properties and their splitting values
	 */
	public Map<OWLDataProperty, List<Double>> computeSplits() {
		// -------------------------------------------------
		// generate relations for positive examples
		// -------------------------------------------------

		Map<OWLDataProperty, ValuesSet> relations = new HashMap<OWLDataProperty, ValuesSet>();

		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();

		for (Individual ind : positiveExamples) {
			Map<OWLDataProperty, ValuesSet> individualRelations = getInstanceValueRelation(
					factory.getOWLNamedIndividual(IRI.create(ind.getURI())), true, null);

			for (OWLDataProperty pro : individualRelations.keySet()) {
				if (relations.keySet().contains(pro))
					relations.get(pro).addAll(individualRelations.get(pro));
				else
					relations.put(pro, individualRelations.get(pro));
			}
		}

		// generate relation for negative examples
		for (Individual ind : negativeExamples) {
			Map<OWLDataProperty, ValuesSet> individualRelations = getInstanceValueRelation(
					factory.getOWLNamedIndividual(IRI.create(ind.getURI())), false, null);

			for (OWLDataProperty pro : individualRelations.keySet()) {
				if (relations.keySet().contains(pro))
					relations.get(pro).addAll(individualRelations.get(pro));
				else
					relations.put(pro, individualRelations.get(pro));
			}
		}

		// -------------------------------------------------
		// calculate the splits for each data property
		// -------------------------------------------------

		Map<OWLDataProperty, List<Double>> splits = new TreeMap<OWLDataProperty, List<Double>>();

		// - - - . + + + + + . = . = . = . + . = . = . - . = . - - -
		for (OWLDataProperty dp : relations.keySet()) {

			if (relations.get(dp).size() > 0) {
				List<Double> values = new ArrayList<Double>();
				ValuesSet propertyValues = relations.get(dp);

				int priorType = propertyValues.first().getType();
				double priorValue = propertyValues.first().getValue();

				Iterator<ValueCount> iterator = propertyValues.iterator();
				while (iterator.hasNext()) {
					ValueCount currentValueCount = iterator.next();
					int currentType = currentValueCount.getType();
					double currentValue = currentValueCount.getValue();

					// check if a new value should be generated: when the type changes or the
					// current value belongs to both pos. and neg.
					if ((currentType == 3) || (currentType != priorType))
						values.add((priorValue + currentValue) / 2.0);

					// update the prior type and value after process the current element
					priorType = currentValueCount.getType();
					priorValue = currentValueCount.getValue();

				}

				// add processed property into the result set (splits)
				splits.put(dp, values);
			}
		}

		return splits;
	}

	/**
	 * ============================================================================================
	 * Find the related values of an individual
	 * 
	 * @param individual
	 *            The individual need to be seek for the related values
	 * @param positiveExample
	 *            True if the given individual is a positive example and false otherwise
	 * @param visitedIndividuals
	 *            Set of individuals that had been visited when finding the related values for the
	 *            given individual
	 * 
	 * @return A map from data property to its related values that had been discovered from the
	 *         given individual
	 */
	private Map<OWLDataProperty, ValuesSet> getInstanceValueRelation(OWLIndividual individual,
			boolean positiveExample, Set<OWLIndividual> visitedIndividuals) {

		if (visitedIndividuals == null)
			visitedIndividuals = new HashSet<OWLIndividual>();

		// if the individual visited
		if (visitedIndividuals.contains(individual))
			return null;
		else
			visitedIndividuals.add(individual);

		Map<OWLDataProperty, ValuesSet> relations = new HashMap<OWLDataProperty, ValuesSet>();

		// get all data property values of the given individual
		Map<OWLDataPropertyExpression, Set<OWLLiteral>> dataPropertyValues = individual
				.getDataPropertyValues(this.ontology);

		// get all object properties value of the given individual
		Map<OWLObjectPropertyExpression, Set<OWLIndividual>> objectPropertyValues = individual
				.getObjectPropertyValues(this.ontology);

		// ---------------------------------------
		// process data properties
		// NOTE: filter the double data property
		// ---------------------------------------
		for (OWLDataPropertyExpression dp : dataPropertyValues.keySet()) {

			if (this.doubleDatatypeProperties.contains(dp)) {

				// process values of each data property: create a ValueCount object and add it into
				// the result
				ValuesSet values = new ValuesSet();
				for (OWLLiteral lit : dataPropertyValues.get(dp)) {
					ValueCount newValue = new ValueCount(Double.parseDouble(lit.getLiteral()),
							positiveExample); // (value, pos)
					values.add(newValue);
				}

				// if the data property exist, update its values
				if (relations.keySet().contains(dp))
					relations.get(df.getOWLDataProperty(IRI.create(dp.asOWLDataProperty().getIRI().toString()))
							.addAll(values);
				// otherwise, create a new map <data property - values and add it into the return
				// value
				else
					relations.put(df.getOWLDataProperty(IRI.create(dp.asOWLDataProperty().getIRI().toString()),
							values);
			}
		}

		// process each object property: call this method recursively
		for (OWLObjectPropertyExpression op : objectPropertyValues.keySet()) {
			for (OWLIndividual ind : objectPropertyValues.get(op)) {
				Map<OWLDataProperty, ValuesSet> subRelations = getInstanceValueRelation(ind,
						positiveExample, visitedIndividuals);

				// sub-relation == null if the ind had been visited
				if (subRelations != null) {
					for (OWLDataProperty dp : subRelations.keySet()) {
						// if the data property exist, update its values
						if (relations.keySet().contains(dp))
							relations.get(dp).addAll(subRelations.get(dp));
						// otherwise, create a new map <data property - values and add it into the
						// return value
						else
							relations.put(dp, subRelations.get(dp));
					}
				}

			}
		}

		return relations;
	}

	
	//-----------------------------
	// getters and setters
	//-----------------------------
	public AbstractReasonerComponent getReasoner() {
		return reasoner;
	}

	public void setReasoner(AbstractReasonerComponent reasoner) {
		this.reasoner = reasoner;
	}

	public Set<Individual> getPositiveExamples() {
		return positiveExamples;
	}

	public void setPositiveExamples(Set<Individual> positiveExamples) {
		this.positiveExamples = positiveExamples;
	}

	public Set<Individual> getNegativeExamples() {
		return negativeExamples;
	}

	public void setNegativeExamples(Set<Individual> negativeExamples) {
		this.negativeExamples = negativeExamples;
	}

}
