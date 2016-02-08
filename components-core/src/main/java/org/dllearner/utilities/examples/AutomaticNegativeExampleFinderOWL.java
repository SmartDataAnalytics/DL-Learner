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
package org.dllearner.utilities.examples;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class AutomaticNegativeExampleFinderOWL {

	// LOGGER
	private static Logger logger = Logger.getLogger(AutomaticNegativeExampleFinderOWL.class);

	private AbstractReasonerComponent reasoningService;
	
	private SortedSet<OWLIndividual> fullPositiveSet;

	private SortedSet<OWLIndividual> fromRelated  = new TreeSet<>();
	private SortedSet<OWLIndividual> fromSuperclasses = new TreeSet<>();
	private SortedSet<OWLIndividual> fromParallelClasses = new TreeSet<>();
	private SortedSet<OWLIndividual> fromAllOther = new TreeSet<>();
	private SortedSet<OWLIndividual> fromDomain = new TreeSet<>();
	private SortedSet<OWLIndividual> fromRange = new TreeSet<>();
	
	static int poslimit = 10;
	static int neglimit = 20;


	/**
	 * 
	 * takes as input a full positive set to make sure no negatives are added as positives
	 * 
	 * @param fullPositiveSet
	 * @param reasoningService
	 */
	public AutomaticNegativeExampleFinderOWL(
			SortedSet<OWLIndividual> fullPositiveSet,
			AbstractReasonerComponent reasoningService) {
		super();
		this.fullPositiveSet = new TreeSet<>();
		this.fullPositiveSet.addAll(fullPositiveSet);
		this.reasoningService = reasoningService;

	}
	
	/**
	 * see <code>  getNegativeExamples(int neglimit, boolean stable )</code>
	 * @param neglimit
	 */
	public SortedSet<OWLIndividual> getNegativeExamples(int neglimit, boolean forceNegLimit ) {
		return getNegativeExamples(neglimit, false, forceNegLimit);
	}

	/**
	 * aggregates all collected neg examples
	 * CAVE: it is necessary to call one of the make functions before calling this
	 * OTHERWISE it will choose random examples
	 * 
	 * @param neglimit size of negative Example set, 0 means all, which can be quite large
	 * @param stable decides whether neg Examples are randomly picked, default false, faster for developing, since the cache can be used
	 * @param forceNegLimit forces that exactly neglimit instances are returned by adding more instances
	 */
	public SortedSet<OWLIndividual> getNegativeExamples(int neglimit, boolean stable, boolean forceNegLimit ) {
		SortedSet<OWLIndividual> negatives = new TreeSet<>();
		negatives.addAll(fromParallelClasses);
		negatives.addAll(fromRelated);
		negatives.addAll(fromSuperclasses);
		if(negatives.size()< neglimit){
			makeNegativeExamplesFromAllOtherInstances();
			
			negatives.addAll(SetManipulation.stableShrinkInd(fromAllOther, neglimit-negatives.size()));
		}
		
		
		
		if(neglimit<=0){
			logger.debug("neg Example size NO shrinking: " + negatives.size());
			return negatives;
		}
		
		
		logger.debug("neg Example size before shrinking: " + negatives.size());
		if (stable ) {
			negatives = SetManipulation.stableShrinkInd(negatives,neglimit);
		}
		else {
			negatives = SetManipulation.fuzzyShrinkInd(negatives,neglimit);
		}
		logger.debug("neg Example size after shrinking: " + negatives.size());
		return negatives;
	}

	
	/**
	 * just takes all other instances from the ontology, except the ones 
	 * in the fullPositiveSet (see Constructor)
	 */
	public void makeNegativeExamplesFromAllOtherInstances() {
		logger.debug("making random examples ");
		fromAllOther.clear();
		fromAllOther.addAll(reasoningService.getIndividuals());
		fromAllOther.removeAll(fullPositiveSet);
		logger.debug("|-negExample size from random: " + fromAllOther.size());
	}
	
	/**
	 * NOT IMPLEMENTED YET, DO NOT USE
	 * makes neg ex from related instances, that take part in a role R(pos,neg)
	 * filters all objects, that don't use the given namespace 
	 * @param instances
	 * @param objectNamespace
	 */
	public void makeNegativeExamplesFromRelatedInstances(SortedSet<OWLIndividual> instances,
			String objectNamespace) {
		logger.debug("making examples from related instances");
		for (OWLIndividual oneInstance : instances) {
			makeNegativeExamplesFromRelatedInstances(oneInstance, objectNamespace);
		}
		logger.debug("|-negExample size from related: " + fromRelated.size());
	}

	/**
	 * NOT IMPLEMENTED YET, DO NOT USE
	 * @param oneInstance
	 * @param objectnamespace
	 */
	private void makeNegativeExamplesFromRelatedInstances(OWLIndividual oneInstance, String objectnamespace) {
		// SortedSet<String> result = new TreeSet<String>();

		//reasoningService.getRoleMembers(atomicRole)
		
		//fromRelated.removeAll(fullPositiveSet);
		throw new RuntimeException("method makeNegativeExamplesFromRelatedInstances not implemented yet");
	}


	
	/**
	 * NOT IMPLEMENTED YET, DO NOT USE
	 * makes negEx from classes, the posEx belong to.
	 * Gets all Classes from PosEx, gets Instances from these Classes, returns all
	 * @param positiveSet
	 */
	public void makeNegativeExamplesFromParallelClasses(SortedSet<OWLIndividual> positiveSet){
		makeNegativeExamplesFromClassesOfInstances(positiveSet);
	}
	
	/**
	 * NOT IMPLEMENTED YET, DO NOT USE
	 * see <code> makeNegativeExamplesFromParallelClasses</code>
	 * @param positiveSet
	 */
	@SuppressWarnings("unused")
	private void makeNegativeExamplesFromClassesOfInstances(SortedSet<OWLIndividual> positiveSet) {
		logger.debug("making neg Examples from parallel classes");
		SortedSet<OWLClassExpression> classes = new TreeSet<>();
		this.fromParallelClasses.clear();
		
		for (OWLIndividual instance : positiveSet) {
			try{
			// realization is not implemented in reasoningservice
			//classes.addAll(reasoningService.realize()
			}catch (Exception e) {
				logger.warn("not implemented in "+this.getClass());
			}
		}
		logger.debug("getting negExamples from " + classes.size() + " parallel classes");
		for (OWLClassExpression oneClass : classes) {
			logger.debug(oneClass);
			// rsc = new
			// JenaResultSetConvenience(queryConcept("\""+oneClass+"\"",limit));
			try{
			this.fromParallelClasses.addAll(reasoningService.getIndividuals(oneClass));
			}catch (Exception e) {
				logger.warn("not implemented in "+this.getClass());
			}
		}
		
		fromParallelClasses.removeAll(fullPositiveSet);
		logger.debug("|-neg Example size from parallelclass: " + fromParallelClasses.size());
		throw new RuntimeException("not implemented in "+ this.getClass()+"method makeNegativeExamplesFromParallelClasses");
	}

	
	
	
	
	/**
	 * if pos ex derive from one class, then neg ex are taken from a superclass
	 * @param concept
	 */
	public void makeNegativeExamplesFromSuperClasses(OWLClass concept) {
		makeNegativeExamplesFromSuperClasses( concept, 0);
	}
	
	/**
	 * if pos ex derive from one class, then neg ex are taken from a superclass
	 * CURRENTLY SAME METHOD AS makeNegativeExamplesFromSuperClasses(OWLClass concept)
	 * but works quite often 
	 * @param concept
	 * @param depth PARAMETER CURRENTLY NOT USED, ONLY DIRECT SUPERCLASSES
	 */
	public void makeNegativeExamplesFromSuperClasses(OWLClass concept, int depth) {

		fromSuperclasses.clear();
		SortedSet<OWLClassExpression> superClasses = reasoningService.getSuperClasses(concept);
		logger.debug("making neg Examples from " + superClasses.size() + " superclasses");

		for (OWLClassExpression oneSuperClass : superClasses) {
			logger.debug(oneSuperClass);
			fromSuperclasses.addAll(reasoningService.getIndividuals(oneSuperClass));
		}
		this.fromSuperclasses.removeAll(fullPositiveSet);
		logger.debug("|-neg Example from superclass: " + fromSuperclasses.size());
	}
	
	/**
	 * misleading method name,
	 * examples are all instances from the a-Part of the atomicRole(a,b)
	 * it has nothing to do with the actual Domain class 
	 * @param atomicRole
	 */
	
	public void makeNegativeExamplesFromDomain(OWLObjectProperty atomicRole){
		fromDomain.clear();
		logger.debug("making Negative Examples from Domain of : "+atomicRole);
		fromDomain.addAll(reasoningService.getPropertyMembers(atomicRole).keySet());
		fromDomain.removeAll(fullPositiveSet);
		logger.debug("|-neg Example size from Domain: "+this.fromDomain.size());
	}
	
	/**
	 * misleading method name,
	 * examples are all instances from the b-Part of the atomicRole(a,b)
	 * it has nothing to do with the actual Range class 
	 * @param atomicRole
	 */

	public void makeNegativeExamplesFromRange(OWLObjectProperty atomicRole){
		fromRange.clear();
		logger.debug("making Negative Examples from Range of : "+atomicRole);
		Collection<SortedSet<OWLIndividual>> tmp = reasoningService.getPropertyMembers(atomicRole).values();
		for (SortedSet<OWLIndividual> set : tmp) {
			fromRange.addAll(set);
		}
		fromRange.removeAll(fullPositiveSet);
		logger.debug("|-neg Example size from Range: "+fromRange.size());
	}
}
