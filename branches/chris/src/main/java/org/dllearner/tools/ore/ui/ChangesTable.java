package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.dllearner.tools.ore.ui.rendering.ManchesterSyntaxTableCellRenderer;
import org.jdesktop.swingx.JXTable;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class ChangesTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1137642531002327026L;

	public ChangesTable(){
		setBackground(Color.WHITE);
		setModel(new ChangesTableModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setEditable(false);
		setTableHeader(null);
		setGridColor(Color.LIGHT_GRAY);
		setRowHeight(getRowHeight() + 5);
		getColumn(0).setMaxWidth(20);
		getColumn(2).setMaxWidth(40);
		setShowGrid(false);
		getColumn(1).setCellRenderer(new ManchesterSyntaxTableCellRenderer());
		getColumn(2).setCellRenderer(new UndoCellRenderer());
		
		addKeyListener(new KeyAdapter() {

            @Override
			public void keyPressed(KeyEvent e)
            {
                handleKeyPressed(e);
            }

           
        });
		
		addMouseMotionListener(new MouseAdapter() {

			final ChangesTable table;
			{
				table = ChangesTable.this;
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				int column = columnAtPoint(e.getPoint());
				
				if(column == 2 && row <= table.getRowCount() && row >= 0){
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					
				} else {
					setCursor(null);
				}
			}
			
		});
		
		addMouseListener(new MouseAdapter() {
			final ChangesTable table;
			{
				table = ChangesTable.this;
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				int column = columnAtPoint(e.getPoint());
				
				if(row >= 0 && row <= table.getRowCount() && column == 2){
					((ChangesTableModel)table.getModel()).removeChange(rowAtPoint(e.getPoint()));
					setCursor(null);
				}
			}
		});
	}
	
	private void handleKeyPressed(KeyEvent e)
    {
        int selRow = getSelectedRow();
        if(e.getKeyCode() == KeyEvent.VK_DELETE){
        	((ChangesTableModel)getModel()).removeChange(selRow);
        }
     
        getSelectionModel().clearSelection();
    }
	
	public void addChanges(List<OWLOntologyChange> changes){
		((ChangesTableModel)getModel()).addChanges(changes);
	}
	
	public void removeChanges(List<OWLOntologyChange> changes){
		((ChangesTableModel)getModel()).removeChanges(changes);
	}
	
	public void clear(){
		((ChangesTableModel)getModel()).clear();
	}
	
	public List<OWLOntologyChange> getChanges(){
		return ((ChangesTableModel)getModel()).getChanges();
	}
	
	
	class UndoCellRenderer extends DefaultTableCellRenderer{


		/**
		 * 
		 */
		private static final long serialVersionUID = 6941030561135280273L;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if(value instanceof LinkLabel){
				return (LinkLabel)value;
			}
			
			return this;
		}
		
	}
}


