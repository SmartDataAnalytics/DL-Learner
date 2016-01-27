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
package org.dllearner.core;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.learningproblems.AxiomScore;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class ProbabilityBasedAxiomScoreCalculator extends AbstractAxiomScoreCalculator{

	public ProbabilityBasedAxiomScoreCalculator(QueryExecutionFactory qef) {
		super(qef);
	}

	@Override
	public AxiomScore calculateScore(OWLAxiom axiom) {
		double score = axiom.accept(this);
		return new AxiomScore(score);
	}
	
	@Override
	public Double visit(OWLSubClassOfAxiom axiom) {
		// TODO Auto-generated method stub
		return super.visit(axiom);
	}
	

}
