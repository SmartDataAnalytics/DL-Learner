package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;


public class ConceptPanel extends JPanel{



	private javax.swing.JList conceptList;
	
	private JPanel contentPanel;
	
	private DefaultListModel model;
	private JXBusyLabel loadingLabel;	
	private JLabel statusLabel;
	
	public ConceptPanel() {
		
		super();
		
		model = new DefaultListModel();
		loadingLabel = new JXBusyLabel(new Dimension(24,24));
		statusLabel = new JLabel();
		
		BusyPainter<?> painter = new BusyPainter(
		new RoundRectangle2D.Float(0, 0,10.0f,3.2f,10.0f,10.0f),
		new Ellipse2D.Float(3.5f,3.5f,17.0f,17.0f));
		painter.setTrailLength(4);
		painter.setPoints(8);
		painter.setFrame(-1);
		loadingLabel.setPreferredSize(new Dimension(24,24));
		loadingLabel.setIcon(new EmptyIcon(24,24));
		loadingLabel.setBusyPainter(painter);

		
		JPanel labelPanel = new JPanel();
		labelPanel.add(statusLabel);
		labelPanel.add(loadingLabel);
		
		contentPanel = getContentPanel();
		setLayout(new java.awt.BorderLayout());
		add(contentPanel,BorderLayout.CENTER);
		add(labelPanel, BorderLayout.SOUTH);
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
    
   
	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public JXBusyLabel getLoadingLabel() {
		return loadingLabel;
	}
    
//    class BlinkLabel extends JLabel implements Runnable{
//    	private boolean blinking = false;
//    	    	
//    	public void start(){
//    		blinking = true;
//    		new Thread(this).start();
//    	}
//    	public void stop(){
//    		blinking = false;
//    		setForeground(Color.black);
//    		
//    	}
//    	public void run(){
//    		while(blinking){
//    			setText("Loading Concept");
//    			
//    			//setForeground(Color.red);
//    			try {
//					Thread.sleep(300);
//				} catch (InterruptedException e) {
//					return;
//				}
//				setText("");
//				
//    		}
//    		if(!blinking)
//    			setText("Done! Select Concept and press 'Next'");
//    	}
//    }
 


	

}