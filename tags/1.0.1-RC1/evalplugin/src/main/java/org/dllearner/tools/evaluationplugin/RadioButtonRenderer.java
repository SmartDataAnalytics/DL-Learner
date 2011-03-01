package org.dllearner.tools.evaluationplugin;

import java.awt.Component;

import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class RadioButtonRenderer extends JRadioButton implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -688293293192402900L;
	private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	

	public RadioButtonRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.CENTER);
		setBorderPainted(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		if (isSelected) {
			setForeground(table.getSelectionForeground());
			super.setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		setSelected((value != null && ((Boolean) value).booleanValue()));

		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		} else {
			setBorder(noFocusBorder);
		}
		if(!((EvaluationTable)table).isAllColumnsEnabled()){
			if(column >= 2 && column <= 4){
				setEnabled(false);
			}
		} else {
			setEnabled(true);
		}

		return this;
	}
}
