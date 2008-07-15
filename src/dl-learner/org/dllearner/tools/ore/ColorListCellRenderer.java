package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;

public class ColorListCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -7592805113197759247L;
	private ORE ore;
	
	public ColorListCellRenderer(ORE ore) {
		setOpaque(true);
		this.ore = ore;
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		if(value instanceof NamedClass)
			setText(((NamedClass)value).toManchesterSyntaxString(ore.getBaseURI(), ore.getPrefixes()));
		else if(value instanceof Individual)
			setText(((Individual)value).toManchesterSyntaxString(ore.getBaseURI(), ore.getPrefixes()));
		
		Color background;
		Color foreground;
		
		if (index % 2 == 0 && !isSelected) {
			background = new Color(242, 242, 242);
			foreground = Color.BLACK;

		} else if(isSelected){
		 
             background = Color.LIGHT_GRAY;
             foreground = Color.BLACK;
        }else{
        	background = Color.WHITE;
            foreground = Color.BLACK;
        }
		
		setForeground(foreground);
		setBackground(background);

		return this;
	}

}
