
package org.dllearner.tools.ore.explanation.laconic;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.tools.ore.explanation.Explanation;
import org.dllearner.tools.ore.explanation.ExplanationException;
import org.dllearner.tools.ore.explanation.PelletExplanationGenerator;
import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasonerFactory;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLLogicalAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;

import com.clarkparsia.explanation.util.ExplanationProgressMonitor;
import com.clarkparsia.explanation.util.SilentExplanationProgressMonitor;

/*
 * This class computes laconic explanations for a given entailment. The algorithm is adapted from the paper
 * 'Laconic and Precise Justifications in OWL' from Matthew Horridge, Bijan Parsia and Ulrike Sattler.
 * 
 */
public class LaconicExplanationGenerator
{

    private PelletExplanationGenerator pelletExplanation;
    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private Set<Explanation> lastRegularExplanations;
    private Set<Explanation> allPreviouslyFoundExplanations;
    private OPlus oPlus;
    
    private static Logger logger = Logger.getLogger(LaconicExplanationGenerator.class);
    
    private ExplanationProgressMonitor progressMonitor = new SilentExplanationProgressMonitor();
    
    public LaconicExplanationGenerator(OWLOntologyManager manager,
			OWLReasonerFactory reasonerFactory, Set<OWLOntology> ontologies) {

		this.manager = manager;

		try {
			ontology = manager.createOntology(URI.create(new StringBuilder().append(
					"http://laconic").append(System.nanoTime()).toString()),
					ontologies, true);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
		oPlus = new OPlus(manager.getOWLDataFactory());
		pelletExplanation = new PelletExplanationGenerator(manager, ontologies);
		lastRegularExplanations = new HashSet<Explanation>();
	}
    
    public LaconicExplanationGenerator(OWLOntologyManager manager) {

		this.manager = manager;

		oPlus = new OPlus(manager.getOWLDataFactory());
		lastRegularExplanations = new HashSet<Explanation>();
	}
    
    public void setOntology(Set<OWLOntology> ontologies){
    	try {
			ontology = manager.createOntology(URI.create(new StringBuilder().append(
			"http://laconic").append(System.nanoTime()).toString()),
			ontologies, true);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pelletExplanation = new PelletExplanationGenerator(manager, ontologies);
		lastRegularExplanations = new HashSet<Explanation>();
    }
    
    /**
     * Computes a more fine grained representation for a set of axioms, which means to split them
     * e.g. for A \sqsubseteq B \sqcap C returning A \sqsubseteq B and A \sqsubseteq C
     * @param axioms to split
     * @return splitted axioms
     */
    public Set<OWLAxiom> computeOPlus(Set<OWLAxiom> axioms) {
//    	oPlus = new OPlus(manager.getOWLDataFactory());
    
    	Set<OWLAxiom> oPlusAxioms = new HashSet<OWLAxiom>();
    	
    	for(OWLAxiom ax : axioms){
    		Set<OWLAxiom> weakenedAxioms = ax.accept(oPlus);
    		oPlusAxioms.addAll(weakenedAxioms);
    	}
	   	return oPlusAxioms;
    }
    
        
    public Set<Explanation> getLastRegularJustifications() {
    	return lastRegularExplanations;
    }
    
    public void setProgressMonitor(ExplanationProgressMonitor mon){
    	this.progressMonitor = mon;
    	pelletExplanation.setProgressMonitor(progressMonitor);
    }
    
    /**
     * Computes the precise explanations
     * @param entailment
     * @param limit
     * @return
     * @throws OWLException
     */
	private Set<Explanation> computePreciseJusts(OWLAxiom entailment, int limit)
			throws OWLException {

		Set<Explanation> regularExplanations = pelletExplanation
				.getExplanations((OWLAxiom) entailment);

		
		logger.debug("Found " + regularExplanations.size() + " regular explanations");
		
		lastRegularExplanations.clear();
		lastRegularExplanations.addAll(regularExplanations);
		allPreviouslyFoundExplanations = new HashSet<Explanation>();
		allPreviouslyFoundExplanations.addAll(regularExplanations);
		Set<Explanation> nonLaconicExplanations = new HashSet<Explanation>();
		Set<Explanation> laconicExplanations = new HashSet<Explanation>();
		Set<OWLAxiom> axiomsInPreviousOntology = new HashSet<OWLAxiom>();
		
		for (;;) {
			if (progressMonitor.isCancelled()) {
	            return laconicExplanations;
	        }
		
			
			Set<OWLAxiom> unionOfAllExplanations = new HashSet<OWLAxiom>();
			for (Explanation expl : allPreviouslyFoundExplanations) {
				unionOfAllExplanations.addAll(expl.getAxioms());
			}

			// Set<OWLAxiom> lastOPlus = new
			// HashSet<OWLAxiom>(computeOPlus(unionOfAllJustifications));
			Set<OWLAxiom> oPlus = computeOPlus(unionOfAllExplanations);
			OWLOntologyManager man2 = OWLManager.createOWLOntologyManager();
			OWLOntology extendedOntology = man2.createOntology(oPlus);
			for (OWLLogicalAxiom logAx : ontology.getLogicalAxioms()) {
				if (!unionOfAllExplanations.contains(logAx)
						|| oPlus.contains(logAx)) {
					man2.addAxiom(extendedOntology, logAx);
				}
			}

			if (extendedOntology.getLogicalAxioms().equals(
					axiomsInPreviousOntology)) {
				logger.debug("Ontology did not changed. Early termination.");
				break;
			}
			axiomsInPreviousOntology.clear();
			axiomsInPreviousOntology
					.addAll(extendedOntology.getLogicalAxioms());
			Set<Explanation> allPrevJustsCopy = new HashSet<Explanation>(
					allPreviouslyFoundExplanations);

			Set<OWLOntology> ont2 = new HashSet<OWLOntology>();
			ont2.add(extendedOntology);
			PelletExplanationGenerator expGen = new PelletExplanationGenerator(man2, ont2);
			Set<Explanation> currentExplanations = expGen
					.getExplanations((OWLAxiom) entailment);

			allPreviouslyFoundExplanations.addAll(currentExplanations);
			if (allPreviouslyFoundExplanations.equals(allPrevJustsCopy)) {
				break;
			}
			for (Explanation currentExplanation : currentExplanations) {
				if (!laconicExplanations.contains(currentExplanation)
						&& !nonLaconicExplanations.contains(currentExplanation)) {
					if (isLaconic(currentExplanation)) {
						laconicExplanations.add(currentExplanation);
					} else {
						nonLaconicExplanations.add(currentExplanation);
					}
					if (laconicExplanations.size() == limit) {
						return laconicExplanations;//retrieveAxioms(laconicExplanations);
					}
				}
			}

		}
		Set<Explanation> explanations = new HashSet<Explanation>();
		for (Explanation explanation : allPreviouslyFoundExplanations) {System.out.println(explanation);
			if (!nonLaconicExplanations.contains(explanation)) {
				if (laconicExplanations.contains(explanation)) {
					explanations.add(explanation);
				} else if (isLaconic(explanation)) {
					explanations.add(explanation);
				}
			}
		}

		return explanations;//retrieveAxioms(explanations);
	}
    
    public boolean isLaconic(Explanation explanation)
			throws ExplanationException {
		boolean laconic;
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

			Set<OWLAxiom> justificationSigmaClosure = computeOPlus(explanation.getAxioms());

			OWLOntology justificationSigmaClosureOnt = manager
					.createOntology(justificationSigmaClosure);

			PelletExplanationGenerator expGen2 = new PelletExplanationGenerator(manager,
					Collections.singleton(justificationSigmaClosureOnt));
			Set<Explanation> exps = expGen2.getExplanations(
					explanation.getEntailment(), 2);

			laconic = Collections.singleton(explanation).equals(exps);

		} catch (OWLOntologyCreationException e) {
			throw new ExplanationException(e);
		} catch (OWLOntologyChangeException e) {
			throw new ExplanationException(e);
		}
		return laconic;
	}
    
    public Set<Explanation> retrieveAxioms(Set<Explanation> explanations) {

		Map<OWLAxiom, Set<OWLAxiom>> sourceAxioms2OPlus = new HashMap<OWLAxiom, Set<OWLAxiom>>();

		for (Explanation explanation : allPreviouslyFoundExplanations) {
			for (OWLAxiom ax : explanation.getAxioms()) {
				if (ontology.containsAxiom(ax)) {
					sourceAxioms2OPlus.put(ax, computeOPlus(Collections.singleton(ax)));
				}
			}
		}
		Set<Explanation> reconstituedExplanations = new HashSet<Explanation>();

		for (Explanation explanation : explanations) {
			Map<OWLClass, Map<OWLAxiom, Set<OWLSubClassAxiom>>> lhs2SubClassAxiom = new HashMap<OWLClass, Map<OWLAxiom, Set<OWLSubClassAxiom>>>();
			Set<OWLAxiom> reconstituedAxioms = new HashSet<OWLAxiom>();
			for (OWLAxiom laconicAx : explanation.getAxioms()) {
				if (laconicAx instanceof OWLSubClassAxiom) {
					OWLSubClassAxiom subAx = (OWLSubClassAxiom) laconicAx;
					if (subAx.getSubClass().isAnonymous()) {
						reconstituedAxioms.add(subAx);
					} else {
						Map<OWLAxiom, Set<OWLSubClassAxiom>> source2AxiomMap = lhs2SubClassAxiom.get(subAx.getSubClass().asOWLClass());
						if (source2AxiomMap == null) {
							source2AxiomMap = new HashMap<OWLAxiom, Set<OWLSubClassAxiom>>();
							lhs2SubClassAxiom.put(subAx.getSubClass().asOWLClass(), source2AxiomMap);
						}

						for (OWLAxiom sourceAxiom : sourceAxioms2OPlus.keySet()) {
							if ((sourceAxioms2OPlus.get(sourceAxiom)).contains(subAx)) {
								Set<OWLSubClassAxiom> subClassAxioms = source2AxiomMap.get(sourceAxiom);
								if (subClassAxioms == null) {
									subClassAxioms = new HashSet<OWLSubClassAxiom>();
									source2AxiomMap.put(sourceAxiom, subClassAxioms);
								}
								subClassAxioms.add(subAx);
							}
						}
					}
				} else {
					reconstituedAxioms.add(laconicAx);
				}
			}
			Set<OWLAxiom> consumedAxioms = new HashSet<OWLAxiom>();
			for (OWLClass lhs : (Set<OWLClass>) lhs2SubClassAxiom.keySet()) {
				Map<OWLAxiom, Set<OWLSubClassAxiom>> source2SubClassAxiom = lhs2SubClassAxiom.get(lhs);
				for (OWLAxiom source : source2SubClassAxiom.keySet()) {
					Set<OWLDescription> rightHandSides = new HashSet<OWLDescription>();
					for (OWLSubClassAxiom sub : source2SubClassAxiom.get(source)) {
						if (!consumedAxioms.contains(sub)) {
							rightHandSides.add(sub.getSuperClass());
							consumedAxioms.add(sub);
						}
					}

					if (rightHandSides.size() == 1)
						reconstituedAxioms.add(manager.getOWLDataFactory().getOWLSubClassAxiom((OWLDescription) lhs,((OWLDescription) rightHandSides.iterator().next())));
					else if (rightHandSides.size() > 1) {
						org.semanticweb.owl.model.OWLObjectIntersectionOf conjunction = manager.getOWLDataFactory().getOWLObjectIntersectionOf(rightHandSides);
						reconstituedAxioms.add(manager.getOWLDataFactory().getOWLSubClassAxiom((OWLDescription) lhs,conjunction));
					}
				}
			}

			reconstituedExplanations.add(new Explanation(explanation.getEntailment(), reconstituedAxioms));

		}
		
		return reconstituedExplanations;
	}

	public Set<Explanation> getExplanations(OWLAxiom entailment)
			throws ExplanationException {
		Set<Explanation> explanations;
		try {
			explanations = computePreciseJusts(entailment, Integer.MAX_VALUE);
		} catch (OWLException e) {
			throw new ExplanationException(e);
		}
		return explanations;
	}

	public Set<Explanation> getExplanations(OWLAxiom entailment, int limit)
			throws ExplanationException {
		
	    logger.debug("Get " + (limit == Integer.MAX_VALUE ? "all" : limit) + " explanation(s) for: " + entailment);
		Set<Explanation> explanations;
		try {
			explanations = computePreciseJusts(entailment, limit);
		} catch (OWLException e) {
			throw new ExplanationException(e);
		}
		return explanations;
	}
    
    public Set<Explanation> getRegularExplanations(OWLAxiom entailment) throws ExplanationException {
    	Set<Explanation> regularJusts;
    	regularJusts = pelletExplanation.getExplanations((OWLAxiom)entailment);
    	lastRegularExplanations.addAll(regularJusts);
    	return regularJusts;
    }
    
    public Set<OWLAxiom> getSourceAxioms(OWLAxiom axiom){
    	Map<OWLAxiom, Set<OWLAxiom>> axioms2SourceMap = oPlus.getAxiomsMap();
    	Set<OWLAxiom> sourceAxioms = new HashSet<OWLAxiom>();
    	System.out.println("Getting source axioms for: " + axiom);
    	for(OWLAxiom ax : axioms2SourceMap.get(axiom)){
    		if(ontology.containsAxiom(ax)){
    			sourceAxioms.add(ax);
    		}
    	}
    	
    	return sourceAxioms;
    }
    
    public Set<OWLAxiom> getLaconicSourceAxioms(OWLAxiom axiom){
    	Map<OWLAxiom, Set<OWLAxiom>> axioms2SourceMap = oPlus.getAxiomsMap();
    	Set<OWLAxiom> sourceAxioms = new HashSet<OWLAxiom>();
    	sourceAxioms.addAll(axioms2SourceMap.get(axiom));
    	
    	return sourceAxioms;
    }
    
    public Set<OWLAxiom> getRemainingAxioms(OWLAxiom source, OWLAxiom part){
    	Set<OWLAxiom> parts = computeOPlus(Collections.singleton(source));
    	for(OWLAxiom p : parts){
    		System.out.println("Part: " + p);
    		System.out.println(oPlus.getAxiomsMap().get(p));
    	}
    	for(OWLAxiom ax :  oPlus.getAxiomsMap().get(part)){
			parts.remove(ax);
		}
    	Set<OWLAxiom> remove = new HashSet<OWLAxiom>();
    	for(OWLAxiom ax : parts){
    		for(OWLAxiom a : computeOPlus(Collections.singleton(ax))){
    			if(!a.equals(ax)){
    				remove.add(a);
    			}
    		}
    		
    	}
    	parts.removeAll(remove);
    	parts.remove(part);
    	return parts;
    	
    	
    }
    
    public static void main(String[] args) throws OWLOntologyCreationException, ExplanationException, OWLOntologyChangeException{
//    	String baseURI = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl";
    	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    	OWLOntology ontology = manager.loadOntologyFromPhysicalURI(URI.create("file:examples/ore/koala.owl"));
    	OWLDataFactory factory = manager.getOWLDataFactory();
    	LaconicExplanationGenerator expGen = new LaconicExplanationGenerator(manager, new PelletReasonerFactory(), Collections.singleton(ontology));
//    	OWLClass koalaWithPhD = factory.getOWLClass(URI.create(baseURI + "#KoalaWithPhD"));
//    	OWLClass koala = factory.getOWLClass(URI.create(baseURI + "#Koala"));
    	
//    	System.out.println(expGen.getExplanations(factory.getOWLSubClassAxiom(koalaWithPhD, factory.getOWLNothing()), 1));
//    	OWLAxiom laconicAx = factory.getOWLSubClassAxiom(koalaWithPhD, koala);
//    	Set<OWLAxiom> sourceAxioms = expGen.getSourceAxioms(laconicAx);
//    	System.out.println("Source axioms: " + sourceAxioms);
//    	for(OWLAxiom sourceAx : sourceAxioms){
//    		System.out.println("\nRebuildet: " + expGen.getRemainingAxioms(sourceAx, laconicAx));
//    	}
    	
    	OWLAxiom ax = factory.getOWLEquivalentClassesAxiom(factory.getOWLClass(URI.create("A")), 
    											factory.getOWLObjectIntersectionOf(factory.getOWLClass(URI.create("C")), 
    																				factory.getOWLObjectSomeRestriction(factory.getOWLObjectProperty(URI.create("R")), 
    																													factory.getOWLClass(URI.create("F")))));
    	OWLAxiom ax2 = factory.getOWLSubClassAxiom(factory.getOWLObjectSomeRestriction(factory.getOWLObjectProperty(URI.create("R")), factory.getOWLThing()),
    												factory.getOWLObjectComplementOf(factory.getOWLClass(URI.create("C"))));
    	Set<OWLAxiom> ont = new HashSet<OWLAxiom>();
    	ont.add(ax);
    	ont.add(ax2);
    	OWLOntology o = manager.createOntology(ont);
    	expGen = new LaconicExplanationGenerator(manager, new PelletReasonerFactory(), Collections.singleton(o));
    	System.out.println(expGen.getExplanations(factory.getOWLSubClassAxiom(factory.getOWLClass(URI.create("A")), factory.getOWLNothing()), 1));
    	
    	OWLAxiom remove = factory.getOWLSubClassAxiom(factory.getOWLClass(URI.create("A")),factory.getOWLObjectSomeRestriction(factory.getOWLObjectProperty(URI.create("R")), factory.getOWLThing()));
    	Set<OWLAxiom> sourceAxioms = expGen.getSourceAxioms(remove);
    	System.out.println("Source axioms: " + sourceAxioms);
    	for(OWLAxiom sourceAx : sourceAxioms){
    		System.out.println("\nRebuildet: " + expGen.getRemainingAxioms(sourceAx, remove));
    	}
    	
    }
	
}
