package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTable;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;

public class ExplanationPanel extends JPanel implements ListSelectionListener, ActionListener{

	private JXList unsatList;
	private JSplitPane splitPane;
	private JScrollPane listScrollPane;
	private JScrollPane explanationsScrollPane;
	private JComponent explanationsPanel;
	private JPanel buttonExplanationsPanel;
	private JPanel buttonPanel;
	private ButtonGroup explanationType;
	private JRadioButton regularButton;
	private JRadioButton laconicButton;
	private OWLSyntaxTableCellRenderer tableRenderer;
	private UnsatClassesListCellRenderer listRenderer;
	
	
	private ExplanationManager manager;
	private OWLClass unsatClass;
	/**
	 * 
	 */
	private static final long serialVersionUID = 2213073383532597460L;

	public ExplanationPanel(ExplanationManager manager){
		
		this.manager = manager;
		
		setLayout(new BorderLayout());
		
		Dimension minimumSize = new Dimension(400, 400);
		
		tableRenderer = new OWLSyntaxTableCellRenderer();
		listRenderer = new UnsatClassesListCellRenderer(manager);
		
		
		

		DefaultListModel model = new DefaultListModel();
		for(OWLClass root : manager.getRootUnsatisfiableClasses()){
			model.addElement(root);
		}
		for(OWLClass unsat : manager.getUnsatisfiableClasses()){
			if(!model.contains(unsat)){
				model.addElement(unsat);
			}
		}
		
		unsatList = new JXList(model);
		
		unsatList.addListSelectionListener(this);
		unsatList.setCellRenderer(listRenderer);
		listScrollPane = new JScrollPane(unsatList);
		listScrollPane.setPreferredSize(minimumSize);
				
		explanationsPanel = new Box(1);
		
		
		JPanel pan = new JPanel(new BorderLayout());
		pan.add(explanationsPanel, BorderLayout.NORTH);
		explanationsScrollPane = new JScrollPane(pan);
		explanationsScrollPane.setPreferredSize(minimumSize);
		explanationsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		explanationsScrollPane.getViewport().setOpaque(false);
        explanationsScrollPane.getViewport().setBackground(null);
        explanationsScrollPane.setOpaque(false);
		
		
		regularButton = new JRadioButton("regular", true);
		regularButton.setActionCommand("regular");
		regularButton.addActionListener(this);
		laconicButton = new JRadioButton("laconic");
		laconicButton.setActionCommand("laconic");
		laconicButton.addActionListener(this);
		explanationType = new ButtonGroup();
		explanationType.add(regularButton);
		explanationType.add(laconicButton);
		buttonPanel = new JPanel();
		buttonPanel.add(regularButton);
		buttonPanel.add(laconicButton);
		
		buttonExplanationsPanel = new JPanel();
		buttonExplanationsPanel.setLayout(new BorderLayout());
		buttonExplanationsPanel.add(explanationsScrollPane, BorderLayout.CENTER);
		buttonExplanationsPanel.add(buttonPanel, BorderLayout.NORTH);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, buttonExplanationsPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);
		splitPane.setBorder(null);
		
		
		add(splitPane);
	}
	
	
	private void addExplanationTable(List<OWLAxiom> explanation, int number){
		
		
		JXTable expTable = new JXTable();
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("axiom", explanation.toArray());
		expTable.setModel(model);
		expTable.setDefaultRenderer(Object.class, tableRenderer);
		
		
		
//		DLSyntaxObjectRenderer r = new DLSyntaxObjectRenderer();
//		Vector<String> t = new Vector<String>();
//		for(OWLAxiom ax : explanation)
//			t.add(r.render(ax));
//		model.addColumn("axiom", t);

//		expTable.setModel(new ExplanationTableModel());
//		expTable.setDefaultRenderer(JButton.class, new ExplanationTableCellRenderer(expTable.getDefaultRenderer(JButton.class)));
//		expTable.addMouseListener(new JTableButtonMouseListener(expTable));

		expTable.getColumn(0).sizeWidthToFit();
		expTable.setSize(300, 300);
		expTable.setEditable(false);
		
		
		

		
//		JPanel tablePanel = new JPanel();tablePanel.setLayout(new GridLayout(0,1));
//		
//		tablePanel.add(expTable);
//		tablePanel.setPreferredSize(new Dimension(300, 300));
//		tablePanel.setBorder(BorderFactory.createTitledBorder("explanation " + number));
		
		explanationsPanel.add(new ExplanationTablePanel(expTable, number));
		
	}
	
	private void clearExplanationsPanel(){
		explanationsPanel.removeAll();
	}
	
	private void showLaconicExplanations(){
		clearExplanationsPanel();
		int counter = 1;
		for(List<OWLAxiom> explanation : manager.getOrderedLaconicUnsatisfiableExplanations(unsatClass)){
			addExplanationTable(explanation, counter);
			counter++;
		}
		explanationsPanel.add(Box.createVerticalStrut(10));
		explanationsPanel.add(new JSeparator());
		explanationsPanel.add(Box.createVerticalStrut(10));
		this.updateUI();
	}
	
	private void showRegularExplanations(){
		clearExplanationsPanel();
		int counter = 1;
		for(List<OWLAxiom> explanation : manager.getOrderedUnsatisfiableExplanations(unsatClass)){
			addExplanationTable(explanation, counter);
			counter++;
		}
		explanationsPanel.add(Box.createVerticalStrut(10));
		explanationsPanel.add(new JSeparator());
		explanationsPanel.add(Box.createVerticalStrut(10));
		this.updateUI();
	}
	

	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		unsatClass = (OWLClass)((JXList)e.getSource()).getSelectedValue();
		
		if(regularButton.isSelected()){
			showRegularExplanations();
		} else {
			showLaconicExplanations();
		}
		
	}
	
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("regular")){
			showRegularExplanations();
		} else if(e.getActionCommand().equals("laconic") && !unsatList.isSelectionEmpty()){
			showLaconicExplanations();
			
		}
		
	}
	
	public static void main(String[] args){
		
		String file = "file:examples/ore/miniEconomy.owl";
		
		ExplanationManager manager = ExplanationManager.getExplanationManager(file);
		ExplanationPanel panel = new ExplanationPanel(manager);
		
		
		JFrame test = new JFrame();
		test.setLayout(new GridLayout(0, 1));
		test.setSize(new Dimension(800, 500));
		test.add(panel);
		test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		test.setVisible(true);
		
	
}
}
