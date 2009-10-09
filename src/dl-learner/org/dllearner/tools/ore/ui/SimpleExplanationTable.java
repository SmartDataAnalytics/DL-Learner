package org.dllearner.tools.ore.ui;

import java.awt.Color;

import javax.swing.JTable;

import org.dllearner.tools.ore.explanation.Explanation;
import org.dllearner.tools.ore.ui.rendering.TextAreaRenderer;
import org.jdesktop.swingx.JXTable;

public class SimpleExplanationTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5258296616232184974L;

	
	public SimpleExplanationTable(Explanation exp){	
		setBackground(Color.WHITE);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setModel(new SimpleExplanationTableModel(exp));
		getColumn(0).setCellRenderer(new TextAreaRenderer());
		setRowHeightEnabled(true);
	}
}
