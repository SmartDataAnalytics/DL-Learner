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

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;

import org.dllearner.algorithms.refexamples.*;

/**
 * TreeWindow
 * 
 * @author Tilo Hielscher
 */
public class TreeWindow extends JFrame implements TreeWillExpandListener {

	private static final long serialVersionUID = -5807192061389763835L;

	@SuppressWarnings("unused")
	private Config config;
	private EBNodeTreeModel ebNodeModel;
	private ExampleBasedNode rootNode;
	private JTree tree;

	public TreeWindow(Config config) {
		this.config = config;
		this.setTitle("DL-Learner Tree");
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLocationByPlatform(true);
		this.setSize(640, 300);

		// set icon
		if (this.getClass().getResource("icon.gif") != null)
			setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
					this.getClass().getResource("icon.gif")));

		// tree model
		if (config.getLearningAlgorithm() instanceof ExampleBasedROLComponent) {
			ExampleBasedROLComponent ebrol = (ExampleBasedROLComponent) config
					.getLearningAlgorithm();
			this.rootNode = ebrol.getStartNode();

			System.out.println("childs1: " + rootNode.getChildren());

			this.ebNodeModel = new EBNodeTreeModel(rootNode);

			// childrens to treeModel
			Object first = ebNodeModel.getChild(rootNode, 0);
			System.out.println("getIndexOfChild: " + ebNodeModel.getIndexOfChild(rootNode, first));

			// System.out.println("childs2: " +
			// ebNodeModel.getChildren((ExampleBasedNode) first));

			tree = new JTree(ebNodeModel);
			tree.addTreeWillExpandListener(this);
			this.add(new JScrollPane(tree));
		}

		// }
		this.repaint();
		this.setVisible(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TreeWillExpandListener#treeWillCollapse(javax.swing.event.TreeExpansionEvent)
	 */
	@Override
	public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TreeWillExpandListener#treeWillExpand(javax.swing.event.TreeExpansionEvent)
	 */
	@Override
	public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
		//System.out.println("getIndexOfChild: "+ ebNodeModel.getIndexOfChild(rootNode, event.getPath()));

		//System.out.println("row_for_path: " + this.tree.getRowForPath(event.getPath()));
		int index = this.tree.getRowForPath(event.getPath());
		// ebNodeModel.getChild(rootNode, 9);
		if (index > 0)
			ebNodeModel.getChild(rootNode, index);

	}

}
