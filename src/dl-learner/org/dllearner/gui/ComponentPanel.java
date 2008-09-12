package org.dllearner.gui;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.dllearner.core.Component;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;

/**
 * Class displaying a component (and its options).
 * 
 * @author Jens Lehmann
 *
 */
public class ComponentPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -7678275020058043937L;

	private Config config;
//	private StartGUI startGUI;
	private List<Class<? extends Component>> selectableComponents;
	private OptionPanel optionPanel;
//	private Class<? extends Component> panelClass;
	private Component currentComponent;
	
	// GUI elements
	private JButton clearButton;	
	private JComboBox comboBox = new JComboBox();	
	
	public ComponentPanel(LayoutManager layout) {
		super(layout);
	}
	
	ComponentPanel(final Config config, StartGUI startGUI, Class<? extends Component> panelClass, Class<? extends Component> defaultComponent) {
		this(config, startGUI, panelClass, defaultComponent,null);
	}
		
	ComponentPanel(final Config config, StartGUI startGUI, Class<? extends Component> panelClass, Class<? extends Component> defaultComponent, List<Class<? extends Component>> ignoredComponents) {
		super(new BorderLayout());

		this.config = config;
//		this.startGUI = startGUI;
//		this.panelClass = panelClass;
		
		// get all classes of the correct type
		selectableComponents = new LinkedList<Class<? extends Component>>();
		if(panelClass == KnowledgeSource.class) {
			selectableComponents.addAll(config.getComponentManager().getKnowledgeSources());
		} else if(panelClass == ReasonerComponent.class) {
			selectableComponents.addAll(config.getComponentManager().getReasonerComponents());
		} else if(panelClass == LearningProblem.class) {
			selectableComponents.addAll(config.getComponentManager().getLearningProblems());
		} else if(panelClass == LearningAlgorithm.class) {
			selectableComponents.addAll(config.getComponentManager().getLearningAlgorithms());
		}
		
		// set default component class (move it to first position)
		selectableComponents.remove(defaultComponent);
		selectableComponents.add(0, defaultComponent);
		
		// remove ignored component classes
		if(ignoredComponents != null) {
			selectableComponents.removeAll(ignoredComponents);
		}
		
		// create combo box
		for (int i = 0; i < selectableComponents.size(); i++) {
			comboBox.addItem(config.getComponentManager().getComponentName(selectableComponents.get(i)));
		}
		comboBox.addActionListener(this);
		
		clearButton = new JButton("Reset to Default Values");
		clearButton.addActionListener(this);

		// create upper panel
		JPanel choosePanel = new JPanel();
		choosePanel.add(comboBox);
//		choosePanel.add(new JLabel("       "));
		choosePanel.add(clearButton);

		// we create a new default component
		currentComponent = newInstance(defaultComponent);
		optionPanel = new OptionPanel(config, currentComponent);

		add(choosePanel, BorderLayout.NORTH);
		add(optionPanel, BorderLayout.CENTER);

	} 
	

	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == comboBox) {
			// change component and update option panel
			Class<? extends Component> c = selectableComponents.get(comboBox.getSelectedIndex());
			currentComponent = changeInstance(c);
			updateOptionPanel();
		} else if (e.getSource() == clearButton) {
			// clearing everything corresponds to changing to an unconfigured
			// component of the same type
			currentComponent = changeInstance(currentComponent.getClass());
			updateOptionPanel();
		}
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
	}	
	
	// creates an instance of the specified component class
	@SuppressWarnings("unchecked")
	private Component newInstance(Class<? extends Component> clazz) {
		Component newComponent = null;
		if(KnowledgeSource.class.isAssignableFrom(clazz)) {
			newComponent = config.newKnowledgeSource((Class<KnowledgeSource>) clazz);
		} else if(ReasonerComponent.class.isAssignableFrom(clazz)) {
			newComponent = config.newReasoner((Class<ReasonerComponent>) clazz);
		} else if(LearningProblem.class.isAssignableFrom(clazz)) {
			newComponent = config.newLearningProblem((Class<LearningProblem>) clazz);
		} else if(LearningAlgorithm.class.isAssignableFrom(clazz)) {
			try {
				newComponent = config.newLearningAlgorithm((Class<LearningAlgorithm>) clazz);
			} catch (LearningProblemUnsupportedException e) {
				// TODO status message
				e.printStackTrace();
			}
		}		
		return newComponent;
	}
	
	// changes current component to an instance of the specified class
	@SuppressWarnings("unchecked")
	private Component changeInstance(Class<? extends Component> clazz) {
		Component newComponent = null;
		if(KnowledgeSource.class.isAssignableFrom(clazz)) {
			newComponent = config.changeKnowledgeSource((Class<KnowledgeSource>) clazz);
		} else if(ReasonerComponent.class.isAssignableFrom(clazz)) {
			newComponent = config.changeReasoner((Class<ReasonerComponent>) clazz);
		} else if(LearningProblem.class.isAssignableFrom(clazz)) {
			newComponent = config.changeLearningProblem((Class<LearningProblem>) clazz);
		} else if(LearningAlgorithm.class.isAssignableFrom(clazz)) {
			try {
				newComponent = config.changeLearningAlgorithm((Class<LearningAlgorithm>) clazz);
			} catch (LearningProblemUnsupportedException e) {
				// TODO status message
				e.printStackTrace();
			}
		}		
		return newComponent;
	}	
	
}
