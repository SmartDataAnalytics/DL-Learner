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
import org.dllearner.utilities.TreeUtils;
import org.dllearner.utilities.datastructures.SearchTree;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.*;

/**
 * @author Lorenz Buehmann
 *         created on 2/11/16
 */
public class ComplexityBoundedVersionSpaceGenerator extends AbstractVersionSpaceGenerator {

	private ComplexityBoundedOperator operator;

	public ComplexityBoundedVersionSpaceGenerator(ComplexityBoundedOperator operator) {
		this.operator = operator;
	}

	@Override
	public RootedDirectedGraph generate() {
		RootedDirectedGraph searchTree = new RootedDirectedGraph(topConcept);

		Set<OWLClassExpression> visited = new HashSet<>();

		Queue<OWLClassExpression> todo = new ArrayDeque<>();
		todo.add(topConcept);

		while(!todo.isEmpty()) {
			OWLClassExpression parent = todo.poll();

			Set<OWLClassExpression> refinements = operator.refine(parent);

			for (OWLClassExpression ref : refinements) {
				if(!ref.equals(parent)) {
					searchTree.addVertex(ref);
					searchTree.addEdge(parent, ref);
				}

				// add to todo list only if not already processed before
				if(visited.add(ref)) {
					todo.add(ref);
				}
			}
			visited.add(parent);

		}

		return searchTree;
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

		ComplexityBoundedOperator op = new ComplexityBoundedOperatorALC(reasoner);
		op.setComplexityModel(complexityModel);
		op.init();

		VersionSpaceGenerator generator = new ComplexityBoundedVersionSpaceGenerator(op);
		RootedDirectedGraph g = generator.generate();

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
