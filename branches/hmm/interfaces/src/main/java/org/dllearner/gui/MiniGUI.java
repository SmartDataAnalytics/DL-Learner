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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.DIGReasoner;

/**
 * Mini GUI Demo.
 * 
 * @author Jens Lehmann
 * 
 */
public class MiniGUI extends JPanel implements ActionListener {

	private static final long serialVersionUID = -4247224068574471307L;

	private static ComponentManager cm = ComponentManager.getInstance();
	private AbstractReasonerComponent rs;
	private File selectedFile;
	
	private JButton openButton;
	private JButton startLearnButton;
	private JTextField fileDisplay;
	private JFileChooser fc;
	private List<Individual> individuals;
	private JList exampleList;
	private JTextField solutionDisplay;
	
	public MiniGUI() {
		super(new BorderLayout());

		// create file chooser and a text field showing
		// the file name
		// (for simplicity we only allow to select one knowledge source)
		fc = new JFileChooser(new File("examples/"));
		openButton = new JButton("Open File ...");
		openButton.addActionListener(this);
		fileDisplay = new JTextField(40);
		fileDisplay.setEditable(false);
		
		JPanel openPanel = new JPanel();
		openPanel.add(fileDisplay);
		openPanel.add(openButton);
		
		// create a scrollable list of examples
		JPanel examplePanel = new JPanel();
		exampleList = new JList();
		exampleList.setLayoutOrientation(JList.VERTICAL);
		exampleList.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(exampleList);
		listScroller.setPreferredSize(new Dimension(250, 80));
		examplePanel.add(listScroller);
		
		// create a "learn" button and a text field for displaying
		// the obtained solution
		startLearnButton = new JButton("Learn Concept");
		startLearnButton.addActionListener(this);
		JPanel startLearnButtonPanel = new JPanel();
		startLearnButtonPanel.add(startLearnButton);
		solutionDisplay = new JTextField(50);
		solutionDisplay.setEditable(false);
		JPanel startPanel = new JPanel(new BorderLayout());
		startPanel.add(startLearnButtonPanel, BorderLayout.NORTH);
		startPanel.add(solutionDisplay, BorderLayout.SOUTH);

		// arrange the components on this panel
		add(openPanel, BorderLayout.PAGE_START);
		add(examplePanel, BorderLayout.CENTER);
		add(startPanel, BorderLayout.PAGE_END);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openButton) {
			int returnVal = fc.showOpenDialog(MiniGUI.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				selectedFile = fc.getSelectedFile();
				fileDisplay.setText(selectedFile.toString());
				
				// we blindly assume an OWL file was selected
				// (everything else will cause an exception)
				AbstractKnowledgeSource source = cm.knowledgeSource(OWLFile.class);
				cm.applyConfigEntry(source, "url", selectedFile.toURI().toString());				
				try {
					source.init();
				} catch (ComponentInitException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				// use a reasoner to find out which instances exist
				// in the background knowledge
				AbstractReasonerComponent reasoner = cm.reasoner(DIGReasoner.class, source);
				try {
					reasoner.init();
				} catch (ComponentInitException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//				rs = cm.reasoningService(reasoner);
				Set<Individual> individualsSet = reasoner.getIndividuals();
				individuals = new LinkedList<Individual>(individualsSet);
				
				DefaultListModel listModel = new DefaultListModel();
				for(Individual ind : individuals)
					listModel.addElement(ind);
				
				exampleList.setModel(listModel);
			} 
		} else if(e.getSource() == startLearnButton) {			
			// detect which examples have been selected			
			Set<String> exampleSet = new HashSet<String>();
			int[] selectedIndices = exampleList.getSelectedIndices();
			for(int i : selectedIndices)
				exampleSet.add(individuals.get(i).toString());
			
			// create a positive only learning problem
			AbstractLearningProblem lp = cm.learningProblem(PosOnlyLP.class, rs);
			cm.applyConfigEntry(lp, "positiveExamples", exampleSet);
			try {
				lp.init();
			} catch (ComponentInitException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			// try the refinement operator based learning algorithm to solve
			// the problem
			AbstractCELA la = null;
			try {
				la = cm.learningAlgorithm(ROLearner.class, lp, rs);
			} catch (LearningProblemUnsupportedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				la.init();
			} catch (ComponentInitException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			la.start();
			
			// wait for a solution (note that not all learning problems have a
			// solution (100% accuracy), so one usually has to run the algorithm in its own
			// thread, such that it can be aborted after some time
			Description solution = la.getCurrentlyBestDescription();
			solutionDisplay.setText(solution.toString());
		}
	}

	public static void main(String[] args) {

		// create a frame containing the main panel
		JFrame frame = new JFrame("Mini DL-Learner GUI Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MiniGUI());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

}
