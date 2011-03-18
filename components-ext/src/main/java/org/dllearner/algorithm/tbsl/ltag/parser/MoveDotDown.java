package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;

/**
 * The MoveDotDown parsing operation creates a new ParseState where the dot is
 * moved down in the tree.
 **/

class MoveDotDown implements ParserOperation {

	public List<ParseState> go(int i, ParseState state, String[] input,
			ParseGrammar G) {

		if (state.isMDDState()) {

			ArrayList<ParseState> output = new ArrayList<ParseState>();

			TreeNode child = state.dot.getChildren().get(0);

			if (child != null) {
				
				state.pos = 'a';
				state.dot = child;
				state.i = i;

				output.add(state);

			}

			return output;

		}

		return new ArrayList<ParseState>();

	}

}
