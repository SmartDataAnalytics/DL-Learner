package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;

import org.dllearner.algorithm.tbsl.ltag.data.TerminalNode;

/**
 * The Scanner parsing operation scans the input string and compares it to the
 * terminal string in the current tree. If it matches, Scanner writes a new
 * ParseState into the next StateSet.
 **/

class Scanner implements ParserOperation {

	public ArrayList<ParseState> go(int i, ParseState state, String[] input,
			ParseGrammar G) {

		if (state.isScanState()) {

			ArrayList<ParseState> output = new ArrayList<ParseState>();

			TerminalNode t = (TerminalNode) state.dot;

			// Case 1: tree terminal matches input at a_i+1
			String word_i = "";
			try {
				word_i = input[i + 1];
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				word_i = "";
			}

			if (t.getTerminal().equalsIgnoreCase(word_i)) {

				state.side = 'r';
				state.pos = 'a';
				state.i = i+1;
				output.add(state);

			}

			// Case 2: tree terminal matches empty symbol
			else if (t.getTerminal().equals("")) {

				state.side = 'r';
				state.pos = 'a';
				state.i = i;
				output.add(state);

			}

			return output;
		}

		return new ArrayList<ParseState>();

	}

}
