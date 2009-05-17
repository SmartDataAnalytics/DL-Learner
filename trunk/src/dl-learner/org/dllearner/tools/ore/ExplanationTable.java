package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.JXTable;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;

public class ExplanationTable extends JXTable implements ImpactManagerListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5580730282611559609L;
	
	private List<OWLAxiom> explanation;
	private ImpactManager impMan;

	public ExplanationTable(List<OWLAxiom> explanation, ImpactManager impMan, ExplanationManager expMan, OWLClass cl) {
		this.explanation = explanation;
		this.impMan = impMan;
		setBackground(Color.WHITE);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setModel(new ExplanationTableModel(explanation, expMan, impMan, cl));
		getColumn(0).setCellRenderer(new OWLSyntaxTableCellRenderer(impMan));
		TableColumn column4 = getColumn(3);
		column4.setCellRenderer(new ButtonCellRenderer());
		column4.setCellEditor(new ButtonCellEditor());
		column4.setResizable(false);
		setColumnSizes();
		getSelectionModel().addListSelectionListener(new ListSelectionListener(){

			
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
			
            public void focusGained(FocusEvent focusevent)
            {
            }

            public void focusLost(FocusEvent e)
            {
                table.clearSelection();
                table.changeSelection();
                
            }

            

            
            {
            	
                table = ExplanationTable.this;
                
            }
        });
	}
	
	private OWLAxiom getOWLAxiomAtRow(int rowIndex){
		return ((ExplanationTableModel)getModel()).getOWLAxiomAtRow(rowIndex);
	}
	
	private void setColumnSizes(){
		getColumn(1).setMaxWidth(30);
		getColumn(2).setMaxWidth(30);
		getColumn(3).setMaxWidth(80);
	}
	
	private void changeSelection() {

		
		if(getSelectedRow() >=0){
			OWLAxiom rowAxiom = getOWLAxiomAtRow(getSelectedRow());
			impMan.setActualAxiom(rowAxiom);
		} else {
			impMan.setActualAxiom(null);
		}

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
				setText( (value ==null) ? "" : value.toString() );
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
			editButton.setText(text);
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
	public void axiomForImpactChanged() {
		repaint();
		
	}
	

	

}
