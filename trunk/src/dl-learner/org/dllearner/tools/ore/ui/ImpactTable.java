package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.TaskManager;
import org.dllearner.tools.ore.explanation.Explanation;
import org.jdesktop.swingx.JXTable;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class ImpactTable extends JXTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4146762679753151490L;

	public ImpactTable(){
		
		setModel(new ImpactTableModel());
	  
		setBackground(Color.WHITE);
	    setShowHorizontalLines(true);
	    setGridColor(Color.LIGHT_GRAY);
	    setTableHeader(null);
	    setRowHeightEnabled(true);
	    getColumnModel().getColumn(1).setCellRenderer(new MultiLineTableCellRenderer());
	    setRowHeight(getRowHeight() + 5);
	    getColumn(0).setMaxWidth(60);
	    getColumn(2).setMaxWidth(60);
	    
	
	
	addMouseMotionListener(new MouseAdapter() {

		final ImpactTable table;
		{
			table = ImpactTable.this;
		}

		public void mouseMoved(MouseEvent e) {
			int row = rowAtPoint(e.getPoint());
			int column = columnAtPoint(e.getPoint());
			if(column == 2 && row <= table.getRowCount() && row >= 0 && ((ImpactTableModel)getModel()).isLostEntailment(row)){
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				
			} else {
				setCursor(null);
			}
		}
		
	});
	
	addMouseListener(new MouseAdapter() {
		final ImpactTable table;
		{
			table = ImpactTable.this;
		}

		public void mouseClicked(MouseEvent e) {
			int row = rowAtPoint(e.getPoint());
			int column = columnAtPoint(e.getPoint());
			
			if(row >= 0 && row <= table.getRowCount() && column == 2 && ((ImpactTableModel)getModel()).isLostEntailment(row)){
				((ImpactTableModel)table.getModel()).addToRepairPlan(rowAtPoint(e.getPoint()));
				setCursor(null);
			}
		}
		
	
			public void mousePressed(MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				if (row >= 0 && row < getRowCount() && e.isPopupTrigger()) {
					showPopupMenu(e);
					
				}

			}

			public void mouseReleased(MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				if (row >= 0 && row < getRowCount() && e.isPopupTrigger()) {
					
					showPopupMenu(e);
					
				}
			}

	
	
	
	});
	
	}
	private void showPopupMenu(MouseEvent e){
		final int row = rowAtPoint(e.getPoint());
		JPopupMenu menu = new JPopupMenu();
        menu.add(new AbstractAction("Why?") {
        	/**
			 * 
			 */
			private static final long serialVersionUID = 950445739098337169L;
			final ImpactTable table = ImpactTable.this;
            
            public void actionPerformed(ActionEvent e){
            	
					
					OWLAxiom ax = (OWLAxiom)table.getValueAt(row, 1);
					showWhy(ax);
            	
                
            }

            
        });
        menu.show(this, e.getX(), e.getY());
	}
	
	private void showWhy(OWLAxiom entailment){
		try {
			OREManager oreMan = OREManager.getInstance();
			RepairManager repMan = RepairManager.getInstance(oreMan);
			ExplanationManager expMan = ExplanationManager.getInstance(oreMan);
			OWLOntologyManager man = oreMan.getReasoner().getOWLOntologyManager();
			List<OWLOntologyChange>repairPlan = repMan.getRepairPlan();
			
			StringBuilder sb = new StringBuilder();
			sb.append(ManchesterSyntaxRenderer.renderSimple(entailment));
			if(((ImpactTableModel)getModel()).isLostEntailment(entailment)){
				sb.append(" is lost because");
				new ExplanationDialog(sb.toString(), expMan.getEntailmentExplanations(entailment));
			} else {
				sb.append(" is added because");
				man.applyChanges(repairPlan);
				new ExplanationDialog(sb.toString(), expMan.getEntailmentExplanations(entailment));
				man.applyChanges(repMan.getInverseChanges(repairPlan));
			}
		
		} catch (OWLOntologyChangeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private class ExplanationDialog extends JDialog{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private JComponent explanationsPanel;
		
		public ExplanationDialog(String title, Set<Explanation> explanations){
			super(TaskManager.getInstance().getDialog(), title, true);
			setLayout(new BorderLayout());
			
			explanationsPanel = new Box(1);
			
			int counter = 1;
			for(Explanation exp : explanations){
				ExplanationTablePanel panel = new ExplanationTablePanel(new SimpleExplanationTable(exp), counter);
				explanationsPanel.add(panel);
			}
			add(explanationsPanel, BorderLayout.NORTH);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setSize(700, 400);
			
			setVisible(true);
			
			

		}
	}
	
	

	
}
