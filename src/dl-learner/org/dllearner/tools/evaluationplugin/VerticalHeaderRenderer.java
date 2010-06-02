package org.dllearner.tools.evaluationplugin;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;

public class VerticalHeaderRenderer extends DefaultTableCellRenderer {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -877134005972107198L;

	@Override
	public Component getTableCellRendererComponent (JTable table,
            Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        
        JLabel label = new JLabel ();
        Icon icon = VerticalCaption.getVerticalCaption (label, value.toString (), false);
        label.setIcon (icon);
        label.setHorizontalAlignment (SwingConstants.CENTER);
        label.setBorder (new BevelBorder (BevelBorder.RAISED));
        if(!((EvaluationTable)table).isAllColumnsEnabled()){
			if(column >= 2 && column <= 4){
				label.setEnabled(false);
			}
		} else {
			label.setEnabled(true);
		}
        return label;
    }
    
}
