package org.dllearner.reasoning;

import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.ObjectProperty;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Conjunction;
import org.dllearner.core.dl.FlatABox;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.Negation;
import org.dllearner.core.dl.RoleHierarchy;
import org.dllearner.core.dl.SubsumptionHierarchy;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.SortedSetTuple;

public class FastRetrievalReasoner extends ReasonerComponent {

	FlatABox abox;
	FastRetrieval fastRetrieval;
	Set<AtomicConcept> atomicConcepts;
	Set<ObjectProperty> atomicRoles;
	SortedSet<Individual> individuals;
	
	ReasoningService rs;
	ReasonerComponent rc;
	
	public FastRetrievalReasoner(Set<KnowledgeSource> sources) {
		rc = new DIGReasoner(sources);
		rc.init();
		atomicConcepts = rc.getAtomicConcepts();
		atomicRoles = rc.getAtomicRoles();
		individuals = rc.getIndividuals();
		rs = new ReasoningService(rc);
		try {
			abox = Helper.createFlatABox(rs);
		} catch (ReasoningMethodUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fastRetrieval = new FastRetrieval(abox);
	}	
	
	public FastRetrievalReasoner(FlatABox abox) {
		this.abox = abox;
		fastRetrieval = new FastRetrieval(abox);
		
		// atomare Konzepte und Rollen initialisieren
		atomicConcepts = new HashSet<AtomicConcept>();
		for(String concept : abox.concepts) {
			atomicConcepts.add(new AtomicConcept(concept));
		}
		atomicRoles = new HashSet<ObjectProperty>();
		for(String role : abox.roles) {
			atomicRoles.add(new ObjectProperty(role));
		}
		individuals = new TreeSet<Individual>();
		for(String individualName : abox.domain)
			individuals.add(new Individual(individualName));
		
	}
	
	public ReasonerType getReasonerType() {
		return ReasonerType.FAST_RETRIEVAL;
	}

	@Override		
	public SortedSetTuple<Individual> doubleRetrieval(Concept concept) {
		return Helper.getIndividualTuple(fastRetrieval.calculateSets(concept));
	}	
	
	@Override		
	public SortedSetTuple<Individual> doubleRetrieval(Concept concept, Concept adc) {
		SortedSetTuple<String> adcSet = fastRetrieval.calculateSets(adc);
		return Helper.getIndividualTuple(fastRetrieval.calculateSetsADC(concept, adcSet));
	}	
	
	@Override		
	public SortedSet<Individual> retrieval(Concept concept) {
		return Helper.getIndividualSet(fastRetrieval.calculateSets(concept).getPosSet());
	}
	
	public Set<AtomicConcept> getAtomicConcepts() {
		return atomicConcepts;
	}

	public Set<ObjectProperty> getAtomicRoles() {
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
	public boolean subsumes(Concept superConcept, Concept subConcept) {
		Negation neg = new Negation(subConcept);
		Conjunction c = new Conjunction(neg,superConcept);
		return fastRetrieval.calculateSets(c).getPosSet().isEmpty();
	}
	
	@Override
	public void prepareRoleHierarchy(Set<ObjectProperty> allowedRoles) {
		rs.prepareRoleHierarchy(allowedRoles);
	}	
	
	@Override
	public RoleHierarchy getRoleHierarchy() {
		return rs.getRoleHierarchy();
	}	
	
	public void prepareSubsumptionHierarchy(Set<AtomicConcept> allowedConcepts) {
		rs.prepareSubsumptionHierarchy(allowedConcepts);
	}

	@Override
	public SubsumptionHierarchy getSubsumptionHierarchy() {
		return rs.getSubsumptionHierarchy();
	}	
	
	@Override
	public boolean isSatisfiable() {
		return rs.isSatisfiable();
	}
	
	@Override
	public boolean instanceCheck(Concept concept, Individual individual) {
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
}
