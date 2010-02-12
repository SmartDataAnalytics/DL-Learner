package org.dllearner.tools.evaluationplugin;

import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

public class RadioButtonEditor extends AbstractCellEditor implements TableCellEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1124942535574963403L;

	private JRadioButton button;
	
	public RadioButtonEditor(){
		button = new JRadioButton();
		button.setHorizontalAlignment(SwingConstants.CENTER);
		button.setBackground(Color.WHITE);
	}
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		button.setSelected((Boolean)value);
		return button;
	}
	
	

	@Override
	public Object getCellEditorValue() {
		return button.isSelected();
	}
}
