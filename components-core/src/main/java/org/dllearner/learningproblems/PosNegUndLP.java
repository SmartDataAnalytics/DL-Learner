/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.config.ConfigOption;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
/**
 * A ternary learning problem (positive, negative and uncertain instances) to manage the problem of the Open World Assumption
 * typically employed for ontologies
 * @author Utente
 *
 */
@ComponentAnn(name="PosNegUndLP", shortName="posNegUndLP", version=1.0, description="A learning problem with uncertain-membership instances")
public class PosNegUndLP extends PosNegLPStandard implements Cloneable{
	//private SortedSet<OWLIndividual> positiveExample;
	//private SortedSet<OWLIndividual> negativeExample;
	@ConfigOption(description = "the uncertain examples", required = true)
	private Set<OWLIndividual> uncertainExamples;
	
	// getter and setters
	public Set<OWLIndividual> getPositiveExamples() {
		return new TreeSet<>(super.getPositiveExamples());
	}

	public void setPositiveExamples(SortedSet<OWLIndividual> positiveExample) {
		this.positiveExamples = positiveExample;
	}

	public Set<OWLIndividual> getNegativeExamples() {
		return new TreeSet<>(super.getNegativeExamples());//negativeExamples;
	}

	public void setNegativeExamples(Set<OWLIndividual> negativeExample) {
		this.negativeExamples = negativeExample;
	}

	public Set<OWLIndividual> getUncertainExamples() {
		return new TreeSet<>(uncertainExamples);
	}

	public void setUncertainExamples(Set<OWLIndividual> uncertainExample) {
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

	//TODO add two methods: the first one performs classification by inducing the derived concept definition (see the source code of PosNegstandard )
		// the second one performs classification with the induced algorithms.tree.models in order to deal with specific settings such as binary classification or missing values for TDTs and ETDTs
	/**
	 * A method for binarizing a ternary learning problem. This is important to work if you want to run a method
	 * such as CELOE starting from randomly generated queries
	 * @return the pos/neg learning problem
	 */
	public PosNegLP getPosNegLP(){
		PosNegLPStandard  binaryProblem= new PosNegLPStandard(getReasoner());
		binaryProblem.setPositiveExamples(getPositiveExamples());
	    SortedSet<OWLIndividual> therestOfWorld= new TreeSet<>();
	    //positive vs. the rest  of world
	    therestOfWorld.addAll(getNegativeExamples());
	    therestOfWorld.addAll(uncertainExamples);
	    binaryProblem.setNegativeExamples(therestOfWorld);
//	    System.out.println(getPositiveExamples().size()+"    "+therestOfWorld.size());
		
	return binaryProblem;
	}

}
