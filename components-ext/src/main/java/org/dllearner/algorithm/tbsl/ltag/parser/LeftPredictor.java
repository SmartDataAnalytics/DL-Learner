package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.data.FootNode;
import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;
import org.dllearner.algorithm.tbsl.sem.util.Pair;

/**
 * The LeftPredictor parsing operations splits a parse path into several paths.
 * It predicts an adjunction at the current TreeNode if adjunction is possible
 * and an auxiliary tree is available (i.e. not predicted for adjunction yet
 * along this parse path or current TreeNode allows adjunction). It therefore
 * creates several different states which have to have independent pointers to
 * previous states.
 **/

class LeftPredictor implements ParserOperation {

	public List<ParseState> go(int i, ParseState state, String[] input,
			ParseGrammar G) {

		if (state.isLPState()) { // then

			List<ParseState> output = new ArrayList<ParseState>();

			// (Step 1) Get all possible adjunction trees
			// out of G that can be adjoined to t
			List<Pair<TreeNode, Short>> possibleAdjuncts = G.getAdjunctTrees(state);

			// for each possible adjunction tree b create a new parseState
			// [ b, 0, left, above, i,- ,- ,- ,- ,- ];
			if (!possibleAdjuncts.isEmpty() && !(state.dot instanceof FootNode)) {

				for (Pair<TreeNode, Short> pair : possibleAdjuncts) {
					TreeNode auxTree = pair.getFirst();
					short tid = pair.getSecond();

					ParseState newState = new ParseState(auxTree, tid);
					newState.dot = newState.t; // dot is on the root node
					newState.side = 'l';
					newState.pos = 'a';
					newState.l = i;
					newState.usedTrees.addAll(state.usedTrees);
					newState.usedTrees.add(tid);
					newState.pointer = state.createNewPointer();
					newState.pointer.put(tid, new AdjunctionPointer(state.tid,
							state.dot, state));
					newState.substPointer = state.substPointer;

					newState.i = i;

					output.add(newState);

				}
			}

			// (Step 2) Consider the case that there is no adjunction at t(dot)
			if ((state.dot instanceof FootNode)) {

				// Case 2: the dot is on the foot node. Necessarily, since
				// the foot node has not been already traversed,
				// state.f_l and state.f_r are unspecified.

				ParseState newState = new ParseState(state);
				newState.side = 'l';
				newState.pos = 'b';
				newState.f_l = i; // !!!
				newState.f_r = null;
				newState.i = i;

				output.add(newState);

			} else {

				// Case 1: the dot is not on the foot node
				ParseState newState = new ParseState(state);
				newState.side = 'l';
				newState.pos = 'b';
				newState.i = i;

				output.add(newState);

			}

			return output;

		}

		return new ArrayList<ParseState>();

	}

}
