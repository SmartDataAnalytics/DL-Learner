package org.dllearner.algorithms.parcel.split;

import java.util.*;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.owlapi.model.*;

/**
 * This class implements a splitting strategy for datatype property. In this strategy, the splitted
 * value will be generate in a manner such that there is no range which contains both positive
 * and negative examples to avoid the learning get stuck in its specialisation
 * 
 * <br>
 * 
 * NOTE: values for a data property can be queried from the ontology. However, to find know that
 * 	a value "relates" to positive or negative examples, we need to build a dependence graph because
 * 	a value may not related directly with the value but it may be indirectly related through some 
 * 	object properties. 	
 * 
 * <br>
 * Procedure:
 * <ol>
 * 	<li>filter out a list of numeric datatype properties,</li>
 * 	<li>build a relation graph that connects each value with positive/negative examples related to it,</li>
 * 	<li>apply splitting strategy on the list of values (along with its positive.negative examples)</li>
 * </ol>
 * 
 * @author An C. Tran
 * 
 */

@ComponentAnn(name = "ParCEL double splitter v1", shortName = "parcelSplitterV1", version = 0)
public class ParCELDoubleSplitterV1 implements ParCELDoubleSplitterAbstract {

	private AbstractReasonerComponent reasoner = null;
	private Set<OWLIndividual> positiveExamples = null;
	private Set<OWLIndividual> negativeExamples = null;

	private Set<OWLDataPropertyExpression> numericDatatypeProperties;
	
	private final Logger logger = Logger.getLogger(this.getClass());

	public ParCELDoubleSplitterV1() {

	}

	/**
	 * Create a Splitter given a reasoner, positive and negative examples
	 * 
	 * @param reasoner A reasoner with ontology loaded before
	 * @param positiveExamples Set of positive examples
	 * @param negativeExamples Set of negative examples 
	 */
	public ParCELDoubleSplitterV1(AbstractReasonerComponent reasoner,
								  Set<OWLIndividual> positiveExamples,
								  Set<OWLIndividual> negativeExamples) {
		this.reasoner = reasoner;

		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
	}

	/**
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

		// get a list of double data type properties for filtering out other properties
		this.numericDatatypeProperties = new HashSet<>(reasoner.getDoubleDatatypeProperties());

		//logger.info("Numeric data properties: " + numericDatatypeProperties);

		//get a list of integer datatype properties
		this.numericDatatypeProperties.addAll(reasoner.getIntDatatypeProperties());
		
		
		if (logger.isInfoEnabled())
				logger.info("Splitter created: " + this.getClass().getSimpleName() + "...");
		
	}

	/**
	 * Compute splits for all double data properties in the ontology
	 * 
	 * @return A map of datatype properties and their splitting values
	 */
	public Map<OWLDataProperty, List<Double>> computeSplits() {
		// -------------------------------------------------
		// generate relations for positive examples
		// -------------------------------------------------

		Map<OWLDataProperty, ValueCountSet> relations = new HashMap<>();

		for (OWLIndividual ind : positiveExamples) {
			Map<OWLDataProperty, ValueCountSet> individualRelations = getInstanceValueRelation(ind, true, null);

			for (OWLDataProperty pro : individualRelations.keySet()) {
				if (relations.containsKey(pro))
					relations.get(pro).addAll(individualRelations.get(pro));
				else
					relations.put(pro, individualRelations.get(pro));
			}
		}

		// generate relation for negative examples
		for (OWLIndividual ind : negativeExamples) {
			Map<OWLDataProperty, ValueCountSet> individualRelations = getInstanceValueRelation(ind, false, null);

			for (OWLDataProperty pro : individualRelations.keySet()) {
				if (relations.containsKey(pro))
					relations.get(pro).addAll(individualRelations.get(pro));
				else
					relations.put(pro, individualRelations.get(pro));
			}
		}

		// -------------------------------------------------
		// calculate the splits for each data property
		// -------------------------------------------------

		Map<OWLDataProperty, List<Double>> splits = new TreeMap<>();

		// - - - . + + + + + . = . = . = . + . = . = . - . = . - - -
		for (OWLDataProperty dp : relations.keySet()) {

			if (relations.get(dp).size() > 0) {
				List<Double> values = new ArrayList<>();
				ValueCountSet propertyValues = relations.get(dp);

				int priorType = propertyValues.first().getType();
				double priorValue = propertyValues.first().getValue();

				for (ValueCount currentValueCount : propertyValues) {
					int currentType = currentValueCount.getType();
					double currentValue = currentValueCount.getValue();

					// check if a new value should be generated: when the type changes or the
					// current value belongs to both pos. and neg.
					if ((currentType == 3) || (currentType != priorType)) {
						//calculate the middle/avg. value
						//TODO: how to identify the splitting strategy here? For examples: time,... 
						values.add((priorValue + currentValue) / 2.0);

						//Double newValue = new Double(new TimeSplitter().calculateSplit((int)priorValue, (int)currentValue));
						//if (!values.contains(newValue))
						//	values.add(newValue);

						//values.add((priorValue + currentValue) / 2.0);
					}

					// update the prior type and value after process the current element
					priorType = currentValueCount.getType();
					priorValue = currentValueCount.getValue();

				}

				// add processed property into the result set (splits)
				splits.put(dp, values);
				
				if (logger.isInfoEnabled())
					logger.info("Splitting: " + dp + ", no of values: " + relations.get(dp).size()
							+ ", splits: " + values.size());
				
			}
		}
		
		if (logger.isInfoEnabled())
			logger.info("Splitting result: " + splits);

		return splits;
	}

