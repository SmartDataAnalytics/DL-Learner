package org.dllearner.tools.ore.ui;

import java.awt.Color;

import org.jdesktop.swingx.JXTable;

public class ImpactTable extends JXTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4146762679753151490L;

	public ImpactTable(){
		super(new ImpactTableModel());
	        
		setBackground(Color.WHITE);
	    setShowHorizontalLines(true);
	    setGridColor(Color.LIGHT_GRAY);
	    setTableHeader(null);
	    getColumnModel().getColumn(1).setCellRenderer(new OWLSyntaxTableCellRenderer());
	    setRowHeight(getRowHeight() + 4);
	    getColumn(0).setMaxWidth(30);
	    
	}
	
	
	
}
