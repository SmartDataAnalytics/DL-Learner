/**
 * 
 */
package org.dllearner.algorithms;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.SortedSet;

import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.dllearner.utilities.owl.OWLEntityTypeAdder;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
//import org.dllearner.utilities.OwlApiJenaUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * A wrapper class for CELOE that allows for returning the result in forms of OWL axioms.
 * @author Lorenz Buehmann
 *
 */
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
			fragment.getOWLOntologyManager().saveOntology(fragment, new RDFXMLDocumentFormat(), new FileOutputStream("/tmp/ont.owl"));
		} catch (OWLOntologyStorageException | FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			AbstractReasonerComponent rc = new FastInstanceChecker(new OWLAPIOntology(fragment));
			rc.init();
			
			AbstractLearningProblem lp = new PosNegLPStandard(rc, posExamples, negExamples);
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
