package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.dllearner.core.owl.Description;

public class ColumnListCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 3024913291199515567L;
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
		setLayout(new GridBagLayout());
		desc.setText(((Description) value).toManchesterSyntaxString(
				ore.getBaseURI(), ore.getPrefixes()));
		cor.setText((ore.getCorrectness((Description) value)).toString());
		add(cor,new GridBagConstraints(0, 0, 1, 1, 0.1, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));//, BorderLayout.WEST);
		add(desc,new GridBagConstraints(1, 0, 1, 1, 0.8, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));//, BorderLayout.EAST);

		Color background;
		Color foreground;
		
		if (index % 2 == 0 && !isSelected) {
			background = new Color(242, 242, 242);
			foreground = Color.BLACK;

		} else if(isSelected){
		 
             background = Color.LIGHT_GRAY;
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