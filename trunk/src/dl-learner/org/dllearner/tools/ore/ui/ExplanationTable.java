package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.RepairManagerListener;
import org.dllearner.tools.ore.explanation.Explanation;
import org.jdesktop.swingx.JXTable;
import org.protege.editor.core.Disposable;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntologyChange;

public class ExplanationTable extends JXTable implements RepairManagerListener, Disposable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5580730282611559609L;
	
	private RepairManager repMan;
	
	protected String[] columnToolTips = {
		    null, 
		    "The number of already computed explanations where the axiom occurs.",
		    "TODO",
		    "TODO",
		    "If checked, the axiom is selected to remove from the ontology.",
		    "Edit the axiom."
		};

	
	public ExplanationTable(Explanation exp, OWLClass cl) {
		
		repMan = RepairManager.getInstance(OREManager.getInstance());
		
		repMan.addListener(this);
		setBackground(Color.WHITE);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setModel(new ExplanationTableModel(exp,	cl));
		TableColumn column6 = getColumn(5);
		column6.setCellRenderer(new ButtonCellRenderer());
		column6.setCellEditor(new ButtonCellEditor());
		column6.setResizable(false);
		setRowHeight(getRowHeight() + 4);
		setRowHeightEnabled(true);
		getColumn(0).setCellRenderer(new MultiLineTableCellRenderer());
		getColumn(1).setMaxWidth(60);
		getColumn(2).setMaxWidth(60);
		getColumn(3).setMaxWidth(60);
		getColumn(4).setMaxWidth(30);
		getColumn(5).setMaxWidth(30);
		getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					final ExplanationTable table;

					@Override
					public void valueChanged(ListSelectionEvent e) {

						table.changeSelection();

					}

					{
						table = ExplanationTable.this;

					}

				});

		addFocusListener(new FocusListener() {

			final ExplanationTable table;

			public void focusGained(FocusEvent focusevent) {
			}

			public void focusLost(FocusEvent e) {
				table.clearSelection();
				table.changeSelection();

			}

			{

				table = ExplanationTable.this;

			}
		});

		addMouseListener(new MouseAdapter() {

			final ExplanationTable table;
			{
				table = ExplanationTable.this;
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					System.out.println(getValueAt(table
							.rowAtPoint(e.getPoint()), 0));
				}
			}
		});
	}
	
	@Override
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
            /**
			 * 
			 */
			private static final long serialVersionUID = -3386641672808329591L;

			public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex = 
                        columnModel.getColumn(index).getModelIndex();
                return columnToolTips[realIndex];
            }
        };

	}
	
	public void strikeOut(boolean strikeOut){
		((ExplanationTableModel)getModel()).setStriked(strikeOut);
	}
	
	private void changeSelection() {

	}
	
	class ButtonCellRenderer extends JButton implements TableCellRenderer{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1962950956976967243L;

		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
				if (isSelected) {
					setForeground(table.getSelectionForeground());
					setBackground(table.getSelectionBackground());
				} else {
					setForeground(table.getForeground());
					setBackground(UIManager.getColor("Button.background"));
				}
				setIcon(new ImageIcon("src/dl-learner/org/dllearner/tools/ore/Edit16.gif"));
				setText("");
				return this;
				}
				 
		
	}
	
	class ButtonCellEditor extends AbstractCellEditor implements 
			TableCellEditor, ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9017452102605141646L;
		JTable table;
		JButton editButton;
		String text;
		

		public ButtonCellEditor() {
			super();
			
			editButton = new JButton();
			editButton.setFocusPainted(false);
			editButton.addActionListener(this);
		}

		
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			text = (value == null) ? "" : value.toString();
			editButton.setText("");
			return editButton;
		}

		@Override
		public Object getCellEditorValue() {
			return text;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEditingStopped();
		}
	}

	@Override
	public void repairPlanExecuted(List<OWLOntologyChange> changes) {
		repaint();	
	}

	@Override
	public void repairPlanChanged() {
		repaint();
	}

	@Override
	public void dispose() throws Exception {
		repMan.removeListener(this);
	}
	

	

	

}
