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
package org.dllearner.algorithms;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Sets;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.dllearner.utilities.owl.OWLEntityTypeAdder;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.SortedSet;

//import org.dllearner.utilities.OwlApiJenaUtils;

/**
 * A wrapper class for CELOE that allows for returning the result in forms of OWL axioms.
 * @author Lorenz Buehmann
 *
 */
// not for conf
public class CELOEWrapper extends AbstractAxiomLearningAlgorithm<OWLClassAxiom, OWLIndividual, OWLClass> {
	
	private boolean equivalence = true;

	private int maxClassExpressionDepth = 2;
	
	private int maxNrOfPosExamples = 10;
	private int maxNrOfNegExamples = 20;
	
	private static final ParameterizedSparqlString SAMPLE_QUERY = new ParameterizedSparqlString(
			"CONSTRUCT {?s a ?entity . ?s a ?cls . ?cls a <http://www.w3.org/2002/07/owl#Class> .} "
			+ "WHERE {?s a ?entity . OPTIONAL {?s a ?cls . ?cls a <http://www.w3.org/2002/07/owl#Class> . FILTER(!sameTerm(?cls, ?entity))}}");
	
	public CELOEWrapper(SparqlEndpointKS ks) {
		super.ks = ks;
		useSampling = false;
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getExistingAxioms()
	 */
	@Override
	protected void getExistingAxioms() {
		if(equivalence){
			SortedSet<OWLClassExpression> equivalentClasses = reasoner.getEquivalentClasses(entityToDescribe);
			for (OWLClassExpression equivCls : equivalentClasses) {
				existingAxioms.add(df.getOWLEquivalentClassesAxiom(entityToDescribe, equivCls));
			}
		} else {
			SortedSet<OWLClassExpression> superClasses = reasoner.getSuperClasses(entityToDescribe);
			for (OWLClassExpression supCls : superClasses) {
				existingAxioms.add(df.getOWLSubClassOfAxiom(entityToDescribe, supCls));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#learnAxioms()
	 */
	@Override
	protected void learnAxioms() {
		// get the popularity of the class
		int popularity = reasoner.getPopularity(entityToDescribe);

		// we have to skip here if there are not instances for the given class
		if (popularity == 0) {
			logger.warn("Cannot compute statements for empty class " + entityToDescribe);
			return;
		}
		
		// get positive examples
		SortedSet<OWLIndividual> posExamples = reasoner.getIndividuals(entityToDescribe, maxNrOfPosExamples );
		
		// get negative examples
		SortedSet<OWLIndividual> negExamples = Sets.newTreeSet();
		
		OWLOntology fragment = buildFragment(posExamples, negExamples);
		try {
			fragment.getOWLOntologyManager().saveOntology(fragment, new RDFXMLDocumentFormat(), new FileOutputStream(System.getProperty("java.io.tmpdir") + File.separator + "ont.owl"));
		} catch (OWLOntologyStorageException | FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			AbstractReasonerComponent rc = new ClosedWorldReasoner(new OWLAPIOntology(fragment));
			rc.init();
			
			AbstractClassExpressionLearningProblem lp = new PosNegLPStandard(rc, posExamples, negExamples);
			lp.init();
			
			CELOE la = new CELOE(lp, rc);
			la.init();
			
			la.start();
		} catch (ComponentInitException e) {
			logger.error("CELOE execution failed.", e);
		}
	}
	
	private OWLOntology buildFragment(Set<OWLIndividual> posExamples, Set<OWLIndividual> negExamples){
		StringBuilder filter = new StringBuilder("VALUES ?s0 {");
		for (OWLIndividual ind : Sets.union(posExamples, negExamples)) {
			filter.append(ind).append(" ");
		}
		filter.append("}");
		
		StringBuilder sb = new StringBuilder("CONSTRUCT {");
		sb.append("?s0 ?p0 ?o0 . ?p0 a ?p_type0 .");
		for (int i = 1; i < maxClassExpressionDepth; i++) {
			sb.append("?o").append(i-1).append(" ?p").append(i).append(" ?o").append(i).append(" .");
			sb.append("?p").append(i).append(" a ").append(" ?p_type").append(i).append(" .");
		}
		sb.append("} WHERE {");
		sb.append("?s0 ?p0 ?o0 . OPTIONAL{?p0 a ?p_type0 .}");
		
		for (int i = 1; i < maxClassExpressionDepth; i++) {
			sb.append("OPTIONAL {");
			sb.append("?o").append(i-1).append(" ?p").append(i).append(" ?o").append(i).append(" .");
			sb.append("OPTIONAL{").append("?p").append(i).append(" a ").append(" ?p_type").append(i).append(" .}");
		}
		for (int i = 1; i < maxClassExpressionDepth; i++) {
			sb.append("}");
		}
		sb.append(filter);
		sb.append("}");
		
		QueryExecutionFactoryPaginated qef = new QueryExecutionFactoryPaginated(super.qef, 10000);
		QueryExecution qe = qef.createQueryExecution(sb.toString());
		Model sample = qe.execConstruct();
		qe.close();
		
		StmtIterator iter = sample.listStatements();
		while(iter.hasNext()){
			Statement st = iter.next();
			if(st.getObject().isLiteral() && st.getObject().asLiteral().getDatatype() != null){
				try {
					st.getObject().asLiteral().getValue();
				} catch (Exception e) {
					iter.remove();
				}
			}
		}
		
//		org.dllearner.utilities.owl.
		OWLEntityTypeAdder.addEntityTypes(sample);
		return OwlApiJenaUtils.getOWLOntology(sample);
	}
	

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractAxiomLearningAlgorithm#getSampleQuery()
	 */
	@Override
	protected ParameterizedSparqlString getSampleQuery() {
		return SAMPLE_QUERY;
	}
	
	public static void main(String[] args) throws Exception{
		CELOEWrapper la = new CELOEWrapper(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW()));
		la.setEntityToDescribe(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Book")));
		la.init();
		la.start();
//		new CELOEWrapper(new SparqlEndpointKS(SPARqlend))
	}

}
