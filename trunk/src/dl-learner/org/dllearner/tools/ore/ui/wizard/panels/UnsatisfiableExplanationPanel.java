package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.ui.ExplanationTable;
import org.dllearner.tools.ore.ui.ImpactTable;
import org.dllearner.tools.ore.ui.UnsatClassesTableCellRenderer;
import org.dllearner.tools.ore.ui.UnsatisfiableClassesTable;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;

public class UnsatisfiableExplanationPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UnsatisfiableClassesTable unsatClassesTable;
	private JSplitPane splitPane;
	private JSplitPane statsSplitPane;
	private JScrollPane listScrollPane;
	private JScrollPane explanationsScrollPane;
	private JComponent explanationsPanel;
	private JPanel buttonExplanationsPanel;
	private JPanel buttonPanel;
	
	private ButtonGroup explanationType;
	private JRadioButton regularButton;
	private JRadioButton laconicButton;
	private JRadioButton mostRelevantButton;
	private JRadioButton computeAllExplanationsRadioButton;
    private  JRadioButton computeMaxExplanationsRadioButton;
	private JSpinner maxExplanationsSelector;


	private ExplanationManager expMan;
	private ImpactManager impMan;
	private RepairManager repMan;
	
	
	private OWLClass unsatClass;
	
	public UnsatisfiableExplanationPanel(ExplanationManager expMan, ImpactManager impMan, RepairManager repMan){
		this.expMan = expMan;
		this.impMan = impMan;
		this.repMan = repMan;
		
		setLayout(new BorderLayout());

		Dimension minimumSize = new Dimension(400, 400);

		unsatClassesTable = new UnsatisfiableClassesTable();
		unsatClassesTable.getColumn(0).setCellRenderer(new UnsatClassesTableCellRenderer(expMan));
		

		listScrollPane = new JScrollPane(unsatClassesTable);
		listScrollPane.setPreferredSize(minimumSize);

		explanationsPanel = new Box(1);

		JPanel pan = new JPanel(new BorderLayout());
		pan.add(explanationsPanel, BorderLayout.NORTH);
		explanationsScrollPane = new JScrollPane(pan);
		explanationsScrollPane.setPreferredSize(minimumSize);
		explanationsScrollPane.setBorder(BorderFactory
				.createLineBorder(Color.LIGHT_GRAY));
		explanationsScrollPane.getViewport().setOpaque(false);
		explanationsScrollPane.getViewport().setBackground(null);
		explanationsScrollPane.setOpaque(false);
		
		
		
		
		GridBagLayout layout = new GridBagLayout();
		buttonPanel = new JPanel();
		buttonPanel.setLayout(layout);
		
		regularButton = new JRadioButton("show regular explanations", true);
		regularButton.setActionCommand("regular");
		regularButton.setSelected(true);
		laconicButton = new JRadioButton("show precise explanations");
		laconicButton.setActionCommand("laconic");
		
		mostRelevantButton = new JRadioButton("show most relevant explanations");
		mostRelevantButton.setActionCommand("relevant");
		
		explanationType = new ButtonGroup();
		explanationType.add(regularButton);
		explanationType.add(laconicButton);
		
		buttonPanel.add(regularButton, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 2, 30), 0, 0));
		buttonPanel.add(laconicButton, new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 30), 0, 0));
		
		JPanel buttonPanelHolder = new JPanel(new BorderLayout());
		buttonPanelHolder.add(buttonPanel, "West");
		
		maxExplanationsSelector = new JSpinner();
		maxExplanationsSelector.setEnabled(true);
	    javax.swing.SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 500, 1);
	    maxExplanationsSelector.setModel(spinnerModel);
	    
	    computeAllExplanationsRadioButton = new JRadioButton("compute all explanations");
	    computeAllExplanationsRadioButton.setActionCommand("all");
	    
	        
	    computeMaxExplanationsRadioButton = new JRadioButton("limit explanation count to:");
	    computeMaxExplanationsRadioButton.setActionCommand("max");
	    computeMaxExplanationsRadioButton.setSelected(true);
	    
	    ButtonGroup limitButtonGroup = new ButtonGroup();
	    limitButtonGroup.add(computeAllExplanationsRadioButton);
	    limitButtonGroup.add(computeMaxExplanationsRadioButton);
	       
	        buttonPanel.add(computeAllExplanationsRadioButton, new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 0), 0, 0));
	        buttonPanel.add(computeMaxExplanationsRadioButton, new GridBagConstraints(1, 1, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 0), 0, 0));
	        
	       
	        buttonPanel.add(maxExplanationsSelector, new GridBagConstraints(3, 1, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 0), 0, 0));
	        maxExplanationsSelector.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
	       
	        

		buttonExplanationsPanel = new JPanel();
		buttonExplanationsPanel.setLayout(new BorderLayout());
		buttonExplanationsPanel
				.add(explanationsScrollPane, BorderLayout.CENTER);
		buttonExplanationsPanel.add(buttonPanelHolder, BorderLayout.NORTH);

		statsSplitPane = new JSplitPane(0);
		statsSplitPane.setResizeWeight(1.0D);
		statsSplitPane.setTopComponent(buttonExplanationsPanel);
		
		//repair panel
		JPanel impactRepairPanel = new JPanel();
		impactRepairPanel.setLayout(new BorderLayout());
		impactRepairPanel.add(new JLabel("Repair plan"), BorderLayout.NORTH);
		JSplitPane impRepSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		impRepSplit.setOneTouchExpandable(true);
		impRepSplit.setDividerLocation(600);
		impRepSplit.setBorder(null);
		impactRepairPanel.add(impRepSplit);
		
		JPanel impactPanel = new JPanel();
		impactPanel.setLayout(new BorderLayout());
		impactPanel.add(new JLabel("Lost entailments"), BorderLayout.NORTH);
		JScrollPane impScr = new JScrollPane(new ImpactTable(impMan));
		impactPanel.add(impScr);
		impRepSplit.setRightComponent(impactPanel);
		
		RepairPlanPanel repairPanel = new RepairPlanPanel(repMan); 
		impRepSplit.setLeftComponent(repairPanel);
		
		
		statsSplitPane.setBottomComponent(impactRepairPanel);
		
		statsSplitPane.setBorder(null);
		statsSplitPane.setDividerLocation(500);
		statsSplitPane.setOneTouchExpandable(true);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane,
				statsSplitPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);
		splitPane.setBorder(null);

		add(splitPane);
	}
	
	public void fillUnsatClassesTable(List<OWLClass> unsatClasses) {
		unsatClassesTable.addUnsatClasses(unsatClasses);
	}
	
	public void clearExplanationsPanel() {		
		explanationsPanel.removeAll();
	}

	public void addExplanation(List<OWLAxiom> explanation, OWLClass unsat, int counter) {
		ExplanationTable expTable = new ExplanationTable(explanation, repMan, impMan,
				expMan, unsat);
		explanationsPanel.add(new ExplanationTablePanel(expTable, counter));

		explanationsPanel.add(Box.createVerticalStrut(10));
		explanationsPanel.add(new JSeparator());
		explanationsPanel.add(Box.createVerticalStrut(10));
		this.updateUI();
	}
	
	public void setMaxExplanationsMode(boolean value){
		maxExplanationsSelector.setEnabled(value);	
	}

	public void addActionListeners(ActionListener aL) {
		regularButton.addActionListener(aL);
		laconicButton.addActionListener(aL);
		computeAllExplanationsRadioButton.addActionListener(aL);
		computeMaxExplanationsRadioButton.addActionListener(aL);	
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
}
