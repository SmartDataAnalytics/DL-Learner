package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ColorListCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -7592805113197759247L;

	public ColorListCellRenderer() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		setText(value.toString());
		Color background;
		Color foreground;
		
		if (index % 2 == 0 && !isSelected) {
			background = new Color(242, 242, 242);
			foreground = Color.BLACK;

		} else if(isSelected){
		 
             background = Color.BLUE;
             foreground = Color.WHITE;
        }else{
        	background = Color.WHITE;
            foreground = Color.BLACK;
        }
		
		setForeground(foreground);
		setBackground(background);

		return this;
	}

}
