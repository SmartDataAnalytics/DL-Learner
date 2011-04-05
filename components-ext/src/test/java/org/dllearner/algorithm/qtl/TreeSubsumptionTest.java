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
package org.dllearner.algorithm.qtl;

import org.dllearner.algorithm.qtl.datastructures.impl.QueryTreeImpl;
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
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A");
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?");
		Assert.assertTrue(tree1.isSubsumedBy(tree2));
	}
	
	@Test
	public void test2(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("A");
		tree1.addChild(new QueryTreeImpl<String>("B"), "r");
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?");
		QueryTreeImpl<String> child = new QueryTreeImpl<String>("A");
		child.addChild(new QueryTreeImpl<String>("B"), "r");
		tree2.addChild(child, "r");
		Assert.assertFalse(tree1.isSubsumedBy(tree2));
	}
	
	@Test
	public void test3(){
		QueryTreeImpl<String> tree1 = new QueryTreeImpl<String>("?");
		tree1.addChild(new QueryTreeImpl<String>("B"), "r");
		tree1.addChild(new QueryTreeImpl<String>("A"), "s");
		
		QueryTreeImpl<String> tree2 = new QueryTreeImpl<String>("?");
		tree2.addChild(new QueryTreeImpl<String>("A"), "r");
		tree2.addChild(new QueryTreeImpl<String>("B"), "r");
		tree2.addChild(new QueryTreeImpl<String>("C"), "s");
		Assert.assertFalse(tree2.isSubsumedBy(tree1));
	}

}
