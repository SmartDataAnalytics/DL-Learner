package org.dllearner.tools.ore.ui;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.jdesktop.swingx.JXTable;

public class MarkableClassExpressionsTable extends JXTable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4193878042914394758L;
	private ImageIcon icon = new ImageIcon("src/dl-learner/org/dllearner/tools/ore/untoggled.gif");
	
	public MarkableClassExpressionsTable(){
		super(new MarkableClassExpressionsTableModel());
//		getColumn(1).setCellRenderer(new ManchesterSyntaxTableCellRenderer());
		getColumn(0).setMaxWidth(30);
		setTableHeader(null);
		setBorder(null);
		setShowVerticalLines(false);
		setShowHorizontalLines(false);
		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(false);
		getColumn(0).setCellRenderer(new TableCellRenderer() {
			
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				if(value.equals(">")){
					return new JLabel(icon);
				} else {
					return new JLabel("");
				}
			}
		});
	}
	
	@Override
	public String getToolTipText(MouseEvent e){
		String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        if(rowIndex != -1){
        	tip = getValueAt(rowIndex, 1).toString();
        	
        } else {
        	tip = super.getToolTipText(e);
        }
        return tip;
	}
	
	public void clear(){
		((MarkableClassExpressionsTableModel)getModel()).clear();
	}
	
}
