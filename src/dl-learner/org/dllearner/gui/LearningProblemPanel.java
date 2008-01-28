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

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.LearningProblem;
import org.dllearner.core.dl.Individual;

/**
 * LearningProblemPanel
 * 
 * @author Tilo Hielscher
 */
public class LearningProblemPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = -3819627680918930203L;

    private List<Class<? extends LearningProblem>> problems;
    private String[] lpBoxItems = {};
    private JComboBox cb = new JComboBox(lpBoxItems);
    private JPanel choosePanel = new JPanel();
    private JPanel centerPanel = new JPanel();
    private JPanel lpPanel = new JPanel();
    private JLabel posLabel = new JLabel("positive Examples");
    private JLabel negLabel = new JLabel("negative Examples");
    private JButton initButton, readListButton;
    private int choosenClassIndex;
    private List<Individual> individuals;
    private JList posList = new JList();
    private JList negList = new JList();

    private Config config;

    LearningProblemPanel(final Config config) {
	super(new BorderLayout());

	this.config = config;

	initButton = new JButton("Init LearningProblem");
	initButton.addActionListener(this);

	readListButton = new JButton("Read List");
	readListButton.addActionListener(this);

	choosePanel.add(cb);
	choosePanel.add(readListButton);
	lpPanel.add(initButton);
	add(choosePanel, BorderLayout.PAGE_START);
	add(centerPanel, BorderLayout.CENTER);
	add(lpPanel, BorderLayout.PAGE_END);

	problems = config.getComponentManager().getLearningProblems();

	// add into comboBox
	for (int i = 0; i < problems.size(); i++) {
	    // cb.addItem(problems.get(i).getSimpleName());
	    cb.addItem(config.getComponentManager().getComponentName(
		    problems.get(i)));
	}

	// read choosen LearningProblem
	choosenClassIndex = cb.getSelectedIndex();

	// create a scrollable list of positive examples
	posList = new JList();
	posList.setLayoutOrientation(JList.VERTICAL);
	posList.setVisibleRowCount(-1);
	JScrollPane posListScroller = new JScrollPane(posList);
	posListScroller.setPreferredSize(new Dimension(300, 200));

	// create a scrollable list of negative examples
	negList = new JList();
	negList.setLayoutOrientation(JList.VERTICAL);
	negList.setVisibleRowCount(-1);
	JScrollPane negListScroller = new JScrollPane(negList);
	negListScroller.setPreferredSize(new Dimension(300, 200));

	// define GridBag
	GridBagLayout gridbag = new GridBagLayout();
	centerPanel.setLayout(gridbag);
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;

	buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
	gridbag.setConstraints(posLabel, constraints);
	centerPanel.add(posLabel);

	buildConstraints(constraints, 1, 0, 1, 1, 100, 100);
	gridbag.setConstraints(negLabel, constraints);
	centerPanel.add(negLabel);

	buildConstraints(constraints, 0, 1, 1, 1, 100, 100);
	gridbag.setConstraints(posListScroller, constraints);
	centerPanel.add(posListScroller);

	buildConstraints(constraints, 1, 1, 1, 1, 100, 100);
	gridbag.setConstraints(negListScroller, constraints);
	centerPanel.add(negListScroller);

	add(centerPanel, BorderLayout.CENTER);

	// listener for posList
	posList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent evt) {
		if (evt.getValueIsAdjusting())
		    return;
		// detect witch examples have been selected
		Set<String> posExampleSet = new HashSet<String>();
		int[] selectedIndices = posList.getSelectedIndices();
		for (int i : selectedIndices)
		    posExampleSet.add(individuals.get(i).toString());
		config.setPosExampleSet(posExampleSet);
	    }
	});

	// listener for negList
	negList.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent evt) {
		if (evt.getValueIsAdjusting())
		    return;
		// detect witch examples have been selected
		Set<String> negExampleSet = new HashSet<String>();
		int[] selectedIndices = negList.getSelectedIndices();
		for (int i : selectedIndices)
		    negExampleSet.add(individuals.get(i).toString());
		config.setNegExampleSet(negExampleSet);
	    }
	});
    }

    public void actionPerformed(ActionEvent e) {
	// read selected LearningProblemClass
	choosenClassIndex = cb.getSelectedIndex();

	// get list after reasoner init
	if (e.getSource() == readListButton
		&& config.getReasoningService() != null) {
	    // fill lists
	    Set<Individual> individualsSet = config.getReasoningService()
		    .getIndividuals();
	    individuals = new LinkedList<Individual>(individualsSet);
	    DefaultListModel listModel = new DefaultListModel();
	    for (Individual ind : individuals) {
		listModel.addElement(ind);
	    }
	    posList.setModel(listModel);
	    negList.setModel(listModel);
	}

	// init
	if (e.getSource() == initButton
		&& config.getReasoningService() != null
		&& (config.getPosExampleSet().size() > 0 || config
			.getNegExampleSet().size() > 0)) {
	    config.setLearningProblem(config.getComponentManager()
		    .learningProblem(problems.get(choosenClassIndex),
			    config.getReasoningService()));
	    config.getComponentManager().applyConfigEntry(
		    config.getLearningProblem(), "positiveExamples",
		    config.getPosExampleSet());
	    config.getComponentManager().applyConfigEntry(
		    config.getLearningProblem(), "negativeExamples",
		    config.getNegExampleSet());
	    config.getLearningProblem().init();
	    System.out.println("init LearningProblem");
	}

    }

    /*
     * Define GridBagConstraints
     */
    private void buildConstraints(GridBagConstraints gbc, int gx, int gy,
	    int gw, int gh, int wx, int wy) {
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = wx;
	gbc.weighty = wy;
    }

}
