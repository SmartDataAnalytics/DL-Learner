package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.OREManager;
import org.jdesktop.swingx.JXTable;
import org.semanticweb.owlapi.model.OWLClass;

public class UnsatisfiableClassesTable extends JXTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 59201134390657458L;
	
	public UnsatisfiableClassesTable(){
		ExplanationManager expMan = ExplanationManager.getInstance(OREManager.getInstance());
		setBackground(Color.WHITE);
		setModel(new UnsatisfiableClassesTableModel());
		setEditable(false);
		setTableHeader(null);
		setGridColor(Color.LIGHT_GRAY);
		getColumn(0).setMaxWidth(20);
		setRowHeight(getRowHeight() + 2);
		getColumn(0).setCellRenderer(new UnsatClassesTableCellRenderer(expMan));
		
//		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

//		addMouseListener(new MouseAdapter() {
//			public void mousePressed(MouseEvent e) {
//				int row = rowAtPoint(e.getPoint());
//				if (row >= 0 && row < getRowCount() && e.isPopupTrigger()) {
//					OWLClass cl = (OWLClass) getValueAt(row, 1);
//					if (ExplanationManager
//							.getInstance(OREManager.getInstance())
//							.getDerivedClasses().contains(cl)) {
//						showPopupMenu(e);
//					}
//				}
//
//			}
//
//			public void mouseReleased(MouseEvent e) {
//				int row = rowAtPoint(e.getPoint());
//				if (row >= 0 && row < getRowCount() && e.isPopupTrigger()) {
//					OWLClass cl = (OWLClass) getValueAt(row, 1);
//					if (ExplanationManager
//							.getInstance(OREManager.getInstance())
//							.getDerivedClasses().contains(cl)) {
//						showPopupMenu(e);
//					}
//				}
//			}
//
//		});
	}
	
//	private void showPopupMenu(MouseEvent e){
//		JPopupMenu menu = new JPopupMenu();
//        menu.add(new AbstractAction("Why is derived class?") {
//        	final UnsatisfiableClassesTable table = UnsatisfiableClassesTable.this;
//            
//            public void actionPerformed(ActionEvent e)
//            {
//                
//            }
//
//            
//        });
//        menu.show(this, e.getX(), e.getY());
//	}
	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
	    Dimension size = super.getPreferredScrollableViewportSize();
	    return new Dimension(Math.min(getPreferredSize().width, size.width), size.height);
	}

	
	public void addUnsatClasses(List<OWLClass> unsatClasses){
		((UnsatisfiableClassesTableModel)getModel()).addUnsatClasses(unsatClasses);	
	}
	
	public OWLClass getSelectedClass(){
		return (OWLClass)((UnsatisfiableClassesTableModel)getModel()).getValueAt(getSelectedRow(), 0);
	}
	
	public List<OWLClass> getSelectedClasses(){
		List<OWLClass> selectedClasses = new ArrayList<OWLClass>(getSelectedRows().length);
		int[] rows = getSelectedRows();
		for(int i = 0; i < rows.length; i++){
			selectedClasses.add(((UnsatisfiableClassesTableModel)getModel()).getClassAt(rows[i]));
		}
		
		return selectedClasses;
	}
	
	@Override
	public String getToolTipText(MouseEvent e){
		String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        if(rowIndex != -1){
        	tip = ((OWLClass)getValueAt(rowIndex, 0)).getIRI().toString();
        	
        } else {
        	tip = super.getToolTipText(e);
        }
        return tip;
	}
	
	public void clear(){
		((UnsatisfiableClassesTableModel)getModel()).clear();
	}

}
