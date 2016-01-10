package org.dllearner.kb.aquisitors;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This class is used to save disambiguated information for a blankNode in an RDF
 * Graph. It is used to silently transport the info to the layer above.
 * 
 * @author Sebastian Hellmann
 *
 */
public class RDFBlankNode implements RDFNode {
	
	
	private RDFNode blankNode;
	private int bNodeId;
		
	public RDFBlankNode(int id, RDFNode blankNode) {
		super();
		this.bNodeId = id;
		this.blankNode = blankNode;
	}
	
	public int getBNodeId(){
		return bNodeId;
	}
	
	
	@Override
	public String toString(){
		//RBC
		return "bnodeid: "+bNodeId+" ||"+blankNode;
	}
	
	// overidden Functions
	@SuppressWarnings("all")
	public RDFNode as(Class view) {
		
		return blankNode.as(view);
	}

	@SuppressWarnings("all")
	public boolean canAs(Class view) {
		
		return blankNode.canAs(view);
	}

	public RDFNode inModel(Model m) {
		
		return blankNode.inModel(m);
	}

	public boolean isAnon() {
		
		return blankNode.isAnon();
	}

	public boolean isLiteral() {
		
		return blankNode.isLiteral();
	}

	public boolean isResource() {
		
		return blankNode.isResource();
	}

	public boolean isURIResource() {
		
		return blankNode.isURIResource();
	}

	public Object visitWith(RDFVisitor rv) {
		
		return blankNode.visitWith(rv);
	}

	public Node asNode() {
		
		return blankNode.asNode();
	}

    /**
     *{@inheritDoc}
     */
    @Override
    public Model getModel() {
        return blankNode.getModel();
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public Resource asResource() {
        return blankNode.asResource();
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public Literal asLiteral() {
        return blankNode.asLiteral();
    }
}
