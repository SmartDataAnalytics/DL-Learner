/**
 * 
 */
package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.data.SubstNode;
import org.dllearner.algorithm.tbsl.ltag.data.Tree;
import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;
import org.dllearner.algorithm.tbsl.ltag.reader.TokenMgrError;
import org.dllearner.algorithm.tbsl.sem.dudes.data.DUDE_Constructor;
import org.dllearner.algorithm.tbsl.sem.dudes.data.Dude;
import org.dllearner.algorithm.tbsl.sem.dudes.reader.ParseException;
import org.dllearner.algorithm.tbsl.sem.util.Pair;

/**
 * DerivedTree creates a new TreeNode object based on the operations of a
 * Derivation Tree. If CONSTRUCT_SEMANTICS is set, the corresponding Dudes are
 * also parsed (DudeParser), constructed and merged (adjunction) / applied
 * (subsitution).
 * 
 */
public class DerivedTree {

	static List<Pair<TreeNode, Dude>> build(DerivationTree parse,
			ParseGrammar parseGram, LTAGLexicon G, boolean CONSTRUCT_SEMANTICS)
			throws ParseException {

		List<Pair<TreeNode, Dude>> output = new ArrayList<Pair<TreeNode, Dude>>();

		// create a local index of ids to trees which is updated after every
		// adjunction/substitution
		Hashtable<Short, TreeNode> myIndex = new Hashtable<Short, TreeNode>(
				parse.getTreeMappings());

		ArrayList<Tree> dots = new ArrayList<Tree>();
		TreeNode newTree = null;

		// semantics
		DUDE_Constructor dudeConstructor = new DUDE_Constructor();
		Hashtable<Short, List<Dude>> myDudeIndex = new Hashtable<Short, List<Dude>>();
		List<Dude> newDudes = new ArrayList<Dude>();

		if (parse.getOperations().isEmpty()) {

			newDudes = getDudeFromTid(parse.getInitTreeID(),
					dudeConstructor, parseGram, G, CONSTRUCT_SEMANTICS);
			for (Dude d : newDudes) {
				output.add(new Pair<TreeNode, Dude>(parse.getTreeMappings()
						.get(parse.getInitTreeID()), d));
			}
			return output;

		}

		for (Operation op : parse.getOperations()) {

			if (op.getType().equals(OperationType.ADJUNCTION)) {

				Tree dot = (Tree) op.getAddress();
				dot.setAdjLabel("" + op.getTid2());
				dots.add(dot);

			}

			// semantics
			myDudeIndex.put(op.getTid1(), getDudeFromTid(op.getTid1(),
					dudeConstructor, parseGram, G, CONSTRUCT_SEMANTICS));
			myDudeIndex.put(op.getTid2(), getDudeFromTid(op.getTid2(),
					dudeConstructor, parseGram, G, CONSTRUCT_SEMANTICS));
			

		}
		
		for (int i = parse.getOperations().size() - 1; i >= 0; i--) {

			Operation op = parse.getOperations().get(i);

			if (op.getType().equals(OperationType.SUBSTITUTION)) {

				TreeNode t1 = myIndex.get(op.getTid1()).clone();
				SubstNode dot = (SubstNode) op.getAddress().clone();
				TreeNode t2 = myIndex.get(op.getTid2()).clone();
				newTree = t1.substitute(dot.getIndex(), t2);
				myIndex.put(op.getTid1(), newTree);

				// semantics
				if (CONSTRUCT_SEMANTICS) {
					newDudes = new ArrayList<Dude>();
					
					List<Dude> dudeList1 = myDudeIndex.get(op.getTid1());
					List<Dude> dudeList2 = myDudeIndex.get(op.getTid2());
					for (Dude d1 : dudeList1) {
						for (Dude d2 : dudeList2) {
							newDudes.add(d1.apply(dot.getIndex(), d2));
						}
					}
					myDudeIndex.put(op.getTid1(), newDudes);
				}

			} else if (op.getType().equals(OperationType.ADJUNCTION)) {

				TreeNode t1 = myIndex.get(op.getTid1()).clone();
				TreeNode t2 = myIndex.get(op.getTid2()).clone();

				newTree = t1.adjoin("" + op.getTid2(), t2);
				myIndex.put(op.getTid1(), newTree);

				// semantics
				if (CONSTRUCT_SEMANTICS) {
					newDudes = new ArrayList<Dude>();
					
					List<Dude> dudeList1 = myDudeIndex.get(op.getTid1());
					List<Dude> dudeList2 = myDudeIndex.get(op.getTid2());
					for (Dude d1 : dudeList1) {
						for (Dude d2 : dudeList2) {
							newDudes.add(d1.merge(d2));
						}
					}
					myDudeIndex.put(op.getTid1(), newDudes);
				}

			}

		}

		for (Tree dot : dots) {

			dot.setAdjLabel("");

		}

		for (Dude d : newDudes) {
			output.add(new Pair<TreeNode,Dude>(newTree, d));
		}
		
		return output;

	}

	private static List<Dude> getDudeFromTid(short tid,
			DUDE_Constructor dudeConstructor, ParseGrammar parseGram,
			LTAGLexicon g, boolean CONSTRUCT_SEMANTICS) throws ParseException {

		if (!CONSTRUCT_SEMANTICS) {
			return Collections.singletonList(new Dude());
		}

		List<Dude> output = new ArrayList<Dude>();

		List<String> semStrings = g.getIdToSemantics().get(
				parseGram.getLocalIdsToGlobalIds().get(tid));

		for (String s : semStrings) {
			try {
				output.add(dudeConstructor.construct(s));

			} catch (ParseException e) {
				throw new ParseException(parseGram.getIndex().get(tid)
						.toFileString()
						+ " || " + s);
			} catch (TokenMgrError tme) {
				throw new ParseException(parseGram.getIndex().get(tid)
						.toFileString()
						+ " || " + s);
			}

		}

		return output;

	}

}
