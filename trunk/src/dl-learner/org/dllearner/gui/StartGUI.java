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
package org.dllearner.gui;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import javax.swing.filechooser.FileFilter;

/**
 * This class builds the basic GUI elements and is used to start the DL-Learner GUI.
 * 
 * @author Tilo Hielscher
 * @author Jens Lehmann
 */
public class StartGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = -739265982906533775L;

	private JTabbedPane tabPane = new JTabbedPane();

	private Config config = new Config();

	private ConfigLoad configLoad = new ConfigLoad(config, this);
	private ConfigSave configSave = new ConfigSave(config, this);

	private KnowledgeSourcePanel tab0;
	private ReasonerPanel tab1;
	private LearningProblemPanel tab2;
	private LearningAlgorithmPanel tab3;
	private RunPanel tab4;

	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu("File");
	private JMenuItem openItem = new JMenuItem("Open Conf File ...");
	private JMenuItem saveItem = new JMenuItem("Save As Conf File ...");
	private JMenuItem exitItem = new JMenuItem("Exit");
	private JMenu menuHelp = new JMenu("Help");
	private JMenuItem aboutItem = new JMenuItem("About");
	private JMenuItem tutorialItem = new JMenuItem("Tutorial");

	public StartGUI() {
		this(null);
	}

	public StartGUI(File file) {
		this.setTitle("DL-Learner GUI");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationByPlatform(true);
		this.setSize(800, 600);
		
		// set icon
		if (this.getClass().getResource("icon.gif") != null)
			setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
					this.getClass().getResource("icon.gif")));

		tab0 = new KnowledgeSourcePanel(config, this);
		tab1 = new ReasonerPanel(config, this);
		tab2 = new LearningProblemPanel(config, this);
		tab3 = new LearningAlgorithmPanel(config, this);
		tab4 = new RunPanel(config, this);
		tabPane.addTab("Knowledge Source", tab0);
		tabPane.addTab("Reasoner", tab1);
		tabPane.addTab("Learning Problem", tab2);
		tabPane.addTab("Learning Algorithm", tab3);
		tabPane.addTab("Run", tab4);

		setJMenuBar(menuBar);
		menuBar.add(menuFile);
		menuFile.add(openItem);
		openItem.addActionListener(this);
		menuFile.add(saveItem);
		saveItem.addActionListener(this);
		menuFile.add(exitItem);
		exitItem.addActionListener(this);
		menuBar.add(menuHelp);
		menuHelp.add(tutorialItem);
		tutorialItem.addActionListener(this);
		menuHelp.add(aboutItem);
		aboutItem.addActionListener(this);

		add(tabPane);
		setVisible(true);
		updateTabColors();

		// Register a change listener
		tabPane.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				init();
			}
		});

		if (file != null) {
			configLoad.openFile(file);
			configLoad.startParser();
		}
	}

	public void init() {
		tab0.init();
		tab1.init();
		tab2.init();
		tab3.init();
		updateTabColors();
	}

	public static void main(String[] args) {
		// create GUI logger
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.INFO);

		File file = null;
		if (args.length > 0)
			file = new File(args[args.length - 1]);

		new StartGUI(file);
	}

	public void actionPerformed(ActionEvent e) {
		// open config file
		if (e.getSource() == openItem) {
			// file dialog
			JFileChooser fc = new JFileChooser(new File("examples/"));
			// FileFilter only *.conf
			fc.addChoosableFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isDirectory())
						return true;
					return f.getName().toLowerCase().endsWith(".conf");
				}

				@Override
				public String getDescription() {
					return "*.conf"; // name for filter
				}
			});
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				System.out.println("FILE: " + fc.getSelectedFile());
				configLoad.openFile(fc.getSelectedFile());
				configLoad.startParser();
			}
		// save as config file
		} else if (e.getSource() == saveItem) {
			JFileChooser fc = new JFileChooser(new File("examples/"));
			// FileFilter only *.conf
			fc.addChoosableFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isDirectory())
						return true;
					return f.getName().toLowerCase().endsWith(".conf");
				}

				@Override
				public String getDescription() {
					return "*.conf"; // name for filter
				}
			});
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				// System.out.println("FILE: " + fc.getSelectedFile());
				File file = fc.getSelectedFile();
				try {
					PrintWriter out = new PrintWriter(new FileWriter(file));
					// out.println("test");
					configSave.startParser(out);
					out.flush();
					out.close();
				} catch (Exception ex2) {
					System.out.println(ex2);
				}
			}
			System.out.println("config file saved");
		// exit
		} else if (e.getSource() == exitItem) {
			dispose();
		// tutorial
		} else if (e.getSource() == tutorialItem) {
			new TutorialWindow();
		// about
		} else if (e.getSource() == aboutItem) {
			new AboutWindow();
		}
	}

	/**
	 * Update colors of tabulators; red should be clicked, black for OK.
	 */
	public void updateTabColors() {
		if (config.isInitKnowledgeSource())
			tabPane.setForegroundAt(0, Color.BLACK);
		else
			tabPane.setForegroundAt(0, Color.RED);
		if (config.isInitReasoner())
			tabPane.setForegroundAt(1, Color.BLACK);
		else
			tabPane.setForegroundAt(1, Color.RED);
		if (config.isInitLearningProblem())
			tabPane.setForegroundAt(2, Color.BLACK);
		else
			tabPane.setForegroundAt(2, Color.RED);
		if (config.isInitLearningAlgorithm()) {
			tabPane.setForegroundAt(3, Color.BLACK);
			tabPane.setForegroundAt(4, Color.BLACK);
		} else {
			tabPane.setForegroundAt(3, Color.RED);
			tabPane.setForegroundAt(4, Color.RED);
		}
		tab0.updateAll();
		tab1.updateAll();
		tab2.updateAll();
		tab3.updateAll();

	}
}
