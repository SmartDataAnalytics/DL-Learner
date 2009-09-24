package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.explanation.Explanation;
import org.dllearner.tools.ore.ui.ExplanationTable;
import org.dllearner.tools.ore.ui.ExplanationTablePanel;
import org.dllearner.tools.ore.ui.ImpactTable;
import org.dllearner.tools.ore.ui.RepairPlanPanel;
import org.dllearner.tools.ore.ui.UnsatisfiableClassesTable;
import org.semanticweb.owl.model.OWLClass;

public class UnsatisfiableExplanationPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UnsatisfiableClassesTable unsatClassesTable;
	private JScrollPane unsatClassesScrollPane;
	
	private JScrollPane explanationsScrollPane;
	private JComponent explanationsPanel;
	
	private JPanel buttonExplanationsPanel;
	
	private JPanel buttonPanel;
	private ButtonGroup explanationType;
	private JRadioButton regularButton;
	private JRadioButton laconicButton;
	private JRadioButton computeAllExplanationsRadioButton;
    private  JRadioButton computeMaxExplanationsRadioButton;
	private JSpinner maxExplanationsSelector;
	private JCheckBox strikeOutBox;
	
	private Set<ExplanationTablePanel> explanationPanels;
	private Set<ExplanationTable> explanationTables;

	
	private ExplanationManager expMan;
	
	public UnsatisfiableExplanationPanel(){
		expMan = ExplanationManager.getInstance(OREManager.getInstance());
		createUI();
	}
	
	private void createUI(){
		setLayout(new BorderLayout());
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setDividerLocation(200);
		mainSplitPane.setBorder(null);
		
		mainSplitPane.setLeftComponent(createUnsatClassesPanel());
		mainSplitPane.setRightComponent(createDebuggingPanel());
		
		add(mainSplitPane);		
		
		
//		String layoutDef = "(ROW unsat (COLUMN explanation (ROW repair impact)))";
//		MultiSplitLayout.Node modelRoot = MultiSplitLayout.parseModel(layoutDef);
//		JXMultiSplitPane mainSplitPane = new JXMultiSplitPane();
//		mainSplitPane.setModel(modelRoot);
//		mainSplitPane.add(createUnsatClassesPanel(), "unsat");
//		mainSplitPane.add(createExplanationPanel(), "explanation");
//		mainSplitPane.add(createRepairPanel(), "repair");
//		mainSplitPane.add(createImpactPanel(), "impact");
//		add(mainSplitPane);
	}
	
	private JComponent createUnsatClassesPanel(){
		JPanel unsatClassesHolder = new JPanel();
		unsatClassesHolder.setLayout(new BorderLayout());
		
		JLabel title = new JLabel("Unsatisfiable classes");
		title.setFont(getFont().deriveFont(Font.BOLD));
		unsatClassesHolder.add(title, BorderLayout.PAGE_START);
		
		unsatClassesTable = new UnsatisfiableClassesTable();
		unsatClassesScrollPane = new JScrollPane(unsatClassesTable);
		unsatClassesScrollPane.setPreferredSize(new Dimension(400, 400));	
		unsatClassesHolder.add(unsatClassesScrollPane, BorderLayout.CENTER);
		
		return unsatClassesHolder;
	}
	
	private JComponent createDebuggingPanel(){
		JSplitPane debuggingSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		debuggingSplitPane.setDividerLocation(500);
		debuggingSplitPane.setOneTouchExpandable(true);

		debuggingSplitPane.setTopComponent(createExplanationPanel());
		debuggingSplitPane.setBottomComponent(createImpactRepairPanel());
		
		return debuggingSplitPane;
	}
	
	private JComponent createExplanationPanel(){
		explanationsPanel = new Box(1);
		
		explanationPanels = new HashSet<ExplanationTablePanel>();
		explanationTables = new HashSet<ExplanationTable>();

		JPanel pan = new JPanel(new BorderLayout());
		pan.add(explanationsPanel, BorderLayout.NORTH);
		explanationsScrollPane = new JScrollPane(pan);
//		explanationsScrollPane.setPreferredSize(minimumSize);
		explanationsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		explanationsScrollPane.getViewport().setOpaque(false);
		explanationsScrollPane.getViewport().setBackground(null);
		explanationsScrollPane.setOpaque(false);
			
		regularButton = new JRadioButton("Show regular explanations", true);
		regularButton.setActionCommand("regular");
		regularButton.setSelected(true);
		laconicButton = new JRadioButton("Show precise explanations");
		laconicButton.setActionCommand("laconic");
			
		explanationType = new ButtonGroup();
		explanationType.add(regularButton);
		explanationType.add(laconicButton);
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		buttonPanel.add(regularButton, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 2, 30), 0, 0));
		buttonPanel.add(laconicButton, new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 30), 0, 0));
		
		JPanel buttonPanelHolder = new JPanel(new BorderLayout());
		buttonPanelHolder.add(buttonPanel, "West");
		
		maxExplanationsSelector = new JSpinner();
		maxExplanationsSelector.setEnabled(true);
	    javax.swing.SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 500, 1);
	    maxExplanationsSelector.setModel(spinnerModel);
	    maxExplanationsSelector.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
	    
	    computeAllExplanationsRadioButton = new JRadioButton("Compute all explanations");
	    computeAllExplanationsRadioButton.setActionCommand("all");
	            
	    computeMaxExplanationsRadioButton = new JRadioButton("Limit explanation count to:");
	    computeMaxExplanationsRadioButton.setActionCommand("MAX");
	    computeMaxExplanationsRadioButton.setSelected(true);
	    
	    ButtonGroup limitButtonGroup = new ButtonGroup();
	    limitButtonGroup.add(computeAllExplanationsRadioButton);
	    limitButtonGroup.add(computeMaxExplanationsRadioButton);
	       
	    buttonPanel.add(computeAllExplanationsRadioButton, new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 0), 0, 0));
	    buttonPanel.add(computeMaxExplanationsRadioButton, new GridBagConstraints(1, 1, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 0), 0, 0));
	    buttonPanel.add(maxExplanationsSelector, new GridBagConstraints(3, 1, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 0), 0, 0));
	  
	    strikeOutBox = new JCheckBox("Strike out irrelevant parts");
	    strikeOutBox.setActionCommand("strike");
	    buttonPanel.add(strikeOutBox, new GridBagConstraints(3, 0, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 0), 0, 0));
	       
		buttonExplanationsPanel = new JPanel();
		buttonExplanationsPanel.setLayout(new BorderLayout());
		buttonExplanationsPanel.add(explanationsScrollPane, BorderLayout.CENTER);
		buttonExplanationsPanel.add(buttonPanelHolder, BorderLayout.NORTH);
		
		return buttonExplanationsPanel;

	}
	
	private JComponent createImpactRepairPanel(){
		JPanel impactRepairPanel = new JPanel();
		impactRepairPanel.setLayout(new BorderLayout());
		impactRepairPanel.add(new JLabel("Repair plan"), BorderLayout.NORTH);
		
		JSplitPane impRepSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		impRepSplit.setOneTouchExpandable(true);
		impRepSplit.setDividerLocation(600);
		impRepSplit.setBorder(null);
		impactRepairPanel.add(impRepSplit);
		
		ImpactTable impactTable = new ImpactTable();
		JScrollPane impScr = new JScrollPane(impactTable);
		JPanel impactPanel = new JPanel();
		impactPanel.setLayout(new BorderLayout());
		impactPanel.add(new JLabel("Impact"), BorderLayout.NORTH);
		impactPanel.add(impScr);
		impRepSplit.setRightComponent(impactPanel);
		
		RepairPlanPanel repairPanel = new RepairPlanPanel(); 
		impRepSplit.setLeftComponent(repairPanel);
		
		return impactRepairPanel;
	}
	
	private JComponent createRepairPanel(){
		return new RepairPlanPanel();
	}
	
	private JComponent createImpactPanel(){
		ImpactTable impactTable = new ImpactTable();
		JScrollPane impScr = new JScrollPane(impactTable);
		JPanel impactPanel = new JPanel();
		impactPanel.setLayout(new BorderLayout());
		impactPanel.add(new JLabel("Impact"), BorderLayout.NORTH);
		impactPanel.add(impScr);
		return impactPanel;
	}
	
	public void fillUnsatClassesTable(List<OWLClass> unsatClasses) {
		unsatClassesTable.addUnsatClasses(unsatClasses);
		unsatClassesScrollPane.validate();
	}
	
	public void clearExplanationsPanel() {		
		explanationsPanel.removeAll();
		explanationsPanel.validate();
	}

	public void addExplanation(Explanation explanation, OWLClass unsat, int counter) {
		ExplanationTable expTable = new ExplanationTable(explanation, unsat);
		ExplanationTablePanel panel = new ExplanationTablePanel(expTable, counter);
		explanationsPanel.add(panel);
		explanationsPanel.add(Box.createVerticalStrut(10));
		explanationPanels.add(panel);
//		explanationsPanel.add(new JSeparator());
//		explanationsPanel.add(Box.createVerticalStrut(10));
//		this.updateUI();
	}
	
	public void addExplanations(Set<Explanation> explanations, OWLClass unsat){
		explanationTables.clear();
		Box explanationHolderPanel = new Box(1);
		
		explanationHolderPanel.setBorder(new TitledBorder(unsat + " is unsatisfiable"));
		int counter = 1;
		for(Explanation exp : explanations){
			ExplanationTable expTable = new ExplanationTable(exp, unsat);
			explanationTables.add(expTable);
			ExplanationTablePanel panel = new ExplanationTablePanel(expTable, counter);
			explanationHolderPanel.add(panel);
			explanationHolderPanel.add(Box.createVerticalStrut(5));
			counter++;
			if(counter > expMan.getMaxExplantionCount() && !expMan.isComputeAllExplanationsMode()){
				break;
			}
		}
		explanationsPanel.add(explanationHolderPanel);
	}
	
	public void validate(){
		explanationsScrollPane.validate();
	}
	
	public void setMaxExplanationsMode(boolean value){
		maxExplanationsSelector.setEnabled(value);	
	}
	
	public void strikeOutIrrelevantParts(boolean strikeOut){
		for(ExplanationTable table : explanationTables){
			table.strikeOut(strikeOut);
		}	
	}
	
	public void setStrikeEnabled(boolean enabled){
		strikeOutBox.setEnabled(enabled);
	}

	public void addActionListeners(ActionListener aL) {
		regularButton.addActionListener(aL);
		laconicButton.addActionListener(aL);
		computeAllExplanationsRadioButton.addActionListener(aL);
		computeMaxExplanationsRadioButton.addActionListener(aL);	
		strikeOutBox.addActionListener(aL);
	}
	
	public void addListSelectionListener(ListSelectionListener l){
		unsatClassesTable.getSelectionModel().addListSelectionListener(l);
	}
	
	public void addChangeListener(ChangeListener cL){
		maxExplanationsSelector.addChangeListener(cL);
	}
	
	public UnsatisfiableClassesTable getUnsatTable(){
		return unsatClassesTable;
	}
	
	public static void main(String[] args){
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JSplitPane pane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pane1.setDividerLocation(200);
		JSplitPane pane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		pane2.setDividerLocation(200);
		JSplitPane pane3 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		pane1.setLeftComponent(new JPanel());
		pane1.setRightComponent(pane2);
		pane2.setTopComponent(new JPanel());
		pane2.setBottomComponent(pane3);
		pane3.setLeftComponent(new JPanel());
		pane3.setRightComponent(new JPanel());
		panel.add(pane1);
		frame.add(panel);
		frame.setSize(400, 400);
		frame.setVisible(true);
		
		
	}
}
