package org.dllearner.tools.evaluationplugin;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

public class RadioButtonRenderer extends JRadioButton implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -688293293192402900L;


	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		setSelected((Boolean)value);
		setHorizontalAlignment(SwingConstants.CENTER);
		setBackground(Color.WHITE);
		return this;
	}
}
