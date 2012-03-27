/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.reasoning;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.FlatABox;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SortedSetTuple;

/**
 * 
 * Reasoner for fast retrieval inference (other tasks redirected to OWL API reasoner). Not actively used anymore.
 * 
 * @author Jens Lehmann
 *
 */
public class FastRetrievalReasoner extends AbstractReasonerComponent {
	
	FlatABox abox;
	FastRetrieval fastRetrieval;
	Set<NamedClass> atomicConcepts;
	Set<ObjectProperty> atomicRoles;
	SortedSet<Individual> individuals;
	
	AbstractReasonerComponent rc;
	
	public FastRetrievalReasoner(Set<KnowledgeSource> sources) {
		super(sources);
		
		rc = new OWLAPIReasoner(sources);
		try {
			rc.init();
		} catch (ComponentInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		atomicConcepts = rc.getNamedClasses();
		atomicRoles = rc.getObjectProperties();
		individuals = rc.getIndividuals();
//		rs = new ReasonerComponent(rc);
		try {
			abox = Helper.createFlatABox(rc);
		} catch (ReasoningMethodUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fastRetrieval = new FastRetrieval(abox);
	}	
	
	public FastRetrievalReasoner(FlatABox abox) {
		super(null);
		this.abox = abox;
		fastRetrieval = new FastRetrieval(abox);

		// atomare Konzepte und Rollen initialisieren
		atomicConcepts = new HashSet<NamedClass>();
		for(String concept : abox.concepts) {
			atomicConcepts.add(new NamedClass(concept));
		}
		atomicRoles = new HashSet<ObjectProperty>();
		for(String role : abox.roles) {
			atomicRoles.add(new ObjectProperty(role));
		}
		individuals = new TreeSet<Individual>();
		for(String individualName : abox.domain)
			individuals.add(new Individual(individualName));
		
	}
	
	@Override
	public ReasonerType getReasonerType() {
		return ReasonerType.FAST_RETRIEVAL;
	}

	@Override		
	public SortedSetTuple<Individual> doubleRetrievalImpl(Description concept) {
		return Helper.getIndividualTuple(fastRetrieval.calculateSets(concept));
	}	
	
//	@Override		
//	public SortedSetTuple<Individual> doubleRetrieval(Description concept, Description adc) {
//		SortedSetTuple<String> adcSet = fastRetrieval.calculateSets(adc);
//		return Helper.getIndividualTuple(fastRetrieval.calculateSetsADC(concept, adcSet));
//	}	
	
	@Override		
	public SortedSet<Individual> getIndividualsImpl(Description concept) {
		return Helper.getIndividualSet(fastRetrieval.calculateSets(concept).getPosSet());
	}
	
	public Set<NamedClass> getNamedClasses() {
		return atomicConcepts;
	}

	@Override
	public Set<ObjectProperty> getObjectProperties() {
		return atomicRoles;
	}

	public SortedSet<Individual> getIndividuals() {
		return individuals;
	}

	public FlatABox getFlatAbox() {
		return abox;
	}

	// C \sqsubseteq D is rewritten to a retrieval for \not C \sqcap D
	@Override
	public boolean isSuperClassOfImpl(Description superConcept, Description subConcept) {
//		Negation neg = new Negation(subConcept);
//		Intersection c = new Intersection(neg,superConcept);
//		return fastRetrieval.calculateSets(c).getPosSet().isEmpty();
		return rc.isSuperClassOf(superConcept, subConcept);
	}
	
//	@Override
//	public void prepareRoleHierarchy(Set<ObjectProperty> allowedRoles) {
//		rs.prepareRoleHierarchy(allowedRoles);
//	}	
	
//	@Override
//	public ObjectPropertyHierarchy getRoleHierarchy() {
//		return rs.getRoleHierarchy();
//	}	
	
//	public void prepareSubsumptionHierarchy(Set<NamedClass> allowedConcepts) {
//		rs.prepareSubsumptionHierarchy(allowedConcepts);
//	}

//	@Override
//	public ClassHierarchy getClassHierarchy() {
//		return rs.getClassHierarchy();
//	}	
	
	@Override
	public boolean isSatisfiableImpl() {
		return rc.isSatisfiable();
	}
	
	@Override
	public boolean hasTypeImpl(Description concept, Individual individual) {
		return fastRetrieval.calculateSets(concept).getPosSet().contains(individual.getName());
	}
	
	public static String getName() {
		return "fast retrieval reasoner";
	} 	
	
	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getBaseURI()
	 */
	public String getBaseURI() {
		return rc.getBaseURI();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getPrefixes()
	 */
	public Map<String, String> getPrefixes() {
		return rc.getPrefixes();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.ReasonerComponent#releaseKB()
	 */
	@Override
	public void releaseKB() {
		rc.releaseKB();
	}


//	@Override
//	public boolean hasDatatypeSupport() {
//		return true;
//	}
}
