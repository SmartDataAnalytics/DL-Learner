package org.dllearner.gui;

/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

import javax.swing.*;
import javax.swing.tree.*;
import org.dllearner.algorithms.refexamples.*;

/**
 * TreeWindow
 * 
 * @author Tilo Hielscher
 */
public class TreeWindow extends JFrame {

	private static final long serialVersionUID = -5807192061389763835L;

	@SuppressWarnings("unused")
	private Config config;

	public TreeWindow(Config config) {
		this.config = config;
		this.setTitle("DL-Learner Tree");
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLocationByPlatform(true);
		this.setSize(300, 400);

		// set icon
		setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
				this.getClass().getResource("icon.gif")));

		// test example tree
		DefaultMutableTreeNode exampleRoot = new DefaultMutableTreeNode("root");
		for (int knot = 0; knot < 4; knot++) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode("knot " + knot);
			exampleRoot.add(node);
			for (int leaf = 1; leaf < 4; leaf++)
				node.add(new DefaultMutableTreeNode("leaf " + (knot * 3 + leaf)));
		}

		// tree model
		EBNodeTreeModel root = null;
		// if (config.getLearningAlgorithm() != null) {
		if (config.getLearningAlgorithm() instanceof ExampleBasedROLComponent) {
			ExampleBasedROLComponent ebrol = (ExampleBasedROLComponent) config
					.getLearningAlgorithm();
			ExampleBasedNode rootNode = ebrol.getStartNode();
			root = new EBNodeTreeModel(rootNode);

			// show tree
			if (root != null) {
				//System.out.println("TEST: " + root.getChildCount(rootNode));
				for (int i = 0; i < root.getChildCount(rootNode); i++) {
					// Object node = root.getChild(rootNode, i);
					
					root.getChild(rootNode, i);
					//System.out.println("TEST: " + root.getChild(rootNode, i));
				}
				JTree tree = new JTree(root);
				this.add(new JScrollPane(tree));
			}
		} else {
			JTree tree = new JTree(exampleRoot);
			this.add(new JScrollPane(tree));
		}

		// }
		this.repaint();
		this.setVisible(true);
	}

}
