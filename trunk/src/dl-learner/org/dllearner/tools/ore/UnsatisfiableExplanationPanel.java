package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXList;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;

public class UnsatisfiableExplanationPanel extends JPanel{

	private JXList unsatList;
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

	private UnsatClassesListCellRenderer listRenderer;

	private ExplanationManager expMan;
	private ImpactManager impMan;

	
	
	private OWLClass unsatClass;
	
	public UnsatisfiableExplanationPanel(ExplanationManager expMan, ImpactManager impMan){
		this.expMan = expMan;
		this.impMan = impMan;

		
		setLayout(new BorderLayout());

		Dimension minimumSize = new Dimension(400, 400);

		listRenderer = new UnsatClassesListCellRenderer(expMan);
		unsatList = new JXList();
		
		
		unsatList.setCellRenderer(listRenderer);
		listScrollPane = new JScrollPane(unsatList);
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

		regularButton = new JRadioButton("regular", true);
		regularButton.setActionCommand("regular");
		
		laconicButton = new JRadioButton("laconic");
		laconicButton.setActionCommand("laconic");
		
		explanationType = new ButtonGroup();
		explanationType.add(regularButton);
		explanationType.add(laconicButton);
		buttonPanel = new JPanel();
		buttonPanel.add(regularButton);
		buttonPanel.add(laconicButton);

		buttonExplanationsPanel = new JPanel();
		buttonExplanationsPanel.setLayout(new BorderLayout());
		buttonExplanationsPanel
				.add(explanationsScrollPane, BorderLayout.CENTER);
		buttonExplanationsPanel.add(buttonPanel, BorderLayout.NORTH);

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
		
		RepairPlanPanel repairPanel = new RepairPlanPanel(impMan); 
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
	
	public void fillUnsatClassesList(List<OWLClass> unsatClasses) {
		DefaultListModel model = new DefaultListModel();
		for(OWLClass cl : unsatClasses){
			model.addElement(cl);
		}
		
		unsatList.setModel(model);
	}
	
	public void clearExplanationsPanel() {
		
		explanationsPanel.removeAll();
	}

	public void addExplanation(List<OWLAxiom> explanation, OWLClass unsat, int counter) {
		ExplanationTable expTable = new ExplanationTable(explanation, impMan,
				expMan, unsat);
		explanationsPanel.add(new ExplanationTablePanel(expTable, counter));

		explanationsPanel.add(Box.createVerticalStrut(10));
		explanationsPanel.add(new JSeparator());
		explanationsPanel.add(Box.createVerticalStrut(10));
		this.updateUI();
	}

	public void addActionListeners(ActionListener aL) {
		regularButton.addActionListener(aL);
		laconicButton.addActionListener(aL);
	}
	
	public void addListSelectionListener(ListSelectionListener l){
		unsatList.addListSelectionListener(l);
	}

	
}
