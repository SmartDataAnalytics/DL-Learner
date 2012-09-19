package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.data.SubstNode;

/**
 * The SubstCompletor parsing operations completes a predicted substitution by
 * jumping back into the superior tree.
 **/

public class SubstCompletor implements ParserOperation {

	public List<ParseState> go(int i, ParseState state, String[] input,
			ParseGrammar G) {

		if (state.isSCState()) {

			ArrayList<ParseState> output = new ArrayList<ParseState>();

			ParseState sl = state.substPointer.get(state.tid).getSp();

			if ((sl.dot instanceof SubstNode)
					&& (sl.dot.getCategory().equals(state.t.getCategory()))) {

				ParseState newState = new ParseState(sl);
				newState.side = 'r';
				newState.pos = 'a';
				newState.i = i;
				newState.usedTrees = state.usedTrees;
				newState.pointer = state.pointer;
				newState.substPointer = state.createNewSubstPointer();

				output.add(newState);

			}

			return output;

		}

		return new ArrayList<ParseState>();

	}

}
