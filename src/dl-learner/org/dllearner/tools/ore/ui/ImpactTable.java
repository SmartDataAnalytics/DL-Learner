package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.jdesktop.swingx.JXTable;

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
	    getColumnModel().getColumn(1).setCellRenderer(new OWLSyntaxTableCellRenderer());
	    setRowHeight(getRowHeight() + 5);
	    getColumn(0).setMaxWidth(50);
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
	});
	
	}
	
}
