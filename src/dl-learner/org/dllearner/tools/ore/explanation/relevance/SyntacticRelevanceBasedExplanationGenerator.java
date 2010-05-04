package org.dllearner.tools.ore.explanation.relevance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.tools.ore.explanation.AxiomSelector;
import org.mindswap.pellet.utils.SetUtils;
import org.mindswap.pellet.utils.Timer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapiv3.OntologyUtils;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class SyntacticRelevanceBasedExplanationGenerator {

	private PelletReasoner reasoner;
	private PelletReasoner localReasoner;
	private OWLOntologyManager manager;
	private OWLOntology ontology;

	public static enum Strategie {
		All_Just_Relevance, CM_Just_Relevance;
	}

	private Strategie strategie;
	private Set<Set<OWLAxiom>> justifications;
	private Set<HittingSet> hittingSets;

	private static Logger logger = Logger.getRootLogger();

	public SyntacticRelevanceBasedExplanationGenerator(PelletReasoner reasoner, OWLOntologyManager manager) {
		this.reasoner = reasoner;
		this.manager = manager;
		this.ontology = reasoner.getRootOntology();

		justifications = new HashSet<Set<OWLAxiom>>();
		hittingSets = new HashSet<HittingSet>();

		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.addAppender(consoleAppender);

		FileAppender fileAppender = null;
		try {
			fileAppender = new FileAppender(layout, "log/relevance.log", false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.addAppender(fileAppender);
		logger.setLevel(Level.DEBUG);

	}

	public Set<Set<OWLAxiom>> getExplanations(OWLAxiom entailment, Strategie strategie) {
		this.strategie = strategie;

		return rel_all_just(entailment);
	}

	public Set<Set<OWLAxiom>> getUnsatisfiableExplanations(OWLClass unsat, Strategie strategie) {
		this.strategie = strategie;
		OWLDataFactory factory = manager.getOWLDataFactory();

		return rel_all_just(factory.getOWLSubClassOfAxiom(unsat, factory.getOWLNothing()));
	}

	private Set<Set<OWLAxiom>> computeRelevantJustifications(OWLClass unsat) {

		OWLOntology ont = null;
		Set<Set<OWLAxiom>> justifications = new HashSet<Set<OWLAxiom>>();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();

		int k = 1;
		try {
			ont = man.createOntology(IRI.create("file:/home/lorenz/test.owl"));
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		localReasoner = new PelletReasonerFactory().createReasoner(ont);

		Set<OWLAxiom> relevant = AxiomSelector.getSyntacticRelevantAxioms(ontology, unsat);// getSyntacticRelevantAxioms(unsat,
																							// k);

		Set<HittingSet> hittingSets = new HashSet<HittingSet>();
		Set<HittingSet> hittingSetLocal = new HashSet<HittingSet>();
		try {
			man.addAxioms(ont, relevant);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (!relevant.isEmpty()) {
			logger.debug("step " + k + ": selected axioms: " + relevant);
			try {
				man.addAxioms(ont, relevant);
			} catch (OWLOntologyChangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reasoner.refresh();
			if (!hittingSetLocal.isEmpty()) {// checking for global hitting sets
				for (HittingSet hit : hittingSetLocal) {
					try {
						for (OWLAxiom ax : hit) {
							manager.applyChange(new RemoveAxiom(ontology, ax));
						}
						reasoner.refresh();

						if (reasoner.isSatisfiable(unsat)) {
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
				if ((!strategie.equals(Strategie.All_Just_Relevance) && !hittingSets.isEmpty())
						|| (hittingSetLocal.isEmpty())) {
					logger.debug("early termination");
					System.out.println(hittingSets);
					return justifications;
				}
				Set<HittingSet> temp = new HashSet<HittingSet>(hittingSetLocal);
				for (HittingSet hit : temp) {
					try {
						for (OWLAxiom ax : hit) {
							man.applyChange(new RemoveAxiom(ont, ax));
						}
						List<Set<? extends Set<OWLAxiom>>> result = computeJustifications(unsat, ont, hit,
								justifications, k);
						justifications.addAll(result.get(0));
						Set<HittingSet> localTemp = (Set<HittingSet>) result.get(1);
						for (HittingSet h : localTemp) {
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

			} else if (!localReasoner.isSatisfiable(unsat)) {

				List<Set<? extends Set<OWLAxiom>>> result = computeJustifications(unsat, ont, null, null, k);
				logger.debug("step " + k + ": justifications computed : " + result.get(0));
				justifications.addAll(result.get(0));
				hittingSetLocal.addAll((Set<HittingSet>) result.get(1));
			}
			k++;
			relevant = AxiomSelector.getSyntacticRelevantAxioms(ontology, relevant);// getSyntacticRelevantAxioms(unsat,
																					// k);
		}
		return justifications;
	}

	private List<Set<? extends Set<OWLAxiom>>> computeJustifications(OWLClass unsat, OWLOntology ont, HittingSet path,
			Set<Set<OWLAxiom>> justifications, int step) {

		Set<Set<OWLAxiom>> newJustifications = new HashSet<Set<OWLAxiom>>();
		PelletExplanation expGen = new PelletExplanation(ont);
		Set<HittingSet> hittingSets = new HashSet<HittingSet>();
		Set<HittingSet> hittingSets1 = new HashSet<HittingSet>();
		Set<OWLAxiom> justification = null;
		if (path != null && !justifications.isEmpty()) {
			for (Set<OWLAxiom> just : justifications) {
				if (!SetUtils.intersects(path, just)) {
					justification = just;
					logger.debug("using justification reuse: " + just);
					break;
				}
			}
		}
		if (justification == null) {
			justification = expGen.getUnsatisfiableExplanation(unsat);
		}

		newJustifications.add(justification);
		for (OWLAxiom ax : justification) {
			hittingSets1.add(new HittingSet(ax));
		}

		while (true) {
			Set<HittingSet> hittingSets2 = new HashSet<HittingSet>();
			for (HittingSet hit : hittingSets1) {
				try {
					for (OWLAxiom ax : hit) {
						manager.applyChange(new RemoveAxiom(ont, ax));
					}
					localReasoner.refresh();

					if (localReasoner.isSatisfiable(unsat)) {
						hittingSets.add(hit);
						logger.debug("step " + step + ": found local hitting set: " + hit);
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
			if ((!strategie.equals(Strategie.All_Just_Relevance) && !hittingSets.isEmpty()) || hittingSets1.isEmpty()
					|| hittingSets2.isEmpty()) {
				List<Set<? extends Set<OWLAxiom>>> result = new ArrayList<Set<? extends Set<OWLAxiom>>>();
				result.add(newJustifications);
				result.add(hittingSets);
				return result;
			}
			hittingSets1.clear();
			for (HittingSet hit2 : hittingSets2) {
				try {
					for (OWLAxiom ax : hit2) {
						manager.applyChange(new RemoveAxiom(ont, ax));
					}
					// justification reuse
					Set<OWLAxiom> just = null;
					if (!newJustifications.isEmpty()) {
						for (Set<OWLAxiom> jus : newJustifications) {
							if (!SetUtils.intersects(hit2, jus)) {
								just = jus;
								logger.debug("using justification reuse: " + just);
								break;
							}
						}
					}
					if (just == null) {
						expGen = new PelletExplanation(ont);
						just = expGen.getUnsatisfiableExplanation(unsat);
					}
					// ///////////////////

					newJustifications.add(just);
					for (OWLAxiom a : just) {
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

	public Set<Set<OWLAxiom>> rel_all_just(OWLAxiom entailment) {
		try {
			justifications.clear();
			Set<HittingSet> hittingSetsGlobal = new HashSet<HittingSet>();
			Set<HittingSet> hittingSetsLocal = new HashSet<HittingSet>();

			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontologyTemp = man.createOntology(Collections.<OWLAxiom> emptySet());
			localReasoner = new PelletReasonerFactory().createReasoner(ontologyTemp);
			int k = 1;
			Set<OWLAxiom> selectedAxioms = AxiomSelector.getSyntacticRelevantAxioms(ontology, entailment);

			while (!selectedAxioms.isEmpty()) {
				OntologyUtils.addAxioms(ontologyTemp, selectedAxioms);
				localReasoner.refresh();
				logger.debug("Step " + k + ": selected axioms " + selectedAxioms);
				if (!hittingSetsLocal.isEmpty()) {
					for (HittingSet hit : hittingSetsLocal) {
						OntologyUtils.removeAxioms(ontology, hit);
						reasoner.refresh();
						if (!reasoner.isEntailed(entailment)) {
							hittingSetsGlobal.add(hit);
							logger.debug("Step " + k + ": global hitting set found");
						}
						OntologyUtils.addAxioms(ontology, hit);
					}
					hittingSetsLocal.removeAll(hittingSetsGlobal);
					if (hittingSetsLocal.isEmpty()) {
						logger.info("Step " + k + ": early termination");
						return justifications;
					}
					Set<HittingSet> hittingSetsTemp = new HashSet<HittingSet>(hittingSetsLocal);
					for (HittingSet hit : hittingSetsTemp) {
						logger.debug("Step " + k + ": expanding branch " + hit);
						OntologyUtils.removeAxioms(ontologyTemp, hit);
						for (HittingSet h : expand_HST(entailment, ontologyTemp, hit, k)) {
							h.addAll(hit);
							hittingSetsLocal.add(h);
						}
						hittingSetsLocal.remove(hit);

					}
				} else if (localReasoner.isEntailed(entailment)) {
					hittingSetsLocal.addAll(expand_HST(entailment, ontologyTemp, new HittingSet(), k));
				}
				k++;

				selectedAxioms = AxiomSelector.getSyntacticRelevantAxioms(ontology, selectedAxioms);
			}

		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return justifications;

	}

	public Set<HittingSet> expand_HST(OWLAxiom entailment, OWLOntology ont, HittingSet currentPath, int k) {

		Set<HittingSet> hittingSets = new HashSet<HittingSet>();
		Set<HittingSet> hittingSets1 = new HashSet<HittingSet>();
		Set<HittingSet> hittingSets2 = new HashSet<HittingSet>();
		Set<Set<OWLAxiom>> currentJustifications = new HashSet<Set<OWLAxiom>>();
		PelletExplanation pellet = new PelletExplanation(ont);
		Set<OWLAxiom> justification = null;
		if (!justifications.isEmpty()) {
			for (Set<OWLAxiom> just : justifications) {
				if (!SetUtils.intersects(just, currentPath)) {
					logger.debug("using global ontology justification reuse:" + just);
					justification = just;
					break;
				}
			}
		}
		if (k == 2) {
			justification = new HashSet<OWLAxiom>();
			OWLDataFactory factory = manager.getOWLDataFactory();
			OWLClass z = factory.getOWLClass(IRI.create("z"));
			OWLClass a1 = factory.getOWLClass(IRI.create("a1"));
			OWLClass a2 = factory.getOWLClass(IRI.create("a2"));
			OWLClass a3 = factory.getOWLClass(IRI.create("a3"));
			OWLClass p1 = factory.getOWLClass(IRI.create("p1"));
			OWLClass p2 = factory.getOWLClass(IRI.create("p2"));
			OWLClass q1 = factory.getOWLClass(IRI.create("q1"));
			OWLClass q2 = factory.getOWLClass(IRI.create("q2"));
			justification.add(factory.getOWLSubClassOfAxiom(a1, factory.getOWLObjectIntersectionOf(p1, q1, z)));
			justification.add(factory.getOWLSubClassOfAxiom(p1, factory.getOWLObjectIntersectionOf(a2, z)));
			justification.add(factory.getOWLSubClassOfAxiom(a2, factory.getOWLObjectIntersectionOf(p2, q2, z)));
			justification.add(factory.getOWLSubClassOfAxiom(p2, factory.getOWLObjectIntersectionOf(a3, z)));
		}
		if (justification == null) {
			justification = pellet.getEntailmentExplanation(entailment);
			if (!justification.isEmpty()) {
				justifications.add(justification);
				logger.debug("new justification computed: " + justification);
			}

		}

		for (OWLAxiom ax : justification) {
			hittingSets1.add(new HittingSet(ax));
		}

		while (true) {
			hittingSets2.clear();
			for (HittingSet hit : hittingSets1) {
				boolean earlyPathTermination = false;
				for (HittingSet h : hittingSets) {
					if (SetUtils.intersects(h, hit)) {
						earlyPathTermination = true;
						logger.info("early path termination:" + hit + " intersects " + h);
						break;
					}
				}
				if (!earlyPathTermination) {
					OntologyUtils.removeAxioms(ont, hit);
					localReasoner.refresh();
					if (!localReasoner.isEntailed(entailment)) {
						hittingSets.add(hit);
						logger.debug("local hitting set found:" + hit);
					} else {
						hittingSets2.add(hit);
						logger.debug("adding new branch " + hit);
					}
					OntologyUtils.addAxioms(ont, hit);
				}
			}
			if (hittingSets1.isEmpty() || hittingSets2.isEmpty()) {
				return hittingSets;
			}
			hittingSets1.clear();
			for (HittingSet hit : hittingSets2) {
				logger.debug("current path: " + hit);
				// for(HittingSet h : hittingSets){
				// if(SetUtils.intersects(h, hit)){
				// logger.info("early path termination:" + hit + " intersects "
				// + h);break;
				// }
				// }
				justification = null;
				if (!currentJustifications.isEmpty()) {
					for (Set<OWLAxiom> just : currentJustifications) {
						if (!SetUtils.intersects(just, hit)) {
							logger.debug("using sub ontology justification reuse: " + just);
							justification = just;
							break;
						}
					}
				}
				if (justification == null) {
					OntologyUtils.removeAxioms(ont, hit);
					pellet = new PelletExplanation(ont);
					justification = pellet.getEntailmentExplanation(entailment);
					currentJustifications.add(justification);
					justifications.add(justification);
					OntologyUtils.addAxioms(ont, hit);
					logger.debug("new justification computed: " + justification);
				}

				for (OWLAxiom ax : justification) {
					HittingSet h = new HittingSet();
					h.addAll(hit);
					h.add(ax);
					hittingSets1.add(h);
					System.out.println("new branch" + h);
					// hit.remove(ax);
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

	private boolean areSyntacticRelevant(OWLAxiom ax1, OWLAxiom ax2) {
		return org.mindswap.pellet.utils.SetUtils.intersects(ax1.getSignature(), ax2.getSignature());
	}

	public static void main(String[] args) {
		IRI fileIRI = IRI.create("http://krono.act.uji.es/Links/ontologies/tambis.owl/at_download/file");
		String base = "http://krono.act.uji.es/Links/ontologies/tambis.owl#";
		IRI classIRI = IRI.create(base + "metal");

		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory factory = manager.getOWLDataFactory();
			OWLClass cl = factory.getOWLClass(classIRI);
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(fileIRI);

			// PelletExplanation exp1 = new PelletExplanation(manager,
			// Collections.singleton(ontology));
			//			
			// Timer t1 = new Timer("pellet");
			// t1.start();
			// exp1.getUnsatisfiableExplanations(cl,5);
			// t1.stop();
			// Timer t2 = new Timer("module");
			// t2.start();
			// OWLOntology module =
			// OntologyUtils.getOntologyFromAxioms(ModularityUtils.extractModule(ontology,
			// cl.getSignature(), ModuleType.TOP_OF_BOT));
			// System.out.println(module.getLogicalAxiomCount());
			// PelletExplanation exp2 = new PelletExplanation(manager,
			// Collections.singleton(module));
			// exp2.getUnsatisfiableExplanations(cl,5);
			// t2.stop();
			// System.out.println(t1.getTotal() +"-- " + t2.getTotal());

			// OWLClass cl1 = factory.getOWLClass(URI.create("Manager"));
			// OWLClass cl2 = factory.getOWLClass(URI.create("Employee"));
			// OWLClass cl3 = factory.getOWLClass(URI.create("JobPosition"));
			// OWLClass cl4 = factory.getOWLClass(URI.create("Leader"));
			// OWLClass cl5 = factory.getOWLClass(URI.create("Situation"));
			// OWLClass cl6 = factory.getOWLClass(URI.create("Happening"));
			// OWLClass cl7 = factory.getOWLClass(URI.create("Patent"));
			// OWLIndividual ind =
			// factory.getOWLIndividual(URI.create("lectureship"));
			// Set<OWLAxiom> examples = new HashSet<OWLAxiom>();
			// examples.add(factory.getOWLSubClassAxiom(cl1, cl2));
			// examples.add(factory.getOWLSubClassAxiom(cl2, cl3));
			// examples.add(factory.getOWLSubClassAxiom(cl4, cl3));
			// examples.add(factory.getOWLSubClassAxiom(cl3, cl5));
			// examples.add(factory.getOWLSubClassAxiom(cl5, cl6));
			// examples.add(factory.getOWLSubClassAxiom(cl4,
			// factory.getOWLObjectComplementOf(cl7)));
			// examples.add(factory.getOWLSubClassAxiom(cl6,
			// factory.getOWLObjectComplementOf(cl1)));
			// examples.add(factory.getOWLSubClassAxiom(cl3,
			// factory.getOWLObjectComplementOf(cl2)));
			// examples.add(factory.getOWLClassAssertionAxiom(ind, cl3));
			// OWLOntology example = manager.createOntology(examples);

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
			examples.add(factory.getOWLSubClassOfAxiom(u, a));
			examples.add(factory.getOWLSubClassOfAxiom(u, factory.getOWLObjectComplementOf(a)));
			examples.add(factory.getOWLSubClassOfAxiom(u, c));
			examples.add(factory.getOWLSubClassOfAxiom(c, factory.getOWLObjectComplementOf(b)));
			examples.add(factory.getOWLSubClassOfAxiom(a, b));
			examples.add(factory.getOWLSubClassOfAxiom(u, g));
			examples.add(factory.getOWLSubClassOfAxiom(g, e));
			examples.add(factory.getOWLSubClassOfAxiom(u, f));
			examples.add(factory.getOWLSubClassOfAxiom(f, factory.getOWLObjectComplementOf(e)));
			examples.add(factory.getOWLSubClassOfAxiom(u, d));
			examples.add(factory.getOWLSubClassOfAxiom(d, e));
			examples.add(factory.getOWLSubClassOfAxiom(c, k));
			examples.add(factory.getOWLSubClassOfAxiom(k, factory.getOWLObjectComplementOf(h)));
			examples.add(factory.getOWLSubClassOfAxiom(b, h));
			OWLOntology example = manager.createOntology(new HashSet<OWLAxiom>(examples));
			Map<OWLAxiom, Integer> axiomMap = new HashMap<OWLAxiom, Integer>();
			for (int i = 1; i <= examples.size(); i++) {
				axiomMap.put(examples.get(i - 1), Integer.valueOf(i));
			}

			// Timer t1 = new Timer("pellet");
			// t1.start();
			// PelletExplanation exp1 = new PelletExplanation(manager,
			// Collections.singleton(ontology));
			// exp1.getUnsatisfiableExplanations(cl);
			// t1.stop();
			// Timer t3 = new Timer("module-based");
			// t3.start();
			// OWLOntology module =
			// OntologyUtils.getOntologyFromAxioms(ModularityUtils.extractModule(ontology,
			// cl.getSignature(), ModuleType.TOP_OF_BOT));
			// System.out.println(module);
			// PelletExplanation exp2 = new PelletExplanation(manager,
			// Collections.singleton(module));
			// System.out.println("Module based explanationcount: " +
			// exp2.getUnsatisfiableExplanations(cl));
			// t3.stop();

			Timer t2 = new Timer("syntactic relevance");
			t2.start();
			PelletReasoner reasoner = new PelletReasonerFactory().createReasoner(example);
			SyntacticRelevanceBasedExplanationGenerator exp3 = new SyntacticRelevanceBasedExplanationGenerator(
					reasoner, manager);
			System.out.println(exp3.getUnsatisfiableExplanations(u, Strategie.All_Just_Relevance));

			System.out.print("J = {");
			// for(Set<OWLAxiom> explanation :
			// expGen.getUnsatisfiableExplanations(cl,
			// Strategie.All_Just_Relevance)){
			// System.out.print("{");
			// for(OWLAxiom ax : explanation){
			// System.out.print(axiomMap.get(ax));
			// System.out.print(",");
			// }
			// System.out.print("}, ");
			// }
			System.out.print("}");
			t2.stop();
			// System.out.println(t1.getTotal() +"--" + t3.getTotal() + "--" +
			// t2.getTotal());

			// Set<OWLAxiom> test = new HashSet<OWLAxiom>();
			// OWLClass z = factory.getOWLClass(IRI.create("z"));
			// OWLClass a1 = factory.getOWLClass(IRI.create("a1"));
			// OWLClass a2 = factory.getOWLClass(IRI.create("a2"));
			// OWLClass a3 = factory.getOWLClass(IRI.create("a3"));
			// OWLClass p1 = factory.getOWLClass(IRI.create("p1"));
			// OWLClass p2 = factory.getOWLClass(IRI.create("p2"));
			// OWLClass q1 = factory.getOWLClass(IRI.create("q1"));
			// OWLClass q2 = factory.getOWLClass(IRI.create("q2"));
			// test.add(factory.getOWLSubClassOfAxiom(a1,
			// factory.getOWLObjectIntersectionOf(p1, q1, z)));
			// test.add(factory.getOWLSubClassOfAxiom(a2,
			// factory.getOWLObjectIntersectionOf(p2, q2, z)));
			// test.add(factory.getOWLSubClassOfAxiom(p1,
			// factory.getOWLObjectIntersectionOf(a2, z)));
			// test.add(factory.getOWLSubClassOfAxiom(p2,
			// factory.getOWLObjectIntersectionOf(a3, z)));
			// test.add(factory.getOWLSubClassOfAxiom(q1,
			// factory.getOWLObjectIntersectionOf(a2, z)));
			// test.add(factory.getOWLSubClassOfAxiom(q2,
			// factory.getOWLObjectIntersectionOf(a3, z)));
			// OWLOntology testOntology = manager.createOntology(test);
			// OWLAxiom ax = factory.getOWLSubClassOfAxiom(a1, a3);
			//			
			// Reasoner reasoner = new
			// PelletReasonerFactory().createReasoner(manager);
			// reasoner.loadOntologies(manager.getImportsClosure(testOntology));
			// SyntacticRelevanceBasedExplanationGenerator expGen = new
			// SyntacticRelevanceBasedExplanationGenerator(reasoner, manager);
			//			
			// System.out.println(expGen.getExplanations(ax,
			// Strategie.All_Just_Relevance));
			//			

		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