	/**
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
	private Map<OWLDataProperty, ValueCountSet> getInstanceValueRelation(OWLIndividual individual,
                                                                         boolean positiveExample, Set<OWLIndividual> visitedIndividuals) {

		if (visitedIndividuals == null)
			visitedIndividuals = new HashSet<>();

		// if the individual visited
		if (visitedIndividuals.contains(individual))
			return null;
		else
			visitedIndividuals.add(individual);

		Map<OWLDataProperty, ValueCountSet> relations = new HashMap<>();

		// get all data property values of the given individual
		Map<OWLDataProperty, Set<OWLLiteral>> dataPropertyValues = reasoner.getDataPropertyRelationships(individual);

		// get all object properties value of the given individual
		Map<OWLObjectProperty, Set<OWLIndividual>> objectPropertyValues = reasoner.getObjectPropertyRelationships(individual);

		// ---------------------------------------
		// process data properties
		// NOTE: filter the double data property
		// ---------------------------------------
		for (OWLDataProperty dp : dataPropertyValues.keySet()) {

			if (this.numericDatatypeProperties.contains(dp)) {

				// process values of each data property: create a ValueCount object and add it into
				// the result
				ValueCountSet values = new ValueCountSet();
				for (OWLLiteral lit : dataPropertyValues.get(dp)) {
					ValueCount newValue = new ValueCount(Double.parseDouble(lit.getLiteral()),
							positiveExample); // (value, pos)
					values.add(newValue);
				}

				// if the data property exist, update its values
				if (relations.containsKey(dp))
					relations.get(dp).addAll(values);
				// otherwise, create a new map <data property - values and add it into the return
				// value
				else
					relations.put(dp, values);
			}
		}

		// process each object property: call this method recursively
		for (OWLObjectPropertyExpression op : objectPropertyValues.keySet()) {
			for (OWLIndividual ind : objectPropertyValues.get(op)) {
				Map<OWLDataProperty, ValueCountSet> subRelations = getInstanceValueRelation(ind,
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

	public Set<OWLIndividual> getPositiveExamples() {
		return positiveExamples;
	}

	public void setPositiveExamples(Set<OWLIndividual> positiveExamples) {
		this.positiveExamples = positiveExamples;
	}

	public Set<OWLIndividual> getNegativeExamples() {
		return negativeExamples;
	}

	public void setNegativeExamples(Set<OWLIndividual> negativeExamples) {
		this.negativeExamples = negativeExamples;
	}

}
