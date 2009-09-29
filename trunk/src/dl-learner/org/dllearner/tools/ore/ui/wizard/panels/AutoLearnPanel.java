package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.dllearner.tools.ore.ui.ClassesTable;
import org.dllearner.tools.ore.ui.ResultTable;

public class AutoLearnPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5204979906041331328L;
	
	private ClassesTable classesTable;
	
	private JPanel subPanel;
	private JPanel equivalentPanel;
	
	private ResultTable equivalentResultsTable;
	private ResultTable subResultsTable;

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
		
		subPanel = new JPanel();
		subResultsTable = new ResultTable();
		subPanel.add(subResultsTable);
		
		equivalentPanel = new JPanel();
		equivalentResultsTable = new ResultTable();
		equivalentPanel.add(equivalentResultsTable);
		
		equivSubSplitPane.setTopComponent(equivalentPanel);
		equivSubSplitPane.setBottomComponent(subPanel);
		
		resultPanel.add(equivSubSplitPane);
		return resultPanel;
	}
	
	public void fillClassesTable(Set<NamedClass> classes){
		classesTable.addClasses(classes);
	}
	
	public void fillSubClassExpressionsTable(List<EvaluatedDescriptionClass> resultList){
		subResultsTable.addResults(resultList);
	}
	
	public void fillEquivalentClassExpressionsTable(List<EvaluatedDescriptionClass> resultList){
		equivalentResultsTable.addResults(resultList);
	}
	
}
