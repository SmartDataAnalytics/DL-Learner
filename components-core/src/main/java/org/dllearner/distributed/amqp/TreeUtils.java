package org.dllearner.distributed.amqp;

import java.util.Map;

public class TreeUtils {

	public static String toTreeString(SearchTree tree,
			String baseURI, Map<String, String> prefixes) {
		return TreeUtils.toTreeString(tree.getRoot(), baseURI, prefixes);
	}
	public static String toTreeString(OENode node,
			String baseURI, Map<String, String> prefixes) {
		return TreeUtils.toTreeString(node, 0, baseURI, prefixes).toString();
	}

	private static StringBuilder toTreeString(OENode node,
			int depth, String baseURI, Map<String, String> prefixes) {
		StringBuilder treeString = new StringBuilder();
		for(int i=0; i<depth-1; i++)
			treeString.append("  ");
		if(depth!=0)
			treeString.append("|--> ");
		treeString.append(node.getShortDescription(baseURI, prefixes)).append("\n");
		for(OENode child : node.getChildren()) {
			treeString.append(TreeUtils.toTreeString(child, depth+1, baseURI, prefixes));
		}
		return treeString;
	}
}
