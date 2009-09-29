package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
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
		
		equivalentPanel = new JPanel();
		equivalentClassResultsTable = new EquivalentClassExpressionsTable();
		equivalentClassResultsTable.setName("equivalent");
		equivalentPanel.add(new JScrollPane(equivalentClassResultsTable));
		equivalentClassCoveragePanel = new GraphicalCoveragePanel("");
		equivalentPanel.add(equivalentClassCoveragePanel);
		equivalentPanel.setBorder(BorderFactory.createTitledBorder("Equivalent class expressions"));
		
		superPanel = new JPanel();
		superClassResultsTable = new EquivalentClassExpressionsTable();
		superClassResultsTable.setName("super");
		superPanel.add(new JScrollPane(superClassResultsTable));
		superClassCoveragePanel = new GraphicalCoveragePanel("");
		superPanel.add(superClassCoveragePanel);
		superPanel.setBorder(BorderFactory.createTitledBorder("Superclass expressions"));
		
		addTableSelectionListeners();
		
		equivSubSplitPane.setTopComponent(equivalentPanel);
		equivSubSplitPane.setBottomComponent(superPanel);
		
		resultPanel.add(equivSubSplitPane);
		return resultPanel;
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
	
	private void addTableSelectionListeners(){
		equivalentClassResultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {		
				@Override
				public void valueChanged(ListSelectionEvent e) {
					
					if (!e.getValueIsAdjusting() && equivalentClassResultsTable.getSelectedRow() >= 0){
						
						EvaluatedDescriptionClass selectedClassExpression = equivalentClassResultsTable.getSelectedValue();
						OREManager.getInstance().setNewClassDescription(selectedClassExpression);
						equivalentClassCoveragePanel.setNewClassDescription(selectedClassExpression);					
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
	
}
