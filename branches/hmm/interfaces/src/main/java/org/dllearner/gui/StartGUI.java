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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.el.ELLearningAlgorithm;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosNegLPStrict;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.FastRetrievalReasoner;

/**
 * This class builds the basic GUI elements and is used to start the DL-Learner
 * GUI.
 * 
 * @author Tilo Hielscher
 * @author Jens Lehmann
 */
public class StartGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = -739265982906533775L;

	private static Logger logger = Logger.getLogger(StartGUI.class);	
	private JTabbedPane tabPane = new JTabbedPane();

	private Config config = new Config(this);

//	private ConfigLoad configLoad = new ConfigLoad(config, this);
//	private ConfigSave configSave = new ConfigSave(config, this);

	// the four component panels
	protected ComponentPanel[] panels = new ComponentPanel[4];
	protected RunPanel runPanel;
	private int currentPanelIndex = 0;
	
//	protected KnowledgeSourcePanel tab0;
//	protected ReasonerPanel tab1;
//	protected LearningProblemPanel tab2;
//	protected LearningAlgorithmPanel tab3;

	private JMenuBar menuBar = new JMenuBar();
	private JMenu menuFile = new JMenu("File");
	private JMenuItem openItem = new JMenuItem("Open Conf File ...");
	private JMenuItem saveItem = new JMenuItem("Save As Conf File ...");
	private JMenuItem exitItem = new JMenuItem("Exit");
	private JMenu menuHelp = new JMenu("Help");
	private JMenuItem aboutItem = new JMenuItem("About");
	private JMenuItem tutorialItem = new JMenuItem("Quick Tutorial");

	private StatusPanel statusPanel = new StatusPanel();
	
	public StartGUI() {
		this(null);
	}

	public StartGUI(File file) {
		this.setTitle("DL-Learner GUI");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		this.setLocationByPlatform(true);
		this.setSize(800, 600);

		// center frame
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension size = getSize();
		screenSize.height = screenSize.height/2;
		screenSize.width = screenSize.width/2;
		size.height = size.height/2;
		size.width = size.width/2;
		int y = screenSize.height - size.height;
		int x = screenSize.width - size.width;
		setLocation(x, y);

		// set icon
		if (this.getClass().getResource("icon.gif") != null)
			setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
					this.getClass().getResource("icon.gif")));

		// create panels
		List<Class<? extends AbstractComponent>> ignoredKnowledgeSources = new LinkedList<Class<? extends AbstractComponent>>();
		ignoredKnowledgeSources.add(OWLAPIOntology.class);
		panels[0] = new ComponentPanel(config, this, AbstractKnowledgeSource.class, OWLFile.class, ignoredKnowledgeSources);
		List<Class<? extends AbstractComponent>> ignoredReasoners = new LinkedList<Class<? extends AbstractComponent>>();
		ignoredReasoners.add(FastRetrievalReasoner.class);
		panels[1] = new ComponentPanel(config, this, AbstractReasonerComponent.class, FastInstanceChecker.class, ignoredReasoners);
		List<Class<? extends AbstractComponent>> ignoredLearningProblems = new LinkedList<Class<? extends AbstractComponent>>();
		ignoredLearningProblems.add(PosNegLPStrict.class);
		panels[2] = new ComponentPanel(config, this, AbstractLearningProblem.class, PosNegLPStandard.class, ignoredLearningProblems);
		List<Class<? extends AbstractComponent>> ignoredAlgorithms = new LinkedList<Class<? extends AbstractComponent>>();
		ignoredAlgorithms.add(ELLearningAlgorithm.class);
		panels[3] = new ComponentPanel(config, this, AbstractCELA.class, OCEL.class, ignoredAlgorithms);
		runPanel = new RunPanel(config, this);		
		
		// add tabs for panels
		tabPane.addTab("Knowledge Source", panels[0]);
		tabPane.addTab("Reasoner", panels[1]);
		tabPane.addTab("Learning Problem", panels[2]);
		tabPane.addTab("Learning Algorithm", panels[3]);
		tabPane.addTab("Run", runPanel);
		
		/*
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
		*/

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

		add(tabPane, BorderLayout.CENTER);
		add(statusPanel, BorderLayout.SOUTH);
		setVisible(true);
		updateTabs();

		// load file
		if(file != null) {
			config.loadFile(file);
		}
		
		// Register a change listener
		tabPane.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				if (evt.getSource().equals(tabPane)) {

					int index = tabPane.getSelectedIndex();
//					System.out.println(index);
					
					// a list of all components (0 = knowledge source,
					// 1 = reasoner etc.) which have to be initialised;
					// the user can init several components at once
					List<Integer> componentsToInit = new LinkedList<Integer>();
					// check whether we need to initialise components
					if (index != 0 && config.tabNeedsInit(index - 1)) {
						for (int i = 0; i < index; i++) {
							if(config.tabNeedsInit(i)) {
								componentsToInit.add(i);
							}
						}
					}
					config.init(componentsToInit);

					currentPanelIndex = index;
					updateTabs();
					updateStatusPanel();
					
					// send signals to panels
					switch(index) {
					case 0: panels[0].panelActivated(); break;
					case 1: panels[1].panelActivated(); break;
					case 2: panels[2].panelActivated(); break;
					case 3: panels[3].panelActivated(); break;
					}

					
					
				}
			}
		});

