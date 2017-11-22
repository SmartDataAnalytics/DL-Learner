/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.core;

import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.accuracymethods.AccMethodFMeasure;
import org.dllearner.accuracymethods.AccMethodPredAcc;
import org.dllearner.accuracymethods.AccMethodTwoValued;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.ReasoningUtils;
import org.dllearner.utilities.datastructures.DescriptionSubsumptionTree;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Abstract superclass of all class expression learning algorithm implementations.
 * Includes support for anytime learning algorithms and resumable
 * learning algorithms. Provides methods for filtering the best
 * descriptions found by the algorithm. As results of the algorithm,
 * you can either get only descriptions or evaluated descriptions.
 * Evaluated descriptions have information about accuracy and
 * example coverage associated with them. However, retrieving those
 * may require addition reasoner queries, because the learning
 * algorithms usually use but do not necessarily store this information.
 * 
 * Changes (March/April 2011): Learning algorithms no longer have to use
 * this class, but it still serves as a prototypical template for class
 * expression learning algorithms.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class AbstractCELA extends AbstractComponent implements ClassExpressionLearningAlgorithm, StoppableLearningAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractCELA.class);

	@NoConfigOption
	protected OWLObjectRenderer renderer = StringRenderer.getRenderer();
	
	protected EvaluatedDescriptionSet bestEvaluatedDescriptions = new EvaluatedDescriptionSet(AbstractCELA.MAX_NR_OF_RESULTS);
	protected DecimalFormat dfPercent = new DecimalFormat("0.00%");
	protected String baseURI;
	protected Map<String, String> prefixes;
	protected OWLDataFactory dataFactory = new OWLDataFactoryImpl();
	
	protected static final OWLClass OWL_THING = new OWLClassImpl(OWLRDFVocabulary.OWL_THING.getIRI());
	protected static final OWLClass OWL_NOTHING = new OWLClassImpl(OWLRDFVocabulary.OWL_NOTHING.getIRI());
	
	protected long nanoStartTime;
	protected boolean isRunning = false;
	protected boolean stop = false;
	
	protected OWLClassExpressionMinimizer minimizer;
	
	@ConfigOption(defaultValue="true", description="Specifies whether returned expressions should " +
			"be minimised by removing those parts, which are not needed. (Basically the minimiser tries to find the " +
			"shortest expression which is equivalent to the learned expression). Turning this feature off may improve " +
			"performance.")
	private boolean useMinimizer = true;
	
	@ConfigOption(defaultValue = "10", description = "maximum execution of the algorithm in seconds")
	protected long maxExecutionTimeInSeconds = 10;

	/**
	 * The learning problem variable, which must be used by
	 * all learning algorithm implementations.
	 */
	@ConfigOption(description="The Learning Problem variable to use in this algorithm")
	protected AbstractClassExpressionLearningProblem<? extends Score> learningProblem;
	
	/**
	 * The reasoning service variable, which must be used by
	 * all learning algorithm implementations.
	 */
	@ConfigOption(description="The reasoner variable to use for this learning problem")
	protected AbstractReasonerComponent reasoner;

	protected OWLObjectDuplicator duplicator = new OWLObjectDuplicator(new OWLDataFactoryImpl());
	
	@ConfigOption(description="List of classes that are allowed")
	protected Set<OWLClass> allowedConcepts = null;
	@ConfigOption(description="List of classes to ignore")
	protected Set<OWLClass> ignoredConcepts = null;
	@ConfigOption(description="List of object properties to allow")
	protected Set<OWLObjectProperty> allowedObjectProperties = null;
	@ConfigOption(description="List of object properties to ignore")
	protected Set<OWLObjectProperty> ignoredObjectProperties = null;
	@ConfigOption(description="List of data properties to allow")
	protected Set<OWLDataProperty> allowedDataProperties = null;
	@ConfigOption(description="List of data properties to ignore")
	protected Set<OWLDataProperty> ignoredDataProperties = null;

    /**
     * Default Constructor
     */
    public AbstractCELA(){}
    
	/**
	 * Each learning algorithm gets a learning problem and
	 * a reasoner as input.
	 * @param learningProblem The learning problem to solve.
	 * @param reasoningService The reasoner connecting to the
	 * underlying knowledge base.
	 */
	public AbstractCELA(AbstractClassExpressionLearningProblem learningProblem, AbstractReasonerComponent reasoningService) {
		this.learningProblem = learningProblem;
		this.reasoner = reasoningService;
	}
	
	/**
	 * Call this when you want to change the learning problem, but
	 * leave everything else as is. Method can be used to apply
	 * a configured algorithm to different learning problems.
	 * Implementations, which do not only use the provided learning
	 * algorithm variable, must make sure that a call to this method
	 * indeed changes the learning problem.
	 * @param learningProblem The new learning problem.
	 */
	public void changeLearningProblem(AbstractClassExpressionLearningProblem learningProblem) {
		this.learningProblem = learningProblem;
	}

	/**
	 * Call this when you want to change the reasoning service, but
	 * leave everything else as is. Method can be used to use
	 * a configured algorithm with different reasoners.
	 * Implementations, which do not only use the provided reasoning
	 * service class variable, must make sure that a call to this method
	 * indeed changes the reasoning service.
	 * @param reasoningService The new reasoning service.
	 */
	public void changeReasonerComponent(AbstractReasonerComponent reasoningService) {
		this.reasoner = reasoningService;
	}
	
	/**
	 * This is the maximum number of results, which the learning
	 * algorithms are asked to store. (Note, that algorithms are not
	 * required to store any results except the best one, so this limit
	 * is used to limit the performance cost for those which
	 * choose to store results.)
	 */
	public static final int MAX_NR_OF_RESULTS = 100;

	/**
	 * @see #getCurrentlyBestEvaluatedDescription()
	 * @return The best class OWLClassExpression found by the learning algorithm so far.
	 */
	public OWLClassExpression getCurrentlyBestDescription() {
		return getCurrentlyBestEvaluatedDescription().getDescription();
	}
	
	/**
	 * @see #getCurrentlyBestEvaluatedDescriptions()
	 * @return The best class descriptions found by the learning algorithm so far.
	 */
	public List<OWLClassExpression> getCurrentlyBestDescriptions() {
		return bestEvaluatedDescriptions.toDescriptionList();
	}
	
	/**
	 * @see #getCurrentlyBestEvaluatedDescriptions(int)
	 * @param nrOfDescriptions Limit for the number or returned descriptions.
	 * @return The best class descriptions found by the learning algorithm so far.
	 */
	@Override
	public synchronized List<OWLClassExpression> getCurrentlyBestDescriptions(int nrOfDescriptions) {
		return getCurrentlyBestDescriptions(nrOfDescriptions, false);
	}
	
	/**
	 * @see #getCurrentlyBestEvaluatedDescriptions(int,double,boolean)
	 * @param nrOfDescriptions Limit for the number or returned descriptions.
	 * @param filterNonMinimalDescriptions Remove non-minimal descriptions (e.g. those which can be shortened
	 * to an equivalent concept) from the returned set.
	 * @return The best class descriptions found by the learning algorithm so far.
	 */
	public synchronized List<OWLClassExpression> getCurrentlyBestDescriptions(int nrOfDescriptions, boolean filterNonMinimalDescriptions) {
		List<OWLClassExpression> currentlyBest = getCurrentlyBestDescriptions();
		List<OWLClassExpression> returnList = new LinkedList<>();
		for(OWLClassExpression ed : currentlyBest) {
			if(returnList.size() >= nrOfDescriptions) {
				return returnList;
			}
			
			if(!filterNonMinimalDescriptions || ConceptTransformation.isDescriptionMinimal(ed)) {
				returnList.add(ed);
			}
			
		}
		return returnList;
	}
	
	/**
	 * Returns the best descriptions obtained so far.
	 * @return Best class class expression found so far.
	 */
	public EvaluatedDescription<? extends Score> getCurrentlyBestEvaluatedDescription() {
		return bestEvaluatedDescriptions.getSet().last();
	}
	
	/**
	 * Returns a sorted set of the best descriptions found so far. We
	 * assume that they are ordered such that the best ones come in
	 * last. (In Java, iterators traverse a SortedSet in ascending order.)
	 * @return Best class descriptions found so far.
	 */
	public NavigableSet<? extends EvaluatedDescription<? extends Score>> getCurrentlyBestEvaluatedDescriptions() {
		return bestEvaluatedDescriptions.getSet();
	}
	
	/**
	 * Returns a filtered list of currently best class descriptions.
	 * 
	 * @param nrOfDescriptions Maximum number of restrictions. Use Integer.MAX_VALUE
	 * if you do not want this filter to be active.
	 * 
	 * @param accuracyThreshold Minimum accuracy. All class descriptions with lower
	 * accuracy are disregarded. Specify a value between 0.0 and 1.0. Use 0.0 if
	 * you do not want this filter to be active.
	 * 
	 * @param filterNonMinimalDescriptions If true, non-minimal descriptions are
	 * filtered, e.g. ALL r.TOP (being equivalent to TOP), male AND male (can be
	 * shortened to male). Currently, non-minimal descriptions are just skipped,
	 * i.e. they are completely omitted from the return list. Later, implementation
	 * might be changed to return shortened versions of those descriptions.
	 * 
	 * @return A list of currently best class descriptions.
	 */
	public synchronized List<? extends EvaluatedDescription<? extends Score>> getCurrentlyBestEvaluatedDescriptions(int nrOfDescriptions, double accuracyThreshold, boolean filterNonMinimalDescriptions) {
		NavigableSet<? extends EvaluatedDescription<? extends Score>> currentlyBest = getCurrentlyBestEvaluatedDescriptions();
		List<EvaluatedDescription<? extends Score>> returnList = new LinkedList<>();
		for(EvaluatedDescription<? extends Score> ed : currentlyBest.descendingSet()) {
			// once we hit a OWLClassExpression with a below threshold accuracy, we simply return
			// because learning algorithms are advised to order descriptions by accuracy,
			// so we won't find any concept with higher accuracy in the remaining list
			if(ed.getAccuracy() < accuracyThreshold) {
				return returnList;
			}

			// return if we have sufficiently many descriptions
			if(returnList.size() >= nrOfDescriptions) {
				return returnList;
			}
			
			if(!filterNonMinimalDescriptions || ConceptTransformation.isDescriptionMinimal(ed.getDescription())) {
				// before we add the OWLClassExpression we replace EXISTS r.TOP with
				// EXISTS r.range(r) if range(r) is atomic
				// (we need to clone, otherwise we change descriptions which could
				// be in the search of the learning algorithm, which leads to
				// unpredictable behaviour)
				OWLClassExpression d = duplicator.duplicateObject(ed.getDescription());
				
				//commented out because reasoner is called. leads in swing applications sometimes to exceptions
//				ConceptTransformation.replaceRange(d, reasoner);
				ed.setDescription(d);
				
				returnList.add(ed);
			}
			
		}
		return returnList;
	}
	
	/**
	 * Return the best currently found concepts up to some maximum
	 * count (no minimality filter used).
	 * @param nrOfDescriptions Maximum number of descriptions returned.
	 * @return Return value is getCurrentlyBestDescriptions(nrOfDescriptions, 0.0, false).
	 */
	@Override
	public synchronized List<? extends EvaluatedDescription<? extends Score>> getCurrentlyBestEvaluatedDescriptions(int nrOfDescriptions) {
		return getCurrentlyBestEvaluatedDescriptions(nrOfDescriptions, 0.0, false);
	}
	
	/**
	 * Returns a fraction of class descriptions with sufficiently high accuracy.
	 * @param accuracyThreshold Only return solutions with this accuracy or higher.
	 * @return Return value is getCurrentlyBestDescriptions(Integer.MAX_VALUE, accuracyThreshold, false).
	 */
	public synchronized  List<? extends EvaluatedDescription<? extends Score>> getCurrentlyBestEvaluatedDescriptions(double accuracyThreshold) {
		return getCurrentlyBestEvaluatedDescriptions(Integer.MAX_VALUE, accuracyThreshold, false);
	}
	
	public synchronized  List<? extends EvaluatedDescription<? extends Score>> getCurrentlyBestMostGeneralEvaluatedDescriptions() {
		List<? extends EvaluatedDescription<? extends Score>> l = getCurrentlyBestEvaluatedDescriptions(getCurrentlyBestEvaluatedDescriptions().last().getAccuracy());
		DescriptionSubsumptionTree t = new DescriptionSubsumptionTree(reasoner);
		t.insert(l);
		return t.getMostGeneralDescriptions(true);
	}
		
	/**
	 * Returns all learning problems supported by this component. This can be used to indicate that, e.g.
	 * an algorithm is only suitable for positive only learning.
	 * @return All classes implementing learning problems, which are supported by this learning algorithm.
	 */
	public static Collection<Class<? extends AbstractClassExpressionLearningProblem>> supportedLearningProblems() {
		return new LinkedList<>();
	}
	
	// central function for printing description
	protected String descriptionToString(OWLClassExpression description) {
		return renderer.render(description);
	}
		
	
	protected String getSolutionString() {
		int current = 1;
		String str = "";
		for(EvaluatedDescription<? extends Score> ed : getCurrentlyBestEvaluatedDescriptions().descendingSet()) {
			// temporary code
			OWLClassExpression description = ed.getDescription();
			String descriptionString = descriptionToString(description);
			if(learningProblem instanceof PosNegLP) {
				Set<OWLIndividual> positiveExamples = ((PosNegLP)learningProblem).getPositiveExamples();
				Set<OWLIndividual> negativeExamples = ((PosNegLP)learningProblem).getNegativeExamples();
				ReasoningUtils reasoningUtil = learningProblem.getReasoningUtil();
				
				str += current + ": " + descriptionString + " (pred. acc.: "
						+ dfPercent.format(reasoningUtil.getAccuracyOrTooWeak2(new AccMethodPredAcc(true), description, positiveExamples, negativeExamples, 1))
						+ ", F-measure: "+ dfPercent.format(reasoningUtil.getAccuracyOrTooWeak2(new AccMethodFMeasure(true), description, positiveExamples, negativeExamples, 1));

				AccMethodTwoValued accuracyMethod = ((PosNegLP)learningProblem).getAccuracyMethod();
				if ( !(accuracyMethod instanceof AccMethodPredAcc)
						&& !(accuracyMethod instanceof AccMethodFMeasure) ) {
					str += ", " + AnnComponentManager.getName(accuracyMethod) + ": " + dfPercent.format(ed.getAccuracy());
				}
				str += ")\n";
			} else {
				str += current + ": " + descriptionString + " " + dfPercent.format(ed.getAccuracy()) + "\n";
//				System.out.println(ed);
			}
			current++;
		}
		return str;
	}
	
	/**
	 * Computes an internal class hierarchy that only contains classes
	 * that are allowed.
	 * @return optimized class hierarchy
	 */
	protected ClassHierarchy initClassHierarchy() {
		// we ignore all unsatisfiable classes
		Set<OWLClass> unsatisfiableClasses = null;
		try {
		unsatisfiableClasses = reasoner.getInconsistentClasses();
		} catch (UnsupportedOperationException e) {
			logger.warn("Ignoring unsatisfiable check due to "+e.getStackTrace()[0]);
		}
		if(unsatisfiableClasses != null && !unsatisfiableClasses.isEmpty()) {
			logger.warn("Ignoring unsatisfiable classes " + unsatisfiableClasses);
			if(ignoredConcepts == null) {
				ignoredConcepts = unsatisfiableClasses;
			} else {
				ignoredConcepts.addAll(unsatisfiableClasses);
			}
		}
		
		
		Set<OWLClass> usedConcepts;
		if(allowedConcepts != null) {
			// sanity check to control if no non-existing concepts are in the list
			Helper.checkConcepts(reasoner, allowedConcepts);
			usedConcepts = allowedConcepts;
		} else if(ignoredConcepts != null) {
			usedConcepts = Helper.computeConceptsUsingIgnoreList(reasoner, ignoredConcepts);
		} else {
			usedConcepts = Helper.computeConcepts(reasoner);
		}
		
		ClassHierarchy hierarchy = (ClassHierarchy) reasoner.getClassHierarchy().cloneAndRestrict(new HashSet<OWLClassExpression>(usedConcepts));
//		hierarchy.thinOutSubsumptionHierarchy();
		return hierarchy;
	}
	
	/**
	 * Computes an internal object property hierarchy that only contains
	 * object properties that are allowed.
	 * @return optimized object property hierarchy
	 */
	protected ObjectPropertyHierarchy initObjectPropertyHierarchy() {
		Set<OWLObjectProperty> usedProperties;
		if(allowedObjectProperties != null) {
			// sanity check to control if no non-existing object properties are in the list
			Helper.checkRoles(reasoner, allowedObjectProperties);
			usedProperties = allowedObjectProperties;
		} else if(ignoredObjectProperties != null) {
			usedProperties = Helper.computeEntitiesUsingIgnoreList(reasoner, EntityType.OBJECT_PROPERTY, ignoredObjectProperties);
		} else {
			usedProperties = Helper.computeEntities(reasoner, EntityType.OBJECT_PROPERTY);
		}
		
		ObjectPropertyHierarchy hierarchy = (ObjectPropertyHierarchy) reasoner.getObjectPropertyHierarchy().cloneAndRestrict(usedProperties);
//		hierarchy.thinOutSubsumptionHierarchy();
		
		return hierarchy;
	}
	
	/**
	 * Computes an internal data property hierarchy that only contains
	 * data properties that are allowed.
	 * @return optimized data property hierarchy
	 */
	protected DatatypePropertyHierarchy initDataPropertyHierarchy() {
		Set<OWLDataProperty> usedProperties;
		if(allowedDataProperties != null) {
			// sanity check to control if no non-existing data properties are in the list
			Helper.checkEntities(reasoner, allowedDataProperties);
			usedProperties = allowedDataProperties;
		} else if(ignoredDataProperties != null) {
			usedProperties = Helper.computeEntitiesUsingIgnoreList(reasoner, EntityType.DATA_PROPERTY, ignoredDataProperties);
		} else {
			usedProperties = Helper.computeEntities(reasoner, EntityType.DATA_PROPERTY);
		}

		DatatypePropertyHierarchy hierarchy = (DatatypePropertyHierarchy) reasoner.getDatatypePropertyHierarchy().cloneAndRestrict(usedProperties);
//		hierarchy.thinOutSubsumptionHierarchy();
		return hierarchy;
	}
	
	protected boolean isTimeExpired() {
		return getCurrentRuntimeInMilliSeconds() >= TimeUnit.SECONDS.toMillis(maxExecutionTimeInSeconds);
	}

	protected long getCurrentRuntimeInMilliSeconds() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nanoStartTime);
	}

	protected long getRemainingRuntimeInMilliseconds() {
		return Math.max(0, TimeUnit.SECONDS.toMillis(maxExecutionTimeInSeconds) - getCurrentRuntimeInMilliSeconds());
	}
	
	protected String getDurationAsString(long durationInMillis) {
		PeriodFormatter formatter = new PeriodFormatterBuilder()
	     .appendDays().appendSuffix("d")
	     .appendHours().appendSuffix("h")
	     .appendMinutes().appendSuffix("m")
	     .appendSeconds().appendSuffix("s")
	     .appendMillis().appendSuffix("ms")
	     .printZeroNever()
	     .toFormatter();
		
		return formatter.print(new Period(durationInMillis));
	}
	
	protected OWLClassExpression rewrite(OWLClassExpression ce) {
		// minimize class expression (expensive!) - also performs some human friendly rewrites
		OWLClassExpression niceDescription;
		if (useMinimizer) {
			niceDescription = minimizer.minimizeClone(ce);
		} else {
			niceDescription = ce;
		}
		
		// replace \exists r.\top with \exists r.range(r) which is easier to read for humans
		niceDescription = ConceptTransformation.replaceRange(niceDescription, reasoner);
		
		niceDescription = ConceptTransformation.appendSomeValuesFrom(niceDescription);
		
		return niceDescription;
	}

    /**
     * The learning problem variable, which must be used by
     * all learning algorithm implementations.
     */
	@Override
    public AbstractClassExpressionLearningProblem<? extends Score> getLearningProblem() {
        return learningProblem;
    }

    @Autowired
    @Override
    public void setLearningProblem(LearningProblem learningProblem) {
        this.learningProblem = (AbstractClassExpressionLearningProblem<? extends Score>)learningProblem;
    }

    /**
     * The reasoning service variable, which must be used by
     * all learning algorithm implementations.
     */
    public AbstractReasonerComponent getReasoner() {
        return reasoner;
    }

    @Autowired
    public void setReasoner(AbstractReasonerComponent reasoner) {
        this.reasoner = reasoner;
        
        baseURI = reasoner.getBaseURI();
		prefixes = reasoner.getPrefixes();
    }
    
    @Override
	public void stop() {
		stop = true;
	}
	
	@Override
	public boolean isRunning() {
		return isRunning;
	}
	
	public Set<OWLClass> getAllowedConcepts() {
		return allowedConcepts;
	}

	public void setAllowedConcepts(Set<OWLClass> allowedConcepts) {
		this.allowedConcepts = allowedConcepts;
	}

	public Set<OWLClass> getIgnoredConcepts() {
		return ignoredConcepts;
	}

	public void setIgnoredConcepts(Set<OWLClass> ignoredConcepts) {
		this.ignoredConcepts = ignoredConcepts;
	}
	
	/**
	 * @param allowedObjectProperties the allowed object properties to set
	 */
	public void setAllowedObjectProperties(Set<OWLObjectProperty> allowedObjectProperties) {
		this.allowedObjectProperties = allowedObjectProperties;
	}
	
	/**
	 * @return the allowed object properties
	 */
	public Set<OWLObjectProperty> getAllowedObjectProperties() {
		return allowedObjectProperties;
	}
	
	/**
	 * @param ignoredObjectProperties the ignored object properties to set
	 */
	public void setIgnoredObjectProperties(Set<OWLObjectProperty> ignoredObjectProperties) {
		this.ignoredObjectProperties = ignoredObjectProperties;
	}
	
	/**
	 * @return the ignored object properties
	 */
	public Set<OWLObjectProperty> getIgnoredObjectProperties() {
		return ignoredObjectProperties;
	}
	
	/**
	 * @param allowedDataProperties the allowed data properties to set
	 */
	public void setAllowedDataProperties(Set<OWLDataProperty> allowedDataProperties) {
		this.allowedDataProperties = allowedDataProperties;
	}
	
	/**
	 * @return the allowed data properties
	 */
	public Set<OWLDataProperty> getAllowedDataProperties() {
		return allowedDataProperties;
	}
	
	/**
	 * @param ignoredDataProperties the ignored data properties to set
	 */
	public void setIgnoredDataProperties(Set<OWLDataProperty> ignoredDataProperties) {
		this.ignoredDataProperties = ignoredDataProperties;
	}
	
	/**
	 * @return the ignored data properties
	 */
	public Set<OWLDataProperty> getIgnoredDataProperties() {
		return ignoredDataProperties;
	}
	
	public boolean isUseMinimizer() {
		return useMinimizer;
	}

	public void setUseMinimizer(boolean useMinimizer) {
		this.useMinimizer = useMinimizer;
	}
	
	public long getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	/**
	 * Set the max. execution time in seconds of the algorithm.  It's expected that the
	 * algorithm will terminate gracefully.
	 *
	 * @param maxExecutionTimeInSeconds max. execution time in seconds
	 */
	public void setMaxExecutionTimeInSeconds(long maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	/**
	 * Set the max. execution time of the algorithm. It's expected that the
	 * algorithm will terminate gracefully.
	 *
	 * @param maxExecutionTime max. execution time
	 * @param timeUnit the time unit
	 */
	public void setMaxExecutionTime(long maxExecutionTime, TimeUnit timeUnit) {
		this.maxExecutionTimeInSeconds = timeUnit.toSeconds(maxExecutionTime);
	}
	
	/**
	 * @param renderer the renderer of OWL objects to set
	 */
	public void setRenderer(OWLObjectRenderer renderer) {
		this.renderer = renderer;
	}
    
    /**
	 * The goal of this method is to rewrite the class expression CE, to get a more informative one by e.g.
	 * <ul><li>replacing the role fillers in CE with the range of the property, if exists.</li></ul>
	 * @param ce the class expression
	 * @return the modified class expression
	 */
	protected OWLClassExpression getNiceDescription(OWLClassExpression ce){
		OWLClassExpression rewrittenClassExpression = ce;
		if(ce instanceof OWLObjectIntersectionOf){
			Set<OWLClassExpression> newOperands = new TreeSet<>(((OWLObjectIntersectionOf) ce).getOperands());
			for (OWLClassExpression operand : ((OWLObjectIntersectionOf) ce).getOperands()) {
				newOperands.add(getNiceDescription(operand));
			}
			rewrittenClassExpression = dataFactory.getOWLObjectIntersectionOf(newOperands);
		} else if(ce instanceof OWLObjectSomeValuesFrom) {
			// \exists r.\bot \equiv \bot
			OWLObjectProperty property = ((OWLObjectSomeValuesFrom) ce).getProperty().asOWLObjectProperty();
			OWLClassExpression filler = ((OWLObjectSomeValuesFrom) ce).getFiller();
			if(filler.isOWLThing()) {
				OWLClassExpression range = reasoner.getRange(property);
				filler = range;
			} else if(filler.isAnonymous()){
				filler = getNiceDescription(filler);
			}
			rewrittenClassExpression = dataFactory.getOWLObjectSomeValuesFrom(property, filler);
		}
		return rewrittenClassExpression;
	}
}