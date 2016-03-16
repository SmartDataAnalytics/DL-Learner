package org.dllearner.algorithms.versionspace;

import org.dllearner.algorithms.versionspace.complexity.ClassExpressionDepthComplexityModel;
import org.dllearner.algorithms.versionspace.complexity.ClassExpressionLengthComplexityModel;
import org.dllearner.algorithms.versionspace.complexity.ComplexityModel;
import org.dllearner.algorithms.versionspace.complexity.HybridComplexityModel;
import org.dllearner.algorithms.versionspace.operator.ComplexityBoundedOperator;
import org.dllearner.algorithms.versionspace.operator.ComplexityBoundedOperatorALC;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Lorenz Buehmann
 */
public class ComplexityBoundedVersionSpaceGenerator extends AbstractVersionSpaceGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComplexityBoundedVersionSpaceGenerator.class);

	private ComplexityBoundedOperator operator;

	public ComplexityBoundedVersionSpaceGenerator(ComplexityBoundedOperator operator) {
		this.operator = operator;
	}

	@Override
	public VersionSpace generate() {
		LOGGER.info("Generating version space...");
		LOGGER.info("#classes:" + operator.getReasoner().getClasses().size());
		LOGGER.info("#ops:" + operator.getReasoner().getObjectProperties().size());
		LOGGER.info("#dps:" + operator.getReasoner().getDatatypeProperties().size());
		long startTime = System.currentTimeMillis();

		// the root node is owl:Thing
		DefaultVersionSpaceNode rootNode = new DefaultVersionSpaceNode(topConcept);

		// create the version space
		final VersionSpace versionSpace = new VersionSpace(rootNode);

		// keep track of already visited(refined) nodes
		final Set<DefaultVersionSpaceNode> visited = new HashSet<>();

		// the list of nodes we have to process
		Queue<DefaultVersionSpaceNode> todo = new ArrayDeque<>();
		todo.add(rootNode);

		while(!todo.isEmpty()) {
			// pick next node to process
			DefaultVersionSpaceNode parent = todo.poll();

			// compute all refinements
			Set<OWLClassExpression> refinements = operator.refine(parent.getHypothesis());

			// add child node and edge to parent for each refinement
			for (OWLClassExpression ref : refinements) {
				DefaultVersionSpaceNode child = new DefaultVersionSpaceNode(ref);

				if(!child.equals(parent)) {
					versionSpace.addVertex(child);
					versionSpace.addEdge(parent, child);
				}

				// add to todo list only if not already processed before
				if(!visited.contains(child)) {
					todo.add(child);
				}
			}
			visited.add(parent);
		}

//		versionSpace.vertexSet().forEach(v -> System.out.println(v));

		// perform pruning, e.g. combine semantically equivalent concepts


//		final BlockingQueue<DefaultVersionSpaceNode> todoQueue = new ArrayBlockingQueue<DefaultVersionSpaceNode>(1024);
//		todoQueue.add(rootNode);
//		ThreadPoolExecutor tp = (ThreadPoolExecutor)Executors.newFixedThreadPool(4);
//
//		while(!todoQueue.isEmpty() || tp.getActiveCount() > 0) {
//			tp.submit(new Runnable() {
//				@Override
//				public void run() {
//					// pick next node to process
//					try {
//						DefaultVersionSpaceNode parent = todoQueue.take();
//
//						System.out.println(Thread.currentThread().getId() + "::" + parent.getHypothesis());
//
//						// compute all refinements
//						Set<OWLClassExpression> refinements = operator.refine(parent.getHypothesis());
//
//						// add child node and edge to parent for each refinement
//						for (OWLClassExpression ref : refinements) {
//							DefaultVersionSpaceNode child = new DefaultVersionSpaceNode(ref);
//
//							if(!child.equals(parent)) {
//								versionSpace.addVertex(child);
//								versionSpace.addEdge(parent, child);
//							}
//
//							// add to todo list only if not already processed before
//							if(!visited.contains(child)) {
//								todoQueue.put(child);
//							}
//						}
//						visited.add(parent);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			});
//		}
//
//		tp.shutdown();
//		try {
//			tp.awaitTermination(1, TimeUnit.HOURS);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}


		LOGGER.info("...finished generating version space(#nodes: {}) in {}ms.",
					versionSpace.vertexSet().size(), (System.currentTimeMillis() - startTime));
		return versionSpace;
	}

	public static void main(String[] args) throws Exception{
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ont = man.loadOntologyFromOntologyDocument(new File("../examples/father.owl"));

		AbstractReasonerComponent reasoner = new ClosedWorldReasoner(new OWLAPIOntology(ont));
		reasoner.init();

		ComplexityModel complexityModel = new HybridComplexityModel(
				new ClassExpressionLengthComplexityModel(7),
				new ClassExpressionDepthComplexityModel(2)
		);

		ComplexityBoundedOperator op = new ComplexityBoundedOperatorALC(reasoner, complexityModel);
		op.init();

		VersionSpaceGenerator generator = new ComplexityBoundedVersionSpaceGenerator(op);
		VersionSpace g = generator.generate();

		GraphUtils.writeGraphML(g, "/tmp/versionspace.graphml");

//		Set<OWLClassExpression> refinements = new TreeSet<>();
//		refinements.add(man.getOWLDataFactory().getOWLThing());
//
//		Set<OWLClassExpression> tmp;
//		do {
//			tmp = new HashSet<>();
//			for (OWLClassExpression ce : refinements) {
//				tmp.addAll(op.refine(ce));
//			}
//		} while(!tmp.isEmpty() && refinements.addAll(tmp));
//
//		System.out.println("#Refinements: " + refinements.size());
//		for (OWLClassExpression ref : refinements) {
//			System.out.println(ref);
//		}
	}
}
