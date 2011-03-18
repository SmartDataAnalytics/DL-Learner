package org.dllearner.algorithm.tbsl.ltag.data;

import java.io.StringReader;

import org.dllearner.algorithm.tbsl.ltag.reader.LTAGTreeParser;
import org.dllearner.algorithm.tbsl.ltag.reader.ParseException;


public class LTAG_Tree_Constructor {

	public TreeNode construct(String string) throws ParseException 
	{
		// new TreeNode interface in fracosem.ltag
		TreeNode tree;
		LTAGTreeParser parser =  new LTAGTreeParser(new StringReader(new String(string)));
		parser.ReInit(new StringReader(new String(string)));		
		tree = parser.Tree();
		
		return tree;
	}
	
	
}
