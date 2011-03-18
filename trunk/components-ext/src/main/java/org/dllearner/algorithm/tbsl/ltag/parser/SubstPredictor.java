package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.agreement.Unification;
import org.dllearner.algorithm.tbsl.ltag.data.SubstNode;
import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;
import org.dllearner.algorithm.tbsl.sem.util.Pair;

/**
 * The SubstPredictor parsing operation generates several states which predict
 * possible trees that can be substituted at the current SubstNode.
 **/

public class SubstPredictor implements ParserOperation {

	public List<ParseState> go(int i, ParseState state, String[] input,
			ParseGrammar G) {

		if (state.isSPState()) {

			ArrayList<ParseState> output = new ArrayList<ParseState>();

			// for each tree in the grammar
			for (Pair<TreeNode, Short> pair : G) {
				TreeNode tree = pair.getFirst();
				short tid = pair.getSecond();
				SubstNode dot = (SubstNode) state.dot;
				// if state(dot) == tree(0)
				if (state.dot.getCategory().equals(tree.getCategory())
						&& !tree.isAuxTree()
						&& !state.usedTrees.contains(tid)
						&& Unification.isUnifiable(dot.getFeatureConstraints(), tree.getFeature())) {
//					&& (dot.getFeatureConstraints() == null
//							|| tree.getFeature() == null || dot
//							.getCaseConstraint().equals(tree.getCase()))) {
					ParseState newState = new ParseState(tree, tid);
					newState.l = i;
					newState.i = i;
					newState.subst = true;
					newState.usedTrees.addAll(state.usedTrees);
					newState.usedTrees.add(tid);
					newState.pointer = state.pointer;
					newState.substPointer = state.createNewSubstPointer();
					newState.substPointer.put(tid, new SubstitutionPointer(
							state.tid, state.dot, state));

					output.add(newState);

				}

			}

			return output;
		}

		return new ArrayList<ParseState>();

	}

}
