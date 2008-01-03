package org.dllearner.gui;

/**
 * Copyright (C) 2007, Jens Lehmann
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

/**
 * StartGUI
 * 
 * @author Tilo Hielscher
 * 
 */

public class StartGUI extends JFrame {
	
	private static final long serialVersionUID = -739265982906533775L;
	
	public static Config config = new Config();
	
	private static final JTabbedPane tabPane = new JTabbedPane();
	private JPanel tab1 = new JPanel();
	private JPanel tab2 = new JPanel();
	private JPanel tab3 = new JPanel();
	private JPanel tab4 = new JPanel();
	private JPanel tab5 = new JPanel();

	public StartGUI() {
		this.setTitle("DL-Learner GUI");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationByPlatform(true);
		this.setSize(640, 480);
		tab1.add(new KnowledgeSourcePanel());
		tab2.add(new ReasonerPanel());
		tab3.add(new LearningProblemPanel());
		tab4.add(new LearningAlgorithmPanel());
		tab5.add(new OutputPanel());
		tabPane.addTab("Knowledge Source", tab1);
		tabPane.addTab("Reasoner", tab2);
		tabPane.addTab("Learning Problem", tab3);
		tabPane.addTab("Learning Algortihm", tab4);
		tabPane.addTab("Output", tab5);
		this.add(tabPane);
		this.setVisible(true);
	}

	public static void main(String[] args) {
		new StartGUI();
	}
	
	protected static void renew() {
		tabPane.repaint();
	}
}
