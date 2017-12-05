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
package org.dllearner.algorithms.pattern;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Lorenz Buehmann
 *
 */
public class IndividualBasedFragmentExtractor implements FragmentExtractor{
	
	public static final FragmentExtractionStrategy extractionStrategy = FragmentExtractionStrategy.INDIVIDUALS;
	private QueryExecutionFactory qef;
	
	private long maxNrOfIndividuals;
	private long startTime;
	
	private ConciseBoundedDescriptionGenerator cbdGen;
	
	private OWLDataFactory df = new OWLDataFactoryImpl();
	
	public IndividualBasedFragmentExtractor(SparqlEndpointKS ks, int maxNrOfIndividuals) {
		this.maxNrOfIndividuals = maxNrOfIndividuals;
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(ks.getQueryExecutionFactory());
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.pattern.FragmentExtractor#extractFragment(org.dllearner.core.owl.NamedClass)
	 */
	@Override
	public Model extractFragment(OWLClass cls, int maxFragmentDepth) {
		startTime = System.currentTimeMillis();
		Model fragment = ModelFactory.createDefaultModel();
		
		//get some random individuals
		Set<OWLIndividual> individuals = getRandomIndividuals(cls);
		
		//get for each individual the CBD
		Model cbd;
		for (OWLIndividual ind : individuals) {
			cbd = cbdGen.getConciseBoundedDescription(ind.toStringID(), maxFragmentDepth);
			fragment.add(cbd);
		}
		return fragment;
	}
	
	private Set<OWLIndividual> getRandomIndividuals(OWLClass cls){
		Set<OWLIndividual> individuals = new HashSet<>();
		
		String query = "SELECT ?s WHERE {?s a <" + cls.toStringID() + ">} LIMIT " + maxNrOfIndividuals;
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("s").isURIResource()){
				individuals.add(df.getOWLNamedIndividual(IRI.create(qs.getResource("s").getURI())));
			}
		}
		qe.close();
		
		return individuals;
	}
}
