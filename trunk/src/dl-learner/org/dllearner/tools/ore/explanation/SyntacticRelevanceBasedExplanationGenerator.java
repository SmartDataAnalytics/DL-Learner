package org.dllearner.tools.ore.explanation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.mindswap.pellet.owlapi.Reasoner;
import org.mindswap.pellet.utils.SetUtils;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.RemoveAxiom;

import uk.ac.manchester.cs.owl.dlsyntax.DLSyntaxObjectRenderer;

import com.clarkparsia.explanation.PelletExplanation;

public class SyntacticRelevanceBasedExplanationGenerator {
	
	private Reasoner reasoner;
	private Reasoner localReasoner;
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	public static enum Strategie{
		All_Just_Relevance,
		CM_Just_Relevance;
	}
	
	private Strategie strategie;
	
	private static Logger logger = Logger.getRootLogger();

	public SyntacticRelevanceBasedExplanationGenerator(Reasoner reasoner, OWLOntologyManager manager){
		this.reasoner = reasoner;
		this.manager = manager;
		this.ontology = reasoner.getLoadedOntologies().iterator().next();
		
		SimpleLayout layout = new SimpleLayout();
	    ConsoleAppender consoleAppender = new ConsoleAppender( layout );
	    logger.addAppender( consoleAppender );
	    logger.setLevel(Level.DEBUG);

	}
	
	public Set<Set<OWLAxiom>> getExplanations(OWLClass unsat, Strategie strategie){
		this.strategie = strategie;
		return computeRelevantJustifications(unsat);
	}
	
