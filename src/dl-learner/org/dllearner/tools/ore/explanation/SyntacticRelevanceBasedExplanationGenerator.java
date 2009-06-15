package org.dllearner.tools.ore.explanation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.model.UnknownOWLOntologyException;

import com.clarkparsia.explanation.PelletExplanation;

public class SyntacticRelevanceBasedExplanationGenerator {
	
	private Reasoner reasoner;
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	
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
	
	public Set<Set<OWLAxiom>> getExplanations(OWLClass unsat){
		return computeRelevantJustifications(unsat);
	}
	
	private Set<Set<OWLAxiom>> computeRelevantJustifications(OWLClass unsat){
		
		OWLOntology ont = null;
		Set<Set<OWLAxiom>> justifications = new HashSet<Set<OWLAxiom>>();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		Reasoner reasoner = new PelletReasonerFactory().createReasoner(man);
		int k = 1;
		try {
			ont = man.createOntology(URI.create("file:/home/lorenz/test.owl"));
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reasoner.loadOntology(ont);
		
		Set<OWLAxiom> relevant = getSyntacticRelevantAxioms(unsat, k);
		logger.debug("step " + k + ": selected axioms: " + relevant);
		Set<OWLAxiom> hittingSets = new HashSet<OWLAxiom>();
		Set<Set<OWLAxiom>> hittingSetLocal = new HashSet<Set<OWLAxiom>>();
		try {
			man.addAxioms(ont, relevant);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		while(!relevant.isEmpty()){
			try {
				man.addAxioms(ont, relevant);
			} catch (OWLOntologyChangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reasoner.refresh();
			if(!hittingSetLocal.isEmpty()){
				
			} else if(!reasoner.isSatisfiable(unsat)){
				System.out.println(reasoner.getLoadedOntologies() + "" + reasoner.isSatisfiable(unsat));
				List<Set<Set<OWLAxiom>>> result = computeJustifications(unsat, ont);
				justifications.addAll(result.get(0));
				hittingSetLocal.addAll(result.get(1));
			}
			k++;
			relevant = getSyntacticRelevantAxioms(unsat, k);
		}
		return justifications;
	}
	
	private List<Set<Set<OWLAxiom>>> computeJustifications(OWLClass unsat, OWLOntology ont){
		Set<Set<OWLAxiom>> justifications = new HashSet<Set<OWLAxiom>>();
		PelletExplanation expGen = new PelletExplanation(manager, Collections.singleton(ont));
		Set<Set<OWLAxiom>> hittingSets = new HashSet<Set<OWLAxiom>>();
		Set<Set<OWLAxiom>> hittingSets1 = new HashSet<Set<OWLAxiom>>();
		
		Set<OWLAxiom> justification = expGen.getUnsatisfiableExplanation(unsat);
		System.out.println(justification);
		justifications.add(justification);
		for(OWLAxiom ax : justification){
			hittingSets1.add(Collections.singleton(ax));
		}
		
		while(true){
			Set<Set<OWLAxiom>> hittingSets2 = new HashSet<Set<OWLAxiom>>();
			for(Set<OWLAxiom> axioms : hittingSets1){
				try {
					for(OWLAxiom ax : axioms){
						manager.applyChange(new RemoveAxiom(ont, ax));
					}
					
					if(reasoner.isSatisfiable(unsat)){
						hittingSets.add(axioms);
					} else {
						hittingSets2.add(axioms);
					}
					manager.addAxioms(ont, axioms);
				} catch (OWLOntologyChangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(hittingSets1.isEmpty() || hittingSets2.isEmpty()){
				List<Set<Set<OWLAxiom>>> result = new ArrayList<Set<Set<OWLAxiom>>>();
				result.add(justifications);
				result.add(hittingSets);
				return result;
			}
			hittingSets1.clear();
			for(Set<OWLAxiom> axioms : hittingSets2){
				try {
					for(OWLAxiom ax : axioms){
						manager.applyChange(new RemoveAxiom(ont, ax));
					}
					expGen = new PelletExplanation(manager, Collections.singleton(ont));
					Set<OWLAxiom> just = expGen.getUnsatisfiableExplanation(unsat);
					justifications.add(just);
					for(OWLAxiom a : just){
						Set<OWLAxiom> temp = new HashSet<OWLAxiom>(axioms);
						temp.add(a);
						hittingSets1.add(temp);
					}
					manager.addAxioms(ont, axioms);
				} catch (OWLOntologyChangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	
	public Set<OWLAxiom> getSyntacticRelevantAxioms(OWLClass cl, int k){
		
		Set<OWLAxiom> relevantAxioms = new HashSet<OWLAxiom>(); 
		
		if(k == 1){
			for(OWLAxiom ax : ontology.getLogicalAxioms()){
				if(ax.getSignature().contains(cl)){
					relevantAxioms.add(ax);
				}
			}
			
		} else {
			Set<OWLAxiom> axioms = getSyntacticRelevantAxioms(cl, k - 1);
		
			for(OWLAxiom ax1 : axioms){
				
				for(OWLAxiom ax2 : ontology.getLogicalAxioms()){
					
					if(areSyntacticRelevant(ax1, ax2)){
						
						relevantAxioms.add(ax2);
					}
				}
			}
			for(int i = k - 1; i>= 1 ;i--){
				relevantAxioms.removeAll(getSyntacticRelevantAxioms(cl,i));
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
		URI classURI = URI.create(base + "#Koala");
		
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory factory = manager.getOWLDataFactory();
			OWLClass cl = factory.getOWLClass(classURI);
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(file);
			
			OWLClass cl1 = factory.getOWLClass(URI.create("Manager"));
			OWLClass cl2 = factory.getOWLClass(URI.create("Employee"));
			OWLClass cl3 = factory.getOWLClass(URI.create("JobPosition"));
			OWLClass cl4 = factory.getOWLClass(URI.create("Leader"));
			OWLClass cl5 = factory.getOWLClass(URI.create("Situation"));
			OWLClass cl6 = factory.getOWLClass(URI.create("Happening"));
			OWLClass cl7 = factory.getOWLClass(URI.create("Patent"));
			OWLIndividual ind = factory.getOWLIndividual(URI.create("lectureship"));
			Set<OWLAxiom> examples = new HashSet<OWLAxiom>();
			examples.add(factory.getOWLSubClassAxiom(cl1, cl2));
			examples.add(factory.getOWLSubClassAxiom(cl2, cl3));
			examples.add(factory.getOWLSubClassAxiom(cl4, cl3));
			examples.add(factory.getOWLSubClassAxiom(cl3, cl5));
			examples.add(factory.getOWLSubClassAxiom(cl5, cl6));
			examples.add(factory.getOWLSubClassAxiom(cl4, factory.getOWLObjectComplementOf(cl7)));
			examples.add(factory.getOWLSubClassAxiom(cl6, factory.getOWLObjectComplementOf(cl1)));
			examples.add(factory.getOWLSubClassAxiom(cl3, factory.getOWLObjectComplementOf(cl2)));
			examples.add(factory.getOWLClassAssertionAxiom(ind, cl3));
			
			OWLOntology example = manager.createOntology(examples);
			
			
			Reasoner reasoner = new PelletReasonerFactory().createReasoner(manager);
			reasoner.loadOntologies(Collections.singleton(ontology));
			SyntacticRelevanceBasedExplanationGenerator expGen = 
				new SyntacticRelevanceBasedExplanationGenerator(reasoner, manager);
			
				System.out.println(expGen.getExplanations(cl));
			
			
			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
