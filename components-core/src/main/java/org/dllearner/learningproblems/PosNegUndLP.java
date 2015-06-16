package org.dllearner.learningproblems;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.Score;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.ScorePosNeg;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
/**
 * A ternary learning problem (positive, negative and uncertain instances) to manage the problem of the Open World Assumption 
 * typically employed for ontologies 
 * @author Utente
 *
 */
@ComponentAnn(name="PosNegUndLP", shortName="posNegUndLP", version=1.0, description="A learning problem with uncertain-memebrship instances")
public class PosNegUndLP extends PosNegLPStandard implements Cloneable{
	//private SortedSet<OWLIndividual> positiveExample;
	//private SortedSet<OWLIndividual> negativeExample;
	private Set<OWLIndividual> uncertainExamples;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "pos neg und learning problem";
	}

	
	// getter and setters
	public Set<OWLIndividual> getPositiveExample() {
		return new TreeSet<OWLIndividual>(super.getPositiveExamples());
	}

	public void setPositiveExample(SortedSet<OWLIndividual> positiveExample) {
		this.positiveExamples = positiveExample;
	}

	public Set<OWLIndividual> getNegativeExample() {
		return new TreeSet<OWLIndividual>(super.getNegativeExamples());//negativeExamples;
	}

	public void setNegativeExample(Set<OWLIndividual> negativeExample) {
		this.negativeExamples = negativeExample;
	}

	public Set<OWLIndividual> getUncertainExample() {
		return new TreeSet<OWLIndividual>(uncertainExamples);
	}

	public void setUncertainExample(Set<OWLIndividual> uncertainExample) {
		this.uncertainExamples = uncertainExample;
	}

	
	
	public PosNegUndLP(){
		
		super();
	}

	public PosNegUndLP(AbstractReasonerComponent reasoner){
		super(reasoner);
		
	}

	public PosNegUndLP(AbstractReasonerComponent reasoningService, SortedSet<OWLIndividual> positiveExamples, SortedSet<OWLIndividual> negativeExamples, SortedSet<OWLIndividual> uncertainExamples){
		
//		this.setReasoner(reasoningService);
//		this.positiveExamples=positiveExamples;
//		this.negativeExamples=negativeExamples;
		super(reasoningService, positiveExamples, negativeExamples);
		this.uncertainExamples=uncertainExamples;
	}
	
			
			
  //useless methods and therefore empty implementations
	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ScorePosNeg computeScore(OWLClassExpression description) {
		// TODO Auto-generated method stub
		 return super.computeScore(description);
	}

	@Override
	public EvaluatedDescription evaluate(OWLClassExpression description) {
		// TODO Auto-generated method stub
		return super.evaluate(description);
	}

	@Override
	public double getAccuracy(OWLClassExpression description) {
		// TODO Auto-generated method stub
		return super.getAccuracy(description);
	}

	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression description, double noise) {
		// TODO Auto-generated method stub
		return super.getAccuracyOrTooWeak(description, noise);
	}
	//TODO add two methods: the first one performs classification by inducing the derived concept definition (see the source code of PosNegstandard )
		// the second one performs classification with the induced algorithms.tree.models in order to deal with specific settings such as binary classification or missing values for TDTs and ETDTs
	/**
	 * A method for binarizing a ternary learning problem. This is important to work if you want to run a method 
	 * such as CELOE starting from randomly generated queries 
	 * @return
	 */
	public PosNegLP getPosNegLP(){
		PosNegLPStandard  binaryProblem= new PosNegLPStandard(getReasoner());
		binaryProblem.setPositiveExamples(getPositiveExample());
	    SortedSet<OWLIndividual> therestOfWorld= new TreeSet<OWLIndividual>();
	    //positive vs. the rest  of world
	    therestOfWorld.addAll(getNegativeExample());
	    therestOfWorld.addAll(uncertainExamples);
	    binaryProblem.setNegativeExamples(therestOfWorld);
//	    System.out.println(getPositiveExamples().size()+"    "+therestOfWorld.size());
		
	return binaryProblem;	
	}

}
