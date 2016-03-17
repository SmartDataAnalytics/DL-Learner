package org.dllearner.algorithms.versionspace;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.dllearner.algorithms.versionspace.complexity.ClassExpressionDepthComplexityModel;
import org.dllearner.algorithms.versionspace.complexity.ClassExpressionLengthComplexityModel;
import org.dllearner.algorithms.versionspace.complexity.ComplexityModel;
import org.dllearner.algorithms.versionspace.complexity.HybridComplexityModel;
import org.dllearner.algorithms.versionspace.operator.ComplexityBoundedOperator;
import org.dllearner.algorithms.versionspace.operator.ComplexityBoundedOperatorALC;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.utilities.datastructures.UniqueQueue;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.File;
import java.util.*;

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
		VersionSpace<DefaultVersionSpaceNode> versionSpace = new VersionSpace(rootNode);

		// keep track of already visited(refined) nodes
		final Set<DefaultVersionSpaceNode> visited = new HashSet<>();

		// the list of nodes we have to process
		Queue<DefaultVersionSpaceNode> todo = new UniqueQueue<>();
		todo.add(rootNode);

		int i = 0;
		Monitor mon = MonitorFactory.getTimeMonitor("ref");
		while(!todo.isEmpty()) {
			// pick next node to process
			DefaultVersionSpaceNode parent = todo.poll();

			// compute all refinements
			mon.start();
			Set<OWLClassExpression> refinements = operator.refine(parent.getHypothesis());
			mon.stop();
//			System.out.println(parent.getHypothesis() + ":" + refinements);

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
			if(i++ % 1000 == 0) {
				System.out.println(i + ":" + todo.size() + "  avg_t(rho)=" + mon.getAvg() + "ms");
			}
		}

		LOGGER.info("#nodes(before pruning): {}", versionSpace.vertexSet().size());


		LOGGER.info("performing version space pruning...");
		// perform pruning, e.g. combine semantically equivalent concepts
		// 1. apply syntactic rules
		Multimap<DefaultVersionSpaceNode, DefaultVersionSpaceNode> map = HashMultimap.create();
		Map<DefaultVersionSpaceNode, DefaultVersionSpaceNode> node2NewNode = new HashMap<>();
		OWLClassExpressionMinimizer minimizer = new OWLClassExpressionMinimizer(new OWLDataFactoryImpl(), operator.getReasoner());
		for (DefaultVersionSpaceNode node : versionSpace.vertexSet()) {
			OWLClassExpression hypothesis = node.getHypothesis();
			OWLClassExpression cleanedHypothesis = ConceptTransformation.applyEquivalenceRules(hypothesis);
			cleanedHypothesis = minimizer.minimizeClone(cleanedHypothesis);
//			System.out.println(hypothesis + " --> " + cleanedHypothesis);
			DefaultVersionSpaceNode newNode = new DefaultVersionSpaceNode(cleanedHypothesis);
			map.put(newNode, node);
			node2NewNode.put(node, newNode);
		}

		// merge the nodes that contain the same hypothesis
		VersionSpace<DefaultVersionSpaceNode> prunedVersionSpace = new VersionSpace(rootNode);
//		for (Map.Entry<DefaultVersionSpaceNode, DefaultVersionSpaceNode> entry : map.entries()) {
//			DefaultVersionSpaceNode newNode = entry.getKey();
//			prunedVersionSpace.addVertex(newNode);
//
//			DefaultVersionSpaceNode oldNodes = entry.getValue();
//
//
//		}
		for (Map.Entry<DefaultVersionSpaceNode, DefaultVersionSpaceNode> entry : node2NewNode.entrySet()) {
			DefaultVersionSpaceNode node = entry.getKey();
			DefaultVersionSpaceNode newNode = entry.getValue();

			prunedVersionSpace.addVertex(newNode);
			Set<DefaultEdge> inEdges = versionSpace.incomingEdgesOf(node);
			for (DefaultEdge edge : inEdges) {
				DefaultVersionSpaceNode oldParent = versionSpace.getEdgeSource(edge);
				DefaultVersionSpaceNode newParent = node2NewNode.get(oldParent);
				prunedVersionSpace.addVertex(newParent);

				prunedVersionSpace.addEdge(newNode, newParent);
			}
		}
		versionSpace = prunedVersionSpace;

		// restructure the graph, i.e. we have only one node

		// 2. use reasoner


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
