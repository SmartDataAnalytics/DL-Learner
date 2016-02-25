package org.dllearner.utilities;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.AccMethodApproximate;
import org.dllearner.learningproblems.AccMethodThreeValued;
import org.dllearner.learningproblems.AccMethodTwoValued;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Set;

/**
 * Created by Simon Bin on 16-1-25.
 */
public class ReasoningUtilsCLP extends ReasoningUtils {
	final private ClassLearningProblem problem;
	private OWLDataFactory df = new OWLDataFactoryImpl();
	private OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
	public ReasoningUtilsCLP(ClassLearningProblem problem, AbstractReasonerComponent reasoner) {
		super(reasoner);
		this.problem = problem;
	}

	@Override
	protected boolean interrupted() {
		return problem.terminationTimeExpired();
	}

	public Coverage[] getCoverageCLP(OWLClassExpression description, Set<OWLIndividual> classInstances,
	                             Set<OWLIndividual> superClassInstances) {
		if (reasoner instanceof SPARQLReasoner) {
			SPARQLReasoner reasoner2 = (SPARQLReasoner)reasoner;
			Coverage[] ret = new Coverage[2];
			ret[0] = new Coverage();
			ret[1] = new Coverage();

			// R(C)
			String query = "SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {"
					+ "?s a ?sup . ?classToDescribe <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?sup . "
					+ converter.convert("?s", description)
					+ "FILTER NOT EXISTS {?s a ?classToDescribe}}";
			ParameterizedSparqlString template = new ParameterizedSparqlString(query);
			//System.err.println(converter.convert("?s", description));
			//template.setIri("cls", description.asOWLClass().toStringID());
			template.setIri("classToDescribe", problem.getClassToDescribe().toStringID());

			QueryExecution qe = reasoner2.getQueryExecutionFactory().createQueryExecution(template.toString());
			ret[1].trueCount = qe.execSelect().next().getLiteral("cnt").getInt();
			ret[1].falseCount = superClassInstances.size() - ret[1].trueCount;

			// R(A)
			OWLObjectIntersectionOf ce = df.getOWLObjectIntersectionOf(problem.getClassToDescribe(), description);
			ret[0].trueCount = reasoner2.getPopularityOf(ce);
			ret[0].falseCount = classInstances.size() - ret[0].trueCount;

			return ret;
		} else {
			return getCoverage(description, classInstances, superClassInstances);
		}

	}

	public double getAccuracyCLP(AccMethodTwoValued accuracyMethod,
	                             OWLClassExpression description, Set<OWLIndividual> classInstances,
	                             Set<OWLIndividual> superClassInstances,
	                             double noise
			) {
		/*
		// computing R(A)
		CoverageCount[] coveredInstanceCount = getCoverageCount(description, classInstances);
		if (coveredInstanceCount == null) {
			return 0;
		}

		// if even the optimal case (no additional instances covered) is not sufficient,
		// the concept is too weak
		if(coveredInstanceCount[0].trueCount / (double) classInstances.size() <= 1 - noise) {
			return -1;
		}

		CoverageCount[] additionalInstanceCount = getCoverageCount(description, superClassInstances);
		// computing R(C) restricted to relevant instances
		if (additionalInstanceCount == null) {
			return 0;
		}

		return accuracyMethod.getAccOrTooWeak2(coveredInstanceCount[0].trueCount,
				coveredInstanceCount[0].falseCount,
				additionalInstanceCount[0].trueCount,
				additionalInstanceCount[0].falseCount,
				noise);

												*/

		// computing R(A) and R(C)
		CoverageCount[] cc = getCoverageCount(description, classInstances, superClassInstances);
		if (cc == null) {
			return 0;
		}
		return accuracyMethod.getAccOrTooWeak2(cc[0].trueCount, cc[0].falseCount, cc[1].trueCount, cc[1].falseCount, noise);
	}

	public double getAccuracyOrTooWeak3(AccMethodThreeValued accuracyMethod, OWLClassExpression description, Set<OWLIndividual> classInstances, Set<OWLIndividual> superClassInstances, Set<OWLIndividual> negatedClassInstances, double noise) {
		if (accuracyMethod instanceof AccMethodApproximate) {
			throw new RuntimeException();
		} else {
			return getAccuracyOrTooWeakExact3(accuracyMethod, description, classInstances, superClassInstances, negatedClassInstances, noise);
		}
	}

	public double getAccuracyOrTooWeakExact3(AccMethodThreeValued accuracyMethod, OWLClassExpression description, Set<OWLIndividual> classInstances, Set<OWLIndividual> superClassInstances, Set<OWLIndividual> negatedClassInstances, double noise) {
		// implementation is based on:
		// http://sunsite.informatik.rwth-aachen.de/Publications/CEUR-WS/Vol-426/swap2008_submission_14.pdf
		// default negation should be turned off when using fast instance checker
		// compute I_C (negated and non-negated concepts separately)
		ReasoningUtils.Coverage3[] cc = getCoverage3(description, df.getOWLObjectComplementOf(description), Sets.union(classInstances, superClassInstances));
		// trueSet = icPos, falseSet = icNeg
		if (cc == null) { // timeout
			return 0;
		}
		// semantic precision
		// first compute I_C \cap Cn(DC)
		// it seems that in our setting, we can ignore Cn, because the examples (class instances)
		// are already part of the background knowledge
		Set<OWLIndividual> tmp1Pos = Sets.intersection(cc[0].trueSet, classInstances);
		Set<OWLIndividual> tmp1Neg = Sets.intersection(cc[0].falseSet, negatedClassInstances);
		// icPos + icNeg <===> all returned results
		// --> precision = tmp1size / (icpos + icneg)
		// classInstances + negatedClassInstances <==> all results that should be returned
		// -> recall = tmp1size / (cI + ncI)

		// F_beta = true positives / (true positives + false negatives + false positives)

		// Cn(I_C) \cap D_C is the same set if we ignore Cn ...
		// ---> @@@@ AccMethodGenFMeasure
		return accuracyMethod.getAccOrTooWeak3(tmp1Pos.size(), tmp1Neg.size(), cc[0].trueCount, cc[0].falseCount, classInstances.size(), negatedClassInstances.size(), noise);
	}
}
