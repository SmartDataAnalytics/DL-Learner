package org.dllearner.algorithms.versionspace;

import org.dllearner.algorithms.versionspace.complexity.ClassExpressionDepthComplexityModel;
import org.dllearner.algorithms.versionspace.complexity.ClassExpressionLengthComplexityModel;
import org.dllearner.algorithms.versionspace.complexity.ComplexityModel;
import org.dllearner.algorithms.versionspace.complexity.HybridComplexityModel;
import org.dllearner.algorithms.versionspace.operator.ComplexityBoundedOperator;
import org.dllearner.algorithms.versionspace.operator.ComplexityBoundedOperatorALC;
import org.dllearner.core.*;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public class VersionSpaceLearningAlgorithm extends AbstractCELA {

	private VersionSpaceGenerator versionSpaceGenerator;

	private VersionSpace<DefaultVersionSpaceNode> versionSpace;

	private ComplexityModel complexityModel;
	private ComplexityBoundedOperator operator;

	private RandomNodePicker<DefaultVersionSpaceNode> exploitation;

	private Set<OWLClassExpression> classesToIgnore = new HashSet<>();

	public VersionSpaceLearningAlgorithm(
			AbstractClassExpressionLearningProblem learningProblem,
			AbstractReasonerComponent reasoner,
			ComplexityModel complexityModel) {
		super(learningProblem, reasoner);

		this.complexityModel = complexityModel;
	}

	@Override
	public void init() throws ComponentInitException {
		// generate the refinement operator
		operator = new ComplexityBoundedOperatorALC(reasoner, complexityModel);
		operator.init();

		// create the version space generator
		versionSpaceGenerator = new ComplexityBoundedVersionSpaceGenerator(operator);

		// generate the version space
		versionSpace = versionSpaceGenerator.generate();

		// the helper for exploitation
		exploitation = new RandomNodePicker<>(versionSpace);

		if(learningProblem.getClass().isAssignableFrom(ClassLearningProblem.class)) {
			classesToIgnore.add(((ClassLearningProblem)learningProblem).getClassToDescribe());
		}
	}

	@Override
	public void start() {
		nanoStartTime = System.nanoTime();

		while(!isTimeExpired()) {

			DefaultVersionSpaceNode currentNode = exploitation.selectRandomNode();

			EvaluatedDescription<? extends Score> evaluatedConcept = learningProblem.evaluate(currentNode.getHypothesis());

			currentNode.setScore(evaluatedConcept.getScore());

			if(!classesToIgnore.contains(evaluatedConcept.getDescription()) &&
					(getCurrentlyBestEvaluatedDescriptions().isEmpty() ||
					evaluatedConcept.getAccuracy() > getCurrentlyBestEvaluatedDescription().getAccuracy())) {
				System.out.println("Found better solution: " + evaluatedConcept);
			}
			bestEvaluatedDescriptions.add(evaluatedConcept);
		}
	}

	public static void main(String[] args) throws Exception{
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();

		OWLOntology ont = man.loadOntologyFromOntologyDocument(new File("../examples/family/father_oe.owl"));
		OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://example.com/father#father"));

//		OWLOntology ont = man.loadOntologyFromOntologyDocument(new File("../examples/swore/swore.rdf"));
//		OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://ns.softwiki.de/req/CustomerRequirement"));

		AbstractReasonerComponent reasoner = new ClosedWorldReasoner(new OWLAPIOntology(ont));
		reasoner.init();

		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(classToDescribe);
		lp.init();

		ComplexityModel complexityModel = new HybridComplexityModel(
				new ClassExpressionLengthComplexityModel(5),
				new ClassExpressionDepthComplexityModel(2)
		);

		VersionSpaceLearningAlgorithm la = new VersionSpaceLearningAlgorithm(lp, reasoner, complexityModel);
		la.init();

		la.start();
	}
}
