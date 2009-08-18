package org.dllearner.tools.ore.ui;

import java.awt.Color;

import org.dllearner.tools.ore.ImpactManager;
import org.jdesktop.swingx.JXTable;

public class ImpactTable extends JXTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4146762679753151490L;

	public ImpactTable(ImpactManager manager){
		super(new ImpactTableModel(manager));
	        
		setBackground(Color.WHITE);
	    setShowHorizontalLines(true);
	    setGridColor(Color.LIGHT_GRAY);
	    setTableHeader(null);
	    getColumnModel().getColumn(0).setCellRenderer(new OWLSyntaxTableCellRenderer());
	    setRowHeight(getRowHeight() + 4);
	    
	}
	
	
	
}
