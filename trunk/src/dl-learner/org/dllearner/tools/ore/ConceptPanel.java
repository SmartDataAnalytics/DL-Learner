package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

public class ConceptPanel extends JPanel{



	private javax.swing.JList conceptList;
	
	private JPanel contentPanel;
	
	private DefaultListModel model;
	private BlinkLabel blink;	
	
	public ConceptPanel() {
		
		super();
		model = new DefaultListModel();
		blink = new BlinkLabel();
		blink.setText("Loading Concepts");
		contentPanel = getContentPanel();
		setLayout(new java.awt.BorderLayout());
		add(contentPanel,BorderLayout.CENTER);
		add(blink, BorderLayout.SOUTH);
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
	
	public DefaultListModel getModel(){
		return model;
	}
	
	public void setModel(DefaultListModel dm){
		conceptList.setModel(dm);
	}
	
	public void addSelectionListener(ListSelectionListener l){
		conceptList.addListSelectionListener(l);
	}
          
    public JList getList(){
    	return conceptList;
    }
    
    public BlinkLabel getBlinkLabel(){
    	return blink;
    }
    
    class BlinkLabel extends JLabel implements Runnable{
    	private boolean blinking = false;
    	
    	public void start(){
    		blinking = true;
    		new Thread(this).start();
    	}
    	public void stop(){
    		blinking = false;
    		setText("Done! Select Concept and press 'Next'");
    	}
    	public void run(){
    		while(blinking){
    			setForeground(Color.red);
    			try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return;
				}
				setForeground(Color.black);
    		}
    	}
    }
 


	

}