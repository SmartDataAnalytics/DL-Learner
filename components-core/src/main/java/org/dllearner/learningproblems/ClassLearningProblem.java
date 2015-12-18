/**
 * Copyright (C) 2007-2012, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.learningproblems;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.ReasoningUtils.Coverage;
import org.dllearner.utilities.owl.OWLClassExpressionToSPARQLConverter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;

/**
 * The problem of learning the OWL class expression of an existing class
 * in an OWL ontology.
 * 
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "ClassLearningProblem", shortName = "clp", version = 0.6)
public class ClassLearningProblem extends AbstractClassExpressionLearningProblem<ClassScore> {
	
	private static Logger logger = LoggerFactory.getLogger(ClassLearningProblem.class);
    private long nanoStartTime;
    @ConfigOption(name="maxExecutionTimeInSeconds",defaultValue="10",description="Maximum execution time in seconds")
	private int maxExecutionTimeInSeconds = 10;
	
	@ConfigOption(name = "classToDescribe", description="class of which an OWL class expression should be learned", required=true)
	private OWLClass classToDescribe;
	
	private List<OWLIndividual> classInstances;
	private TreeSet<OWLIndividual> classInstancesSet;
	@ConfigOption(name="equivalence",defaultValue="true",description="Whether this is an equivalence problem (or superclass learning problem)")
	private boolean equivalence = true;

	// factor for higher weight on recall (needed for subclass learning)
	private double coverageFactor;
	
	@ConfigOption(name = "betaSC", description="beta index for F-measure in super class learning", required=false, defaultValue="3.0")
	private double betaSC = 3.0;
	
	@ConfigOption(name = "betaEq", description="beta index for F-measure in definition learning", required=false, defaultValue="1.0")
	private double betaEq = 1.0;
	
	// instances of super classes excluding instances of the class itself
	private List<OWLIndividual> superClassInstances;
	// instances of super classes including instances of the class itself
	private List<OWLIndividual> classAndSuperClassInstances;
	// specific variables for generalised F-measure
	private TreeSet<OWLIndividual> negatedClassInstances;
	
    @ConfigOption(name = "accuracyMethod", description = "Specifies, which method/function to use for computing accuracy. Available measues are \"pred_acc\" (predictive accuracy), \"fmeasure\" (F measure), \"generalised_fmeasure\" (generalised F-Measure according to Fanizzi and d'Amato).",defaultValue = "PRED_ACC")
	protected AccMethodTwoValued accuracyMethod;
	
	@ConfigOption(name = "checkConsistency", description = "whether to check for consistency of suggestions (when added to ontology)", required=false, defaultValue="true")
	private boolean checkConsistency = true;
	
	private OWLDataFactory df = new OWLDataFactoryImpl();
	@ConfigOption(name="useInstanceChecks",defaultValue="true",description="Whether to use instance checks (or get individiuals query)")
	private boolean useInstanceChecks = true;
	
	
	private OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();

	public ClassLearningProblem() {
		
	}
	
	public ClassLearningProblem(AbstractReasonerComponent reasoner) {
		super(reasoner);
	}
	
	public static String getName() {
		return "class learning problem";
	}
	
	@Override
	public void init() throws ComponentInitException {
	
		if(accuracyMethod != null && accuracyMethod instanceof AccMethodPredAccApprox) {
			logger.warn("Approximating predictive accuracy is an experimental feature. USE IT AT YOUR OWN RISK. If you consider to use it for anything serious, please extend the unit tests at org.dllearner.test.junit.HeuristicTests first to verify that it works.");
		}
		
		if(!getReasoner().getClasses().contains(classToDescribe)) {
			throw new ComponentInitException("The class \"" + classToDescribe + "\" does not exist. Make sure you spelled it correctly.");
		}
		
		classInstances = new LinkedList<OWLIndividual>(getReasoner().getIndividuals(classToDescribe));
		// sanity check
		if(classInstances.size() == 0) {
			throw new ComponentInitException("Class " + classToDescribe + " has 0 instances according to \"" + AnnComponentManager.getName(getReasoner().getClass()) + "\". Cannot perform class learning with 0 instances.");
		}
		
		classInstancesSet = new TreeSet<OWLIndividual>(classInstances);
		
		if(equivalence) {
			coverageFactor = betaEq;
		} else {
			coverageFactor = betaSC;
		}
		
		// we compute the instances of the super class to perform
		// optimisations later on
		Set<OWLClassExpression> superClasses = getReasoner().getSuperClasses(classToDescribe);
		TreeSet<OWLIndividual> superClassInstancesTmp = new TreeSet<OWLIndividual>(getReasoner().getIndividuals());
		for(OWLClassExpression superClass : superClasses) {
			superClassInstancesTmp.retainAll(getReasoner().getIndividuals(superClass));
		}
		// we create one list, which includes instances of the class (an instance of the class is also instance of all super classes) ...
		classAndSuperClassInstances = new LinkedList<OWLIndividual>(superClassInstancesTmp);
		// ... and a second list not including them
		superClassInstancesTmp.removeAll(classInstances);
		// since we use the instance list for approximations, we want to avoid
		// any bias through URI names, so we shuffle the list once pseudo-randomly
		superClassInstances = new LinkedList<OWLIndividual>(superClassInstancesTmp);
		Random rand = new Random(1);
		Collections.shuffle(classInstances, rand);
		Collections.shuffle(superClassInstances, rand);
		
		if (accuracyMethod == null) {
			accuracyMethod = new AccMethodPredAcc(true);
		}
		if (accuracyMethod instanceof AccMethodApproximate) {
			((AccMethodApproximate)accuracyMethod).setReasoner(getReasoner());
		}
		reasoningUtil.setClassLearningProblem(this);
		if(accuracyMethod instanceof AccMethodGenFMeasure) {
			OWLClassExpression classToDescribeNeg = df.getOWLObjectComplementOf(classToDescribe);
			negatedClassInstances = new TreeSet<OWLIndividual>();
			for(OWLIndividual ind : superClassInstances) {
				if(getReasoner().hasType(classToDescribeNeg, ind)) {
					negatedClassInstances.add(ind);
				}
			}
//			System.out.println("negated class instances: " + negatedClassInstances);
		}
		
//		System.out.println(classInstances.size() + " " + superClassInstances.size());
	}
		
	@Override
	public ClassScore computeScore(OWLClassExpression description, double noise) {
		
		// TODO: reuse code to ensure that we never return inconsistent results
		// between getAccuracy, getAccuracyOrTooWeak and computeScore
		Set<OWLIndividual> additionalInstances = new TreeSet<OWLIndividual>();
		Set<OWLIndividual> coveredInstances = new TreeSet<OWLIndividual>();
		
		int additionalInstancesCnt = 0;
		int coveredInstancesCnt = 0;
		
		if(reasoner instanceof SPARQLReasoner) {
			// R(C)
			String query = "SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {"
					+ "?s a ?sup . ?classToDescribe <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?sup . "
					+ converter.convert("?s", description)
					+ "FILTER NOT EXISTS {?s a ?classToDescribe}}";
			ParameterizedSparqlString template = new ParameterizedSparqlString(query);
			//System.err.println(converter.convert("?s", description));
			//template.setIri("cls", description.asOWLClass().toStringID());
			template.setIri("classToDescribe", classToDescribe.toStringID());
			
			QueryExecution qe = ((SPARQLReasoner) reasoner).getQueryExecutionFactory().createQueryExecution(template.toString());
			additionalInstancesCnt = qe.execSelect().next().getLiteral("cnt").getInt();
			
			// R(A)
			OWLObjectIntersectionOf ce = df.getOWLObjectIntersectionOf(classToDescribe, description);
			coveredInstancesCnt = ((SPARQLReasoner) reasoner).getPopularityOf(ce);
		} else {
			Coverage[] cc = reasoningUtil.getCoverage(description, Sets.newTreeSet(classInstances), Sets.newTreeSet(superClassInstances));
			// overhang
			additionalInstances.addAll(cc[1].trueSet);
			
			// coverage
			coveredInstances.addAll(cc[0].trueSet);

			additionalInstancesCnt = additionalInstances.size();
			coveredInstancesCnt = coveredInstances.size();
		}
		
		double recall = coveredInstancesCnt/(double)classInstances.size();
		double precision = (additionalInstancesCnt + coveredInstancesCnt == 0) ? 0 : coveredInstancesCnt/(double)(coveredInstancesCnt+additionalInstancesCnt);
		// for each OWLClassExpression with less than 100% coverage, we check whether it is
		// leads to an inconsistent knowledge base
		
		double acc = 0;
		if(accuracyMethod.equals(HeuristicType.FMEASURE)) {
			acc = Heuristics.getFScore(recall, precision, coverageFactor);
		} else if(accuracyMethod.equals(HeuristicType.AMEASURE)) {
			acc = Heuristics.getAScore(recall, precision, coverageFactor);
		} else {
			// TODO: some superfluous instance checks are required to compute accuracy =>
			// move accuracy computation here if possible
			acc = getAccuracyOrTooWeakExact(description, noise);
		}
		
		if(checkConsistency) {
			
			// we check whether the axiom already follows from the knowledge base
//			boolean followsFromKB = reasoner.isSuperClassOf(description, classToDescribe);
			
//			boolean followsFromKB = equivalence ? reasoner.isEquivalentClass(description, classToDescribe) : reasoner.isSuperClassOf(description, classToDescribe);
			boolean followsFromKB = followsFromKB(description);
			
			// workaround due to a bug (see http://sourceforge.net/tracker/?func=detail&aid=2866610&group_id=203619&atid=986319)
//			boolean isConsistent = coverage >= 0.999999 || isConsistent(description);
			// (if the axiom follows, then the knowledge base remains consistent)
			boolean isConsistent = followsFromKB || isConsistent(description);
			
//			double acc = useFMeasure ? getFMeasure(coverage, protusion) : getAccuracy(coverage, protusion);
			return new ClassScore(coveredInstances, Sets.difference(classInstancesSet, coveredInstances), recall, additionalInstances, precision, acc, isConsistent, followsFromKB);
		
		} else {
			return new ClassScore(coveredInstances, Sets.difference(classInstancesSet, coveredInstances), recall, additionalInstances, precision, acc);
		}
	}
	
	public boolean isEquivalenceProblem() {
		return equivalence;
	}

	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression description, double noise) {
		if (accuracyMethod instanceof AccMethodApproximate) {
			return ((AccMethodCLPApproximate) accuracyMethod).getAccApproxCLP(description, classInstances, superClassInstances, coverageFactor, noise);
		} else {
			return reasoningUtil.getAccuracyOrTooWeak2(accuracyMethod, description, Sets.newTreeSet(classInstances), Sets.newTreeSet(superClassInstances), noise);
		}
	}

	// exact computation for 5 heuristics; each one adapted to super class learning;
	// each one takes the noise parameter into account
	public double getAccuracyOrTooWeakExact(OWLClassExpression description, double noise) {
		//System.out.println(description);
		nanoStartTime = System.nanoTime();
		
		if (accuracyMethod instanceof AccMethodAMeasure
				|| accuracyMethod instanceof AccMethodFMeasure
				|| accuracyMethod instanceof AccMethodPredAcc) {
			
			int additionalInstances = 0;
			int coveredInstances = 0;
			
			if(reasoner instanceof SPARQLReasoner) {
				// R(C)
				String query = "SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {"
						+ "?s a ?sup . ?classToDescribe <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?sup . "
						+ converter.convert("?s", description)
						+ "FILTER NOT EXISTS {?s a ?classToDescribe}}";
				ParameterizedSparqlString template = new ParameterizedSparqlString(query);
				//System.err.println(converter.convert("?s", description));
				//template.setIri("cls", description.asOWLClass().toStringID());
				template.setIri("classToDescribe", classToDescribe.toStringID());
				
				QueryExecution qe = ((SPARQLReasoner) reasoner).getQueryExecutionFactory().createQueryExecution(template.toString());
				additionalInstances = qe.execSelect().next().getLiteral("cnt").getInt();
				
				// R(A)
				OWLObjectIntersectionOf ce = df.getOWLObjectIntersectionOf(classToDescribe, description);
				coveredInstances = ((SPARQLReasoner) reasoner).getPopularityOf(ce);
				
				//System.out.println(coveredInstances);
				//System.out.println(additionalInstances);
			} else {
				// computing R(C) restricted to relevant instances
				if(useInstanceChecks) {
					for(OWLIndividual ind : superClassInstances) {
						if(getReasoner().hasType(description, ind)) {
							additionalInstances++;
						}
						if(terminationTimeExpired()){
							return 0;
						}
					}
				} else {
					SortedSet<OWLIndividual> individuals = getReasoner().getIndividuals(description);
					individuals.retainAll(superClassInstances);
					additionalInstances = individuals.size();
				}
				
				
				// computing R(A)
				if(useInstanceChecks) {
					for(OWLIndividual ind : classInstances) {
						if(getReasoner().hasType(description, ind)) {
							coveredInstances++;
						}
						if(terminationTimeExpired()){
							return 0;
						}
					}
				} else {
					SortedSet<OWLIndividual> individuals = getReasoner().getIndividuals(description);
					individuals.retainAll(classInstances);
					coveredInstances = individuals.size();
				}
			}
			
			double recall = coveredInstances/(double)classInstances.size();
			
			double precision = (additionalInstances + coveredInstances == 0) ? 0 : coveredInstances / (double) (coveredInstances + additionalInstances);
			
			if(accuracyMethod.equals(HeuristicType.AMEASURE)) {
				// best reachable concept has same recall and precision 1:
				// 1/t+1 * (t*r + 1)
				if((coverageFactor*recall+1)/(coverageFactor+1) <(1-noise)) {
					return -1;
				} else {
					return Heuristics.getAScore(recall, precision, coverageFactor);
				}
			} else if(accuracyMethod.equals(HeuristicType.FMEASURE)) {
				// best reachable concept has same recall and precision 1:
				if(((1+Math.sqrt(coverageFactor))*recall)/(Math.sqrt(coverageFactor)+1)<1-noise) {
					return -1;
				} else {
					return Heuristics.getFScore(recall, precision, coverageFactor);
				}
			} else if(accuracyMethod.equals(HeuristicType.PRED_ACC)) {
				if((coverageFactor * coveredInstances + superClassInstances.size()) / (coverageFactor * classInstances.size() + superClassInstances.size()) < 1 -noise) {
					return -1;
				} else {
					// correctly classified divided by all examples
					return (coverageFactor * coveredInstances + superClassInstances.size() - additionalInstances) / (coverageFactor * classInstances.size() + superClassInstances.size());
				}
			}

//			return heuristic.equals(HeuristicType.FMEASURE) ? getFMeasure(recall, precision) : getAccuracy(recall, precision);
		} else if (accuracyMethod.equals(HeuristicType.GEN_FMEASURE)) {
			
			// implementation is based on:
			// http://sunsite.informatik.rwth-aachen.de/Publications/CEUR-WS/Vol-426/swap2008_submission_14.pdf
			// default negation should be turned off when using fast instance checker
			
			// compute I_C (negated and non-negated concepts separately)
			TreeSet<OWLIndividual> icPos = new TreeSet<OWLIndividual>();
			TreeSet<OWLIndividual> icNeg = new TreeSet<OWLIndividual>();
			OWLClassExpression descriptionNeg = df.getOWLObjectComplementOf(description);
			// loop through all relevant instances
			for(OWLIndividual ind : classAndSuperClassInstances) {
				if(getReasoner().hasType(description, ind)) {
					icPos.add(ind);
				} else if(getReasoner().hasType(descriptionNeg, ind)) {
					icNeg.add(ind);
				}
				if(terminationTimeExpired()){
					return 0;
				}
			}
			
			// semantic precision
			// first compute I_C \cap Cn(DC)
			// it seems that in our setting, we can ignore Cn, because the examples (class instances)
			// are already part of the background knowledge
			Set<OWLIndividual> tmp1Pos = Sets.intersection(icPos, classInstancesSet);
			Set<OWLIndividual> tmp1Neg = Sets.intersection(icNeg, negatedClassInstances);
			int tmp1Size = tmp1Pos.size() + tmp1Neg.size();
			
			// Cn(I_C) \cap D_C is the same set if we ignore Cn ...
			
			int icSize = icPos.size() + icNeg.size();
			double prec = (icSize == 0) ? 0 : tmp1Size / (double) icSize;
			double rec = tmp1Size / (double) (classInstances.size() + negatedClassInstances.size());

			// we only return too weak if there is no recall
			if(rec <= 0.0000001) {
				return -1;
			}
			
			return getFMeasure(rec,prec);
		}
		
		throw new Error("ClassLearningProblem error: not implemented");
	}
	
	public boolean terminationTimeExpired(){
		boolean val = ((System.nanoTime() - nanoStartTime) >= (maxExecutionTimeInSeconds*1000000000l));
		if(val) {
			logger.warn("Description test aborted, because it took longer than " + maxExecutionTimeInSeconds + " seconds.");
		}
		return val;
	}
	
	// please note that getting recall and precision wastes some computational
	// resource, because both methods need to compute the covered instances
	public double getRecall(OWLClassExpression description) {
		int coveredInstances = 0;
		for(OWLIndividual ind : classInstances) {
			if(getReasoner().hasType(description, ind)) {
				coveredInstances++;
			}
		}
		return coveredInstances/(double)classInstances.size();
	}
	
	public double getPrecision(OWLClassExpression description) {

		int additionalInstances = 0;
		for(OWLIndividual ind : superClassInstances) {
			if(getReasoner().hasType(description, ind)) {
				additionalInstances++;
			}
		}
		
		int coveredInstances = 0;
		for(OWLIndividual ind : classInstances) {
			if(getReasoner().hasType(description, ind)) {
				coveredInstances++;
			}
		}

		return (additionalInstances + coveredInstances == 0) ? 0 : coveredInstances / (double) (coveredInstances + additionalInstances);
	}
	
	// see http://sunsite.informatik.rwth-aachen.de/Publications/CEUR-WS/Vol-426/swap2008_submission_14.pdf
	// for all methods below (currently dummies)
	
	private double getFMeasure(double recall, double precision) {
		// balanced F measure
//		return (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);
		// see e.g. http://en.wikipedia.org/wiki/F-measure
		return (precision + recall == 0) ? 0 :
		  ( (1+Math.sqrt(coverageFactor)) * (precision * recall)
				/ (Math.sqrt(coverageFactor) * precision + recall) );
	}
	
	/**
	 * @return the classToDescribe
	 */
	public OWLClass getClassToDescribe() {
		return classToDescribe;
	}

	public void setClassToDescribe(OWLClass classToDescribe) {
		this.classToDescribe = classToDescribe;
	}
	
	public void setClassToDescribe(IRI classIRI) {
		this.classToDescribe = df.getOWLClass(classIRI);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#evaluate(org.dllearner.core.owl.Description)
	 */
	@Override
	public EvaluatedDescriptionClass evaluate(OWLClassExpression description, double noise) {
		ClassScore score = computeScore(description, noise);
		return new EvaluatedDescriptionClass(description, score);
	}

	/**
	 * @return the isConsistent
	 */
	public boolean isConsistent(OWLClassExpression description) {
		OWLAxiom axiom;
		if(equivalence) {
			axiom = df.getOWLEquivalentClassesAxiom(classToDescribe, description);
		} else {
			axiom = df.getOWLSubClassOfAxiom(classToDescribe, description);
		}
		return getReasoner().remainsSatisfiable(axiom);
	}
	
	public boolean followsFromKB(OWLClassExpression description) {
		return equivalence ? getReasoner().isEquivalentClass(description, classToDescribe) : getReasoner().isSuperClassOf(description, classToDescribe);
	}

	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds;
	}

	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}

	public boolean isEquivalence() {
		return equivalence;
	}

	public void setEquivalence(boolean equivalence) {
		this.equivalence = equivalence;
	}

	public double getBetaSC() {
		return betaSC;
	}

	public void setBetaSC(double betaSC) {
		this.betaSC = betaSC;
	}

	public double getBetaEq() {
		return betaEq;
	}

	public void setBetaEq(double betaEq) {
		this.betaEq = betaEq;
	}

	public boolean isCheckConsistency() {
		return checkConsistency;
	}

	public void setCheckConsistency(boolean checkConsistency) {
		this.checkConsistency = checkConsistency;
	}

	public AccMethodTwoValued getAccuracyMethod() {
		return accuracyMethod;
	}

	@Autowired(required=false)
	public void setAccuracyMethod(AccMethodTwoValued accuracyMethod) {
		this.accuracyMethod = accuracyMethod;
	}
	
	/**
	 * @param useInstanceChecks the useInstanceChecks to set
	 */
	public void setUseInstanceChecks(boolean useInstanceChecks) {
		this.useInstanceChecks = useInstanceChecks;
	}
	
	/**
	 * @return the useInstanceChecks
	 */
	public boolean isUseInstanceChecks() {
		return useInstanceChecks;
	}
}
