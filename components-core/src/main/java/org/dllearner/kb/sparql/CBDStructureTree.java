package org.dllearner.kb.sparql;

import org.dllearner.algorithms.qtl.datastructures.impl.GenericTree;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;

/**
 * @author Lorenz Buehmann
 */
public class CBDStructureTree extends GenericTree<String, CBDStructureTree> {

	private static final String ROOT_NODE = "root";
	private static final String IN_NODE = "in";
	private static final String OUT_NODE = "out";

	public CBDStructureTree() {
		super(ROOT_NODE);
	}

	public CBDStructureTree(String data) {
		super(data);
	}

	public boolean isInNode() {
		return data.equals(IN_NODE);
	}

	public boolean isOutNode() {
		return data.equals(OUT_NODE);
	}

	public CBDStructureTree addInNode() {
		CBDStructureTree child = new CBDStructureTree(IN_NODE);
		addChild(child);
		return child;
	}

	public CBDStructureTree addOutNode() {
		CBDStructureTree child = new CBDStructureTree(OUT_NODE);
		addChild(child);
		return child;
	}

	public boolean hasOutChild() {
		return children.stream().anyMatch(CBDStructureTree::isOutNode);
	}

	public boolean hasInChild() {
		return children.stream().anyMatch(CBDStructureTree::isInNode);
	}

	public static CBDStructureTree fromTreeString(String treeString) {
		treeString = treeString.replace(":", "");

		int i = treeString.indexOf("[");
		String token = treeString.substring(0, i);
		CBDStructureTree tree = new CBDStructureTree(token);
		parseString(treeString.substring(i + 1, treeString.length() - 1), tree);
		return tree;
	}

	private static int parseString(String input, CBDStructureTree parent) {
		StringBuilder currentString = new StringBuilder();
		int index = 0;
		CBDStructureTree child = null;
		while(index < input.length()) {
			char c = input.charAt(index);

			if(c == ' ') { // ignore spaces
				index++;
				continue;
			}

			if(c == ',') { // end of menu entry, add to the list
				if(!currentString.toString().isEmpty()) {
					child = new CBDStructureTree(currentString.toString());
					if(parent != null)
						parent.addChild(child);
				}
				currentString.delete(0, currentString.length());
				index++;
				continue;
			}

			if(c == ']') { // end of sublist, return
				return index + 1;
			}

			if(c == '[') { // start of sublist, recursive call
				if(!currentString.toString().isEmpty()) {
					child = new CBDStructureTree(currentString.toString());
					if(parent != null)
						parent.addChild(child);
				}
				currentString.delete(0, currentString.length());

				int temp = parseString(input.substring(index + 1), child);
				index += temp;
				index++;
				continue;
			}

			currentString.append(c);
			index++;
		}
		return 0;
	}

	public static void main(String[] args) {
		CBDStructureTree tree = new CBDStructureTree();
		tree.addInNode().addInNode();
		System.out.println(tree.toStringVerbose());
		fromTreeString("root:[in:[in:[]],out:[in:[]]]");
	}
}
