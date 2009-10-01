package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.ui.ClassesTable;
import org.dllearner.tools.ore.ui.EquivalentClassExpressionsTable;
import org.dllearner.tools.ore.ui.GraphicalCoveragePanel;

public class AutoLearnPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5204979906041331328L;
	
	private ClassesTable classesTable;
	
	private JPanel superPanel;
	private JPanel equivalentPanel;
	
	private EquivalentClassExpressionsTable equivalentClassResultsTable;
	private EquivalentClassExpressionsTable superClassResultsTable;
	
	private GraphicalCoveragePanel equivalentClassCoveragePanel;
	private GraphicalCoveragePanel superClassCoveragePanel;
	
	private JLabel equivalentInconsistencyLabel;
	private JLabel superInconsistencyLabel;
	
	private JButton skipButton;
	
	private final static String INCONSISTENY_WARNING = "<html><font color=red>" +
														"Warning. Selected class expressions leads to an inconsistent ontology!" +
														"</font></html>";
	public AutoLearnPanel(){
		createUI();
	}
	
	private void createUI(){
		setLayout(new BorderLayout());
		
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setDividerLocation(0.2);
		
		mainSplitPane.setLeftComponent(createClassesPanel());
		mainSplitPane.setRightComponent(createResultPanel());
		
		add(mainSplitPane);
	}
	
	private JComponent createClassesPanel(){
		classesTable = new ClassesTable();
		classesTable.setBorder(null);
		classesTable.setEnabled(false);
		JScrollPane classesScroll = new JScrollPane(classesTable);
		return classesScroll;
	}
	
	private JComponent createResultPanel(){
		JPanel resultPanel = new JPanel(new BorderLayout());
		JSplitPane equivSubSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		equivSubSplitPane.setOneTouchExpandable(true);
		equivSubSplitPane.setDividerLocation(0.5);
		
		equivSubSplitPane.setTopComponent(createEquivalentPanel());
		equivSubSplitPane.setBottomComponent(createSuperPanel());
		
		addTableSelectionListeners();
		
		skipButton = new JButton("Skip");
		skipButton.setActionCommand("skip");
		
		resultPanel.add(equivSubSplitPane, BorderLayout.CENTER);
		resultPanel.add(skipButton, BorderLayout.SOUTH);
		
		return resultPanel;
	}
	
	private JComponent createEquivalentPanel(){
		GridBagConstraints c = new GridBagConstraints();
		equivalentPanel = new JPanel();
		equivalentPanel.setLayout(new GridBagLayout());
		
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		equivalentClassResultsTable = new EquivalentClassExpressionsTable();
		equivalentClassResultsTable.setName("equivalent");
		equivalentPanel.add(new JScrollPane(equivalentClassResultsTable), c);
		
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridx = 1;
		c.gridy = 0;
		equivalentClassCoveragePanel = new GraphicalCoveragePanel("");
		equivalentPanel.add(equivalentClassCoveragePanel, c);
		equivalentPanel.setBorder(BorderFactory.createTitledBorder("Equivalent class expressions"));
		
		c.gridx = 0;
		c.gridy = 1;
		equivalentInconsistencyLabel = new JLabel(" ");
		equivalentPanel.add(equivalentInconsistencyLabel, c);
		
		return equivalentPanel;
	}
	
	private JComponent createSuperPanel(){
		GridBagConstraints c = new GridBagConstraints();
		superPanel = new JPanel();
		superPanel.setLayout(new GridBagLayout());
		
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		superClassResultsTable = new EquivalentClassExpressionsTable();
		superClassResultsTable.setName("super");
		superPanel.add(new JScrollPane(superClassResultsTable), c);
		
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridx = 1;
		c.gridy = 0;
		superClassCoveragePanel = new GraphicalCoveragePanel("");
		superPanel.add(superClassCoveragePanel, c);
		superPanel.setBorder(BorderFactory.createTitledBorder("Superclass expressions"));
		
		c.gridx = 0;
		c.gridy = 1;
		superInconsistencyLabel = new JLabel(" ");
		superPanel.add(superInconsistencyLabel, c);
		
		return superPanel;
	}
	
	public void fillClassesTable(Set<NamedClass> classes){
		classesTable.addClasses(classes);
	}
	
	public void fillSuperClassExpressionsTable(List<EvaluatedDescriptionClass> resultList){
		superClassResultsTable.addResults(resultList);
	}
	
	public void fillEquivalentClassExpressionsTable(List<EvaluatedDescriptionClass> resultList){
		equivalentClassResultsTable.addResults(resultList);
	}
	
	public void addActionListener(ActionListener aL){
		skipButton.addActionListener(aL);
	}
	
	public void resetPanel(){
		equivalentClassResultsTable.clear();
		superClassResultsTable.clear();
		equivalentClassCoveragePanel.clear();
		superClassCoveragePanel.clear();
	}
	
	private void addTableSelectionListeners(){
		equivalentClassResultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {		
				@Override
				public void valueChanged(ListSelectionEvent e) {
					
					if (!e.getValueIsAdjusting() && equivalentClassResultsTable.getSelectedRow() >= 0){
						
						EvaluatedDescriptionClass selectedClassExpression = equivalentClassResultsTable.getSelectedValue();
						OREManager.getInstance().setNewClassDescription(selectedClassExpression);
						equivalentClassCoveragePanel.setNewClassDescription(selectedClassExpression);
						if(!selectedClassExpression.isConsistent()){
							equivalentInconsistencyLabel.setText(INCONSISTENY_WARNING);
						} else {
							equivalentInconsistencyLabel.setText(" ");
						}
					}				
				}			
			
		});
		
		superClassResultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				if (!e.getValueIsAdjusting() && superClassResultsTable.getSelectedRow() >= 0){
					
					EvaluatedDescriptionClass selectedClassExpression = superClassResultsTable.getSelectedValue();
					OREManager.getInstance().setNewClassDescription(selectedClassExpression);
					superClassCoveragePanel.setNewClassDescription(selectedClassExpression);
					if(!selectedClassExpression.isConsistent()){
						superInconsistencyLabel.setText(INCONSISTENY_WARNING);
					} else {
						superInconsistencyLabel.setText(" ");
					}
				}				
			}	
		});
	}
	
	public void updateEquivalentGraphicalCoveragePanel(EvaluatedDescriptionClass desc){
		equivalentClassCoveragePanel.setNewClassDescription(desc);
	}
	
	public void updateSuperGraphicalCoveragePanel(EvaluatedDescriptionClass desc){
		superClassCoveragePanel.setNewClassDescription(desc);
	}
	
	public static void main(String[] args){
		JFrame frame = new JFrame();
		
		
		frame.add(new AutoLearnPanel());
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
}
