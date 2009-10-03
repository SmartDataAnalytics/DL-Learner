package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeListener;

import org.dllearner.tools.ore.explanation.Explanation;
import org.dllearner.tools.ore.ui.ExplanationTable;
import org.dllearner.tools.ore.ui.ExplanationTablePanel;
import org.dllearner.tools.ore.ui.HelpablePanel;
import org.dllearner.tools.ore.ui.RepairPlanPanel;
import org.semanticweb.owl.apibinding.OWLManager;

public class InconsistencyExplanationPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9206626647697013786L;

	private JScrollPane explanationsScrollPane;
	private JComponent explanationsPanel;
	
	private JPanel buttonExplanationsPanel;
	
	
	private ButtonGroup explanationType;
	private JRadioButton regularButton;
	private JRadioButton laconicButton;
	private JRadioButton computeAllExplanationsRadioButton;
    private  JRadioButton computeMaxExplanationsRadioButton;
	private JSpinner maxExplanationsSelector;
	

	public InconsistencyExplanationPanel() {
		createUI();
	}
	
	private void createUI(){
		setLayout(new BorderLayout());
		add(createDebuggingPanel());		
	}
	
	private JComponent createDebuggingPanel(){
		JSplitPane debuggingSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		debuggingSplitPane.setDividerLocation(500);
		debuggingSplitPane.setOneTouchExpandable(true);

		debuggingSplitPane.setTopComponent(createExplanationPanel());
		debuggingSplitPane.setBottomComponent(createRepairPanel());
		
		return debuggingSplitPane;
	}
	
	private JComponent createExplanationPanel(){
		explanationsPanel = new Box(1);

		JPanel pan = new JPanel(new BorderLayout());
		pan.add(explanationsPanel, BorderLayout.NORTH);
		explanationsScrollPane = new JScrollPane(pan);

		explanationsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		explanationsScrollPane.getViewport().setOpaque(false);
		explanationsScrollPane.getViewport().setBackground(null);
		explanationsScrollPane.setOpaque(false);
	       
		buttonExplanationsPanel = new JPanel();
		buttonExplanationsPanel.setLayout(new BorderLayout());
		buttonExplanationsPanel.add(explanationsScrollPane, BorderLayout.CENTER);
		JPanel holder = new JPanel(new BorderLayout());
		holder.add(createExplanationHeaderPanel(), BorderLayout.WEST);
		buttonExplanationsPanel.add(holder, BorderLayout.NORTH);
		
		return buttonExplanationsPanel;

	}
	
	private JComponent createExplanationHeaderPanel(){
		JPanel headerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		
		JPanel explanationTypePanel = new JPanel(new GridLayout(0, 1));
		regularButton = new JRadioButton("Show regular explanations", true);
		regularButton.setActionCommand("regular");
		regularButton.setSelected(true);
		laconicButton = new JRadioButton("Show precise explanations");
		laconicButton.setActionCommand("laconic");
		explanationType = new ButtonGroup();
		explanationType.add(regularButton);
		explanationType.add(laconicButton);
		explanationTypePanel.add(regularButton);
		explanationTypePanel.add(laconicButton);
		HelpablePanel explanationTypeHelpPanel = new HelpablePanel(explanationTypePanel);
		explanationTypeHelpPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		
		JPanel explanationCountPanel = new JPanel(new GridBagLayout());
		
		maxExplanationsSelector = new JSpinner();
		maxExplanationsSelector.setEnabled(true);
	    javax.swing.SpinnerModel spinnerModel = new SpinnerNumberModel(1, 1, 500, 1);
	    maxExplanationsSelector.setModel(spinnerModel);
	    maxExplanationsSelector.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
	    
	    computeAllExplanationsRadioButton = new JRadioButton("Compute all explanations");
	    computeAllExplanationsRadioButton.setActionCommand("all");
	            
	    computeMaxExplanationsRadioButton = new JRadioButton("Limit explanation count to:");
	    computeMaxExplanationsRadioButton.setActionCommand("max");
	    computeMaxExplanationsRadioButton.setSelected(true);
	    
	    ButtonGroup limitButtonGroup = new ButtonGroup();
	    limitButtonGroup.add(computeAllExplanationsRadioButton);
	    limitButtonGroup.add(computeMaxExplanationsRadioButton);
	       
	    explanationCountPanel.add(computeAllExplanationsRadioButton, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 0), 0, 0));
	    explanationCountPanel.add(computeMaxExplanationsRadioButton, new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 0), 0, 0));
	    explanationCountPanel.add(maxExplanationsSelector, new GridBagConstraints(1, 1, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 0), 0, 0));
	    
	    HelpablePanel explanationCountHelpPanel = new HelpablePanel(explanationCountPanel);
	    explanationCountHelpPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	    
	    c.fill = GridBagConstraints.VERTICAL;
	    
	    headerPanel.add(explanationTypeHelpPanel, c);
	    c.anchor = GridBagConstraints.LINE_END;
	    headerPanel.add(explanationCountHelpPanel, c);
	    
	    return headerPanel;
	}
	
	private JComponent createRepairPanel(){
		JPanel repairPanel = new JPanel(new BorderLayout());
		repairPanel.add(new RepairPlanPanel(), BorderLayout.CENTER);
		return repairPanel;
	}
	
	public void clearExplanationsPanel() {		
		explanationsPanel.removeAll();
		explanationsPanel.validate();
	}
	
	public void setMaxExplanationsMode(boolean value){
		maxExplanationsSelector.setEnabled(value);	
	}
	
	public void addExplanation(Explanation explanation, int counter){
		ExplanationTable expTable = new ExplanationTable(explanation, OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLThing());
		explanationsPanel.add(new ExplanationTablePanel(expTable, counter));
		explanationsPanel.add(Box.createVerticalStrut(10));

	}
	
	public void addActionListeners(ActionListener aL){
		regularButton.addActionListener(aL);
		laconicButton.addActionListener(aL);
		computeAllExplanationsRadioButton.addActionListener(aL);
		computeMaxExplanationsRadioButton.addActionListener(aL);
	}
	
	public void addChangeListener(ChangeListener cL){
		maxExplanationsSelector.addChangeListener(cL);
	}
	
}
