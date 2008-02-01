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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

/**
 * StartGUI
 * 
 * @author Tilo Hielscher
 * 
 */
public class StartGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = -739265982906533775L;

    private JTabbedPane tabPane = new JTabbedPane();

    private Config config = new Config();

    private JPanel tab1 = new JPanel();
    private JPanel tab2 = new JPanel();
    private JPanel tab3 = new JPanel();
    private JPanel tab4 = new JPanel();
    private JPanel tab5 = new JPanel();

    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu("File");
    private JMenuItem openItem = new JMenuItem("Open Config");
    private JMenuItem saveItem = new JMenuItem("Save Config");

    public StartGUI() {
	this.setTitle("DL-Learner GUI");
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.setLocationByPlatform(true);
	this.setSize(800, 600);

	tab1.add(new KnowledgeSourcePanel(config));
	tab2.add(new ReasonerPanel(config));
	tab3.add(new LearningProblemPanel(config));
	tab4.add(new LearningAlgorithmPanel(config));
	tab5.add(new RunPanel(config));
	tabPane.addTab("Knowledge Source", tab1);
	tabPane.addTab("Reasoner", tab2);
	tabPane.addTab("Learning Problem", tab3);
	tabPane.addTab("Learning Algorithm", tab4);
	tabPane.addTab("Run", tab5);

	this.setJMenuBar(menuBar);
	menuBar.add(menuFile);
	menuFile.add(openItem);
	openItem.addActionListener(this);
	menuFile.add(saveItem);
	saveItem.addActionListener(this);

	this.add(tabPane);
	this.setVisible(true);
    }

    public static void main(String[] args) {
	// create GUI logger
	SimpleLayout layout = new SimpleLayout();
	ConsoleAppender consoleAppender = new ConsoleAppender(layout);
	Logger logger = Logger.getRootLogger();
	logger.removeAllAppenders();
	logger.addAppender(consoleAppender);
	logger.setLevel(Level.INFO);

	new StartGUI();
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == openItem) {
	    System.out.println("openItem was pressed");
	}
	if (e.getSource() == saveItem) {
	    System.out.println("saveItem was pressed");
	}
    }

}
