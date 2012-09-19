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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.PelletReasoner;
import org.dllearner.reasoning.ProtegeReasoner;

/**
 * Class displaying a component (and its options).
 * 
 * @author Jens Lehmann
 * 
 */
public class ComponentPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -7678275020058043937L;

	private Config config;
	private StartGUI startGUI;
	private List<Class<? extends AbstractComponent>> selectableComponents;
	private OptionPanel optionPanel;
	private Class<? extends AbstractComponent> panelClass;
	private AbstractComponent currentComponent;

	// GUI elements
	private JButton clearButton;
	private JComboBox comboBox = new JComboBox();

	/**
	 * Calls this(config, startGUI, panelClass, defaultComponent,null).
	 * 
	 * @param config
	 *            See main constructor.
	 * @param startGUI
	 *            See main constructor.
	 * @param panelClass
	 *            See main constructor.
	 * @param defaultComponent
	 *            See main constructor.
	 */
	ComponentPanel(final Config config, StartGUI startGUI, Class<? extends AbstractComponent> panelClass,
			Class<? extends AbstractComponent> defaultComponent) {
		this(config, startGUI, panelClass, defaultComponent, null);
	}

	/**
	 * Constructs a panel for configuring a component.
	 * 
	 * @param config
	 *            Central configuration handler.
	 * @param startGUI
	 *            Central GUI class.
	 * @param panelClass
	 *            Class of this panel, e.g. one of KnowledgeSource.class,
	 *            ReasonerComponent.class, ...
	 * @param defaultComponent
	 *            The default component, e.g. OWLFile.class.
	 * @param ignoredComponents
	 *            Components of DL-Learner, which should not be displayed.
	 */
	ComponentPanel(final Config config, StartGUI startGUI, Class<? extends AbstractComponent> panelClass,
			Class<? extends AbstractComponent> defaultComponent,
			List<Class<? extends AbstractComponent>> ignoredComponents) {
		super(new BorderLayout());

		this.config = config;
		this.startGUI = startGUI;
		this.panelClass = panelClass;

		// get all classes of the correct type
		selectableComponents = new LinkedList<Class<? extends AbstractComponent>>();
		if (panelClass == AbstractKnowledgeSource.class) {
			selectableComponents.addAll(config.getComponentManager().getKnowledgeSources());
		} else if (panelClass == AbstractReasonerComponent.class) {
			selectableComponents.addAll(config.getComponentManager().getReasonerComponents());
			selectableComponents.remove(PelletReasoner.class);
			selectableComponents.remove(ProtegeReasoner.class);
		} else if (panelClass == AbstractLearningProblem.class) {
			selectableComponents.addAll(config.getComponentManager().getLearningProblems());
		} else if (panelClass == AbstractCELA.class) {
//			selectableComponents.addAll(config.getComponentManager().getLearningAlgorithms());
			selectableComponents.addAll(config.getComponentManager().getApplicableLearningAlgorithms(config.getLearningProblem().getClass()));
		}

		// set default component class (move it to first position)
		selectableComponents.remove(defaultComponent);
		selectableComponents.add(0, defaultComponent);

		// remove ignored component classes
		if (ignoredComponents != null) {
			selectableComponents.removeAll(ignoredComponents);
		}

		// create combo box
		for (int i = 0; i < selectableComponents.size(); i++) {
			comboBox.addItem(config.getComponentManager().getComponentName(
					selectableComponents.get(i)));
			// comboBox.addItem(selectableComponents.get(i));
		}
		comboBox.addActionListener(this);

		clearButton = new JButton("Reset to Default Values");
		clearButton.addActionListener(this);

		// create upper panel
		JPanel choosePanel = new JPanel();
		choosePanel.add(comboBox);
		// choosePanel.add(new JLabel(" "));
		choosePanel.add(clearButton);

		// we create a new default component
		currentComponent = newInstance(defaultComponent);
		optionPanel = new OptionPanel(config, currentComponent);

		add(choosePanel, BorderLayout.NORTH);
		add(optionPanel, BorderLayout.CENTER);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == comboBox) {
			// change component and update option panel
			Class<? extends AbstractComponent> c = selectableComponents.get(comboBox.getSelectedIndex());
			currentComponent = changeInstance(c);
			// we may have to change the learning algorithm depending on the learning problem
			if(c.equals(ClassLearningProblem.class) || c.equals(PosOnlyLP.class)) {
				try {
					config.changeLearningAlgorithm(CELOE.class);
				} catch (LearningProblemUnsupportedException e1) {
					// cannot happend since CELOE supports class learning problem
					e1.printStackTrace();
				}
			}
						
			updateOptionPanel();
			// if the component does not have mandatory values, we can
			// enable the following tabs
			config.enableComponentsIfPossible();
			startGUI.updateTabs();
		} else if (e.getSource() == clearButton) {
			// clearing everything corresponds to changing to an unconfigured
			// component of the same type
			currentComponent = changeInstance(currentComponent.getClass());
			updateOptionPanel();
		}
	}

	/**
	 * Update according to (possibly changed) config.
	 */
	public void update() {
		// detect current component
		if (panelClass == AbstractKnowledgeSource.class) {
			currentComponent = config.getKnowledgeSource();
		} else if (panelClass == AbstractReasonerComponent.class) {
			currentComponent = config.getReasoner();
		} else if (panelClass == AbstractLearningProblem.class) {
			currentComponent = config.getLearningProblem();
		} else if (panelClass == AbstractCELA.class) {
			currentComponent = config.getLearningAlgorithm();
		}
		// select component without sending an event;
		// we need to disable events send by JComboBox
		comboBox.removeActionListener(this);
		String val = config.getComponentManager().getComponentName(currentComponent.getClass());
		comboBox.setSelectedItem(val);
		comboBox.addActionListener(this);

		// update option panel accordingly
		updateOptionPanel();
	}

	/**
	 * Updates the option panel.
	 */
	public void updateOptionPanel() {
		optionPanel.rebuild(currentComponent);
	}

	/**
	 * Method performing actions when panel is activated.
	 */
	public void panelActivated() {
		// hook method, which does nothing yet
		if(panelClass.equals(AbstractCELA.class)) {
			// update selectable components
			selectableComponents.clear();
			selectableComponents.addAll(config.getComponentManager().getApplicableLearningAlgorithms(config.getLearningProblem().getClass()));
			// clear combo box and add selectable items to it
			comboBox.removeActionListener(this);
			comboBox.removeAllItems();
			// recreate combo box
			for (int i = 0; i < selectableComponents.size(); i++) {
				comboBox.addItem(config.getComponentManager().getComponentName(
						selectableComponents.get(i)));
			}			
			comboBox.addActionListener(this);
			update();
		}
	}

	// creates an instance of the specified component class
	@SuppressWarnings("unchecked")
	private AbstractComponent newInstance(Class<? extends AbstractComponent> clazz) {
		AbstractComponent newComponent = null;
		if (AbstractKnowledgeSource.class.isAssignableFrom(clazz)) {
			newComponent = config.newKnowledgeSource((Class<AbstractKnowledgeSource>) clazz);
		} else if (AbstractReasonerComponent.class.isAssignableFrom(clazz)) {
			newComponent = config.newReasoner((Class<AbstractReasonerComponent>) clazz);
		} else if (AbstractLearningProblem.class.isAssignableFrom(clazz)) {
			newComponent = config.newLearningProblem((Class<AbstractLearningProblem>) clazz);
		} else if (AbstractCELA.class.isAssignableFrom(clazz)) {
			try {
				newComponent = config.newLearningAlgorithm((Class<AbstractCELA>) clazz);
			} catch (LearningProblemUnsupportedException e) {
				// TODO status message
				e.printStackTrace();
			}
		}
		return newComponent;
	}

	// changes current component to an instance of the specified class
	@SuppressWarnings("unchecked")
	private AbstractComponent changeInstance(Class<? extends AbstractComponent> clazz) {
		AbstractComponent newComponent = null;
		if (AbstractKnowledgeSource.class.isAssignableFrom(clazz)) {
			newComponent = config.changeKnowledgeSource((Class<AbstractKnowledgeSource>) clazz);
		} else if (AbstractReasonerComponent.class.isAssignableFrom(clazz)) {
			newComponent = config.changeReasoner((Class<AbstractReasonerComponent>) clazz);
		} else if (AbstractLearningProblem.class.isAssignableFrom(clazz)) {
			newComponent = config.changeLearningProblem((Class<AbstractLearningProblem>) clazz);
		} else if (AbstractCELA.class.isAssignableFrom(clazz)) {
			try {
				newComponent = config.changeLearningAlgorithm((Class<AbstractCELA>) clazz);
			} catch (LearningProblemUnsupportedException e) {
				// TODO status message
				e.printStackTrace();
			}
		}
		return newComponent;
	}

	/**
	 * @return the currentComponent
	 */
	public AbstractComponent getCurrentComponent() {
		return currentComponent;
	}

}
