package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * The RightCompletor parsing operation requires a pointer to the LeftPredictor
 * ParseState and a pointer to the RightPredictor ParseState. It combines them
 * to a new ParseState which denotes the successful and complete recognition of
 * an adjunction.
 **/

class RightCompletor implements ParserOperation {

	public List<ParseState> go(int i, ParseState state, String[] input,
			ParseGrammar G) {

		if (state.isRCState() && state.t.getParent() == null) { // then

			ArrayList<ParseState> output = new ArrayList<ParseState>();

			ParseState sl = state.pointer.get(state.tid).getLp();
			ParseState sf = state.pointer.get(state.tid).getRp();

			// if sl.dot is left above & state.t can be adjoined to sl.dot
			if (sl.side == 'l' && sl.pos == 'a'
					&& sl.dot.getCategory().equals(state.t.getCategory())
					&& sl.tid != state.tid) {

				ParseState help = sf;

				ParseState newState = new ParseState(help);
				newState.side = 'r';
				newState.pos = 'a';
				newState.f_l = help.f_l;
				newState.f_r = help.f_r;
				newState.star = sl.star;
				newState.t_star_l = sl.t_star_l;
				newState.b_star_l = sl.b_star_l;
				newState.i = i;
				newState.pointer = state.createNewPointer();
				newState.usedTrees = state.usedTrees;
				newState.substPointer = state.substPointer;

				output.add(newState);

			}

			return output;

		}

		return new ArrayList<ParseState>();

	}
}
