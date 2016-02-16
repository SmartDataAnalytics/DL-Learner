package org.dllearner.algorithms.ParCEL.split;

import org.dllearner.algorithms.ParCEL.ParCELOntologyUtil;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;

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
	private Set<OWLIndividual> positiveExamples = null;
	private Set<OWLIndividual> negativeExamples = null;

	private KnowledgeSource knowledgeSource = null;
	private OWLOntology ontology = null;

	Set<OWLDataProperty> doubleDatatypeProperties;

	private OWLDataFactory df = new OWLDataFactoryImpl();

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
			Set<OWLIndividual> positiveExamples, Set<OWLIndividual> negativeExamples) {
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
		this.doubleDatatypeProperties = reasoner.getDoubleDatatypeProperties();

	}

	/**
	 * ============================================================================================
	 * Compute splits for all data properties in the ontology
	 * 
	 * @return a map of datatype properties and their splitting values
	 */
	public Map<OWLDataProperty, List<OWLLiteral>> computeSplits() {
		// -------------------------------------------------
		// generate relations for positive examples
		// -------------------------------------------------

		Map<OWLDataPropertyExpression, ValuesSet> relations = new HashMap<>();

		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();

		for (OWLIndividual ind : positiveExamples) {
			Map<OWLDataPropertyExpression, ValuesSet> individualRelations = getInstanceValueRelation(
					ind, true, null);

			for (OWLDataPropertyExpression pro : individualRelations.keySet()) {
				if (relations.keySet().contains(pro))
					relations.get(pro).addAll(individualRelations.get(pro));
				else
					relations.put(pro, individualRelations.get(pro));
			}
		}

		// generate relation for negative examples
		for (OWLIndividual ind : negativeExamples) {
			Map<OWLDataPropertyExpression, ValuesSet> individualRelations = getInstanceValueRelation(
					ind, false, null);

			for (OWLDataPropertyExpression pro : individualRelations.keySet()) {
				if (relations.keySet().contains(pro))
					relations.get(pro).addAll(individualRelations.get(pro));
				else
					relations.put(pro, individualRelations.get(pro));
			}
		}

		// -------------------------------------------------
		// calculate the splits for each data property
		// -------------------------------------------------

		Map<OWLDataProperty, List<OWLLiteral>> splits = new TreeMap<>();

		// - - - . + + + + + . = . = . = . + . = . = . - . = . - - -
		for (OWLDataPropertyExpression dp : relations.keySet()) {

			if (relations.get(dp).size() > 0) {
				List<OWLLiteral> values = new ArrayList<>();
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
						values.add(df.getOWLLiteral((priorValue + currentValue) / 2.0));

					// update the prior type and value after process the current element
					priorType = currentValueCount.getType();
					priorValue = currentValueCount.getValue();

				}

				// add processed property into the result set (splits)
				splits.put(dp.asOWLDataProperty(), values);
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
	private Map<OWLDataPropertyExpression, ValuesSet> getInstanceValueRelation(OWLIndividual individual,
			boolean positiveExample, Set<OWLIndividual> visitedIndividuals) {

		if (visitedIndividuals == null)
			visitedIndividuals = new HashSet<OWLIndividual>();

		// if the individual visited
		if (visitedIndividuals.contains(individual))
			return null;
		else
			visitedIndividuals.add(individual);

		Map<OWLDataPropertyExpression, ValuesSet> relations = new HashMap<>();

		// get all data property values of the given individual
		Map<OWLDataPropertyExpression, Collection<OWLLiteral>> dataPropertyValues = EntitySearcher.getDataPropertyValues(
				individual, this.ontology).asMap();

		// get all object properties value of the given individual
		Map<OWLObjectPropertyExpression, Collection<OWLIndividual>> objectPropertyValues = EntitySearcher.getObjectPropertyValues(
				individual, this.ontology).asMap();

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
				Map<OWLDataPropertyExpression, ValuesSet> subRelations = getInstanceValueRelation(ind,
						positiveExample, visitedIndividuals);

				// sub-relation == null if the ind had been visited
				if (subRelations != null) {
					for (OWLDataPropertyExpression dp : subRelations.keySet()) {
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
