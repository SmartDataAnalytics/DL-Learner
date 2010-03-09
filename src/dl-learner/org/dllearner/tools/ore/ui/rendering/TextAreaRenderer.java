package org.dllearner.tools.ore.ui.rendering;

import java.awt.Component;

import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.semanticweb.owl.model.OWLAxiom;

public class TextAreaRenderer extends JEditorPane implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -995751851182606053L;
	private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();


	public TextAreaRenderer() {
		setContentType("text/html");
	}

	public Component getTableCellRendererComponent(//
			JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
		// set the colors, etc. using the standard for that platform
		adaptee.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
		if(obj instanceof OWLAxiom){
			setText(ManchesterSyntaxRenderer.render((OWLAxiom)obj, false, 0));
		} else {
			setText(adaptee.getText());
		}	
		setForeground(adaptee.getForeground());
		setBackground(adaptee.getBackground());
		setBorder(adaptee.getBorder());
		setFont(adaptee.getFont());
		


		TableColumnModel columnModel = table.getColumnModel();
		setSize(columnModel.getColumn(column).getWidth(), 100000);
		int height_wanted = (int) getPreferredSize().getHeight();

		if (height_wanted != table.getRowHeight(row)) {
			table.setRowHeight(row, height_wanted);
		}
		return this;
	}

}
