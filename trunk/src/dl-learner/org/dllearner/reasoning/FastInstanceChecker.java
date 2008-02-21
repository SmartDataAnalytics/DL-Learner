/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.reasoning;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.owl.FlatABox;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;

/**
 * Reasoner for fast instance checks. It works by completely dematerialising the knowledge
 * base to speed up later reasoning requests. It  
 * 
 * @author Jens Lehmann
 *
 */
public class FastInstanceChecker extends ReasonerComponent {

	private Set<NamedClass> atomicConcepts;
	private Set<ObjectProperty> atomicRoles;
	private SortedSet<Individual> individuals;
	
	private ReasoningService rs;
	private ReasonerComponent rc;
	
	// instances of classes
    public Map<NamedClass,SortedSet<Individual>> classInstancesPos = new HashMap<NamedClass,SortedSet<Individual>>();
    public Map<NamedClass,SortedSet<Individual>> classInstancesNeg = new HashMap<NamedClass,SortedSet<Individual>>();
    
    // object property mappings
    public Map<ObjectProperty,Map<Individual,SortedSet<Individual>>> opPos = new HashMap<ObjectProperty,Map<Individual,SortedSet<Individual>>>();
    public Map<ObjectProperty,Map<Individual,SortedSet<Individual>>> opNeg = new HashMap<ObjectProperty,Map<Individual,SortedSet<Individual>>>();    	
	
    // TODO: datatype properties
    
	public FastInstanceChecker(Set<KnowledgeSource> sources) {
		rc = new OWLAPIReasoner(sources);
		try {
			rc.init();
		} catch (ComponentInitException e1) {
			e1.printStackTrace();
		}
		atomicConcepts = rc.getAtomicConcepts();
		atomicRoles = rc.getAtomicRoles();
		individuals = rc.getIndividuals();
		rs = new ReasoningService(rc);
		
		// TODO: some code taken from Helper.createFlatABox, but pasted here because additional things need to 
		// be done (maybe this can be merge again with the FastRetrievalReasoner later)
		long dematStartTime = System.currentTimeMillis();

		FlatABox aBox = new FlatABox();
		for (NamedClass atomicConcept : rs.getAtomicConcepts()) {
			// aBox.atomicConceptsPos.put(atomicConcept.getName(), getStringSet(rs
			//		.retrieval(atomicConcept)));
//			Negation negatedAtomicConcept = new Negation(atomicConcept);
			// aBox.atomicConceptsNeg.put(atomicConcept.getName(), getStringSet(rs
			//		.retrieval(negatedAtomicConcept)));
			aBox.concepts.add(atomicConcept.getName());
		}

		for (ObjectProperty atomicRole : rs.getAtomicRoles()) {
			// aBox.rolesPos.put(atomicRole.getName(), getStringMap(rs.getRoleMembers(atomicRole)));
			aBox.roles.add(atomicRole.getName());
		}

		// aBox.domain = getStringSet(rs.getIndividuals());
		// aBox.top = aBox.domain;

		// System.out.println(aBox);

		long dematDuration = System.currentTimeMillis() - dematStartTime;
		System.out.println("OK (" + dematDuration + " ms)");		
		
	}	
	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.config.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getAtomicConcepts()
	 */
	public Set<NamedClass> getAtomicConcepts() {
		return atomicConcepts;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getAtomicRoles()
	 */
	public Set<ObjectProperty> getAtomicRoles() {
		return atomicRoles;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getIndividuals()
	 */
	public SortedSet<Individual> getIndividuals() {
		return individuals;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getReasonerType()
	 */
	public ReasonerType getReasonerType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#prepareSubsumptionHierarchy(java.util.Set)
	 */
	public void prepareSubsumptionHierarchy(Set<NamedClass> allowedConcepts) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
