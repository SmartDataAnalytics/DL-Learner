package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * The LeftCompletor parsing operation requires a ParseState which is "in" an
 * auxiliary tree plus a pointer to a former state along this path, which was in
 * the superior tree. It creates a new ParseState with the superior tree
 * defining that the predicted adjunction has been partially recognized until
 * the FootNode of the auxiliary tree
 **/

class LeftCompletor implements ParserOperation {

	public List<ParseState> go(int i, ParseState state, String[] input,
			ParseGrammar G) {

		if (state.isLCState() && state.t.isAuxTree() && state.f_l != null
				&& state.f_l.equals(i) && !state.usedTrees.isEmpty()) { // then

			ArrayList<ParseState> output = new ArrayList<ParseState>();

			ParseState s = state.pointer.get(state.tid).getLp();

			/*
			 * if category of s.t(dot) == category of rootnode of state.t and
			 * dot of s.t is left and below
			 */

			if ((s.dot.getCategory().equals(state.t.getCategory()))
					&& (s.side == 'l') && (s.pos == 'a')
					// however i cannot adjoin to myself
					&& (s.tid != state.tid)) {

				// Case 1: s.dot is on foot node => f_l' and f_r' unbound
				if ((s.t.getFootNodes().contains(s.dot))) {

					ParseState newState = new ParseState(s);
					newState.side = 'l';
					newState.pos = 'b';
					newState.f_l = i;
					newState.f_r = null;
					newState.star = s.dot;
					newState.t_star_l = state.l;
					newState.b_star_l = i;
					newState.usedTrees = state.usedTrees;
					newState.pointer = state.createNewPointer();
					newState.pointer.get(state.tid).setLc(state);
					newState.substPointer = state.substPointer;

					newState.i = i;

					output.add(newState);

				}

				// Case 2: s.dot is not on the foot node
				else {
					
					ParseState newState = new ParseState(s);
					newState.side = 'l';
					newState.pos = 'b';
					newState.star = s.dot;
					newState.t_star_l = state.l;
					newState.b_star_l = i;
					newState.usedTrees = state.usedTrees;
					newState.pointer = state.createNewPointer();
					newState.pointer.get(state.tid).setLc(state);
					newState.substPointer = state.substPointer;

					newState.i = i;

					output.add(newState);

				}

			}

			return output;

		}

		return new ArrayList<ParseState>();

	}

}
