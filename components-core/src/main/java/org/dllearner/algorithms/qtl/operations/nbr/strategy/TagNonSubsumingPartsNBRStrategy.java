package org.dllearner.algorithms.qtl.operations.nbr.strategy;

import java.util.List;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;


/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class TagNonSubsumingPartsNBRStrategy implements NBRStrategy{

	@Override
	public RDFResourceTree computeNBR(RDFResourceTree posExampleTree,
			List<RDFResourceTree> negExampleTrees) {
		
		for(RDFResourceTree negExampleTree : negExampleTrees){
			QueryTreeUtils.isSubsumedBy(negExampleTree, posExampleTree);
		}
		
		RDFResourceTree nbr = buildNBR(posExampleTree);
		
		return nbr;
	}

	@Override
	public List<RDFResourceTree> computeNBRs(RDFResourceTree posExampleTree,
			List<RDFResourceTree> negExampleTrees) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private RDFResourceTree buildNBR(RDFResourceTree tree){
		RDFResourceTree nbr = new RDFResourceTree(tree.getData());
		
		for(RDFResourceTree child : tree.getChildren()){
//			if(child.isTagged()){
//				nbr.addChild(buildNBR(child), tree.getEdgeToChild(child));
//			}
			//TODO refactor
		}
		
		return nbr;
	}

}
