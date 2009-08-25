package org.dllearner.tools.ore.ui;

import java.awt.Color;

import org.jdesktop.swingx.JXTable;

public class RepairTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -621497634521668635L;

	public RepairTable(){
		super(new RepairTableModel());
		setBackground(Color.WHITE);
	    setShowHorizontalLines(true);
	    setGridColor(Color.LIGHT_GRAY);
	    setTableHeader(null);
	    getColumnModel().getColumn(0).setCellRenderer(new RepairTableCellRenderer());
	    
	    setRowHeight(getRowHeight() + 4);
	    getColumn(0).setMaxWidth(20);
	    
	}
}
