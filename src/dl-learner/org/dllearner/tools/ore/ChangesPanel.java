package org.dllearner.tools.ore;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class ChangesPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7538532926820669891L;

	public ChangesPanel(){
		super(new GridLayout(0, 1));
		
	}
	
	public void init(){
		setBorder(new TitledBorder("changes"));
	}
	
	
}
