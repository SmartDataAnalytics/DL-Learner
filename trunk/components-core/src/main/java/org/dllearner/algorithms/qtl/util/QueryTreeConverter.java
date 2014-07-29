/**
 * 
 */
package org.dllearner.algorithms.qtl.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.xml.bind.DatatypeConverter;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeConversionStrategy;
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
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataRangeVisitor;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Converts query trees into OWL class expressions and vice versa.
 * @author Lorenz Buehmann
 *
 */
public class QueryTreeConverter implements OWLClassExpressionVisitor, OWLDataRangeVisitor{
	
	OWLDataFactory df = new OWLDataFactoryImpl();
	
	Stack<QueryTree<String>> stack = new Stack<QueryTree<String>>();
	int id = 0;
	
	/**
	 * Returns a OWL class expression of the union of the given query trees.
	 * @param queryTrees
	 */
	public OWLClassExpression asOWLClassExpression(QueryTree<String> tree){
		Set<OWLClassExpression> classExpressions = asOWLClassExpressions(tree);
		OWLClassExpression expression;
		if(classExpressions.isEmpty()){
			expression = df.getOWLThing();
		} else if(classExpressions.size() == 1){
			expression = classExpressions.iterator().next();
		} else {
			expression = df.getOWLObjectIntersectionOf(classExpressions);
		}
		return expression;
	}
	
	/**
	 * Returns a OWL class expression representation of the given query tree.
	 * @param queryTrees
	 */
	public Set<OWLClassExpression> asOWLClassExpressions(QueryTree<String> tree){
		Set<OWLClassExpression> classExpressions = new HashSet<OWLClassExpression>();
    	
    	List<QueryTree<String>> children = tree.getChildren();
    	for(QueryTree<String> child : children){
    		String childLabel = (String) child.getUserObject();
    		String predicateString = (String) tree.getEdge(child);
    		if(predicateString.equals(RDF.type.getURI()) || predicateString.equals(RDFS.subClassOf.getURI())){//A
    			if(child.isVarNode()){
    				classExpressions.addAll(asOWLClassExpressions(child));
    			} else {
    				if(!childLabel.equals(OWL.Thing.getURI())){//avoid trivial owl:Thing statements
    					classExpressions.add(df.getOWLClass(IRI.create(childLabel)));
    				}
    			}
    		} else {
    			if(child.isLiteralNode()){
    				OWLDataProperty p = df.getOWLDataProperty(IRI.create((String) tree.getEdge(child)));
    				if(childLabel.equals("?")){//p some int
    					Set<Literal> literals = child.getLiterals();
    					OWLDataRange dataRange = null;
    					if(literals.isEmpty()){//happens if there are heterogeneous datatypes
    						String datatypeURI = OWL2Datatype.RDFS_LITERAL.getURI().toString();
    						dataRange = df.getOWLDatatype(IRI.create(datatypeURI));
    					} else {
    						for (LiteralNodeConversionStrategy strategy : LiteralNodeConversionStrategy.values()) {
    							if(strategy == LiteralNodeConversionStrategy.DATATYPE){
        							Literal lit = literals.iterator().next();
                        			RDFDatatype datatype = lit.getDatatype();
                        			String datatypeURI;
                        			if(datatype == null){
                        				datatypeURI = OWL2Datatype.RDF_PLAIN_LITERAL.getURI().toString();
                        			} else {
                        				datatypeURI = datatype.getURI();
                        			}
                        			dataRange = df.getOWLDatatype(IRI.create(datatypeURI));
        						} else if(strategy == LiteralNodeConversionStrategy.DATA_ONE_OF){
        							dataRange = asDataOneOf(df, literals);
        						} else if(strategy == LiteralNodeConversionStrategy.MIN_MAX){
        							dataRange = asFacet(df, literals);
        						} else if(strategy == LiteralNodeConversionStrategy.MIN){
        							dataRange = asMinFacet(df, literals);
        						} else if(strategy == LiteralNodeConversionStrategy.MAX){
        							dataRange = asMaxFacet(df, literals);
        						}
							}
    					}
            			classExpressions.add(df.getOWLDataSomeValuesFrom(p, dataRange));
    				} else {//p value 1.2
    					Set<Literal> literals = child.getLiterals();
            			Literal lit = literals.iterator().next();
            			OWLLiteral owlLiteral = asOWLLiteral(df, lit);
            			classExpressions.add(df.getOWLDataHasValue(p, owlLiteral));
    				}
        		} else {
        			OWLObjectProperty p = df.getOWLObjectProperty(IRI.create((String) tree.getEdge(child)));
        			OWLClassExpression filler;
        			if(child.isVarNode()){//p some C
            			Set<OWLClassExpression> fillerClassExpressions = asOWLClassExpressions(child);
            			if(fillerClassExpressions.isEmpty()){
            				filler = df.getOWLThing();
            			} else if(fillerClassExpressions.size() == 1){
            				filler = fillerClassExpressions.iterator().next();
            			} else {
            				filler = df.getOWLObjectIntersectionOf(fillerClassExpressions);
            			}
            			classExpressions.add(df.getOWLObjectSomeValuesFrom(p, filler));
            		} else {//p value {a}
            			classExpressions.add(df.getOWLObjectHasValue(p, df.getOWLNamedIndividual(IRI.create(childLabel))));
            		}
        		}
    		}
    	}
    	return classExpressions;
	}
	
