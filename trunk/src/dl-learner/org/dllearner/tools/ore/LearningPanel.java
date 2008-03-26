package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class LearningPanel extends JPanel{



	private javax.swing.JList conceptList;
	
	private JPanel contentPanel;
	
	private DefaultListModel model;
	private JLabel result;
	private JButton run;
	
	public LearningPanel() {
		
		super();
		model = new DefaultListModel();
		result = new JLabel();
		run = new JButton("Run");
		contentPanel = getContentPanel();
		setLayout(new java.awt.BorderLayout());
		add(run,BorderLayout.EAST);
		add(contentPanel,BorderLayout.CENTER);
		add(result, BorderLayout.SOUTH);
	}

	private JPanel getContentPanel() {
		
		JPanel contentPanel1 = new JPanel();
		JScrollPane scroll = new JScrollPane();
		
			
		conceptList = new JList(model);
		scroll.setSize(100,100);
		scroll.setViewportView(conceptList);
				
		contentPanel1.add(scroll);
		
		

		return contentPanel1;
	}
	
	public void addButtonListener(ActionListener a){
		run.addActionListener(a);
	}
	
	public void setResult(String resultStr){
		result.setText(resultStr);
	}
	
	
	
	
	
}  
    
 


	

