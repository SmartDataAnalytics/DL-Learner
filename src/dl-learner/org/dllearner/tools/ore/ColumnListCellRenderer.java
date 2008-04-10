package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.dllearner.core.owl.Description;

public class ColumnListCellRenderer extends JPanel implements ListCellRenderer {

	private ORE ore;
	
	
	public ColumnListCellRenderer(ORE ore) {
		this.ore = ore;
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		removeAll();
		JLabel cor = new JLabel();
		JLabel desc = new JLabel();
		setLayout(new BorderLayout());
		
		
		desc.setText(value.toString());
		cor.setText( (ore.getCorrectness((Description)value)).toString());
		
		add(cor, BorderLayout.WEST);
		add(desc, BorderLayout.EAST);
		
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