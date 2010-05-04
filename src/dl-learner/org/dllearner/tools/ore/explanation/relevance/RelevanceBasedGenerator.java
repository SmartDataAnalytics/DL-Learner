package org.dllearner.tools.ore.explanation.relevance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mindswap.pellet.utils.SetUtils;
import org.mindswap.pellet.utils.Timer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.owlapi.explanation.GlassBoxExplanation;
import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapi.explanation.TransactionAwareSingleExpGen;
import com.clarkparsia.owlapiv3.OntologyUtils;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class RelevanceBasedGenerator {
	static{
		setup();
	}
	
	public static void setup() {
		GlassBoxExplanation.setup();
	}
	
	public static final Logger log = Logger.getLogger(RelevanceBasedGenerator.class);
	
	
	private PelletReasoner globalReasoner;
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private PelletReasoner localReasoner;
	
	private TransactionAwareSingleExpGen singleExpGen;
	private PelletReasonerFactory reasonerFactory;
	
	private Set<Set<OWLAxiom>> allJusts;
	private Set<HittingSet> hittingSets;
	
	private SimpleSelectionFunction selector;
	
	private PelletExplanation pellet;
	
	
	Map<OWLAxiom, Integer> axiomMap;

	public RelevanceBasedGenerator(OWLOntologyManager manager,
			OWLOntology ontology, PelletReasoner reasoner) {
		log.setLevel(Level.DEBUG);

		this.globalReasoner = reasoner;
		this.manager = manager;
		this.ontology = ontology;//getExampleOntology();

		reasonerFactory = new PelletReasonerFactory();

		allJusts = new HashSet<Set<OWLAxiom>>();
		hittingSets = new HashSet<HittingSet>();

		selector = new SimpleSelectionFunction();
		selector.init(ontology);
	}
	
	private OWLOntology getExampleOntology(){
		try {
			OWLDataFactory factory = manager.getOWLDataFactory();
			OWLClass u = factory.getOWLClass(IRI.create("U"));
			OWLClass a = factory.getOWLClass(IRI.create("A"));
			OWLClass b = factory.getOWLClass(IRI.create("B"));
			OWLClass c = factory.getOWLClass(IRI.create("C"));
			OWLClass d = factory.getOWLClass(IRI.create("D"));
			OWLClass e = factory.getOWLClass(IRI.create("E"));
			OWLClass f = factory.getOWLClass(IRI.create("F"));
			OWLClass g = factory.getOWLClass(IRI.create("G"));
			OWLClass h = factory.getOWLClass(IRI.create("H"));
			OWLClass k = factory.getOWLClass(IRI.create("K"));
			List<OWLAxiom> examples = new ArrayList<OWLAxiom>();
			examples.add( factory.getOWLSubClassOfAxiom(u, a));
			examples.add(  factory.getOWLSubClassOfAxiom(u, factory.getOWLObjectComplementOf(a)));
			examples.add(  factory.getOWLSubClassOfAxiom(u, c));
			examples.add(  factory.getOWLSubClassOfAxiom(c, factory.getOWLObjectComplementOf(b)));
			examples.add(  factory.getOWLSubClassOfAxiom(a, b));
			examples.add(  factory.getOWLSubClassOfAxiom(u, g));
			examples.add(  factory.getOWLSubClassOfAxiom(g, e));
			examples.add(  factory.getOWLSubClassOfAxiom(u, f));
			examples.add( factory.getOWLSubClassOfAxiom(f, factory.getOWLObjectComplementOf(e)));
			examples.add( factory.getOWLSubClassOfAxiom(u, d));
			examples.add(  factory.getOWLSubClassOfAxiom(d, e));
			examples.add(  factory.getOWLSubClassOfAxiom(c, k));
			examples.add( factory.getOWLSubClassOfAxiom(k, factory.getOWLObjectComplementOf(h)));
			examples.add( factory.getOWLSubClassOfAxiom(b, h));
			OWLOntology example = manager.createOntology(new HashSet<OWLAxiom>(examples));
			axiomMap = new HashMap<OWLAxiom, Integer >();
			for(int i = 1; i<=examples.size(); i++){		
				axiomMap.put(examples.get(i - 1), Integer.valueOf(i));
			}
			return example;
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private Set<Integer> axioms2Integer(Set<OWLAxiom> axioms){
		Set<Integer> path = new HashSet<Integer>();
		for(OWLAxiom ax: axioms){
			path.add(axiomMap.get(ax));
		}
		return path;
	}
	
	public Set<Set<OWLAxiom>> getExplanations(OWLClass unsat){
		computeAllJusts(unsat);
		return allJusts;
	}
	
	private void computeAllJusts(OWLClass unsat){
		try {
			allJusts.clear();
			hittingSets.clear();
			
			Set<HittingSet> hittingSetsLocal = new HashSet<HittingSet>();
			
			int step = 1;
			
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology temp = man.createOntology(Collections.<OWLAxiom>emptySet());
			
			localReasoner = new PelletReasonerFactory().createReasoner(temp);
					
			singleExpGen = new GlassBoxExplanation(localReasoner);
			setup();
			
			Set<OWLAxiom> selectedAxioms = selector.getRelatedAxioms(unsat);
			Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
			allAxioms.addAll(selectedAxioms);
			
			while(!selectedAxioms.isEmpty()){log.debug("Step: " + step);
				addAxioms(selectedAxioms);
				localReasoner.refresh();
				
				if(!hittingSetsLocal.isEmpty()){
					for(HittingSet hit : hittingSetsLocal){
						removeAxioms(hit, ontology, manager);globalReasoner.refresh();
						if(globalReasoner.isSatisfiable(unsat)){
							hittingSets.add(hit);
						}
						addAxioms(hit, ontology, manager);globalReasoner.refresh();
					}
					
					hittingSetsLocal.removeAll(hittingSets);
					
					if(hittingSetsLocal.isEmpty()){
						return;
					}
					
					Set<HittingSet> hittingSetsTemp = new HashSet<HittingSet>(hittingSetsLocal);
					for(HittingSet hit : hittingSetsTemp){
						removeAxioms(hit, temp, man);
						Set<HittingSet> hittingSetsLocalTemp = expandHittingSetTree(unsat, temp, man, hit);
						for(HittingSet hit2 : hittingSetsLocalTemp){
							hit2.addAll(hit);
							hittingSetsLocal.add(hit2);
						}
						addAxioms(hit, temp, man);
					}
					
					
				} else if(!singleExpGen.getReasoner().isSatisfiable(unsat)){
					
					hittingSetsLocal.addAll(expandHittingSetTree(unsat, temp, man, new HittingSet()));
					
				}
				step++;
				selectedAxioms = selector.getRelatedAxioms(selectedAxioms);
				selectedAxioms.removeAll(allAxioms);
				allAxioms.addAll(selectedAxioms);
				
		
			}
			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	private Set<HittingSet> expandHittingSetTree(OWLClass unsat, OWLOntology ont, OWLOntologyManager man, HittingSet currentPath){
		Set<HittingSet> hittingSets = new HashSet<HittingSet>();
		Set<HittingSet> hittingSets1 = new HashSet<HittingSet>();
		Set<Set<OWLAxiom>> currentJusts = new HashSet<Set<OWLAxiom>>();
//		log.debug("CurrentPath: " + axioms2Integer(currentPath));
		log.debug("CurrentPath: " + currentPath);
		Set<OWLAxiom> justification = null;
		if(!allJusts.isEmpty()){
			for(Set<OWLAxiom> just : allJusts){
				if(!SetUtils.intersects(just, currentPath)){
					log.debug("using global ontology justification reuse:" + just);
//					log.debug("using global ontology justification reuse:" + axioms2Integer(just));
					justification = just;
					currentJusts.add(justification);
					break;
				}
			}
		}
		if(justification == null){
//			pellet = new PelletExplanation(man, Collections.singleton(ont));
			justification = singleExpGen.getExplanation(unsat);//pellet.getUnsatisfiableExplanation(unsat);
			if(!justification.isEmpty()){
//				log.debug("Found new justification: " + axioms2Integer(justification));
				log.debug("Found new justification: " + justification);
				if(allJusts.isEmpty()){
					singleExpGen.beginTransaction();
				}
				allJusts.add(justification);
				currentJusts.add(justification);
			}
		}
		
		
		for(OWLAxiom ax : justification){
			hittingSets1.add(new HittingSet(ax));
		}
		
		while(true){
			Set<HittingSet> hittingSets2 = new HashSet<HittingSet>();
			for(HittingSet hit : hittingSets1){
				boolean earlyPathTermination = false;
				for(HittingSet h : SetUtils.union(hittingSets,this.hittingSets)){
					if(hit.containsAll(h)){
						earlyPathTermination = true;
//						log.debug("early path termination:" + axioms2Integer(hit) + " contains all " + axioms2Integer(h));
						log.debug("early path termination:" + hit + " contains all " + h);
						break;
					}
				}				
				if(!earlyPathTermination){
					removeAxioms(hit);localReasoner.refresh();
					if(localReasoner.isSatisfiable(unsat)){
						hittingSets.add(hit);
					} else {
						hittingSets2.add(hit);
					}
					addAxioms(hit);localReasoner.refresh();
				}
			}
			
			if(hittingSets1.isEmpty() || hittingSets2.isEmpty()){
				return hittingSets;
			}
			
			hittingSets1.clear();
			for(HittingSet hit : hittingSets2){
//				log.debug("CurrentPath: " + axioms2Integer(hit));
				log.debug("CurrentPath: " + hit);
				
				justification = null;
				if(!currentJusts.isEmpty()){
					for(Set<OWLAxiom> just : allJusts){
						if(!SetUtils.intersects(just, hit)){
							log.debug("using sub ontology justification reuse: " + just);
//							log.debug("using sub ontology justification reuse: " + axioms2Integer(just));
							justification = just;
							break;
						}
					}
				}
				if(justification == null){
					removeAxioms(hit);System.out.println(singleExpGen.getExplanation(unsat));
//					pellet = new PelletExplanation(man, Collections.singleton(ont));
					justification = singleExpGen.getExplanation(unsat);//pellet.getUnsatisfiableExplanation(unsat);
					if(!justification.isEmpty()){
//						log.debug("found new justification: " + axioms2Integer(justification));
						log.debug("found new justification: " + justification);
						allJusts.add(justification);
						currentJusts.add(justification);
					}
				}
				
				for(OWLAxiom ax : justification){
					HittingSet h = new HittingSet(hit);
					h.add(ax);
					hittingSets1.add(h);
				}
				
				addAxioms(hit);
			}
			
		}
	}
	
	private void removeAxioms(Set<OWLAxiom> axioms, OWLOntology ontology, OWLOntologyManager man){
		try {
			for(OWLAxiom ax : axioms){
				OntologyUtils.removeAxiom(ax, Collections.singleton(ontology), man);
			}
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void removeAxioms(Set<OWLAxiom> axioms){
		try {
			for(OWLAxiom ax : axioms){
				OntologyUtils.removeAxiom(ax, singleExpGen.getReasoner().getRootOntology().getImportsClosure(),
						singleExpGen.getOntologyManager());
			}
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void addAxioms(Set<OWLAxiom> axioms, OWLOntology ontology, OWLOntologyManager man){
		try {
			for(OWLAxiom ax : axioms){
				OntologyUtils.addAxiom(ax, Collections.singleton(ontology), man);
			}
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addAxioms(Set<OWLAxiom> axioms){
		try {
			for(OWLAxiom ax : axioms){
				OntologyUtils.addAxiom(ax, singleExpGen.getReasoner().getRootOntology().getImportsClosure(), singleExpGen.getOntologyManager());
			}
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException {
//		IRI file = IRI.create("file:examples/ore/koala.owl");
//		String base = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#";
//		IRI classIRI = IRI.create(base + "KoalaWithPhD");
		
		IRI file = IRI.create("file:examples/ore/tambis.owl");
		String base = "http://krono.act.uji.es/Links/ontologies/tambis.owl#";
		IRI classIRI = IRI.create(base + "metal");

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass cl = factory.getOWLClass(classIRI);
		OWLOntology ontology = manager.loadOntology(file);
		PelletReasoner reasoner = new PelletReasonerFactory().createReasoner(ontology);
	
		PelletExplanation pellet = new PelletExplanation(ontology);
		Timer t1 = new Timer("pellet");
		t1.start();
		System.out.println(pellet.getUnsatisfiableExplanations(cl).size());
		t1.stop();
		System.out.println(t1.getTotal());
		
		RelevanceBasedGenerator expGen = new RelevanceBasedGenerator(manager, ontology, reasoner);
		Timer t2 = new Timer("relevance");
		t2.start();
		System.out.println(expGen.getExplanations(cl).size());
		t2.stop();
		System.out.println(t2.getTotal());

	}
}
