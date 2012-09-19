package org.dllearner.tools.evaluationplugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class RadioButtonEditor extends AbstractCellEditor implements TableCellEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1124942535574963403L;

	private final JRadioButton radioButton;
	/**
	 * The delegate class which handles all methods sent from the
	 * <code>CellEditor</code>.
	 */
	private EditorDelegate delegate;

	public RadioButtonEditor() {
		radioButton = new JRadioButton();
		radioButton.setHorizontalAlignment(SwingConstants.CENTER);
		radioButton.setBackground(Color.WHITE);

		delegate = new EditorDelegate() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -1736759123580734984L;

			@Override
			public void setValue(Object value) {
				boolean selected = false;
				if (value instanceof Boolean) {
					selected = ((Boolean) value).booleanValue();
				} else if (value instanceof String) {
					selected = value.equals("true");
				}
				radioButton.setSelected(selected);
			}

			@Override
			public Object getCellEditorValue() {
				return Boolean.valueOf(radioButton.isSelected());
			}
		};
		radioButton.addActionListener(delegate);
		radioButton.setRequestFocusEnabled(false);
	}

	/**
	 * Returns a reference to the editor component.
	 * 
	 * @return the editor <code>Component</code>
	 */
	public Component getComponent() {
		return radioButton;
	}



	//
	// Override the implementations of the superclass, forwarding all methods
	// from the CellEditor interface to our delegate.
	//

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#getCellEditorValue
	 */
	public Object getCellEditorValue() {
		return delegate.getCellEditorValue();
	}


	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#shouldSelectCell(EventObject)
	 */
	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return delegate.shouldSelectCell(anEvent);
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#stopCellEditing
	 */
	@Override
	public boolean stopCellEditing() {
		return delegate.stopCellEditing();
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#cancelCellEditing
	 */
	@Override
	public void cancelCellEditing() {
		delegate.cancelCellEditing();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		delegate.setValue(value);
		// in order to avoid a "flashing" effect when clicking a radiobutton
		// in a table, it is important for the editor to have as a border
		// the same border that the renderer has, and have as the background
		// the same color as the renderer has. This is primarily only
		// needed for JRadioButton since this editor doesn't fill all the
		// visual space of the table cell, unlike a text field.
		TableCellRenderer renderer = table.getCellRenderer(row, column);
		Component c = renderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
		if (c != null) {
			radioButton.setOpaque(true);
			radioButton.setBackground(c.getBackground());
			if (c instanceof JComponent) {
				radioButton.setBorder(((JComponent) c).getBorder());
			}
		} else {
			radioButton.setOpaque(false);
		}
		return radioButton;
	}

	protected class EditorDelegate implements ActionListener, ItemListener, Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3881040593494915068L;
		/** The value of this cell. */
		protected Object value;

		/**
		 * Returns the value of this cell.
		 * 
		 * @return the value of this cell
		 */
		public Object getCellEditorValue() {
			return value;
		}

		/**
		 * Sets the value of this cell.
		 * 
		 * @param value
		 *            the new value of this cell
		 */
		public void setValue(Object value) {
			this.value = value;
		}


		/**
		 * Returns true to indicate that the editing cell may be selected.
		 * 
		 * @param anEvent
		 *            the event
		 * @return true
		 * @see #isCellEditable
		 */
		public boolean shouldSelectCell(EventObject anEvent) {
			return true;
		}

		/**
		 * Returns true to indicate that editing has begun.
		 * 
		 * @param anEvent
		 *            the event
		 */
		public boolean startCellEditing(EventObject anEvent) {
			return true;
		}

		/**
		 * Stops editing and returns true to indicate that editing has stopped.
		 * This method calls <code>fireEditingStopped</code>.
		 * 
		 * @return true
		 */
		public boolean stopCellEditing() {
			fireEditingStopped();
			return true;
		}

		/**
		 * Cancels editing. This method calls <code>fireEditingCanceled</code>.
		 */
		public void cancelCellEditing() {
			fireEditingCanceled();
		}

		/**
		 * When an action is performed, editing is ended.
		 * 
		 * @param e
		 *            the action event
		 * @see #stopCellEditing
		 */
		public void actionPerformed(ActionEvent e) {
			RadioButtonEditor.this.stopCellEditing();
		}

		/**
		 * When an item's state changes, editing is ended.
		 * 
		 * @param e
		 *            the action event
		 * @see #stopCellEditing
		 */
		public void itemStateChanged(ItemEvent e) {
			RadioButtonEditor.this.stopCellEditing();
		}
	}
}
