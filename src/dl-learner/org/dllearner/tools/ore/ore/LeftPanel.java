package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class LeftPanel extends JPanel{

	private JLabel[] jLabel;
	
	public LeftPanel(int i){
		
		jLabel = new JLabel[5];
		setBackground(new java.awt.Color(255, 255, 255));
    	JPanel panel2 = new JPanel();
    	panel2.setBackground(new java.awt.Color(255, 255, 255));
    	panel2.setLayout(new GridLayout(5,1,0,10));
    	jLabel[0] = new JLabel("1. Introduction");
		jLabel[1] = new JLabel("2. Knowledge Source");
		jLabel[2] = new JLabel("3. Concept");
		jLabel[3] = new JLabel("4. Examples");
		jLabel[4] = new JLabel("5. Learning");
		
		jLabel[i].setFont(jLabel[i].getFont().deriveFont(Font.BOLD));
		
		for(JLabel current : jLabel)
			panel2.add(current);		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(140,500));
		add(panel2, BorderLayout.NORTH);
		
	}
	
	public void set(int i){
		removeAll();
	
		setBackground(new java.awt.Color(255, 255, 255));
    	JPanel panel2 = new JPanel();
    	panel2.setBackground(new java.awt.Color(255, 255, 255));
    	panel2.setLayout(new GridLayout(5,1,0,10));
    	jLabel[0] = new JLabel("1. Introduction");
		jLabel[1] = new JLabel("2. Knowledge Source");
		jLabel[2] = new JLabel("3. Concept");
		jLabel[3] = new JLabel("4. Examples");
		jLabel[4] = new JLabel("5. Learning");
		
		jLabel[i].setFont(jLabel[i].getFont().deriveFont(Font.BOLD));
		
		for(JLabel current : jLabel)
			panel2.add(current);		
		setLayout(new BorderLayout());
		add(panel2, BorderLayout.NORTH);
	}
	
}
