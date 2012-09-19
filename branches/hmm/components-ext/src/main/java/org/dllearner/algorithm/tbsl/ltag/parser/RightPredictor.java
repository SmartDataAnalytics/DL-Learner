package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * The RightPredictor parsing operation requires a pointer to the LeftCompletor
 * ParseState of this partially recognized adjunction. The RightPredictor
 * executes a "jump" from the superior tree back into the auxiliary tree which
 * was predicted and already recognized until its FootNode. The RightPredictor
 * initializes the recognition of the parts of the auxiliary tree that are right
 * of the FootNode.
 **/

class RightPredictor implements ParserOperation {

	public List<ParseState> go(int i, ParseState state, String[] input,
			ParseGrammar G) {

		if (state.isRPState()) { // then

			ArrayList<ParseState> output = new ArrayList<ParseState>();

			// Case 1: dot = star
			if (state.dot.equals(state.star)) {

				ParseState s = findLCState(state);

				if ((s.t.isAuxTree())
						&& (s.t.getCategory().equals(state.dot.getCategory()))
						&& (s.tid != state.tid) && (s.side == 'l')
						&& (s.pos == 'b')

				) {

					ParseState newState = new ParseState(s);
					newState.side = 'r';
					newState.pos = 'b';
					newState.f_r = i;
					newState.l = state.t_star_l;
					newState.f_l = state.b_star_l;
					newState.t_star_l = s.t_star_l;
					newState.b_star_l = s.b_star_l;
					newState.star = s.star;
					newState.pointer = state.createNewPointer();
					newState.pointer.get(s.tid).setRp(state);
					newState.usedTrees = state.usedTrees;
					newState.substPointer = state.substPointer;

					newState.i = i;

					output.add(newState);

				}

			}

			// Case 2: dot != star
			else {

				ParseState newState = new ParseState(state);
				newState.pos = 'a';
				newState.i = i;

				output.add(newState);

			}

			return output;

		}

		return new ArrayList<ParseState>();

	}

	private ParseState findLCState(ParseState state) {
		for (AdjunctionPointer adjs : state.pointer.values()) {
			if (adjs.getTid() == state.tid && adjs.getDot().equals(state.dot)) {
				return adjs.getLc();
			}
		}
		return null;
	}

}