	private Set<Set<OWLAxiom>> computeRelevantJustifications(OWLClass unsat){
		
		OWLOntology ont = null;
		Set<Set<OWLAxiom>> justifications = new HashSet<Set<OWLAxiom>>();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		localReasoner = new PelletReasonerFactory().createReasoner(man);
		int k = 1;
		try {
			ont = man.createOntology(URI.create("file:/home/lorenz/test.owl"));
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		localReasoner.loadOntology(ont);
		
		Set<OWLAxiom> relevant = AxiomSelector.getSyntacticRelevantAxioms(ontology, unsat);//getSyntacticRelevantAxioms(unsat, k);
		
		Set<HittingSet> hittingSets = new HashSet<HittingSet>();
		Set<HittingSet> hittingSetLocal = new HashSet<HittingSet>();
		try {
			man.addAxioms(ont, relevant);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		while(!relevant.isEmpty()){
			logger.debug("step " + k + ": selected axioms: " + relevant);
			try {
				man.addAxioms(ont, relevant);
			} catch (OWLOntologyChangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reasoner.refresh();
			if(!hittingSetLocal.isEmpty()){// checking for global hitting sets
				for(HittingSet hit : hittingSetLocal){
					try {
						for(OWLAxiom ax : hit){
							manager.applyChange(new RemoveAxiom(ontology, ax));
						}
						reasoner.refresh();

						if(reasoner.isSatisfiable(unsat)){
							hittingSets.add(hit);
							logger.debug("step " + k + ": found global hitting set: " + hit);
						}
						manager.addAxioms(ontology, hit);
						reasoner.refresh();
					} catch (OWLOntologyChangeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				hittingSetLocal.removeAll(hittingSets);
				if( (!strategie.equals(Strategie.All_Just_Relevance) && !hittingSets.isEmpty()) || (hittingSetLocal.isEmpty()) ){
					logger.debug("early termination");System.out.println(hittingSets);
					return justifications;
				}
				Set<HittingSet> temp = new HashSet<HittingSet>(hittingSetLocal);
				for(HittingSet hit : temp){
					try {
						for(OWLAxiom ax : hit){
							man.applyChange(new RemoveAxiom(ont, ax));
						}
						List<Set<? extends Set<OWLAxiom>>> result = computeJustifications(unsat, ont, hit, justifications, k);
						justifications.addAll(result.get(0));
						Set<HittingSet> localTemp = (Set<HittingSet>)result.get(1);
						for(HittingSet h : localTemp){
							h.addAll(hit);
							hittingSetLocal.add(h);
						}
						hittingSetLocal.remove(hit);
						man.addAxioms(ont, hit);
					} catch (OWLOntologyChangeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
			} else if(!localReasoner.isSatisfiable(unsat)){
			
				List<Set<? extends Set<OWLAxiom>>> result = computeJustifications(unsat, ont, null, null, k);
				logger.debug("step " + k + ": justifications computed : " + result.get(0));
				justifications.addAll(result.get(0));
				hittingSetLocal.addAll((Set<HittingSet>)result.get(1));
			}
			k++;
			relevant = AxiomSelector.getSyntacticRelevantAxioms(ontology, relevant);//getSyntacticRelevantAxioms(unsat, k);
		}
		return justifications;
	}
	
	private List<Set<? extends Set<OWLAxiom>>> computeJustifications(OWLClass unsat, OWLOntology ont, HittingSet path, Set<Set<OWLAxiom>> justifications, int step){
		
		Set<Set<OWLAxiom>> newJustifications = new HashSet<Set<OWLAxiom>>();
		PelletExplanation expGen = new PelletExplanation(manager, Collections.singleton(ont));
		Set<HittingSet> hittingSets = new HashSet<HittingSet>();
		Set<HittingSet> hittingSets1 = new HashSet<HittingSet>();
		Set<OWLAxiom> justification = null;
		if(path != null && !justifications.isEmpty()){
			for(Set<OWLAxiom> just : justifications){
				if(!SetUtils.intersects(path, just)){
					justification = just;
					logger.debug("using justification reuse: " + just);
					break;
				}
			}
		}
		if(justification == null){
			justification = expGen.getUnsatisfiableExplanation(unsat);
		}
		
		newJustifications.add(justification);
		for(OWLAxiom ax : justification){
			hittingSets1.add(new HittingSet(ax));
		}
		
		while(true){
			Set<HittingSet> hittingSets2 = new HashSet<HittingSet>();
			for(HittingSet hit : hittingSets1){
				try {
					for(OWLAxiom ax : hit){
						manager.applyChange(new RemoveAxiom(ont, ax));
					}
					localReasoner.refresh();
					
					if(localReasoner.isSatisfiable(unsat)){
						hittingSets.add(hit);
						logger.debug("step " + step +": found local hitting set: " + hit);
					} else {
						hittingSets2.add(hit);
					}
					manager.addAxioms(ont, hit);
					localReasoner.refresh();
				} catch (OWLOntologyChangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if( (!strategie.equals(Strategie.All_Just_Relevance) && !hittingSets.isEmpty() ) || hittingSets1.isEmpty() || hittingSets2.isEmpty()){
				List<Set<? extends Set<OWLAxiom>>> result = new ArrayList<Set<? extends Set<OWLAxiom>>>();
				result.add(newJustifications);
				result.add(hittingSets);
				return result;
			}
			hittingSets1.clear();
			for(HittingSet hit2 : hittingSets2){
				try {
					for(OWLAxiom ax : hit2){
						manager.applyChange(new RemoveAxiom(ont, ax));
					}
					// justification reuse
					Set<OWLAxiom> just = null;
					if(!newJustifications.isEmpty()){
						for(Set<OWLAxiom> jus : newJustifications){
							if(!SetUtils.intersects(hit2, jus)){
								just = jus;
								logger.debug("using justification reuse: " + just);
								break;
							}
						}
					}
					if(just == null){
						expGen = new PelletExplanation(manager, Collections.singleton(ont));
						just = expGen.getUnsatisfiableExplanation(unsat);
					}
					/////////////////////
					
					
					
					newJustifications.add(just);
					for(OWLAxiom a : just){
						HittingSet temp = new HittingSet(hit2);
						temp.add(a);
						hittingSets1.add(temp);
					}
					manager.addAxioms(ont, hit2);
				} catch (OWLOntologyChangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	
	public Set<OWLAxiom> getSyntacticRelevantAxioms(OWLClass cl, int k) {

		Set<OWLAxiom> relevantAxioms = new HashSet<OWLAxiom>();

		if (k == 1) {
			for (OWLAxiom ax : ontology.getLogicalAxioms()) {
				if (ax.getSignature().contains(cl)) {
					relevantAxioms.add(ax);
				}
			}

		} else {
			Set<OWLAxiom> axioms = getSyntacticRelevantAxioms(cl, k - 1);

			for (OWLAxiom ax1 : axioms) {

				for (OWLAxiom ax2 : ontology.getLogicalAxioms()) {

					if (areSyntacticRelevant(ax1, ax2)) {

						relevantAxioms.add(ax2);
					}
				}
			}
			for (int i = k - 1; i >= 1; i--) {
				relevantAxioms.removeAll(getSyntacticRelevantAxioms(cl, i));
			}

		}

		return relevantAxioms;
	}
	
	private boolean areSyntacticRelevant(OWLAxiom ax1, OWLAxiom ax2){
		return org.mindswap.pellet.utils.SetUtils.intersects(ax1.getSignature(), ax2.getSignature());
	}
	
	public static void main(String[] args){
		URI file = URI.create("file:examples/ore/koala.owl");
		String base = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl";
		URI classURI = URI.create(base + "#KoalaWithPhD");
		
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory factory = manager.getOWLDataFactory();
			OWLClass cl = factory.getOWLClass(classURI);
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(file);
			
//			OWLClass cl1 = factory.getOWLClass(URI.create("Manager"));
//			OWLClass cl2 = factory.getOWLClass(URI.create("Employee"));
//			OWLClass cl3 = factory.getOWLClass(URI.create("JobPosition"));
//			OWLClass cl4 = factory.getOWLClass(URI.create("Leader"));
//			OWLClass cl5 = factory.getOWLClass(URI.create("Situation"));
//			OWLClass cl6 = factory.getOWLClass(URI.create("Happening"));
//			OWLClass cl7 = factory.getOWLClass(URI.create("Patent"));
//			OWLIndividual ind = factory.getOWLIndividual(URI.create("lectureship"));
//			Set<OWLAxiom> examples = new HashSet<OWLAxiom>();
//			examples.add(factory.getOWLSubClassAxiom(cl1, cl2));
//			examples.add(factory.getOWLSubClassAxiom(cl2, cl3));
//			examples.add(factory.getOWLSubClassAxiom(cl4, cl3));
//			examples.add(factory.getOWLSubClassAxiom(cl3, cl5));
//			examples.add(factory.getOWLSubClassAxiom(cl5, cl6));
//			examples.add(factory.getOWLSubClassAxiom(cl4, factory.getOWLObjectComplementOf(cl7)));
//			examples.add(factory.getOWLSubClassAxiom(cl6, factory.getOWLObjectComplementOf(cl1)));
//			examples.add(factory.getOWLSubClassAxiom(cl3, factory.getOWLObjectComplementOf(cl2)));
//			examples.add(factory.getOWLClassAssertionAxiom(ind, cl3));
//			OWLOntology example = manager.createOntology(examples);
			
			OWLClass u = factory.getOWLClass(URI.create("U"));
			OWLClass a = factory.getOWLClass(URI.create("A"));
			OWLClass b = factory.getOWLClass(URI.create("B"));
			OWLClass c = factory.getOWLClass(URI.create("C"));
			OWLClass d = factory.getOWLClass(URI.create("D"));
			OWLClass e = factory.getOWLClass(URI.create("E"));
			OWLClass f = factory.getOWLClass(URI.create("F"));
			OWLClass g = factory.getOWLClass(URI.create("G"));
			OWLClass h = factory.getOWLClass(URI.create("H"));
			OWLClass k = factory.getOWLClass(URI.create("K"));
			List<OWLAxiom> examples = new ArrayList<OWLAxiom>();
			
			
			
			examples.add( factory.getOWLSubClassAxiom(u, a));
			examples.add(  factory.getOWLSubClassAxiom(u, factory.getOWLObjectComplementOf(a)));
			examples.add(  factory.getOWLSubClassAxiom(u, c));
			examples.add(  factory.getOWLSubClassAxiom(c, factory.getOWLObjectComplementOf(b)));
			examples.add(  factory.getOWLSubClassAxiom(a, b));
			examples.add(  factory.getOWLSubClassAxiom(u, g));
			examples.add(  factory.getOWLSubClassAxiom(g, e));
			examples.add(  factory.getOWLSubClassAxiom(u, f));
			examples.add( factory.getOWLSubClassAxiom(f, factory.getOWLObjectComplementOf(e)));
			examples.add( factory.getOWLSubClassAxiom(u, d));
			examples.add(  factory.getOWLSubClassAxiom(d, e));
			examples.add(  factory.getOWLSubClassAxiom(c, k));
			examples.add( factory.getOWLSubClassAxiom(k, factory.getOWLObjectComplementOf(h)));
			examples.add( factory.getOWLSubClassAxiom(b, h));
			OWLOntology example = manager.createOntology(new HashSet<OWLAxiom>(examples));
			Map<OWLAxiom, Integer  > axiomMap = new HashMap<OWLAxiom, Integer >();
			for(int i = 1; i<=examples.size(); i++){
				
				axiomMap.put(examples.get(i - 1), Integer.valueOf(i));
			}
			
			
			
			
			Reasoner reasoner = new PelletReasonerFactory().createReasoner(manager);
			reasoner.loadOntologies(Collections.singleton(example));
			SyntacticRelevanceBasedExplanationGenerator expGen = 
				new SyntacticRelevanceBasedExplanationGenerator(reasoner, manager);
			
			System.out.print("J = {");
			for(Set<OWLAxiom> explanation : expGen.getExplanations(u, Strategie.CM_Just_Relevance)){
				System.out.print("{");
				for(OWLAxiom ax : explanation){
					System.out.print(axiomMap.get(ax));
					System.out.print(",");
				}
				System.out.print("}, ");
			}
			System.out.print("}");
				
			
			
			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
