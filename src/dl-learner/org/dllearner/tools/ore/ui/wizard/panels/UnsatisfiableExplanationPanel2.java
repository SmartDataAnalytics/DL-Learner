package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.explanation.Explanation;
import org.dllearner.tools.ore.ui.ExplanationTable;
import org.dllearner.tools.ore.ui.ExplanationTablePanel;
import org.dllearner.tools.ore.ui.HelpablePanel;
import org.dllearner.tools.ore.ui.ImpactTable;
import org.dllearner.tools.ore.ui.RepairPlanPanel;
import org.dllearner.tools.ore.ui.UnsatisfiableClassesTable;
import org.jdesktop.swingx.JXTitledPanel;
import org.semanticweb.owlapi.model.OWLClass;

public class UnsatisfiableExplanationPanel2 extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UnsatisfiableClassesTable unsatClassesTable;
	
	private JScrollPane explanationsScrollPane;
	private JComponent explanationsPanel;
	
	
	private static final String EXPLANATION_TYPE_TEXT = "<html>You can select whether the shown explanations shell " +
			"only contain axioms, which are asserted in the ontology," +
			"<br>or whether the explanations show only relevant parts of the original axioms.</html>" +
			"";
	private static final String EXPLANATION_COUNT_TEXT = "<html>You can choose between showing/computing all " +
			"explanations,<br> or only a limited number of them.</html>";
	
	
	private ButtonGroup explanationType;
	
	private JRadioButton regularButton;
	private JRadioButton laconicButton;
	private JRadioButton preciseButton;
	
	private JRadioButton computeAllExplanationsRadioButton;
    private  JRadioButton computeMaxExplanationsRadioButton;
	private JSpinner maxExplanationsSelector;
	private JCheckBox strikeOutBox;
	
	@SuppressWarnings("unused")
	private Set<ExplanationTablePanel> explanationPanels;
	private Set<ExplanationTable> explanationTables;

	
	private ExplanationManager expMan;
	
	public UnsatisfiableExplanationPanel2(){
		expMan = ExplanationManager.getInstance(OREManager.getInstance());
		createUI();
	}
	
	private void createUI(){
		setLayout(new BorderLayout());
		
//		String layoutDescription = 
//			"(ROW " 
//			+ "(LEAF name=classes weight=0.2)" 
//			+ "(COLUMN weight=0.8"
//			+ 	"(LEAF name=explanations weight=0.8)"
//			+ 	"(ROW weight=0.2" 
//			+		"(LEAF name=repair weight=0.5)" 
//			+		"(LEAF name=impact weight=0.5)"
//			+	")"
//			+  ")" 
//			+ ")"; 
//		JXMultiSplitPane splitPane = new JXMultiSplitPane(new MultiSplitLayout(MultiSplitLayout.parseModel(layoutDescription)));
		
		
		
		
//		splitPane.add(createUnsatClassesPanel(), "classes");
//		splitPane.add(createExplanationPanel(), "explanations");
//		splitPane.add(new RepairPlanPanel(), "repair");
//		splitPane.add(createImpactPanel(), "impact");
		
		
		
		
		
		JSplitPane impactRepairSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		impactRepairSplitPane.setDividerLocation(0.5);
		impactRepairSplitPane.setOneTouchExpandable(true);
		impactRepairSplitPane.setResizeWeight(0.5);
		impactRepairSplitPane.setLeftComponent(new RepairPlanPanel());
		impactRepairSplitPane.setRightComponent(createImpactPanel());
		
		JSplitPane debuggingSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		debuggingSplitPane.setDividerLocation(0.7);
		debuggingSplitPane.setOneTouchExpandable(true);
		debuggingSplitPane.setResizeWeight(1.0);
		debuggingSplitPane.setTopComponent(createExplanationPanel());
		debuggingSplitPane.setBottomComponent(impactRepairSplitPane);
		
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainSplitPane.setDividerLocation(0.2);
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setResizeWeight(0.2);
		mainSplitPane.setLeftComponent(createUnsatClassesPanel());
		mainSplitPane.setRightComponent(debuggingSplitPane);
		
		add(mainSplitPane);		
		
	}
	
	private JComponent createUnsatClassesPanel(){
		JXTitledPanel unsatClassesPanel = new JXTitledPanel();
		unsatClassesPanel.setTitle("Unsatisfiable classes");
		unsatClassesPanel.getContentContainer().setLayout(new BorderLayout());
		
		unsatClassesTable = new UnsatisfiableClassesTable();
		unsatClassesPanel.getContentContainer().add(new JScrollPane(unsatClassesTable), BorderLayout.CENTER);
		
		return unsatClassesPanel;
	}
	
	private JComponent createExplanationPanel(){
		JXTitledPanel explanationPanel = new JXTitledPanel();
		explanationPanel.setTitle("Explanations");
		explanationPanel.getContentContainer().setLayout(new BorderLayout());
		
		JPanel holder = new JPanel(new BorderLayout());
		holder.add(createExplanationHeaderPanel(), BorderLayout.WEST);
		explanationPanel.getContentContainer().add(holder, BorderLayout.NORTH);
		
		explanationsPanel = new Box(1);
		
		explanationPanels = new HashSet<ExplanationTablePanel>();
		explanationTables = new HashSet<ExplanationTable>();

		JPanel pan = new JPanel(new BorderLayout());
		pan.add(explanationsPanel, BorderLayout.NORTH);
		explanationsScrollPane = new JScrollPane(pan);

		explanationsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		explanationsScrollPane.getViewport().setOpaque(false);
		explanationsScrollPane.getViewport().setBackground(null);
		explanationsScrollPane.setOpaque(false);
	       
		explanationPanel.getContentContainer().add(explanationsScrollPane, BorderLayout.CENTER);
		
		
		return explanationPanel;

	}
	
	private JComponent createExplanationHeaderPanel(){
		JPanel headerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		
		JPanel explanationTypePanel = new JPanel(new GridBagLayout());
		
		regularButton = new JRadioButton("Show regular explanations", true);
		regularButton.setActionCommand("regular");
		regularButton.setSelected(true);
		laconicButton = new JRadioButton("Show laconic explanations");
		laconicButton.setActionCommand("laconic");
		preciseButton = new JRadioButton("Show precise explanations");
		preciseButton.setActionCommand("precise");
		explanationType = new ButtonGroup();
		explanationType.add(regularButton);
		explanationType.add(laconicButton);
		explanationType.add(preciseButton);
		c.gridx = 0;
		c.gridy = 0;
		explanationTypePanel.add(regularButton, c);
		c.gridx = 0;
		c.gridy = 1;
		explanationTypePanel.add(laconicButton, c);
		c.gridx = 0;
		c.gridy = 2;
		explanationTypePanel.add(preciseButton, c);
		HelpablePanel explanationTypeHelpPanel = new HelpablePanel(explanationTypePanel);
		explanationTypeHelpPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		explanationTypeHelpPanel.setHelpText(EXPLANATION_TYPE_TEXT);
		
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
	    explanationCountHelpPanel.setHelpText(EXPLANATION_COUNT_TEXT);
	  
	    strikeOutBox = new JCheckBox("Strike out irrelevant parts");
	    strikeOutBox.setActionCommand("strike");
//	    buttonPanel.add(strikeOutBox, new GridBagConstraints(3, 0, 1, 1, 0.0D, 0.0D, 12, 2, new Insets(0, 0, 0, 0), 0, 0));
	    
	    c.fill = GridBagConstraints.VERTICAL;
	    c.gridx = 0;
	    c.gridy = 0;
	    headerPanel.add(explanationTypeHelpPanel, c);
	    c.anchor = GridBagConstraints.LINE_END;
	    c.gridx = 1;
	    c.gridy = 0;
	    headerPanel.add(explanationCountHelpPanel, c);
	    
	    return headerPanel;
	}
	
	public JComponent createImpactPanel(){
		JXTitledPanel impactPanel = new JXTitledPanel("Impact");
		impactPanel.getContentContainer().setLayout(new BorderLayout());
		
		ImpactTable impactTable = new ImpactTable();
		impactPanel.getContentContainer().add(new JScrollPane(impactTable));
		
		return impactPanel;
	}
	
	public void fillUnsatClassesTable(List<OWLClass> unsatClasses) {
		unsatClassesTable.addUnsatClasses(unsatClasses);
	}
	
	public void clearExplanationsPanel() {		
		explanationsPanel.removeAll();
		explanationsPanel.validate();
	}
	
	public void addExplanations(Set<Explanation> explanations, OWLClass unsat){
		explanationTables.clear();
		Box explanationHolderPanel = new Box(1);
		
		explanationHolderPanel.setBorder(new TitledBorder(OREManager.getInstance().getManchesterSyntaxRendering(unsat) + " is unsatisfiable"));
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
		preciseButton.addActionListener(aL);
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
}
