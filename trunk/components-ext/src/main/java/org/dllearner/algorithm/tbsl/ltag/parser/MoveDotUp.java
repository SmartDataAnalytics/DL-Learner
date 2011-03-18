package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;

/**
 * The MoveDotUp parsing operation creates a new ParseState with the dot moved
 * up from the incoming ParseState. The dot is either moved to the right sibling
 * of the previous node or moved up to the parent if the incoming TreeNode was
 * already the rightmost sibling.
 **/

class MoveDotUp implements ParserOperation {

	public List<ParseState> go(int i, ParseState state, String[] input,
			ParseGrammar G) {

		if (state.isMDUState()) { // then

			ArrayList<ParseState> output = new ArrayList<ParseState>();

			TreeNode rightSibl = (TreeNode) state.dot.getRightSibling();

			if (rightSibl != null) {
				// Case 1: node where the dot is, has right sibling

				state.side = 'l';
				state.dot = rightSibl;
				state.i = i;
				output.add(state);

			}

			else {
				// Case 2: node where the dot is, is rightmost sibling

				state.pos = 'b';
				state.dot = state.dot.getParent();
				state.i = i;
				output.add(state);

			}

			return output;

		}

		return new ArrayList<ParseState>();

	}

}