//		if (file != null) {
//			configLoad.openFile(file);
//			configLoad.startParser();
//		}
	}

	/*
	public void init() {
		tab0.init();
		tab1.init();
		tab2.init();
		tab3.init();
		updateTabColors();
	}*/

	public void updateStatusPanel() {
		// new tab selected => generate an appropriate status message
		// (e.g. user has to fill in values)
		if(currentPanelIndex == 4) {
			statusPanel.setRunPanelMessage();
		} else {
			if(config.mandatoryOptionsSpecified(panels[currentPanelIndex].getCurrentComponent())) {
				statusPanel.setTabCompleteMessage();
			} else {
				statusPanel.setTabInitMessage();
			}
		}		
	}
	
	public static void main(String[] args) {
		// create GUI logger
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.removeAllAppenders();
		rootLogger.addAppender(consoleAppender);
		rootLogger.setLevel(Level.DEBUG);

		File file = null;
		if (args.length > 0)
			file = new File(args[args.length - 1]);

		// force platform look and feel
		try {
			UIManager.setLookAndFeel(
					UIManager.getCrossPlatformLookAndFeelClassName());
//			 "com.sun.java.swing.plaf.motif.MotifLookAndFeel");
//					UIManager.getSystemLookAndFeelClassName());
			// TODO: currently everything is in bold on Linux (and Win?)
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		new StartGUI(file);
	}

	public void actionPerformed(ActionEvent e) {
		// open config file
		if (e.getSource() == openItem) {
			// file dialog
			JFileChooser fc = new ExampleFileChooser("conf");
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				logger.info("Loading file " + fc.getSelectedFile() + ".");
				config.loadFile(fc.getSelectedFile());
//				configLoad.openFile(fc.getSelectedFile());
//				configLoad.startParser();
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
				File file = fc.getSelectedFile();
				// returns name without path to it
				String name= file.getName();
				// if there is no extension, we append .conf
				if(!name.contains(".")) {
					file = new File(file.getAbsolutePath() + ".conf");
				}
				logger.info("Saving current configuration to " + file + ".");
				ConfigSave save = new ConfigSave(config);
				try {
					save.saveFile(file);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			// exit
		} else if (e.getSource() == exitItem) {
			dispose();
			// tutorial
		} else if (e.getSource() == tutorialItem) {
			TutorialWindow window = new TutorialWindow();
			window.setLocationRelativeTo(this);
			// about
		} else if (e.getSource() == aboutItem) {
			AboutWindow window = new AboutWindow();
			window.setLocationRelativeTo(this);
		}
	}

	/**
	 * Update colors of tabulators; red should be clicked, black for OK.
	 */
	public void updateTabs() {
		for(int i=0; i<4; i++) {
			// red = needs init, black = initialised
			if(config.tabNeedsInit(i)) {
				tabPane.setForegroundAt(i, Color.RED);
			} else {
				tabPane.setForegroundAt(i, Color.BLACK);
			}
			
			// only enable tabs, which can be selected
			// (note the i+1: knowledge source always enabled)
			tabPane.setEnabledAt(i+1, config.isEnabled(i));			
		}
		
		// run panel is enabled if all mandatory algorithm parameters have been set 
//		tabPane.setEnabledAt(4, config.isEnabled(4));
		
//		if (config.needsInitKnowledgeSource())
//			tabPane.setForegroundAt(0, Color.RED);
//		else
//			tabPane.setForegroundAt(0, Color.BLACK);
//		if (config.needsInitReasoner())
//			tabPane.setForegroundAt(1, Color.RED);
//		else
//			tabPane.setForegroundAt(1, Color.BLACK);
//		if (config.needsInitLearningProblem())
//			tabPane.setForegroundAt(2, Color.RED);
//		else
//			tabPane.setForegroundAt(2, Color.BLACK);
//		if (config.needsInitLearningAlgorithm()) {
//			tabPane.setForegroundAt(3, Color.RED);
//			tabPane.setForegroundAt(4, Color.RED);
//		} else {
//			tabPane.setForegroundAt(3, Color.BLACK);
//			tabPane.setForegroundAt(4, Color.BLACK);
//		}

		// commented out as I do not see any reason why the method should update
		// everything
		// (it costs performance to update everything when the user only sees
		// one panel)
		// tab0.updateAll();
		// tab1.updateAll();
		// tab2.updateAll();
		// tab3.updateAll();

	}
	
	// freeze tab
	public void disableTabbedPane() {
		tabPane.setEnabled(false);
	}
	
	public void enableTabbedPane() {
		tabPane.setEnabled(true);
	}
	
	public void setStatusMessage(String message) {
		statusPanel.setStatus(message);
	}

	/**
	 * @return the statusPanel
	 */
	public StatusPanel getStatusPanel() {
		return statusPanel;
	}
}
