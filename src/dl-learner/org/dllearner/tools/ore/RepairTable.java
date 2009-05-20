package org.dllearner.tools.ore;

import java.awt.Color;

import org.jdesktop.swingx.JXTable;

public class RepairTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -621497634521668635L;

	public RepairTable(ImpactManager manager){
		super(new RepairTableModel(manager));
	        
	    setShowHorizontalLines(true);
	    setGridColor(Color.LIGHT_GRAY);
	    setTableHeader(null);
	    getColumnModel().getColumn(0).setCellRenderer(new OWLSyntaxTableCellRenderer());
	    setRowHeight(getRowHeight() + 4);
	    
	}
}
