package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dllearner.core.owl.NamedClass;

public class MoveDialog extends JDialog{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Box buttonBox;
	JButton okButton;
	JButton cancelButton;
	JPanel buttonPanel;
	JPanel listPanel;
	JScrollPane scroll;
	JList conceptList;
	Set<NamedClass> allConcepts;
	NamedClass selectedValue;;
	
	
	public MoveDialog(Set<NamedClass> allConcepts, JDialog dialog){
		super(dialog, "Auswahl", true);
		this.allConcepts = allConcepts;
	
	}
	
	public void init(){
		setSize(500, 500);
		buttonBox = new Box(BoxLayout.X_AXIS);
		
		okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("Ok")){
					
					selectedValue = (NamedClass)conceptList.getSelectedValue();
					
					dispose();
				}
				
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("Cancel")){
					
						
					dispose();
				}
				
			}
		});
	
		buttonBox.add(okButton);
		buttonBox.add(Box.createHorizontalStrut(30));
		buttonBox.add(cancelButton);
	
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);
	
		listPanel = new JPanel();
		DefaultListModel model = new DefaultListModel();
		for (NamedClass cl : allConcepts) {
			model.addElement(cl);
				
		}
	
		scroll = new JScrollPane();
		conceptList = new JList(model);
		scroll.setPreferredSize(new Dimension(400, 400));
		scroll.setViewportView(conceptList);
		listPanel.add(scroll);
	
		getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
		getContentPane().add(listPanel, java.awt.BorderLayout.CENTER);
     
		setVisible(true);

	}
	
	public NamedClass getSelectedValue(){
		return selectedValue;
	}
	
}
