/**
 * Copyright (C) 2007-2010, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.algorithms.qtl;

import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.junit.Assert;
import org.junit.Test;



/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class TreeSubsumptionTest{
	
	@Test
	public void test1(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?");
		Assert.assertTrue(tree1.isSubsumedBy(tree2));
	}
	
	@Test
	public void test2(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
		tree1.addChild(new QueryTreeImpl<String>("B", NodeType.RESOURCE), "r");
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?");
		QueryTreeImpl<String> child = new QueryTreeImpl<String>("A", NodeType.RESOURCE);
		child.addChild(new QueryTreeImpl<String>("B", NodeType.RESOURCE), "r");
		tree2.addChild(child, "r");
		Assert.assertFalse(tree1.isSubsumedBy(tree2));
	}
	
	@Test
	public void test3(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("?");
		tree1.addChild(new QueryTreeImpl<String>("B", NodeType.RESOURCE), "r");
		tree1.addChild(new QueryTreeImpl<String>("A", NodeType.RESOURCE), "s");
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?");
		tree2.addChild(new QueryTreeImpl<String>("A", NodeType.RESOURCE), "r");
		tree2.addChild(new QueryTreeImpl<String>("B", NodeType.RESOURCE), "r");
		tree2.addChild(new QueryTreeImpl<String>("C", NodeType.RESOURCE), "s");
		Assert.assertFalse(tree2.isSubsumedBy(tree1));
	}
	
	@Test
	public void test4(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("?");
		QueryTreeImpl<String> child = new QueryTreeImpl<String>("?");
		tree1.addChild(child, "r");
		child.addChild(new QueryTreeImpl<String>("?", NodeType.LITERAL), "s");
		QueryTreeImpl<String> subChild = new QueryTreeImpl<String>("?");
		child.addChild(subChild, "t");
		subChild.addChild(new QueryTreeImpl<String>("A", NodeType.RESOURCE), "u");
		tree1.dump();
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?");
		child = new QueryTreeImpl<String>("?");
		tree2.addChild(child, "r");
		child.addChild(new QueryTreeImpl<String>("?", NodeType.LITERAL), "s");
		subChild = new QueryTreeImpl<String>("?");
		child.addChild(subChild, "t");
		subChild.addChild(new QueryTreeImpl<String>("?"), "u");
		tree2.dump();
		
		System.out.println(tree1.isSubsumedBy(tree2));
		System.out.println(tree2.isSubsumedBy(tree1));
	}

}