	private OWLDataRange asFacet(OWLDataFactory df, Set<Literal> literals){
    	//return Boolean datatype because it doesn't make sense to return a facet of Boolean values
    	if(getOWLDatatype(df, literals).equals(df.getBooleanOWLDatatype())){
    		return df.getBooleanOWLDatatype();
    	}
    	Literal min = getMin(literals);
    	Literal max = getMax(literals);
    	
    	OWLFacetRestriction minRestriction = df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, asOWLLiteral(df, min));
    	OWLFacetRestriction maxRestriction = df.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, asOWLLiteral(df, max));
    	
    	return df.getOWLDatatypeRestriction(getOWLDatatype(df, literals), minRestriction, maxRestriction);
    }
    
    private OWLDataRange asMinFacet(OWLDataFactory df, Set<Literal> literals){
    	//return Boolean datatype because it doesn't make sense to return a facet of Boolean values
    	if(getOWLDatatype(df, literals).equals(df.getBooleanOWLDatatype())){
    		return df.getBooleanOWLDatatype();
    	}
    	Literal min = getMin(literals);
    	
    	OWLFacetRestriction minRestriction = df.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, asOWLLiteral(df, min));
    	
    	return df.getOWLDatatypeRestriction(getOWLDatatype(df, literals), minRestriction);
    }
    
    private OWLDataRange asMaxFacet(OWLDataFactory df, Set<Literal> literals){
    	//return Boolean datatype because it doesn't make sense to return a facet of Boolean values
    	if(getOWLDatatype(df, literals).equals(df.getBooleanOWLDatatype())){
    		return df.getBooleanOWLDatatype();
    	}
    	Literal max = getMax(literals);
    	
    	OWLFacetRestriction maxRestriction = df.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, asOWLLiteral(df, max));
    	
    	return df.getOWLDatatypeRestriction(getOWLDatatype(df, literals), maxRestriction);
    }
    
    private OWLDataRange asDataOneOf(OWLDataFactory df, Set<Literal> literals){
    	//return Boolean datatype because it doesn't make sense to return a enumeration of Boolean values
    	if(getOWLDatatype(df, literals).equals(df.getBooleanOWLDatatype())){
    		return df.getBooleanOWLDatatype();
    	}
    	return df.getOWLDataOneOf(asOWLLiterals(df, literals));
    }
    
    private Set<OWLLiteral> asOWLLiterals(OWLDataFactory df, Set<Literal> literals){
    	Set<OWLLiteral> owlLiterals = new HashSet<OWLLiteral>(literals.size());
    	for (Literal literal : literals) {
			owlLiterals.add(asOWLLiteral(df, literal));
		}
    	return owlLiterals;
    }
    
    private OWLLiteral asOWLLiteral(OWLDataFactory df, Literal literal){
    	OWLLiteral owlLiteral;
		if(literal.getDatatypeURI() == null){
			owlLiteral = df.getOWLLiteral(literal.getLexicalForm(), literal.getLanguage());
		} else {
			owlLiteral = df.getOWLLiteral(literal.getLexicalForm(), df.getOWLDatatype(IRI.create(literal.getDatatypeURI())));
		}
    	return owlLiteral;
    }
    
    private Literal getMin(Set<Literal> literals){
    	Iterator<Literal> iter = literals.iterator();
    	Literal min = iter.next();
    	Literal l;
    	while(iter.hasNext()){
    		l = iter.next();
    		if(l.getDatatype() == XSDDatatype.XSDinteger || l.getDatatype() == XSDDatatype.XSDint){
    			min = (l.getInt() < min.getInt()) ? l : min;
    		} else if(l.getDatatype() == XSDDatatype.XSDdouble || l.getDatatype() == XSDDatatype.XSDdecimal){
    			min = (l.getDouble() < min.getDouble()) ? l : min;
    		} else if(l.getDatatype() == XSDDatatype.XSDfloat){
    			min = (l.getFloat() < min.getFloat()) ? l : min;
    		} else if(l.getDatatype() == XSDDatatype.XSDdate){
    			min = (DatatypeConverter.parseDate(l.getLexicalForm()).compareTo(DatatypeConverter.parseDate(min.getLexicalForm())) == -1) ? l : min;
    		} 
    	}
    	return min;
    }
    
    private Literal getMax(Set<Literal> literals){
    	Iterator<Literal> iter = literals.iterator();
    	Literal max = iter.next();
    	Literal l;
    	while(iter.hasNext()){
    		l = iter.next();
    		if(l.getDatatype() == XSDDatatype.XSDinteger || l.getDatatype() == XSDDatatype.XSDint){
    			max = (l.getInt() > max.getInt()) ? l : max;
    		} else if(l.getDatatype() == XSDDatatype.XSDdouble || l.getDatatype() == XSDDatatype.XSDdecimal){
    			max = (l.getDouble() > max.getDouble()) ? l : max;
    		} else if(l.getDatatype() == XSDDatatype.XSDfloat){
    			max = (l.getFloat() > max.getFloat()) ? l : max;
    		} else if(l.getDatatype() == XSDDatatype.XSDdate){
    			max = (DatatypeConverter.parseDate(l.getLexicalForm()).compareTo(DatatypeConverter.parseDate(max.getLexicalForm())) == 1) ? l : max;
    		} 
    	}
    	return max;
    }
    
    private OWLDatatype getOWLDatatype(OWLDataFactory df, Set<Literal> literals){
    	return df.getOWLDatatype(IRI.create(literals.iterator().next().getDatatypeURI()));
    }
	
	
	/**
	 * Converts a OWL class expression into a query tree, if possible. Note that this is not possible
	 * for all OWL constructs, e.g. universal restrictions are not allowed. An exceptions is thrown if the conversion
	 * fails.
	 * @param expression
	 * @return
	 */
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
