/**
 * 
 */
package org.dllearner.algorithms.qtl.util;

import java.util.Collection;
import java.util.Stack;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataRangeVisitor;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Converts query trees into OWL class expressions and vice versa.
 * @author Lorenz Buehmann
 *
 */
public class QueryTreeConverter implements OWLClassExpressionVisitor, OWLDataRangeVisitor{
	
	Stack<QueryTree<String>> stack = new Stack<QueryTree<String>>();
	int id = 0;
	
	/**
	 * Returns a OWL class expression of the union of the given query trees.
	 * @param queryTrees
	 */
	public void asOWLClassExpression(Collection<QueryTree<String>> queryTrees){
		
		//check for common paths
		
	}
	
	public QueryTree<String> asQueryTree(OWLClassExpression expression){
//		stack.push(new QueryTreeImpl<String>("?"));
		reset();
		expression.accept(this);
		return stack.pop();
	}
	
	private void reset(){
		id = 0;
		stack.clear();
	}

	private void fireUnsupportedFeatureException(OWLClassExpression expression) {
		throw new IllegalArgumentException("Conversion of " + expression.getClass().getSimpleName() + " is not supported.");
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public void visit(OWLClass cls) {
		stack.peek().addChild(new QueryTreeImpl<String>(cls.toStringID(), NodeType.RESOURCE, id++), RDF.type.getURI());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectIntersectionOf)
	 */
	@Override
	public void visit(OWLObjectIntersectionOf expr) {
		boolean root = stack.isEmpty();
		stack.push(new QueryTreeImpl<String>("?", NodeType.VARIABLE, id++));
		for (OWLClassExpression op : expr.getOperandsAsList()) {
			op.accept(this);
		}
//		if(!root)
//			stack.pop();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectUnionOf)
	 */
	@Override
	public void visit(OWLObjectUnionOf expr) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectComplementOf)
	 */
	@Override
	public void visit(OWLObjectComplementOf expr) {
		fireUnsupportedFeatureException(expr);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom)
	 */
	@Override
	public void visit(OWLObjectSomeValuesFrom expr) {
		QueryTree<String> parent = stack.peek();
		QueryTree<String> child;
		OWLClassExpression filler = expr.getFiller();
		if(filler.isAnonymous()){
			if(!(filler instanceof OWLObjectIntersectionOf)){
				stack.push(new QueryTreeImpl<String>("?", NodeType.VARIABLE, id++));
			}
			expr.getFiller().accept(this);
			child = stack.pop();
		} else {
			child = new QueryTreeImpl<String>(filler.asOWLClass().toStringID(), NodeType.RESOURCE, id++);
		}
		parent.addChild(child, expr.getProperty().asOWLObjectProperty().toStringID());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectAllValuesFrom)
	 */
	@Override
	public void visit(OWLObjectAllValuesFrom expr) {
		fireUnsupportedFeatureException(expr);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectHasValue)
	 */
	@Override
	public void visit(OWLObjectHasValue expr) {
		QueryTree<String> tree = stack.peek();
		tree.addChild(new QueryTreeImpl<String>(expr.getValue().asOWLNamedIndividual().toStringID(), NodeType.RESOURCE, id++), expr.getProperty().asOWLObjectProperty().toStringID());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectMinCardinality)
	 */
	@Override
	public void visit(OWLObjectMinCardinality expr) {
		fireUnsupportedFeatureException(expr);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectExactCardinality)
	 */
	@Override
	public void visit(OWLObjectExactCardinality expr) {
		fireUnsupportedFeatureException(expr);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectMaxCardinality)
	 */
	@Override
	public void visit(OWLObjectMaxCardinality expr) {
		fireUnsupportedFeatureException(expr);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectHasSelf)
	 */
	@Override
	public void visit(OWLObjectHasSelf expr) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLObjectOneOf)
	 */
	@Override
	public void visit(OWLObjectOneOf expr) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataSomeValuesFrom)
	 */
	@Override
	public void visit(OWLDataSomeValuesFrom expr) {
		QueryTree<String> tree = stack.peek();
		expr.getFiller().accept(this);
		QueryTree<String> child = stack.pop();
		tree.addChild(child, expr.getProperty().asOWLDataProperty().toStringID());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataAllValuesFrom)
	 */
	@Override
	public void visit(OWLDataAllValuesFrom expr) {
		fireUnsupportedFeatureException(expr);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataHasValue)
	 */
	@Override
	public void visit(OWLDataHasValue expr) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataMinCardinality)
	 */
	@Override
	public void visit(OWLDataMinCardinality expr) {
		fireUnsupportedFeatureException(expr);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataExactCardinality)
	 */
	@Override
	public void visit(OWLDataExactCardinality expr) {
		fireUnsupportedFeatureException(expr);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb.owlapi.model.OWLDataMaxCardinality)
	 */
	@Override
	public void visit(OWLDataMaxCardinality expr) {
		fireUnsupportedFeatureException(expr);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDatatype)
	 */
	@Override
	public void visit(OWLDatatype arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDataOneOf)
	 */
	@Override
	public void visit(OWLDataOneOf arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDataComplementOf)
	 */
	@Override
	public void visit(OWLDataComplementOf arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDataIntersectionOf)
	 */
	@Override
	public void visit(OWLDataIntersectionOf arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDataUnionOf)
	 */
	@Override
	public void visit(OWLDataUnionOf arg0) {
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb.owlapi.model.OWLDatatypeRestriction)
	 */
	@Override
	public void visit(OWLDatatypeRestriction arg0) {
	}
	
	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		PrefixManager pm = new DefaultPrefixManager("http://example.org/");
		OWLClassExpression ce = df.getOWLObjectIntersectionOf(
				df.getOWLClass("A", pm),
				df.getOWLObjectSomeValuesFrom(
						df.getOWLObjectProperty("p", pm),
						df.getOWLObjectSomeValuesFrom(
								df.getOWLObjectProperty("r", pm),
								df.getOWLObjectIntersectionOf(
										df.getOWLClass("A", pm),
										df.getOWLClass("B", pm))))
				);
		System.out.println(ce);
		QueryTreeConverter converter = new QueryTreeConverter();
		QueryTree<String> tree = converter.asQueryTree(ce);
		tree.dump();
	}

}
