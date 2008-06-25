package org.dllearner.tools.ore;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

public class ChangesPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7538532926820669891L;

	public ChangesPanel(){
		super(new GridLayout(0, 1));
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
	}
	
	public void init(){
//		setBorder(new TitledBorder("changes"));
	}
	
	public void updatePanel(Container cont){
		remove(cont);
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	
}
